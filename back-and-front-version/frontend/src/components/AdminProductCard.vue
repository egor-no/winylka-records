<template>
  <article class="admin-product-card">
    <div class="admin-product-image-frame">
      <img
        v-if="product.img"
        :src="product.img"
        :alt="product.artist + ' — ' + product.name"
        class="admin-product-image"
      >

      <div
        v-else
        class="admin-product-no-image"
      >
        NO COVER
      </div>
    </div>

    <div class="admin-product-info">
      <div class="admin-product-title-row">
        <div>
          <h3 class="admin-product-title">
            {{ product.artist }}
          </h3>

          <p class="admin-product-name">
            {{ product.name }}
          </p>
        </div>

        <span class="admin-product-id">
          ID: {{ product.id }}
        </span>
      </div>

      <div class="admin-product-meta">
        <span>{{ product.format || '—' }}</span>
        <span>{{ product.catNo || 'NO CAT. NO.' }}</span>
        <span>{{ currency(product.price) }}</span>
      </div>

      <div class="admin-product-stock">
        <span class="admin-product-label">Stock:</span>

        <strong
          :class="{
            'stock-empty': Number(product.stockQuantity || 0) === 0
          }"
        >
          {{ product.stockQuantity || 0 }}
        </strong>
      </div>

      <div class="admin-product-actions">
        <button
          type="button"
          class="admin-btn"
          @click="$emit('edit', product)"
        >
          Edit
        </button>

        <button
          type="button"
          class="admin-btn"
          @click="$emit('stock', product)"
        >
          Add stock
        </button>

        <button
          type="button"
          class="admin-btn admin-btn-danger"
          @click="$emit('delete', product)"
        >
          Delete
        </button>
      </div>
    </div>
  </article>
</template>

<script>
import { formatCurrency } from '../utils/formatters'

export default {
  name: 'AdminProductCard',
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  emits: ['edit', 'stock', 'delete'],
  methods: {
    currency(value) {
      return formatCurrency(value)
    }
  }
}
</script>

<style scoped>
.admin-product-card {
  display: grid;
  grid-template-columns: 118px minmax(0, 1fr);
  gap: 12px;
  padding: 10px;
  background: #f6e4bf;
  border: 2px solid #cfa66f;
  box-shadow:
    3px 3px 0 #7a5a32,
    inset 1px 1px 0 #fff8e7;
}

.admin-product-image-frame {
  min-height: 118px;
  padding: 4px;
  background: #fff8e7;
  border-top: 2px solid #7a5a32;
  border-left: 2px solid #7a5a32;
  border-right: 2px solid #fff4d6;
  border-bottom: 2px solid #fff4d6;
}

.admin-product-image {
  display: block;
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}

.admin-product-no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 106px;
  font-family: "Courier New", monospace;
  font-size: 11px;
  color: #7a5a32;
  background:
    repeating-linear-gradient(
      135deg,
      #f6e4bf,
      #f6e4bf 6px,
      #ecd1a0 6px,
      #ecd1a0 12px
    );
}

.admin-product-title-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.admin-product-title {
  margin: 0;
  font-size: 15px;
  text-transform: uppercase;
  color: #142f22;
}

.admin-product-name {
  margin: 2px 0 0;
  font-size: 13px;
  color: #3c2a1a;
}

.admin-product-id {
  flex-shrink: 0;
  padding: 2px 5px;
  font-family: "Courier New", monospace;
  font-size: 10px;
  color: #fff8e7;
  background: #142f22;
}

.admin-product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  font-family: "Courier New", monospace;
  font-size: 11px;
}

.admin-product-meta span {
  padding: 2px 5px;
  background: #fff3d9;
  border: 1px solid #cfa66f;
}

.admin-product-stock {
  margin-top: 8px;
  font-size: 13px;
}

.admin-product-label {
  margin-right: 5px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.stock-empty {
  color: #a12626;
}

.admin-product-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.admin-btn {
  padding: 5px 9px;
  color: #e0c494;
  font-family: inherit;
  font-size: 11px;
  text-transform: uppercase;
  cursor: pointer;
  background: #142f22;
  border-top: 2px solid #416b54;
  border-left: 2px solid #416b54;
  border-right: 2px solid #071b11;
  border-bottom: 2px solid #071b11;
}

.admin-btn:active {
  border-top-color: #071b11;
  border-left-color: #071b11;
  border-right-color: #416b54;
  border-bottom-color: #416b54;
}

.admin-btn-danger {
  color: #fff3d9;
  background: #8f2e2e;
  border-top-color: #c46969;
  border-left-color: #c46969;
  border-right-color: #551414;
  border-bottom-color: #551414;
}

@media (max-width: 600px) {
  .admin-product-card {
    grid-template-columns: 90px minmax(0, 1fr);
  }
}
</style>