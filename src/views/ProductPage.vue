<template>
  <section class="product-page" v-if="product">
    <div class="section-header">
      <h2 class="section-title">
        {{ product.artist }} — {{ product.name }}
      </h2>

      <Cart 
        :cart-objects="cartObjects"
        :display-cart="displayCart"
        :cart-total="cartTotal"
        @delete="deleteFromCart"
        @toggle="displayCart = !displayCart"
      />
    </div>

    <RouterLink to="/catalogue" class="product-back-link">
      ← Back to catalogue
    </RouterLink>

    <div class="product-layout">
      <div class="product-image-col">
        <div class="image-wrap product-image-wrap">
          <img
            :src="product.img"
            :alt="product.artist + ' — ' + product.name"
            class="product-image-full"
          >
        </div>
      </div>

      <div class="product-info-col">
        <p class="product-catno">
          <span class="product-label">Cat. No:</span>
          <span class="product-value">{{ product.catNo }}</span>
        </p>

        <p class="product-meta">
          <span class="product-label">Format:</span>
          <span class="product-value">
            {{ product.format }} — {{ product.note }}
          </span>
        </p>

        <p class="product-price-line">
          <span class="product-label">Price:</span>
          <span class="product-price-main">
            {{ currency(product.price) }}
          </span>
        </p>

        <div class="product-qty-row">
          <label class="product-label">
            Quantity
            <input
              v-model.number="qty"
              type="number"
              min="1"
              class="product-qty-input"
            >
          </label>

          <span
            v-if="inCartCount > 0"
            class="product-in-cart-hint"
          >
            In cart: {{ inCartCount }}
          </span>
        </div>

        <div class="product-actions">
          <button
            type="button"
            class="product-add-btn"
            @click="addToCart"
          >
            Add to cart
          </button>

          <RouterLink to="/catalogue" class="product-secondary-link">
            Back to catalogue
          </RouterLink>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import { formatCurrency } from '../utils/formatters'
import Cart from '../components/Cart.vue'

export default {
  name: 'ProductPage',
  components: { 
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
      product: null,
      qty: 1,
      loading: false,
      error: '',
      displayCart: false
    }
  },
  created() {
    const id = Number(this.$route.params.id)
    this.loadProduct(id)
  },
 methods: {
    currency(value) {
      return formatCurrency(value)
    },
    async loadProduct(id) {
      this.loading = true
      this.error = ''
      try {
        const response = await fetch('/data/products.json')
        const data = await response.json()
        const found = data.find(item => item.id === id)

         if (!found) {
          this.$router.replace({ name: 'not-found' })
          return
        }
        this.product = found || null
      } catch (e) {
        console.error(e)
        this.error = 'Failed to load product.'
      } finally {
        this.loading = false
      }
    },
    addToCart() {
      if (!this.product || this.qty < 1) return

      const count = Number.isNaN(this.qty) ? 1 : this.qty

      for (let i = 0; i < count; i++) {
        this.$emit('add-to-cart', this.product)
      }

      this.qty = 1
    },
    deleteFromCart(product) {
      this.$emit('delete-from-cart', product)
    }
  },
    computed: {
      inCartCount() {
        if (!this.product) return 0
        if (!Array.isArray(this.cartObjects)) return 0
        
        const line = this.cartObjects.find(line => {
          return line.item && line.item.id === this.product.id
        })

        return line ? line.amount : 0
      }
  }
}
</script>

<style scoped>
.product-page {
  max-width: 1100px;
  margin: 0 auto;
}

.product-back-link {
  display: inline-block;
  margin: 4px 0 10px;
  font-size: 12px;
  color: #142f22;
  text-decoration: underline;
}

.product-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1.8fr);
  gap: 20px;
  margin-top: 6px;
}

.product-image-col {
  align-self: flex-start;
}

.product-image-wrap {
  background: #f6e4bf;
  border: 1px solid #cfa66f;
  border-radius: 6px;
  padding: 8px;
}

.product-image-full {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 4px;
}

.product-info-col {
  background: #f6e4bf;
  border: 1px solid #cfa66f;
  border-radius: 6px;
  padding: 10px 12px;
}

.product-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.product-value {
  margin-left: 6px;
  font-size: 13px;
}

.product-catno,
.product-meta,
.product-price-line {
  margin: 4px 0;
}

.product-price-main {
  margin-left: 6px;
  font-weight: 600;
  color: #cd683a;
  font-size: 15px;
}

.product-qty-row {
  margin-top: 10px;
}

.product-qty-input {
  margin-left: 6px;
  width: 60px;
  padding: 3px 6px;
  border-radius: 4px;
  border: 1px solid #cfa66f;
  background: #fff8e7;
  font-size: 13px;
  text-align: center;
}

.product-actions {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-add-btn {
  padding: 6px 14px;
  border-radius: 6px;
  border: 1px solid #142f22;
  background: #142f22;
  color: #e0c494;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  cursor: pointer;
}

.product-add-btn:hover {
  background: #1d402f;
}

.product-secondary-link {
  font-size: 12px;
  color: #142f22;
  text-decoration: underline;
}

.product-qty-row {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-in-cart-hint {
  font-size: 12px;
  color: #7a5a32;
  padding: 2px 6px;
  border-radius: 4px;
  background: #fff3d9;
}
</style>
