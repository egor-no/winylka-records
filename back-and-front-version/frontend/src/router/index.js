import { createRouter, createWebHistory } from 'vue-router'
import Shop from '../views/Shop.vue'
import Catalogue from '../views/CataloguePage.vue'
import Checkout from '../views/CheckoutPage.vue'
import AboutPage from '../views/AboutPage.vue'
import Product from '../views/ProductPage.vue'
import NotFound from '../views/NotFoundPage.vue'
import CheckoutSuccess from '../views/CheckoutSuccessPage.vue'
import AdminProductsPage from '../views/AdminProductsPage.vue'
import AdminLayout from '../views/AdminLayout.vue'
import AdminOrdersPage from '../views/AdminOrdersPage.vue'
import AdminOrderDetailsPage from '../views/AdminOrderDetailsPage.vue'

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
  path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: '',
        redirect: { name: 'admin-products' }
      },
      {
        path: 'products',
        name: 'admin-products',
        component: AdminProductsPage
      },
      {
        path: 'orders',
        name: 'admin-orders',
        component: AdminOrdersPage
      },
      {
        path: 'orders/:id',
        name: 'admin-order-details',
        component: AdminOrderDetailsPage,
        props: true
      }
    ]
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