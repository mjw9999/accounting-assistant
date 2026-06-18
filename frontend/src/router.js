import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'
import Login from './views/Login.vue'
import Layout from './views/Layout.vue'
import Dashboard from './views/Dashboard.vue'
import Categories from './views/Categories.vue'
import Finance from './views/Finance.vue'
import Products from './views/Products.vue'
import Investments from './views/Investments.vue'
import Shares from './views/Shares.vue'
import Users from './views/Users.vue'
import Admins from './views/Admins.vue'
import Profile from './views/Profile.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: Layout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', component: Dashboard },
        { path: 'categories', component: Categories },
        { path: 'finance', component: Finance },
        { path: 'products', component: Products },
        { path: 'investments', component: Investments },
        { path: 'shares', component: Shares },
        { path: 'users', component: Users, meta: { role: 'ADMIN' } },
        { path: 'admins', component: Admins, meta: { role: 'ADMIN' } },
        { path: 'profile', component: Profile }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.token) {
    return '/login'
  }
  if (to.meta.role && auth.user?.role !== to.meta.role) {
    return '/dashboard'
  }
})

export default router
