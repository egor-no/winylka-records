<template>
  <article class="product-card" @click="goToProduct">
    <div class="image-wrap">
      <img
        class="product-image"
        :src="item.img"
        :title="item.name"
        :alt="item.name"
      >

      <button
        v-if="availableStock > 0"
        @click.stop="$emit('add', item)"
        class="image-cart-button"
        :class="isInCart(item) ? 'added' : 'to-add'"
        type="button"
        title="Add to cart"
      >
        🛒
      </button>
    </div>

    <div class="product-body">
      <h3 class="product-title">
        {{ item.artist }} – {{ item.name }}
      </h3>

      <p class="product-note">{{ item.note }}</p>
    </div>

    <div class="product-bottom">
      <div class="product-price">{{ currency(item.price) }}</div>

      <div class="product-tags">
        <span v-if="availableStock <= 0" class="tag tag-out-of-stock">OUT OF STOCK</span>
  
        <template v-else>
          <span v-if="item.price < sale" class="tag tag-sale">SALE!</span>
          <span v-else-if="item.price < cheap" class="tag tag-good">GOOD OFFER!</span>
        </template>

          <span class="tag tag-format">{{ item.format }}</span>
      </div>
    </div>
  </article>
</template>

<script>
import { formatCurrency } from '../utils/formatters'

export default {
  name: 'ProductCard',
  props: {
    item: {
      type: Object,
      required: true
    },
    cheap: {
      type: Number,
      required: true
    },
    sale: {
      type: Number,
      required: true
    },
    isInCart: {
      type: Function,
      required: true
    }
  },
  emits: ['add'],
  computed: {
      availableStock() {
        const stock = Number(this.item.stockQuantity)
        return Number.isFinite(stock)? Math.max(0, stock) : 0
      }
  },
  methods: {
    currency(value) {
      return formatCurrency(value);
    },
    goToProduct() {
      this.$router.push(`/product/${this.item.id}`)
    }
  }
}
</script>