<template>
  <div ref="chartRef" :style="{ width, height }" class="cl-chart"></div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  option: { type: Object, required: true },
  width: { type: String, default: '100%' },
  height: { type: String, default: '320px' }
})

const chartRef = ref(null)
let chartInstance = null

function resize() {
  chartInstance && chartInstance.resize()
}

onMounted(() => {
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption(props.option)
  window.addEventListener('resize', resize)
})

watch(() => props.option, (val) => {
  if (chartInstance) {
    chartInstance.setOption(val, true)
  }
}, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chartInstance && chartInstance.dispose()
})
</script>

<style scoped>
.cl-chart {
  min-height: 200px;
}
</style>
