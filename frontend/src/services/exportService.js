import apiClient from './axiosConfig';

const exportService = {
    /**
     * Export transactions in specified format
     */
    exportTransactions: async (startDate, endDate, format = 'csv') => {
        const params = new URLSearchParams();
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        params.append('format', format);

        const response = await apiClient.get(`/export/transactions?${params}`, {
            responseType: 'blob',
        });

        const extension = format === 'excel' ? 'xlsx' : format;
        downloadFile(response.data, `transactions.${extension}`);
    },

    /**
     * Export all data (transactions, budgets, goals) in specified format
     */
    exportAllData: async (format = 'csv') => {
        const response = await apiClient.get(`/export/all-data?format=${format}`, {
            responseType: 'blob',
        });

        const extension = format === 'excel' ? 'xlsx' : format;
        downloadFile(response.data, `budgetwise-data.${extension}`);
    },

    /**
     * Export dashboard summary in specified format
     */
    exportDashboard: async (format = 'excel') => {
        const response = await apiClient.get(`/export/dashboard?format=${format}`, {
            responseType: 'blob',
        });

        const extension = format === 'excel' ? 'xlsx' : format;
        downloadFile(response.data, `dashboard.${extension}`);
    },

    /**
     * Export budgets in specified format
     */
    exportBudgets: async (format = 'excel') => {
        const response = await apiClient.get(`/export/budgets?format=${format}`, {
            responseType: 'blob',
        });

        const extension = format === 'excel' ? 'xlsx' : format;
        downloadFile(response.data, `budgets.${extension}`);
    },

    /**
     * Export analytics data in specified format
     */
    exportAnalytics: async (timeRange = '3M', format = 'excel') => {
        const response = await apiClient.get(
            `/export/analytics?timeRange=${timeRange}&format=${format}`,
            {
                responseType: 'blob',
            }
        );

        const extension = format === 'excel' ? 'xlsx' : format;
        downloadFile(response.data, `analytics_${timeRange}.${extension}`);
    },

    /**
     * Export dashboard with images
     */
    exportDashboardWithImages: async (format, images) => {
        const response = await apiClient.post('/export/dashboard', {
            format,
            images
        }, {
            responseType: 'blob'
        });
        const extension = format === 'excel' ? 'xlsx' : format;
        downloadFile(response.data, `dashboard.${extension}`);
    },

    /**
     * Export analytics with images
     */
    exportAnalyticsWithImages: async (timeRange, format, images) => {
        const response = await apiClient.post('/export/analytics', {
            timeRange,
            format,
            images
        }, {
            responseType: 'blob'
        });
        const extension = format === 'excel' ? 'xlsx' : format;
        downloadFile(response.data, `analytics_${timeRange}.${extension}`);
    },
};

/**
 * Helper function to trigger file download
 */
const downloadFile = (blob, filename) => {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.parentNode.removeChild(link);
    window.URL.revokeObjectURL(url);
};

export default exportService;
