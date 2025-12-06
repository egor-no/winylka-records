<template>
  <section class="checkout-page">
    <div class="section-header">
      <h2 class="section-title">Checkout</h2>
    </div>

    <!-- если корзина пуста -->
    <div v-if="!cartObjects.length" class="checkout-empty">
      <p class="cart-note">
        Your cart is empty. Go back to the catalogue to add some records.
      </p>
      <RouterLink to="/catalogue" class="nav-link">
        Back to catalogue
      </RouterLink>
    </div>

    <!-- если есть товары -->
    <div v-else class="checkout-layout">
      <!-- Левая колонка: форма -->
      <form class="checkout-form" @submit.prevent="submitOrder">
        <fieldset class="checkout-block">
          <legend class="checkout-block-title">Contact details</legend>

          <label class="checkout-field">
            <span class="checkout-label">Full name *</span>
            <input
              v-model.trim="fullName"
              type="text"
              class="checkout-input"
              required
            >
          </label>

          <label class="checkout-field">
            <span class="checkout-label">Email *</span>
            <input
              v-model.trim="email"
              type="email"
              class="checkout-input"
              required
            >
          </label>

          <label class="checkout-field">
            <span class="checkout-label">Phone</span>
            <input
              v-model.trim="phone"
              type="tel"
              class="checkout-input"
              placeholder="+7 ..."
            >
          </label>
        </fieldset>

        <fieldset class="checkout-block">
          <legend class="checkout-block-title">Shipping address</legend>

          <label class="checkout-field">
            <span class="checkout-label">City *</span>
            <input
              v-model.trim="city"
              type="text"
              class="checkout-input"
              required
            >
          </label>

          <label class="checkout-field">
            <span class="checkout-label">Address *</span>
            <input
              v-model.trim="address"
              type="text"
              class="checkout-input"
              placeholder="Street, house, apartment"
              required
            >
          </label>

          <label class="checkout-field">
            <span class="checkout-label">Postal code</span>
            <input
              v-model.trim="postalCode"
              type="text"
              class="checkout-input"
            >
          </label>
        </fieldset>

        <label class="checkout-field">
          <span class="checkout-label">Comment to order</span>
          <textarea
            v-model.trim="comment"
            rows="3"
            class="checkout-textarea"
            placeholder="Any wishes or additional info"
          />
        </label>

        <p v-if="formError" class="checkout-error">
          {{ formError }}
        </p>

        <button
          class="checkout-submit"
          type="submit"
          :disabled="submitting"
        >
          {{ submitting ? 'Placing order...' : 'Place order' }}
        </button>
      </form>

      <!-- Правая колонка: содержимое корзины с изменением количества -->
      <aside class="checkout-summary">
        <h3 class="checkout-summary-title">Order summary</h3>

        <ul class="checkout-items">
          <li
            v-for="line in localLines"
            :key="line.item.id"
            class="checkout-item"
          >
            <div class="checkout-item-main">
              <span class="checkout-item-name">
                {{ line.item.artist }} — {{ line.item.name }}
              </span>
              <span class="checkout-item-price">
                {{ currency(line.item.price * line.amount) }}
              </span>
            </div>

            <div class="checkout-item-controls">
              <button
                type="button"
                class="qty-btn"
                @click="changeAmount(line, line.amount - 1)"
              >
                −
              </button>
              <input
                type="number"
                min="1"
                class="qty-input"
                v-model.number="line.amount"
                @change="changeAmount(line, line.amount)"
              >
              <button
                type="button"
                class="qty-btn"
                @click="changeAmount(line, line.amount + 1)"
              >
                +
              </button>

              <button
                type="button"
                class="remove-btn"
                @click="removeLine(line)"
                title="Remove from cart"
              >
                🗑
              </button>
            </div>
          </li>
        </ul>

        <dl class="checkout-totals">
          <div class="checkout-totals-row">
            <dt>Items total</dt>
            <dd>{{ currency(itemsTotal) }}</dd>
          </div>
          <div class="checkout-totals-row checkout-totals-row--grand">
            <dt>Total</dt>
            <dd>{{ currency(itemsTotal) }}</dd>
          </div>
        </dl>
      </aside>
    </div>
  </section>
</template>

<script>
import { formatCurrency } from '../utils/formatters'

