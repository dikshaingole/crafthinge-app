import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="footer-grid">
        <div>
          <p className="footer-logo">🥡 CraftHinge</p>
          <p className="muted">Local restaurants, delivered.</p>
        </div>
        <div>
          <h2>Order</h2>
          <ul>
            <li><Link to="/restaurants">All restaurants</Link></li>
            <li><Link to="/restaurants?cuisine=American">Burgers</Link></li>
            <li><Link to="/restaurants?cuisine=Japanese">Ramen &amp; sushi</Link></li>
            <li><Link to="/register">Join Rewards</Link></li>
          </ul>
        </div>
        <div>
          <h2>Work with us</h2>
          <ul>
            <li><Link to="/register?role=RESTAURANT_OWNER">Add your restaurant</Link></li>
            <li><Link to="/register?role=DELIVERY_PARTNER">Drive with CraftHinge</Link></li>
          </ul>
        </div>
        <div>
          <h2>Help</h2>
          <ul>
            <li><Link to="/orders">Track an order</Link></li>
            <li><a href="mailto:help@crafthinge.dev">Contact support</a></li>
          </ul>
        </div>
      </div>
      <p className="fineprint">© {new Date().getFullYear()} CraftHinge. A fictional company built as a cloud engineering portfolio project. No real orders or payments are processed.</p>
    </footer>
  )
}
