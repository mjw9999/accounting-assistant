<template>
  <main class="login-page">
    <section class="login-panel">
      <h1 class="login-title">{{ titleText }}</h1>
      <p v-if="authMode === 'forgot'" class="login-hint">仅限普通用户自助找回。请填写注册时的账号、姓名；若个人资料中已填写手机号，需一并填写。</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top" @keyup.enter="submit">
        <el-form-item v-if="authMode === 'login'" label="登录身份" prop="role">
          <el-segmented v-model="form.role" :options="roleOptions" />
        </el-form-item>
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" size="large" placeholder="请输入账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item v-if="authMode === 'register' || authMode === 'forgot'" label="姓名" prop="realName">
          <el-input v-model="form.realName" size="large" placeholder="请输入注册时的真实姓名" />
        </el-form-item>
        <el-form-item v-if="authMode === 'forgot'" label="手机号" prop="phone">
          <el-input v-model="form.phone" size="large" placeholder="资料中已填写手机号时必填" maxlength="11" />
        </el-form-item>
        <el-form-item v-if="authMode === 'login' || authMode === 'register'" label="密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item v-if="authMode === 'forgot'" label="新密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请设置新密码（6-32位）" :prefix-icon="Lock" />
        </el-form-item>

        <el-form-item v-if="authMode !== 'register'" label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" size="large" placeholder="验证码" />
            <div class="captcha-box" @click="refreshCaptcha" title="点击刷新">
              <canvas ref="captchaCanvas" width="100" height="40"></canvas>
            </div>
          </div>
        </el-form-item>

        <el-form-item v-if="authMode === 'register' || authMode === 'forgot'" label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" size="large" type="password" show-password placeholder="请再次输入密码" :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">
          {{ submitText }}
        </el-button>
        <div class="auth-footer-link">
          <template v-if="authMode === 'login'">
            <span>没有账号？</span>
            <el-button type="primary" link @click="switchMode('register')">去注册</el-button>
            <span class="footer-divider">|</span>
            <el-button type="primary" link @click="switchMode('forgot')">忘记密码</el-button>
          </template>
          <template v-else-if="authMode === 'register'">
            <span>已有账号？</span>
            <el-button type="primary" link @click="switchMode('login')">去登录</el-button>
          </template>
          <template v-else>
            <span>想起密码了？</span>
            <el-button type="primary" link @click="switchMode('login')">返回登录</el-button>
          </template>
        </div>
      </el-form>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { register, resetPassword } from '../api/modules'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)
const authMode = ref('login')
const roleOptions = [
  { label: '普通用户', value: 'USER' },
  { label: '管理员', value: 'ADMIN' }
]

const captchaCanvas = ref(null)
const generatedCaptcha = ref('')

const form = reactive({
  role: 'USER',
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  captcha: ''
})

const titleText = computed(() => {
  if (authMode.value === 'register') return '注册账号'
  if (authMode.value === 'forgot') return '找回密码'
  return '记账小助手'
})

const submitText = computed(() => {
  if (authMode.value === 'register') return '注册账号'
  if (authMode.value === 'forgot') return '重置密码'
  return '登录系统'
})

function refreshCaptcha() {
  const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  generatedCaptcha.value = code
  drawCaptcha(code)
}

function drawCaptcha(code) {
  const canvas = captchaCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.fillStyle = '#f2f2f7'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = `rgba(${Math.random() * 255},${Math.random() * 255},${Math.random() * 255},0.5)`
    ctx.beginPath()
    ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.stroke()
  }
  ctx.font = 'bold 24px Arial'
  ctx.textBaseline = 'middle'
  for (let i = 0; i < code.length; i++) {
    ctx.fillStyle = `rgb(${Math.random() * 150},${Math.random() * 150},${Math.random() * 150})`
    ctx.save()
    ctx.translate(20 + i * 20, 20)
    ctx.rotate((Math.random() - 0.5) * 0.3)
    ctx.fillText(code[i], -10, 0)
    ctx.restore()
  }
}

function validateConfirmPassword(rule, value, callback) {
  if (authMode.value === 'login') {
    callback()
    return
  }
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const rules = computed(() => ({
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password:
    authMode.value === 'login'
      ? [{ required: true, message: '请输入密码', trigger: 'blur' }]
      : [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 32, message: '密码长度必须在6-32之间', trigger: 'blur' }
        ],
  role: authMode.value === 'login' ? [{ required: true, message: '请选择身份', trigger: 'change' }] : [],
  realName:
    authMode.value === 'register' || authMode.value === 'forgot'
      ? [{ required: true, message: '请输入姓名', trigger: 'blur' }]
      : [],
  phone:
    authMode.value === 'forgot'
      ? [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
      : [],
  captcha: authMode.value === 'register' ? [] : [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  confirmPassword:
    authMode.value === 'register' || authMode.value === 'forgot'
      ? [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      : []
}))

function switchMode(mode) {
  authMode.value = mode
  form.password = ''
  form.confirmPassword = ''
  form.captcha = ''
  form.phone = ''
  if (mode === 'forgot') {
    form.role = 'USER'
    form.realName = ''
  } else if (mode !== 'register') {
    form.realName = ''
  }
  formRef.value?.resetFields()
  nextTick(() => {
    if (mode !== 'register') {
      refreshCaptcha()
    }
  })
}

function validateCaptcha() {
  if (authMode.value === 'register') {
    return true
  }
  if (!form.captcha) {
    ElMessage.warning('请输入验证码')
    return false
  }
  if (form.captcha.toUpperCase() !== generatedCaptcha.value) {
    ElMessage.error('验证码错误')
    refreshCaptcha()
    return false
  }
  return true
}

async function submit() {
  await formRef.value.validate()
  if (!validateCaptcha()) {
    return
  }

  loading.value = true
  try {
    if (authMode.value === 'register') {
      await register({ username: form.username, password: form.password, realName: form.realName })
      ElMessage.success('注册成功，请登录')
      switchMode('login')
    } else if (authMode.value === 'forgot') {
      await resetPassword({
        username: form.username,
        realName: form.realName,
        phone: form.phone || undefined,
        role: 'USER',
        newPassword: form.password
      })
      ElMessage.success('密码重置成功，请登录')
      switchMode('login')
    } else {
      await auth.login(form)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}

watch(() => form.role, () => {
  if (authMode.value !== 'register') {
    form.captcha = ''
    refreshCaptcha()
  }
})

onMounted(() => {
  if (authMode.value !== 'register') {
    refreshCaptcha()
  }
})
</script>

<style scoped>
.login-hint {
  margin: -16px 0 20px;
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.5;
  text-align: center;
}

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.captcha-row .el-input {
  flex: 1;
}

.captcha-box {
  background: #f2f2f7;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  border: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.captcha-box:hover {
  border-color: var(--primary-color);
}

.auth-footer-link {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 24px;
  font-size: 14px;
  color: var(--text-regular);
}

.footer-divider {
  color: var(--border-regular);
  margin: 0 4px;
}
</style>
