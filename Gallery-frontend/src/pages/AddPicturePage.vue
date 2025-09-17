<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">创建图片</h2>
    <PictureUpload :picture="picture" :onSuccess="onSuccess" />
    <a-form layout="vertical" :model="pictureForm" @finish="handleSubmit">
      <a-form-item label="名称" name="name">
        <a-input v-model:value="pictureForm.name" placeholder="请输入名称" />
      </a-form-item>
      <a-form-item label="简介" name="introduction">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          :rows="2"
          autoSize
          allowClear
        />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          allowClear
        />
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请输入标签"
          allowClear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">创建</a-button>
      </a-form-item>
    </a-form>
  </div>

</template>

<script setup lang="ts">

import PictureUpload from '@/components/PictureUpload.vue'
import { ref } from 'vue'

const picture = ref<API.PictureVO>()
const pictureForm = ref<API.PictureEditRequest>({

})

const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

// /**
//  * 提交表单
//  * @param values
//  */
// const handleSubmit = async (values: any) => {
//     //
//     // const res = await userLoginUsingPost(values)
//     // //正常响应, 并且状态码为0
//     // //登录成功, 将登录状态保存到全局状态
//     // if (res.data.code === 20001 && res.data.data) {
//     //   // 同步调用
//     //   await loginUserStore.fetchLoginUser()
//     //   message.success('登录成功')
//     // } else {
//     // }
// }


</script>

<style scoped>
addPicturePage {
}

</style>
