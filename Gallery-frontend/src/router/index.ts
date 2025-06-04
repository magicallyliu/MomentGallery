import { createRouter, createWebHistory } from 'vue-router'

import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,

    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,

    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,

    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,

    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      /*  懒加载, 默认在打开页面时不加载
       *   需要看什么页面才会打开什么页面
       *     追却网页的性能
       * */
      component: () => import('../views/AboutView.vue'),
      // meta:{
      //   access:ACCESS_ENUM.USER,
      // }
    },
  ],
})

export default router
