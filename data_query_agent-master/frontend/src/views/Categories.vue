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
        <!-- 分类树 -->
        <el-tree
          :data="categoryTree"
          :props="defaultProps"
          node-key="id"
          default-expand-all
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
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dataApi } from '../api'

const categories = ref([])
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

onMounted(() => {
  loadCategories()
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
</style>