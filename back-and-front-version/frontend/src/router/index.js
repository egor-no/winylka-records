import { createRouter, createWebHistory } from 'vue-router'
import Shop from '../views/Shop.vue'
import Catalogue from '../views/CataloguePage.vue'
import Checkout from '../views/CheckoutPage.vue'
import AboutPage from '../views/AboutPage.vue'
import Product from '../views/ProductPage.vue'
import NotFound from '../views/NotFoundPage.vue'
import CheckoutSuccess from '../views/CheckoutSuccessPage.vue'

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
      },
      {
        path: 'checkout/success',              
        name: 'checkout-success',
        component: CheckoutSuccess
      }
    ]
  },
  {
    path: '/about',
    name: 'about',
    component: AboutPage
  },
  {
    path: '/404',
    name: 'not-found',
    component: NotFound
  },
  {
    path: '/:pathMatch(.*)*',
    component: NotFound
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router