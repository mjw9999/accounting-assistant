<template>
  <div>
    <h2 class="page-title">分享管理</h2>

    <section class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" clearable placeholder="产品/标题/心得/理财人" style="width: 260px" @keyup.enter="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <el-button v-if="!isAdmin" type="success" :icon="Plus" @click="open()">添加分享</el-button>
      </div>
      <el-table :data="rows" border>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="productName" label="理财产品" width="150" />
        <el-table-column prop="title" label="标题" width="180" />
        <el-table-column prop="content" label="心得" min-width="260" show-overflow-tooltip />
        <el-table-column prop="investorName" label="理财人" width="120" />
        <el-table-column label="投入金额" width="120"><template #default="{ row }">￥{{ money(row.amount) }}</template></el-table-column>
        <el-table-column label="理财收益" width="120"><template #default="{ row }"><span style="color: #f56c6c">￥{{ money(row.income) }}</span></template></el-table-column>
        <el-table-column label="状态" width="90"><template #default="{ row }">{{ row.status === 'ENABLED' ? '展示' : '隐藏' }}</template></el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="View" @click="showDetail(row)">详情</el-button>
            <el-button v-if="isAdmin || row.userId === auth.user?.id" size="small" :icon="Edit" @click="open(row)">编辑</el-button>
            <el-button v-if="isAdmin || row.userId === auth.user?.id" size="small" type="danger" :icon="Delete" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '修改分享' : '添加分享'" width="560px">
      <el-form :model="dialog.form" label-width="86px">
        <el-form-item label="理财记录">
          <el-select v-model="dialog.form.investmentId" filterable style="width: 100%">
            <el-option v-for="item in investments" :key="item.id" :label="`${item.productName} / ￥${item.amount} / ${item.investorName}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="dialog.form.title" /></el-form-item>
        <el-form-item label="心得"><el-input v-model="dialog.form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="dialog.form.status" active-value="ENABLED" inactive-value="DISABLED" active-text="展示" inactive-text="隐藏" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Search, View } from '@element-plus/icons-vue'
import { createShare, deleteShare, listInvestments, listShares, updateShare } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'ADMIN')
const detail = reactive({ visible: false, share: null })
const query = reactive({ page: 0, size: 10, keyword: '' })
const rows = ref([])
const investments = ref([])
const dialog = reactive({ visible: false, form: {} })

function money(value) {
  return Number(value || 0).toFixed(2)
}

function formatTime(value) {
  return value ? value.replace('T', ' ') : '-'
}

async function load() {
  const res = await listShares(query)
  rows.value = res.data?.records || []
}

async function loadInvestments() {
  const res = await listInvestments({ size: 100 })
  investments.value = res.data?.records || []
}

async function open(row) {
  await loadInvestments()
  dialog.form = row ? { ...row } : { investmentId: investments.value[0]?.id, title: '', content: '', status: 'ENABLED' }
  dialog.visible = true
}

async function save() {
  const isUpdate = !!dialog.form.id
  if (isUpdate) await updateShare(dialog.form.id, dialog.form)
  else await createShare(dialog.form)
  ElMessage.success(isUpdate ? '分享修改成功' : '分享发布成功')
  dialog.visible = false
  load()
}

function showDetail(item) {
  detail.share = item
  detail.visible = true
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确认删除该分享内容？', '系统警告', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger'
    })
    await deleteShare(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {}
}

onMounted(load)
</script>
