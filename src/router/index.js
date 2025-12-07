import { createRouter, createWebHistory } from 'vue-router'
import Shop from '../views/Shop.vue'
import Catalogue from '../views/CataloguePage.vue'
import Checkout from '../views/CheckoutPage.vue'
import AboutPage from '../views/AboutPage.vue'
import Product from '../views/ProductPage.vue'

const routes = [
  {
    path: '/',
    redirect: '/catalogue'
  },
  {
    path: '/',
    component: Shop,
    children: [
      {
        path: 'catalogue',
        name: 'catalogue',
        component: Catalogue
      },
      {
        path: 'product/:id',
        name: 'product',
        component: Product
      },
      {
        path: 'checkout',
        name: 'checkout',
        component: Checkout
      }
    ]
  },
  {
    path: '/about',
    name: 'about',
    component: AboutPage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router