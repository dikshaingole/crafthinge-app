import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import { useCart } from '../cart'

export default function Header() {
  const { user, logout } = useAuth()
  const { cart, isCustomer } = useCart()
  const navigate = useNavigate()

  return (
    <header className="site-header">
      <div className="bar">
        <Link to="/" className="logo" aria-label="CraftHinge home">
          <span className="logo-mark" aria-hidden="true">🥡</span>
          <span>CraftHinge</span>
        </Link>
        <nav className="main-nav" aria-label="Main">
          <NavLink to="/restaurants">Restaurants</NavLink>
          <Link to="/restaurants?cuisine=Mexican">Tacos</Link>
          <Link to="/restaurants?cuisine=Italian">Pizza</Link>
          {isCustomer && <NavLink to="/orders">My orders</NavLink>}
          {user?.role === 'RESTAURANT_OWNER' && <NavLink to="/kitchen">Kitchen</NavLink>}
          {user?.role === 'DELIVERY_PARTNER' && <NavLink to="/drive">Deliveries</NavLink>}
        </nav>
        <div className="header-actions">
          {isCustomer && (
            <Link to="/cart" className="cart-btn">
              Cart{cart.itemCount > 0 && <span className="cart-count" aria-label={`${cart.itemCount} items`}>{cart.itemCount}</span>}
            </Link>
          )}
          {user ? (
            <button className="link-btn" onClick={() => { logout(); navigate('/') }}>
              Sign out <span className="who">{user.fullName.split(' ')[0]}</span>
            </button>
          ) : (
            <Link to="/login" className="btn btn-small">Sign in</Link>
          )}
        </div>
      </div>
    </header>
  )
}
