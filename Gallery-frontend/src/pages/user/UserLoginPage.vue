<template>
  <div id="userLoginPage">
    <h2 class="title">须臾图库 - 用户登录</h2>
    <div class="desc">智能协同云图库</div>
    <!--  是否需要将历史记录显示
        autocomplete="off"
        点击提交就换调用后端接口, 将数据提交到后端
        @finish="handleSubmit"-->
    <a-form :model="formState" autocomplete="off" @finish="handleSubmit">
      <!--      :rules="[{ required: true, message: '请输入账号' }]" 固定的输出-->
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="账号名" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不小于八位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入登录密码" />
      </a-form-item>

      <!--      跳转注册页面-->
      <div class="tips">
        没有账号?
        <RouterLink to="/user/register">立即注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>

import { reactive } from 'vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'


//用于接收表单输入的值
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
})

const loginUserStore = useLoginUserStore()
/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  try {
    const res = await userLoginUsingPost(values)
    //正常响应, 并且状态码为20001
    //登录成功, 将登录状态保存到全局状态
    if (res.data.code === 20001 && res.data.data) {
      // 同步调用
      await loginUserStore.fetchLoginUser()
      message.success('登录成功')

      //返回上一级页面 -- 登录前的页面
      // window.history.back()

      //替换页面为主页
      //使用回退就不会返回登录页面,而是返回主页
      router.push({
        path: '/',
        replace: true,
      })
    } else {
      //登录失败
      message.error('登录失败, ' + res.data.message)
    }
  } catch (e) {
    message.error(`登录失败, ` + e.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  max-width: 360px;
  margin: 0 auto;
}
.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  color: #bbb;
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
