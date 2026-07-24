<template>
  <div class="query-page">
    <el-card>
      <template #header><div class="header"><h2>数据查询中心</h2><span>自然语言、SQL、可视化与 AI 洞察</span></div></template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="智能问答" name="natural">
          <el-input v-model="naturalQuery" type="textarea" :rows="4" placeholder="例如：查询销量最高的前 10 个商品" />
          <el-button class="action" type="primary" :loading="loading" @click="runNaturalQuery">立即查询</el-button>
        </el-tab-pane>
        <el-tab-pane label="SQL 查询" name="sql">
          <el-input v-model="sql" type="textarea" :rows="5" placeholder="仅支持 SELECT 查询" />
          <el-button class="action" type="primary" :loading="loading" @click="runSqlQuery">执行查询</el-button>
        </el-tab-pane>
        <el-tab-pane label="查询历史" name="history">
          <el-table :data="history" stripe>
            <el-table-column prop="naturalLanguage" label="查询内容" min-width="220" />
            <el-table-column prop="sqlQuery" label="SQL" min-width="300" show-overflow-tooltip />
            <el-table-column prop="resultCount" label="结果数" width="90" />
            <el-table-column prop="createdAt" label="时间" width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <template v-if="rows.length">
      <el-card class="result-card">
        <template #header>
          <div class="result-header">
            <div><h3>查询结果</h3><small>共 {{ rows.length }} 条记录</small></div>
            <el-space>
              <el-button @click="exportCsv">导出 CSV</el-button>
              <el-button @click="exportExcel">导出 Excel</el-button>
              <el-button type="primary" :loading="analyzing" @click="analyze">AI 分析</el-button>
            </el-space>
          </div>
        </template>
        <div v-if="result.sql" class="sql-box"><b>执行 SQL</b><code>{{ result.sql }}</code></div>
        <el-table :data="rows" border stripe max-height="480">
          <el-table-column v-for="column in columns" :key="column" :prop="column" :label="column" min-width="130" show-overflow-tooltip />
        </el-table>
      </el-card>

      <el-card v-if="chartConfig" class="result-card">
        <template #header>
          <div class="result-header"><div><h3>自动可视化</h3><small>{{ chartDescription }}</small></div>
            <el-radio-group v-model="chartType" size="small"><el-radio-button label="bar">柱状图</el-radio-button><el-radio-button label="line">折线图</el-radio-button><el-radio-button label="pie">饼图</el-radio-button></el-radio-group>
          </div>
        </template>
        <div ref="chartElement" class="chart"></div>
      </el-card>

      <el-card v-if="analysis" class="result-card analysis-card">
        <template #header><h3>AI 数据洞察</h3></template>
        <div class="analysis markdown-body" v-html="renderedAnalysis"></div>
      </el-card>
    </template>
    <el-empty v-else-if="hasQueried" description="查询成功，但没有返回数据" />
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { queryApi } from '../api'

const activeTab = ref('natural')
const naturalQuery = ref('')
const sql = ref('')
const loading = ref(false)
const analyzing = ref(false)
const hasQueried = ref(false)
const result = ref({ result: [] })
const history = ref([])
const analysis = ref('')
const chartType = ref('bar')
const chartElement = ref()
let chart

const rows = computed(() => Array.isArray(result.value?.result) ? result.value.result : [])
const columns = computed(() => rows.value.length ? Object.keys(rows.value[0]) : [])
const categoryColumn = computed(() => columns.value.find(c => rows.value.some(r => typeof r[c] === 'string')) || columns.value[0])
const numericColumn = computed(() => columns.value.find(c => rows.value.some(r => typeof r[c] === 'number' || (!Number.isNaN(Number(r[c])) && r[c] !== ''))))
const chartConfig = computed(() => rows.value.length > 0 && categoryColumn.value && numericColumn.value && categoryColumn.value !== numericColumn.value)
const chartDescription = computed(() => `${categoryColumn.value || ''} 与 ${numericColumn.value || ''} 的关系（最多展示 30 条）`)
marked.setOptions({ gfm: true, breaks: true })
const renderedAnalysis = computed(() => DOMPurify.sanitize(marked.parse(analysis.value || '')))

