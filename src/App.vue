<template>
  <div class="page">
    <header class="header">
      <div class="logo-wrap">
        <img
          class="logo-img"
          src="/src/assets/logo.jpg"
          alt="Winylka logo"
        >
        <div class="header-text">
          <h1 class="site-title">Winylka Records</h1>
          <p class="site-subtitle">Selected vinyl records</p>
        </div>
      </div>
    </header>

    <h2 class="section-title">Catalogue</h2>

    <div class="filter-bar">
      <div class="filter-left">
        <label for="max-price" class="filter-label">
          Max price:
          <span class="filter-value">{{ currency(max) }}</span>
        </label>

        <input
          id="max-price"
          v-model.number="max"
          type="range"
          min="0"
          max="200"
          class="filter-range"
        >
      </div>

      <div class="filter-right">
        <span class="result-badge">
          {{ filteredProducts.length }} records
        </span>
      </div>
    </div>

    <div class="grid">
      <article v-for="item in filteredProducts" :key="item.id" class="product-card">
        <img class="product-image" :src="item.img" :title="item.name" :alt="item.name">

        <div class="product-body">
          <h3 class="product-title">
            {{ item.artist }} – {{ item.name }}
          </h3>

          <p class="product-note">{{ item.note }}</p>
        </div>

        <div class="product-bottom">
          <div class="product-price">{{ currency(item.price) }}</div>

          <div class="product-tags">
            <span v-if="item.price < sale" class="tag tag-sale">SALE!</span>
            <span v-else-if="item.price < cheap" class="tag tag-good">GOOD OFFER!</span>

            <span class="tag tag-format">{{ item.format }}</span>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<script>
import { products } from './data/products.js'

export default {
  data() {
    return {
      max: 200,
      cheap: 25, 
      sale: 20, 
      items: products
    }
  },
  methods: {
    currency(value) {
      return `$${Number.parseFloat(value).toFixed(2)}`
    }
  },
  computed: {
    filteredProducts() {
      return this.items.filter( item => (item.price < this.max))
    }
  }
}
</script>
