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

export default {
  name: 'Shop',
  data() {
    return {
      cart: []
    }
  },
  computed: {
    cartTotal() {
      return this.cart.reduce((sum, item) => sum + Number(item.price), 0)
    },
    cartObjects() {
      const map = new Map()
      this.cart.forEach(product => {
        if (!map.has(product.id)) {
          map.set(product.id, { item: product, amount: 1 })
        } else {
          map.get(product.id).amount++
        }
      })
      return Array.from(map.values())
    }
  },
  methods: {
    currency(value) {
      return formatCurrency(value)
    },
    addToCart(product) {
      this.cart.push(product)
    },
    deleteFromCart(product) {
      const index = this.cart.findIndex(item => item.id === product.id)
      if (index !== -1) this.cart.splice(index, 1)
    },
    updateCart(lines) {
      const next = []
      lines.forEach(line => {
        for (let i = 0; i < line.amount; i++) {
          next.push(line.item)
        }
      })
      this.cart = next
    },
    isInCart(product) {
      return this.cart.some(item => item.id === product.id)
    },
    async handlePlaceOrder(order) {
      try {
        const payload = {
          customer: order.customer,
          shipping: order.shipping,
          comment: order.comment,
          items: order.items.map(line => ({
            productId: line.item.id,
            amount: line.amount
          }))
        }

        const { data } = await http.post('/orders', payload)
        console.log('Order created:', data)

        this.cart = []
        this.$router.push({ name: 'checkout-success' })
      } catch (e) {
        console.error(e)
        alert('Failed to place order.')
      }
    }
  }
}
</script>
