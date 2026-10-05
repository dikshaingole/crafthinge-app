import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api } from './api'
import { useAuth } from './auth'

const CartContext = createContext(null)
const EMPTY = { restaurantId: null, items: [], itemCount: 0, subtotal: 0 }

export function CartProvider({ children }) {
  const { user } = useAuth()
  const [cart, setCart] = useState(EMPTY)
  const isCustomer = user?.role === 'CUSTOMER'

  const refresh = useCallback(async () => {
    if (!isCustomer) return setCart(EMPTY)
    try { setCart(await api('/api/cart')) } catch { setCart(EMPTY) }
  }, [isCustomer])

  useEffect(() => { refresh() }, [refresh])

  const add = async (menuItemId, quantity = 1) => {
    try {
      setCart(await api('/api/cart/items', { method: 'POST', body: { menuItemId, quantity } }))
    } catch (e) {
      if (e.status === 409 && window.confirm(`${e.message}\n\nStart a new cart with this dish?`)) {
        setCart(await api('/api/cart/items?replace=true', { method: 'POST', body: { menuItemId, quantity } }))
      } else if (e.status !== 409) {
        throw e
      }
    }
  }

  const setQuantity = async (menuItemId, quantity) =>
    setCart(await api(`/api/cart/items/${menuItemId}`, { method: 'PATCH', body: { quantity } }))

  return (
    <CartContext.Provider value={{ cart, add, setQuantity, refresh, isCustomer }}>
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)
