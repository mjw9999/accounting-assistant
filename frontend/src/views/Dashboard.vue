<template>
  <div>
    <h2 class="page-title">{{ isAdmin ? '后台主页' : '系统首页' }}</h2>

    <template v-if="isAdmin">
      <section class="panel admin-share-panel">
        <div class="admin-share-header">
          <h3 style="margin: 0">理财收益分享</h3>
          <div>
            <el-button type="primary" link @click="router.push('/shares')">分享管理</el-button>
          </div>
        </div>
        <el-empty v-if="wallShares.length === 0" description="暂无理财收益分享" :image-size="80" />
        <div v-else class="admin-share-list">
          <div v-for="item in wallShares" :key="item.id" class="admin-share-item" @click="showDetail(item)">
            <div class="admin-share-item-main">
              <div class="admin-share-title">{{ item.title }}</div>
              <p class="admin-share-summary">{{ shareSummary(item) }}</p>
            </div>
            <span class="admin-share-time">{{ formatTime(item.createdAt) }}</span>
          </div>
        </div>
      </section>
    </template>

    <template v-else>
      <div class="stats-grid">
        <div class="stat">
          <div class="stat-label">收入合计</div>
          <div class="stat-value">￥{{ money(summary.income) }}</div>
        </div>
        <div class="stat">
          <div class="stat-label">支出合计</div>
          <div class="stat-value">￥{{ money(summary.expense) }}</div>
        </div>
        <div class="stat">
          <div class="stat-label">本月结余</div>
          <div class="stat-value">￥{{ money(summary.monthBalance) }}</div>
        </div>
        <div class="stat">
          <div class="stat-label">持有理财</div>
          <div class="stat-value">￥{{ money(investment.holdingAmount) }}</div>
        </div>
      </div>
      <div class="chart-grid">
        <section class="panel month-trend-panel">
          <div class="trend-header">
            <div class="trend-header-left">
              <h3 class="trend-title">{{ trendModeText }}趋势统计</h3>
              <div class="trend-meta">
                <div class="trend-meta-item">
                  <span class="trend-meta-label">月度平均{{ trendModeText }}</span>
                  <span class="trend-meta-value">¥{{ money(averageTrendValue) }}</span>
                </div>
                <div class="trend-meta-item" v-if="selectedTrendMonth">
                  <span class="trend-meta-label">{{ selectedTrendMonth.month }} 实际{{ trendModeText }}</span>
                  <span class="trend-meta-value highlight">¥{{ money(selectedTrendMonth[trendMode.toLowerCase()]) }}</span>
                </div>
              </div>
            </div>
            <el-segmented v-model="trendMode" :options="trendModeOptions" size="small" />
          </div>
          <div v-if="monthTrendItems.length > 0" ref="monthRef" class="chart trend-chart"></div>
          <el-empty v-else description="暂无收支趋势数据" :image-size="80" />
        </section>
        <section class="panel category-stats-panel">
          <div class="category-stats-header">
            <h3 style="margin: 0">{{ categoryModeText }}分类统计</h3>
            <el-segmented v-model="categoryMode" :options="categoryModeOptions" size="small" />
          </div>
          <template v-if="currentCategoryItems.length > 0">
            <div ref="categoryRef" class="chart category-chart"></div>
            <div class="category-rank-list">
              <div v-for="(item, index) in topCategoryItems" :key="item.categoryName" class="category-card-item" @click="goToFinance(item)">
                <div class="category-card-top">
                  <div class="category-card-info">
                    <i class="category-card-dot" :style="{ backgroundColor: chartColors[index % chartColors.length] }"></i>
                    <span class="category-card-name">{{ item.categoryName }}</span>
                    <span class="category-card-percent">{{ Number(item.percent).toFixed(2) }}%</span>
                  </div>
                  <div class="category-card-right">
                    <span class="category-card-amount">￥{{ money(item.amount) }}</span>
                    <el-icon class="category-card-arrow"><ArrowRight /></el-icon>
                  </div>
                </div>
                <div class="category-card-bottom">
                  <div class="category-card-progress">
                    <el-progress :percentage="Number(item.percent)" :color="chartColors[index % chartColors.length]" :show-text="false" :stroke-width="6" />
                  </div>
                  <span class="category-card-count">{{ item.count ?? 0 }}笔</span>
                </div>
              </div>
            </div>
          </template>
          <el-empty v-else :description="categoryMode === 'EXPENSE' ? '暂无支出数据' : '暂无收入数据'" :image-size="80" />
        </section>
      </div>
      <div v-show="monthTrendItems.length === 0" style="margin-top: -10px; margin-bottom: 20px;">
        <el-alert title="暂无收支数据，快去记一笔吧！" type="info" show-icon :closable="false" />
      </div>

      <section class="panel share-wall-panel" style="margin-top: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
          <h3 class="section-heading">理财收益分享</h3>
          <div class="share-wall-actions">
            <el-button type="primary" link size="small" @click="router.push('/shares')">发布分享</el-button>
            <el-button
              v-if="wallShares.length > 4"
              type="primary"
              link
              size="small"
              @click="isShareExpanded = !isShareExpanded"
            >
              {{ isShareExpanded ? '收起' : '展开更多' }}
              <el-icon class="share-expand-icon">
                <component :is="isShareExpanded ? ArrowUp : ArrowDown" />
              </el-icon>
            </el-button>
          </div>
        </div>
        <div v-if="wallShares.length === 0">
          <el-empty description="暂无理财收益分享" :image-size="80" />
        </div>
        <el-row :gutter="20" v-else>
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in visibleWallShares" :key="item.id" style="margin-bottom: 20px;">
            <el-card shadow="hover" class="share-card" style="cursor: pointer;" @click="showDetail(item)">
              <template #header>
                <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                  <span style="font-weight: bold; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" :title="item.title">{{ item.title }}</span>
                  <el-tag size="small" type="success" effect="light">展示中</el-tag>
                </div>
              </template>
              <div class="card-body" style="font-size: 14px; color: #666; line-height: 1.6;">
                <p style="margin: 0 0 8px;"><strong>理财产品：</strong>{{ item.productName }}</p>
                <p style="margin: 0 0 8px;"><strong>理财人：</strong>{{ item.investorName }}</p>
                <p style="margin: 0 0 8px;"><strong>投入金额：</strong>￥{{ money(item.amount) }}</p>
                <p style="margin: 0 0 8px;"><strong>理财收益：</strong><span style="color: #f56c6c; font-weight: bold;">￥{{ money(item.income) }}</span></p>
                <p style="margin: 0; padding-top: 8px; border-top: 1px dashed #eee;">
                  <strong>心得内容：</strong>
                  <span style="display: block; margin-top: 4px; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical;" :title="item.content">{{ item.content }}</span>
                </p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </section>
    </template>

    <el-dialog v-model="detail.visible" title="分享详情" width="700px" destroy-on-close>
      <div v-if="detail.share" class="share-detail-content">
        <div class="detail-header" style="margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 15px;">
          <h2 style="margin: 0 0 10px">{{ detail.share.title }}</h2>
          <div style="color: #999; font-size: 13px;">
            <span>发布人：{{ detail.share.userName }}</span>
            <span style="margin-left: 20px">发布时间：{{ formatTime(detail.share.createdAt) }}</span>
          </div>
        </div>
        <div class="detail-info" style="background: #f9f9f9; padding: 15px; border-radius: 4px; margin-bottom: 20px;">
          <el-row :gutter="20">
            <el-col :span="12"><p><strong>理财产品：</strong>{{ detail.share.productName }}</p></el-col>
            <el-col :span="12"><p><strong>投入金额：</strong>￥{{ money(detail.share.amount) }}</p></el-col>
            <el-col :span="12"><p><strong>理财收益：</strong><span style="color: #f56c6c">￥{{ money(detail.share.income) }}</span></p></el-col>
          </el-row>
        </div>
        <div class="detail-body" style="line-height: 1.8; white-space: pre-wrap;">
          {{ detail.share.content }}
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, ArrowRight, ArrowUp } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { financeSummary, investmentStats, listShares } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'ADMIN')

