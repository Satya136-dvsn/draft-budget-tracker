import api from './api';

const BillsService = {
    getAllBills: async () => {
        const response = await api.get('/bills');
        return response.data;
    },

    getUpcomingBills: async (days = 7) => {
        const response = await api.get(`/bills/upcoming?days=${days}`);
        return response.data;
    },

    getBillById: async (id) => {
        const response = await api.get(`/bills/${id}`);
        return response.data;
    },

    createBill: async (billData) => {
        const response = await api.post('/bills', billData);
        return response.data;
    },

    updateBill: async (id, billData) => {
        const response = await api.put(`/bills/${id}`, billData);
        return response.data;
    },

    markAsPaid: async (id) => {
        const response = await api.post(`/bills/${id}/pay`);
        return response.data;
    },

    deleteBill: async (id) => {
        await api.delete(`/bills/${id}`);
    }
};

export default BillsService;
