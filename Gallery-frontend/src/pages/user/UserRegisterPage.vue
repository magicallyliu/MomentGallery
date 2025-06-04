<template>
  <div id="userRegisterPage">
    <h2 class="title">须臾图库 - 用户注册</h2>
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
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入注册密码" />
      </a-form-item>

<!--      确认密码-->
      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请确认密码' },
          { min: 8, message: '确认密码长度不小于八位' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码" />
      </a-form-item>

      <!--      跳转注册页面-->
      <div class="tips">
        已有账号?
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script lang="ts" setup>

import { reactive } from 'vue'
import { userRegisterUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'


//用于接收表单输入的值
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  try {
    const res = await userRegisterUsingPost(values)
    //效验两次密码是否一致
    if (values.userPassword !== values.checkPassword) {
      message.error("密码输入不一致");
      return;
    }
    //注册成功, 跳转到登录页面
    if (res.data.code === 20001 && res.data.data) {

      message.success('注册成功')
      //跳转到登录页面
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      //注册失败
      message.error('注册失败, ' + res.data.message)
    }
  } catch (e) {
    message.error(`注册失败, 请重新尝试, ` + e.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
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
