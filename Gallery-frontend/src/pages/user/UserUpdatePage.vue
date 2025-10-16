<template>
  <div class="user-update-page">
    <a-row justify="center">
      <a-col :xs="24" :sm="22" :md="20" :lg="16" :xl="12">
        <a-card :bordered="false" class="profile-card">
          <template #title>
            <div class="card-title">
              <span>编辑个人资料</span>
            </div>
          </template>

          <a-row :gutter="24" :wrap="true">
            <!-- 左侧：头像与预览 -->
            <a-col :xs="24" :md="8" class="left-col">
              <div class="avatar-section">
                <a-avatar :size="96" :src="formState.userAvatar || loginUserStore.loginUser.userAvatar" />
                <div class="avatar-tip">头像预览</div>
                <a-input
                  v-model:value="formState.userAvatar"
                  placeholder="请输入头像图片 URL"
                  allow-clear
                />
              </div>
              <a-divider />
              <a-descriptions :column="1" size="small" title="原始信息" colon>
                <a-descriptions-item label="用户名">
                  {{ originData.userName || '—' }}
                </a-descriptions-item>
                <a-descriptions-item label="账号">
                  {{ originData.userAccount || '—' }}
                </a-descriptions-item>
                <a-descriptions-item label="简介">
                  {{ originData.userProfile || '—' }}
                </a-descriptions-item>
              </a-descriptions>
            </a-col>

            <!-- 右侧：表单编辑 -->
            <a-col :xs="24" :md="16">
              <a-form
                ref="formRef"
                :model="formState"
                :rules="rules"
                layout="vertical"
              >
                <a-form-item label="用户名" name="userName">
                  <a-input v-model:value="formState.userName" placeholder="请输入用户名" />
                </a-form-item>

                <a-form-item label="个人简介" name="userProfile">
                  <a-textarea
                    v-model:value="formState.userProfile"
                    :rows="5"
                    show-count
                    :maxlength="200"
                    placeholder="介绍一下你自己"
                  />
                </a-form-item>

                <a-form-item>
                  <a-space>
                    <a-button type="primary" :loading="submitting" @click="onSubmit">
                      保存
                    </a-button>
                    <a-button @click="onReset">重置为原始数据</a-button>
                  </a-space>
                </a-form-item>
              </a-form>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>
  </div>

</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { message, type FormInstance } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { getLoginUserUsingGet, updateUserVoUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const formRef = ref<FormInstance>()
const submitting = ref<boolean>(false)

// 原始数据（展示与重置使用）
const originData = reactive<API.LoginUserVO>({})

// 可编辑表单数据
const formState = reactive<API.UserUpdateVORequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const rules = {
  userName: [
    { required: true, message: '请输入用户名' },
    { min: 2, max: 32, message: '用户名长度应在 2-32 个字符' },
  ],
  userProfile: [
    { max: 200, message: '简介不超过 200 字' },
  ],
}

const fillFromSource = (src?: API.LoginUserVO) => {
  if (!src) return
  originData.id = src.id
  originData.userName = src.userName
  originData.userAccount = src.userAccount
  originData.userAvatar = src.userAvatar
  originData.userProfile = src.userProfile
  originData.userRole = src.userRole

  formState.id = src.id
  formState.userName = src.userName || ''
  formState.userAvatar = src.userAvatar || ''
  formState.userProfile = src.userProfile || ''
}

const fetchAndInit = async () => {
  // 优先使用全局状态，若为空则请求一次
  if (!loginUserStore.loginUser.id) {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
    }
  }
  fillFromSource(loginUserStore.loginUser)
}

const onSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }
  if (!formState.id) {
    message.error('未获取到用户信息，无法保存')
    return
  }
  submitting.value = true
  const res = await updateUserVoUsingPost({
    id: formState.id,
    userName: formState.userName,
    userAvatar: formState.userAvatar,
    userProfile: formState.userProfile,
  })
  submitting.value = false
  if (res.data.code === 20001 && res.data.data) {
    message.success('保存成功')
    // 同步更新全局登录信息
    loginUserStore.setLoginUser({
      ...loginUserStore.loginUser,
      userName: formState.userName,
      userAvatar: formState.userAvatar,
      userProfile: formState.userProfile,
    })
    fillFromSource(loginUserStore.loginUser)
  } else {
    message.error(res.data.message || '保存失败')
  }
}

const onReset = () => {
  fillFromSource(originData)
}

onMounted(() => {
  fetchAndInit()
})
</script>

<style scoped>
.user-update-page {
  padding: 16px;
}

.profile-card {
  border-radius: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.left-col {
  margin-bottom: 16px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-tip {
  color: #666;
  font-size: 12px;
}

@media (max-width: 767px) {
  .profile-card {
    padding: 8px;
  }
}
</style>


