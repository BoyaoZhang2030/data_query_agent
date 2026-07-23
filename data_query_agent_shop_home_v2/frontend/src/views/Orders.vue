<template>
  <div class="orders-container">
    <el-card class="orders-card">
      <template #header>
        <div class="card-header">
          <h2>订单管理</h2>
          <div><el-button type="primary" @click="openCreateDialog('SALES')">新增销售订单</el-button><el-button type="success" @click="openCreateDialog('PURCHASE')">新增入库订单</el-button></div>
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
        <el-table-column label="类型" width="100"><template #default="scope"><el-tag :type="orderType(scope.row) === 'PURCHASE' ? 'success' : 'primary'">{{ orderType(scope.row) === 'PURCHASE' ? '入库' : '销售' }}</el-tag></template></el-table-column>
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="商品明细" min-width="230"><template #default="scope"><div v-if="scope.row.items?.length" class="items-cell"><span v-for="item in scope.row.items" :key="item.productId">{{ item.productName }} × {{ item.quantity }}</span></div><span v-else class="muted">无明细</span></template></el-table-column>
        <el-table-column label="总金额" width="140">
          <template #default="scope">¥{{ formatAmount(scope.row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120" />
        <el-table-column label="备注" min-width="150" show-overflow-tooltip><template #default="scope">{{ scope.row.shippingAddress || '—' }}</template></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="openEditDialog(scope.row)">修改</el-button>
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

    <el-dialog v-model="dialogVisible" :title="createForm.orderType === 'PURCHASE' ? '新增入库订单' : '新增销售订单'" width="720px" @closed="resetCreateForm">
      <el-form ref="createFormRef" :model="createForm" :rules="rules" label-width="90px">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="createForm.orderNo" placeholder="留空将自动生成" />
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="createForm.userId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="订单状态" prop="status">
          <el-select v-model="createForm.status" placeholder="请选择订单状态">
            <el-option v-for="status in statuses" :key="status" :label="status" :value="status" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="可填写客户、供应商或其他说明" /></el-form-item>
        <el-form-item label="购买商品" required>
          <div class="order-items">
            <div v-for="(item, index) in createItems" :key="index" class="order-item-row">
              <el-select v-model="item.productId" filterable placeholder="选择商品" class="product-select"
                @change="normalizeQuantity(item)">
                <el-option v-for="product in availableProducts" :key="product.id"
                  :label="`${product.name}（库存 ${product.stock}，售价 ¥${formatAmount(product.price)}）`"
                  :value="product.id" :disabled="isProductSelected(product.id, index) || (createForm.orderType === 'SALES' && product.stock <= 0)" />
              </el-select>
              <el-input-number v-model="item.quantity" :min="1" :max="createForm.orderType === 'SALES' ? productStock(item.productId) : 999999"
                :precision="0" controls-position="right" />
              <el-input-number v-if="createForm.orderType === 'PURCHASE'" v-model="item.unitPrice" :min="0" :precision="2" placeholder="采购单价" controls-position="right" />
              <el-button type="danger" link :disabled="createItems.length === 1" @click="removeOrderItem(index)">删除</el-button>
            </div>
            <el-button type="primary" plain @click="addOrderItem">添加商品</el-button>
          </div>
        </el-form-item>
        <el-form-item label="订单总额">
          <strong class="calculated-total">¥{{ calculatedTotal }}</strong>
          <span class="total-tip">{{ createForm.orderType === 'PURCHASE' ? '按采购单价自动计算并增加库存' : '按当前售价自动计算并扣减库存' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">确定新增</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="修改订单状态" width="460px">
      <el-descriptions v-if="editingOrder" :column="1" border class="edit-order-info">
        <el-descriptions-item label="订单号">{{ editingOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ formatAmount(editingOrder.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ editingOrder.status }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="90px">
        <el-form-item label="修改为">
          <el-select v-model="editStatus" placeholder="请选择新状态" style="width: 100%">
            <el-option v-for="status in statuses" :key="status" :label="status" :value="status" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="submitStatusUpdate">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dataApi } from '../api'

const statuses = ['待付款', '已付款', '待发货', '待收货', '已完成', '已取消', '已入库']
const orders = ref([])
const availableProducts = ref([])
const createItems = ref([{ productId: '', quantity: 1, unitPrice: 0 }])
const selectedOrders = ref([])
const loading = ref(false)
const submitting = ref(false)
const updating = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const editDialogVisible = ref(false)
const editingOrder = ref(null)
const editStatus = ref('')
const createFormRef = ref()
const filterForm = reactive({ orderNo: '', userId: '', status: '' })
const createForm = reactive({ orderNo: '', userId: 1, status: '待付款', orderType: 'SALES', remark: '' })
const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
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
const calculatedTotal = computed(() => formatAmount(createItems.value.reduce((sum, item) => {
  const product = availableProducts.value.find(product => product.id === item.productId)
  const price = createForm.orderType === 'PURCHASE' ? item.unitPrice : product?.price
  return sum + Number(price || 0) * Number(item.quantity || 0)
}, 0)))
const orderType = (order) => order.paymentMethod === 'PURCHASE' ? 'PURCHASE' : 'SALES'
const formatAmount = (value) => Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await dataApi.getOrders({
      orderNo: filterForm.orderNo || undefined,
      userId: filterForm.userId || undefined,
      status: filterForm.status || undefined
    })
    if (response.code === 200) {
      orders.value = await Promise.all((response.data || []).map(async order => {
        const detail = await dataApi.getOrderItems(order.id).catch(() => ({ data: [] }))
        return { ...order, items: detail.data || [] }
      }))
    }
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
const openCreateDialog = (type) => {
  Object.assign(createForm, { orderType: type, status: type === 'PURCHASE' ? '已入库' : '待付款' })
  dialogVisible.value = true
}
const resetCreateForm = () => {
  createFormRef.value?.resetFields()
  Object.assign(createForm, { orderNo: '', userId: 1, status: '待付款', orderType: 'SALES', remark: '' })
  createItems.value = [{ productId: '', quantity: 1, unitPrice: 0 }]
}
const productStock = (productId) => availableProducts.value.find(product => product.id === productId)?.stock || 1
const isProductSelected = (productId, currentIndex) => createItems.value.some((item, index) => index !== currentIndex && item.productId === productId)
const normalizeQuantity = (item) => { if (createForm.orderType === 'SALES') item.quantity = Math.min(Math.max(1, item.quantity), productStock(item.productId)) }
const addOrderItem = () => { createItems.value.push({ productId: '', quantity: 1, unitPrice: 0 }) }
const removeOrderItem = (index) => { createItems.value.splice(index, 1) }
const submitOrder = async () => {
  if (!(await createFormRef.value.validate().catch(() => false))) return
  if (createItems.value.some(item => !item.productId || item.quantity < 1)) {
    return ElMessage.warning('请选择商品并填写有效的购买数量')
  }
  if (createForm.orderType === 'PURCHASE' && createItems.value.some(item => item.unitPrice == null || item.unitPrice < 0)) return ElMessage.warning('请输入有效的采购单价')
  const overStockItem = createForm.orderType === 'SALES' && createItems.value.find(item => item.quantity > productStock(item.productId))
  if (overStockItem) return ElMessage.warning('购买数量不能超过商品当前库存')
  submitting.value = true
  try {
    const response = await dataApi.createOrder({ ...createForm, items: createItems.value })
    if (response.code !== 200) return ElMessage.error(response.message)
    ElMessage.success('订单新增成功，主页面统计将同步更新')
    dialogVisible.value = false
    await Promise.all([loadOrders(), loadProducts()])
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '新增订单失败，订单号可能已存在')
  } finally {
    submitting.value = false
  }
}
const loadProducts = async () => {
  try {
    const response = await dataApi.getProducts()
    if (response.code === 200) availableProducts.value = response.data || []
  } catch (error) {
    ElMessage.error('加载商品库存失败')
  }
}
const openEditDialog = (order) => {
  editingOrder.value = order
  editStatus.value = order.status
  editDialogVisible.value = true
}
const submitStatusUpdate = async () => {
  if (!editingOrder.value || !editStatus.value) {
    return ElMessage.warning('请选择订单状态')
  }
  if (editStatus.value === editingOrder.value.status) {
    return ElMessage.warning('订单状态没有变化')
  }
  updating.value = true
  try {
    const response = await dataApi.updateOrderStatus(editingOrder.value.id, editStatus.value)
    if (response.code !== 200) return ElMessage.error(response.message || '修改失败')
    ElMessage.success(`订单状态已修改为“${editStatus.value}”`)
    editDialogVisible.value = false
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '修改订单状态失败')
  } finally {
    updating.value = false
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

onMounted(() => { loadOrders(); loadProducts() })
</script>

<style scoped>
.orders-container { width: 100%; }
.orders-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header h2 { margin: 0; color: #409eff; }
.selection-summary { margin: 4px 0 16px; }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.edit-order-info { margin-bottom: 20px; }
.order-items { width: 100%; }
.order-item-row { display: flex; gap: 10px; margin-bottom: 10px; }
.product-select { flex: 1; }
.calculated-total { color: #f56c6c; font-size: 20px; margin-right: 10px; }
.total-tip { color: #909399; font-size: 12px; }
.items-cell { display: flex; flex-direction: column; gap: 3px; color: #4d6278; font-size: 13px; }.muted { color: #a6b2bf; }
</style>
