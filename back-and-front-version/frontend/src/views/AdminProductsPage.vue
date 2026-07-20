<template>
  <section class="admin-page">
    <div class="admin-window">
      <div class="admin-titlebar">
        <span>WINYLKA PRODUCT MANAGER</span>

        <span class="admin-titlebar-version">
          v1.0
        </span>
      </div>

      <div class="admin-toolbar">
        <button
          type="button"
          class="toolbar-btn"
          @click="openCreateForm"
        >
          + New product
        </button>

        <button
          type="button"
          class="toolbar-btn"
          :disabled="loading"
          @click="loadProducts"
        >
          Refresh
        </button>

        <div class="admin-toolbar-right">
          <label class="admin-search-label">
            Search:

            <input
              v-model.trim="searchQuery"
              type="search"
              class="admin-search-input"
              placeholder="Artist, album or catalogue no."
            >
          </label>

          <span class="admin-result-badge">
            {{ filteredProducts.length }} / {{ products.length }} products
          </span>
        </div>
      </div>

      <div class="admin-content">
        <p
          v-if="loading"
          class="admin-status"
        >
          Loading product database...
        </p>

        <div
          v-else-if="error"
          class="admin-error"
        >
          {{ error }}
        </div>

        <p
          v-else-if="!filteredProducts.length"
          class="admin-status"
        >
          {{ products.length
            ? 'No products found.'
            : 'Product database is empty.'
          }}
        </p>

        <div
          v-else
          class="admin-product-list"
        >
          <AdminProductCard
            v-for="product in filteredProducts"
            :key="product.id"
            :product="product"
            @edit="openEditForm"
            @stock="openStockForm"
            @delete="deleteProduct"
          />
        </div>
      </div>

      <div class="admin-statusbar">
        <span>{{ statusText }}</span>
        <span>{{ saving ? 'WORKING...' : 'READY' }}</span>
      </div>
    </div>

    <AdminProductForm
      v-if="productFormVisible"
      :product="selectedProduct"
      :saving="saving"
      @save="saveProduct"
      @close="closeProductForm"
    />

    <AdminStockForm
      v-if="stockFormVisible && selectedProduct"
      :product="selectedProduct"
      :saving="saving"
      @save="addStock"
      @close="closeStockForm"
    />
  </section>
</template>

<script>
import AdminProductCard from '../components/AdminProductCard.vue'
import AdminProductForm from '../components/AdminProductForm.vue'
import AdminStockForm from '../components/AdminStockForm.vue'
import { adminProductsApi } from '../api/adminProducts'

