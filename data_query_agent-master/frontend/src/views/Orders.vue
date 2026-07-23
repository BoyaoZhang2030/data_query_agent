<template>
  <div class="orders-container">
    <el-card class="orders-card">
      <template #header>
        <div class="card-header">
          <h2>订单管理</h2>
          <el-button type="primary" @click="openCreateDialog">新增订单</el-button>
        </div>
      </template>

      <el-form :model="filterForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="filterForm.orderNo" clearable placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="filterForm.userId" clearable placeholder="请输入用户ID" type="number" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="filterForm.status" clearable placeholder="请选择订单状态">
            <el-option v-for="status in statuses" :key="status" :label="status" :value="status" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="filterOrders">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert class="selection-summary" type="info" :closable="false" show-icon>
        <template #title>
          已选择 {{ selectedOrders.length }} 笔订单，合计金额：<strong>¥{{ selectedTotal }}</strong>
        </template>
      </el-alert>

      <el-table v-loading="loading" :data="pagedOrders" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" reserve-selection />
        <el-table-column prop="id" label="订单ID" width="90" />
        <el-table-column prop="orderNo" label="订单号" min-width="190" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="总金额" width="140">
          <template #default="scope">¥{{ formatAmount(scope.row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="danger" link @click="deleteOrder(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
          :total="total" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增订单" width="520px" @closed="resetCreateForm">
      <el-form ref="createFormRef" :model="createForm" :rules="rules" label-width="90px">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="createForm.orderNo" placeholder="留空将自动生成" />
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="createForm.userId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="订单金额" prop="totalAmount">
          <el-input-number v-model="createForm.totalAmount" :min="0" :precision="2" :step="100" controls-position="right" />
        </el-form-item>
        <el-form-item label="订单状态" prop="status">
          <el-select v-model="createForm.status" placeholder="请选择订单状态">
            <el-option v-for="status in statuses" :key="status" :label="status" :value="status" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">确定新增</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dataApi } from '../api'

const statuses = ['待付款', '待发货', '待收货', '已完成', '已取消']
const orders = ref([])
const selectedOrders = ref([])
const loading = ref(false)
const submitting = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const createFormRef = ref()
const filterForm = reactive({ orderNo: '', userId: '', status: '' })
const createForm = reactive({ orderNo: '', userId: 1, totalAmount: 0, status: '待付款' })
const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  totalAmount: [{ required: true, message: '请输入订单金额', trigger: 'blur' }],
  status: [{ required: true, message: '请选择订单状态', trigger: 'change' }]
}

const total = computed(() => orders.value.length)
const pagedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return orders.value.slice(start, start + pageSize.value)
})
const selectedTotal = computed(() => formatAmount(
  selectedOrders.value.reduce((sum, order) => sum + Number(order.totalAmount || 0), 0)
))
const formatAmount = (value) => Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await dataApi.getOrders({
      orderNo: filterForm.orderNo || undefined,
      userId: filterForm.userId || undefined,
      status: filterForm.status || undefined
    })
    if (response.code === 200) orders.value = response.data
    if (currentPage.value > Math.max(1, Math.ceil(orders.value.length / pageSize.value))) currentPage.value = 1
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载订单数据失败')
  } finally {
    loading.value = false
  }
}
const filterOrders = () => { currentPage.value = 1; loadOrders() }
const resetFilter = () => {
  Object.assign(filterForm, { orderNo: '', userId: '', status: '' })
  currentPage.value = 1
  loadOrders()
}
const handleSelectionChange = (rows) => { selectedOrders.value = rows }
const openCreateDialog = () => { dialogVisible.value = true }
const resetCreateForm = () => {
  createFormRef.value?.resetFields()
  Object.assign(createForm, { orderNo: '', userId: 1, totalAmount: 0, status: '待付款' })
}
const submitOrder = async () => {
  if (!(await createFormRef.value.validate().catch(() => false))) return
  submitting.value = true
  try {
    const response = await dataApi.createOrder(createForm)
    if (response.code !== 200) return ElMessage.error(response.message)
    ElMessage.success('订单新增成功，主页面统计将同步更新')
    dialogVisible.value = false
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '新增订单失败，订单号可能已存在')
  } finally {
    submitting.value = false
  }
}
const deleteOrder = async (order) => {
  const confirmed = await ElMessageBox.confirm(`确定删除订单 ${order.orderNo} 吗？`, '删除确认', { type: 'warning' })
    .catch(() => false)
  if (!confirmed) return
  try {
    const response = await dataApi.deleteOrder(order.id)
    if (response.code !== 200) return ElMessage.error(response.message)
    ElMessage.success('订单已删除，主页面统计将同步更新')
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '删除订单失败')
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-container { width: 100%; }
.orders-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header h2 { margin: 0; color: #409eff; }
.selection-summary { margin: 4px 0 16px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
