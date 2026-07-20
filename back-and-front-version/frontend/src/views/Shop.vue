<template>
  <RouterView v-slot="{ Component }">
    <component
      :is="Component"
      :cart-objects="cartObjects"
      :cart-total="cartTotal"
      :is-in-cart="isInCart"
      @add-to-cart="addToCart"
      @delete-from-cart="deleteFromCart"
      @update-cart="updateCart"
      @place-order="handlePlaceOrder"
    />
  </RouterView>
</template>

<script>
import { formatCurrency } from '../utils/formatters'
import { http } from '../api/http'
import { cartApi } from '../api/cart'

export default {
  name: 'Shop',
  data() {
    return {
      cartState: { lines: [], itemsTotal: 0 }
    }
  },
  computed: {
    cartTotal() {
      return Number(this.cartState?.itemsTotal || 0)
    },
    cartObjects() {
      return (this.cartState?.lines || []).map(l => ({
        item: l.item,
        amount: l.amount
      }))
    }
  },
  async created() {
    await this.refreshCart()
  },
  methods: {
    currency(value) {
      return formatCurrency(value)
    },

    async refreshCart() {
      const { data } = await cartApi.get()
      this.cartState = data
    },

   async addToCart(product) {
    try {
        const { data } = await cartApi.add(product.id, 1)

        this.cartState = data
      } catch (e) {
        console.error(e)

        const message =
          e?.response?.data?.message
          || 'Failed to add product to cart.'

        alert(message)

        await this.refreshCart()
      }
    },

    async deleteFromCart(product) {
      const { data } = await cartApi.remove(product.id)
      this.cartState = data
    },

    async updateCart(lines) {
      const payload = lines.map(line => ({
        productId: line.item.id,
        amount: line.amount
      }))

      try {
        const { data } = await cartApi.replace(payload)

        this.cartState = data
      } catch (e) {
        console.error(e)

        const message =
          e?.response?.data?.message
          || 'Failed to update cart.'

        alert(message)

        await this.refreshCart()
      }
    },

    isInCart(product) {
      return this.cartObjects.some(l => l.item.id === product.id && l.amount > 0)
    },

    async handlePlaceOrder(order) {
      try {
        const payload = {
          customer: order.customer,
          shipping: order.shipping,
          comment: order.comment,
          items: order.items
        }

        const { data } = await http.post('/orders', payload)

        console.log('Order created:', data)

        await cartApi.clear()
        await this.refreshCart()

        sessionStorage.removeItem('shuffled-products-v2')

        window.dispatchEvent(
          new CustomEvent('winylka-products-updated')
        )

        this.$router.push({
          name: 'checkout-success',
          query: {
            orderId: data.orderId
          }
        })
      } catch (e) {
        console.error(e)

        const message =
          e?.response?.data?.message
          || 'Failed to place order.'

        alert(message)

        await this.refreshCart()
      }
    }
  }
}
</script>