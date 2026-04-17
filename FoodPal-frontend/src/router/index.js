import { createRouter, createWebHistory } from 'vue-router';
import { getLoginUser } from '../api';

const HomeView = () => import('../views/HomeView.vue');
const Login = () => import('../views/LoginView.vue');
const Register = () => import('../views/RegisterView.vue');
const LayoutView = () => import('../views/LayoutView.vue');
const ProfileView = () => import('../views/ProfileView.vue');

const UserView = () => import('../views/admin/UserView.vue');
const SupervisorUserView = () => import('../views/admin/SupervisorUserView.vue');
const CanteenMerchantView = () => import('../views/admin/CanteenMerchantView.vue');
const CanteenComplaintView = () => import('../views/admin/CanteenComplaintView.vue');
const CanteenContentView = () => import('../views/admin/CanteenContentView.vue');

const SupervisorComplaintView = () => import('../views/supervisor/SupervisorComplaintView.vue');
const SupervisorMerchantView = () => import('../views/supervisor/SupervisorMerchantView.vue');
const SupervisorDishView = () => import('../views/supervisor/SupervisorDishView.vue');

const MerchantProfileView = () => import('../views/merchant/MerchantProfileView.vue');
const MerchantDishView = () => import('../views/merchant/MerchantDishView.vue');
const MerchantComplaintView = () => import('../views/merchant/MerchantComplaintView.vue');

const routes = [
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  {
    path: '/',
    component: LayoutView,
    children: [
      { path: '/', component: HomeView },
      { path: '/profile', component: ProfileView, meta: { requiresAuth: true } },
    ],
  },
  {
    path: '/admin',
    component: LayoutView,
    redirect: '/admin/users',
    children: [
      { path: 'users', component: UserView, meta: { requiresAuth: true } },
      { path: 'supervisors', component: SupervisorUserView, meta: { requiresAuth: true } },
      { path: 'merchants', component: CanteenMerchantView, meta: { requiresAuth: true } },
      { path: 'complaints', component: CanteenComplaintView, meta: { requiresAuth: true } },
      { path: 'content', component: CanteenContentView, meta: { requiresAuth: true } },
    ],
  },
  {
    path: '/merchant',
    component: LayoutView,
    redirect: '/merchant/profile',
    children: [
      { path: 'profile', component: MerchantProfileView, meta: { requiresAuth: true } },
      { path: 'dishes', component: MerchantDishView, meta: { requiresAuth: true } },
      { path: 'complaints', component: MerchantComplaintView, meta: { requiresAuth: true } },
    ],
  },
  {
    path: '/supervisor',
    component: LayoutView,
    redirect: '/supervisor/dishes',
    children: [
      { path: 'dishes', component: SupervisorDishView, meta: { requiresAuth: true } },
      { path: 'complaints', component: SupervisorComplaintView, meta: { requiresAuth: true } },
      { path: 'merchants', component: SupervisorMerchantView, meta: { requiresAuth: true } },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to, from, next) => {
  if (to.path === '/login' || to.path === '/register') {
    next();
    return;
  }

  if (to.meta.requiresAuth || to.path === '/' || to.path.startsWith('/admin')) {
    try {
      const res = await getLoginUser();
      if (res.code === 0) {
        const isAdmin = res?.data?.userRole === 'admin';
        const isSupervisor = res?.data?.userRole === 'supervisor';
        if (to.path.startsWith('/admin') && !isAdmin) {
          next('/');
          return;
        }
        if (to.path.startsWith('/supervisor') && !isAdmin && !isSupervisor) {
          next('/');
          return;
        }
        next();
      } else {
        next('/login');
      }
    } catch (error) {
      next('/login');
    }
  } else {
    next();
  }
});

export default router;
