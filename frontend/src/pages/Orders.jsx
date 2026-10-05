import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, money, STATUS_LABEL } from '../api'

export default function Orders() {
  const [orders, setOrders] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => { api('/api/orders').then(setOrders).catch((e) => setError(e.message)) }, [])

  return (
    <div className="page section narrow">
      <h1>My orders</h1>
      {error && <p className="banner-error">{error}</p>}
      {orders?.length === 0 && <p>No orders yet. <Link to="/restaurants">Find something to eat</Link>.</p>}
      <ul className="order-list">
        {orders?.map((o) => (
          <li key={o.id}>
            <Link to={`/orders/${o.id}`} className="order-row">
              <span className="grow"><strong>{o.restaurantName}</strong><br />
                <span className="muted small">#{o.id} · {new Date(o.createdAt).toLocaleString()}</span></span>
              <span className={`status status-${o.status}`}>{STATUS_LABEL[o.status]}</span>
              <span className="price">{money(o.total)}</span>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
