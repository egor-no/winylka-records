<template>
  <section class="checkout-page">
    <div class="section-header">
      <h2 class="section-title">Thank you!</h2>
    </div>

    <div class="checkout-success">
      <p class="cart-note">
        Your order has been placed. We&apos;ll contact you soon to confirm the details.
      </p>

      <RouterLink to="/catalogue" class="checkout-link">
        ← Back to catalogue
      </RouterLink>
    </div>
  </section>
</template>

<script>
export default {
  name: 'CheckoutSuccessPage'
}
</script>

<style scoped>
.checkout-page {
  max-width: 1100px;
  margin: 0 auto;
}

.checkout-success {
  margin-top: 16px;
  margin-left: 5px;
}
</style>