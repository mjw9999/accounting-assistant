<template>
  <div>
    <h2 class="page-title">记账分类管理</h2>
    <section class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" clearable placeholder="分类名称" style="width: 220px" @keyup.enter="load" />
        <el-select v-model="query.type" clearable placeholder="类型" style="width: 140px">
          <el-option label="收入" value="INCOME" />
          <el-option label="支出" value="EXPENSE" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <el-button v-if="isAdmin" type="success" :icon="Plus" @click="open()">新增分类</el-button>
      </div>
      <el-table :data="rows" border>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }"><el-tag :type="row.type === 'INCOME' ? 'success' : 'warning'">{{ typeText(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ row.status === 'ENABLED' ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="Edit" @click="open(row)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '编辑分类' : '新增分类'" width="480px">
      <el-form :model="dialog.form" label-width="86px">
        <el-form-item label="分类名称"><el-input v-model="dialog.form.name" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="dialog.form.type">
            <el-radio-button label="INCOME">收入</el-radio-button>
            <el-radio-button label="EXPENSE">支出</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dialog.form.status" active-value="ENABLED" inactive-value="DISABLED" active-text="启用" inactive-text="停用" />
        </el-form-item>
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
import { createCategory, deleteCategory, listCategories, updateCategory } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'ADMIN')
const query = reactive({ keyword: '', type: '' })
const rows = ref([])
const dialog = reactive({ visible: false, form: {} })

function typeText(type) {
  return type === 'INCOME' ? '收入' : '支出'
}

async function load() {
  const res = await listCategories(query)
  rows.value = res.data || []
}

function open(row) {
  dialog.form = row ? { ...row } : { name: '', type: 'EXPENSE', status: 'ENABLED', remark: '' }
  dialog.visible = true
}

async function save() {
  if (dialog.form.id) {
    await updateCategory(dialog.form.id, dialog.form)
  } else {
    await createCategory(dialog.form)
  }
  ElMessage.success('保存成功')
  dialog.visible = false
  load()
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确认删除该记账分类？', '系统警告', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger'
    })
    await deleteCategory(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {}
}

onMounted(load)
</script>
