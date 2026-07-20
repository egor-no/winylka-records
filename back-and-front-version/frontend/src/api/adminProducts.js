import { http } from './http'

export const adminProductsApi = {
  getAll() {
    return http.get('/products')
  },

  create(formData) {
    return http.post('/admin/products', formData)
  },

  update(id, formData) {
    return http.put(`/admin/products/${id}`, formData)
  },

  addStock(id, quantity) {
    return http.patch(`/admin/products/${id}/stock`, {
      quantity
    })
  },

  delete(id) {
    return http.delete(`/admin/products/${id}`)
  }
}