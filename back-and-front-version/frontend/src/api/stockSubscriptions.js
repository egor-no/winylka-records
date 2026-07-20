import { http } from './http'

export const stockSubscriptionsApi = {
  subscribe(productId, email) {
    return http.post(
      `/products/${productId}/stock-subscriptions`,
      { email }
    )
  }
}