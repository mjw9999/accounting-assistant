<template>
  <div>
    <h2 class="page-title">理财记录管理</h2>
    <p class="muted" style="margin: -10px 0 16px">本模块用于辅助登记个人理财投入、持有与赎回情况，系统将根据产品信息估算收益，不涉及真实资金交易。</p>
    <div class="stats-grid">
      <div class="stat"><div class="stat-label">持有金额</div><div class="stat-value">￥{{ money(stats.holdingAmount) }}</div></div>
      <div class="stat"><div class="stat-label">赎回收益</div><div class="stat-value">￥{{ money(stats.redeemedIncome) }}</div></div>
      <div class="stat"><div class="stat-label">产品类型数</div><div class="stat-value">{{ Object.keys(stats.amountByType || {}).length }}</div></div>
      <div class="stat"><div class="stat-label">记录状态</div><div class="stat-value">正常</div></div>
    </div>
    <section class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" clearable placeholder="产品/类型/理财人" style="width: 220px" @keyup.enter="load" />
        <el-select v-model="query.status" clearable placeholder="状态" style="width: 130px">
          <el-option label="持有中" value="HOLDING" />
          <el-option label="已赎回" value="REDEEMED" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <el-button v-if="!isAdmin" type="success" :icon="Plus" @click="open()">新增理财记录</el-button>
        <el-button :icon="Printer" @click="openPrintDialog">打印理财记录</el-button>
        <el-button type="danger" :icon="Delete" :disabled="selectedRows.length === 0" @click="batchRemove">批量删除</el-button>
      </div>
      <el-table ref="tableRef" :data="rows" border @selection-change="handleSelectionChange">
        <el-table-column width="50" align="center">
          <template #header>
            <el-checkbox :model-value="isAllChecked" :indeterminate="isIndeterminate" @change="toggleAll" />
          </template>
          <template #default="{ row }">
            <el-checkbox v-model="row._checked" @change="onRowSelect" />
          </template>
        </el-table-column>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="productCode" label="理财编号" width="160" />
        <el-table-column prop="productName" label="名称" />
        <el-table-column prop="productType" label="类型" width="100" />
        <el-table-column label="年收益率" width="100"><template #default="{ row }">{{ row.annualRate }}%</template></el-table-column>
        <el-table-column label="投入金额" width="120"><template #default="{ row }">￥{{ money(row.amount) }}</template></el-table-column>
        <el-table-column label="预估收益" width="120"><template #default="{ row }">￥{{ money(row.expectedIncome) }}</template></el-table-column>
        <el-table-column label="赎回收益" width="120"><template #default="{ row }"><span v-if="row.actualIncome != null" style="color: #f56c6c">￥{{ money(row.actualIncome) }}</span><span v-else>-</span></template></el-table-column>
        <el-table-column prop="investorName" label="理财人" width="110" />
        <el-table-column label="理财天数" width="100">
          <template #default="{ row }">
            {{ row.status === 'HOLDING' ? getHeldDays(row.startDate) : row.actualDays }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 'HOLDING' ? 'success' : 'info'">{{ row.status === 'HOLDING' ? '持有中' : '已赎回' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" :disabled="row.status !== 'HOLDING'" @click="open(row)">编辑</el-button>
            <el-button size="small" type="warning" :disabled="row.status !== 'HOLDING'" @click="redeem(row)">登记赎回</el-button>
            <el-button v-if="row.status === 'HOLDING'" size="small" type="danger" :icon="Delete" @click="remove(row.id)">删除</el-button>
            <el-button v-else size="small" type="info" disabled>不可删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px; display: flex; justify-content: flex-end">
        <el-pagination v-model:current-page="query.page" background layout="total, prev, pager, next" :total="rowsTotal" :page-size="query.size" @current-change="load" />
      </div>
    </section>

    <section class="panel">
      <h3 style="margin: 0 0 16px">赎回记录查询</h3>
      <el-table :data="redemptions" border>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="productName" label="名称" />
        <el-table-column label="投入金额" width="120"><template #default="{ row }">￥{{ money(row.amount) }}</template></el-table-column>
        <el-table-column label="收益" width="120"><template #default="{ row }"><span style="color: #f56c6c">￥{{ money(row.income) }}</span></template></el-table-column>
        <el-table-column prop="actualDays" label="实际天数" width="110" />
        <el-table-column prop="redeemerName" label="赎回人" width="120" />
        <el-table-column prop="redeemDate" label="赎回日期" width="130" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
      <div style="margin-top: 14px; display: flex; justify-content: flex-end">
        <el-pagination v-model:current-page="redemptionQuery.page" background layout="total, prev, pager, next" :total="redemptionsTotal" :page-size="redemptionQuery.size" @current-change="loadRedemptions" />
      </div>
    </section>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '修改理财记录' : '新增理财记录'" width="580px">
      <el-form :model="dialog.form" label-width="108px">
        <el-alert title="本操作仅用于建立理财记录，不代表真实申购金融产品。" type="info" :closable="false" show-icon style="margin-bottom: 16px" />
        <el-form-item>
          <span>当前可登记理财金额：¥{{ money(investableBalance) }}</span>
          <div class="muted" style="font-size: 12px; margin-top: 4px">该金额依据个人收支结余计算，用于限制理财记录登记。</div>
        </el-form-item>
        <el-form-item label="理财产品">
          <el-select v-model="dialog.form.productId" filterable style="width: 100%" @change="handleProductChange">
            <el-option v-for="item in products" :key="item.id" :label="`${item.name}（${item.type}，${item.annualRate}%）`" :value="item.id" />
          </el-select>
        </el-form-item>

        <section v-if="selectedProduct" class="product-readonly-panel">
          <div class="section-title">产品信息</div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="理财编号">{{ selectedProduct.productCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="产品名称">{{ selectedProduct.name }}</el-descriptions-item>
            <el-descriptions-item label="产品类型">{{ selectedProduct.type }}</el-descriptions-item>
            <el-descriptions-item label="年收益率">{{ selectedProduct.annualRate }}%</el-descriptions-item>
            <el-descriptions-item label="理财期限">{{ selectedProduct.termDays || 30 }} 天</el-descriptions-item>
            <el-descriptions-item label="最低投入金额">¥{{ money(selectedProduct.minAmount) }}</el-descriptions-item>
            <el-descriptions-item label="最高投入金额">¥{{ money(selectedProduct.maxAmount) }}</el-descriptions-item>
          </el-descriptions>
        </section>

        <div class="section-title">理财记录信息</div>
        <el-form-item label="理财人">
          <el-input :model-value="investorDisplay" disabled />
        </el-form-item>
        <el-form-item label="投入金额">
          <el-input-number v-model="dialog.form.amount" :min="0.01" :max="maxInvestableAmount || undefined" :precision="2" style="width: 100%" />
          <div class="amount-hint">当前最多可登记理财金额 ¥{{ money(maxInvestableAmount) }}</div>
          <div v-if="amountBelowMin" class="amount-warning">金额不能低于产品最低额度 ¥{{ money(selectedProduct?.minAmount) }}</div>
          <div v-else-if="amountAboveProductMax" class="amount-warning">金额不能高于产品最高额度 ¥{{ money(selectedProduct?.maxAmount) }}</div>
          <div v-else-if="amountExceedsBalance" class="amount-warning">登记金额超过当前可登记理财金额</div>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="dialog.form.startDate" value-format="YYYY-MM-DD" style="width: 100%" @change="handleStartDateChange" />
        </el-form-item>
        <el-form-item label="预计赎回">
          <el-date-picker v-model="dialog.form.expectedRedeemDate" value-format="YYYY-MM-DD" style="width: 100%" />
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
          <el-radio value="all">当前筛选结果全部理财记录 (共 {{ rowsTotal }} 条)</el-radio>
          <el-radio value="page">仅打印当前页理财记录 (共 {{ rows.length }} 条)</el-radio>
          <el-radio value="selection" :disabled="selectedRows.length === 0">
            打印已勾选理财记录 (已选 {{ selectedRows.length }} 条)
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Printer, Search } from '@element-plus/icons-vue'
import { createInvestment, deleteInvestment, financeSummary, investmentStats, listInvestments, listProducts, listRedemptions, redeemInvestment, updateInvestment } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'ADMIN')
const tableRef = ref()
const query = reactive({ page: 1, size: 10, keyword: '', status: '' })
const rows = ref([])
const rowsTotal = ref(0)
const redemptions = ref([])
const redemptionsTotal = ref(0)
const redemptionQuery = reactive({ page: 1, size: 10 })
const products = ref([])
const stats = reactive({ holdingAmount: 0, redeemedIncome: 0, amountByType: {}, incomeByProduct: {} })
const dialog = reactive({ visible: false, form: {} })
const investableBalance = ref(0)

const selectedRows = ref([])
const printDialog = reactive({ visible: false, range: 'all' })

const isAllChecked = computed(() => rows.value.length > 0 && rows.value.every(r => r._checked))
const isIndeterminate = computed(() => {
  const checkedCount = rows.value.filter(r => r._checked).length
  return checkedCount > 0 && checkedCount < rows.value.length
})

const selectedProduct = computed(() => products.value.find(item => item.id === dialog.form.productId) || null)
const investorDisplay = computed(() => dialog.form.investorName || auth.user?.displayName || auth.user?.username || '-')
const maxInvestableAmount = computed(() => {
  const balance = Number(investableBalance.value || 0)
  const productMax = Number(selectedProduct.value?.maxAmount || 0)
  if (!selectedProduct.value) return balance
  return Math.min(balance, productMax)
})
const amountExceedsBalance = computed(() => Number(dialog.form.amount || 0) > Number(investableBalance.value || 0))
const amountBelowMin = computed(() => {
  const minAmount = Number(selectedProduct.value?.minAmount || 0)
  return minAmount > 0 && Number(dialog.form.amount || 0) < minAmount
})
const amountAboveProductMax = computed(() => {
  const maxAmount = Number(selectedProduct.value?.maxAmount || 0)
  return maxAmount > 0 && Number(dialog.form.amount || 0) > maxAmount
})

function addDays(dateStr, days) {
  const date = new Date(`${dateStr}T00:00:00`)
  date.setDate(date.getDate() + days)
  return date.toISOString().slice(0, 10)
}

function getHeldDays(startDate) {
  if (!startDate) return 0
  const start = new Date(`${startDate}T00:00:00`)
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  const diff = Math.floor((now - start) / (1000 * 60 * 60 * 24))
  return Math.max(0, diff)
}

function applyDefaultRedeemDate() {
  if (!selectedProduct.value || !dialog.form.startDate) return
  dialog.form.expectedRedeemDate = addDays(dialog.form.startDate, Number(selectedProduct.value.termDays || 30))
}

function handleProductChange() { applyDefaultRedeemDate() }
function handleStartDateChange() { applyDefaultRedeemDate() }

function money(value) {
  return Number(value || 0).toFixed(2)
}

async function load() {
  const [listRes, statRes] = await Promise.all([
    listInvestments({ ...query, page: query.page - 1 }),
    investmentStats()
  ])
  rows.value = (listRes.data?.records || []).map(r => ({ ...r, _checked: false }))
  rowsTotal.value = listRes.data?.total || 0
  Object.assign(stats, statRes.data || {})
  selectedRows.value = []
  loadRedemptions()
}

async function loadRedemptions() {
  const res = await listRedemptions({ ...redemptionQuery, page: redemptionQuery.page - 1 })
  redemptions.value = res.data?.records || []
  redemptionsTotal.value = res.data?.total || 0
}
async function loadProducts() {
  const res = await listProducts({ size: 100, status: 'ENABLED' })
  products.value = res.data?.records || []
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
    const res = await listInvestments({ ...query, size: 99999 })
    printData = res.data?.records || []
    rangeLabel = '当前筛选全部记录'
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
  const statusLabel = query.status === 'HOLDING' ? '持有中' : query.status === 'REDEEMED' ? '已赎回' : '全部'
  const keyword = query.keyword || '无'

  const rows_html = dataList.map((row, i) => `
    <tr>
      <td>${i + 1}</td>
      <td>${row.productCode || '-'}</td>
      <td style="text-align:left">${row.productName || '-'}</td>
      <td>${row.productType || '-'}</td>
      <td>${row.annualRate != null ? row.annualRate + '%' : '-'}</td>
      <td style="text-align:right">￥${money(row.amount)}</td>
      <td style="text-align:right">￥${money(row.expectedIncome)}</td>
      <td style="text-align:right">${row.actualIncome != null ? '￥' + money(row.actualIncome) : '-'}</td>
      <td>${row.investorName || '-'}</td>
      <td>${row.status === 'HOLDING' ? getHeldDays(row.startDate) : row.actualDays}</td>
      <td>${row.status === 'HOLDING' ? '持有中' : '已赎回'}</td>
    </tr>`).join('')

  const totalAmount = money(dataList.reduce((s, r) => s + Number(r.amount || 0), 0))
  const totalExpected = money(dataList.reduce((s, r) => s + Number(r.expectedIncome || 0), 0))
  const totalActual = money(dataList.filter(r => r.actualIncome != null).reduce((s, r) => s + Number(r.actualIncome || 0), 0))
  const actualCount = dataList.filter(r => r.actualIncome != null).length

  const html = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>理财记录打印表</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #fff; color: #000; font-size: 12px; }
    .page { max-width: 1100px; margin: 0 auto; padding: 30px 36px; }
    .sys-name { text-align: center; font-size: 11px; color: #666; margin-bottom: 4px; }
    h2 { text-align: center; font-size: 18px; font-weight: bold; letter-spacing: 2px; margin-bottom: 16px; }
    .meta { display: flex; justify-content: space-between; font-size: 12px; color: #333; margin-bottom: 6px; }
    .filter-line { font-size: 12px; color: #444; margin-bottom: 10px; padding: 8px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 4px; }
    hr.thick { border: none; border-top: 2px solid #000; margin-bottom: 2px; }
    hr.thin  { border: none; border-top: 1px solid #999; margin-bottom: 14px; }
    table { width: 100%; border-collapse: collapse; font-size: 11.5px; }
    th { background: #f1f5f9; font-weight: bold; padding: 8px 6px; border: 1px solid #333; text-align: center; }
    td { padding: 6px 6px; border: 1px solid #666; text-align: center; }
    tfoot tr td { background: #f8fafc; font-weight: bold; border: 1px solid #333; }
    .sign-area { margin-top: 40px; display: flex; justify-content: space-between; font-size: 12px; }
    .sign-item { display: flex; flex-direction: column; align-items: center; gap: 30px; }
    .sign-item .line { width: 140px; border-top: 1px solid #000; }
    @media print { .page { padding: 10px 10px; } .filter-line { background: #fff !important; } }
  </style>
</head>
<body>
<div class="page">
  <div class="sys-name">个人记账助手系统</div>
  <h2>理财投入与持有明细打印表</h2>
  <div class="meta">
    <span>打印范围：${rangeLabel}</span>
    <span>操作人：${user}</span>
    <span>打印时间：${now}</span>
  </div>
  <div class="filter-line">
    <strong>筛选摘要：</strong>
    状态【${statusLabel}】 | 关键词【${keyword}】 | 总计：${dataList.length} 条记录
  </div>
  <hr class="thick"><hr class="thin">
  <table>
    <thead>
      <tr>
        <th width="40">序号</th>
        <th width="110">理财编号</th>
        <th>产品名称</th>
        <th width="70">类型</th>
        <th width="70">收益率</th>
        <th width="100">投入金额</th>
        <th width="100">预估收益</th>
        <th width="100">赎回收益</th>
        <th width="90">理财人</th>
        <th width="70">理财天数</th>
        <th width="60">状态</th>
      </tr>
    </thead>
    <tbody>${rows_html}</tbody>
    <tfoot>
      <tr>
        <td colspan="5" style="text-align:right">本报表合计</td>
        <td style="text-align:right">￥${totalAmount}</td>
        <td style="text-align:right">￥${totalExpected}</td>
        <td style="text-align:right">￥${totalActual} (${actualCount}笔)</td>
        <td colspan="3"></td>
      </tr>
    </tfoot>
  </table>
  <div class="sign-area">
    <div class="sign-item"><div class="line"></div><span>制表人签名</span></div>
    <div class="sign-item"><div class="line"></div><span>理财审核</span></div>
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

async function open(row) {
  await loadProducts()
  const scope = isAdmin.value && row ? { userId: row.investorId } : {}
  const [financeRes, statRes] = await Promise.all([financeSummary(scope), investmentStats(scope)])
  const financeBalance = Number(financeRes.data?.balance || 0)
  const holdingAmount = Number(statRes.data?.holdingAmount || 0)
  const releasedAmount = row && row.status === 'HOLDING' ? Number(row.amount || 0) : 0
  investableBalance.value = Math.max(0, financeBalance - holdingAmount + releasedAmount)
  dialog.form = row ? { ...row } : { productId: products.value[0]?.id, amount: 1000, startDate: new Date().toISOString().slice(0, 10), expectedRedeemDate: '' }
  if (!row) applyDefaultRedeemDate()
  dialog.visible = true
}
async function save() {
  if (dialog.form.expectedRedeemDate && dialog.form.startDate && dialog.form.expectedRedeemDate < dialog.form.startDate) {
     return ElMessage.warning('预计赎回日期不能早于投资开始日期')
  }
  if (dialog.form.id) await updateInvestment(dialog.form.id, dialog.form)
  else await createInvestment(dialog.form)
  ElMessage.success('保存成功')
  dialog.visible = false
  load()
}
async function redeem(row) {
  const today = new Date().toISOString().slice(0, 10)
  if (row.startDate === today) {
    try {
      await ElMessageBox.confirm(
        '您当前的理财持有不足 24 小时（当天赎回），赎回收益将计算为 0 元。确认要登记赎回本金并释放额度吗？',
        '当天赎回警告',
        {
          type: 'warning',
          confirmButtonText: '确认赎回',
          cancelButtonText: '取消',
          confirmButtonClass: 'el-button--warning'
        }
      )
    } catch (e) {
      return // 用户取消了操作
    }
  }

  const { value } = await ElMessageBox.prompt('赎回后将生成赎回记录，并将赎回收益与本金自动计入收支明细。可在此填写备注：', '登记赎回确认', { 
    inputPlaceholder: '赎回备注', 
    confirmButtonText: '确认登记', 
    cancelButtonText: '取消' 
  })
  await redeemInvestment(row.id, { remark: value })
  ElMessage.success('赎回登记成功')
  load()
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确认删除该理财记录？此操作将永久删除数据，不可恢复。', '系统警告', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger'
    })
    await deleteInvestment(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {}
}

async function batchRemove() {
  const deletable = selectedRows.value.filter(r => r.status !== 'REDEEMED')
  if (deletable.length === 0) {
    return ElMessage.warning('选中的记录中没有可删除的项目（已赎回项目不可删除）')
  }
  try {
    await ElMessageBox.confirm(
      `现已选中 ${selectedRows.value.length} 条记录，其中包含 ${deletable.length} 条可删除记录。\n注意：此操作将永久删除数据且不可恢复！`,
      '批量删除安全警告',
      { type: 'error', confirmButtonText: '确定批量删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }
    )
    for (const row of deletable) {
      await deleteInvestment(row.id)
    }
    ElMessage.success(`成功删除 ${deletable.length} 条记录`)
    load()
  } catch (e) {}
}
onMounted(load)
</script>

<style scoped>
.section-title {
  margin: 16px 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}
.product-readonly-panel {
  margin-bottom: 8px;
}
.amount-hint {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}
.amount-warning {
  margin-top: 8px;
  color: #f56c6c;
  font-size: 12px;
  line-height: 1.4;
}
</style>
