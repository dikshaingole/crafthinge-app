import { Navigate, Route, Routes } from 'react-router-dom'
import Header from './components/Header'
import Footer from './components/Footer'
import { useAuth } from './auth'
import Home from './pages/Home'
import Restaurants from './pages/Restaurants'
import RestaurantPage from './pages/RestaurantPage'
import CartPage from './pages/CartPage'
import Orders from './pages/Orders'
import OrderTracking from './pages/OrderTracking'
import { Login, Register } from './pages/Auth'
import OwnerDashboard from './pages/OwnerDashboard'
import DriverDashboard from './pages/DriverDashboard'

function RequireRole({ role, children }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (role && user.role !== role) return <Navigate to="/" replace />
  return children
}

export default function App() {
  return (
    <>
      <a className="skip" href="#main">Skip to content</a>
      <Header />
      <main id="main">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/restaurants" element={<Restaurants />} />
          <Route path="/restaurants/:id" element={<RestaurantPage />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/cart" element={<RequireRole role="CUSTOMER"><CartPage /></RequireRole>} />
          <Route path="/orders" element={<RequireRole role="CUSTOMER"><Orders /></RequireRole>} />
          <Route path="/orders/:id" element={<RequireRole><OrderTracking /></RequireRole>} />
          <Route path="/kitchen" element={<RequireRole role="RESTAURANT_OWNER"><OwnerDashboard /></RequireRole>} />
          <Route path="/drive" element={<RequireRole role="DELIVERY_PARTNER"><DriverDashboard /></RequireRole>} />
          <Route path="*" element={<div className="page narrow"><h1>Page not found</h1><p>That link doesn’t lead anywhere. Head back to the <a href="/">home page</a>.</p></div>} />
        </Routes>
      </main>
      <Footer />
    </>
  )
}
