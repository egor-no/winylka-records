<template>
  <section class="admin-layout">
    <nav class="admin-section-nav">
      <RouterLink
        to="/admin/products"
        class="admin-section-link"
      >
        Stock
      </RouterLink>

      <RouterLink
        to="/admin/orders"
        class="admin-section-link"
      >
        Orders
      </RouterLink>
    </nav>

    <RouterView />
  </section>
</template>

<script>
export default {
  name: 'AdminLayout'
}
</script>

<style scoped>
.admin-layout {
  width: 100%;
}

.admin-section-nav {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  margin: 10px 0 0;
  padding: 0 8px;
  border-bottom: 3px solid #60492f;
}

.admin-section-link {
  min-width: 90px;
  padding: 6px 14px 5px;
  color: #3c2a1a;
  background: #c5af82;
  border-top: 2px solid #fff3d9;
  border-left: 2px solid #fff3d9;
  border-right: 2px solid #60492f;
  text-align: center;
  font-family: "Courier New", monospace;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  text-decoration: none;
}

.admin-section-link:hover {
  background: #d8c59d;
}

.admin-section-link.router-link-active {
  position: relative;
  top: 3px;
  padding-bottom: 8px;
  color: #e0c494;
  background: #142f22;
  border-top-color: #3d6a51;
  border-left-color: #3d6a51;
  border-right-color: #0e2319;
}
</style>