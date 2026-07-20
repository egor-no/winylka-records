import { http } from './http'

export const stockSubscriptionsApi = {
  subscribe(productId, email, type) {
    return http.post(
      `/products/${productId}/stock-subscriptions`,
      {
        email,
        type
      }
    )
  }
}