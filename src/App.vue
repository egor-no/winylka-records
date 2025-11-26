<template>
  <div class="page">
    <header class="header">
      <div class="logo-wrap">
        <img
          class="logo-img"
          src="/src/assets/logo.jpg"
          alt="Winylka logo"
        >
        <div class="header-text">
          <h1 class="site-title">Winylka Records</h1>
          <p class="site-subtitle">Selected vinyl records</p>
        </div>
      </div>
    </header>

  <div class="section-header">
    <h2 class="section-title">Catalogue</h2>

    <div class="cart-summary-wrap">
      <button
        @click="displayCart = !displayCart"
        class="cart-summary"
        :class="{ 'cart-open': displayCart }"
        type="button"
      >
        <span class="cart-summary-icon">🛒</span>
        <span class="cart-summary-count" :class="{ 'cart-count-open': displayCart }">
          {{ currency(cartTotal) }}
        </span>
      </button>

        <transition name="cart-dropdown">
          <div v-if="displayCart" class="cart-list">
            <template v-if="cartObjects.length">
              <transition-group
                name="cart-item"
                tag="div"
                class="cart-list-inner"
                appear
               >
                <div
                  v-for="line in cartObjects"
                  :key="line.item.id"
                  class="cart-list-item"
                >
                  <span
                    @click="deleteFromCart(line.item)"
                    type="button"
                    class="cart-delete-btn"
                    title="Remove from cart"
                  >
                    🗑
                  </span>

                  <span class="cart-item-name">{{ line.item.name }}</span>
                  <span class="cart-item-qty">×{{ line.amount }}</span>
                  <span class="cart-item-price">{{ currency(line.item.price * line.amount) }}</span>
                </div>
              </transition-group>
            </template>

            <transition name="empty-fade" appear>
              <div v-if="!cartObjects.length" class="cart-empty">
                <p class="cart-note">Корзина пуста</p>
              </div>
            </transition>
          </div>
        </transition> 
    </div>
  </div>

    <div class="filter-bar">
      <div class="filter-left">
        <label for="max-price" class="filter-label">
          Max price:
          <span class="filter-value">{{ currency(max) }}</span>
        </label>

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
      <article v-for="item in filteredProducts" :key="item.id" class="product-card">
        <div class="image-wrap">
          <img class="product-image" :src="item.img" :title="item.name" :alt="item.name">

          <button @click="addToCart(item)" class="image-cart-button" :class="isInCart(item) ? 'added' : 'to-add'" type="button" title="Add to cart">
            🛒
          </button>
        </div>

        <div class="product-body">
          <h3 class="product-title">
            {{ item.artist }} – {{ item.name }}
          </h3>

          <p class="product-note">{{ item.note }}</p>
        </div>

        <div class="product-bottom">
          <div class="product-price">{{ currency(item.price) }}</div>

          <div class="product-tags">
            <span v-if="item.price < sale" class="tag tag-sale">SALE!</span>
            <span v-else-if="item.price < cheap" class="tag tag-good">GOOD OFFER!</span>

            <span class="tag tag-format">{{ item.format }}</span>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      max: 200,
      cheap: 25, 
      sale: 20, 
      cart: [], 
      displayCart: false, 
      items: [], 
      search: '' 
    }
  },
  created() {
    fetch("/data/products.json")
      .then(response => response.json())
      .then(data => {
        this.items = data;
      })
  },
  methods: {
    currency(value) {
      return `$${Number.parseFloat(value).toFixed(2)}`
    },
    addToCart(product) {
      this.cart.push(product);
    },
    deleteFromCart(product) {
      const index = this.cart.findIndex(item => item.id === product.id);
      if (index !== -1) {
        this.cart.splice(index, 1); 
      }
    },
    isInCart(product) {
      return this.cart.some(item => item.id === product.id);
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
    },
    cartTotal() {
      return this.cart.reduce((inc, item) => Number(item.price) + inc, 0);
    },
    cartObjects() {
      const map = new Map();

      this.cart.forEach(product => {
        if (!map.has(product.id)) {
          map.set(product.id, {
            item: product,
            amount: 1
          });
        } else {
          map.get(product.id).amount++;
        }
      });

      return Array.from(map.values());
    }
  }
}
</script>
