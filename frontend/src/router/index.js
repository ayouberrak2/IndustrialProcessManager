import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import ChefAtelierDashboard from '../views/ChefAtelierDashboard.vue'

const routes = [
  { path: '/', name: 'Login', component: LoginView },
  { path: '/admin', name: 'Admin', component: AdminDashboard, meta: { requiresAuth: true, role: 'ADMIN' } },
  { path: '/chef', name: 'Chef', component: ChefAtelierDashboard, meta: { requiresAuth: true, role: 'CHEF_ATELIER' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const currentUser = localStorage.getItem('currentUser')
  const user = currentUser ? JSON.parse(currentUser) : null

  if (to.meta && to.meta.requiresAuth) {
    if (!user) return next({ name: 'Login' })
    if (to.meta.role && user.role !== to.meta.role) {
      // redirect to appropriate dashboard if role mismatch
      if (user.role === 'ADMIN') return next({ name: 'Admin' })
      if (user.role === 'CHEF_ATELIER') return next({ name: 'Chef' })
      return next({ name: 'Login' })
    }
  }

  // prevent logged-in users from accessing login page
  if (to.name === 'Login' && user) {
    if (user.role === 'ADMIN') return next({ name: 'Admin' })
    if (user.role === 'CHEF_ATELIER') return next({ name: 'Chef' })
  }

  next()
})

export default router
