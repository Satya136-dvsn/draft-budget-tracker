import api from './api';

const savingsGoalService = {
  getAll: () => api.get('/savings-goals'),
  
  getById: (id) => api.get(`/savings-goals/${id}`),
  
  create: (data) => api.post('/savings-goals', data),
  
  update: (id, data) => api.put(`/savings-goals/${id}`, data),
  
  delete: (id) => api.delete(`/savings-goals/${id}`),
  
  contribute: (id, amount) => api.post(`/savings-goals/${id}/contribute`, { amount }),
};

export default savingsGoalService;
