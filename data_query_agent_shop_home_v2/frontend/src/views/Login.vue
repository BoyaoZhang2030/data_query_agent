<template>
  <main class="login-page" :class="{ admin: adminMode }">
    <button class="back" @click="$router.push('/')">← 返回商城</button>
    <section class="login-shell">
      <div class="login-intro">
        <span class="mini-logo">{{ adminMode ? 'BI' : '橙' }}</span>
        <small>{{ adminMode ? 'ADMIN CONSOLE' : 'ORANGE SELECT' }}</small>
        <h1>{{ adminMode ? '管理系统' : '欢迎回来' }}</h1>
        <p>{{ adminMode ? '库存、订单与智能数据分析工作台，仅限管理员账号访问。' : '登录橙选商城，继续挑选喜欢的商品并管理你的订单。' }}</p>
        <div class="mode-switch">
          <button v-if="adminMode" @click="$router.push('/login')">切换到用户登录</button>
          <button v-else @click="$router.push('/login?mode=admin')">进入管理员系统</button>
        </div>
      </div>
      <div class="login-panel">
        <div class="panel-title"><small>{{ adminMode ? 'ADMIN LOGIN' : 'ACCOUNT LOGIN' }}</small><h2>{{ adminMode ? '管理员登录' : '用户登录' }}</h2><p>{{ adminMode ? '请输入管理员账号信息' : '登录后即可购物和查看订单' }}</p></div>
        <el-alert v-if="adminMode" title="该入口仅接受管理员账号" type="warning" :closable="false" show-icon />
        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-position="top" @keyup.enter="login">
          <el-form-item label="用户名" prop="username"><el-input v-model="loginForm.username" size="large" placeholder="请输入用户名" clearable /></el-form-item>
          <el-form-item label="密码" prop="password"><el-input v-model="loginForm.password" size="large" type="password" placeholder="请输入密码" show-password /></el-form-item>
          <el-button class="login-button" type="primary" :loading="loading" @click="login">{{ adminMode ? '进入管理系统' : '登录商城' }} <span>→</span></el-button>
        </el-form>
        <div v-if="adminMode" class="demo-account">管理员体验账号：<b>bf</b><span>/</span>密码：<b>123456</b></div>
        <div v-else class="register-link">还没有账号？<button @click="$router.push('/register')">免费注册</button></div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
const router = useRouter()
const route = useRoute()
const adminMode = computed(() => route.query.mode === 'admin')
const loginFormRef = ref()
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const login = async () => {
  if (!(await loginFormRef.value?.validate().catch(() => false))) return
  loading.value = true
  try {
    const response = await authApi.login(loginForm)
    if (response.code !== 200) return ElMessage.error(response.message || '用户名或密码错误')
    if (adminMode.value && response.data.role !== 'admin') {
      return ElMessage.error('该账号不是管理员，请从用户登录入口进入')
    }
    localStorage.setItem('user', JSON.stringify(response.data))
    ElMessage.success(`欢迎回来，${response.data.username}`)
    if (response.data.role === 'admin') router.push('/dashboard')
    else router.push(String(route.query.redirect || '/shop'))
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败，请检查后端服务')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page{min-height:100vh;display:grid;place-items:center;position:relative;overflow:hidden;padding:30px;background:radial-gradient(circle at 10% 10%,#ff9b42,#ff5a00 42%,#d93600);font-family:Inter,"PingFang SC",sans-serif}.login-page.admin{background:radial-gradient(circle at 10% 10%,#1a4774,#07182c 48%,#030d18)}.back{position:absolute;left:34px;top:30px;border:0;color:#fff;background:transparent;cursor:pointer;font-size:14px;z-index:2}.login-shell{width:min(960px,100%);min-height:570px;display:grid;grid-template-columns:.9fr 1.1fr;overflow:hidden;border:1px solid #ffffff40;border-radius:28px;background:#ffffff20;box-shadow:0 40px 100px #00000045;backdrop-filter:blur(20px);position:relative;z-index:1}.login-intro{padding:65px 54px;display:flex;flex-direction:column;justify-content:center;color:#fff;background:#6f1d0026}.admin .login-intro{background:#102f52aa}.mini-logo{width:54px;height:54px;display:grid;place-items:center;margin-bottom:20px;border-radius:17px;color:#ff5000;background:#fff;font-size:23px;font-weight:900}.admin .mini-logo{color:#071727;background:linear-gradient(135deg,#6ce8ff,#92a9ff)}.login-intro>small{letter-spacing:3px;opacity:.65}.login-intro h1{margin:18px 0 16px;font-size:46px}.login-intro p{color:#ffe4d2;line-height:1.9;font-size:16px}.admin .login-intro p{color:#b4c9da}.mode-switch{margin-top:45px}.mode-switch button{padding:10px 18px;border:1px solid #ffffff55;border-radius:18px;color:#fff;background:#ffffff12;cursor:pointer}.login-panel{padding:55px 68px;background:#fff}.panel-title small{color:#ff5000;letter-spacing:2px}.admin .panel-title small{color:#409eff}.panel-title h2{margin:10px 0 7px;color:#17283a;font-size:32px}.panel-title p{margin:0 0 25px;color:#91a0ae}.login-panel :deep(.el-alert){margin-bottom:18px}.login-button{width:100%;height:50px;margin-top:8px;border:0;border-radius:12px;font-size:16px;background:linear-gradient(90deg,#ff7a00,#ff4200)}.admin .login-button{background:linear-gradient(90deg,#358df1,#657bea)}.login-button span{margin-left:12px}.demo-account{margin-top:22px;padding:12px;text-align:center;color:#788999;border-radius:10px;background:#edf5ff;font-size:13px}.demo-account span{margin:0 10px}.register-link{margin-top:24px;text-align:center;color:#8a99a8}.register-link button{border:0;color:#ff5000;background:transparent;cursor:pointer}
@media(max-width:760px){.login-shell{grid-template-columns:1fr}.login-intro{display:none}.login-panel{padding:50px 30px}}
</style>
