<template>
  <div class="dashboard-container">
    <el-card class="dashboard-card">
      <template #header>
        <div class="card-header">
          <h2>数据概览</h2>
        </div>
      </template>
      <div class="dashboard-content">
        <div class="stats-grid">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ productCount }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </el-card>
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ orderCount }}</div>
              <div class="stat-label">交易量（订单数）</div>
            </div>
          </el-card>
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ categoryCount }}</div>
              <div class="stat-label">分类总数</div>
            </div>
          </el-card>
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value">{{ totalSales }}</div>
              <div class="stat-label">交易额（全部订单）</div>
            </div>
          </el-card>
        </div>
        <div class="charts-grid">
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>商品分类分布</h3>
              </div>
            </template>
            <div ref="categoryChartRef" class="chart-container"></div>
          </el-card>
          <el-card class="chart-card">
            <template #header>
              <div class="chart-header">
                <h3>订单状态分布</h3>
              </div>
            </template>
            <div ref="orderStatusChartRef" class="chart-container"></div>
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { dataApi } from '../api'

// 统计数据
const productCount = ref(0)
const orderCount = ref(0)
const categoryCount = ref(0)
const totalSales = ref(0)

// 图表引用
const categoryChartRef = ref(null)
const orderStatusChartRef = ref(null)
let categoryChart = null
let orderStatusChart = null

// 初始化图表
const initCharts = () => {
  // 商品分类分布图表
  if (categoryChartRef.value) {
    categoryChart = echarts.init(categoryChartRef.value)
    categoryChart.setOption({
      title: {
        text: '商品分类分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '商品数量',
          type: 'pie',
          radius: '50%',
          data: [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }

  // 订单状态分布图表
  if (orderStatusChartRef.value) {
    orderStatusChart = echarts.init(orderStatusChartRef.value)
    orderStatusChart.setOption({
      title: {
        text: '订单状态分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '订单数量',
          type: 'pie',
          radius: '50%',
          data: [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }
}

// 加载数据
const loadData = async () => {
  try {
    const [overview, category, orderStatus] = await Promise.all([
      dataApi.getOverview(),
      dataApi.getCategoryStatistics(),
      dataApi.getOrderStatusStatistics()
    ])
    const data = overview.data
    productCount.value = data.productCount
    orderCount.value = data.orderCount
    categoryCount.value = data.categoryCount
    totalSales.value = Number(data.transactionAmount || 0).toLocaleString('zh-CN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
    categoryChart?.setOption({ series: [{ data: category.data.categoryStats.map(item => ({
      value: item.productCount,
      name: item.categoryName
    })) }] })
    orderStatusChart?.setOption({ series: [{ data: orderStatus.data.orderStatusStats.map(item => ({
      value: item.count,
      name: item.status
    })) }] })
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const resizeCharts = () => {
  categoryChart?.resize()
  orderStatusChart?.resize()
}

onMounted(async () => {
  initCharts()
  await loadData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  categoryChart?.dispose()
  orderStatusChart?.dispose()
})
</script>

<style scoped>
.dashboard-container {
  width: 100%;
}

.dashboard-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #409EFF;
}

.dashboard-content {
  padding: 20px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  height: 400px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
