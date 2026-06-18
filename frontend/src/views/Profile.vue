<template>
  <div>
    <section class="panel profile-header">
      <div class="profile-header-inner">
        <div class="avatar-wrap">
          <img class="avatar-lg" :src="avatarSrc" alt="头像" @error="onAvatarError" />
          <el-upload v-if="!isAdmin" :show-file-list="false" :http-request="upload">
            <el-button type="primary" link size="small">更换头像</el-button>
          </el-upload>
        </div>
        <div>
          <h2 class="profile-name">{{ profile.realName || profile.username }}</h2>
          <p class="profile-meta muted">{{ isAdmin ? '管理员' : '用户' }} · {{ profile.username }}</p>
        </div>
      </div>
    </section>

    <section class="panel profile-body">
      <h3 class="section-title">基本资料</h3>
      <el-form :model="profile" label-width="80px" class="profile-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="profile.username" :disabled="isAdmin" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="profile.realName" :disabled="isAdmin" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!isAdmin">
            <el-form-item label="手机号">
              <el-input v-model="profile.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!isAdmin">
            <el-form-item label="住址">
              <el-input v-model="profile.address" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!isAdmin">
          <el-button type="primary" @click="saveProfile">保存</el-button>
        </el-form-item>
        <el-alert
          v-if="isAdmin"
          title="管理员资料请在「管理员管理」中维护。"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form>

      <h3 class="section-title section-title--split">修改密码</h3>
      <el-form :model="passwordForm" label-width="100px" class="profile-form profile-form--narrow">
        <el-form-item label="当前密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirm" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="savePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword, getProfile, updateUser, uploadAvatar } from '../api/modules'
import { onAvatarError, resolveAvatarSrc } from '../utils/avatar'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'ADMIN')
const profile = reactive({ id: auth.user?.id, username: auth.user?.username, realName: auth.user?.displayName, phone: '', address: '', avatarUrl: auth.user?.avatarUrl })
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const avatarSrc = computed(() => resolveAvatarSrc(profile.avatarUrl))

async function load() {
  if (isAdmin.value) return
  const res = await getProfile()
  Object.assign(profile, res.data || {})
}
async function upload({ file }) {
  const res = await uploadAvatar(file)
  profile.avatarUrl = res.data.url
  auth.updateUser({ avatarUrl: profile.avatarUrl })
  ElMessage.success('头像上传成功')
}
async function saveProfile() {
  await updateUser(profile.id, profile)
  auth.updateUser({ displayName: profile.realName, avatarUrl: profile.avatarUrl })
  ElMessage.success('保存成功')
}
async function savePassword() {
  if (passwordForm.newPassword !== passwordForm.confirm) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirm = ''
  ElMessage.success('密码已修改')
}
onMounted(load)
</script>

<style scoped>
.profile-header {
  margin-bottom: 16px;
}

.profile-header-inner {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-lg {
  width: 72px;
  height: 72px;
  border-radius: 10px;
  object-fit: cover;
  border: none;
  background: #e5e5ea;
}

.profile-name {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 600;
}

.profile-meta {
  margin: 0;
  font-size: 13px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
}

.section-title--split {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
}

.profile-form--narrow {
  max-width: 480px;
}
</style>
