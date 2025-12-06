<template>
  <div class="page">
    <Header />

    <nav class="main-nav">
      <RouterLink to="/catalogue" class="nav-link">Catalogue</RouterLink>
      <RouterLink to="/about" class="nav-link">About</RouterLink>
    </nav>

    <main class="page-content">
        <RouterView v-slot="{ Component }">
          <component
            :is="Component"
            :cart-objects="cartObjects"
            :cart-total="cartTotal"
            @add-to-cart="addToCart"
            @delete-from-cart="deleteFromCart"
            @update-cart="updateCart"
            @is-in-cart="isInCart"
          />
        </RouterView>
    </main>
  </div>
</template>

<script>
import Header from './components/HeaderBar.vue'
import { formatCurrency } from './utils/formatters'

export default {
  name: 'App',
  components: {
    Header
  },
   data() {
    return {
      cart: []
    }
  },
  methods: {
    currency(value) {
      return formatCurrency(value);
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
    updateCart(lines) {
      const next = [];
      lines.forEach(line => {
        for (let i = 0; i < line.amount; i++) {
          next.push(line.item);
        }
      });
      this.cart = next;
    },
    isInCart(product) {
      return this.cart.some(item => item.id === product.id);
    }
  },
   computed: {
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