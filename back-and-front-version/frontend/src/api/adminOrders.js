import { http } from './http'

export const adminOrdersApi = {
  getAll() {
    return http.get('/admin/orders')
  },

  getById(id) {
    return http.get(`/admin/orders/${id}`)
  },

  ship(id, trackingNumber) {
    return http.patch(
      `/admin/orders/${id}/ship`,
      {
        trackingNumber
      }
    )
  }
}