const detail = reactive({ visible: false, share: null })
const summary = reactive({ income: 0, expense: 0, balance: 0, monthBalance: 0, categoryChart: {}, categoryStats: [], monthChart: {}, monthTrend: [] })
const investment = reactive({ holdingAmount: 0, redeemedIncome: 0 })
const wallShares = ref([])
const isShareExpanded = ref(false)
const visibleWallShares = computed(() => isShareExpanded.value ? wallShares.value : wallShares.value.slice(0, 4))
const monthRef = ref()
const categoryRef = ref()
const categoryMode = ref('EXPENSE')
const categoryModeOptions = [
  { label: '支出', value: 'EXPENSE' },
  { label: '收入', value: 'INCOME' }
]
const trendMode = ref('EXPENSE')
const trendModeOptions = [
  { label: '支出', value: 'EXPENSE' },
  { label: '收入', value: 'INCOME' },
  { label: '结余', value: 'BALANCE' }
]
const trendModeText = computed(() => {
  const map = { EXPENSE: '支出', INCOME: '收入', BALANCE: '结余' }
  return map[trendMode.value]
})

const categoryModeText = computed(() => categoryMode.value === 'EXPENSE' ? '支出' : '收入')

const selectedTrendIndex = ref(-1)
const selectedTrendMonth = computed(() => {
  const trend = monthTrendItems.value
  if (trend.length === 0) return null
  if (selectedTrendIndex.value === -1) return trend[trend.length - 1]
  return trend[selectedTrendIndex.value] || trend[trend.length - 1]
})

