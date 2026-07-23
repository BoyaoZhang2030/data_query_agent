<template>
  <div class="app-container">
    <div v-if="discoActive" class="disco-layer" aria-hidden="true">
      <div class="disco-beams"><i v-for="n in 12" :key="n" :style="{ '--beam-index': n }"></i></div>
      <div class="disco-ball"><span v-for="n in 36" :key="n"></span></div>
      <div class="disco-floor"></div>
      <div class="sparkles"><i v-for="n in 28" :key="n" :style="{ '--spark-index': n, left: `${(n * 37) % 100}%`, top: `${(n * 61) % 100}%` }"></i></div>
    </div>
    <audio ref="bgmPlayer" :src="bgmPath" loop preload="none"></audio>
    <router-view v-if="isPublicPage" v-slot="{ Component, route }">
      <transition name="page-slide" mode="out-in"><component :is="Component" :key="route.path" /></transition>
    </router-view>
    <el-container v-else style="height: 100vh;" class="admin-layout">
        <el-aside width="238px" class="aside">
          <div class="side-brand" @click="router.push('/dashboard')">
            <span class="side-logo">问</span>
            <div><strong>电商问数</strong><small>DATA INTELLIGENCE</small></div>
          </div>
          <div class="menu-caption">工作台</div>
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
          <div class="side-footer">
            <div class="status-dot"></div>
            <div><strong>系统运行正常</strong><small>数据服务已连接</small></div>
          </div>
        </el-aside>
        <el-container class="workspace">
          <el-header height="76px" class="header">
            <div class="page-heading"><small>电商数据工作台</small><strong>{{ currentPageTitle }}</strong></div>
            <div class="header-actions" v-if="user">
              <div class="user-avatar">{{ user.username?.slice(0, 1).toUpperCase() }}</div>
              <div class="user-info"><strong>{{ user.username }}</strong><small>{{ user.role === 'admin' ? '管理员' : '普通用户' }}</small></div>
              <el-button class="logout-button" @click="logout">退出登录</el-button>
              <button class="secret-disco" aria-label="隐藏模式" @click="toggleDisco"></button>
            </div>
          </el-header>
        <el-main class="main">
          <router-view v-slot="{ Component, route }">
            <transition name="content-slide" mode="out-in"><component :is="Component" :key="route.path" /></transition>
          </router-view>
        </el-main>
        </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, Search, Goods, Tickets, Menu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const user = ref(null)
const discoActive = ref(false)
const bgmPlayer = ref(null)
const bgmPath = '/audio/disco-bgm.mp3'
const isPublicPage = computed(() => Boolean(route.meta.public))

const activeMenu = computed(() => {
  return router.currentRoute.value.path
})
const pageTitles = { '/dashboard': '仪表盘', '/query': '数据查询', '/products': '商品管理', '/orders': '订单管理', '/categories': '分类管理' }
const currentPageTitle = computed(() => pageTitles[route.path] || '数据工作台')

const logout = () => {
  stopDisco()
  localStorage.removeItem('user')
  user.value = null
  router.push('/login')
}
const stopDisco = () => {
  discoActive.value = false
  if (bgmPlayer.value) {
    bgmPlayer.value.pause()
    bgmPlayer.value.currentTime = 0
  }
}
const toggleDisco = async () => {
  if (discoActive.value) return stopDisco()
  discoActive.value = true
  await nextTick()
  bgmPlayer.value?.play().catch(() => {
    ElMessage.info('Disco 特效已开启；放入 BGM 文件后会自动播放')
  })
}

watch(() => route.fullPath, () => {
  const savedUser = localStorage.getItem('user')
  user.value = savedUser ? JSON.parse(savedUser) : null
}, { immediate: true })
</script>

<style scoped>
.app-container {
  width: 100%;
  height: 100vh;
}

.header {
  background: rgba(255,255,255,.88);
  color: #172b4d;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 28px;
  border-bottom: 1px solid #e8eef6;
  backdrop-filter: blur(16px);
}

