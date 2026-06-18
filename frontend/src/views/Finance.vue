<template>
  <div>
    <h2 class="page-title">财务管理</h2>
    <p v-if="isAdmin" class="muted" style="margin: -8px 0 16px">全站用户财务记录管理</p>
    <div class="stats-grid">
      <div v-for="item in statItems" :key="item.key" class="stat" :class="item.tone ? `stat--${item.tone}` : ''">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">￥{{ money(summary[item.key]) }}</div>
      </div>
    </div>
    <section class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" clearable placeholder="分类/备注/添加人" style="width: 220px" @keyup.enter="load" />
        <el-select v-model="query.type" clearable placeholder="类型" style="width: 120px">
          <el-option label="收入" value="INCOME" />
          <el-option label="支出" value="EXPENSE" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <el-button v-if="!isAdmin" type="success" :icon="Plus" @click="open()">添加记账</el-button>
        <el-upload :show-file-list="false" :http-request="uploadCsv">
          <el-button :icon="Upload">导入CSV</el-button>
        </el-upload>
        <el-button :icon="Printer" @click="openPrintDialog">打印记录</el-button>
        <el-button type="danger" :icon="Delete" :disabled="selectedRows.length === 0" @click="batchRemove">批量删除</el-button>
      </div>
      <el-table ref="tableRef" :data="rows" @selection-change="handleSelectionChange">
        <el-table-column width="50" align="center">
          <template #header>
            <span style="color: var(--primary-color); cursor: pointer; font-weight: bold; font-size: 13px" @click="toggleAll">全选</span>
          </template>
          <template #default="{ row }">
            <el-checkbox v-model="row._checked" @change="onRowSelect" />
          </template>
        </el-table-column>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INCOME' ? 'success' : 'warning'">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="isAutoRecord(row)" size="small" type="info" effect="plain">理财自动入账</el-tag>
            <el-tag v-else size="small" type="success" effect="plain">手动记账</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" />
        <el-table-column label="金额" width="120"><template #default="{ row }">￥{{ money(row.amount) }}</template></el-table-column>
        <el-table-column prop="recordDate" label="日期" width="130" />
        <el-table-column prop="createdBy" label="添加人" width="120" />
        <el-table-column label="备注">
          <template #default="{ row }">
            {{ formatRemark(row.remark) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <template v-if="isAutoRecord(row)">
              <el-tooltip content="该记录由理财赎回自动生成，不支持手动修改或删除" placement="top">
                <span style="display: inline-block; cursor: not-allowed">
                  <el-button size="small" disabled :icon="Edit">编辑</el-button>
                  <el-button size="small" type="danger" disabled :icon="Delete">删除</el-button>
                </span>
              </el-tooltip>
            </template>
            <template v-else>
              <el-button size="small" :icon="Edit" @click="open(row)">编辑</el-button>
              <el-button size="small" type="danger" :icon="Delete" @click="remove(row.id)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="load" />
      </div>
    </section>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '修改记账' : '添加记账'" width="500px">
      <el-form :model="dialog.form" label-width="86px">
        <el-form-item label="类型">
          <el-radio-group v-model="dialog.form.type" @change="loadCategories">
            <el-radio value="EXPENSE">支出</el-radio>
            <el-radio value="INCOME">收入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="dialog.form.categoryId" placeholder="选择分类" @change="onCategoryChange">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="dialog.form.amount" :precision="2" :step="10" :min="0.01" style="width: 100%" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="dialog.form.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dialog.form.remark" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="printDialog.visible" title="打印范围选择" width="400px">
      <div style="padding: 10px 0">
        <el-radio-group v-model="printDialog.range" style="display: flex; flex-direction: column; gap: 15px">
          <el-radio value="all">当前筛选结果全部记录 (共 {{ total }} 条)</el-radio>
          <el-radio value="page">仅打印当前页记录 (共 {{ rows.length }} 条)</el-radio>
          <el-radio value="selection" :disabled="selectedRows.length === 0">
            打印已勾选记录 (已选 {{ selectedRows.length }} 条)
          </el-radio>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button @click="printDialog.visible = false">取消</el-button>
        <el-button type="primary" :icon="Printer" @click="executePrint">确认打印</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Printer, Search, Upload } from '@element-plus/icons-vue'
import { createFinance, deleteFinance, financeSummary, importFinanceCsv, listCategories, listFinance, updateFinance } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'ADMIN')
const tableRef = ref()

const query = reactive({ page: 1, size: 10, keyword: '', type: '' })
const dateRange = ref([])
const rows = ref([])
const total = ref(0)
const summary = reactive({ income: 0, expense: 0, balance: 0, monthBalance: 0 })
const categories = ref([])
const dialog = reactive({ visible: false, form: {} })

const selectedRows = ref([])
const printDialog = reactive({ visible: false, range: 'all' })

