<template>
  <div class="space-tag-analyze">
    <a-card title="空间图片标签分析">
      <v-chart :option="options" style="height: 320px; max-width: 100%" :loading="loading" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts'
import 'echarts'

import { computed, ref, watchEffect } from 'vue'
import { getSpaceTagAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

// 图表数据
const dataList = ref<API.SpaceTagAnalyzeResponse[]>([])
// 加载状态
const loading = ref(true)

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const res = await getSpaceTagAnalyzeUsingPost({
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    spaceId: props.spaceId,
  })
  if (res.data.code === 20001 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

/**
 * 监听变量，参数改变时触发数据的重新加载
 */
watchEffect(() => {
  fetchData()
})

const options = computed(() => {
  const tag = dataList.value.map((item) => item.tag)
  const countData = dataList.value.map((item) => item.count)

  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['图片数量'], top: 'bottom' },
    xAxis: { type: 'category', data: tag },
    yAxis: [
      {
        type: 'value',
        name: '图片数量',
        axisLine: { show: true, lineStyle: { color: '#54c6bc' } }, // 左轴颜色
        splitLine: {
          lineStyle: {
            color: '#54c6bc', // 调整网格线颜色
            type: 'dashed', // 线条样式：可选 'solid', 'dashed', 'dotted'
          },
        },
      },

    ],
    series: [
      { name: '图片数量', type: 'bar', data: countData, yAxisIndex: 0 },

    ],
  }
})
// 图表选项
// const options =computed(() => {
//   const tagData = dataList.value.map((item) => ({
//     name: item.tag,
//     value: item.count,
//   }))
//
//   return {
//     tooltip: {
//       trigger: 'item',
//       formatter: (params: any) => `${params.name}: ${params.value} 次`,
//     },
//     series: [
//       {
//         type: 'wordCloud',
//         gridSize: 10,
//         sizeRange: [12, 50], // 字体大小范围
//         rotationRange: [-90, 90],
//         shape: 'circle',
//         textStyle: {
//           color: () =>
//             `rgb(${Math.round(Math.random() * 255)}, ${Math.round(
//               Math.random() * 255,
//             )}, ${Math.round(Math.random() * 255)})`, // 随机颜色
//         },
//         data: tagData,
//       },
//     ],
//   }
// })
</script>

<style scoped></style>
