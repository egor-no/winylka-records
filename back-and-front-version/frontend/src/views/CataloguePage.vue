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
        <div class="filter-label-row">
          <label for="max-price" class="filter-label">
            Max price:
            <span class="filter-value">{{ currency(max) }}</span>
          </label>

          <button
            type="button"
            class="filter-preset"
            :class="{ active: max <= sale }"
            @click="setMax(sale)"
          >
            Sale
          </button>

          <button
            type="button"
            class="filter-preset"
            :class="{ active: max > sale && max <= cheap }"
            @click="setMax(cheap)"
          >
            Good offer
          </button>
        </div>

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
import { http } from '../api/http'

const STORAGE_KEY = 'shuffled-products-v2'

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
    }, 
    isInCart: {       
      type: Function,
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
  const saved = sessionStorage.getItem(STORAGE_KEY)

  if (saved) {
    try {
      this.items = JSON.parse(saved)
      return
    } catch (e) {
      console.warn('Failed to parse cached products, refetching...', e)
    }
  }

  http.get('/products')
    .then(({ data }) => {
      const shuffled = this.shuffleArray(data)
      this.items = shuffled
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(shuffled))
    })
    .catch(err => {
      console.error(err)
    })
  },
  methods: {
    currency(value) {
      return formatCurrency(value);
    },
    addToCart(product) {
      this.$emit('add-to-cart', product);
    },
    deleteFromCart(product) {
      this.$emit('delete-from-cart', product);
    },
    setMax(value) {
      this.max = value;
    },
    shuffleArray(list) {
      const arr = list.slice() // чтобы не мутировать исходный массив на всякий случай
      for (let i = arr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1))
        const tmp = arr[i]
        arr[i] = arr[j]
        arr[j] = tmp
      }
      return arr
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