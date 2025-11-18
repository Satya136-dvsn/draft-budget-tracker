import api from './api';

const transactionService = {
  getAll: (params) => api.get('/transactions', { params }),
  
  getById: (id) => api.get(`/transactions/${id}`),
  
  create: (data) => api.post('/transactions', data),
  
  update: (id, data) => api.put(`/transactions/${id}`, data),
  
  delete: (id) => api.delete(`/transactions/${id}`),
  
  getRecent: (limit = 10) => api.get(`/transactions/recent?limit=${limit}`),
};

export default transactionService;
