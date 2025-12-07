<template>
  <div class="cart-summary-wrap">
    <button
      @click="$emit('toggle')"
      class="cart-summary"
      :class="{ 'cart-open': displayCart }"
      type="button"
    >
      <span class="cart-summary-icon">🛒</span>
      <span
        class="cart-summary-count"
        :class="{ 'cart-count-open': displayCart }"
      >
        {{ currency(cartTotal) }}
      </span>
    </button>

    <transition name="cart-dropdown">
      <div v-if="displayCart" class="cart-list">
        <template v-if="cartObjects.length">
          <transition-group
            name="cart-item"
            tag="div"
            class="cart-list-inner"
            appear
          >
            <CartItem
              v-for="line in cartObjects"
              :key="line.item.id"
              :cartItem="line"
              @delete="$emit('delete', $event)"
            />
          </transition-group>
           <div class="cart-footer" v-if="cartObjects.length">
            <RouterLink
              to="/checkout"
              class="checkout-link"
              @click="$emit('toggle')"
            >
              Go to checkout
            </RouterLink>
          </div>
        </template>

        <transition name="empty-fade" appear>
          <div v-if="!cartObjects.length" class="cart-empty">
            <p class="cart-note">Корзина пуста</p>
          </div>
        </transition>
      </div>
    </transition>
  </div>
</template>

<script>
import { formatCurrency } from '../utils/formatters'
import CartItem from '../components/CartItem.vue'

export default {
  name: 'Cart',
  components: {
    CartItem
  },
  props: {
    displayCart: {
      type: Boolean,
      required: true
    },
    cartObjects: {
      type: Array,
      required: true
    },
    cartTotal: {
      type: Number,
      required: true
    }
  },
  emits: ['toggle', 'delete'],
  methods: {
    currency(value) {
      return formatCurrency(value);
    }
  }
}
</script>