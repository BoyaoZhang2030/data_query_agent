<template>
  <div class="query-container">
    <el-card class="query-card">
      <template #header>
        <div class="card-header">
          <h2>📊 数据查询中心</h2>
        </div>
      </template>
      <div class="query-content">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="🤖 智能问答" name="natural-language">
            <div class="naturallanguage-section">
              <el-form :model="naturalLanguageForm">
                <el-form-item label="请描述你的问题">
                  <el-input
                    v-model="naturalLanguageForm.query"
                    type="textarea"
                    :rows="4"
                    placeholder="例如：查询本月销量最好的商品有哪些？"
                    @keyup.enter.ctrl="naturalLanguageQuery"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="naturalLanguageQuery" :loading="loading" size="large">
                    <span v-if="!loading">🔍 立即查询</span>
                    <span v-else>AI正在分析...</span>
                  </el-button>
                  <el-button @click="showTemplates" size="large">📋 查看示例</el-button>
                </el-form-item>
              </el-form>

              <!-- 快捷查询标签 -->
              <div class="quick-queries">
                <span class="quick-label">快捷查询：</span>
                <el-tag
                  v-for="(q, index) in quickQueries"
                  :key="index"
                  class="quick-tag"
                  @click="useQuickQuery(q)"
                >
                  {{ q }}
                </el-tag>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="💻 SQL查询" name="sql">
            <el-form :model="sqlForm">
              <el-form-item label="SQL语句">
                <el-input
                  v-model="sqlForm.sql"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入SQL语句，例如：SELECT * FROM products"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="sqlQuery" :loading="loading" size="large">
                  <span v-if="!loading">▶ 执行查询</span>
                  <span v-else>执行中...</span>
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="📜 查询历史" name="history">
            <el-table :data="queryHistory" style="width: 100%" v-if="queryHistory.length > 0">
              <el-table-column prop="naturalLanguage" label="查询语句" width="300" show-overflow-tooltip />
              <el-table-column prop="sqlQuery" label="SQL语句" width="350" show-overflow-tooltip />
              <el-table-column prop="resultCount" label="结果数" width="100" />
              <el-table-column prop="createdAt" label="查询时间" width="180" />
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button type="text" @click="reexecuteQuery(scope.row)">重新执行</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无查询历史" />
          </el-tab-pane>
        </el-tabs>

        <!-- 查询结果 -->
        <el-card v-if="queryResult" class="result-card">
          <template #header>
            <div class="result-header">
              <h3>📋 查询结果</h3>
              <div>
                <el-button type="text" @click="exportResult">📥 导出CSV</el-button>
                <el-button type="text" @click="queryResult = null">❌ 关闭</el-button>
              </div>
            </div>
          </template>
          <div class="result-content">
            <div class="sql-display" v-if="queryResult.sql">
              <el-tag type="info">生成的SQL</el-tag>
              <pre>{{ queryResult.sql }}</pre>
            </div>
            <el-table :data="queryResult.result" style="width: 100%" border stripe>
              <el-table-column
                v-for="(value, key) in queryResult.result[0] || {}"
                :key="key"
                :prop="key"
                :label="formatLabel(key)"
                min-width="120"
              />
            </el-table>
            <div class="result-info">
              共 {{ queryResult.count }} 条记录
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 常用模板对话框 -->
    <el-dialog
      v-model="templatesDialogVisible"
      title="📚 常用查询示例"
      width="700px"
    >
      <div class="template-categories">
        <div v-for="category in templateCategories" :key="category.name" class="template-category">
          <h4>{{ category.name }}</h4>
          <el-space wrap>
            <el-tag
              v-for="(template, index) in category.templates"
              :key="index"
              class="template-tag"
              @click="useTemplate(template)"
            >
              {{ template }}
            </el-tag>
          </el-space>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { queryApi } from '../api'

const activeTab = ref('natural-language')
const loading = ref(false)
const queryResult = ref(null)
const queryHistory = ref([])
const queryTemplates = ref([])
const templatesDialogVisible = ref(false)

const naturalLanguageForm = ref({
  query: ''
})

const sqlForm = ref({
  sql: ''
})

const quickQueries = [
  '本月销售额',
  '销量前10商品',
  '待发货订单',
  '库存不足商品',
  '用户购买统计'
]

const templateCategories = [
  {
    name: '📦 商品查询',
    templates: [
      '查询所有商品信息',
      '查询价格大于500元的商品',
      '查询库存低于100的商品',
      '查询每个分类的商品数量',
      '查询销量最高的前10个商品',
      '查询商品平均价格'
    ]
  },
  {
    name: '🛒 订单查询',
    templates: [
      '查询所有订单',
      '查询已完成的订单',
      '查询待发货的订单',
      '查询待付款的订单',
      '查询最近7天的订单',
      '查询订单总额超过1000元的订单',
      '查询每个用户的订单数量'
    ]
  },
  {
    name: '💰 销售统计',
    templates: [
      '查询总销售额',
      '查询本月销售额',
      '查询每个商品的销售总额',
      '查询销售额最高的前10个商品',
      '查询各类别的销售占比'
    ]
  },
  {
    name: '👥 用户统计',
    templates: [
      '查询所有用户',
      '查询下单次数最多的用户',
      '查询每个用户的消费总额',
      '查询新用户数量'
    ]
  },
  {
    name: '📂 分类统计',
    templates: [
      '查询所有分类',
      '查询每个分类的商品数量',
      '查询每个分类的销售总额',
      '查询热门分类'
    ]
  }
]

