<template>
  <a-config-provider :locale="appLocale">
    <div id="app">
      <BasicLayout />
    </div>
  </a-config-provider>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { healthUsingGet } from '@/api/mainController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

import dayjs from 'dayjs'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
//修改为在权限效验文件效验
//获取当前登录用户信息
// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()

// healthUsingGet().then((response) => {
//   console.log(response)
// })

//中文
dayjs.locale('zh-cn')

//设置当前应用的语言环境
const appLocale = zhCN
</script>

<style scoped></style>
