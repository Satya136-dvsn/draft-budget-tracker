import api from './api';

const categoryService = {
  getAll: () => api.get('/categories'),
  
  getById: (id) => api.get(`/categories/${id}`),
  
  create: (data) => api.post('/categories', data),
  
  update: (id, data) => api.put(`/categories/${id}`, data),
  
  delete: (id) => api.delete(`/categories/${id}`),
  
  getUserCategories: () => api.get('/categories/user'),
};

export default categoryService;