export default {
  name: 'AdminProductsPage',

  components: {
    AdminProductCard,
    AdminProductForm,
    AdminStockForm
  },

  data() {
    return {
      products: [],
      loading: false,
      saving: false,
      error: '',
      statusText: 'Product database',
      searchQuery: '',

      selectedProduct: null,
      productFormVisible: false,
      stockFormVisible: false
    }
  },

  created() {
    this.loadProducts()
  },

  computed: {
    filteredProducts() {
      const query = this.searchQuery
        .trim()
        .toLowerCase()

      return [...this.products]
        .filter(product => {
          if (!query) {
            return true
          }

          const searchableText = [
            product.id,
            product.artist,
            product.name,
            product.catNo,
            product.format
          ]
            .filter(value => value != null)
            .join(' ')
            .toLowerCase()

          return searchableText.includes(query)
        })
        .sort((a, b) => Number(b.id) - Number(a.id))
    }
  },

  methods: {
    notifyProductsChanged() {
      sessionStorage.removeItem('shuffled-products-v2')

      window.dispatchEvent(
        new CustomEvent('winylka-products-updated')
      )
    },

    async loadProducts() {
      this.loading = true
      this.error = ''
      this.statusText = 'Loading...'

      try {
        const { data } = await adminProductsApi.getAll()

        this.products = Array.isArray(data)
          ? data
          : data.content || []

        this.statusText =
          `${this.products.length} product(s) loaded`
      } catch (e) {
        console.error(e)
        this.error = 'Failed to load products.'
        this.statusText = 'Load error'
      } finally {
        this.loading = false
      }
    },

    openCreateForm() {
      this.selectedProduct = null
      this.stockFormVisible = false
      this.productFormVisible = true
    },

    openEditForm(product) {
      this.selectedProduct = product
      this.stockFormVisible = false
      this.productFormVisible = true
    },

    closeProductForm() {
      if (this.saving) {
        return
      }

      this.productFormVisible = false
      this.selectedProduct = null
    },

    openStockForm(product) {
      this.selectedProduct = product
      this.productFormVisible = false
      this.stockFormVisible = true
    },

    closeStockForm() {
      if (this.saving) {
        return
      }

      this.stockFormVisible = false
      this.selectedProduct = null
    },

    async saveProduct(formData) {
      this.saving = true

      try {
        if (this.selectedProduct) {
          const productId = this.selectedProduct.id

          const { data } = await adminProductsApi.update(
            productId,
            formData
          )

          this.replaceProduct(data)

          this.statusText =
            `${data.artist} — ${data.name} updated`
        } else {
          const { data } =
            await adminProductsApi.create(formData)

          this.products.unshift(data)

          this.statusText =
            `${data.artist} — ${data.name} created`
        }

        this.notifyProductsChanged()

        this.productFormVisible = false
        this.selectedProduct = null
      } catch (e) {
        console.error(e)

        alert(
          e?.response?.data?.message ||
          'Failed to save product.'
        )

        this.statusText = 'Save error'
      } finally {
        this.saving = false
      }
    },

    async addStock(quantity) {
      if (!this.selectedProduct) {
        return
      }

      this.saving = true

      try {
        const productId = this.selectedProduct.id

        const { data } = await adminProductsApi.addStock(
          productId,
          quantity
        )

        /*
         * Предполагаем, что backend возвращает обновлённый Product.
         * Если endpoint возвращает только новое количество,
         * покажи ответ — подстроим этот участок.
         */
        if (data && typeof data === 'object') {
          this.replaceProduct(data)

          this.statusText =
            `${data.artist} — ${data.name}: stock updated`
        } else {
          await this.loadProducts()
          this.statusText = 'Stock updated'
        }

        this.notifyProductsChanged()

        this.stockFormVisible = false
        this.selectedProduct = null
      } catch (e) {
        console.error(e)

        alert(
          e?.response?.data?.message ||
          'Failed to update stock.'
        )

        this.statusText = 'Stock update error'
      } finally {
        this.saving = false
      }
    },

    replaceProduct(updatedProduct) {
      const index = this.products.findIndex(
        product => product.id === updatedProduct.id
      )

      if (index === -1) {
        this.products.unshift(updatedProduct)
        return
      }

      this.products.splice(index, 1, updatedProduct)
    },

    async deleteProduct(product) {
      const confirmed = window.confirm(
        `Delete "${product.artist} — ${product.name}"?\n\n` +
        'The product and its cover file will be deleted.'
      )

      if (!confirmed) {
        return
      }

      this.saving = true

      try {
        await adminProductsApi.delete(product.id)

        this.products = this.products.filter(
          item => item.id !== product.id
        )

        this.notifyProductsChanged()

        this.statusText =
          `${product.artist} — ${product.name} deleted`
      } catch (e) {
        console.error(e)

        alert(
          e?.response?.data?.message ||
          'Failed to delete product.'
        )

        this.statusText = 'Delete error'
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.admin-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 10px 0 30px;
}

.admin-window {
  background: #d8c59d;
  border-top: 3px solid #fff3d9;
  border-left: 3px solid #fff3d9;
  border-right: 3px solid #60492f;
  border-bottom: 3px solid #60492f;
  box-shadow: 5px 5px 0 rgba(60, 42, 26, 0.35);
}

.admin-titlebar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 28px;
  padding: 3px 7px;
  color: #e0c494;
  font-family: "Courier New", monospace;
  font-size: 13px;
  font-weight: bold;
  background: linear-gradient(
    90deg,
    #142f22 0%,
    #28543d 60%,
    #142f22 100%
  );
}

.admin-titlebar-version {
  font-size: 10px;
}

.admin-toolbar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px;
  border-bottom: 1px solid #7a5a32;
}

.toolbar-btn {
  padding: 5px 9px;
  font-family: inherit;
  font-size: 11px;
  cursor: pointer;
  background: #e0c494;
  border-top: 2px solid #fff3d9;
  border-left: 2px solid #fff3d9;
  border-right: 2px solid #60492f;
  border-bottom: 2px solid #60492f;
}

.toolbar-btn:active {
  border-top-color: #60492f;
  border-left-color: #60492f;
  border-right-color: #fff3d9;
  border-bottom-color: #fff3d9;
}

.toolbar-btn:disabled {
  cursor: default;
  opacity: 0.5;
}

.admin-content {
  min-height: 300px;
  padding: 10px;
  background: #fff8e7;
  border-top: 2px solid #60492f;
  border-left: 2px solid #60492f;
  border-right: 2px solid #fff3d9;
  border-bottom: 2px solid #fff3d9;
}

.admin-product-list {
  display: grid;
  gap: 12px;
}

.admin-status {
  font-family: "Courier New", monospace;
  font-size: 12px;
}

.admin-error {
  padding: 8px;
  color: #fff8e7;
  font-family: "Courier New", monospace;
  font-size: 12px;
  background: #8f2e2e;
  border: 2px solid #551414;
}

.admin-statusbar {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 4px 7px;
  font-family: "Courier New", monospace;
  font-size: 10px;
  border-top: 1px solid #fff3d9;
}

.admin-search-label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 6px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  white-space: nowrap;
}

.admin-search-input {
  box-sizing: border-box;
  min-width: 240px;
  padding: 3px 8px;
  border-radius: 999px;
  border: 1px solid #cfa66f;
  background: #fff8e7;
  color: #3c2a1a;
  font-family: inherit;
  font-size: 12px;
}

.admin-search-input:focus {
  outline: 1px solid #142f22;
  outline-offset: 1px;
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
  min-width: 240px;
  padding: 3px 8px;
  border-radius: 999px;
  border: 1px solid #cfa66f;
  background: #fff8e7;
  color: #3c2a1a;
  font-family: inherit;
  font-size: 12px;
}

.admin-search-input:focus {
  outline: 1px solid #142f22;
  outline-offset: 1px;
}

.admin-result-badge {
  padding: 3px 10px;
  border-radius: 999px;
  background: #142f22;
  color: #e0c494;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  white-space: nowrap;
}
@media (max-width: 700px) {
  .admin-toolbar {
    flex-wrap: wrap;
  }

  .admin-toolbar-right {
    width: 100%;
    margin-left: 0;
    flex-wrap: wrap;
  }

  .admin-search-label {
    flex: 1;
    min-width: 220px;
  }

  .admin-search-input {
    flex: 1;
    min-width: 0;
  }

  .admin-result-badge {
    margin-left: auto;
  }
}
</style>