import { useCallback, useEffect, useState } from 'react'
import { api, money, STATUS_LABEL } from '../api'

// What the kitchen can do next for each status (mirrors OrderStatus.java rules).
const NEXT = {
  PLACED: [['ACCEPTED', 'Accept'], ['CANCELLED', 'Decline']],
  ACCEPTED: [['PREPARING', 'Start preparing']],
  PREPARING: [['READY_FOR_PICKUP', 'Ready for pickup']],
}
const BLANK = { name: '', description: '', category: '', price: '', emoji: '🍽️', vegetarian: false, spicy: false, available: true }

export default function OwnerDashboard() {
  const [restaurants, setRestaurants] = useState([])
  const [current, setCurrent] = useState(null)
  const [orders, setOrders] = useState([])
  const [menu, setMenu] = useState([])
  const [form, setForm] = useState(BLANK)
  const [error, setError] = useState('')

  useEffect(() => {
    api('/api/restaurants/mine').then((r) => { setRestaurants(r); setCurrent(r[0]?.id ?? null) }).catch((e) => setError(e.message))
  }, [])

  const load = useCallback(async () => {
    if (!current) return
    try {
      const [o, m] = await Promise.all([api(`/api/orders/restaurant/${current}`), api(`/api/menu/restaurants/${current}/items`)])
      setOrders(o)
      setMenu(m)
    } catch (e) { setError(e.message) }
  }, [current])

  useEffect(() => {
    load()
    const t = setInterval(load, 8000)
    return () => clearInterval(t)
  }, [load])

  const move = async (orderId, status) => {
    try { await api(`/api/orders/${orderId}/status`, { method: 'PATCH', body: { status } }); load() } catch (e) { setError(e.message) }
  }

  const addItem = async (e) => {
    e.preventDefault()
    try {
      await api(`/api/menu/restaurants/${current}/items`, { method: 'POST', body: { ...form, price: Number(form.price) } })
      setForm(BLANK)
      load()
    } catch (err) { setError(err.message) }
  }

  const toggle = async (item) => {
    const { id, restaurantId, featured, ...rest } = item
    try { await api(`/api/menu/items/${id}`, { method: 'PUT', body: { ...rest, available: !item.available } }); load() } catch (e) { setError(e.message) }
  }

  const remove = async (item) => {
    if (!window.confirm(`Delete ${item.name} from the menu?`)) return
    try { await api(`/api/menu/items/${item.id}`, { method: 'DELETE' }); load() } catch (e) { setError(e.message) }
  }

  const active = orders.filter((o) => NEXT[o.status])

  return (
    <div className="page section">
      <h1>Kitchen</h1>
      {restaurants.length > 1 && (
        <label className="inline">Restaurant
          <select value={current ?? ''} onChange={(e) => setCurrent(Number(e.target.value))}>
            {restaurants.map((r) => <option key={r.id} value={r.id}>{r.name}</option>)}
          </select>
        </label>
      )}
      {error && <p className="banner-error" role="alert">{error}</p>}
      {!restaurants.length && !error && <p>You don’t have a restaurant yet. Restaurants are created through the API for now (POST /api/restaurants).</p>}

      <section className="menu-section">
        <h2>Orders to handle ({active.length})</h2>
        {!active.length && <p className="muted">No open orders. New orders appear here automatically.</p>}
        <ul className="ticket-list">
          {active.map((o) => (
            <li key={o.id} className="ticket">
              <p><strong>#{o.id}</strong> <span className={`status status-${o.status}`}>{STATUS_LABEL[o.status]}</span></p>
              <ul className="small">{o.items.map((i) => <li key={i.menuItemId}>{i.quantity} × {i.name}</li>)}</ul>
              <p className="muted small">{money(o.total)} · {new Date(o.createdAt).toLocaleTimeString()}</p>
              <div className="ticket-actions">
                {NEXT[o.status].map(([s, label]) => (
                  <button key={s} className={`btn btn-small ${s === 'CANCELLED' ? 'btn-ghost' : ''}`} onClick={() => move(o.id, s)}>{label}</button>
                ))}
              </div>
            </li>
          ))}
        </ul>
      </section>

      <section className="menu-section owner-menu">
        <h2>Menu</h2>
        <ul className="menu-list">
          {menu.map((m) => (
            <li key={m.id} className={`menu-item ${m.available ? '' : 'unavailable'}`}>
              <span className="menu-emoji" aria-hidden="true">{m.emoji}</span>
              <div className="menu-text"><h3>{m.name}</h3><p className="muted small">{m.category} · {money(m.price)}</p></div>
              <div className="menu-buy">
                <button className="btn btn-small btn-ghost" onClick={() => toggle(m)}>{m.available ? 'Mark sold out' : 'Mark available'}</button>
                <button className="link-btn small" onClick={() => remove(m)}>Delete</button>
              </div>
            </li>
          ))}
        </ul>
        <form className="panel add-item" onSubmit={addItem}>
          <h3>Add a dish</h3>
          <label>Name<input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
          <label>Category<input required value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} placeholder="Burgers" /></label>
          <label>Price (USD)<input required type="number" min="0" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} /></label>
          <label>Emoji<input value={form.emoji} onChange={(e) => setForm({ ...form, emoji: e.target.value })} maxLength={4} /></label>
          <label className="wide">Description<input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label>
          <label className="check"><input type="checkbox" checked={form.vegetarian} onChange={(e) => setForm({ ...form, vegetarian: e.target.checked })} /> Vegetarian</label>
          <label className="check"><input type="checkbox" checked={form.spicy} onChange={(e) => setForm({ ...form, spicy: e.target.checked })} /> Spicy</label>
          <button className="btn">Add dish</button>
        </form>
      </section>
    </div>
  )
}
