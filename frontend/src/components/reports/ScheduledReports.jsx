import { useState, useEffect } from 'react';
import {
    Box,
    Typography,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    IconButton,
    Chip,
    Button,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    MenuItem,
    Snackbar,
    Alert,
    CircularProgress
} from '@mui/material';
import { Edit, Delete, PlayArrow, Add } from '@mui/icons-material';
import ProfessionalCard from '../ui/ProfessionalCard';
import reportService from '../../services/reportService';

const ScheduledReports = () => {
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);
    const [dialogOpen, setDialogOpen] = useState(false);
    const [currentReport, setCurrentReport] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        frequency: 'WEEKLY',
        recipients: '',
        reportType: 'TEMPLATE'
    });
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

    useEffect(() => {
        loadReports();
    }, []);

    const loadReports = async () => {
        try {
            setLoading(true);
            const response = await reportService.getScheduledReports();
            setReports(response.data);
        } catch (error) {
            console.error('Failed to load reports:', error);
            showSnackbar('Failed to load scheduled reports', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleOpenDialog = (report = null) => {
        if (report) {
            setCurrentReport(report);
            setFormData({
                name: report.name,
                frequency: report.frequency,
                recipients: report.recipients,
                reportType: report.reportType || 'TEMPLATE'
            });
        } else {
            setCurrentReport(null);
            setFormData({
                name: '',
                frequency: 'WEEKLY',
                recipients: '',
                reportType: 'TEMPLATE'
            });
        }
        setDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setDialogOpen(false);
        setCurrentReport(null);
    };

    const handleSave = async () => {
        try {
            if (currentReport) {
                await reportService.updateScheduledReport(currentReport.id, formData);
                showSnackbar('Report updated successfully');
            } else {
                await reportService.scheduleReport(formData);
                showSnackbar('Report scheduled successfully');
            }
            handleCloseDialog();
            loadReports();
        } catch (error) {
            console.error('Failed to save report:', error);
            showSnackbar('Failed to save report', 'error');
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this scheduled report?')) return;
        try {
            await reportService.deleteScheduledReport(id);
            showSnackbar('Report deleted successfully');
            loadReports();
        } catch (error) {
            console.error('Failed to delete report:', error);
            showSnackbar('Failed to delete report', 'error');
        }
    };

    const handleRun = async (id) => {
        try {
            await reportService.runScheduledReport(id);
            showSnackbar('Report run triggered successfully');
        } catch (error) {
            console.error('Failed to run report:', error);
            showSnackbar('Failed to run report', 'error');
        }
    };

    const showSnackbar = (message, severity = 'success') => {
        setSnackbar({ open: true, message, severity });
    };

    return (
        <>
            <ProfessionalCard
                title="Scheduled Reports"
                subheader="Manage your automated report deliveries"
                action={
                    <Button
                        variant="contained"
                        startIcon={<Add />}
                        onClick={() => handleOpenDialog()}
                    >
                        Schedule New Report
                    </Button>
                }
            >
                {loading ? (
                    <Box display="flex" justifyContent="center" p={3}>
                        <CircularProgress />
                    </Box>
                ) : reports.length === 0 ? (
                    <Typography variant="body1" align="center" py={4} color="text.secondary">
                        No scheduled reports found. Create one to get started!
                    </Typography>
                ) : (
                    <TableContainer>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Report Name</TableCell>
                                    <TableCell>Frequency</TableCell>
                                    <TableCell>Next Run</TableCell>
                                    <TableCell>Recipients</TableCell>
                                    <TableCell>Status</TableCell>
                                    <TableCell align="right">Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {reports.map((report) => (
                                    <TableRow key={report.id}>
                                        <TableCell sx={{ fontWeight: 500 }}>{report.name}</TableCell>
                                        <TableCell>{report.frequency}</TableCell>
                                        <TableCell>{report.nextRun}</TableCell>
                                        <TableCell>{report.recipients}</TableCell>
                                        <TableCell>
                                            <Chip
                                                label={report.status}
                                                color={report.status === 'Active' ? 'success' : 'default'}
                                                size="small"
                                                variant="outlined"
                                            />
                                        </TableCell>
                                        <TableCell align="right">
                                            <IconButton size="small" title="Run Now" onClick={() => handleRun(report.id)}>
                                                <PlayArrow fontSize="small" />
                                            </IconButton>
                                            <IconButton size="small" title="Edit" onClick={() => handleOpenDialog(report)}>
                                                <Edit fontSize="small" />
                                            </IconButton>
                                            <IconButton size="small" title="Delete" color="error" onClick={() => handleDelete(report.id)}>
                                                <Delete fontSize="small" />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}
            </ProfessionalCard>

            <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                <DialogTitle>{currentReport ? 'Edit Scheduled Report' : 'Schedule New Report'}</DialogTitle>
                <DialogContent>
                    <Box display="flex" flexDirection="column" gap={2} mt={1}>
                        <TextField
                            label="Report Name"
                            fullWidth
                            value={formData.name}
                            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                        />
                        <TextField
                            select
                            label="Frequency"
                            fullWidth
                            value={formData.frequency}
                            onChange={(e) => setFormData({ ...formData, frequency: e.target.value })}
                        >
                            <MenuItem value="WEEKLY">Weekly</MenuItem>
                            <MenuItem value="MONTHLY">Monthly</MenuItem>
                            <MenuItem value="QUARTERLY">Quarterly</MenuItem>
                        </TextField>
                        <TextField
                            label="Recipients (comma separated)"
                            fullWidth
                            value={formData.recipients}
                            onChange={(e) => setFormData({ ...formData, recipients: e.target.value })}
                            placeholder="email@example.com"
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDialog}>Cancel</Button>
                    <Button variant="contained" onClick={handleSave} disabled={!formData.name || !formData.recipients}>
                        Save
                    </Button>
                </DialogActions>
            </Dialog>

            <Snackbar
                open={snackbar.open}
                autoHideDuration={6000}
                onClose={() => setSnackbar({ ...snackbar, open: false })}
            >
                <Alert severity={snackbar.severity} onClose={() => setSnackbar({ ...snackbar, open: false })}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </>
    );
};

export default ScheduledReports;
