<template>
    <div class="cart-list-item"> 
        <span
            @click="$emit('delete', cartItem.item)"
            type="button"
            class="cart-delete-btn"
            title="Remove from cart"
        >
        🗑
        </span>

        <span class="cart-item-name">{{ cartItem.item.name }}</span>
        <span class="cart-item-qty">×{{ cartItem.amount }}</span>
        <span class="cart-item-price">
            {{ currency(cartItem.item.price * cartItem.amount) }}
        </span>
    </div>
</template>

<script>
import { formatCurrency } from '../utils/formatters'

export default {
  name: 'CartItem',
  props: {
    cartItem: {
      type: Object,
      required: true
    }
  },
  emits: ['delete'],
  methods: {
    currency(value) {
      return formatCurrency(value);
    }
  }
}
</script>
