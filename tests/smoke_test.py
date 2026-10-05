#!/usr/bin/env python3
"""
CraftHinge end-to-end smoke test (Python 3 standard library only).

Plays all three roles through one full order, using the demo accounts:
  customer orders -> restaurant accepts/prepares -> driver picks up/delivers
  -> customer sees DELIVERED + notifications. Also checks a declined card.

  python3 tests/smoke_test.py                         # against docker compose (localhost:8080)
  python3 tests/smoke_test.py https://dev.crafthinge.example.com   # against AKS later

Exit code 0 = everything worked, 1 = a step failed (usable as a CI gate).
"""
import json
import sys
import time
import urllib.error
import urllib.request

BASE = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080").rstrip("/")
PASSWORD = "Password123!"
GOOD_CARD = "4242 4242 4242 4242"
DECLINED_CARD = "4000 0000 0000 0002"


def call(method, path, token=None, body=None, expect=(200, 201, 204)):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header("Accept", "application/json")
    if data is not None:
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(req, timeout=15) as res:
            status, raw = res.status, res.read()
    except urllib.error.HTTPError as e:
        status, raw = e.code, e.read()
    if status not in expect:
        raise AssertionError(f"{method} {path} -> {status}: {raw.decode(errors='replace')[:300]}")
    return json.loads(raw) if raw else None


def step(msg):
    print(f"  ✔ {msg}")


def login(email):
    return call("POST", "/api/auth/login", body={"email": email, "password": PASSWORD})["token"]


def wait_for_platform(timeout=180):
    print(f"Waiting for CraftHinge at {BASE} ...")
    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            call("GET", "/api/restaurants")
            call("POST", "/api/auth/login", body={"email": "customer@crafthinge.dev", "password": PASSWORD})
            return
        except Exception:
            time.sleep(3)
    raise AssertionError("Platform did not become ready in time")


def main():
    wait_for_platform()
    print("Running smoke test")

    customer, owner, driver = (login(f"{r}@crafthinge.dev") for r in ("customer", "owner", "driver"))
    step("all three demo users can log in (user-service issues JWTs)")

    restaurants = call("GET", "/api/restaurants")
    assert len(restaurants) >= 1
    restaurant = next(r for r in restaurants if r["name"].startswith("Tsuki"))
    menu = call("GET", f"/api/menu/restaurants/{restaurant['id']}/items")
    dish = next(m for m in menu if m["available"])
    step(f"browse without login: {len(restaurants)} restaurants, '{dish['name']}' on the menu")

    call("DELETE", "/api/cart", customer)
    cart = call("POST", "/api/cart/items", customer, {"menuItemId": dish["id"], "quantity": 2})
    assert cart["itemCount"] == 2 and float(cart["subtotal"]) == round(float(dish["price"]) * 2, 2)
    step(f"cart-service priced 2 x {dish['name']} via food-service: ${cart['subtotal']}")

    failed = call("POST", "/api/orders", customer, {"deliveryAddress": "1 Test St, Austin, TX", "cardNumber": DECLINED_CARD}, expect=(201,))
    assert failed["status"] == "PAYMENT_FAILED", failed["status"]
    assert call("GET", "/api/cart", customer)["itemCount"] == 2, "cart must survive a failed payment"
    step("declined card -> PAYMENT_FAILED, cart kept for retry")

    order = call("POST", "/api/orders", customer, {"deliveryAddress": "1 Test St, Austin, TX", "cardNumber": GOOD_CARD}, expect=(201,))
    assert order["status"] == "PLACED", order
    assert call("GET", "/api/cart", customer)["itemCount"] == 0, "cart should be emptied after a paid order"
    oid = order["id"]
    step(f"order #{oid} placed and paid (order -> cart, restaurant, payment services), total ${order['total']}")

    incoming = call("GET", f"/api/orders/restaurant/{restaurant['id']}", owner)
    assert any(o["id"] == oid for o in incoming)
    for status in ("ACCEPTED", "PREPARING", "READY_FOR_PICKUP"):
        call("PATCH", f"/api/orders/{oid}/status", owner, {"status": status})
    step("restaurant owner: ACCEPTED -> PREPARING -> READY_FOR_PICKUP")

    call("PATCH", f"/api/orders/{oid}/status", customer, {"status": "DELIVERED"}, expect=(403, 404, 409))
    step("customer is NOT allowed to mark the order delivered (state machine enforced)")

    delivery = None
    for _ in range(20):  # the READY_FOR_PICKUP event reaches delivery-service asynchronously
        delivery = next((d for d in call("GET", "/api/deliveries/available", driver) if d["orderId"] == oid), None)
        if delivery:
            break
        time.sleep(0.5)
    assert delivery, "delivery job was not created from the order event"
    step("ORDER_STATUS_CHANGED event created a delivery job in delivery-service")

    call("POST", f"/api/deliveries/{delivery['id']}/accept", driver)
    call("POST", f"/api/deliveries/{delivery['id']}/accept", driver, expect=(409,))
    call("POST", f"/api/deliveries/{delivery['id']}/pickup", driver)
    call("POST", f"/api/deliveries/{delivery['id']}/deliver", driver)
    step("driver: accept (second accept rejected) -> pickup -> deliver")

    final = call("GET", f"/api/orders/{oid}", customer)
    assert final["status"] == "DELIVERED", final["status"]
    assert [h["status"] for h in final["history"]][-1] == "DELIVERED"
    step(f"customer tracking shows DELIVERED with {len(final['history'])} timeline entries")

    notes = []
    for _ in range(10):
        notes = [n for n in call("GET", "/api/notifications/me", customer) if n["orderId"] == oid]
        if any("delivered" in n["message"].lower() for n in notes):
            break
        time.sleep(0.5)
    assert notes, "no notifications received"
    step(f"notification-service sent {len(notes)} updates for order #{oid}")

    print(f"\nSMOKE TEST PASSED against {BASE}")


if __name__ == "__main__":
    try:
        main()
    except AssertionError as e:
        print(f"\nSMOKE TEST FAILED: {e}")
        sys.exit(1)
