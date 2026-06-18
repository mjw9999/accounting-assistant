<template>
  <div>
    <h2 class="page-title">{{ isAdmin ? '理财产品信息管理' : '理财产品信息库' }}</h2>
    <p v-if="!isAdmin" class="muted" style="margin: -10px 0 16px">本页面展示由管理员统一维护的理财产品信息，供用户在理财记录中选择参考，不涉及真实金融产品购买。</p>
    <section class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" clearable placeholder="产品名称/发布者" style="width: 220px" @keyup.enter="load" />
        <el-input v-model="query.type" clearable placeholder="产品类型" style="width: 150px" @keyup.enter="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <el-button v-if="isAdmin" type="success" :icon="Plus" @click="open()">添加产品</el-button>
      </div>
      <el-table :data="rows" border>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="productCode" label="理财编号" width="180" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column label="年收益率" width="120"><template #default="{ row }">{{ row.annualRate }}%</template></el-table-column>
        <el-table-column label="金额范围" width="190"><template #default="{ row }">￥{{ row.minAmount }} - ￥{{ row.maxAmount }}</template></el-table-column>
        <el-table-column prop="termDays" label="理财天数" width="100" />
        <el-table-column prop="riskLevel" label="风险等级" width="110" />
        <el-table-column prop="publisher" label="发布者" width="120" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column v-if="isAdmin" label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="open(row)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 14px; display: flex; justify-content: flex-end">
        <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="query.size" @current-change="page => { query.page = page - 1; load() }" />
      </div>
    </section>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '修改理财产品信息' : '新增理财产品信息'" width="560px">
      <el-form :model="dialog.form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="dialog.form.name" /></el-form-item>
        <el-form-item label="类型"><el-input v-model="dialog.form.type" /></el-form-item>
        <el-form-item label="年收益率"><el-input-number v-model="dialog.form.annualRate" :min="0" :precision="4" style="width: 100%" /></el-form-item>
        <el-form-item label="最小金额"><el-input-number v-model="dialog.form.minAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="最大金额"><el-input-number v-model="dialog.form.maxAmount" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="理财天数"><el-input-number v-model="dialog.form.termDays" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="风险等级"><el-input v-model="dialog.form.riskLevel" /></el-form-item>
        <el-form-item label="发布者"><el-input v-model="dialog.form.publisher" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="dialog.form.status" active-value="ENABLED" inactive-value="DISABLED" active-text="启用" inactive-text="停用" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="dialog.form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import { createProduct, deleteProduct, listProducts, updateProduct } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'ADMIN')
const query = reactive({ page: 0, size: 10, keyword: '', type: '' })
const rows = ref([])
const total = ref(0)
const dialog = reactive({ visible: false, form: {} })

async function load() {
  const res = await listProducts(query)
  rows.value = res.data?.records || []
  total.value = res.data?.total || 0
}
function open(row) {
  dialog.form = row ? { ...row } : {
    name: '',
    type: '货币',
    annualRate: 2.3,
    publisher: '管理员',
    minAmount: 100,
    maxAmount: 10000,
    termDays: 30,
    riskLevel: '低风险',
    status: 'ENABLED',
    remark: ''
  }
  dialog.visible = true
}
async function save() {
  if (dialog.form.id) await updateProduct(dialog.form.id, dialog.form)
  else await createProduct(dialog.form)
  ElMessage.success('保存成功')
  dialog.visible = false
  load()
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确认删除该理财产品？删除后将影响已购该产品的用户记录。', '系统警告', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger'
    })
    await deleteProduct(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {}
}
onMounted(load)
</script>
