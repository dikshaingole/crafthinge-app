import { Link } from 'react-router-dom'
import { money } from '../api'

export function RestaurantCard({ r }) {
  return (
    <Link to={`/restaurants/${r.id}`} className="restaurant-card">
      <span className="r-emoji" aria-hidden="true">{r.emoji || '🍽️'}</span>
      <span className="r-body">
        <span className="r-name">{r.name}</span>
        <span className="muted">{r.cuisine} in {r.city}</span>
        <span className="r-meta">
          <span>★ {Number(r.rating).toFixed(1)}</span>
          <span>{r.deliveryTimeMinutes} min</span>
          <span>{Number(r.deliveryFee) === 0 ? 'Free delivery' : `${money(r.deliveryFee)} delivery`}</span>
        </span>
        {!r.open && <span className="tag tag-closed">Closed now</span>}
      </span>
    </Link>
  )
}

export function DishTile({ item, restaurantName }) {
  return (
    <Link to={`/restaurants/${item.restaurantId}`} className="dish-tile">
      <span className="dish-plate" aria-hidden="true">{item.emoji || '🍽️'}</span>
      <span className="dish-name">{item.name}</span>
      <span className="muted small">{restaurantName || item.category}</span>
      <span className="dish-price">{money(item.price)}</span>
    </Link>
  )
}
