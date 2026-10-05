import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../api'
import { RestaurantCard } from '../components/Cards'

export default function Restaurants() {
  const [params, setParams] = useSearchParams()
  const cuisine = params.get('cuisine') || ''
  const q = params.get('q') || ''
  const [list, setList] = useState(null)
  const [cuisines, setCuisines] = useState([])
  const [error, setError] = useState('')

  useEffect(() => { api('/api/restaurants/cuisines').then(setCuisines).catch(() => {}) }, [])
  useEffect(() => {
    setList(null)
    const qs = new URLSearchParams()
    if (cuisine) qs.set('cuisine', cuisine)
    if (q) qs.set('q', q)
    api(`/api/restaurants?${qs}`).then(setList).catch((e) => setError(e.message))
  }, [cuisine, q])

  const pick = (c) => {
    const next = new URLSearchParams(params)
    c ? next.set('cuisine', c) : next.delete('cuisine')
    setParams(next)
  }

  return (
    <div className="page section">
      <h1>{cuisine ? `${cuisine} restaurants` : q ? `Results for “${q}”` : 'All restaurants'}</h1>
      <div className="chips" role="group" aria-label="Filter by cuisine">
        <button className={`chip ${!cuisine ? 'on' : ''}`} aria-pressed={!cuisine} onClick={() => pick('')}>All</button>
        {cuisines.map((c) => (
          <button key={c.cuisine} className={`chip ${cuisine === c.cuisine ? 'on' : ''}`}
                  aria-pressed={cuisine === c.cuisine} onClick={() => pick(c.cuisine)}>{c.cuisine}</button>
        ))}
      </div>
      {error && <p className="banner-error">{error}</p>}
      {list === null && !error && <p className="muted">Loading restaurants…</p>}
      {list?.length === 0 && <p>No restaurants match that yet. Try another cuisine or clear the search.</p>}
      <div className="restaurant-grid">{list?.map((r) => <RestaurantCard key={r.id} r={r} />)}</div>
    </div>
  )
}
