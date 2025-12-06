<template>
  <div class="page">
    <Header />

    <nav class="main-nav">
      <RouterLink to="/catalogue" class="nav-link">Catalogue</RouterLink>
      <RouterLink to="/about" class="nav-link">About</RouterLink>
    </nav>

    <main class="page-content">
      <RouterView />
    </main>
  </div>
</template>

<script>
import Header from './components/HeaderBar.vue'

export default {
  name: 'App',
  components: {
    Header
  }
}
</script>