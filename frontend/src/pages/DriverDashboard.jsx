import { useCallback, useEffect, useState } from 'react'
import { api, money } from '../api'

const LABEL = { ASSIGNED: 'Go to restaurant', PICKED_UP: 'On the way to customer', DELIVERED: 'Delivered' }

export default function DriverDashboard() {
  const [available, setAvailable] = useState([])
  const [mine, setMine] = useState([])
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    try {
      const [a, m] = await Promise.all([api('/api/deliveries/available'), api('/api/deliveries/mine')])
      setAvailable(a)
      setMine(m)
    } catch (e) { setError(e.message) }
  }, [])

  useEffect(() => {
    load()
    const t = setInterval(load, 8000)
    return () => clearInterval(t)
  }, [load])

  const act = async (id, action) => {
    setError('')
    try { await api(`/api/deliveries/${id}/${action}`, { method: 'POST' }); load() } catch (e) { setError(e.message); load() }
  }

  const current = mine.filter((d) => d.status !== 'DELIVERED')

  return (
    <div className="page section">
      <h1>Deliveries</h1>
      {error && <p className="banner-error" role="alert">{error}</p>}

      <section className="menu-section">
        <h2>Your current jobs</h2>
        {!current.length && <p className="muted">No active deliveries. Accept one below.</p>}
        <ul className="ticket-list">
          {current.map((d) => (
            <li key={d.id} className="ticket">
              <p><strong>Order #{d.orderId}</strong> · {LABEL[d.status]}</p>
              <p className="small">Deliver to {d.deliveryAddress}</p>
              <div className="ticket-actions">
                {d.status === 'ASSIGNED' && <button className="btn btn-small" onClick={() => act(d.id, 'pickup')}>I picked it up</button>}
                {d.status === 'PICKED_UP' && <button className="btn btn-small btn-saffron" onClick={() => act(d.id, 'deliver')}>Mark delivered</button>}
              </div>
            </li>
          ))}
        </ul>
      </section>

      <section className="menu-section">
        <h2>Ready for pickup ({available.length})</h2>
        {!available.length && <p className="muted">Nothing waiting right now. This list refreshes on its own.</p>}
        <ul className="ticket-list">
          {available.map((d) => (
            <li key={d.id} className="ticket">
              <p><strong>Order #{d.orderId}</strong> · {money(d.orderTotal)}</p>
              <p className="small">To {d.deliveryAddress}</p>
              <div className="ticket-actions"><button className="btn btn-small" onClick={() => act(d.id, 'accept')}>Accept job</button></div>
            </li>
          ))}
        </ul>
      </section>

      {mine.some((d) => d.status === 'DELIVERED') && (
        <p className="muted small">Completed today: {mine.filter((d) => d.status === 'DELIVERED').length}</p>
      )}
    </div>
  )
}
