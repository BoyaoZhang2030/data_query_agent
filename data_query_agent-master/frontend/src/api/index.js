import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    return Promise.reject(error)
  }
)

// 认证相关API
export const authApi = {
  register: (user) => api.post('/auth/register', user),
  login: (loginData) => api.post('/auth/login', loginData),
  getProfile: (userId) => api.get('/auth/profile', { params: { userId } }),
  updateProfile: (user) => api.put('/auth/profile', user)
}

// 数据查询相关API
export const queryApi = {
  naturalLanguageQuery: (data) => api.post('/query/natural-language', data),
  sqlQuery: (data) => api.post('/query/sql', data),
  analyzeResult: (data) => api.post('/query/analyze', data),
  getHistory: (userId) => api.get('/query/history', { params: { userId } }),
  getTemplates: () => api.get('/query/templates')
}

// 数据管理相关API
export const dataApi = {
  getProducts: (categoryId) => api.get('/data/products', { params: { categoryId } }),
  createProduct: (product) => api.post('/data/products', product),
  adjustProductStock: (id, change) => api.put(`/data/products/${id}/stock`, { change }),
  updateProductPrice: (id, price) => api.put(`/data/products/${id}/price`, { price }),
  getCategories: (parentId) => api.get('/data/categories', { params: { parentId } }),
  getOrders: (params = {}) => api.get('/data/orders', { params }),
  getOrderItems: (id) => api.get(`/data/orders/${id}/items`),
  createOrder: (order) => api.post('/data/orders', order),
  updateOrderStatus: (id, status) => api.put(`/data/orders/${id}/status`, { status }),
  deleteOrder: (id) => api.delete(`/data/orders/${id}`),
  getOverview: () => api.get('/statistics/overview'),
  getCategoryStatistics: () => api.get('/statistics/category'),
  getOrderStatusStatistics: () => api.get('/statistics/order-status')
}

export default api
