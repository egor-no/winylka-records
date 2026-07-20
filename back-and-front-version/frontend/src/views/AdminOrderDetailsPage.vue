<template>
  <section class="admin-page">
    <div class="admin-window">
      <div class="admin-titlebar">
        <span>ORDER #{{ id }}</span>

        <span class="admin-titlebar-version">
          {{ order ? formatStatus(order.status) : 'DETAILS' }}
        </span>
      </div>

      <div class="admin-toolbar">
        <button
          type="button"
          class="toolbar-btn"
          @click="goBack"
        >
          ← Back to orders
        </button>

        <button
          type="button"
          class="toolbar-btn"
          :disabled="loading"
          @click="loadOrder"
        >
          Refresh
        </button>

        <span
          v-if="order"
          class="order-status-badge"
          :class="statusClass(order.status)"
        >
          {{ formatStatus(order.status) }}
        </span>
      </div>

      <div class="admin-content">
        <p
          v-if="loading"
          class="admin-status"
        >
          Loading order...
        </p>

        <div
          v-else-if="error"
          class="admin-error"
        >
          {{ error }}
        </div>

        <template v-else-if="order">
          <div class="order-details-grid">
            <section class="order-panel">
              <div class="order-panel-title">
                Customer
              </div>

              <div class="order-panel-content">
                <div class="order-detail-row">
                  <span class="order-detail-label">
                    Name
                  </span>

                  <strong>
                      {{ order.customerFullName  || '—' }}
                  </strong>
                </div>

                <div class="order-detail-row">
                  <span class="order-detail-label">
                    Email
                  </span>

                  <span>
                    {{ order.customerEmail  || '—' }}
                  </span>
                </div>

                <div class="order-detail-row">
                  <span class="order-detail-label">
                    Phone
                  </span>

                  <span>
                    {{ order.customerPhone || '—' }}
                  </span>
                </div>

                <div class="order-detail-row">
                  <span class="order-detail-label">
                    Created
                  </span>

                  <span>
                    {{ formatDate(order.createdAt) }}
                  </span>
                </div>
              </div>
            </section>

            <section class="order-panel">
              <div class="order-panel-title">
                Delivery
              </div>

              <div class="order-panel-content">
                <div class="order-detail-row">
                  <span class="order-detail-label">
                    Address
                  </span>

                  <span>
                    {{ formatShippingAddress(order) }}
                  </span>
                </div>

                <div class="order-detail-row">
                  <span class="order-detail-label">
                    Tracking
                  </span>

                  <span class="tracking-number">
                    {{ order.trackingNumber || 'Not assigned' }}
                  </span>
                </div>

                <div class="order-detail-row">
                  <span class="order-detail-label">
                    Shipped
                  </span>

                  <span>
                    {{ formatDate(order.shippedAt) }}
                  </span>
                </div>
              </div>
            </section>
          </div>

          <section
            v-if="order.comment || order.customerComment"
            class="order-panel order-comment-panel"
          >
            <div class="order-panel-title">
              Customer comment
            </div>

            <div class="order-panel-content">
              {{ order.comment || order.customerComment }}
            </div>
          </section>

          <section class="order-panel order-items-panel">
            <div class="order-panel-title">
              Order items
            </div>

            <div class="order-panel-content order-items-content">
              <p
                v-if="!orderItems.length"
                class="admin-status"
              >
                No order items found.
              </p>

              <div
                v-else
                class="order-items-table"
              >
                <div class="order-items-header">
                  <span>Product</span>
                  <span>Price</span>
                  <span>Qty</span>
                  <span>Total</span>
                </div>

                <div
                  v-for="item in orderItems"
                  :key="item.productId"
                  class="order-item-row"
                >
                  <div class="order-item-product">
                    <strong>
                      {{ item.artist }}
                    </strong>

                    <span>
                      {{ item.album }}
                    </span>
                  </div>

                  <span>
                    {{ formatMoney(item.unitPrice) }}
                  </span>

                  <span>
                    {{ item.amount }}
                  </span>

                  <strong>
                    {{ formatMoney(item.lineTotal) }}
                  </strong>
                </div>
              </div>
            </div>
          </section>

          <div class="order-summary">
            <span>
              {{ totalItems }} item(s)
            </span>

            <strong>
              Total: {{ formatMoney(orderTotal) }}
            </strong>
          </div>

          <section
            v-if="order.status === 'NEW'"
            class="order-panel shipping-panel"
          >
            <div class="order-panel-title">
              Ship order
            </div>

            <div class="order-panel-content">
              <form
                class="shipping-form"
                @submit.prevent="shipOrder"
              >
                <label class="shipping-label">
                  Tracking number

                  <input
                    v-model.trim="trackingNumber"
                    type="text"
                    class="shipping-input"
                    maxlength="100"
                    placeholder="Enter tracking number"
                    :disabled="saving"
                  >
                </label>

                <button
                  type="submit"
                  class="toolbar-btn ship-order-btn"
                  :disabled="saving || !trackingNumber"
                >
                  {{ saving
                    ? 'Shipping...'
                    : 'Mark as shipped'
                  }}
                </button>
              </form>
            </div>
          </section>

          <section
            v-else-if="order.status === 'SHIPPED'"
            class="shipped-message"
          >
            This order has already been shipped.
          </section>
        </template>
      </div>

      <div class="admin-statusbar">
        <span>{{ statusText }}</span>

        <span>
          {{ loading || saving ? 'WORKING...' : 'READY' }}
        </span>
      </div>
    </div>
  </section>
