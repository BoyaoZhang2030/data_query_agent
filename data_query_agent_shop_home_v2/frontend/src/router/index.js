import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'HomeShop',
    component: () => import('../views/Shop.vue'),
    meta: { public: true, shop: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { admin: true }
  },
  {
    path: '/query',
    name: 'Query',
    component: () => import('../views/Query.vue'),
    meta: { admin: true }
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('../views/Products.vue'),
    meta: { admin: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: { admin: true }
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('../views/Categories.vue'),
    meta: { admin: true }
  },
  {
    path: '/shop',
    name: 'Shop',
    component: () => import('../views/Shop.vue'),
    meta: { shop: true }
  },
  {
    path: '/shop/orders',
    name: 'ShopOrders',
    component: () => import('../views/ShopOrders.vue'),
    meta: { shop: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const savedUser = localStorage.getItem('user')
  const user = savedUser ? JSON.parse(savedUser) : null
  if (!to.meta.public && !user) return '/login'
  if (to.meta.admin && user?.role !== 'admin') return '/shop'
  if ((to.path === '/login' || to.path === '/register') && user && to.query.mode !== 'admin') {
    return user.role === 'admin' ? '/dashboard' : '/shop'
  }
})

export default router
