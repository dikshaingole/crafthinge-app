import { useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth'

const HOME_FOR = { CUSTOMER: '/', RESTAURANT_OWNER: '/kitchen', DELIVERY_PARTNER: '/drive' }

export function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('customer@crafthinge.dev')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const user = await login(email, password)
      navigate(location.state?.from || HOME_FOR[user.role] || '/')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page section narrow">
      <h1>Sign in</h1>
      <form className="panel" onSubmit={submit}>
        <label>Email<input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" /></label>
        <label>Password<input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" /></label>
        {error && <p className="banner-error" role="alert">{error}</p>}
        <button className="btn btn-wide">Sign in</button>
        <p className="muted small">Demo accounts (password <code>Password123!</code>): customer@crafthinge.dev, owner@crafthinge.dev, driver@crafthinge.dev</p>
      </form>
      <p>New to CraftHinge? <Link to="/register">Create an account</Link></p>
    </div>
  )
}

export function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '', role: params.get('role') || 'CUSTOMER' })
  const [error, setError] = useState('')
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const user = await register(form)
      navigate(HOME_FOR[user.role] || '/')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page section narrow">
      <h1>Create your account</h1>
      <form className="panel" onSubmit={submit}>
        <fieldset className="role-pick">
          <legend>I want to</legend>
          {[['CUSTOMER', 'Order food'], ['RESTAURANT_OWNER', 'Sell food from my restaurant'], ['DELIVERY_PARTNER', 'Deliver orders']].map(([v, label]) => (
            <label key={v} className={`role-option ${form.role === v ? 'on' : ''}`}>
              <input type="radio" name="role" value={v} checked={form.role === v} onChange={set('role')} /> {label}
            </label>
          ))}
        </fieldset>
        <label>Full name<input required value={form.fullName} onChange={set('fullName')} autoComplete="name" /></label>
        <label>Email<input type="email" required value={form.email} onChange={set('email')} autoComplete="email" /></label>
        <label>Phone (optional)<input value={form.phone} onChange={set('phone')} autoComplete="tel" /></label>
        <label>Password<input type="password" required minLength={8} value={form.password} onChange={set('password')} autoComplete="new-password" />
          <span className="muted small">At least 8 characters.</span></label>
        {error && <p className="banner-error" role="alert">{error}</p>}
        <button className="btn btn-wide">Create account</button>
      </form>
    </div>
  )
}