</template>

<script>
import { adminOrdersApi } from '../api/adminOrders'

export default {
  name: 'AdminOrderDetailsPage',

  props: {
    id: {
      type: String,
      required: true
    }
  },

  data() {
    return {
      order: null,
      loading: false,
      saving: false,
      error: '',
      statusText: 'Order details',
      trackingNumber: ''
    }
  },

  computed: {
    orderItems() {
      return this.order?.items || []
    },

    totalItems() {
      return this.order?.itemsCount || 0
    },

    orderTotal() {
      return Number(this.order?.itemsTotal || 0)
    }
  },

  created() {
    this.loadOrder()
  },

  watch: {
    id() {
      this.loadOrder()
    }
  },

  methods: {
    async loadOrder() {
      this.loading = true
      this.error = ''
      this.statusText = `Loading order #${this.id}...`

      try {
        const { data } =
          await adminOrdersApi.getById(this.id)

        this.order = data
        this.trackingNumber =
          data.trackingNumber || ''

        this.statusText =
          `Order #${this.id} loaded`
      } catch (e) {
        console.error(e)

        this.error =
          e?.response?.data?.message ||
          `Failed to load order #${this.id}.`

        this.statusText = 'Load error'
      } finally {
        this.loading = false
      }
    },

    async shipOrder() {
      if (
        !this.order ||
        this.order.status !== 'NEW'
      ) {
        return
      }

      const trackingNumber =
        this.trackingNumber.trim()

      if (!trackingNumber) {
        return
      }

      const confirmed = window.confirm(
        `Mark order #${this.id} as shipped?\n\n` +
        `Tracking number: ${trackingNumber}`
      )

      if (!confirmed) {
        return
      }

      this.saving = true
      this.statusText =
        `Shipping order #${this.id}...`

      try {
        const { data } =
          await adminOrdersApi.ship(
            this.id,
            trackingNumber
          )

        if (data && typeof data === 'object') {
          this.order = data
        } else {
          await this.loadOrder()
        }

        this.statusText =
          `Order #${this.id} marked as shipped`
      } catch (e) {
        console.error(e)

        alert(
          e?.response?.data?.message ||
          'Failed to ship order.'
        )

        this.statusText = 'Shipping error'
      } finally {
        this.saving = false
      }
    },

    goBack() {
      this.$router.push({
        name: 'admin-orders'
      })
    },

    formatShippingAddress(order) {
      return [
        order.shippingPostalCode,
        order.shippingCity,
        order.shippingAddress
      ]
        .filter(Boolean)
        .join(', ') || '—'
    },

    formatMoney(value) {
      const amount = Number(value)

      if (!Number.isFinite(amount)) {
        return '—'
      }

      return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
      }).format(amount)
    },

    formatDate(value) {
      if (!value) {
        return '—'
      }

      const date = new Date(value)

      if (Number.isNaN(date.getTime())) {
        return value
      }

      return new Intl.DateTimeFormat('en-GB', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(date)
    },

    formatStatus(status) {
      return status || 'UNKNOWN'
    },

    statusClass(status) {
      return {
        'status-new': status === 'NEW',
        'status-shipped': status === 'SHIPPED'
      }
    }
  }
}
</script>

