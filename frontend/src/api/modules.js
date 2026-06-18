import http from './http'

export const login = (data) => http.post('/auth/login', data)
export const register = (data) => http.post('/auth/register', data)
export const resetPassword = (data) => http.post('/auth/reset-password', data)
export const changePassword = (data) => http.post('/auth/change-password', data)
export const uploadAvatar = (file) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/uploads/avatar', form)
}

export const listCategories = (params) => http.get('/categories', { params })
export const createCategory = (data) => http.post('/categories', data)
export const updateCategory = (id, data) => http.put(`/categories/${id}`, data)
export const deleteCategory = (id) => http.delete(`/categories/${id}`)

export const listFinance = (params) => http.get('/finance-records', { params })
export const financeSummary = (params) => http.get('/finance-records/summary', { params })
export const createFinance = (data) => http.post('/finance-records', data)
export const updateFinance = (id, data) => http.put(`/finance-records/${id}`, data)
export const deleteFinance = (id) => http.delete(`/finance-records/${id}`)
export const importFinanceCsv = (file) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/finance-records/import-csv', form)
}

export const listProducts = (params) => http.get('/products', { params })
export const createProduct = (data) => http.post('/products', data)
export const updateProduct = (id, data) => http.put(`/products/${id}`, data)
export const deleteProduct = (id) => http.delete(`/products/${id}`)

export const listInvestments = (params) => http.get('/investments', { params })
export const createInvestment = (data) => http.post('/investments', data)
export const updateInvestment = (id, data) => http.put(`/investments/${id}`, data)
export const deleteInvestment = (id) => http.delete(`/investments/${id}`)
export const redeemInvestment = (id, params) => http.post(`/investments/${id}/redeem`, null, { params })
export const listRedemptions = (params) => http.get('/investments/redemptions', { params })
export const investmentStats = (params) => http.get('/investments/stats', { params })
export const fixHistoryData = () => http.get('/investments/fix-data')

export const listShares = (params) => http.get('/shares', { params })
export const getShare = (id) => http.get(`/shares/${id}`)
export const createShare = (data) => http.post('/shares', data)
export const updateShare = (id, data) => http.put(`/shares/${id}`, data)
export const deleteShare = (id) => http.delete(`/shares/${id}`)

export const listUsers = (params) => http.get('/users', { params })
export const getUser = (id) => http.get(`/users/${id}`)
export const getProfile = () => http.get('/users/profile')
export const createUser = (data) => http.post('/users', data)
export const updateUser = (id, data) => http.put(`/users/${id}`, data)
export const deleteUser = (id) => http.delete(`/users/${id}`)

export const listAdmins = (params) => http.get('/admins', { params })
export const createAdmin = (data) => http.post('/admins', data)
export const updateAdmin = (id, data) => http.put(`/admins/${id}`, data)
export const deleteAdmin = (id) => http.delete(`/admins/${id}`)
