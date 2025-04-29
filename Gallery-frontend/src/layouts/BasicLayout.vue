<template>
  <div id="basicLayout">
    <a-layout style="height: 100vh">
<!--      页面导航条-->
      <a-layout-header class="header"><GlobalHeader/></a-layout-header>
<!--      页面中部-->
      <a-layout-content class="content">
        <!--   会自动根据url找到页面文件, 然后替换页面     -->
        <router-view/>
      </a-layout-content>
<!--      页面底部-->
      <a-layout-footer class="footer"> © 2025 幻化.All rights reserved</a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
</script>

<style scoped>
#basicLayout .header {
  padding-inline: 20px;
  margin-bottom: 16px;
  color: unset;
  background: white;
}

#basicLayout .content {
  background: linear-gradient(to right, #fefefe, #fff);
  margin-bottom: 28px;
  padding: 20px;
}

#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