<style scoped>
.order-status-badge {
  min-width: 76px;
  margin-left: auto;
  padding: 3px 8px;
  color: #fff8e7;
  background: #6f6757;
  border: 1px solid #3c2a1a;
  font-family: "Courier New", monospace;
  font-size: 10px;
  font-weight: bold;
  text-align: center;
}

.order-status-badge.status-new {
  color: #142f22;
  background: #e5ca53;
  border-color: #806b15;
}

.order-status-badge.status-shipped {
  color: #fff8e7;
  background: #28543d;
  border-color: #142f22;
}

.order-details-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.order-panel {
  background: #d8c59d;
  border-top: 2px solid #fff3d9;
  border-left: 2px solid #fff3d9;
  border-right: 2px solid #60492f;
  border-bottom: 2px solid #60492f;
}

.order-panel-title {
  padding: 5px 7px;
  color: #e0c494;
  background: #28543d;
  font-family: "Courier New", monospace;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.order-panel-content {
  padding: 9px;
}

.order-detail-row {
  display: grid;
  grid-template-columns: 100px minmax(0, 1fr);
  gap: 10px;
  padding: 5px 0;
  font-size: 12px;
  border-bottom: 1px dotted #8b714d;
}

.order-detail-row:last-child {
  border-bottom: 0;
}

.order-detail-label {
  color: #765d3e;
  font-family: "Courier New", monospace;
  font-size: 10px;
  font-weight: bold;
  text-transform: uppercase;
}

.tracking-number {
  font-family: "Courier New", monospace;
  overflow-wrap: anywhere;
}

.order-comment-panel,
.order-items-panel,
.shipping-panel {
  margin-top: 12px;
}

.order-items-content {
  padding: 0;
}

.order-items-table {
  width: 100%;
}

.order-items-header,
.order-item-row {
  display: grid;
  grid-template-columns:
    minmax(220px, 1fr)
    100px
    60px
    100px;
  gap: 10px;
  align-items: center;
}

.order-items-header {
  padding: 6px 9px;
  color: #3c2a1a;
  background: #c5af82;
  border-bottom: 2px solid #60492f;
  font-family: "Courier New", monospace;
  font-size: 10px;
  font-weight: bold;
  text-transform: uppercase;
}

.order-item-row {
  padding: 8px 9px;
  background: #fff8e7;
  border-bottom: 1px solid #c9af83;
  font-size: 12px;
}

.order-item-row:last-child {
  border-bottom: 0;
}

.order-item-product {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 2px;
}

.order-item-product strong,
.order-item-product span {
  overflow-wrap: anywhere;
}

.order-item-product small {
  color: #765d3e;
  font-family: "Courier New", monospace;
}

.order-summary {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 24px;
  margin-top: 10px;
  padding: 8px 10px;
  color: #fff8e7;
  background: #142f22;
  border: 2px solid #60492f;
  font-family: "Courier New", monospace;
  font-size: 12px;
}

.shipping-form {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.shipping-label {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 5px;
  color: #3c2a1a;
  font-family: "Courier New", monospace;
  font-size: 10px;
  font-weight: bold;
  text-transform: uppercase;
}

.shipping-input {
  box-sizing: border-box;
  width: 100%;
  padding: 6px 8px;
  background: #fff8e7;
  border-top: 2px solid #60492f;
  border-left: 2px solid #60492f;
  border-right: 2px solid #fff3d9;
  border-bottom: 2px solid #fff3d9;
  color: #3c2a1a;
  font-family: "Courier New", monospace;
}

.shipping-input:focus {
  outline: 1px dotted #142f22;
}

.ship-order-btn {
  min-width: 145px;
}

.shipped-message {
  margin-top: 12px;
  padding: 10px;
  color: #fff8e7;
  background: #28543d;
  border: 2px solid #142f22;
  font-family: "Courier New", monospace;
  font-size: 12px;
  font-weight: bold;
  text-align: center;
}

@media (max-width: 700px) {
  .order-details-grid {
    grid-template-columns: 1fr;
  }

  .order-items-header {
    display: none;
  }

  .order-item-row {
    grid-template-columns: 1fr 1fr;
  }

  .order-item-product {
    grid-column: 1 / -1;
  }

  .shipping-form {
    align-items: stretch;
    flex-direction: column;
  }

  .ship-order-btn {
    align-self: flex-end;
  }
}
</style>