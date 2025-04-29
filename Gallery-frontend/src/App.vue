<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { healthUsingGet } from '@/api/mainController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

//获取当前登录用户信息
const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()


// healthUsingGet().then((response) => {
//   console.log(response)
// })
</script>

<style scoped>

</style>
