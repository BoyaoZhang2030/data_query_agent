<template>
  <div class="products-container">
    <el-card class="products-card">
      <template #header>
        <div class="card-header">
          <h2>商品管理</h2>
          <el-button type="primary" @click="openAddDialog">添加商品</el-button>
        </div>
      </template>
      <div class="products-content">
        <!-- 筛选条件 -->
        <el-form :model="filterForm" inline>
          <el-form-item label="商品名称">
            <el-input v-model="filterForm.name" placeholder="请输入商品名称" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="filterForm.categoryId" placeholder="请选择分类">
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="filterProducts">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 商品列表 -->
        <el-table :data="pagedProducts" style="width: 100%" empty-text="暂无商品">
          <el-table-column prop="id" label="商品ID" width="80" />
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="description" label="商品描述" width="300" />
          <el-table-column prop="price" label="价格" width="100" />
          <el-table-column prop="stock" label="库存" width="80" />
          <el-table-column label="分类" width="160">
            <template #default="scope">{{ categoryName(scope.row.categoryId) }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="190">
            <template #default="scope">
              <el-button type="success" size="small" @click="openStockDialog(scope.row)">库存</el-button>
              <el-button type="primary" size="small" @click="openPriceDialog(scope.row)">改价</el-button>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredProducts.length"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="addDialogVisible" title="添加商品" width="520px" @closed="resetProductForm">
      <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" maxlength="255" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="productForm.price" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitProduct">确定添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockDialogVisible" title="调整商品库存" width="460px">
      <el-descriptions v-if="stockProduct" :column="1" border class="stock-info">
        <el-descriptions-item label="商品名称">{{ stockProduct.name }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">{{ stockProduct.stock }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="90px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="stockAction">
            <el-radio-button label="in">入库</el-radio-button>
            <el-radio-button label="out">出库</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="操作数量">
          <el-input-number v-model="stockQuantity" :min="1"
            :max="stockAction === 'out' ? Number(stockProduct?.stock || 0) : 999999"
            :precision="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockSubmitting" @click="submitStockChange">确认{{ stockAction === 'in' ? '入库' : '出库' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="priceDialogVisible" title="修改商品售价" width="440px">
      <el-alert title="只影响之后创建的销售订单，历史订单仍保留下单时的价格" type="info" :closable="false" show-icon />
      <el-form label-width="90px" class="price-form">
        <el-form-item label="商品名称">{{ priceProduct?.name }}</el-form-item>
        <el-form-item label="当前售价">¥{{ priceProduct?.price }}</el-form-item>
        <el-form-item label="新售价"><el-input-number v-model="newPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="priceDialogVisible = false">取消</el-button><el-button type="primary" :loading="priceSubmitting" @click="submitPrice">保存价格</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { dataApi } from '../api'

const products = ref([])
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const addDialogVisible = ref(false)
const submitting = ref(false)
const productFormRef = ref(null)
const stockDialogVisible = ref(false)
const stockSubmitting = ref(false)
const stockProduct = ref(null)
const stockAction = ref('in')
const stockQuantity = ref(1)
const priceDialogVisible = ref(false)
const priceSubmitting = ref(false)
const priceProduct = ref(null)
const newPrice = ref(0)

const emptyProductForm = () => ({ name: '', description: '', price: 0, stock: 0, categoryId: '' })
const productForm = ref(emptyProductForm())
const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'change' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'change' }]
}

const filterForm = ref({
  name: '',
  categoryId: ''
})

const filteredProducts = computed(() => {
  const keyword = filterForm.value.name.trim().toLowerCase()
  if (!keyword) return products.value
  return products.value.filter(product => product.name?.toLowerCase().includes(keyword))
})

const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredProducts.value.slice(start, start + pageSize.value)
})

const categoryName = (categoryId) => categories.value.find(item => item.id === categoryId)?.name || `分类 ${categoryId}`

// 加载商品数据
const loadProducts = async () => {
  try {
    const response = await dataApi.getProducts(filterForm.value.categoryId)
    if (response.code === 200) {
      products.value = response.data
      currentPage.value = 1
    }
  } catch (error) {
    console.error('加载商品数据失败:', error)
  }
}

// 加载分类数据
const loadCategories = async () => {
  try {
    const response = await dataApi.getCategories()
    if (response.code === 200) {
      categories.value = response.data
    }
  } catch (error) {
    console.error('加载分类数据失败:', error)
  }
}

// 筛选商品
const filterProducts = () => {
  currentPage.value = 1
  loadProducts()
}

// 重置筛选
const resetFilter = () => {
  filterForm.value = {
    name: '',
    categoryId: ''
  }
  loadProducts()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

const openAddDialog = () => {
  addDialogVisible.value = true
}

const resetProductForm = () => {
  productForm.value = emptyProductForm()
  productFormRef.value?.clearValidate()
}

const submitProduct = async () => {
  if (!productFormRef.value) return
  await productFormRef.value.validate()
  submitting.value = true
  try {
    const response = await dataApi.createProduct(productForm.value)
    if (response.code !== 200) {
      ElMessage.error(response.message || '商品添加失败')
      return
    }
    ElMessage.success('商品添加成功')
    addDialogVisible.value = false
    await loadProducts()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '商品添加失败，请检查后端服务')
  } finally {
    submitting.value = false
  }
}

const openStockDialog = (product) => {
  stockProduct.value = product
  stockAction.value = 'in'
  stockQuantity.value = 1
  stockDialogVisible.value = true
}

const submitStockChange = async () => {
  if (!stockProduct.value || stockQuantity.value < 1) return
  if (stockAction.value === 'out' && stockQuantity.value > stockProduct.value.stock) {
    return ElMessage.warning('出库数量不能超过当前库存')
  }
  stockSubmitting.value = true
  try {
    const change = stockAction.value === 'in' ? stockQuantity.value : -stockQuantity.value
    const response = await dataApi.adjustProductStock(stockProduct.value.id, change)
    if (response.code !== 200) return ElMessage.error(response.message || '库存调整失败')
    ElMessage.success(response.message)
    stockDialogVisible.value = false
    await loadProducts()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '库存调整失败')
  } finally {
    stockSubmitting.value = false
  }
}
const openPriceDialog = (product) => { priceProduct.value = product; newPrice.value = Number(product.price); priceDialogVisible.value = true }
const submitPrice = async () => {
  if (!priceProduct.value || newPrice.value < 0) return
  priceSubmitting.value = true
  try {
    const response = await dataApi.updateProductPrice(priceProduct.value.id, newPrice.value)
    if (response.code !== 200) return ElMessage.error(response.message)
    ElMessage.success('售价修改成功，历史订单价格不受影响')
    priceDialogVisible.value = false
    await loadProducts()
  } catch (error) { ElMessage.error(error.response?.data?.message || '价格修改失败') }
  finally { priceSubmitting.value = false }
}

onMounted(() => {
  loadProducts()
  loadCategories()
})
</script>

<style scoped>
.products-container {
  width: 100%;
}

.products-card {
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

.products-content {
  padding: 20px 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.stock-info {
  margin-bottom: 20px;
}
.price-form { margin-top: 20px; }
</style>
