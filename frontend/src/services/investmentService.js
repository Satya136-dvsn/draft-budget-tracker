import api from './api';

const InvestmentService = {
    getAllInvestments: async () => {
        const response = await api.get('/investments');
        return response.data;
    },

    getPortfolioSummary: async () => {
        const response = await api.get('/investments/summary');
        return response.data;
    },

    getInvestmentById: async (id) => {
        const response = await api.get(`/investments/${id}`);
        return response.data;
    },

    createInvestment: async (investmentData) => {
        const response = await api.post('/investments', investmentData);
        return response.data;
    },

    updateInvestment: async (id, investmentData) => {
        const response = await api.put(`/investments/${id}`, investmentData);
        return response.data;
    },

    updateCurrentPrice: async (id, currentPrice) => {
        const response = await api.put(`/investments/${id}/price?currentPrice=${currentPrice}`);
        return response.data;
    },

    deleteInvestment: async (id) => {
        await api.delete(`/investments/${id}`);
    }
};

export default InvestmentService;
