<template>
  <div>
    <h2 class="page-title">用户管理</h2>
    <section class="panel">
      <div class="toolbar">
        <el-input v-model="query.keyword" clearable placeholder="用户名/姓名/手机号" style="width: 240px" @keyup.enter="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <el-button type="success" :icon="Plus" @click="open()">新增用户</el-button>
      </div>
      <el-table :data="rows" border>
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="phone" label="联系方式" width="140" />
        <el-table-column prop="address" label="家庭住址" />
        <el-table-column label="状态" width="90"><template #default="{ row }">{{ row.status === 'ENABLED' ? '启用' : '停用' }}</template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
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

    <el-dialog v-model="dialog.visible" :title="dialog.form.id ? '修改用户' : '新增用户'" width="520px">
      <el-form :model="dialog.form" label-width="86px">
        <el-form-item label="用户名"><el-input v-model="dialog.form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="dialog.form.password" type="password" show-password placeholder="不填则保持原密码" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="dialog.form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="dialog.form.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="dialog.form.address" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="dialog.form.status" active-value="ENABLED" inactive-value="DISABLED" active-text="启用" inactive-text="停用" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import { createUser, deleteUser, listUsers, updateUser } from '../api/modules'

const query = reactive({ page: 0, size: 10, keyword: '' })
const rows = ref([])
const total = ref(0)
const dialog = reactive({ visible: false, form: {} })

async function load() {
  const res = await listUsers(query)
  rows.value = res.data?.records || []
  total.value = res.data?.total || 0
}
function open(row) {
  dialog.form = row ? { ...row, password: '' } : { username: '', password: '123456', realName: '', phone: '', address: '', status: 'ENABLED' }
  dialog.visible = true
}
async function save() {
  if (dialog.form.id) await updateUser(dialog.form.id, dialog.form)
  else await createUser(dialog.form)
  ElMessage.success('保存成功')
  dialog.visible = false
  load()
}
async function remove(id) {
  try {
    await ElMessageBox.confirm('确认删除该用户账号？删除后其名下理财与财务数据将同步受影响。', '系统警告', {
      type: 'warning', confirmButtonText: '确定删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger'
    })
    await deleteUser(id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {}
}
onMounted(load)
</script>
