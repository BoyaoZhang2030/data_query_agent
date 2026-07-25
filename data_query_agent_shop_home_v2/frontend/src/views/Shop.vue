<template>
  <div class="shop-page">
    <header class="topbar">
      <div class="top-inner">
        <span>欢迎来到淘气宝贝</span>
        <nav>
          <template v-if="user.id">
            <span>Hi，{{ user.username }}</span>
            <button v-if="user.role === 'admin'" class="admin-link" @click="$router.push('/dashboard')">进入后台仓库</button>
            <button v-else @click="$router.push('/shop/orders')">我的订单</button>
            <button @click="logout">退出登录</button>
          </template>
          <template v-else>
            <button class="login-link" @click="$router.push('/login')">亲，请登录</button>
            <button @click="$router.push('/register')">免费注册</button>
            <button class="admin-link" @click="$router.push('/login?mode=admin')">管理员后台登录</button>
          </template>
        </nav>
      </div>
    </header>

    <section class="main-header">
      <button class="brand-dog" type="button" aria-label="cute dog" @click="dropPoop"><span>🐶</span><small>点我</small></button>
      <div class="brand" @click="resetFilters"><span>淘</span><div><b>淘气宝贝</b><small>品质好物 · 随心购</small></div></div>
      <div class="search">
        <el-input v-model="keyword" size="large" clearable placeholder="搜索商品名称或描述" @keyup.enter="applySearch" />
        <button @click="applySearch">搜索</button>
      </div>
      <button class="cart-button" @click="cartVisible = true">🛒 购物车 <em>{{ cartCount }}</em></button>
    </section>

    <main class="shop-content">
      <section class="hero">
        <aside class="category-panel">
          <h3>商品分类</h3>
          <button :class="{ active: selectedCategory === null }" @click="selectCategory(null)">全部商品 <span>›</span></button>
          <button v-for="category in rootCategories" :key="category.id" :class="{ active: selectedCategory === category.id }" @click="selectCategory(category.id)">
            {{ category.name }} <span>›</span>
          </button>
        </aside>
        <div class="banner">
          <div><small>NAUGHTY BABY</small><h1>把喜欢的生活<br>装进购物车</h1><p>库存数据实时同步，放心选购每一件好物</p><button @click="scrollToProducts">立即选购</button></div>
          <div class="banner-art"><span>🛍️</span><i></i><i></i><i></i></div>
        </div>
        <div class="service-card">
          <h3>淘气宝贝保障</h3>
          <div><span>✓</span><p><b>库存透明</b><small>实时展示剩余库存</small></p></div>
          <div><span>✓</span><p><b>价格清晰</b><small>所有商品明码标价</small></p></div>
          <div><span>✓</span><p><b>快捷下单</b><small>一站式购物体验</small></p></div>
        </div>
      </section>

      <section ref="productSection" class="products-section">
        <div class="section-title">
          <div><small>GOOD THINGS</small><h2>{{ selectedCategoryName }}</h2></div>
          <div class="sorts"><button :class="{ active: sortBy === 'default' }" @click="sortBy = 'default'">综合</button><button :class="{ active: sortBy === 'priceAsc' }" @click="sortBy = 'priceAsc'">价格升序</button><button :class="{ active: sortBy === 'stock' }" @click="sortBy = 'stock'">库存优先</button><button :class="{ active: sortBy === 'rating' }" @click="sortBy = 'rating'">评分优先</button></div>
        </div>

        <div v-loading="loading" class="product-grid">
          <article v-for="(product, index) in displayedProducts" :key="product.id" class="product-card" tabindex="0" @click="openProductDetail(product)" @keydown.enter="openProductDetail(product)">
            <div class="product-image" :style="{ '--tone': tones[index % tones.length] }">
              <img v-if="productImages(product).length" :src="productImages(product)[0]" :alt="product.name" />
              <span v-else>{{ productEmoji(product.name) }}</span>
              <i v-if="product.stock <= 0">已售罄</i>
              <i v-else-if="product.stock < 20" class="warning">仅剩 {{ product.stock }} 件</i>
            </div>
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p>{{ product.description || '精选品质商品，库存实时同步' }}</p>
              <div class="stock-line"><span>库存 {{ product.stock }} 件</span><span>{{ categoryName(product.categoryId) }}</span></div><div class="rating-line"><el-rate :model-value="ratingSummary(product).average" disabled allow-half size="small" /><b>{{ ratingSummary(product).average.toFixed(1) }}</b><small>（{{ ratingSummary(product).count }}条评价）</small></div>
              <div class="buy-line"><strong><small>¥</small>{{ formatPrice(product.price) }}</strong><el-button type="primary" round :disabled="product.stock <= 0" @click.stop="addToCart(product)">加入购物车</el-button></div>
            </div>
          </article>
        </div>
        <el-empty v-if="!loading && !displayedProducts.length" description="没有找到符合条件的商品" />
      </section>
    </main>

    <div class="poop-layer" aria-hidden="true">
      <transition-group name="poop-pop">
        <span v-for="poop in poops" :key="poop.id" class="dog-poop" :style="poopStyle(poop)">💩</span>
      </transition-group>
    </div>

    <el-dialog v-model="detailVisible" :title="selectedProduct?.name || '商品详情'" width="min(900px, 94vw)" class="product-detail-dialog" append-to-body>
      <div v-if="selectedProduct" class="product-detail">
        <div class="detail-gallery">
          <el-carousel v-if="selectedProductImages.length" height="360px" indicator-position="outside" arrow="always">
            <el-carousel-item v-for="(image, index) in selectedProductImages" :key="image">
              <el-image class="detail-image" :src="image" :alt="`${selectedProduct.name} 图片 ${index + 1}`" fit="contain" :preview-src-list="selectedProductImages" :initial-index="index" preview-teleported />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="detail-placeholder">{{ productEmoji(selectedProduct.name) }}<small>图片准备中</small></div>
          <p v-if="selectedProductImages.length" class="preview-tip">点击图片可放大预览，左右滑动可切换</p>
        </div>
        <div class="detail-copy">
          <el-tag effect="plain">{{ categoryName(selectedProduct.categoryId) }}</el-tag>
          <div class="detail-rating"><el-rate :model-value="ratingSummary(selectedProduct).average" disabled allow-half /><strong>{{ ratingSummary(selectedProduct).average.toFixed(1) }}</strong><span>{{ ratingSummary(selectedProduct).count }}条评价</span></div>
          <h2>{{ selectedProduct.name }}</h2>
          <strong class="detail-price">¥{{ formatPrice(selectedProduct.price) }}</strong>
          <div class="detail-stock">当前库存：{{ selectedProduct.stock }} 件</div>
          <h3>商品介绍</h3>
          <p class="detail-description">{{ selectedProduct.description || '暂无详细文字描述，后续可在商品管理中补充。' }}</p>
          <el-button type="primary" size="large" round :disabled="selectedProduct.stock <= 0" @click="addToCart(selectedProduct)">加入购物车</el-button>
        </div>
      </div>
      <section class="comments-section">
        <div class="comments-heading"><h3>用户评价</h3><span>真实体验分享</span></div>
        <div class="comment-editor"><el-rate v-model="commentScore" /><el-input v-model="commentDraft" maxlength="100" show-word-limit placeholder="说说你的使用感受吧" @keyup.enter="submitComment" /><el-button type="primary" @click="submitComment">发表评论</el-button></div>
        <div class="comment-list"><article v-for="comment in selectedProductComments" :key="comment.id" class="comment-item"><div><b>{{ comment.user }}</b><el-rate :model-value="comment.score" disabled size="small" /><el-button v-if="isAdmin" class="comment-delete" link type="danger" @click.stop="deleteComment(comment.id)">删除</el-button></div><p>{{ comment.text }}</p></article></div>
      </section>
    </el-dialog>

    <el-drawer v-model="cartVisible" title="我的购物车" size="min(520px, 92vw)">
      <div v-if="cart.length" class="cart-list">
        <div v-for="item in cart" :key="item.product.id" class="cart-item">
          <div class="cart-thumb">{{ productEmoji(item.product.name) }}</div>
          <div class="cart-meta"><b>{{ item.product.name }}</b><small>¥{{ formatPrice(item.product.price) }} · 库存 {{ item.product.stock }}</small></div>
          <el-input-number v-model="item.quantity" :min="1" :max="item.product.stock" size="small" @change="saveCart" />
          <button class="remove" @click="removeFromCart(item.product.id)">×</button>
        </div>
      </div>
      <el-empty v-else description="购物车还是空的" />
      <template #footer>
        <div class="cart-footer"><div><span>共 {{ cartCount }} 件</span><strong>合计 ¥{{ formatPrice(cartTotal) }}</strong></div><el-button type="primary" size="large" :disabled="!cart.length" @click="checkoutVisible = true">去结算</el-button></div>
      </template>
    </el-drawer>

    <el-dialog v-model="checkoutVisible" title="确认订单" width="min(560px, 94vw)">
      <div class="checkout-summary"><span>共 {{ cartCount }} 件商品</span><b>¥{{ formatPrice(cartTotal) }}</b></div>
      <el-form ref="checkoutFormRef" :model="checkoutForm" :rules="checkoutRules" label-position="top">
        <el-form-item label="收货地址" prop="address"><el-input v-model="checkoutForm.address" type="textarea" :rows="3" placeholder="请输入详细收货地址" /></el-form-item>
        <el-form-item label="支付方式" prop="payment"><el-radio-group v-model="checkoutForm.payment"><el-radio label="支付宝">支付宝</el-radio><el-radio label="微信支付">微信支付</el-radio><el-radio label="货到付款">货到付款</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="checkoutVisible = false">取消</el-button><el-button type="primary" :loading="submitting" @click="submitOrder">提交订单</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { dataApi } from '../api'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = user.role === 'admin'
