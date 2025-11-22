import api from './api';

const aiService = {
    // Suggest category for a transaction
    categorizeTransaction: (description, amount) => {
        return api.post('/ai/categorize', {
            description,
            amount
        });
    },

    // Get spending predictions
    getPredictions: () => {
        return api.get('/ai/predictions');
    },

    // Get budget recommendations
    getBudgetAdvisor: () => {
        return api.get('/ai/advisor');
    },

    // Get spending anomalies
    getAnomalies: () => {
        return api.get('/ai/anomalies');
    },
};

export default aiService;