.page-slide-enter-active, .page-slide-leave-active { transition: opacity .42s ease, transform .42s cubic-bezier(.22,1,.36,1); }
.page-slide-enter-from { opacity: 0; transform: translateX(45px); }
.page-slide-leave-to { opacity: 0; transform: translateX(-35px); }
.content-slide-enter-active, .content-slide-leave-active { transition: opacity .3s ease, transform .3s cubic-bezier(.22,1,.36,1); }
.content-slide-enter-from { opacity: 0; transform: translateX(24px); }
.content-slide-leave-to { opacity: 0; transform: translateX(-18px); }
.admin-layout { background: #f3f7fc; }

.aside {
  position: relative;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #0d2b4d 0%, #081c34 100%);
  border-right: 0;
  box-shadow: 12px 0 40px rgba(20,56,93,.08);
  overflow: hidden;
}

.menu {
  flex: 1;
  padding: 0 14px;
  border-right: 0;
  background: transparent;
  --el-menu-text-color: #9db2c9;
  --el-menu-hover-text-color: #fff;
  --el-menu-bg-color: transparent;
  --el-menu-hover-bg-color: rgba(74,156,241,.12);
  --el-menu-active-color: #fff;
}
.menu :deep(.el-menu-item) { height: 52px; margin: 6px 0; padding: 0 17px !important; border-radius: 13px; font-size: 15px; transition: all .25s ease; }
.menu :deep(.el-menu-item .el-icon) { font-size: 20px; margin-right: 14px; }
.menu :deep(.el-menu-item.is-active) { background: linear-gradient(90deg, #287fdf, #459ef0); box-shadow: 0 10px 24px rgba(25,112,211,.3); transform: translateX(3px); }
.side-brand { height: 90px; margin: 0 18px; display: flex; align-items: center; gap: 13px; color: #fff; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,.08); }
.side-logo { width: 43px; height: 43px; display: grid; place-items: center; border-radius: 14px; color: #09203a; background: linear-gradient(135deg, #67e6ff, #7ba4ff); font-size: 22px; font-weight: 900; box-shadow: 0 9px 25px rgba(54,184,255,.25); }
.side-brand strong { display: block; font-size: 19px; letter-spacing: 1px; }.side-brand small { display: block; margin-top: 3px; color: #63819e; font-size: 8px; letter-spacing: 1.8px; }
.menu-caption { padding: 25px 30px 8px; color: #506d89; font-size: 11px; letter-spacing: 2px; }
.side-footer { margin: 18px; padding: 15px; display: flex; align-items: center; gap: 11px; color: #b8cadb; border: 1px solid rgba(127,185,231,.1); border-radius: 14px; background: rgba(255,255,255,.035); }
.side-footer strong, .side-footer small { display: block; font-size: 11px; }.side-footer small { margin-top: 4px; color: #56738f; }.status-dot { width: 9px; height: 9px; border-radius: 50%; background: #45d8a0; box-shadow: 0 0 0 5px rgba(69,216,160,.1); }
.workspace { min-width: 0; }
.page-heading small, .page-heading strong { display: block; }.page-heading small { margin-bottom: 4px; color: #9aaabd; font-size: 11px; letter-spacing: 1px; }.page-heading strong { font-size: 20px; }
.header-actions { display: flex; align-items: center; gap: 11px; }.user-avatar { width: 39px; height: 39px; display: grid; place-items: center; color: #fff; border-radius: 12px; background: linear-gradient(135deg, #3188e7, #6d80e9); font-weight: 700; box-shadow: 0 7px 18px rgba(49,136,231,.22); }
.user-info strong, .user-info small { display: block; }.user-info strong { color: #253852; font-size: 14px; }.user-info small { margin-top: 2px; color: #9aa9b8; font-size: 11px; }.logout-button { margin-left: 8px; border-radius: 10px; color: #5e7288; border-color: #dce6f1; }
.logout-button:hover { color: #287fdf; border-color: #73b4f5; background: #eef7ff; }
.secret-disco { width: 28px; height: 34px; margin-left: -5px; border: 0; border-radius: 9px; background: #fff; opacity: .025; cursor: pointer; outline: none; }
.secret-disco:hover { opacity: .07; }
.disco-layer { position: fixed; inset: 0; z-index: 9999; overflow: hidden; pointer-events: none; background: radial-gradient(circle at 50% 5%, rgba(255,255,255,.1), transparent 22%), rgba(3,4,18,.33); mix-blend-mode: screen; animation: disco-tint 3s linear infinite; }
.disco-ball { position: absolute; z-index: 3; left: 50%; top: 7%; width: 112px; height: 112px; transform: translateX(-50%); border-radius: 50%; overflow: hidden; display: grid; grid-template-columns: repeat(6, 1fr); grid-template-rows: repeat(6, 1fr); background: #d9f5ff; border: 2px solid rgba(255,255,255,.8); box-shadow: 0 0 18px #fff, 0 0 52px #50cfff, 0 0 95px rgba(220,67,255,.8); animation: ball-spin 2.8s linear infinite, ball-float 2s ease-in-out infinite alternate; }
.disco-ball::before { content: ''; position: absolute; left: 50%; bottom: 100%; width: 2px; height: 12vh; background: rgba(255,255,255,.6); }
.disco-ball span { border: 1px solid rgba(18,65,105,.3); background: linear-gradient(135deg, rgba(255,255,255,.98), rgba(71,196,255,.5) 45%, rgba(232,91,255,.72)); animation: tile-flash 1.3s calc(var(--spark-index, 1) * .04s) infinite alternate; }
.disco-beams { position: absolute; left: 50%; top: 12%; width: 0; height: 0; z-index: 1; animation: beams-spin 8s linear infinite; }
.disco-beams i { --angle: calc(var(--beam-index) * 30deg); position: absolute; left: -26px; top: 0; width: 52px; height: 95vh; transform-origin: 50% 0; transform: rotate(var(--angle)); clip-path: polygon(42% 0, 58% 0, 100% 100%, 0 100%); opacity: .28; background: linear-gradient(to bottom, hsla(calc(var(--beam-index) * 31), 100%, 72%, .9), transparent 78%); filter: blur(4px); }
.disco-floor { position: absolute; inset: 45% -20% -35%; transform: perspective(500px) rotateX(62deg); opacity: .28; background-image: linear-gradient(rgba(75,201,255,.7) 1px, transparent 1px), linear-gradient(90deg, rgba(230,72,255,.7) 1px, transparent 1px); background-size: 70px 70px; animation: floor-move 1.2s linear infinite; }
.sparkles i { position: absolute; width: 7px; height: 7px; border-radius: 50%; color: hsl(calc(var(--spark-index) * 47), 100%, 72%); background: currentColor; box-shadow: 0 0 16px currentColor; animation: sparkle 1.35s infinite alternate; animation-delay: calc(var(--spark-index) * -.09s); }
@keyframes ball-spin { to { rotate: 360deg; } } @keyframes ball-float { to { translate: 0 8px; } } @keyframes tile-flash { to { filter: brightness(1.8); } } @keyframes beams-spin { to { transform: rotate(360deg); } } @keyframes floor-move { to { background-position: 0 70px, 70px 0; } } @keyframes sparkle { from { opacity: .12; scale: .5; } to { opacity: 1; scale: 1.8; } } @keyframes disco-tint { 0%,100% { background-color: rgba(255,0,120,.08); } 33% { background-color: rgba(0,180,255,.1); } 66% { background-color: rgba(130,0,255,.1); } }

.main {
  padding: 26px;
  overflow-y: auto;
  background: radial-gradient(circle at 100% 0, rgba(73,157,239,.07), transparent 30%), #f3f7fc;
}
@media (max-width: 800px) { .aside { width: 78px !important; }.side-brand div, .menu-caption, .menu :deep(.el-menu-item span), .side-footer { display: none; }.side-brand { margin: 0; justify-content: center; }.menu { padding: 0 10px; }.menu :deep(.el-menu-item) { justify-content: center; padding: 0 !important; }.menu :deep(.el-menu-item .el-icon) { margin: 0; }.user-info { display: none; } }
</style>