const products = ref([])
const categories = ref([])
const cart = ref([])
const keyword = ref('')
const appliedKeyword = ref('')
const selectedCategory = ref(null)
const sortBy = ref('default')
const loading = ref(false)
const submitting = ref(false)
const cartVisible = ref(false)
const checkoutVisible = ref(false)
const detailVisible = ref(false)
const selectedProduct = ref(null)
const imageManifest = ref({})
const commentMap = ref({})
const commentDraft = ref('')
const commentScore = ref(5)
const poops = ref([])
const poopTimers = new Set()
let poopSequence = 0
const checkoutFormRef = ref()
const productSection = ref()
const checkoutForm = reactive({ address: '', payment: '支付宝' })
const checkoutRules = { address: [{ required: true, message: '请输入收货地址', trigger: 'blur' }], payment: [{ required: true, message: '请选择支付方式', trigger: 'change' }] }
const tones = ['#edf5f2', '#f4eac5', '#e2efeb', '#fbf7e9', '#dcebe7', '#f0ead4']
const cartKey = 'shop-cart'

const rootCategories = computed(() => categories.value.filter(c => !c.parentId))
const selectedCategoryName = computed(() => selectedCategory.value ? categoryName(selectedCategory.value) : (appliedKeyword.value ? `“${appliedKeyword.value}”的搜索结果` : '猜你喜欢'))
const childCategoryIds = computed(() => selectedCategory.value === null ? [] : categories.value.filter(c => c.parentId === selectedCategory.value).map(c => c.id))
const displayedProducts = computed(() => {
  const ids = [selectedCategory.value, ...childCategoryIds.value]
  let list = products.value.filter(p => selectedCategory.value === null || ids.includes(p.categoryId))
  if (appliedKeyword.value) {
    const terms = appliedKeyword.value.split(/\s+/).map(normalizeText).filter(Boolean)
    list = list.filter(product => {
      const searchable = normalizeText(`${product.name} ${product.description || ''} ${categoryName(product.categoryId)} ${product.id}`)
      return terms.every(term => fuzzyMatch(searchable, term))
    })
  }
  if (sortBy.value === 'priceAsc') list = [...list].sort((a, b) => Number(a.price) - Number(b.price))
  if (sortBy.value === 'stock') list = [...list].sort((a, b) => b.stock - a.stock)
  if (sortBy.value === 'rating') list = [...list].sort((a, b) => ratingSummary(b).average - ratingSummary(a).average)
  return list
})
const cartCount = computed(() => cart.value.reduce((sum, item) => sum + item.quantity, 0))
const cartTotal = computed(() => cart.value.reduce((sum, item) => sum + Number(item.product.price) * item.quantity, 0))