const averageTrendValue = computed(() => {
  const trend = monthTrendItems.value
  if (trend.length === 0) return 0
  const key = trendMode.value.toLowerCase()
  const sum = trend.reduce((acc, item) => acc + Number(item[key] || 0), 0)
  return sum / trend.length
})

const chartColors = ['#007aff', '#34c759', '#ff9500', '#ff3b30', '#5856d6', '#af52de', '#5ac8fa', '#ff2d55', '#8e8e93']
let monthChartInstance = null
let categoryChartInstance = null

const monthTrendItems = computed(() => summary.monthTrend || [])

const currentCategoryItems = computed(() => (summary.categoryStats || [])
  .filter(item => item.type === categoryMode.value)
  .sort((a, b) => Number(b.amount || 0) - Number(a.amount || 0)))
const topCategoryItems = computed(() => currentCategoryItems.value.slice(0, 5))
const currentCategoryTotal = computed(() => Number(categoryMode.value === 'EXPENSE' ? summary.expense : summary.income))

watch(trendMode, () => {
  renderMonthChart()
})

function money(value) {
  return Number(value || 0).toFixed(2)
}

function formatTime(value) {
  return value ? value.replace('T', ' ') : '-'
}

function shareSummary(item) {
  const text = item.content || item.title || ''
  return text.length > 60 ? `${text.slice(0, 60)}...` : text
}

async function load() {
  const shareRes = await listShares({ page: 0, size: isAdmin.value ? 8 : 12, status: 'ENABLED' })
  wallShares.value = shareRes.data?.records || []
  if (!isAdmin.value) {
    const [financeRes, investRes] = await Promise.all([financeSummary(), investmentStats()])
    Object.assign(summary, financeRes.data || {})
    Object.assign(investment, investRes.data || {})
    await nextTick()
    renderCharts()
  }
}

function showDetail(item) {
  detail.share = item
  detail.visible = true
}
function goToFinance(item) {
  router.push({ path: '/finance', query: { categoryName: item.categoryName, type: item.type } })
}

function renderMonthChart() {
  const monthNode = monthRef.value
  if (!monthNode) {
    return
  }
  const trend = monthTrendItems.value
  if (trend.length === 0) {
    monthChartInstance?.dispose()
    monthChartInstance = null
    return
  }
  if (!monthChartInstance) {
    monthChartInstance = echarts.init(monthNode)
    monthChartInstance.on('click', (params) => {
      selectedTrendIndex.value = params.dataIndex
    })
  } else {
    monthChartInstance.resize()
  }

  const key = trendMode.value.toLowerCase()
  const seriesName = trendModeText.value
  const data = trend.map(item => Number(item[key] || 0))
  
  const themeColors = {
    EXPENSE: { line: '#ff3b30', area: ['rgba(255, 59, 48, 0.25)', 'rgba(255, 59, 48, 0.04)'] },
    INCOME: { line: '#34c759', area: ['rgba(52, 199, 89, 0.25)', 'rgba(52, 199, 89, 0.04)'] },
    BALANCE: { line: '#007aff', area: ['rgba(0, 122, 255, 0.25)', 'rgba(0, 122, 255, 0.04)'] }
  }
  const theme = themeColors[trendMode.value]

  monthChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'line' },
      formatter(params) {
        const item = params[0]
        return `${item.axisValue}<br/>${item.marker}${seriesName}：￥${money(item.value)}`
      }
    },
    grid: {
      left: 12,
      right: 24,
      top: 40,
      bottom: 20,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trend.map(item => item.month),
      axisLine: { lineStyle: { color: '#e5e5ea' } },
      axisLabel: { color: '#8e8e93', fontSize: 11 }
    },
    yAxis: { 
      type: 'value',
      splitLine: { lineStyle: { type: 'dashed', color: '#f2f2f7' } },
      axisLabel: { color: '#8e8e93', fontSize: 11 }
    },
    series: [
      {
        name: seriesName,
        type: 'line',
        smooth: true,
        showSymbol: true,
        symbolSize: 8,
        data: data,
        itemStyle: { color: theme.line },
        lineStyle: { width: 3, color: theme.line },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: theme.area[0] },
            { offset: 1, color: theme.area[1] }
          ])
        },
        emphasis: {
          scale: true,
          itemStyle: { borderWidth: 2, borderColor: '#fff' }
        }
      }
    ]
  }, true)
}

