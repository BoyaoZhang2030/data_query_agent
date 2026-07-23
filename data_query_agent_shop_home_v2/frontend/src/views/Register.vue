<template>
  <main class="register-page">
    <button class="back" @click="$router.push('/')">← 返回首页</button>
    <el-card class="register-card">
      <div class="title"><small>CREATE ACCOUNT</small><h1>创建账号</h1><p>注册后将自动登录并进入数据工作台</p></div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="register">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" size="large" maxlength="50" placeholder="3-50 个字符" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" size="large" placeholder="name@example.com" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" size="large" type="password" show-password placeholder="至少 6 位" /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="form.confirmPassword" size="large" type="password" show-password placeholder="再次输入密码" /></el-form-item>
        <el-button class="submit" type="primary" :loading="loading" @click="register">注册并登录</el-button>
      </el-form>
      <div class="login-link">已有账号？<button @click="$router.push('/login')">返回登录</button></div>
    </el-card>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', email: '', password: '', confirmPassword: '' })
const validateConfirm = (_rule, value, callback) => value !== form.password ? callback(new Error('两次输入的密码不一致')) : callback()
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 50, message: '用户名长度应为 3-50 个字符', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }]
}
const register = async () => {
  if (!(await formRef.value?.validate().catch(() => false))) return
  loading.value = true
  try {
    const payload = { username: form.username.trim(), email: form.email.trim(), password: form.password }
    const registered = await authApi.register(payload)
    if (registered.code !== 200) throw new Error(registered.message || '注册失败')
    const loggedIn = await authApi.login({ username: payload.username, password: payload.password })
    if (loggedIn.code !== 200) throw new Error(loggedIn.message || '自动登录失败')
    localStorage.setItem('user', JSON.stringify(loggedIn.data))
    ElMessage.success('注册成功，欢迎使用')
    router.push('/shop')
  } catch (e) { ElMessage.error(e.response?.data?.message || e.message || '注册失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.register-page { min-height:100vh; display:grid; place-items:center; padding:40px 20px; position:relative; background:radial-gradient(circle at 10% 10%,#1a4774,#07182c 50%,#030d18) }.back { position:absolute; left:32px; top:28px; border:0; color:#9db3c8; background:transparent; cursor:pointer }.register-card { width:min(480px,100%); padding:18px; border-radius:24px }.title { margin-bottom:28px }.title small { color:#409eff; letter-spacing:2px }.title h1 { margin:8px 0; color:#17283a }.title p { margin:0; color:#909399 }.submit { width:100%; height:48px; margin-top:8px; border:0; border-radius:12px; background:linear-gradient(90deg,#358df1,#657bea) }.login-link { margin-top:22px; text-align:center; color:#8a99a8 }.login-link button { border:0; color:#409eff; background:transparent; cursor:pointer }
</style>