const userId = () => JSON.parse(localStorage.getItem('user') || '{}').id || 1
const acceptResult = async (response) => {
  if (response.code !== 200) throw new Error(response.message || '查询失败')
  result.value = response.data || { result: [] }
  analysis.value = ''
  hasQueried.value = true
  await loadHistory()
  await nextTick()
  renderChart()
}
const runNaturalQuery = async () => {
  if (!naturalQuery.value.trim()) return ElMessage.warning('请输入查询问题')
  loading.value = true
  try { await acceptResult(await queryApi.naturalLanguageQuery({ naturalLanguage: naturalQuery.value.trim(), userId: userId() })); ElMessage.success('查询成功') }
  catch (e) { ElMessage.error(e.response?.data?.message || e.message || '查询失败') }
  finally { loading.value = false }
}
const runSqlQuery = async () => {
  if (!sql.value.trim()) return ElMessage.warning('请输入 SQL')
  loading.value = true
  try { await acceptResult(await queryApi.sqlQuery({ sql: sql.value.trim(), userId: userId() })); ElMessage.success('查询成功') }
  catch (e) { ElMessage.error(e.response?.data?.message || e.message || '查询失败') }
  finally { loading.value = false }
}
const analyze = async () => {
  analyzing.value = true
  try {
    const response = await queryApi.analyzeResult({ question: naturalQuery.value || sql.value || '分析查询结果', result: rows.value.slice(0, 100) })
    if (response.code !== 200) throw new Error(response.message)
    analysis.value = response.data.analysis
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message || '分析失败') }
  finally { analyzing.value = false }
}
const renderChart = () => {
  if (!chartConfig.value || !chartElement.value) return
  chart ||= echarts.init(chartElement.value)
  const data = rows.value.slice(0, 30)
  const labels = data.map(row => String(row[categoryColumn.value] ?? ''))
  const values = data.map(row => Number(row[numericColumn.value]) || 0)
  const series = chartType.value === 'pie'
    ? [{ type: 'pie', radius: ['35%', '70%'], data: labels.map((name, i) => ({ name, value: values[i] })) }]
    : [{ type: chartType.value, data: values, smooth: chartType.value === 'line', itemStyle: { color: '#409eff' } }]
  chart.setOption({ tooltip: { trigger: chartType.value === 'pie' ? 'item' : 'axis' }, legend: chartType.value === 'pie' ? { bottom: 0 } : undefined, grid: { left: 55, right: 25, top: 25, bottom: 70 }, xAxis: chartType.value === 'pie' ? undefined : { type: 'category', data: labels, axisLabel: { rotate: labels.length > 8 ? 35 : 0 } }, yAxis: chartType.value === 'pie' ? undefined : { type: 'value' }, series }, true)
}
watch(chartType, () => nextTick(renderChart))
watch(rows, () => nextTick(renderChart))

const csvCell = value => `"${String(value ?? '').replaceAll('"', '""')}"`
const download = (content, type, extension) => {
  const url = URL.createObjectURL(new Blob([content], { type }))
  const link = document.createElement('a'); link.href = url; link.download = `query-result-${Date.now()}.${extension}`; link.click(); URL.revokeObjectURL(url)
}
const exportCsv = () => {
  const content = '\ufeff' + [columns.value.map(csvCell).join(','), ...rows.value.map(row => columns.value.map(c => csvCell(row[c])).join(','))].join('\r\n')
  download(content, 'text/csv;charset=utf-8', 'csv'); ElMessage.success('CSV 已导出')
}
const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]))
const exportExcel = () => {
  const table = `<html><head><meta charset="UTF-8"></head><body><table border="1"><tr>${columns.value.map(c => `<th>${escapeHtml(c)}</th>`).join('')}</tr>${rows.value.map(row => `<tr>${columns.value.map(c => `<td>${escapeHtml(row[c])}</td>`).join('')}</tr>`).join('')}</table></body></html>`
  download('\ufeff' + table, 'application/vnd.ms-excel;charset=utf-8', 'xls'); ElMessage.success('Excel 已导出')
}
const loadHistory = async () => { try { const r = await queryApi.getHistory(userId()); history.value = r.data || [] } catch (_) {} }
const resize = () => chart?.resize()
onMounted(() => { loadHistory(); window.addEventListener('resize', resize) })
onBeforeUnmount(() => { window.removeEventListener('resize', resize); chart?.dispose() })
</script>

<style scoped>
.query-page { display: grid; gap: 20px; }.header,.result-header { display:flex; align-items:center; justify-content:space-between; gap:16px }.header h2,.result-header h3 { margin:0 }.header span,.result-header small { color:#909399 }.action { margin-top:16px }.result-card { margin-top:20px }.sql-box { display:grid; gap:8px; margin-bottom:16px; padding:14px; border-radius:10px; background:#f5f7fa }.sql-box code { white-space:pre-wrap; color:#337ecc }.chart { width:100%; height:420px }.analysis { line-height:1.85; color:#34495e }.analysis-card { border-left:4px solid #409eff }
.markdown-body :deep(h1),.markdown-body :deep(h2),.markdown-body :deep(h3),.markdown-body :deep(h4) { margin:1.25em 0 .55em; color:#183b5b; line-height:1.35 }.markdown-body :deep(h1) { font-size:1.65rem }.markdown-body :deep(h2) { padding-bottom:.35em; border-bottom:1px solid #e5edf5; font-size:1.4rem }.markdown-body :deep(h3) { font-size:1.18rem }.markdown-body :deep(p) { margin:.65em 0 }.markdown-body :deep(ul),.markdown-body :deep(ol) { margin:.65em 0; padding-left:1.8em }.markdown-body :deep(li) { margin:.3em 0 }.markdown-body :deep(strong) { color:#1d5f9b }.markdown-body :deep(blockquote) { margin:1em 0; padding:.7em 1em; color:#5c7083; border-left:4px solid #70b8f4; border-radius:0 8px 8px 0; background:#f2f8fd }.markdown-body :deep(code) { padding:.18em .4em; color:#d14; border-radius:5px; background:#f1f4f7; font-family:"SFMono-Regular",Consolas,monospace }.markdown-body :deep(pre) { overflow:auto; margin:1em 0; padding:16px; border-radius:10px; background:#182433 }.markdown-body :deep(pre code) { padding:0; color:#e8f1f8; background:transparent }.markdown-body :deep(table) { width:100%; margin:1em 0; border-collapse:collapse }.markdown-body :deep(th),.markdown-body :deep(td) { padding:9px 12px; border:1px solid #dce6ef; text-align:left }.markdown-body :deep(th) { color:#254765; background:#edf6fd }.markdown-body :deep(a) { color:#337ecc; text-decoration:none }.markdown-body :deep(hr) { margin:1.4em 0; border:0; border-top:1px solid #dce6ef }
@media (max-width:700px) { .header,.result-header { align-items:flex-start; flex-direction:column }.chart { height:320px } }
</style>
