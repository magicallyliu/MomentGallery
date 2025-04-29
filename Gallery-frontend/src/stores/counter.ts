import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

//一个状态就存储一个要共享的数据
export const useCounterStore = defineStore('counter', () => {
  //定义状态的初始值
  const count = ref(0)
  //定义变量的计算逻辑
  const doubleCount = computed(() => count.value * 2)
  //定义怎么修改状态
  function increment() {
    count.value++
  }
  //返回定义的函数
  return { count, doubleCount, increment }
})