const loadData = async () => {
  loading.value = true
  try {
    const [productResponse, categoryResponse] = await Promise.all([dataApi.getProducts(), dataApi.getCategories()])
    products.value = productResponse.data || []
    categories.value = categoryResponse.data || []
    ensureComments()
    const saved = JSON.parse(localStorage.getItem(cartKey) || '[]')
    cart.value = saved.map(item => ({ product: products.value.find(p => p.id === item.productId), quantity: item.quantity })).filter(item => item.product && item.product.stock > 0)
    saveCart()
  } catch (error) { ElMessage.error('商城数据加载失败，请确认后端已启动') }
  finally { loading.value = false }
}
const categoryName = id => categories.value.find(c => c.id === id)?.name || '精选好物'
const formatPrice = value => Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const productEmoji = name => /手机|电脑|数码|耳机/.test(name) ? '📱' : /衣|鞋|包|服/.test(name) ? '👕' : /食品|水果|零食/.test(name) ? '🍊' : /运动|户外/.test(name) ? '🏃' : /家|床|桌/.test(name) ? '🏠' : '🎁'
const normalizeText = value => String(value || '').toLocaleLowerCase().replace(/[\s\-_，。,.、/\\]+/g, '')
const fuzzyMatch = (text, query) => {
  if (!query) return true
  if (text.includes(query)) return true
  let queryIndex = 0
  for (const character of text) {
    if (character === query[queryIndex]) queryIndex += 1
    if (queryIndex === query.length) return true
  }
  return false
}
const productImages = product => (imageManifest.value[String(product?.id)] || []).map(file => `/product-images/${encodeURIComponent(product.id)}/${String(file).split('/').map(encodeURIComponent).join('/')}`)
const selectedProductImages = computed(() => productImages(selectedProduct.value))
const selectedProductComments = computed(() => selectedProduct.value ? (commentMap.value[String(selectedProduct.value.id)] || []) : [])
const commentStorageKey = "shop-comments"
const seedTexts = ["性价比不错，使用起来很顺手。", "包装很仔细，和描述基本一致。", "体验超出预期，会推荐给朋友。", "整体还可以，细节再优化就更好了。"]
const buildComments = product => {
  const id = Number(product.id) || 0
  const count = 2 + (id % 3)
  const scoreSets = [[5, 2], [4, 1, 5], [5, 3, 1, 4]]
  const scores = scoreSets[id % scoreSets.length]
  return Array.from({ length: count }, (_, index) => ({ id: String(id) + "-" + index, user: ["小鹿", "阿布", "晴天", "匿名用户"][index], score: scores[index % scores.length], text: seedTexts[(id + index) % seedTexts.length] }))
}
const persistComments = () => localStorage.setItem(commentStorageKey, JSON.stringify(commentMap.value))
const ensureComments = () => {
  const saved = JSON.parse(localStorage.getItem(commentStorageKey) || "{}")
  const next = { ...saved }
  products.value.forEach(product => { if (!Object.prototype.hasOwnProperty.call(next, String(product.id))) next[String(product.id)] = buildComments(product) })
  commentMap.value = next
  persistComments()
}
const ratingSummary = product => {
  const comments = commentMap.value[String(product?.id)] || []
  const count = comments.length
  return { count, average: count ? comments.reduce((sum, comment) => sum + Number(comment.score), 0) / count : 0 }
}
const submitComment = () => {
  if (!selectedProduct.value || !commentDraft.value.trim()) return ElMessage.info("请先写下你的评价")
  const key = String(selectedProduct.value.id)
  const comments = commentMap.value[key] || []
  comments.unshift({ id: key + "-" + Date.now(), user: user.username || "匿名用户", score: commentScore.value || 5, text: commentDraft.value.trim() })
  commentMap.value = { ...commentMap.value, [key]: comments }
  persistComments()
  commentDraft.value = ""; commentScore.value = 5
  ElMessage.success("评价发布成功")
}
const deleteComment = commentId => {
  if (!isAdmin || !selectedProduct.value) return
  const key = String(selectedProduct.value.id)
  commentMap.value = { ...commentMap.value, [key]: (commentMap.value[key] || []).filter(comment => comment.id !== commentId) }
  persistComments()
  ElMessage.success("评价已删除")
}
const openProductDetail = product => {
  selectedProduct.value = product
  detailVisible.value = true
}
let poopAnimationFrame = 0
let lastPoopFrame = 0
const poopStyle = poop => ({ transform: "translate3d(" + poop.x + "px," + poop.y + "px,0) rotate(" + poop.rotate + "deg)" })
const animatePoops = time => {
  const step = Math.min((time - (lastPoopFrame || time)) / 16.67, 2)
  lastPoopFrame = time
  const maxX = window.innerWidth - 52
  const maxY = window.innerHeight - 52
  poops.value.forEach(poop => {
    if (poop.y < maxY - 6) poop.vx += (Math.random() - 0.5) * 0.12 * step
    poop.vy += 0.42 * step
    poop.x += poop.vx * step
    poop.y += poop.vy * step
    poop.rotate += poop.spin * step
    if (poop.x <= 0 || poop.x >= maxX) { poop.x = Math.max(0, Math.min(maxX, poop.x)); poop.vx *= -1 }
    if (poop.y <= 0) { poop.y = 0; poop.vy = Math.abs(poop.vy) }
    if (poop.y >= maxY) { poop.y = maxY; poop.vy *= -1 }
  })
  const collisionSize = 48
  for (let i = 0; i < poops.value.length; i += 1) {
    for (let j = i + 1; j < poops.value.length; j += 1) {
      const a = poops.value[i]
      const b = poops.value[j]
      const dx = b.x - a.x
      const dy = b.y - a.y
      const distance = Math.sqrt(dx * dx + dy * dy) || 0.01
      if (distance >= collisionSize) continue
      const nx = dx / distance
      const ny = dy / distance
      const overlap = (collisionSize - distance) / 2
      a.x -= nx * overlap; a.y -= ny * overlap
      b.x += nx * overlap; b.y += ny * overlap
      const relativeSpeed = (b.vx - a.vx) * nx + (b.vy - a.vy) * ny
      if (relativeSpeed < 0) {
        a.vx += relativeSpeed * nx; a.vy += relativeSpeed * ny
        b.vx -= relativeSpeed * nx; b.vy -= relativeSpeed * ny
      }
    }
  }
  if (poops.value.length) poopAnimationFrame = window.requestAnimationFrame(animatePoops)
  else { poopAnimationFrame = 0; lastPoopFrame = 0 }
}
const dropPoop = event => {
  const rect = event.currentTarget.getBoundingClientRect()
  const id = ++poopSequence
  poops.value.push({ id, x: rect.left + rect.width * 0.5 - 24, y: rect.top + rect.height * 0.5 - 24, vx: 0, vy: 0, rotate: Math.random() * 360, spin: -8 + Math.random() * 16 })
  if (!poopAnimationFrame) poopAnimationFrame = window.requestAnimationFrame(animatePoops)
  const timer = window.setTimeout(() => {
    poops.value = poops.value.filter(poop => poop.id !== id)
    poopTimers.delete(timer)
  }, 20000)
  poopTimers.add(timer)
}
const saveCart = () => localStorage.setItem(cartKey, JSON.stringify(cart.value.map(item => ({ productId: item.product.id, quantity: item.quantity }))))
watch(cart, saveCart, { deep: true })
const addToCart = product => {
  const item = cart.value.find(item => item.product.id === product.id)
  if (item) item.quantity = Math.min(item.quantity + 1, product.stock)
  else cart.value.push({ product, quantity: 1 })
  ElMessage.success(`${product.name} 已加入购物车`)
}
const removeFromCart = id => { cart.value = cart.value.filter(item => item.product.id !== id) }
const applySearch = () => { appliedKeyword.value = keyword.value.trim(); selectedCategory.value = null; scrollToProducts() }
const selectCategory = id => { selectedCategory.value = id; appliedKeyword.value = ''; keyword.value = ''; scrollToProducts() }
const resetFilters = () => { keyword.value = ''; appliedKeyword.value = ''; selectedCategory.value = null; router.push(user.id ? '/shop' : '/') }
const scrollToProducts = () => productSection.value?.scrollIntoView({ behavior: 'smooth' })
const submitOrder = async () => {
  if (!user.id) {
    ElMessage.info('登录后才能提交订单')
    return router.push('/login?redirect=/shop')
  }
  if (!(await checkoutFormRef.value?.validate().catch(() => false))) return
  const invalid = cart.value.find(item => item.quantity > item.product.stock)
  if (invalid) return ElMessage.error(`${invalid.product.name} 库存不足，请调整数量`)
  submitting.value = true
  try {
    const response = await dataApi.createOrder({
      userId: user.id, status: '待付款', orderType: 'SALES',
      remark: `${checkoutForm.address}｜支付方式：${checkoutForm.payment}`,
      items: cart.value.map(item => ({ productId: item.product.id, quantity: item.quantity }))
    })
    if (response.code !== 200) throw new Error(response.message || '下单失败')
    cart.value = []; saveCart(); checkoutVisible.value = false; cartVisible.value = false
    ElMessage.success('订单提交成功')
    router.push('/shop/orders')
  } catch (error) { ElMessage.error(error.response?.data?.message || error.message || '订单提交失败') }
  finally { submitting.value = false }
}
const logout = () => { localStorage.removeItem('user'); router.push('/') }
onMounted(async () => {
  await Promise.all([
    loadData(),
    fetch('/product-images/manifest.json')
      .then(response => response.ok ? response.json() : {})
      .then(data => { imageManifest.value = data || {} })
      .catch(() => { imageManifest.value = {} })
  ])
})
onBeforeUnmount(() => {
  poopTimers.forEach(timer => window.clearTimeout(timer))
  if (poopAnimationFrame) window.cancelAnimationFrame(poopAnimationFrame)
})
</script>