function renderCategoryChart() {
  const categoryNode = categoryRef.value
  if (!categoryNode) {
    return
  }
  const items = currentCategoryItems.value
  if (items.length === 0) {
    categoryChartInstance?.dispose()
    categoryChartInstance = null
    return
  }
  if (!categoryChartInstance) {
    categoryChartInstance = echarts.init(categoryNode)
  } else {
    categoryChartInstance.resize()
  }
  const totalLabel = categoryMode.value === 'EXPENSE' ? '总支出' : '总收入'
  const chartData = items.map(item => ({
    name: item.categoryName,
    value: Number(item.amount || 0)
  }))
  categoryChartInstance.setOption({
    color: chartColors,
    tooltip: {
      trigger: 'item',
      formatter(params) {
        return `${params.name}<br/>金额：￥${money(params.value)}<br/>占比：${Number(params.percent || 0).toFixed(2)}%`
      }
    },
    series: [{
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '46%'],
      avoidLabelOverlap: true,
      label: {
        formatter: '{b}\n{d}%'
      },
      labelLine: {
        length: 12,
        length2: 10
      },
      data: chartData
    }],
    graphic: [{
      type: 'text',
      left: 'center',
      top: '40%',
      style: {
        text: `${totalLabel}\n￥${money(currentCategoryTotal.value)}`,
        textAlign: 'center',
        fill: '#303133',
        fontSize: 14,
        fontWeight: 600,
        lineHeight: 22
      }
    }]
  }, true)
}

function renderCharts() {
  renderMonthChart()
  renderCategoryChart()
}

function handleResize() {
  monthChartInstance?.resize()
  categoryChartInstance?.resize()
}

watch(categoryMode, async () => {
  if (isAdmin.value) {
    return
  }
  await nextTick()
  renderCategoryChart()
})

onMounted(() => {
  load()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.share-wall-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.share-wall-actions .el-button.is-link {
  padding: 0 4px;
}

.share-expand-icon {
  margin-left: 2px;
  vertical-align: middle;
}

.admin-share-panel {
  margin-top: 4px;
}

.admin-share-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.admin-share-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.admin-share-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 12px 14px;
  border: none;
  border-radius: var(--radius-sm);
  background: #f9f9fb;
  cursor: pointer;
}

.admin-share-item:hover {
  background: #f2f2f7;
}

.admin-share-item-main {
  min-width: 0;
}

.admin-share-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.admin-share-summary {
  margin: 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
}

.admin-share-time {
  flex-shrink: 0;
  color: #909399;
  font-size: 12px;
  white-space: nowrap;
}

.category-chart {
  height: 260px;
}

.trend-chart {
  height: 480px;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.trend-header-left {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.trend-title {
  margin: 0;
  font-size: 16px;
  color: var(--text-main);
  font-weight: 700;
}

.trend-meta {
  display: flex;
  gap: 24px;
}

.trend-meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.trend-meta-label {
  font-size: 12px;
  color: #6b7280;
}

.trend-meta-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-main);
}

.trend-meta-value.highlight {
  color: var(--primary-color);
}

.month-trend-panel {
  display: flex;
  flex-direction: column;
}

.category-stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.category-rank-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-card-item {
  padding: 12px 14px;
  border-radius: var(--radius-md);
  background: #fafafa;
  cursor: pointer;
  border: 1px solid var(--border-light);
}

.category-card-item:hover {
  background: #fff;
  border-color: var(--border-regular);
}

.category-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.category-card-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-card-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.category-card-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
}

.category-card-percent {
  font-size: 12px;
  color: #6b7280;
}

.category-card-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-card-amount {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
}

.category-card-arrow {
  color: #9ca3af;
  font-size: 14px;
}

.category-card-bottom {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-card-progress {
  flex: 1;
}

.category-card-count {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
  min-width: 32px;
  text-align: right;
}
</style>
