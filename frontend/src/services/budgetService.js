import api from './api';

const budgetService = {
  getAll: () => api.get('/budgets'),
  
  getById: (id) => api.get(`/budgets/${id}`),
  
  create: (data) => api.post('/budgets', data),
  
  update: (id, data) => api.put(`/budgets/${id}`, data),
  
  delete: (id) => api.delete(`/budgets/${id}`),
  
  getActive: () => api.get('/budgets/active'),
};

export default budgetService;
