<template>
  <div class="catalogue-page">
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
import ProductCard from '../components/ProductCard.vue'
import Cart from '../components/Cart.vue'
import { formatCurrency } from '../utils/formatters'

export default {
  name: 'Catalogue',
  components: {
    ProductCard,
    Cart
  },
  props: {
    cartObjects: {
      type: Array,
      required: true
    },
    cartTotal: {
      type: Number,
      required: true
    }
  },
  emits: ['add-to-cart', 'delete-from-cart', 'update-cart'],
  data() {
    return {
      max: 200,
      cheap: 25,
      sale: 20,
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
    isInCart(product) {
      this.$emit('is-in-cart', product);
    },
    addToCart(product) {
      this.$emit('add-to-cart', product);
    },
    deleteFromCart(product) {
      this.$emit('delete-from-cart', product);
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
    }
  }
}
</script>