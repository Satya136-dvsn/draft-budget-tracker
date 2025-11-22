import api from './api';

const reportService = {
    generateReport: (templateId, format = 'text') => api.post(`/reports/generate/${templateId}?format=${format}`, {}, {
        responseType: 'blob',
    }),

    generateCustomReport: (config) => api.post('/reports/custom', config, {
        responseType: 'blob',
    }),

    getScheduledReports: () => api.get('/reports/scheduled'),

    scheduleReport: (config) => api.post('/reports/schedule', config),

    updateScheduledReport: (id, config) => api.put(`/reports/schedule/${id}`, config),

    deleteScheduledReport: (id) => api.delete(`/reports/schedule/${id}`),

    runScheduledReport: (id) => api.post(`/reports/schedule/${id}/run`),
};

export default reportService;
