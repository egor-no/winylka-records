<template>
  <section class="checkout-page">
    <div class="section-header">
      <h2 class="section-title">Checkout</h2>
    </div>

    <Transition name="empty-fade" mode="out-in">
      <div
        v-if="!cartObjects.length"
        key="empty"
        class="checkout-empty"
      >
        <p class="cart-note">
          Your cart is empty. Go back to the catalogue to add some records.
        </p>
        <RouterLink to="/catalogue" class="checkout-link">
          Back to catalogue
        </RouterLink>
      </div>

      <div
        v-else
        key="full"
        class="checkout-layout"
      >
        <CheckoutCustomerForm
          :has-items="!!localLines.length"
          @submit="handleFormSubmit"
        />

        <CheckoutSummary
          :lines="localLines"
          :total="itemsTotal"
          @change-amount="changeAmount"
          @remove-line="removeLine"
        />
      </div>
    </Transition>
  </section>
</template>

<script>
import CheckoutCustomerForm from '../components/CheckoutCustomerForm.vue'
import CheckoutSummary from '../components/CheckoutSummary.vue'

export default {
  name: 'CheckoutPage',
  components: {
    CheckoutCustomerForm,
    CheckoutSummary
  },
  props: {
    cartObjects: {
      type: Array,
      default: () => []
    }
  },
  emits: ['place-order', 'update-cart'],
  data() {
    return {
      localLines: []
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
        this.localLines = newVal.map(line => ({
          item: line.item,
          amount: line.amount
        }))
      }
    }
  },
  methods: {
    changeAmount(line, newAmount) {
       if (newAmount <= 0 || Number.isNaN(newAmount)) {
          this.removeLine(line)
          return
        }

        const target = this.localLines.find(
          l => l.item.id === line.item.id
        )

        if (target) {
          target.amount = newAmount
        }

        this.$emit('update-cart', this.localLines)
    },
    removeLine(line) {
      this.localLines = this.localLines.filter(
        l => l.item.id !== line.item.id
      )
      this.$emit('update-cart', this.localLines)
    },
    handleFormSubmit(formData) {
      if (!this.localLines.length) {
        return
      }

      const order = {
        ...formData,
        items: this.localLines,
        totals: {
          itemsTotal: this.itemsTotal
        },
        createdAt: new Date().toISOString()
      }

      this.$emit('place-order', order)
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
  margin-left: 5px;
}

.empty-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}

.empty-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

.empty-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
</style>