import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, tokenStore } from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('ch_user')) } catch { return null }
  })

  const save = useCallback((res) => {
    tokenStore.set(res.token)
    localStorage.setItem('ch_user', JSON.stringify(res.user))
    setUser(res.user)
    return res.user
  }, [])

  const logout = useCallback(() => {
    tokenStore.clear()
    localStorage.removeItem('ch_user')
    setUser(null)
  }, [])

  useEffect(() => {
    window.addEventListener('ch-logout', logout)
    return () => window.removeEventListener('ch-logout', logout)
  }, [logout])

  const value = useMemo(() => ({
    user,
    login: (email, password) => api('/api/auth/login', { method: 'POST', body: { email, password } }).then(save),
    register: (form) => api('/api/auth/register', { method: 'POST', body: form }).then(save),
    logout,
  }), [user, save, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