const getUserId = () => {
  const user = localStorage.getItem('user')
  if (user) {
    return JSON.parse(user).id
  }
  return 1
}

const naturalLanguageQuery = async () => {
  if (!naturalLanguageForm.value.query) {
    ElMessage.warning('请输入查询语句')
    return
  }

  loading.value = true
  try {
    const response = await queryApi.naturalLanguageQuery({
      naturalLanguage: naturalLanguageForm.value.query,
      userId: getUserId()
    })
    if (response.code === 200) {
      queryResult.value = response.data
      loadQueryHistory()
      ElMessage.success('查询成功')
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const sqlQuery = async () => {
  if (!sqlForm.value.sql) {
    ElMessage.warning('请输入SQL语句')
    return
  }

  loading.value = true
  try {
    const response = await queryApi.sqlQuery({
      sql: sqlForm.value.sql,
      userId: getUserId()
    })
    if (response.code === 200) {
      queryResult.value = response.data
      loadQueryHistory()
      ElMessage.success('查询成功')
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const loadQueryHistory = async () => {
  try {
    const response = await queryApi.getHistory(getUserId())
    if (response.code === 200) {
      queryHistory.value = response.data || []
    }
  } catch (error) {
    console.error('加载查询历史失败:', error)
  }
}

const loadQueryTemplates = async () => {
  try {
    const response = await queryApi.getTemplates()
    if (response.code === 200) {
      queryTemplates.value = response.data.templates
    }
  } catch (error) {
    console.error('加载查询模板失败:', error)
  }
}

const showTemplates = () => {
  templatesDialogVisible.value = true
}

const useTemplate = (template) => {
  naturalLanguageForm.value.query = template
  activeTab.value = 'natural-language'
  templatesDialogVisible.value = false
}

const useQuickQuery = (query) => {
  naturalLanguageForm.value.query = query
  naturalLanguageQuery()
}

const reexecuteQuery = (history) => {
  if (history.naturalLanguage !== 'SQL查询') {
    naturalLanguageForm.value.query = history.naturalLanguage
    activeTab.value = 'natural-language'
  } else {
    sqlForm.value.sql = history.sqlQuery
    activeTab.value = 'sql'
  }
}

const formatLabel = (key) => {
  const labelMap = {
    'id': 'ID',
    'name': '名称',
    'username': '用户名',
    'email': '邮箱',
    'price': '价格',
    'stock': '库存',
    'total_amount': '总金额',
    'total_sales': '销售总额',
    'total_quantity': '销售数量',
    'order_count': '订单数',
    'product_count': '商品数',
    'user_count': '用户数',
    'category_name': '分类名称',
    'status': '状态',
    'created_at': '创建时间',
    'updated_at': '更新时间'
  }
  return labelMap[key] || key
}

const exportResult = () => {
  if (!queryResult.value || !queryResult.value.result) return

  const headers = Object.keys(queryResult.value.result[0] || {})
  const csvContent = [
    headers.join(','),
    ...queryResult.value.result.map(row =>
      headers.map(header => {
        const val = row[header]
        if (val === null || val === undefined) return ''
        const str = String(val)
        return str.includes(',') ? `"${str}"` : str
      }).join(',')
    )
  ].join('\n')

  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.setAttribute('download', `query-result-${new Date().toISOString().slice(0, 10)}.csv`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('导出成功')
}

onMounted(() => {
  loadQueryHistory()
  loadQueryTemplates()
})
</script>

<style scoped>
.query-container {
  width: 100%;
}

.query-card {
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  color: #409EFF;
  font-size: 24px;
}

.query-content {
  padding: 20px 0;
}

.naturallanguage-section {
  padding: 10px 0;
}

.quick-queries {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.quick-label {
  font-weight: 600;
  color: #606266;
  margin-right: 10px;
}

.quick-tag {
  margin: 5px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-tag:hover {
  transform: scale(1.05);
}

.result-card {
  margin-top: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.sql-display {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.sql-display pre {
  margin: 10px 0 0 0;
  white-space: pre-wrap;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  color: #409EFF;
}

.result-info {
  margin-top: 15px;
  text-align: right;
  color: #606266;
  font-size: 14px;
}

.template-categories {
  padding: 10px 0;
}

.template-category {
  margin-bottom: 20px;
}

.template-category h4 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 15px;
}

.template-tag {
  margin: 5px;
  cursor: pointer;
  transition: all 0.3s;
}

.template-tag:hover {
  background-color: #409EFF;
  color: #fff;
}
</style>