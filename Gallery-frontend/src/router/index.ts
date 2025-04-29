import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
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
    },
  ],
})

export default router
