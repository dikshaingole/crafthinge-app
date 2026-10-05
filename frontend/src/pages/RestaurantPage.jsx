import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api, money } from '../api'
import { useAuth } from '../auth'
import { useCart } from '../cart'

export default function RestaurantPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const { cart, add, isCustomer } = useCart()
  const navigate = useNavigate()
  const [restaurant, setRestaurant] = useState(null)
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [added, setAdded] = useState(null)

  useEffect(() => {
    Promise.all([api(`/api/restaurants/${id}`), api(`/api/menu/restaurants/${id}/items`)])
      .then(([r, m]) => { setRestaurant(r); setItems(m) })
      .catch((e) => setError(e.message))
  }, [id])

  const byCategory = useMemo(() => items.reduce((acc, i) => {
    (acc[i.category] ||= []).push(i)
    return acc
  }, {}), [items])

  const onAdd = async (item) => {
    if (!user) return navigate('/login', { state: { from: `/restaurants/${id}` } })
    try {
      await add(item.id)
      setAdded(item.name)
      setTimeout(() => setAdded(null), 2500)
    } catch (e) {
      setError(e.message)
    }
  }

  if (error && !restaurant) return <div className="page section"><p className="banner-error">{error}</p></div>
  if (!restaurant) return <div className="page section"><p className="muted">Loading menu…</p></div>

  return (
    <div className="page section">
      <div className="restaurant-hero">
        <span className="r-hero-emoji" aria-hidden="true">{restaurant.emoji}</span>
        <div>
          <h1>{restaurant.name}</h1>
          <p>{restaurant.description}</p>
          <p className="r-meta">
            <span>★ {Number(restaurant.rating).toFixed(1)}</span>
            <span>{restaurant.deliveryTimeMinutes} min</span>
            <span>{money(restaurant.deliveryFee)} delivery</span>
            <span>{restaurant.address}, {restaurant.city}</span>
          </p>
          {!restaurant.open && <p className="tag tag-closed">Closed now. You can browse the menu, but ordering is paused.</p>}
        </div>
      </div>

      {error && <p className="banner-error">{error}</p>}
      {user && !isCustomer && <p className="note">You’re signed in as a {user.role === 'RESTAURANT_OWNER' ? 'restaurant owner' : 'driver'}. Sign in with a customer account to order.</p>}

      {Object.entries(byCategory).map(([category, list]) => (
        <section key={category} className="menu-section">
          <h2>{category}</h2>
          <ul className="menu-list">
            {list.map((item) => (
              <li key={item.id} className={`menu-item ${item.available ? '' : 'unavailable'}`}>
                <span className="menu-emoji" aria-hidden="true">{item.emoji}</span>
                <div className="menu-text">
                  <h3>{item.name}</h3>
                  <p className="muted">{item.description}</p>
                  <p className="small">
                    {item.vegetarian && <span className="tag">Vegetarian</span>}
                    {item.spicy && <span className="tag tag-spicy">Spicy</span>}
                    {!item.available && <span className="tag tag-closed">Sold out</span>}
                  </p>
                </div>
                <div className="menu-buy">
                  <span className="price">{money(item.price)}</span>
                  {(isCustomer || !user) && (
                    <button className="btn btn-small" disabled={!item.available || !restaurant.open} onClick={() => onAdd(item)}>
                      Add
                    </button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </section>
      ))}

      {isCustomer && cart.itemCount > 0 && cart.restaurantId === restaurant.id && (
        <div className="sticky-cart" role="status">
          <span>{added ? `Added ${added}` : `${cart.itemCount} in your cart`}: {money(cart.subtotal)}</span>
          <Link to="/cart" className="btn btn-saffron">Go to checkout</Link>
        </div>
      )}
    </div>
  )
}