const statItems = [
  { label: '总收入', key: 'income' },
  { label: '总支出', key: 'expense' },
  { label: '当前结余', key: 'balance' },
  { label: '本月结余', key: 'monthBalance' }
]

function money(value) {
  return Number(value || 0).toFixed(2)
}
function formatRemark(remark) {
  if (!remark) return '-'
  return remark.replace(/#INVESTMENT_ID=\d+#/g, '')
}
function typeText(type) {
  return type === 'INCOME' ? '收入' : '支出'
}
function isAutoRecord(row) {
  return row.recordSource === 'AUTO_REDEMPTION' || 
         row.recordSource === 'AUTO_INVESTMENT' || 
         (row.remark && (row.remark.includes('理财') && (row.remark.includes('自动') || row.remark.includes('自动入账') || row.remark.includes('自动划出') || row.remark.includes('自动收回'))))
}

function params() {
  const p = { ...query }
  p.page = Math.max(0, query.page - 1)
  if (dateRange.value?.length === 2) {
    p.startDate = dateRange.value[0]
    p.endDate = dateRange.value[1]
  }
  return p
}

async function load() {
  const [listRes, summaryRes] = await Promise.all([listFinance(params()), financeSummary(params())])
  rows.value = (listRes.data?.records || []).map(r => ({ ...r, _checked: false }))
  total.value = listRes.data?.total || 0
  Object.assign(summary, summaryRes.data || {})
  selectedRows.value = []
}

function handleSelectionChange(val) {
  selectedRows.value = val
}

function onRowSelect() {
  selectedRows.value = rows.value.filter(r => r._checked)
}

function toggleAll() {
  const isAllChecked = rows.value.length > 0 && rows.value.every(r => r._checked)
  rows.value.forEach(r => r._checked = !isAllChecked)
  onRowSelect()
}

function openPrintDialog() {
  printDialog.range = 'all'
  printDialog.visible = true
}

async function executePrint() {
  let printData = []
  let rangeLabel = ''

  if (printDialog.range === 'all') {
    const res = await listFinance({ ...params(), size: 99999 })
    printData = res.data?.records || []
    rangeLabel = '当前筛选全部结果'
  } else if (printDialog.range === 'page') {
    printData = [...rows.value]
    rangeLabel = '当前页记录'
  } else {
    printData = [...selectedRows.value]
    rangeLabel = '已勾选记录'
  }

  if (printData.length === 0) {
    ElMessage.warning('没有可打印的数据')
    return
  }

  printDialog.visible = false
  doPrint(printData, rangeLabel)
}

function doPrint(dataList, rangeLabel) {
  const now = new Date().toLocaleString('zh-CN')
  const user = auth.user?.displayName || auth.user?.username || '未知用户'
  const typeLabel = query.type === 'INCOME' ? '收入' : query.type === 'EXPENSE' ? '支出' : '全部'
  const keyword = query.keyword || '无'
  const startDate = dateRange.value?.[0] || '不限'
  const endDate = dateRange.value?.[1] || '不限'

  const rows_html = dataList.map((row, i) => `
    <tr>
      <td>${i + 1}</td>
      <td>${row.type === 'INCOME' ? '收入' : '支出'}</td>
      <td>${row.categoryName || '-'}</td>
      <td style="text-align:right">￥${money(row.amount)}</td>
      <td>${row.recordDate || '-'}</td>
      <td>${row.createdBy || '-'}</td>
      <td style="text-align:left">${formatRemark(row.remark)}</td>
    </tr>`).join('')

  const incomeTotal = money(dataList.filter(r => r.type === 'INCOME').reduce((s, r) => s + Number(r.amount || 0), 0))
  const expenseTotal = money(dataList.filter(r => r.type === 'EXPENSE').reduce((s, r) => s + Number(r.amount || 0), 0))
  const amountTotal = money(dataList.reduce((s, r) => s + Number(r.amount || 0), 0))

  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>财务记录打印表</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #fff; color: #000; font-size: 13px; }
    .page { max-width: 860px; margin: 0 auto; padding: 30px 36px; }
    .sys-name { text-align: center; font-size: 12px; color: #666; margin-bottom: 4px; }
    h2 { text-align: center; font-size: 18px; font-weight: bold; letter-spacing: 2px; margin-bottom: 16px; }
    .meta { display: flex; justify-content: space-between; font-size: 12px; color: #333; margin-bottom: 6px; }
    .filter-line { font-size: 12px; color: #444; margin-bottom: 10px; padding: 8px; background: #f8fafc; border-radius: 4px; border: 1px solid #e2e8f0; }
    hr.thick { border: none; border-top: 2px solid #000; margin-bottom: 2px; }
    hr.thin  { border: none; border-top: 1px solid #999; margin-bottom: 14px; }
    table { width: 100%; border-collapse: collapse; font-size: 12.5px; table-layout: auto; }
    th { background: #f1f5f9; font-weight: bold; padding: 8px 10px; border: 1px solid #333; text-align: center; }
    td { padding: 7px 10px; border: 1px solid #666; text-align: center; }
    tfoot tr td { background: #f8fafc; font-weight: bold; border: 1px solid #333; }
    .sign-area { margin-top: 40px; display: flex; justify-content: space-between; font-size: 12px; }
    .sign-item { display: flex; flex-direction: column; align-items: center; gap: 30px; }
    .sign-item .line { width: 140px; border-top: 1px solid #000; }
    @media print { .page { padding: 10px 15px; } .filter-line { background: #fff !important; } }
  </style>
</head>
<body>
<div class="page">
  <div class="sys-name">个人记账助手系统</div>
  <h2>财务收支记录统计表</h2>
  <div class="meta">
    <span>打印范围：${rangeLabel}</span>
    <span>操作人：${user}</span>
    <span>打印时间：${now}</span>
  </div>
  <div class="filter-line">
    <strong>筛选摘要：</strong>
    类型【${typeLabel}】 | 关键词【${keyword}】 | 日期【${startDate} 至 ${endDate}】
  </div>
  <hr class="thick"><hr class="thin">
  <table>
    <thead>
      <tr>
        <th width="50">序号</th>
        <th width="80">类型</th>
        <th>分类名称</th>
        <th width="120">金额</th>
        <th width="120">记账日期</th>
        <th width="100">添加人</th>
        <th>备注</th>
      </tr>
    </thead>
    <tbody>${rows_html}</tbody>
    <tfoot>
      <tr>
        <td colspan="3" style="text-align:right">本报表合计 (共 ${dataList.length} 条)</td>
        <td style="text-align:right">￥${amountTotal}</td>
        <td colspan="3" style="text-align:left;font-weight:normal;font-size:11px">
          收入合计：￥${incomeTotal}　　支出合计：￥${expenseTotal}
        </td>
      </tr>
    </tfoot>
  </table>
  <div class="sign-area">
    <div class="sign-item"><div class="line"></div><span>制表人签名</span></div>
    <div class="sign-item"><div class="line"></div><span>财务审核</span></div>
    <div class="sign-item"><div class="line"></div><span>日期</span></div>
  </div>
</div>
</body>
</html>`

  const win = window.open('', '_blank')
  win.document.write(html)
  win.document.close()
  win.focus()
  setTimeout(() => {
    win.print()
    win.close()
  }, 500)
}

async function loadCategories() {
  const res = await listCategories({ type: dialog.form.type, status: 'ENABLED' })
  categories.value = res.data || []
}
function onCategoryChange(id) {
  const cat = categories.value.find(c => c.id === id)
  if (cat) dialog.form.categoryName = cat.name
}
async function open(row) {
  dialog.form = row ? { ...row } : { type: 'EXPENSE', categoryName: '', amount: 0.01, recordDate: new Date().toISOString().slice(0, 10), remark: '' }
  dialog.visible = true
  await loadCategories()
}
async function save() {
  if (dialog.form.id) await updateFinance(dialog.form.id, dialog.form)
  else await createFinance(dialog.form)
  ElMessage.success('保存成功')
  dialog.visible = false
  load()
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确认删除该记账信息？删除后将无法找回。', '系统警告', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger'
    })
    await deleteFinance(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {}
}

async function batchRemove() {
  const isAuto = (r) => r.recordSource === 'AUTO_REDEMPTION' || (r.remark && r.remark.includes('赎回收益自动入账'))
  const deletable = selectedRows.value.filter(r => !isAuto(r))
  if (deletable.length === 0) {
    return ElMessage.warning('选中的记录中没有可删除的项目（理财自动入账记录不可删除）')
  }
  try {
    await ElMessageBox.confirm(
      `现已选中 ${selectedRows.value.length} 条记录，其中包含 ${deletable.length} 条可删除记录。\n注意：此操作将永久删除数据且不可恢复！`,
      '批量删除安全警告',
      { type: 'error', confirmButtonText: '确定批量删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }
    )
    for (const row of deletable) {
      await deleteFinance(row.id)
    }
    ElMessage.success(`成功删除 ${deletable.length} 条记录`)
    load()
  } catch (e) {}
}
async function uploadCsv({ file }) {
  const res = await importFinanceCsv(file)
  // 双保险设计：若后端由于未重启而继续返回旧版 "导入成功" 文案，前端自动从响应中拼装 count 统计呈现给用户！
  let showMessage = res.message || '导入成功'
  if (showMessage === '导入成功' && res.data && typeof res.data.count === 'number') {
    showMessage = `导入成功！共成功导入 ${res.data.count} 条记录。`
  }
  ElMessage.success(showMessage)
  load()
}

watch(dateRange, load)
onMounted(() => {
  const { categoryName, type } = route.query
  if (categoryName) {
    query.keyword = categoryName
    query.type = type
  }
  load()
})
</script>
