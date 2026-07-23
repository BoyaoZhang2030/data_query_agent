<template>
  <div class="products-container">
    <el-card class="products-card">
      <template #header>
        <div class="card-header">
          <h2>商品管理</h2>
          <el-button type="primary">添加商品</el-button>
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
        <el-table :data="products" style="width: 100%">
          <el-table-column prop="id" label="商品ID" width="80" />
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="description" label="商品描述" width="300" />
          <el-table-column prop="price" label="价格" width="100" />
          <el-table-column prop="stock" label="库存" width="80" />
          <el-table-column prop="categoryId" label="分类ID" width="100" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small">编辑</el-button>
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
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dataApi } from '../api'

const products = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const filterForm = ref({
  name: '',
  categoryId: ''
})

// 加载商品数据
const loadProducts = async () => {
  try {
    const response = await dataApi.getProducts(filterForm.value.categoryId)
    if (response.code === 200) {
      products.value = response.data
      total.value = response.data.length
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
  loadProducts()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadProducts()
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
</style>