<template>
  <div class="page">
    <Header />

    <div class="section-header">
      <h2 class="section-title">Catalogue</h2>

      <Cart 
        :cart-objects="cartObjects"
        :display-cart="displayCart"
        :cart-total="cartTotal"
        @delete="deleteFromCart"
        @toggle="displayCart = !displayCart"
      />
    </div>

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
        <label class="search-label">
          Search:
          <input
            v-model="search"
            type="text"
            class="search-input"
            placeholder="Artist or album"
          >
        </label>

        <span class="result-badge">
          {{ filteredProducts.length }} records
        </span>
      </div>
    </div>

    <div class="grid">
      <ProductCard
        v-for="item in filteredProducts"
        :key="item.id"
        :item="item"
        :cheap="cheap"
        :sale="sale"
        :is-in-cart="isInCart"
        @add="addToCart"
      />
    </div>
  </div>
</template>

<script>
import Header from './components/HeaderBar.vue'
import ProductCard from './components/ProductCard.vue'
import Cart from './components/Cart.vue'

import { formatCurrency } from './utils/formatters'
export default {
  components: {
    Header,
    ProductCard,
    Cart
  },
  data() {
    return {
      max: 200,
      cheap: 25,
      sale: 20,
      cart: [],
      displayCart: false,
      items: [],
      search: ''
    }
  },
  created() {
    fetch("/data/products.json")
      .then(response => response.json())
      .then(data => {
        this.items = data;
      })
  },
  methods: {
    currency(value) {
      return formatCurrency(value);
    },
    addToCart(product) {
      this.cart.push(product);
    },
    deleteFromCart(product) {
      const index = this.cart.findIndex(item => item.id === product.id);
      if (index !== -1) {
        this.cart.splice(index, 1); 
      }
    },
    isInCart(product) {
      return this.cart.some(item => item.id === product.id);
    }
  },
  computed: {
    filteredProducts() {
      const q = this.search.trim().toLowerCase();

      return this.items.filter(item => {
        const matchesPrice = item.price < this.max;

        if (!q) {
          return matchesPrice;
        }

        const haystack = `${item.artist} ${item.name}`.toLowerCase();
        const matchesSearch = haystack.includes(q);

        return matchesPrice && matchesSearch;
      });
    },
    cartTotal() {
      return this.cart.reduce((inc, item) => Number(item.price) + inc, 0);
    },
    cartObjects() {
      const map = new Map();

      this.cart.forEach(product => {
        if (!map.has(product.id)) {
          map.set(product.id, {
            item: product,
            amount: 1
          });
        } else {
          map.get(product.id).amount++;
        }
      });

      return Array.from(map.values());
    }
  }
}
</script>