<style scoped>
.shop-page{min-height:100vh;color:#292929;background:#f6f6f6;font-family:Inter,"PingFang SC",sans-serif}.topbar{height:36px;color:#666;background:#f3f3f3;border-bottom:1px solid #e8e8e8}.top-inner{max-width:1240px;height:100%;margin:auto;padding:0 20px;display:flex;align-items:center;justify-content:space-between;font-size:12px}.top-inner nav{display:flex;gap:18px;align-items:center}.top-inner button{border:0;color:#666;background:none;cursor:pointer}.top-inner button:hover{color:#ff5000}.main-header{max-width:1240px;position:relative;height:112px;margin:auto;padding:0 20px;display:flex;align-items:center;gap:55px;background:#fff}.brand{display:flex;align-items:center;gap:10px;cursor:pointer}.brand>span{width:54px;height:54px;display:grid;place-items:center;color:#fff;border-radius:18px;background:linear-gradient(135deg,#ff7800,#ff3d00);font-size:28px;font-weight:900;box-shadow:0 10px 25px #ff6a0030}.brand b,.brand small{display:block}.brand b{color:#ff5000;font-size:25px}.brand small{margin-top:3px;color:#999;font-size:10px}.search{height:48px;display:flex;flex:1;max-width:650px;padding:3px;border:2px solid #ff5000;border-radius:24px;background:#fff}.search :deep(.el-input__wrapper){box-shadow:none}.search button{width:92px;border:0;color:#fff;border-radius:22px;background:linear-gradient(90deg,#ff7a00,#ff4000);font-size:16px;cursor:pointer}.cart-button{margin-left:auto;padding:13px 18px;border:1px solid #eee;border-radius:22px;color:#555;background:#fff;cursor:pointer}.cart-button em{display:inline-grid;min-width:21px;height:21px;margin-left:5px;place-items:center;color:#fff;border-radius:11px;background:#ff5000;font-style:normal}.shop-content{max-width:1240px;margin:auto;padding:0 20px 50px}.hero{display:grid;grid-template-columns:220px 1fr 220px;gap:14px}.category-panel,.service-card{height:350px;padding:20px;border-radius:14px;background:#fff}.category-panel h3,.service-card h3{margin:0 0 14px}.category-panel button{width:100%;height:41px;padding:0 8px;border:0;display:flex;justify-content:space-between;align-items:center;border-radius:8px;color:#555;background:none;cursor:pointer}.category-panel button:hover,.category-panel button.active{color:#ff5000;background:#fff3ea}.banner{height:350px;padding:48px;display:flex;justify-content:space-between;overflow:hidden;position:relative;border-radius:14px;color:#fff;background:linear-gradient(120deg,#ff7d19,#ff4200)}.banner small{letter-spacing:4px;opacity:.7}.banner h1{margin:15px 0 10px;font-size:40px;line-height:1.25}.banner p{opacity:.85}.banner button{margin-top:24px;padding:12px 25px;border:0;border-radius:22px;color:#ff5000;background:#fff;font-weight:700;cursor:pointer}.banner-art{width:200px;display:grid;place-items:center;position:relative}.banner-art span{z-index:2;font-size:95px;filter:drop-shadow(0 22px 18px #ad240060)}.banner-art i{position:absolute;border:1px solid #ffffff50;border-radius:50%}.banner-art i:nth-of-type(1){width:190px;height:190px}.banner-art i:nth-of-type(2){width:250px;height:250px}.banner-art i:nth-of-type(3){width:320px;height:320px}.service-card div{display:flex;gap:12px;align-items:center;padding:15px 0;border-bottom:1px solid #f2f2f2}.service-card div>span{width:30px;height:30px;display:grid;place-items:center;color:#fff;border-radius:50%;background:#ff6a00}.service-card p,.service-card b,.service-card small{display:block;margin:0}.service-card small{margin-top:4px;color:#999}.products-section{margin-top:28px;padding:26px;border-radius:16px;background:#fff}.section-title{margin-bottom:20px;display:flex;justify-content:space-between;align-items:end}.section-title small{color:#ff5000;letter-spacing:2px}.section-title h2{margin:5px 0 0;font-size:27px}.sorts{display:flex;gap:8px}.sorts button{padding:8px 13px;border:1px solid #eee;border-radius:16px;background:#fff;cursor:pointer}.sorts button.active{color:#ff5000;border-color:#ffb38d;background:#fff5ef}.product-grid{min-height:220px;display:grid;grid-template-columns:repeat(4,1fr);gap:18px}.product-card{overflow:hidden;border:1px solid #f0f0f0;border-radius:14px;background:#fff;transition:.25s}.product-card:hover{transform:translateY(-5px);border-color:#ffb998;box-shadow:0 15px 38px #aa5a1b18}.product-image{height:185px;display:grid;place-items:center;position:relative;background:var(--tone)}.product-image>span{font-size:76px;filter:drop-shadow(0 12px 12px #00000016)}.product-image i{position:absolute;right:10px;top:10px;padding:5px 9px;border-radius:10px;color:#fff;background:#999;font-size:11px;font-style:normal}.product-image i.warning{background:#ff5000}.product-info{padding:16px}.product-info h3{height:24px;margin:0;overflow:hidden;white-space:nowrap;text-overflow:ellipsis}.product-info p{height:38px;margin:7px 0;color:#999;overflow:hidden;font-size:12px;line-height:1.6}.stock-line{display:flex;justify-content:space-between;color:#aaa;font-size:11px}.buy-line{margin-top:15px;display:flex;justify-content:space-between;align-items:center}.buy-line strong{color:#ff5000;font-size:22px}.buy-line strong small{font-size:13px}.buy-line :deep(.el-button--primary){border-color:#ff5000;background:#ff5000}.cart-list{display:grid;gap:14px}.cart-item{display:grid;grid-template-columns:54px 1fr auto 24px;gap:12px;align-items:center;padding-bottom:14px;border-bottom:1px solid #eee}.cart-thumb{width:54px;height:54px;display:grid;place-items:center;border-radius:10px;background:#fff3ea;font-size:28px}.cart-meta b,.cart-meta small{display:block}.cart-meta small{margin-top:6px;color:#999}.remove{border:0;color:#aaa;background:none;font-size:20px;cursor:pointer}.cart-footer{display:flex;justify-content:space-between;align-items:center}.cart-footer div span,.cart-footer div strong{display:block}.cart-footer strong{margin-top:4px;color:#ff5000;font-size:20px}.cart-footer :deep(.el-button--primary){border-color:#ff5000;background:#ff5000}.checkout-summary{margin-bottom:20px;padding:16px;display:flex;justify-content:space-between;border-radius:10px;background:#fff3ea}.checkout-summary b{color:#ff5000;font-size:20px}
.top-inner .login-link{color:#ff5000}.top-inner .admin-link{padding-left:16px;border-left:1px solid #ddd;color:#333;font-weight:700}
.product-image>img{width:100%;height:100%;object-fit:cover}.product-detail{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(260px,.85fr);gap:30px}.detail-gallery{min-width:0}.detail-image{width:100%;height:360px;border-radius:14px;background:#f4eac5;cursor:zoom-in}.detail-placeholder{height:360px;display:grid;place-items:center;align-content:center;gap:16px;border-radius:14px;background:linear-gradient(145deg,#f4eac5,#dcebe7);font-size:110px}.detail-placeholder small{color:#6e827d;font-size:14px}.preview-tip{margin:12px 0 0;text-align:center;color:#82928e;font-size:12px}.detail-copy{padding:8px 4px}.detail-copy h2{margin:16px 0 10px;color:#24413c;font-size:28px}.detail-price{display:block;margin-bottom:16px;color:#3f786e;font-size:30px}.detail-stock{padding:11px 14px;border-radius:10px;color:#3f786e;background:#edf5f2}.detail-copy h3{margin:24px 0 10px}.detail-description{max-height:250px;margin:0 0 25px;overflow:auto;white-space:pre-wrap;color:#596d68;line-height:1.8}
@media(max-width:720px){.product-detail{grid-template-columns:1fr}.detail-image,.detail-placeholder{height:280px}.detail-placeholder{font-size:82px}}
@media(max-width:1000px){.hero{grid-template-columns:190px 1fr}.service-card{display:none}.product-grid{grid-template-columns:repeat(3,1fr)}}@media(max-width:720px){.main-header{height:auto;padding-top:18px;padding-bottom:18px;flex-wrap:wrap;gap:14px}.search{order:3;flex-basis:100%}.hero{grid-template-columns:1fr}.category-panel{height:auto;display:flex;overflow:auto}.category-panel h3{display:none}.category-panel button{min-width:100px}.banner{height:280px;padding:32px}.banner h1{font-size:30px}.banner-art{display:none}.product-grid{grid-template-columns:repeat(2,1fr)}.products-section{padding:15px}.section-title{align-items:flex-start;flex-direction:column;gap:14px}}@media(max-width:470px){.product-grid{grid-template-columns:1fr}.top-inner>span{display:none}.main-header{gap:8px}.brand small{display:none}.cart-button{padding:10px}}
.brand-dog{width:66px;height:66px;position:absolute;left:-64px;top:23px;padding:3px;display:grid;place-items:center;align-content:center;border:2px solid #f4eac5;border-radius:22px;color:#315f56;background:linear-gradient(145deg,#fffdf6,#f4eac5);box-shadow:0 10px 25px rgba(49,95,86,.22);cursor:pointer;transition:transform .2s,box-shadow .2s}.brand-dog:hover{transform:translateY(-4px) rotate(-4deg);box-shadow:0 15px 30px rgba(49,95,86,.28)}.brand-dog:active{transform:scale(.91)}.brand-dog span{font-size:37px;line-height:1}.brand-dog small{margin-top:2px;color:#549688;font-size:10px;font-weight:800}.poop-layer{position:fixed;inset:0;z-index:60;overflow:hidden;pointer-events:none}.dog-poop{width:48px;height:48px;position:absolute;left:0;top:0;display:grid;place-items:center;font-size:43px;will-change:transform;filter:drop-shadow(0 5px 4px rgba(55,35,20,.18));animation:poop-appear .32s cubic-bezier(.2,1.6,.45,1)}.poop-pop-leave-active{transition:opacity .35s,scale .35s}.poop-pop-leave-to{opacity:0;scale:.45}@keyframes poop-appear{0%{opacity:0;scale:.2}70%{opacity:1;scale:1.2}100%{scale:1}}
@media(max-width:720px){.brand-dog{width:54px;height:54px;left:8px;top:18px;border-radius:18px}.brand-dog span{font-size:31px}.brand{margin-left:62px}.main-header{gap:10px}}
.brand-dog{border:0;background:transparent;box-shadow:none;width:54px;height:64px;padding:0}.brand-dog:hover{box-shadow:none}.brand-dog small{display:none}.brand-dog span{font-size:48px;filter:drop-shadow(0 5px 5px rgba(49,95,86,.18))}.rating-line{display:flex;align-items:center;gap:5px;margin-top:8px;color:#549688;font-size:12px}.rating-line .el-rate{height:18px}.rating-line .el-rate__icon{font-size:14px}.rating-line b{font-size:13px}.rating-line small{color:#8a9b97}.detail-rating{display:flex;align-items:center;gap:10px;margin:12px 0 2px;color:#71827e}.detail-rating .el-rate{height:22px}.detail-rating strong{color:#3f786e;font-size:20px}.comments-section{margin-top:28px;padding-top:22px;border-top:1px solid #e5eee9}.comments-heading{display:flex;align-items:baseline;gap:10px}.comments-heading h3{margin:0;color:#24413c}.comments-heading span{color:#94a6a1;font-size:12px}.comment-editor{display:flex;align-items:center;gap:12px;margin:16px 0}.comment-editor .el-input{flex:1}.comment-list{display:grid;gap:10px;max-height:230px;overflow:auto}.comment-item{padding:12px 14px;border-radius:10px;background:#fbf7e9}.comment-item>div{display:flex;align-items:center;gap:12px}.comment-item .el-rate{height:18px}.comment-item p{margin:7px 0 0;color:#586d67;line-height:1.6}@media(max-width:720px){.comment-editor{align-items:stretch;flex-direction:column;gap:8px}.comment-editor .el-rate{align-self:flex-start}}
/* 商品卡片图片与名称布局 */
.product-image{height:230px;box-sizing:border-box;padding:12px;display:flex;align-items:center;justify-content:center;overflow:hidden}.product-image>img{display:block;width:100%;height:100%;object-fit:contain;object-position:center;transform:scale(1.3,1.105);transform-origin:center}.product-info{min-width:0;padding:14px 16px 16px}.product-info h3{display:block;height:27px;margin:0;line-height:27px;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;color:#24413c;font-size:16px;font-weight:700}.product-info p{height:36px;margin:6px 0;line-height:18px}.rating-line{min-height:20px}.buy-line{margin-top:12px}
@media(max-width:720px){.product-image{height:210px}.product-info h3{font-size:15px}}
</style>
