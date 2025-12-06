import { createRouter, createWebHistory } from 'vue-router'
import Checkout from '../views/CheckoutPage.vue'
import Catalogue from '../views/CataloguePage.vue'
import AboutPage from '../views/AboutPage.vue'

const routes = [
    {
    path: '/',
    redirect: '/catalogue',
  },
  {
    path: '/catalogue',
    name: 'catalogue',
    component: Catalogue,
  },
  {
    path: '/about',
    name: 'about',
    component: AboutPage,
  },
    {
    path: '/checkout',
    name: 'checkout',
    component: Checkout,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router