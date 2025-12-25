<template>
  <form class="checkout-form" @submit.prevent="onSubmit">
    <fieldset class="checkout-block">
      <legend class="checkout-block-title">Contact details</legend>

      <label class="checkout-field">
        <span class="checkout-label">Full name *</span>
        <input
          v-model.trim="fullName"
          type="text"
          class="checkout-input"
          required
        >
      </label>

      <label class="checkout-field">
        <span class="checkout-label">Email *</span>
        <input
          v-model.trim="email"
          type="email"
          class="checkout-input"
          required
        >
      </label>

      <label class="checkout-field">
        <span class="checkout-label">Phone</span>
        <input
          v-model.trim="phone"
          type="tel"
          class="checkout-input"
          placeholder="+7 ..."
        >
      </label>
    </fieldset>

    <fieldset class="checkout-block">
      <legend class="checkout-block-title">Shipping address</legend>

      <label class="checkout-field">
        <span class="checkout-label">City *</span>
        <input
          v-model.trim="city"
          type="text"
          class="checkout-input"
          required
        >
      </label>

      <label class="checkout-field">
        <span class="checkout-label">Address *</span>
        <input
          v-model.trim="address"
          type="text"
          class="checkout-input"
          placeholder="Street, house, apartment"
          required
        >
      </label>

      <label class="checkout-field">
        <span class="checkout-label">Postal code</span>
        <input
          v-model.trim="postalCode"
          type="text"
          class="checkout-input"
        >
      </label>
    </fieldset>

    <label class="checkout-field">
      <span class="checkout-label">Comment to order</span>
      <textarea
        v-model.trim="comment"
        rows="3"
        class="checkout-textarea"
        placeholder="Any wishes or additional info"
      />
    </label>

    <p v-if="formError" class="checkout-error">
      {{ formError }}
    </p>

    <button
      class="checkout-submit"
      type="submit"
      :disabled="submitting"
    >
      {{ submitting ? 'Placing order...' : 'Place order' }}
    </button>
  </form>
</template>

<script>
export default {
  name: 'CheckoutCustomerForm',
  props: {
    hasItems: {
      type: Boolean,
      default: true
    }
  },
  emits: ['submit'],
  data() {
    return {
      fullName: '',
      email: '',
      phone: '',
      city: '',
      address: '',
      postalCode: '',
      comment: '',
      submitting: false,
      formError: ''
    }
  },
  methods: {
    onSubmit() {
      this.formError = ''

      if (!this.fullName || !this.email || !this.city || !this.address) {
        this.formError = 'Please fill in all required fields.'
        return
      }

      if (!this.hasItems) {
        this.formError = 'Cart is empty.'
        return
      }

      const payload = {
        customer: {
          fullName: this.fullName,
          email: this.email,
          phone: this.phone
        },
        shipping: {
          city: this.city,
          address: this.address,
          postalCode: this.postalCode
        },
        comment: this.comment
      }

      this.submitting = true
      this.$emit('submit', payload)
      this.submitting = false
    }
  }
}
</script>

<style scoped>
.checkout-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.checkout-block {
  border: 1px solid #cfa66f;
  border-radius: 6px;
  padding: 10px 12px;
  background: #f6e4bf;
}

.checkout-block-title {
  padding: 0 4px;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.checkout-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
}

.checkout-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.checkout-input,
.checkout-textarea {
  border: 1px solid #cfa66f;
  border-radius: 4px;
  padding: 5px 8px;
  font-size: 13px;
  font-family: inherit;
  background: #fff8e7;
}

.checkout-textarea {
  resize: vertical;
}

.checkout-error {
  color: #8b0000;
  font-size: 12px;
}

.checkout-submit {
  margin-top: 6px;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #142f22;
  background: #142f22;
  color: #e0c494;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  cursor: pointer;
}

.checkout-submit:disabled {
  opacity: 0.6;
  cursor: default;
}
</style>