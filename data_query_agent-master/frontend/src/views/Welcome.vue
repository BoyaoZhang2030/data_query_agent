<template>
  <main class="welcome-page">
    <div class="orb orb-one"></div>
    <div class="orb orb-two"></div>
    <nav class="welcome-nav">
      <div class="brand"><span class="brand-mark">BQ</span> 电商问数智能体</div>
      <span class="nav-tag">AI · DATA · COMMERCE</span>
    </nav>

    <section class="hero">
      <div class="hero-copy">
        <div class="eyebrow">新一代电商数据工作台</div>
        <h1><span class="main-word">问数</span><br><b>让数据驱动经营</b></h1>
        <p>从商品库存到订单销售，从自然语言问数到可视化洞察，一站式掌握你的电商经营状态。</p>
        <button class="enter-button" @click="enterSystem">
          <span>登录进入</span><span class="arrow">→</span>
        </button>
        <div class="feature-list">
          <span>自然语言查询</span><span>实时库存</span><span>销售分析</span>
        </div>
      </div>

      <div class="hero-visual">
        <div class="dashboard-preview">
          <div class="preview-top"><i></i><i></i><i></i><span>实时经营概览</span></div>
          <div class="metric-grid">
            <div class="metric"><small>今日销售额</small><strong>¥ 28,640</strong><em>+18.6%</em></div>
            <div class="metric"><small>订单数量</small><strong>1,286</strong><em>+9.2%</em></div>
          </div>
          <div class="chart-card">
            <div class="chart-title">销售趋势 <span>近 7 天</span></div>
            <div class="bars"><i v-for="height in bars" :key="height" :style="{ height: `${height}%` }"></i></div>
          </div>
          <div class="insight"><span class="pulse"></span><div><b>AI 洞察</b><p>数码电子类目销售额增长明显</p></div></div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const bars = [38, 54, 46, 70, 62, 82, 94]
const enterSystem = () => router.push('/login')
</script>

