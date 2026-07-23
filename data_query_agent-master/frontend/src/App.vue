<template>
  <div class="app-container">
    <el-container style="height: 100vh;">
      <el-header height="60px" class="header">
        <div class="logo">电商问数智能体</div>
        <div class="user-info" v-if="user">
          <span>{{ user.username }}</span>
          <el-button type="text" @click="logout">退出</el-button>
        </div>

      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu
            :default-active="activeMenu"
            class="menu"
            router
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/query">
              <el-icon><Search /></el-icon>
              <span>数据查询</span>
            </el-menu-item>
            <el-menu-item index="/products">
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </el-menu-item>
            <el-menu-item index="/orders">
              <el-icon><Tickets /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="/categories">
              <el-icon><Menu /></el-icon>
              <span>分类管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { House, Search, Goods, Tickets, Menu } from '@element-plus/icons-vue'

const router = useRouter()
const user = ref(null)

const activeMenu = computed(() => {
  return router.currentRoute.value.path
})

const logout = () => {
  localStorage.removeItem('user')
  user.value = null
  router.push('/login')
}

onMounted(() => {
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    user.value = JSON.parse(savedUser)
  }
})
</script>

<style scoped>
.app-container {
  width: 100%;
  height: 100vh;
}

.header {
  background-color: #409EFF;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.aside {
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
}

.menu {
  height: 100%;
}

.main {
  padding: 20px;
  overflow-y: auto;
}
</style>