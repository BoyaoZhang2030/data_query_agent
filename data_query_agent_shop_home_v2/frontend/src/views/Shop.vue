<template>
  <div class="shop-page">
    <header class="topbar">
      <div class="top-inner">
        <span>欢迎来到橙选商城</span>
        <nav>
          <template v-if="user.id">
            <span>Hi，{{ user.username }}</span>
            <button v-if="user.role === 'admin'" class="admin-link" @click="$router.push('/dashboard')">进入管理系统</button>
            <button v-else @click="$router.push('/shop/orders')">我的订单</button>
            <button @click="logout">退出登录</button>
          </template>
          <template v-else>
            <button class="login-link" @click="$router.push('/login')">亲，请登录</button>
            <button @click="$router.push('/register')">免费注册</button>
            <button class="admin-link" @click="$router.push('/login?mode=admin')">管理员系统</button>
          </template>
        </nav>
      </div>
    </header>

    <section class="main-header">
      <div class="brand" @click="resetFilters"><span>橙</span><div><b>橙选</b><small>品质好物 · 随心购</small></div></div>
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
          <div><small>ORANGE SELECT</small><h1>把喜欢的生活<br>装进购物车</h1><p>库存数据实时同步，放心选购每一件好物</p><button @click="scrollToProducts">立即选购</button></div>
          <div class="banner-art"><span>🛍️</span><i></i><i></i><i></i></div>
        </div>
        <div class="service-card">
          <h3>橙选保障</h3>
          <div><span>✓</span><p><b>库存透明</b><small>实时展示剩余库存</small></p></div>
          <div><span>✓</span><p><b>价格清晰</b><small>所有商品明码标价</small></p></div>
          <div><span>✓</span><p><b>快捷下单</b><small>一站式购物体验</small></p></div>
        </div>
      </section>

      <section ref="productSection" class="products-section">
        <div class="section-title">
          <div><small>GOOD THINGS</small><h2>{{ selectedCategoryName }}</h2></div>
          <div class="sorts"><button :class="{ active: sortBy === 'default' }" @click="sortBy = 'default'">综合</button><button :class="{ active: sortBy === 'priceAsc' }" @click="sortBy = 'priceAsc'">价格升序</button><button :class="{ active: sortBy === 'stock' }" @click="sortBy = 'stock'">库存优先</button></div>
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
              <div class="stock-line"><span>库存 {{ product.stock }} 件</span><span>{{ categoryName(product.categoryId) }}</span></div>
              <div class="buy-line"><strong><small>¥</small>{{ formatPrice(product.price) }}</strong><el-button type="primary" round :disabled="product.stock <= 0" @click.stop="addToCart(product)">加入购物车</el-button></div>
            </div>
          </article>
        </div>
        <el-empty v-if="!loading && !displayedProducts.length" description="没有找到符合条件的商品" />
      </section>
    </main>

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
          <h2>{{ selectedProduct.name }}</h2>
          <strong class="detail-price">¥{{ formatPrice(selectedProduct.price) }}</strong>
          <div class="detail-stock">当前库存：{{ selectedProduct.stock }} 件</div>
          <h3>商品介绍</h3>
          <p class="detail-description">{{ selectedProduct.description || '暂无详细文字描述，后续可在商品管理中补充。' }}</p>
          <el-button type="primary" size="large" round :disabled="selectedProduct.stock <= 0" @click="addToCart(selectedProduct)">加入购物车</el-button>
        </div>
      </div>
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { dataApi } from '../api'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')
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
const openProductDetail = product => {
  selectedProduct.value = product
  detailVisible.value = true
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
</script>

<style scoped>
.shop-page{min-height:100vh;color:#292929;background:#f6f6f6;font-family:Inter,"PingFang SC",sans-serif}.topbar{height:36px;color:#666;background:#f3f3f3;border-bottom:1px solid #e8e8e8}.top-inner{max-width:1240px;height:100%;margin:auto;padding:0 20px;display:flex;align-items:center;justify-content:space-between;font-size:12px}.top-inner nav{display:flex;gap:18px;align-items:center}.top-inner button{border:0;color:#666;background:none;cursor:pointer}.top-inner button:hover{color:#ff5000}.main-header{max-width:1240px;height:112px;margin:auto;padding:0 20px;display:flex;align-items:center;gap:55px;background:#fff}.brand{display:flex;align-items:center;gap:10px;cursor:pointer}.brand>span{width:54px;height:54px;display:grid;place-items:center;color:#fff;border-radius:18px;background:linear-gradient(135deg,#ff7800,#ff3d00);font-size:28px;font-weight:900;box-shadow:0 10px 25px #ff6a0030}.brand b,.brand small{display:block}.brand b{color:#ff5000;font-size:25px}.brand small{margin-top:3px;color:#999;font-size:10px}.search{height:48px;display:flex;flex:1;max-width:650px;padding:3px;border:2px solid #ff5000;border-radius:24px;background:#fff}.search :deep(.el-input__wrapper){box-shadow:none}.search button{width:92px;border:0;color:#fff;border-radius:22px;background:linear-gradient(90deg,#ff7a00,#ff4000);font-size:16px;cursor:pointer}.cart-button{margin-left:auto;padding:13px 18px;border:1px solid #eee;border-radius:22px;color:#555;background:#fff;cursor:pointer}.cart-button em{display:inline-grid;min-width:21px;height:21px;margin-left:5px;place-items:center;color:#fff;border-radius:11px;background:#ff5000;font-style:normal}.shop-content{max-width:1240px;margin:auto;padding:0 20px 50px}.hero{display:grid;grid-template-columns:220px 1fr 220px;gap:14px}.category-panel,.service-card{height:350px;padding:20px;border-radius:14px;background:#fff}.category-panel h3,.service-card h3{margin:0 0 14px}.category-panel button{width:100%;height:41px;padding:0 8px;border:0;display:flex;justify-content:space-between;align-items:center;border-radius:8px;color:#555;background:none;cursor:pointer}.category-panel button:hover,.category-panel button.active{color:#ff5000;background:#fff3ea}.banner{height:350px;padding:48px;display:flex;justify-content:space-between;overflow:hidden;position:relative;border-radius:14px;color:#fff;background:linear-gradient(120deg,#ff7d19,#ff4200)}.banner small{letter-spacing:4px;opacity:.7}.banner h1{margin:15px 0 10px;font-size:40px;line-height:1.25}.banner p{opacity:.85}.banner button{margin-top:24px;padding:12px 25px;border:0;border-radius:22px;color:#ff5000;background:#fff;font-weight:700;cursor:pointer}.banner-art{width:200px;display:grid;place-items:center;position:relative}.banner-art span{z-index:2;font-size:95px;filter:drop-shadow(0 22px 18px #ad240060)}.banner-art i{position:absolute;border:1px solid #ffffff50;border-radius:50%}.banner-art i:nth-of-type(1){width:190px;height:190px}.banner-art i:nth-of-type(2){width:250px;height:250px}.banner-art i:nth-of-type(3){width:320px;height:320px}.service-card div{display:flex;gap:12px;align-items:center;padding:15px 0;border-bottom:1px solid #f2f2f2}.service-card div>span{width:30px;height:30px;display:grid;place-items:center;color:#fff;border-radius:50%;background:#ff6a00}.service-card p,.service-card b,.service-card small{display:block;margin:0}.service-card small{margin-top:4px;color:#999}.products-section{margin-top:28px;padding:26px;border-radius:16px;background:#fff}.section-title{margin-bottom:20px;display:flex;justify-content:space-between;align-items:end}.section-title small{color:#ff5000;letter-spacing:2px}.section-title h2{margin:5px 0 0;font-size:27px}.sorts{display:flex;gap:8px}.sorts button{padding:8px 13px;border:1px solid #eee;border-radius:16px;background:#fff;cursor:pointer}.sorts button.active{color:#ff5000;border-color:#ffb38d;background:#fff5ef}.product-grid{min-height:220px;display:grid;grid-template-columns:repeat(4,1fr);gap:18px}.product-card{overflow:hidden;border:1px solid #f0f0f0;border-radius:14px;background:#fff;transition:.25s}.product-card:hover{transform:translateY(-5px);border-color:#ffb998;box-shadow:0 15px 38px #aa5a1b18}.product-image{height:185px;display:grid;place-items:center;position:relative;background:var(--tone)}.product-image>span{font-size:76px;filter:drop-shadow(0 12px 12px #00000016)}.product-image i{position:absolute;right:10px;top:10px;padding:5px 9px;border-radius:10px;color:#fff;background:#999;font-size:11px;font-style:normal}.product-image i.warning{background:#ff5000}.product-info{padding:16px}.product-info h3{height:24px;margin:0;overflow:hidden;white-space:nowrap;text-overflow:ellipsis}.product-info p{height:38px;margin:7px 0;color:#999;overflow:hidden;font-size:12px;line-height:1.6}.stock-line{display:flex;justify-content:space-between;color:#aaa;font-size:11px}.buy-line{margin-top:15px;display:flex;justify-content:space-between;align-items:center}.buy-line strong{color:#ff5000;font-size:22px}.buy-line strong small{font-size:13px}.buy-line :deep(.el-button--primary){border-color:#ff5000;background:#ff5000}.cart-list{display:grid;gap:14px}.cart-item{display:grid;grid-template-columns:54px 1fr auto 24px;gap:12px;align-items:center;padding-bottom:14px;border-bottom:1px solid #eee}.cart-thumb{width:54px;height:54px;display:grid;place-items:center;border-radius:10px;background:#fff3ea;font-size:28px}.cart-meta b,.cart-meta small{display:block}.cart-meta small{margin-top:6px;color:#999}.remove{border:0;color:#aaa;background:none;font-size:20px;cursor:pointer}.cart-footer{display:flex;justify-content:space-between;align-items:center}.cart-footer div span,.cart-footer div strong{display:block}.cart-footer strong{margin-top:4px;color:#ff5000;font-size:20px}.cart-footer :deep(.el-button--primary){border-color:#ff5000;background:#ff5000}.checkout-summary{margin-bottom:20px;padding:16px;display:flex;justify-content:space-between;border-radius:10px;background:#fff3ea}.checkout-summary b{color:#ff5000;font-size:20px}
.top-inner .login-link{color:#ff5000}.top-inner .admin-link{padding-left:16px;border-left:1px solid #ddd;color:#333;font-weight:700}
.product-image>img{width:100%;height:100%;object-fit:cover}.product-detail{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(260px,.85fr);gap:30px}.detail-gallery{min-width:0}.detail-image{width:100%;height:360px;border-radius:14px;background:#f4eac5;cursor:zoom-in}.detail-placeholder{height:360px;display:grid;place-items:center;align-content:center;gap:16px;border-radius:14px;background:linear-gradient(145deg,#f4eac5,#dcebe7);font-size:110px}.detail-placeholder small{color:#6e827d;font-size:14px}.preview-tip{margin:12px 0 0;text-align:center;color:#82928e;font-size:12px}.detail-copy{padding:8px 4px}.detail-copy h2{margin:16px 0 10px;color:#24413c;font-size:28px}.detail-price{display:block;margin-bottom:16px;color:#3f786e;font-size:30px}.detail-stock{padding:11px 14px;border-radius:10px;color:#3f786e;background:#edf5f2}.detail-copy h3{margin:24px 0 10px}.detail-description{max-height:250px;margin:0 0 25px;overflow:auto;white-space:pre-wrap;color:#596d68;line-height:1.8}
@media(max-width:720px){.product-detail{grid-template-columns:1fr}.detail-image,.detail-placeholder{height:280px}.detail-placeholder{font-size:82px}}
@media(max-width:1000px){.hero{grid-template-columns:190px 1fr}.service-card{display:none}.product-grid{grid-template-columns:repeat(3,1fr)}}@media(max-width:720px){.main-header{height:auto;padding-top:18px;padding-bottom:18px;flex-wrap:wrap;gap:14px}.search{order:3;flex-basis:100%}.hero{grid-template-columns:1fr}.category-panel{height:auto;display:flex;overflow:auto}.category-panel h3{display:none}.category-panel button{min-width:100px}.banner{height:280px;padding:32px}.banner h1{font-size:30px}.banner-art{display:none}.product-grid{grid-template-columns:repeat(2,1fr)}.products-section{padding:15px}.section-title{align-items:flex-start;flex-direction:column;gap:14px}}@media(max-width:470px){.product-grid{grid-template-columns:1fr}.top-inner>span{display:none}.main-header{gap:8px}.brand small{display:none}.cart-button{padding:10px}}
</style>
