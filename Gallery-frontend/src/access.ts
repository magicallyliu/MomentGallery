import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

//是否为首次获取用户登录信息
let firstFetchLoginUser: boolean = true

/**
 * 全局权限效验, 每次切换页面都会执行
 */
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  //确保页面刷新时, 能够等待后端返回用户信息后再进行权限效验
  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }

  //判断权限
  //获取需要访问的页面
  const toUrl = to.fullPath
  //自定义权限效验规则
  //1. 管理员才可以访问 /admin开头的页面
  if (toUrl.startsWith('/admin')) {
    //用户不存在, 或者用户不是管理员, 则拒绝访问
    if (!loginUser || loginUser.userRole !== 'admin') {
      message.error('没有权限')
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }
  next()
})
