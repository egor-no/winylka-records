<template>
  <section class="admin-page">
    <div class="admin-window">
      <div class="admin-titlebar">
        <span>WINYLKA ORDER MANAGER</span>
        <span class="admin-titlebar-version">v1.0</span>
      </div>

      <div class="admin-toolbar">
        <button
          type="button"
          class="toolbar-btn"
          :disabled="loading"
          @click="loadOrders"
        >
          Refresh
        </button>

        <div class="order-status-filters">
          <button
            v-for="filter in statusFilters"
            :key="filter.value"
            type="button"
            class="toolbar-btn order-filter-btn"
            :class="{ active: statusFilter === filter.value }"
            @click="statusFilter = filter.value"
          >
            {{ filter.label }}
          </button>
        </div>

        <div class="admin-toolbar-right">
          <label class="admin-search-label">
            Search:

            <input
              v-model.trim="searchQuery"
              type="search"
              class="admin-search-input"
              placeholder="Order, customer or email"
            >
          </label>

          <span class="admin-result-badge">
            {{ filteredOrders.length }} / {{ orders.length }} orders
          </span>
        </div>
      </div>

      <div class="admin-content">
        <p
          v-if="loading"
          class="admin-status"
        >
          Loading order database...
        </p>

        <div
          v-else-if="error"
          class="admin-error"
        >
          {{ error }}
        </div>

        <p
          v-else-if="!filteredOrders.length"
          class="admin-status"
        >
          {{ orders.length
            ? 'No orders found.'
            : 'Order database is empty.'
          }}
        </p>

        <div
          v-else
          class="admin-order-list"
        >
          <article
            v-for="order in filteredOrders"
            :key="order.id"
            class="admin-order-card"
          >
            <div class="order-card-header">
              <div class="order-number">
                ORDER #{{ order.id }}
              </div>

              <span
                class="order-status-badge"
                :class="statusClass(order.status)"
              >
                {{ formatStatus(order.status) }}
              </span>
            </div>

            <div class="order-card-body">
              <div class="order-info-grid">
                <div class="order-field">
                  <span class="order-field-label">
                    Customer
                  </span>

                  <strong>
                    {{ order.customerFullName || '—' }}
                  </strong>
                </div>

                <div class="order-field">
                  <span class="order-field-label">
                    Email
                  </span>

                  <span>
                    {{ order.customerEmail || '—' }}
                  </span>
                </div>

                <div class="order-field">
                  <span class="order-field-label">
                    Created
                  </span>

                  <span>
                    {{ formatDate(order.createdAt) }}
                  </span>
                </div>

                <div class="order-field">
                  <span class="order-field-label">
                    Items
                  </span>

                  <span>
                    {{ itemCount(order) }}
                  </span>
                </div>

                <div class="order-field">
                  <span class="order-field-label">
                    Total
                  </span>

                  <strong>
                    {{ formatMoney(order.itemsTotal) }}
                  </strong>
                </div>

                <div
                  v-if="order.trackingNumber"
                  class="order-field"
                >
                  <span class="order-field-label">
                    Tracking
                  </span>

                  <span class="tracking-number">
                    {{ order.trackingNumber }}
                  </span>
                </div>
              </div>

              <button
                type="button"
                class="toolbar-btn order-open-btn"
                @click="openOrder(order.id)"
              >
                Open
              </button>
            </div>
          </article>
        </div>
      </div>

      <div class="admin-statusbar">
        <span>{{ statusText }}</span>
        <span>{{ loading ? 'WORKING...' : 'READY' }}</span>
      </div>
    </div>
  </section>
</template>

<script>
import { adminOrdersApi } from '../api/adminOrders'

