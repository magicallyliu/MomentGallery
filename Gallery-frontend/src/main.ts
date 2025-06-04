import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

//全局权限管理
import "@/access";


// 全局注册
import Antd from "ant-design-vue";
import "ant-design-vue/dist/reset.css";


const app = createApp(App)

app.use(createPinia())
app.use(router)

//全局注册
app.use(Antd);
app.mount('#app')



