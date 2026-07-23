<template>
  <div class="categories-container">
    <el-card class="categories-card">
      <template #header>
        <div class="card-header">
          <h2>分类管理</h2>
          <el-button type="primary">添加分类</el-button>
        </div>
      </template>
      <div class="categories-content">
        <el-card class="module-card tree-module" shadow="never">
          <template #header><div class="module-title"><span>分类目录</span><small>点击分类查看包含的商品</small></div></template>
        <!-- 分类树 -->
        <el-tree
          :data="categoryTree"
          :props="defaultProps"
          node-key="id"
          default-expand-all
          highlight-current
          @node-click="selectCategory"
        >
          <template #default="{ node, data }">
            <span class="category-node">
              <span>{{ node.label }}</span>
              <span class="category-actions">
                <el-button type="primary" size="small">编辑</el-button>
                <el-button type="danger" size="small">删除</el-button>
              </span>
            </span>
          </template>
        </el-tree>

        </el-card>

        <el-card class="module-card detail-module" shadow="never">
          <template #header><div class="module-title"><span>分类详情</span><small>{{ selectedCategory ? `当前：${selectedCategory.name}` : '请先选择一个分类' }}</small></div></template>

        <el-card v-if="selectedCategory" class="category-products" shadow="never">
          <template #header>
            <div class="selected-header">
              <span>“{{ selectedCategory.name }}”下的商品</span>
              <el-tag>{{ selectedProducts.length }} 件</el-tag>
            </div>
          </template>
          <el-table :data="selectedProducts" style="width: 100%" empty-text="该分类下暂无商品">
            <el-table-column prop="id" label="商品ID" width="90" />
            <el-table-column prop="name" label="商品名称" min-width="180" />
            <el-table-column label="所属分类" width="160">
              <template #default="scope">{{ categoryName(scope.row.categoryId) }}</template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="120" />
            <el-table-column prop="stock" label="库存" width="100" />
            <el-table-column prop="description" label="商品描述" min-width="240" show-overflow-tooltip />
          </el-table>
        </el-card>

        <!-- 分类列表 -->
        <el-table :data="categories" style="width: 100%; margin-top: 20px;">
          <el-table-column prop="id" label="分类ID" width="80" />
          <el-table-column prop="name" label="分类名称" />
          <el-table-column prop="parentId" label="父分类ID" width="120" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small">编辑</el-button>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dataApi } from '../api'

const categories = ref([])
const allProducts = ref([])
const selectedCategory = ref(null)
const defaultProps = {
  children: 'children',
  label: 'name'
}

// 构建分类树
const categoryTree = computed(() => {
  const tree = []
  const map = new Map()

  // 构建ID到分类的映射
  categories.value.forEach(category => {
    map.set(category.id, { ...category, children: [] })
  })

  // 构建树形结构
  categories.value.forEach(category => {
    if (category.parentId === null || category.parentId === 0) {
      // 根节点
      tree.push(map.get(category.id))
    } else {
      // 子节点
      const parent = map.get(category.parentId)
      if (parent) {
        parent.children.push(map.get(category.id))
      }
    }
  })

  return tree
})

const descendantCategoryIds = computed(() => {
  if (!selectedCategory.value) return new Set()
  const ids = new Set([selectedCategory.value.id])
  let changed = true
  while (changed) {
    changed = false
    categories.value.forEach(category => {
      if (ids.has(category.parentId) && !ids.has(category.id)) {
        ids.add(category.id)
        changed = true
      }
    })
  }
  return ids
})

const selectedProducts = computed(() => allProducts.value.filter(product => descendantCategoryIds.value.has(product.categoryId)))
const categoryName = (categoryId) => categories.value.find(item => item.id === categoryId)?.name || `分类 ${categoryId}`

const selectCategory = (category) => {
  selectedCategory.value = category
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

const loadProducts = async () => {
  try {
    const response = await dataApi.getProducts()
    if (response.code === 200) allProducts.value = response.data || []
  } catch (error) {
    console.error('加载分类商品失败:', error)
  }
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.categories-container {
  width: 100%;
}

.categories-card {
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

.categories-content {
  padding: 20px 0;
}
.module-card { border-radius: 14px; border: 1px solid #e5edf7; }
.detail-module { margin-top: 20px; }
.module-title { display: flex; align-items: center; justify-content: space-between; font-weight: 700; color: #263d58; }
.module-title small { color: #94a5b6; font-weight: 400; }
.tree-module :deep(.el-tree-node__content) { height: 44px; border-radius: 9px; margin: 2px 0; }
.tree-module :deep(.el-tree-node__content:hover) { background: #eef7ff; }

.category-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.category-actions {
  display: flex;
  gap: 10px;
}

.category-products {
  margin-top: 20px;
}

.selected-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}
</style>
