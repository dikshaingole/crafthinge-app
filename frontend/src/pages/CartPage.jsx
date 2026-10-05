import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, money } from '../api'
import { useCart } from '../cart'

export default function CartPage() {
  const { cart, setQuantity, refresh } = useCart()
  const navigate = useNavigate()
  const [address, setAddress] = useState('')
  const [card, setCard] = useState('4242 4242 4242 4242')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const placeOrder = async (e) => {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      const order = await api('/api/orders', { method: 'POST', body: { deliveryAddress: address, cardNumber: card } })
      if (order.status === 'PAYMENT_FAILED') {
        setError(`Payment didn’t go through: ${order.paymentMessage}. Check the card number and try again.`)
      } else {
        await refresh()
        navigate(`/orders/${order.id}`)
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (!cart.items.length) {
    return (
      <div className="page section narrow">
        <h1>Your cart is empty</h1>
        <p>Pick a restaurant and add a few dishes to get started.</p>
        <Link to="/restaurants" className="btn">Browse restaurants</Link>
      </div>
    )
  }

  return (
    <div className="page section checkout">
      <div>
        <h1>Your cart</h1>
        <ul className="cart-lines">
          {cart.items.map((l) => (
            <li key={l.menuItemId}>
              <span aria-hidden="true" className="menu-emoji">{l.emoji}</span>
              <span className="grow">{l.name}<br /><span className="muted small">{money(l.unitPrice)} each</span></span>
              <span className="qty">
                <button aria-label={`One less ${l.name}`} onClick={() => setQuantity(l.menuItemId, l.quantity - 1)}>−</button>
                <span aria-live="polite">{l.quantity}</span>
                <button aria-label={`One more ${l.name}`} onClick={() => setQuantity(l.menuItemId, l.quantity + 1)}>+</button>
              </span>
              <span className="price">{money(l.lineTotal)}</span>
            </li>
          ))}
        </ul>
        <p className="subtotal">Subtotal <strong>{money(cart.subtotal)}</strong></p>
        <p className="muted small">Delivery fee is added at checkout.</p>
      </div>

      <form className="panel" onSubmit={placeOrder}>
        <h2>Checkout</h2>
        <label>Delivery address
          <input required value={address} onChange={(e) => setAddress(e.target.value)} placeholder="123 Main St, Apt 4, Austin, TX" autoComplete="street-address" />
        </label>
        <label>Card number
          <input required value={card} onChange={(e) => setCard(e.target.value)} inputMode="numeric" autoComplete="cc-number" />
        </label>
        <p className="muted small">Test cards: 4242 4242 4242 4242 is approved, 4000 0000 0000 0002 is declined. No real payment is made.</p>
        {error && <p className="banner-error" role="alert">{error}</p>}
        <button className="btn btn-saffron btn-wide" disabled={busy}>{busy ? 'Placing order…' : 'Place order'}</button>
      </form>
    </div>
  )
}
