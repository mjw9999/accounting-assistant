<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-name">记账小助手</div>
      </div>
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/dashboard"><el-icon><DataLine /></el-icon><span>系统首页</span></el-menu-item>
        <el-menu-item index="/finance"><el-icon><Wallet /></el-icon><span>财务管理</span></el-menu-item>
        <el-menu-item index="/categories"><el-icon><CollectionTag /></el-icon><span>分类管理</span></el-menu-item>
        <el-menu-item index="/products"><el-icon><Goods /></el-icon><span>{{ isAdmin ? '产品管理' : '理财库' }}</span></el-menu-item>
        <el-menu-item index="/investments"><el-icon><TrendCharts /></el-icon><span>理财记录</span></el-menu-item>
        <el-menu-item index="/shares"><el-icon><ChatLineRound /></el-icon><span>理财分享</span></el-menu-item>
        <el-menu-item v-if="isAdmin" index="/users"><el-icon><UserFilled /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item v-if="isAdmin" index="/admins"><el-icon><Setting /></el-icon><span>管理员管理</span></el-menu-item>
        <el-menu-item index="/profile"><el-icon><Avatar /></el-icon><span>个人资料</span></el-menu-item>
      </el-menu>
    </aside>
    <section class="main">
      <header class="topbar">
        <div class="topbar-left">
          <span class="page-indicator">{{ pageName }}</span>
        </div>
        <div class="topbar-right">
          <div class="user-info">
            <img class="avatar" :src="avatarSrc" alt="头像" @error="onAvatarError" />
            <div class="user-meta">
              <span class="user-name">{{ auth.user?.displayName || auth.user?.username }}</span>
              <span class="user-role">{{ auth.user?.role === 'ADMIN' ? '管理员' : '用户' }}</span>
            </div>
          </div>
          <el-button link :icon="SwitchButton" @click="logout" style="margin-left: 20px">退出登录</el-button>
        </div>
      </header>
      <main class="page-scroll">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Avatar, ChatLineRound, CollectionTag, DataLine, Goods, Setting, SwitchButton, TrendCharts, UserFilled, Wallet } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { onAvatarError, resolveAvatarSrc } from '../utils/avatar'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'ADMIN')
const avatarSrc = computed(() => resolveAvatarSrc(auth.user?.avatarUrl))
const names = {
  '/dashboard': '系统首页',
  '/finance': '财务管理',
  '/categories': '记账分类管理',
  '/products': isAdmin.value ? '理财产品信息管理' : '理财产品信息库',
  '/investments': '理财记录管理',
  '/shares': '分享管理',
  '/users': '用户管理',
  '/admins': '管理员管理',
  '/profile': '个人资料'
}
const pageName = computed(() => names[route.path] || '系统')

function logout() {
  auth.logout()
  router.replace('/login')
}
</script>
