<template>
  <el-card class="chart-card">
    <template #header>
      <span>数据统计图表</span>
    </template>
    <div ref="chartRef" class="chart-container"></div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import type { StatisticsVO } from '@/api/admin'

const props = defineProps<{
  stats: StatisticsVO
}>()

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null
let refreshTimer: number | null = null

// 根据 stats 生成饼图数据
const getChartData = () => {
  return [
    { name: '总用户数', value: props.stats.totalUsers || 0 },
    { name: '今日活跃', value: props.stats.todayActiveUsers || 0 },
    { name: '今日消息', value: props.stats.todayMessages || 0 },
    { name: '在线人数', value: props.stats.onlineUsers || 0 }
  ]
}

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: getChartData().map(item => item.name)
    },
    series: [
      {
        name: '平台数据统计',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%',
          position: 'outside'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: getChartData()
      }
    ]
  }

  chartInstance.setOption(option)
}

const updateChart = () => {
  if (chartInstance) {
    chartInstance.setOption({
      legend: {
        data: getChartData().map(item => item.name)
      },
      series: [{
        data: getChartData()
      }]
    })
  }
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
  refreshTimer = setInterval(() => emit('refresh'), 30000) as unknown as number
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (refreshTimer) clearInterval(refreshTimer)
  chartInstance?.dispose()
})

// 监听 stats 变化，更新图表
watch(() => props.stats, () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
.chart-card {
  margin-top: 20px;
}

.chart-container {
  width: 100%;
  height: 400px;
}
</style>