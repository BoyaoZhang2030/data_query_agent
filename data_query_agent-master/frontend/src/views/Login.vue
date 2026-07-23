<template>
  <main class="login-page">
    <button class="back" @click="$router.push('/')">← 返回首页</button>
    <section class="login-shell">
      <div class="login-intro">
        <span class="mini-logo">BQ</span>
        <h1>欢迎回来</h1>
        <p>登录你的电商数据工作台，继续洞察每一笔经营数据。</p>
        <div class="intro-line"><span></span><small>安全连接 · 实时数据</small></div>
      </div>
      <div class="login-panel">
        <div class="panel-title"><small>ACCOUNT LOGIN</small><h2>账号登录</h2><p>请输入你的数据库账号信息</p></div>
        <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-position="top" @keyup.enter="login">
          <el-form-item label="用户名" prop="username"><el-input v-model="loginForm.username" size="large" placeholder="请输入用户名" clearable /></el-form-item>
          <el-form-item label="密码" prop="password"><el-input v-model="loginForm.password" size="large" type="password" placeholder="请输入密码" show-password /></el-form-item>
          <el-button class="login-button" type="primary" :loading="loading" @click="login">登录系统 <span>→</span></el-button>
        </el-form>
        <div class="demo-account">体验账号：<b>bf</b><span>/</span>密码：<b>123456</b></div>
        <div class="register-link">还没有账号？<button @click="$router.push('/register')">立即注册</button></div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const rules = { username: [{ required: true, message: '请输入用户名', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }] }
const login = async () => {
  if (!(await loginFormRef.value?.validate().catch(() => false))) return
  loading.value = true
  try {
    const response = await authApi.login(loginForm)
    if (response.code !== 200) return ElMessage.error(response.message || '用户名或密码错误')
    localStorage.setItem('user', JSON.stringify(response.data))
    ElMessage.success(`欢迎回来，${response.data.username}`)
    router.push('/dashboard')
  } catch (error) { ElMessage.error(error.response?.data?.message || '登录失败，请检查后端服务') }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: grid; place-items: center; position: relative; overflow: hidden; padding: 30px; background: radial-gradient(circle at 10% 10%, #1a4774, #07182c 48%, #030d18); font-family: Inter, "PingFang SC", sans-serif; }
.login-page::after { content: ''; position: absolute; width: 600px; height: 600px; right: -260px; bottom: -300px; border-radius: 50%; background: rgba(56,179,238,.1); }
.back { position: absolute; left: 34px; top: 30px; border: 0; color: #9db3c8; background: transparent; cursor: pointer; font-size: 14px; z-index: 2; }.back:hover { color: #72ddf5; }
.login-shell { width: min(960px, 100%); min-height: 570px; display: grid; grid-template-columns: .9fr 1.1fr; overflow: hidden; border: 1px solid rgba(150,210,255,.2); border-radius: 28px; background: rgba(8,27,48,.78); box-shadow: 0 40px 100px rgba(0,0,0,.45); backdrop-filter: blur(20px); position: relative; z-index: 1; }
.login-intro { padding: 65px 54px; display: flex; flex-direction: column; justify-content: center; color: white; background: linear-gradient(145deg, rgba(34,116,182,.65), rgba(16,54,94,.38)); }
.mini-logo { width: 54px; height: 54px; display: grid; place-items: center; border-radius: 17px; color: #071727; background: linear-gradient(135deg, #6ce8ff, #92a9ff); font-weight: 800; }.login-intro h1 { margin: 34px 0 16px; font-size: 46px; }.login-intro p { color: #b4c9da; line-height: 1.9; font-size: 16px; }.intro-line { margin-top: auto; display: flex; align-items: center; gap: 12px; color: #7f9bb3; }.intro-line span { width: 34px; height: 2px; background: #5edcf3; }
.login-panel { padding: 62px 68px; background: #f8fbff; }.panel-title small { color: #409eff; letter-spacing: 2px; }.panel-title h2 { margin: 10px 0 7px; color: #17283a; font-size: 32px; }.panel-title p { margin: 0 0 35px; color: #91a0ae; }
.login-button { width: 100%; height: 50px; margin-top: 8px; border-radius: 12px; font-size: 16px; background: linear-gradient(90deg, #358df1, #657bea); border: 0; }.login-button span { margin-left: 12px; }
.demo-account { margin-top: 22px; padding: 12px; text-align: center; color: #788999; border-radius: 10px; background: #edf5ff; font-size: 13px; }.demo-account span { margin: 0 10px; color: #bdc8d2; }.register-link { margin-top: 24px; text-align: center; color: #8a99a8; font-size: 14px; }.register-link button { border: 0; color: #409eff; background: transparent; cursor: pointer; }
@media (max-width: 760px) { .login-shell { grid-template-columns: 1fr; }.login-intro { display: none; }.login-panel { padding: 50px 30px; } }
</style>
