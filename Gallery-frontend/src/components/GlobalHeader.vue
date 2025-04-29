<template>
  <div id="globalHeader">
    <!--wrop="false" 取消自动换行-->
    <a-row :wrop="false">
      <a-col flex="150px">
        <!--   图标和名称     -->
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">须臾图库</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <!--    菜单
@click="doMenuClick" 绑定事件  点击 MenuItem 调用此函数
        -->
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <a-col flex="100px">
        <!--    总的登录图标    -->
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            {{ loginUserStore.loginUser.userName ?? '无名' }}
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>

      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref } from 'vue'
import { HomeOutlined } from '@ant-design/icons-vue'
import { MenuProps } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
//设置登录
const loginUserStore = useLoginUserStore()



const items = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/about',
    label: '关于',
    title: '关于',
  },
])

//提供应该迅速跳转到其他页面的方法
const router = useRouter()

// 路由跳转事件
//function({ item, key, keyPath })
const doMenuClick = ({ key }) => {
  //实现页面跳转
  //跳转到需要的 key 页面
  router.push( {path: key})
}
// 通过 const current = ref<string[]>(['home']) 来决定高亮
//设置自动高亮某个所在的页面
// 使用钩子
const current = ref<string[]>([])

/* 钩子函数
* 作用: 每次跳转到新页面时, 都会执行
* 参数: 要去哪个页面,  从哪个页面来的,  接下来要去哪个页面
* */
router.afterEach((to, from, next) => {
  /*将current 的值改为接下来要跳转的页面*/
  current.value = [to.path]
})

/*样式*/
</script>
<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 15px;
}

.logo {
  height: 48px;
}
</style>
