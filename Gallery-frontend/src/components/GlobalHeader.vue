<template>
  <div id="globalHeader">
    <!--wrop="false" 取消自动换行-->
    <a-row :wrap="false">
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
      <!--      用户信息展示栏-->
      <a-col flex="100px">
        <!--    总的登录图标    -->
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <!--              间距组件, 将其隔离开来-->
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" size="64" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>

              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <router-link to="/user/my_updateUser">
                      <UserOutlined />
                      修改信息
                    </router-link>
                  </a-menu-item>
                  <a-menu-item>
                    <router-link to="/my_space">
                      <UserOutlined />
                      我的空间
                    </router-link>
                  </a-menu-item>

                  <!--                  采用点击事件-->
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>

                </a-menu>
              </template>
            </a-dropdown>
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
import { computed, h, ref } from 'vue'
import { HomeOutlined, LogoutOutlined ,UserOutlined} from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'
//设置登录
const loginUserStore = useLoginUserStore()


//未经过过滤的导航条
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },

]

//根据权限过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    if (menu) {
      if (menu?.key?.startsWith('/admin')) {
        const loginUser = loginUserStore.loginUser
        if (!loginUser || loginUser.userRole !== 'admin') {
          return false
        }
      }
    }
    return true
  })
}

//过滤菜单
const items = computed(() => {
  return filterMenus(originItems)
})



//提供应该迅速跳转到其他页面的方法
const router = useRouter()

// 路由跳转事件
//function({ item, key, keyPath })
const doMenuClick = ({ key }) => {
  //实现页面跳转
  //跳转到需要的 key 页面
  router.push({ path: key })
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

//用户注销
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code == 20001) {
    //清理登录态
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录')
    //重新返回主页页面
    router.push({
      path: '/',
    })
  } else {
    message.error('注销失败' + res.data.message)
  }
}

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