export default {
  name: 'CheckoutPage',
  props: {
    // ожидаем тот же формат, что и cartObjects в каталоге:
    // [{ item: product, amount: number }]
    cartObjects: {
      type: Array,
      default: () => []
    }
  },
  emits: ['place-order', 'update-cart'],
  data() {
    return {
      localLines: [],
      fullName: '',
      email: '',
      phone: '',
      city: '',
      address: '',
      postalCode: '',
      comment: '',
      submitting: false,
      formError: ''
    }
  },
  computed: {
    itemsTotal() {
      return this.localLines.reduce(
        (sum, line) => sum + Number(line.item.price) * line.amount,
        0
      )
    }
  },
  watch: {
    cartObjects: {
      immediate: true,
      handler(newVal) {
        // делаем локальную копию, чтобы не мутировать проп напрямую
        this.localLines = newVal.map(line => ({
          item: line.item,
          amount: line.amount
        }))
      }
    }
  },
  methods: {
    currency(value) {
      return formatCurrency(value)
    },
    changeAmount(line, newAmount) {
      if (newAmount < 1 || Number.isNaN(newAmount)) {
        newAmount = 1
      }
      line.amount = newAmount
      // можно эмитить наверх, если захочешь синхронизировать корзину
      this.$emit('update-cart', this.localLines)
    },
    removeLine(line) {
      this.localLines = this.localLines.filter(
        l => l.item.id !== line.item.id
      )
      this.$emit('update-cart', this.localLines)
    },
    submitOrder() {
      this.formError = ''

      if (!this.fullName || !this.email || !this.city || !this.address) {
        this.formError = 'Please fill in all required fields.'
        return
      }

      if (!this.localLines.length) {
        this.formError = 'Cart is empty.'
        return
      }

      const order = {
        customer: {
          fullName: this.fullName,
          email: this.email,
          phone: this.phone
        },
        shipping: {
          city: this.city,
          address: this.address,
          postalCode: this.postalCode
        },
        comment: this.comment,
        items: this.localLines,
        totals: {
          itemsTotal: this.itemsTotal
        },
        createdAt: new Date().toISOString()
      }

      this.submitting = true
      this.$emit('place-order', order)
      this.submitting = false
    }
  }
}
</script>

<style scoped>
.checkout-page {
  max-width: 1100px;
  margin: 0 auto;
}

.checkout-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(260px, 1fr);
  gap: 20px;
  margin-top: 12px;
}

.checkout-empty {
  margin-top: 12px;
}

.checkout-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.checkout-block {
  border: 1px solid #cfa66f;
  border-radius: 6px;
  padding: 10px 12px;
  background: #f6e4bf;
}

.checkout-block-title {
  padding: 0 4px;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.checkout-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
}

.checkout-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.checkout-input,
.checkout-textarea {
  border: 1px solid #cfa66f;
  border-radius: 4px;
  padding: 5px 8px;
  font-size: 13px;
  font-family: inherit;
  background: #fff8e7;
}

.checkout-textarea {
  resize: vertical;
}

.checkout-error {
  color: #8b0000;
  font-size: 12px;
}

.checkout-submit {
  margin-top: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #142f22;
  background: #142f22;
  color: #e0c494;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  cursor: pointer;
}

.checkout-submit:disabled {
  opacity: 0.6;
  cursor: default;
}

.checkout-summary {
  border: 1px solid #cfa66f;
  border-radius: 6px;
  padding: 10px 12px;
  background: #f6e4bf;
  align-self: flex-start;
}

.checkout-summary-title {
  margin: 0 0 6px;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.checkout-items {
  list-style: none;
  padding: 0;
  margin: 0 0 10px;
}

.checkout-item {
  padding: 6px 0;
  border-bottom: 1px solid #d6b88a;
}

.checkout-item:last-child {
  border-bottom: none;
}

.checkout-item-main {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
}

.checkout-item-name {
  font-weight: 500;
}

.checkout-item-price {
  color: #cd683a;
  font-weight: 600;
}

.checkout-item-controls {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.qty-btn {
  width: 22px;
  height: 22px;
  border-radius: 4px;
  border: 1px solid #cfa66f;
  background: #fff8e7;
  font-size: 14px;
  cursor: pointer;
}

.qty-input {
  width: 40px;
  padding: 2px 4px;
  border-radius: 4px;
  border: 1px solid #cfa66f;
  background: #fff8e7;
  font-size: 13px;
  text-align: center;
}

.remove-btn {
  margin-left: auto;
  border: none;
  background: transparent;
  cursor: pointer;
}

.checkout-totals {
  margin: 0;
}

.checkout-totals-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-top: 4px;
}

.checkout-totals-row--grand {
  margin-top: 8px;
  font-weight: 600;
}
</style>
