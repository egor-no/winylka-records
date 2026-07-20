<template>
  <div
    v-if="visible"
    class="app-notice-overlay"
    @click.self="$emit('close')"
  >
    <div class="app-notice">
      <div class="app-notice-header">
        <span>Winylka notification</span>

        <button
          type="button"
          class="app-notice-close"
          @click="$emit('close')"
        >
          ×
        </button>
      </div>

      <div class="app-notice-body">
        <p class="app-notice-message">
          {{ message }}
        </p>

        <div
          v-if="product && subscriptionType"
          class="app-notice-subscription"
        >
          <p class="app-notice-subscription-title">
            {{ subscriptionTitle }}
          </p>

          <div class="app-notice-form">
            <input
              v-model.trim="email"
              type="email"
              placeholder="Your email"
              :disabled="loading"
              @keyup.enter="subscribe"
            >

            <button
              type="button"
              :disabled="loading"
              @click="subscribe"
            >
              {{ loading ? 'Saving...' : 'Notify me' }}
            </button>
          </div>

          <p
            v-if="resultMessage"
            class="app-notice-success"
          >
            {{ resultMessage }}
          </p>

          <p
            v-if="errorMessage"
            class="app-notice-error"
          >
            {{ errorMessage }}
          </p>
        </div>

        <div class="app-notice-actions">
          <button
            type="button"
            class="app-notice-ok"
            @click="$emit('close')"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { stockSubscriptionsApi } from '../api/stockSubscriptions'

export default {
  name: 'AppNotice',

  props: {
    visible: {
      type: Boolean,
      required: true
    },

    message: {
      type: String,
      default: ''
    },

    product: {
      type: Object,
      default: null
    }, 

    subscriptionType: {
      type: String,
      default: null,
      validator(value) {
        return value === null
          || value === 'OUT_OF_STOCK'
          || value === 'STOCK_INCREASE'
      }
    }
  },

  emits: ['close'],

  data() {
    return {
      email: '',
      loading: false,
      resultMessage: '',
      errorMessage: ''
    }
  },

  watch: {
    visible(newValue) {
      if (newValue) {
        this.email = ''
        this.loading = false
        this.resultMessage = ''
        this.errorMessage = ''
      }
    }
  },

  computed: {
    subscriptionTitle() {
      if (this.subscriptionType === 'STOCK_INCREASE') {
        return 'Notify me when more copies are added'
      }

      return 'Notify me when this item is back in stock'
    },

    subscriptionButtonText() {
      if (this.subscriptionType === 'STOCK_INCREASE') {
        return 'Notify me'
      }

      return 'Notify me'
    }
  },

  methods: {
   async subscribe() {
      if (
        !this.product
        || !this.subscriptionType
        || this.loading
      ) {
        return
      }

      const email = this.email.trim()

      if (!email) {
        this.errorMessage = 'Please enter your email address.'
        return
      }

      this.loading = true
      this.resultMessage = ''
      this.errorMessage = ''

      try {
        const { data } = await stockSubscriptionsApi.subscribe(
          this.product.id,
          email,
          this.subscriptionType
        )

        this.resultMessage = data.message
        this.email = ''
      } catch (e) {
        console.error(e)

        this.errorMessage =
          e?.response?.data?.message
          || 'Failed to create subscription.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
<style scoped>
.app-notice-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(20, 47, 34, 0.55);
  backdrop-filter: blur(2px);
}

.app-notice {
  width: min(460px, 100%);
  overflow: hidden;
  border: 1px solid #142f22;
  border-radius: 8px;
  background: #f6e4bf;
  box-shadow: 0 14px 40px rgba(20, 47, 34, 0.28);
}

.app-notice-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  background: #142f22;
  color: #e0c494;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.app-notice-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: 1px solid #e0c494;
  border-radius: 4px;
  background: transparent;
  color: #e0c494;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
}

.app-notice-close:hover {
  background: #e0c494;
  color: #142f22;
}

.app-notice-body {
  padding: 16px;
}

.app-notice-message {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  color: #3c2a1a;
}

.app-notice-subscription {
  margin-top: 14px;
  padding: 12px;
  border: 1px solid #cfa66f;
  border-radius: 6px;
  background: #fff3d9;
}

.app-notice-subscription-title {
  margin: 0 0 9px;
  color: #3c2a1a;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.app-notice-form {
  display: flex;
  align-items: stretch;
  gap: 8px;
}

.app-notice-form input {
  min-width: 0;
  flex: 1;
  padding: 7px 9px;
  border: 1px solid #cfa66f;
  border-radius: 5px;
  outline: none;
  background: #fff8e7;
  color: #3c2a1a;
  font-size: 13px;
}

.app-notice-form input:focus {
  border-color: #142f22;
  box-shadow: 0 0 0 2px rgba(20, 47, 34, 0.12);
}

.app-notice-form button {
  padding: 7px 12px;
  border: 1px solid #142f22;
  border-radius: 5px;
  background: #142f22;
  color: #e0c494;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  white-space: nowrap;
  cursor: pointer;
}

.app-notice-form button:hover:not(:disabled) {
  background: #1d402f;
}

.app-notice-form input:disabled,
.app-notice-form button:disabled {
  opacity: 0.6;
  cursor: default;
}

.app-notice-success,
.app-notice-error {
  margin: 9px 0 0;
  font-size: 12px;
  line-height: 1.4;
}

.app-notice-success {
  color: #075105;
}

.app-notice-error {
  color: #a22a21;
}

.app-notice-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}

.app-notice-ok {
  padding: 6px 14px;
  border: 1px solid #cfa66f;
  border-radius: 5px;
  background: transparent;
  color: #142f22;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  cursor: pointer;
}

.app-notice-ok:hover {
  border-color: #142f22;
  background: #142f22;
  color: #e0c494;
}

@media (max-width: 520px) {
  .app-notice-overlay {
    padding: 12px;
  }

  .app-notice-form {
    flex-direction: column;
  }

  .app-notice-form button {
    width: 100%;
  }
}
</style>