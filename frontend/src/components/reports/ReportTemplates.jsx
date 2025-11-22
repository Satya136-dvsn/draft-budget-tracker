import { useState } from 'react';
import { Grid, Box, Button, Typography, Chip, Snackbar, Alert, Menu, MenuItem } from '@mui/material';
import reportService from '../../services/reportService';
import { Description, Assessment, ReceiptLong, TrendingUp, Download, ArrowDropDown } from '@mui/icons-material';
import ProfessionalCard from '../ui/ProfessionalCard';

const templates = [
    {
        id: 1,
        title: 'Monthly Summary',
        description: 'Comprehensive overview of your income, expenses, and savings for the month.',
        icon: <Assessment fontSize="large" color="primary" />,
        tags: ['Popular', 'Monthly'],
    },
    {
        id: 2,
        title: 'Tax Report',
        description: 'Detailed breakdown of deductible expenses and income sources for tax purposes.',
        icon: <ReceiptLong fontSize="large" color="secondary" />,
        tags: ['Tax', 'Annual'],
    },
    {
        id: 3,
        title: 'Expense Analysis',
        description: 'Deep dive into your spending habits by category and merchant.',
        icon: <Description fontSize="large" color="error" />,
        tags: ['Spending', 'Analysis'],
    },
    {
        id: 4,
        title: 'Investment Performance',
        description: 'Track the growth and performance of your investment portfolio.',
        icon: <TrendingUp fontSize="large" color="success" />,
        tags: ['Investments', 'Growth'],
    },
];

const ReportTemplates = () => {
    const [loading, setLoading] = useState(false);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });
    const [anchorEl, setAnchorEl] = useState(null);
    const [selectedTemplate, setSelectedTemplate] = useState(null);

    const handleCloseSnackbar = () => {
        setSnackbar({ ...snackbar, open: false });
    };

    const handleMenuOpen = (event, template) => {
        setAnchorEl(event.currentTarget);
        setSelectedTemplate(template);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
        setSelectedTemplate(null);
    };

    const handleGenerate = async (format) => {
        if (!selectedTemplate) return;

        handleMenuClose();
        try {
            setLoading(true);
            const response = await reportService.generateReport(selectedTemplate.id, format);

            // Create download link with proper file extension
            const extension = format === 'excel' ? 'xlsx' : format === 'pdf' ? 'pdf' : 'txt';
            const url = window.URL.createObjectURL(response.data);
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `${selectedTemplate.title.replace(/\s+/g, '_')}_${new Date().toISOString().split('T')[0]}.${extension}`);
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);

            setSnackbar({ open: true, message: 'Report generated successfully', severity: 'success' });
        } catch (error) {
            console.error('Report generation failed:', error);
            setSnackbar({ open: true, message: 'Failed to generate report', severity: 'error' });
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <Grid container spacing={3}>
                {templates.map((template) => (
                    <Grid item xs={12} md={6} key={template.id}>
                        <ProfessionalCard
                            title={
                                <Box display="flex" alignItems="center" gap={2}>
                                    {template.icon}
                                    <Typography variant="h6">{template.title}</Typography>
                                </Box>
                            }
                            action={
                                <Button
                                    variant="contained"
                                    startIcon={<Download />}
                                    endIcon={<ArrowDropDown />}
                                    onClick={(e) => handleMenuOpen(e, template)}
                                    disabled={loading}
                                >
                                    {loading ? 'Generating...' : 'Generate'}
                                </Button>
                            }
                        >
                            <Typography variant="body2" color="text.secondary" paragraph>
                                {template.description}
                            </Typography>
                            <Box display="flex" gap={1} mt={2}>
                                {template.tags.map((tag) => (
                                    <Chip key={tag} label={tag} size="small" variant="outlined" />
                                ))}
                            </Box>
                        </ProfessionalCard>
                    </Grid>
                ))}
            </Grid>

            <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleMenuClose}
            >
                <MenuItem onClick={() => handleGenerate('text')}>Text Report (.txt)</MenuItem>
                <MenuItem onClick={() => handleGenerate('excel')}>Excel Spreadsheet (.xlsx)</MenuItem>
                <MenuItem onClick={() => handleGenerate('pdf')}>PDF Document (.pdf)</MenuItem>
            </Menu>
            <Snackbar open={snackbar.open} autoHideDuration={6000} onClose={handleCloseSnackbar}>
                <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </>
    );
};

export default ReportTemplates;