export default {
  name: 'AdminOrdersPage',

  data() {
    return {
      orders: [],
      loading: false,
      error: '',
      statusText: 'Order database',
      searchQuery: '',
      statusFilter: 'ALL',

      statusFilters: [
        {
          label: 'All',
          value: 'ALL'
        },
        {
          label: 'New',
          value: 'NEW'
        },
        {
          label: 'Shipped',
          value: 'SHIPPED'
        }
      ]
    }
  },

  computed: {
    filteredOrders() {
      const query = this.searchQuery
        .trim()
        .toLowerCase()

      return [...this.orders]
        .filter(order => {
          if (this.statusFilter === 'ALL') {
            return true
          }

          return order.status === this.statusFilter
        })
        .filter(order => {
          if (!query) {
            return true
          }

          const searchableText = [
            order.id,
            order.customerFullName,
            order.customerEmail,
            order.status
            ]

          return searchableText.includes(query)
        })
        .sort((a, b) => {
          const firstDate = new Date(a.createdAt || 0).getTime()
          const secondDate = new Date(b.createdAt || 0).getTime()

          if (firstDate !== secondDate) {
            return secondDate - firstDate
          }

          return Number(b.id) - Number(a.id)
        })
    }
  },

  created() {
    this.loadOrders()
  },

  methods: {
    async loadOrders() {
      this.loading = true
      this.error = ''
      this.statusText = 'Loading...'

      try {
        const { data } = await adminOrdersApi.getAll()

        this.orders = Array.isArray(data)
          ? data
          : data.content || []

        this.statusText =
          `${this.orders.length} order(s) loaded`
      } catch (e) {
        console.error(e)

        this.error =
          e?.response?.data?.message ||
          'Failed to load orders.'

        this.statusText = 'Load error'
      } finally {
        this.loading = false
      }
    },

    openOrder(orderId) {
      this.$router.push({
        name: 'admin-order-details',
        params: {
          id: orderId
        }
      })
    },

    itemCount(order) {
      return order.itemsCount
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
.admin-order-list {
  display: grid;
  gap: 10px;
}

.admin-order-card {
  background: #d8c59d;
  border-top: 2px solid #fff3d9;
  border-left: 2px solid #fff3d9;
  border-right: 2px solid #60492f;
  border-bottom: 2px solid #60492f;
}

.order-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 5px 7px;
  background: #c5af82;
  border-bottom: 1px solid #7a5a32;
}

.order-number {
  color: #3c2a1a;
  font-family: "Courier New", monospace;
  font-size: 12px;
  font-weight: bold;
}

.order-status-badge {
  min-width: 76px;
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

.order-card-body {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 9px;
}

.order-info-grid {
  display: grid;
  flex: 1;
  grid-template-columns:
    minmax(150px, 1.2fr)
    minmax(180px, 1.4fr)
    minmax(135px, 1fr)
    minmax(60px, 0.5fr)
    minmax(90px, 0.7fr);
  gap: 10px 16px;
}

.order-field {
  min-width: 0;
  font-size: 12px;
}

.order-field-label {
  display: block;
  margin-bottom: 2px;
  color: #765d3e;
  font-family: "Courier New", monospace;
  font-size: 9px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.order-field span,
.order-field strong {
  overflow-wrap: anywhere;
}

.tracking-number {
  font-family: "Courier New", monospace;
}

.order-open-btn {
  flex: 0 0 auto;
  min-width: 72px;
}

.order-status-filters {
  display: flex;
  gap: 4px;
}

.order-filter-btn.active {
  color: #e0c494;
  background: #142f22;
  border-top-color: #60492f;
  border-left-color: #60492f;
  border-right-color: #fff3d9;
  border-bottom-color: #fff3d9;
}

.admin-toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.admin-search-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  white-space: nowrap;
}

.admin-search-input {
  box-sizing: border-box;
  min-width: 220px;
  padding: 3px 8px;
  border: 1px solid #cfa66f;
  border-radius: 999px;
  background: #fff8e7;
  color: #3c2a1a;
  font-family: inherit;
  font-size: 12px;
}

.admin-search-input:focus {
  outline: 1px solid #142f22;
  outline-offset: 1px;
}

@media (max-width: 900px) {
  .admin-toolbar {
    flex-wrap: wrap;
  }

  .admin-toolbar-right {
    width: 100%;
    margin-left: 0;
  }

  .order-info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .admin-toolbar-right {
    align-items: stretch;
    flex-direction: column;
  }

  .admin-search-label {
    width: 100%;
  }

  .admin-search-input {
    flex: 1;
    min-width: 0;
  }

  .admin-result-badge {
    align-self: flex-end;
  }

  .order-card-body {
    align-items: stretch;
    flex-direction: column;
  }

  .order-info-grid {
    grid-template-columns: 1fr;
  }

  .order-open-btn {
    align-self: flex-end;
  }
}
</style>