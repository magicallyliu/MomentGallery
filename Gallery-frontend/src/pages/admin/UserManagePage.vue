<template>
  <div id="userManagePage">
    <!--    搜索表单-->

    <a-form :layout="inline" :model="searchParams" @finish="doSearch">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="账号">
          <!--          allow-clear 快速清除-->
          <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-form-item>
      </a-form>
    </a-form>
    <div style="margin-top: 20px" />
    <!--    表格-->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="50" />
        </template>
        <!--        定义用户类别类-->
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <!--        定义时间-->
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <!--          删除-->
          <a-button danger @click="daDelete(record.id)">删除</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { SmileOutlined, DownOutlined } from '@ant-design/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUserUsingPost, listUserByPageVoUsingPost } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
// 列
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

//定义数据
const dataList = ref<API.UserVO[]>([])
//分页的数据总数
const total = ref(0)

//搜索条件
//每页查询多少数据, 现在是第几页
//默认按照创建时间降序
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 定义一个分页器'
//使用计算属性渲染函数, 使其动态变化
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true, //是否允许切换页码
    //当前有多少文案
    showTotal: (total) => `共${total}条`,
  }
})
//获取数据
const fetchData = async () => {
  const res = await listUserByPageVoUsingPost({
    ...searchParams,
  })
  if (res.data.code == 20001 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取消息失败' + res.data.message)
  }
}

//用于检查每页查询数据的变化
//更改之后需要重新获取数据
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

//删除数据
const daDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingPost({id})
  if (res.data.code == 20001 && res.data.data){
    message.success("删除成功")
    fetchData()
  }else {
    message.error("删除失败")
  }
}

//搜索数据
const doSearch = () => {
  //恢复页面到第一页
  searchParams.current = 1
  fetchData()
}

//页面加载时请求数据
onMounted(() => {
  fetchData()
})
</script>
