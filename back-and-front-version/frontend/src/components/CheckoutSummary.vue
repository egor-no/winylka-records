<template>
  <aside class="checkout-summary">
    <h3 class="checkout-summary-title">Order summary</h3>

    <TransitionGroup
      name="cart-item"
      tag="ul"
      class="checkout-items"
    >
      <li
        v-for="line in lines"
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
            @click="onChangeAmount(line, line.amount - 1)"
          >
            −
          </button>
          <input
            type="number"
            min="1"
            class="qty-input"
            v-model.number="line.amount"
            @change="onChangeAmount(line, line.amount)"
          >
          <button
            type="button"
            class="qty-btn"
            @click="onChangeAmount(line, line.amount + 1)"
          >
            +
          </button>

          <button
            type="button"
            class="remove-btn"
            @click="onRemove(line)"
            title="Remove from cart"
          >
            🗑
          </button>
        </div>
      </li>
    </TransitionGroup>

    <dl class="checkout-totals" v-if="lines.length">
      <div class="checkout-totals-row">
        <dt>Items total</dt>
        <dd>{{ currency(total) }}</dd>
      </div>
      <div class="checkout-totals-row checkout-totals-row--grand">
        <dt>Total</dt>
        <dd>{{ currency(total) }}</dd>
      </div>
    </dl>
  </aside>
</template>

<script>
import { formatCurrency } from '../utils/formatters'

export default {
  name: 'CheckoutSummary',
  props: {
    lines: {
      type: Array,
      default: () => []
    },
    total: {
      type: Number,
      default: 0
    }
  },
  emits: ['change-amount', 'remove-line'],
  methods: {
    currency(value) {
      return formatCurrency(value)
    },
    onChangeAmount(line, newAmount) {
        if (newAmount <= 0 || Number.isNaN(newAmount)) {
            this.$emit('remove-line', line)
            return
        }
        this.$emit('change-amount', line, newAmount)
    },
    onRemove(line) {
      this.$emit('remove-line', line)
    }
  }
}
</script>

<style scoped>
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
