<template>
  <div
    class="admin-modal-overlay"
    @click.self="$emit('close')"
  >
    <section class="stock-dialog">
      <div class="stock-dialog-titlebar">
        <span>ADD STOCK</span>

        <button
          type="button"
          class="window-close-btn"
          :disabled="saving"
          @click="$emit('close')"
        >
          ×
        </button>
      </div>

      <form
        class="stock-form"
        @submit.prevent="submitForm"
      >
        <div class="stock-product-info">
          <strong>{{ product.artist }}</strong>
          <span>{{ product.name }}</span>
        </div>

        <div class="current-stock">
          Current stock:
          <strong>{{ product.stockQuantity || 0 }}</strong>
        </div>

        <label class="stock-field">
          <span>Quantity to add</span>

          <input
            ref="quantityInput"
            v-model.number="quantity"
            type="number"
            min="1"
            step="1"
            required
          >
        </label>

        <p
          v-if="error"
          class="stock-error"
        >
          {{ error }}
        </p>

        <div class="new-stock-preview">
          New stock:
          <strong>{{ resultingStock }}</strong>
        </div>

        <div class="stock-actions">
          <button
            type="submit"
            class="dialog-btn dialog-btn-primary"
            :disabled="saving"
          >
            {{ saving ? 'SAVING...' : 'ADD' }}
          </button>

          <button
            type="button"
            class="dialog-btn"
            :disabled="saving"
            @click="$emit('close')"
          >
            CANCEL
          </button>
        </div>
      </form>
    </section>
  </div>
</template>

<script>
export default {
  name: 'AdminStockForm',

  props: {
    product: {
      type: Object,
      required: true
    },

    saving: {
      type: Boolean,
      default: false
    }
  },

  emits: ['save', 'close'],

  data() {
    return {
      quantity: 1,
      error: ''
    }
  },

  computed: {
    resultingStock() {
      const current = Number(this.product.stockQuantity || 0)
      const addition = Number(this.quantity || 0)

      return current + addition
    }
  },

  mounted() {
    this.$refs.quantityInput?.focus()
    this.$refs.quantityInput?.select()
  },

  methods: {
    submitForm() {
      const quantity = Number(this.quantity)

      if (!Number.isInteger(quantity) || quantity <= 0) {
        this.error =
          'Quantity must be a positive whole number.'
        return
      }

      this.error = ''
      this.$emit('save', quantity)
    }
  }
}
</script>

<style scoped>
.admin-modal-overlay {
  position: fixed;
  z-index: 1000;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(20, 18, 14, 0.66);
}

.stock-dialog {
  width: min(390px, 100%);
  background: #d8c59d;
  border-top: 3px solid #fff3d9;
  border-left: 3px solid #fff3d9;
  border-right: 3px solid #60492f;
  border-bottom: 3px solid #60492f;
  box-shadow: 7px 7px 0 rgba(0, 0, 0, 0.45);
}

.stock-dialog-titlebar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 28px;
  padding: 3px 5px 3px 8px;
  color: #e0c494;
  font-family: "Courier New", monospace;
  font-size: 13px;
  font-weight: bold;
  background: linear-gradient(
    90deg,
    #142f22,
    #28543d,
    #142f22
  );
}

.window-close-btn {
  width: 24px;
  height: 22px;
  padding: 0;
  color: #3c2a1a;
  font-size: 18px;
  line-height: 16px;
  cursor: pointer;
  background: #d8c59d;
  border-top: 2px solid #fff3d9;
  border-left: 2px solid #fff3d9;
  border-right: 2px solid #60492f;
  border-bottom: 2px solid #60492f;
}

.stock-form {
  padding: 14px;
}

.stock-product-info {
  padding: 8px;
  background: #fff8e7;
  border-top: 2px solid #60492f;
  border-left: 2px solid #60492f;
  border-right: 2px solid #fff3d9;
  border-bottom: 2px solid #fff3d9;
}

.stock-product-info strong,
.stock-product-info span {
  display: block;
}

.stock-product-info span {
  margin-top: 2px;
  font-size: 12px;
}

.current-stock,
.new-stock-preview {
  margin-top: 10px;
  font-family: "Courier New", monospace;
  font-size: 12px;
}

.stock-field {
  display: block;
  margin-top: 12px;
}

.stock-field span {
  display: block;
  margin-bottom: 4px;
  font-family: "Courier New", monospace;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
}

.stock-field input {
  box-sizing: border-box;
  width: 100%;
  padding: 7px;
  font-family: inherit;
  font-size: 15px;
  background: #fff8e7;
  border-top: 2px solid #60492f;
  border-left: 2px solid #60492f;
  border-right: 2px solid #fff3d9;
  border-bottom: 2px solid #fff3d9;
  border-radius: 0;
}

.stock-error {
  padding: 7px;
  color: #fff8e7;
  font-family: "Courier New", monospace;
  font-size: 11px;
  background: #8f2e2e;
  border: 2px solid #551414;
}

.stock-actions {
  display: flex;
  justify-content: flex-end;
  gap: 7px;
  margin-top: 16px;
  padding-top: 10px;
  border-top: 1px solid #8e744d;
}

.dialog-btn {
  min-width: 82px;
  padding: 6px 12px;
  font-family: inherit;
  font-size: 11px;
  font-weight: bold;
  cursor: pointer;
  background: #e0c494;
  border-top: 2px solid #fff3d9;
  border-left: 2px solid #fff3d9;
  border-right: 2px solid #60492f;
  border-bottom: 2px solid #60492f;
}

.dialog-btn-primary {
  color: #e0c494;
  background: #142f22;
  border-top-color: #416b54;
  border-left-color: #416b54;
  border-right-color: #071b11;
  border-bottom-color: #071b11;
}

.dialog-btn:disabled,
.window-close-btn:disabled {
  cursor: default;
  opacity: 0.55;
}
</style>