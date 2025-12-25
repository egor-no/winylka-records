import { http } from './http'

export const cartApi = {
  get() {
    return http.get('/cart')
  },
  add(productId, amount = 1) {
    return http.post('/cart/items', { productId, amount })
  },
  setAmount(productId, amount) {
    return http.put(`/cart/items/${productId}`, { productId, amount })
  },
  replace(lines) {
    return http.put('/cart', lines)
  },
  remove(productId) {
    return http.delete(`/cart/items/${productId}`)
  },
  clear() {
    return http.delete('/cart')
  }
}