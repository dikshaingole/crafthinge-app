import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'
import { DishTile, RestaurantCard } from '../components/Cards'

const CUISINE_EMOJI = {
  American: '🍔', Mexican: '🌮', Italian: '🍕', Indian: '🍛', Japanese: '🍜',
  Thai: '🍲', Mediterranean: '🥙', Breakfast: '🥞',
}

export default function Home() {
  const [restaurants, setRestaurants] = useState([])
  const [featured, setFeatured] = useState([])
  const [cuisines, setCuisines] = useState([])
  const [error, setError] = useState('')
  const [q, setQ] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    Promise.all([api('/api/restaurants'), api('/api/menu/featured'), api('/api/restaurants/cuisines')])
      .then(([r, f, c]) => { setRestaurants(r); setFeatured(f); setCuisines(c) })
      .catch((e) => setError(e.message))
  }, [])

  const names = useMemo(() => Object.fromEntries(restaurants.map((r) => [r.id, r.name])), [restaurants])

  const search = (e) => {
    e.preventDefault()
    navigate(`/restaurants${q.trim() ? `?q=${encodeURIComponent(q.trim())}` : ''}`)
  }

  return (
    <>
      {/* Hero: the one loud moment on the page */}
      <section className="hero">
        <div className="hero-inner">
          <h1>What are you hungry for tonight?</h1>
          <form className="hero-search" onSubmit={search} role="search">
            <label htmlFor="hero-q" className="sr-only">Search restaurants</label>
            <input id="hero-q" value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search tacos, ramen, a restaurant name…" />
            <button className="btn btn-saffron" type="submit">Find food</button>
          </form>
          <ul className="stickers" aria-label="Browse by cuisine">
            {Object.entries(CUISINE_EMOJI).map(([c, e]) => (
              <li key={c}>
                <Link to={`/restaurants?cuisine=${c}`} className="sticker">
                  <span className="sticker-emoji" aria-hidden="true">{e}</span>
                  <span>{c}</span>
                </Link>
              </li>
            ))}
          </ul>
        </div>
      </section>

      {error && <p className="banner-error page">{error}</p>}

      <section className="page section">
        <div className="section-head">
          <h2>Fresh picks this week</h2>
          <Link to="/restaurants">See all restaurants</Link>
        </div>
        <div className="rail" tabIndex={0} aria-label="Featured dishes">
          {featured.map((item) => <DishTile key={item.id} item={item} restaurantName={names[item.restaurantId]} />)}
          {!featured.length && !error && <p className="muted">Loading dishes…</p>}
        </div>
      </section>

      <section className="page section">
        <h2>New and tasty</h2>
        <div className="promo-grid">
          <Link to="/restaurants?cuisine=Japanese" className="promo promo-night">
            <span className="promo-emoji" aria-hidden="true">🍜</span>
            <span className="promo-title">Ramen season is here</span>
            <span>Twelve-hour broths from Tsuki Ramen Bar, at your door in 30 minutes.</span>
          </Link>
          <Link to="/restaurants?cuisine=Breakfast" className="promo promo-morning">
            <span className="promo-emoji" aria-hidden="true">🥞</span>
            <span className="promo-title">Breakfast, any time</span>
            <span>Pancakes at 9 pm? Sunny Side Diner serves all day.</span>
          </Link>
        </div>
      </section>

      <section className="rewards">
        <div className="page rewards-inner">
          <div>
            <h2>CraftHinge Rewards</h2>
            <p>One account for every restaurant on CraftHinge. Earn points on each order and get free delivery on your birthday.</p>
          </div>
          <Link to="/register" className="btn btn-ink">Join for free</Link>
        </div>
      </section>

      <section className="page section">
        <h2>Order by cuisine</h2>
        <ul className="cuisine-grid">
          {cuisines.map((c) => (
            <li key={c.cuisine}>
              <Link to={`/restaurants?cuisine=${c.cuisine}`} className="cuisine-tile">
                <span aria-hidden="true" className="cuisine-emoji">{CUISINE_EMOJI[c.cuisine] || '🍽️'}</span>
                <span className="cuisine-name">{c.cuisine}</span>
                <span className="muted small">{c.restaurants} {c.restaurants === 1 ? 'place' : 'places'}</span>
              </Link>
            </li>
          ))}
        </ul>
      </section>

      <section className="page section">
        <h2>Popular near you</h2>
        <div className="restaurant-grid">
          {restaurants.filter((r) => r.featured).map((r) => <RestaurantCard key={r.id} r={r} />)}
        </div>
      </section>
    </>
  )
}
