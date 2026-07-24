<template>
  <div class="orders-page">
    <header>
      <div class="header-inner">
        <div class="brand" @click="$router.push('/shop')"><span>淘</span><b>淘气宝贝</b></div>
        <nav><button @click="$router.push('/shop')">商城首页</button><strong>我的订单</strong><span>{{ user.username }}</span><button @click="logout">退出</button></nav>
      </div>
    </header>
    <main>
      <div class="title"><div><small>MY ORDERS</small><h1>我的订单</h1></div><el-button @click="loadOrders">刷新订单</el-button></div>
      <div class="tabs"><button v-for="item in statusTabs" :key="item.value" :class="{ active: status === item.value }" @click="status = item.value">{{ item.label }}</button></div>
      <div v-loading="loading" class="order-list">
        <article v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-head"><div><b>订单号 {{ order.orderNo }}</b><small>{{ formatTime(order.createdAt) }}</small></div><el-tag :type="statusType(order.status)" effect="light">{{ order.status }}</el-tag></div>
          <div class="items">
            <div v-for="item in order.items" :key="item.productId" class="item"><span>🎁</span><div><b>{{ item.productName }}</b><small>¥{{ formatPrice(item.price) }} × {{ item.quantity }}</small></div><strong>¥{{ formatPrice(item.subtotal) }}</strong></div>
          </div>
          <div class="order-foot"><div><span>收货信息：{{ order.shippingAddress || '未填写' }}</span></div><p>共 {{ itemCount(order) }} 件商品　实付款：<strong>¥{{ formatPrice(order.totalAmount) }}</strong></p></div>
        </article>
        <el-empty v-if="!loading && !filteredOrders.length" description="暂无相关订单"><el-button type="primary" @click="$router.push('/shop')">去逛逛</el-button></el-empty>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { dataApi } from '../api'
const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const orders = ref([])
const status = ref('')
const loading = ref(false)
const statusTabs = [{ label: '全部订单', value: '' }, { label: '待付款', value: '待付款' }, { label: '待发货', value: '待发货' }, { label: '待收货', value: '待收货' }, { label: '已完成', value: '已完成' }]
const filteredOrders = computed(() => status.value ? orders.value.filter(order => order.status === status.value) : orders.value)
const loadOrders = async () => {
  loading.value = true
  try {
    const response = await dataApi.getOrders({ userId: user.id })
    if (response.code !== 200) throw new Error(response.message)
    orders.value = await Promise.all((response.data || []).map(async order => {
      const items = await dataApi.getOrderItems(order.id)
      return { ...order, items: items.data || [] }
    }))
  } catch (error) { ElMessage.error(error.response?.data?.message || error.message || '订单加载失败') }
  finally { loading.value = false }
}
const itemCount = order => order.items?.reduce((sum, item) => sum + item.quantity, 0) || 0
const formatPrice = value => Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const formatTime = value => value ? new Date(value).toLocaleString('zh-CN') : ''
const statusType = value => ({ 待付款: 'warning', 已付款: 'primary', 待发货: 'primary', 待收货: 'success', 已完成: 'success', 已取消: 'info' }[value] || 'info')
const logout = () => { localStorage.removeItem('user'); router.push('/login') }
onMounted(loadOrders)
</script>

<style scoped>
.orders-page{min-height:100vh;color:#333;background:#f5f5f5;font-family:Inter,"PingFang SC",sans-serif}header{height:72px;background:#fff;border-bottom:1px solid #eee}.header-inner{max-width:1100px;height:100%;margin:auto;padding:0 20px;display:flex;align-items:center;justify-content:space-between}.brand{display:flex;align-items:center;gap:10px;color:#ff5000;cursor:pointer}.brand span{width:38px;height:38px;display:grid;place-items:center;color:#fff;border-radius:12px;background:linear-gradient(135deg,#ff7800,#ff3d00);font-weight:800}.brand b{font-size:20px}.header-inner nav{display:flex;gap:22px;align-items:center;color:#777}.header-inner button{border:0;color:#666;background:none;cursor:pointer}.header-inner button:hover{color:#ff5000}main{max-width:1000px;margin:auto;padding:38px 20px}.title{display:flex;justify-content:space-between;align-items:end}.title small{color:#ff5000;letter-spacing:3px}.title h1{margin:7px 0 0}.tabs{margin:25px 0 18px;padding:0 18px;display:flex;gap:34px;border-radius:12px;background:#fff}.tabs button{height:58px;border:0;border-bottom:3px solid transparent;color:#666;background:none;cursor:pointer}.tabs button.active{color:#ff5000;border-bottom-color:#ff5000;font-weight:700}.order-list{display:grid;gap:16px;min-height:300px}.order-card{overflow:hidden;border-radius:14px;background:#fff;box-shadow:0 6px 24px #00000008}.order-head{padding:16px 20px;display:flex;justify-content:space-between;align-items:center;background:#fffaf7;border-bottom:1px solid #f4eee9}.order-head b,.order-head small{display:block}.order-head small{margin-top:5px;color:#999}.items{padding:0 20px}.item{padding:17px 0;display:grid;grid-template-columns:55px 1fr auto;gap:14px;align-items:center;border-bottom:1px solid #f2f2f2}.item>span{width:55px;height:55px;display:grid;place-items:center;border-radius:10px;background:#fff3ea;font-size:28px}.item b,.item small{display:block}.item small{margin-top:7px;color:#999}.item>strong{color:#555}.order-foot{padding:18px 20px;display:flex;justify-content:space-between;align-items:center}.order-foot span{color:#999;font-size:12px}.order-foot p{margin:0}.order-foot p strong{color:#ff5000;font-size:20px}
@media(max-width:650px){.header-inner nav span,.header-inner nav strong{display:none}.tabs{gap:8px;overflow:auto}.tabs button{min-width:70px}.order-foot{align-items:flex-start;flex-direction:column;gap:12px}.item{grid-template-columns:48px 1fr}.item>strong{grid-column:2}}
</style>
