import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 存储登录用户信息的状态
 * loginUser 名称
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<any>({
    userName: '未登录',
  })

  async function fetchLoginUser() {
    // 测试用户登录，3 秒后登录
    setTimeout(() => {
      loginUser.value = { userName: '测试用户', id: 1 }
    }, 3000)
    // todo 由于后端还没提供接口，暂时注释
    // const res = await getCurrentUser();
    // if (res.data.code === 0 && res.data.data) {
    //   loginUser.value = res.data.data;
    // }
  }

  /**
   * 设置登录用户
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