<style scoped>
.welcome-page { min-height: 100vh; position: relative; overflow: hidden; color: #eef6ff; background: radial-gradient(circle at 15% 20%, #173b68 0, #081a31 38%, #04101f 100%); font-family: Inter, "PingFang SC", sans-serif; }
.welcome-page::before { content: ''; position: absolute; inset: 0; opacity: .18; background-image: linear-gradient(rgba(121,190,255,.13) 1px, transparent 1px), linear-gradient(90deg, rgba(121,190,255,.13) 1px, transparent 1px); background-size: 52px 52px; mask-image: linear-gradient(to bottom, #000, transparent 85%); }
.welcome-nav { height: 84px; max-width: 1220px; margin: auto; padding: 0 28px; display: flex; align-items: center; justify-content: space-between; position: relative; z-index: 2; }
.brand { display: flex; align-items: center; gap: 12px; font-weight: 700; font-size: 18px; letter-spacing: .5px; }
.brand-mark { width: 38px; height: 38px; display: grid; place-items: center; border-radius: 12px; color: #061322; background: linear-gradient(135deg, #63e6ff, #7c9dff); box-shadow: 0 8px 25px rgba(73,191,255,.3); }
.nav-tag { color: #7f9bb8; font-size: 12px; letter-spacing: 3px; }
.hero { min-height: calc(100vh - 84px); max-width: 1220px; margin: auto; padding: 55px 28px 100px; display: grid; grid-template-columns: 1fr 1fr; gap: 80px; align-items: center; position: relative; z-index: 1; }
.eyebrow { display: inline-flex; padding: 8px 14px; border: 1px solid rgba(94,211,255,.28); border-radius: 999px; color: #71ddff; background: rgba(34,142,194,.1); font-size: 13px; letter-spacing: 2px; }
h1 { margin: 24px 0; font-size: clamp(54px, 6vw, 82px); line-height: 1.08; letter-spacing: -4px; }
h1 .main-word { color: transparent; background: linear-gradient(90deg, #5ee7ff, #6f9cff); background-clip: text; -webkit-background-clip: text; font-size: 1.35em; text-shadow: 0 16px 55px rgba(65,174,255,.18); }
h1 b { font-size: .58em; font-weight: 700; letter-spacing: -2px; color: #eef7ff; }
.hero-copy p { max-width: 580px; margin: 0 0 36px; color: #9bb0c6; font-size: 18px; line-height: 1.9; }
.enter-button { border: 0; padding: 0 8px 0 26px; height: 58px; border-radius: 16px; display: inline-flex; align-items: center; gap: 26px; color: #071526; font-size: 17px; font-weight: 700; cursor: pointer; background: linear-gradient(135deg, #65e8ff, #8ea8ff); box-shadow: 0 18px 45px rgba(68,192,255,.24); transition: transform .25s, box-shadow .25s; }
.enter-button:hover { transform: translateY(-3px); box-shadow: 0 24px 55px rgba(68,192,255,.35); }
.arrow { width: 42px; height: 42px; display: grid; place-items: center; border-radius: 12px; background: rgba(255,255,255,.38); font-size: 24px; }
.feature-list { display: flex; gap: 28px; margin-top: 32px; color: #718ba6; font-size: 13px; }
.feature-list span::before { content: '✓'; color: #5eddf6; margin-right: 7px; }
.hero-visual { perspective: 1200px; }
.dashboard-preview { padding: 20px; border: 1px solid rgba(135,207,255,.2); border-radius: 26px; background: linear-gradient(145deg, rgba(23,55,88,.88), rgba(8,25,45,.88)); box-shadow: 0 45px 100px rgba(0,0,0,.42); transform: rotateY(-5deg) rotateX(2deg); backdrop-filter: blur(18px); animation: float 5s ease-in-out infinite; }
.preview-top { height: 42px; display: flex; align-items: center; gap: 7px; color: #8da5bd; font-size: 13px; }
.preview-top i { width: 8px; height: 8px; border-radius: 50%; background: #31516d; }.preview-top span { margin-left: auto; }
.metric-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.metric, .chart-card, .insight { border: 1px solid rgba(130,190,235,.12); background: rgba(7,28,50,.68); border-radius: 17px; }
.metric { padding: 20px; }.metric small { color: #829ab1; }.metric strong { display: block; margin: 10px 0; font-size: 24px; }.metric em { color: #5ee5be; font-size: 12px; font-style: normal; }
.chart-card { margin-top: 14px; padding: 20px; }.chart-title { font-weight: 600; }.chart-title span { float: right; color: #7892aa; font-size: 12px; font-weight: 400; }
.bars { height: 170px; display: flex; align-items: flex-end; gap: 14px; padding-top: 20px; }.bars i { flex: 1; min-height: 20px; border-radius: 7px 7px 3px 3px; background: linear-gradient(to top, #3776df, #66e5f6); opacity: .9; }
.insight { margin-top: 14px; padding: 16px; display: flex; align-items: center; gap: 14px; }.insight p { margin: 4px 0 0; color: #829ab2; font-size: 13px; }.pulse { width: 38px; height: 38px; border-radius: 12px; background: radial-gradient(circle, #65e8ff 0 18%, rgba(101,232,255,.17) 20%); }
.orb { position: absolute; border-radius: 50%; filter: blur(2px); }.orb-one { width: 420px; height: 420px; right: -160px; top: 70px; background: rgba(43,108,209,.12); }.orb-two { width: 260px; height: 260px; left: 34%; bottom: -150px; background: rgba(62,219,224,.1); }
@keyframes float { 50% { transform: rotateY(-3deg) rotateX(1deg) translateY(-10px); } }
@media (max-width: 850px) { .hero { grid-template-columns: 1fr; padding-top: 30px; }.hero-visual { display: none; } h1 { letter-spacing: -2px; }.nav-tag { display: none; } }
</style>
