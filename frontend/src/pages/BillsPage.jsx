import React, { useState, useEffect } from 'react';
import {
    Container, Typography, Box, Button, Grid, Card, CardContent,
    IconButton, Chip, Dialog, DialogTitle, DialogContent, DialogActions,
    TextField, MenuItem, FormControl, InputLabel, Select, Tabs, Tab,
    Alert, CircularProgress, Tooltip, Paper
} from '@mui/material';
import {
    Add as AddIcon,
    Edit as EditIcon,
    Delete as DeleteIcon,
    CheckCircle as CheckCircleIcon,
    CalendarMonth as CalendarIcon,
    List as ListIcon,
    Event as EventIcon,
    DateRange as YearIcon,
    ArrowBackIos as PrevIcon,
    ArrowForwardIos as NextIcon,
    Today as TodayIcon
} from '@mui/icons-material';
import {
    format, addDays, startOfMonth, endOfMonth, startOfWeek, endOfWeek,
    eachDayOfInterval, isSameMonth, isSameDay, isToday, parseISO,
    addMonths, subMonths, getYear, getMonth, setMonth, setYear,
    addYears, subYears, getDate, isAfter, isBefore
} from 'date-fns';
import BillsService from '../services/BillsService';

const BillsPage = () => {
    const [bills, setBills] = useState([]);
    const [loading, setLoading] = useState(true);
    const [tabValue, setTabValue] = useState(0);
    const [viewDate, setViewDate] = useState(new Date()); // For calendar navigation
    const [openDialog, setOpenDialog] = useState(false);
    const [currentBill, setCurrentBill] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        amount: '',
        category: '',
        recurrence: 'MONTHLY',
        dueDate: format(new Date(), 'yyyy-MM-dd'),
        autoReminder: true,
        notes: ''
    });

    useEffect(() => {
        fetchBills();
    }, []);

    const fetchBills = async () => {
        try {
            setLoading(true);
            const data = await BillsService.getAllBills();
            setBills(data);
        } catch (error) {
            console.error("Failed to fetch bills", error);
        } finally {
            setLoading(false);
        }
    };

    const handleOpenDialog = (bill = null) => {
        if (bill) {
            setCurrentBill(bill);
            setFormData({
                name: bill.name,
                amount: bill.amount,
                category: bill.category || '',
                recurrence: bill.recurrence,
                dueDate: bill.nextDueDate || bill.dueDate,
                autoReminder: bill.autoReminder,
                notes: bill.notes || ''
            });
        } else {
            setCurrentBill(null);
            setFormData({
                name: '',
                amount: '',
                category: '',
                recurrence: 'MONTHLY',
                dueDate: format(new Date(), 'yyyy-MM-dd'),
                autoReminder: true,
                notes: ''
            });
        }
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async () => {
        try {
            if (currentBill) {
                await BillsService.updateBill(currentBill.id, formData);
            } else {
                await BillsService.createBill(formData);
            }
            fetchBills();
            handleCloseDialog();
        } catch (error) {
            console.error("Failed to save bill", error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this bill?')) {
            try {
                await BillsService.deleteBill(id);
                fetchBills();
            } catch (error) {
                console.error("Failed to delete bill", error);
            }
        }
    };

    const handleMarkAsPaid = async (id) => {
        try {
            await BillsService.markAsPaid(id);
            fetchBills();
        } catch (error) {
            console.error("Failed to mark bill as paid", error);
        }
    };

    // --- Calendar Logic ---

    const handlePrevMonth = () => setViewDate(subMonths(viewDate, 1));
    const handleNextMonth = () => setViewDate(addMonths(viewDate, 1));
    const handlePrevYear = () => setViewDate(subYears(viewDate, 1));
    const handleNextYear = () => setViewDate(addYears(viewDate, 1));
    const handleToday = () => setViewDate(new Date());

    const generateCalendarDays = (date) => {
        const start = startOfWeek(startOfMonth(date));
        const end = endOfWeek(endOfMonth(date));
        return eachDayOfInterval({ start, end });
    };

    // Project recurring bills onto a specific date
    const getProjectedBillsForDate = (targetDate) => {
        const targetDateStr = format(targetDate, 'yyyy-MM-dd');
        const dayOfMonth = getDate(targetDate);
        const month = getMonth(targetDate);
        const year = getYear(targetDate);

        return bills.filter(bill => {
            // 1. Check specific next due date (from DB)
            if (bill.nextDueDate && isSameDay(parseISO(bill.nextDueDate), targetDate)) {
                return true;
            }

            // 2. Project recurrence
            // Only project if status is not OVERDUE (overdue bills stay on their original date)
            // and if the target date is in the future relative to the bill's start/due date
            const billDueDate = parseISO(bill.dueDate);
            if (isBefore(targetDate, billDueDate)) return false;

            // Simple projection logic based on recurrence type
            if (bill.recurrence === 'MONTHLY') {
                // Matches if day of month is same
                // Handle edge cases like 31st in Feb? date-fns handles this but simple check:
                return getDate(billDueDate) === dayOfMonth;
            }
            if (bill.recurrence === 'YEARLY') {
                return getDate(billDueDate) === dayOfMonth && getMonth(billDueDate) === month;
            }
            if (bill.recurrence === 'WEEKLY') {
                // Check if day of week matches
                return billDueDate.getDay() === targetDate.getDay();
            }

            return false;
        }).map(bill => ({
            ...bill,
            isProjected: bill.nextDueDate ? !isSameDay(parseISO(bill.nextDueDate), targetDate) : true
        }));
    };

    // --- Summary Calculations ---
    const totalBills = bills.length;
    const pendingBills = bills.filter(b => b.status === 'PENDING').length;
    const overdueBills = bills.filter(b => b.status === 'OVERDUE').length;
    const totalAmount = bills.reduce((sum, b) => sum + (b.status !== 'PAID' ? b.amount : 0), 0);

    return (
        <Container maxWidth="lg" sx={{ py: 4 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
                <Box>
                    <Typography variant="h4" fontWeight="bold" gutterBottom>
                        Bills & Payments
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                        Track recurring bills and never miss a payment
                    </Typography>
                </Box>
                <Button
                    variant="contained"
                    startIcon={<AddIcon />}
                    onClick={() => handleOpenDialog()}
                    sx={{ borderRadius: 2, px: 3 }}
                >
                    Add Bill
                </Button>
            </Box>

            {/* Summary Cards */}
            <Grid container spacing={3} mb={4}>
                <Grid item xs={12} sm={6} md={3}>
                    <Card sx={{ bgcolor: 'primary.light', color: 'primary.contrastText' }}>
                        <CardContent>
                            <Typography variant="subtitle2" opacity={0.8}>Total Pending Amount</Typography>
                            <Typography variant="h4" fontWeight="bold">₹{totalAmount.toFixed(2)}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Card>
                        <CardContent>
                            <Typography variant="subtitle2" color="text.secondary">Pending Bills</Typography>
                            <Typography variant="h4" fontWeight="bold" color="warning.main">{pendingBills}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Card>
                        <CardContent>
                            <Typography variant="subtitle2" color="text.secondary">Overdue Bills</Typography>
                            <Typography variant="h4" fontWeight="bold" color="error.main">{overdueBills}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid item xs={12} sm={6} md={3}>
                    <Card>
                        <CardContent>
                            <Typography variant="subtitle2" color="text.secondary">Total Active Bills</Typography>
                            <Typography variant="h4" fontWeight="bold">{totalBills}</Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* Tabs */}
            <Paper sx={{ mb: 3 }}>
                <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)} sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tab icon={<ListIcon />} label="List View" iconPosition="start" />
                    <Tab icon={<CalendarIcon />} label="Calendar View" iconPosition="start" />
                    <Tab icon={<YearIcon />} label="Year View" iconPosition="start" />
                </Tabs>
            </Paper>

            {loading ? (
                <Box display="flex" justifyContent="center" py={8}>
                    <CircularProgress />
                </Box>
            ) : (
                <>
                    {/* List View */}
                    {tabValue === 0 && (
                        <Grid container spacing={2}>
                            {bills.length === 0 ? (
                                <Grid item xs={12}>
                                    <Box textAlign="center" py={8} bgcolor="background.paper" borderRadius={2}>
                                        <Typography variant="h6" color="text.secondary">No bills found</Typography>
                                        <Button variant="outlined" sx={{ mt: 2 }} onClick={() => handleOpenDialog()}>
                                            Create your first bill
                                        </Button>
                                    </Box>
                                </Grid>
                            ) : (
                                bills.map((bill) => (
                                    <Grid item xs={12} key={bill.id}>
                                        <Card sx={{ display: 'flex', alignItems: 'center', p: 2 }}>
                                            <Box sx={{
                                                width: 50, height: 50, borderRadius: '50%',
                                                bgcolor: bill.status === 'OVERDUE' ? 'error.light' :
                                                    bill.status === 'PAID' ? 'success.light' : 'warning.light',
                                                display: 'flex', alignItems: 'center', justifyContent: 'center', mr: 2
                                            }}>
                                                <EventIcon sx={{ color: 'white' }} />
                                            </Box>
                                            <Box flexGrow={1}>
                                                <Typography variant="h6">{bill.name}</Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    Due: {format(parseISO(bill.nextDueDate || bill.dueDate), 'MMM dd, yyyy')} • {bill.recurrence}
                                                </Typography>
                                            </Box>
                                            <Box textAlign="right" mr={3}>
                                                <Typography variant="h6">₹{bill.amount.toFixed(2)}</Typography>
                                                <Chip
                                                    label={bill.status}
                                                    color={bill.status === 'OVERDUE' ? 'error' : bill.status === 'PAID' ? 'success' : 'warning'}
                                                    size="small"
                                                />
                                            </Box>
                                            <Box>
                                                {bill.status !== 'PAID' && (
                                                    <Tooltip title="Mark as Paid">
                                                        <IconButton color="success" onClick={() => handleMarkAsPaid(bill.id)}>
                                                            <CheckCircleIcon />
                                                        </IconButton>
                                                    </Tooltip>
                                                )}
                                                <IconButton onClick={() => handleOpenDialog(bill)}>
                                                    <EditIcon />
                                                </IconButton>
                                                <IconButton color="error" onClick={() => handleDelete(bill.id)}>
                                                    <DeleteIcon />
                                                </IconButton>
                                            </Box>
                                        </Card>
                                    </Grid>
                                ))
                            )}
                        </Grid>
                    )}

                    {/* Calendar View */}
                    {tabValue === 1 && (
                        <Paper sx={{ p: 2 }}>
                            {/* Calendar Navigation */}
                            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2} px={2}>
                                <Button startIcon={<TodayIcon />} onClick={handleToday}>Today</Button>
                                <Box display="flex" alignItems="center">
                                    <IconButton onClick={handlePrevMonth}><PrevIcon /></IconButton>
                                    <Typography variant="h6" sx={{ mx: 2, minWidth: 150, textAlign: 'center' }}>
                                        {format(viewDate, 'MMMM yyyy')}
                                    </Typography>
                                    <IconButton onClick={handleNextMonth}><NextIcon /></IconButton>
                                </Box>
                                <Box width={80} /> {/* Spacer for alignment */}
                            </Box>

                            <Grid container spacing={1}>
                                {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
                                    <Grid item xs={12 / 7} key={day} textAlign="center">
                                        <Typography variant="subtitle2" fontWeight="bold" color="text.secondary">
                                            {day}
                                        </Typography>
                                    </Grid>
                                ))}
                                {generateCalendarDays(viewDate).map((day, index) => {
                                    const dayBills = getProjectedBillsForDate(day);
                                    const isCurrentMonth = isSameMonth(day, viewDate);
                                    const isTodayDate = isToday(day);

                                    return (
                                        <Grid item xs={12 / 7} key={index}>
                                            <Box sx={{
                                                height: 120,
                                                border: '1px solid',
                                                borderColor: isTodayDate ? 'primary.main' : 'divider',
                                                bgcolor: isTodayDate ? 'primary.50' : isCurrentMonth ? 'background.paper' : 'action.hover',
                                                p: 1,
                                                overflow: 'hidden',
                                                position: 'relative'
                                            }}>
                                                <Box display="flex" justifyContent="space-between">
                                                    <Typography variant="caption" fontWeight={isTodayDate ? 'bold' : 'normal'} color={isCurrentMonth ? 'text.primary' : 'text.disabled'}>
                                                        {format(day, 'd')}
                                                    </Typography>
                                                    {isTodayDate && <Chip label="Today" size="small" color="primary" sx={{ height: 16, fontSize: '0.6rem' }} />}
                                                </Box>

                                                <Box sx={{ mt: 1, overflowY: 'auto', maxHeight: 85 }}>
                                                    {dayBills.map((bill, i) => (
                                                        <Box key={`${bill.id}-${i}`} sx={{
                                                            bgcolor: bill.status === 'PAID' && !bill.isProjected ? 'success.light' :
                                                                bill.status === 'OVERDUE' && !bill.isProjected ? 'error.light' :
                                                                    bill.isProjected ? 'info.light' : 'warning.light',
                                                            color: 'white',
                                                            fontSize: '0.7rem',
                                                            p: 0.5,
                                                            borderRadius: 1,
                                                            mb: 0.5,
                                                            whiteSpace: 'nowrap',
                                                            overflow: 'hidden',
                                                            textOverflow: 'ellipsis',
                                                            cursor: 'pointer',
                                                            opacity: bill.isProjected ? 0.8 : 1
                                                        }} onClick={() => handleOpenDialog(bill)}>
                                                            {bill.name} {bill.isProjected && '*'}
                                                        </Box>
                                                    ))}
                                                </Box>
                                            </Box>
                                        </Grid>
                                    );
                                })}
                            </Grid>
                            <Box mt={2} display="flex" gap={2} justifyContent="center">
                                <Typography variant="caption" display="flex" alignItems="center"><Box component="span" sx={{ width: 10, height: 10, bgcolor: 'warning.light', mr: 0.5, borderRadius: '50%' }} /> Pending</Typography>
                                <Typography variant="caption" display="flex" alignItems="center"><Box component="span" sx={{ width: 10, height: 10, bgcolor: 'success.light', mr: 0.5, borderRadius: '50%' }} /> Paid</Typography>
                                <Typography variant="caption" display="flex" alignItems="center"><Box component="span" sx={{ width: 10, height: 10, bgcolor: 'error.light', mr: 0.5, borderRadius: '50%' }} /> Overdue</Typography>
                                <Typography variant="caption" display="flex" alignItems="center"><Box component="span" sx={{ width: 10, height: 10, bgcolor: 'info.light', mr: 0.5, borderRadius: '50%' }} /> Projected (Future)</Typography>
                            </Box>
                        </Paper>
                    )}

                    {/* Year View */}
                    {tabValue === 2 && (
                        <Paper sx={{ p: 3 }}>
                            {/* Year Navigation */}
                            <Box display="flex" justifyContent="center" alignItems="center" mb={4}>
                                <IconButton onClick={handlePrevYear}><PrevIcon /></IconButton>
                                <Typography variant="h4" sx={{ mx: 4, fontWeight: 'bold' }}>
                                    {format(viewDate, 'yyyy')}
                                </Typography>
                                <IconButton onClick={handleNextYear}><NextIcon /></IconButton>
                            </Box>

                            <Grid container spacing={3}>
                                {Array.from({ length: 12 }).map((_, monthIndex) => {
                                    const monthDate = new Date(getYear(viewDate), monthIndex, 1);
                                    const monthName = format(monthDate, 'MMMM');
                                    const isCurrentMonth = isSameMonth(monthDate, new Date());

                                    // Get bills for this month
                                    const monthBills = [];
                                    const daysInMonth = eachDayOfInterval({
                                        start: startOfMonth(monthDate),
                                        end: endOfMonth(monthDate)
                                    });

                                    daysInMonth.forEach(day => {
                                        const billsOnDay = getProjectedBillsForDate(day);
                                        billsOnDay.forEach(b => {
                                            // Avoid duplicates if multiple recurrences in same month (e.g. weekly)
                                            monthBills.push({ ...b, day: getDate(day) });
                                        });
                                    });

                                    return (
                                        <Grid item xs={12} sm={6} md={4} key={monthName}>
                                            <Card variant="outlined" sx={{
                                                height: '100%',
                                                borderColor: isCurrentMonth ? 'primary.main' : 'divider',
                                                borderWidth: isCurrentMonth ? 2 : 1
                                            }}>
                                                <Box sx={{
                                                    bgcolor: isCurrentMonth ? 'primary.main' : 'action.hover',
                                                    color: isCurrentMonth ? 'white' : 'text.primary',
                                                    p: 1, textAlign: 'center'
                                                }}>
                                                    <Typography variant="subtitle1" fontWeight="bold">{monthName}</Typography>
                                                </Box>
                                                <CardContent sx={{ p: 1.5, '&:last-child': { pb: 1.5 } }}>
                                                    {monthBills.length === 0 ? (
                                                        <Typography variant="caption" color="text.secondary" display="block" textAlign="center" py={2}>
                                                            No bills
                                                        </Typography>
                                                    ) : (
                                                        <Box sx={{ maxHeight: 150, overflowY: 'auto' }}>
                                                            {monthBills.sort((a, b) => a.day - b.day).map((bill, i) => (
                                                                <Box key={i} display="flex" justifyContent="space-between" mb={0.5} sx={{ fontSize: '0.8rem' }}>
                                                                    <Typography variant="caption" fontWeight="bold" sx={{ minWidth: 20 }}>
                                                                        {bill.day}
                                                                    </Typography>
                                                                    <Typography variant="caption" noWrap sx={{ flex: 1, mx: 1 }}>
                                                                        {bill.name}
                                                                    </Typography>
                                                                    <Typography variant="caption">
                                                                        ₹{bill.amount}
                                                                    </Typography>
                                                                </Box>
                                                            ))}
                                                        </Box>
                                                    )}
                                                    {monthBills.length > 0 && (
                                                        <Box borderTop={1} borderColor="divider" mt={1} pt={0.5} display="flex" justifyContent="space-between">
                                                            <Typography variant="caption" fontWeight="bold">Total:</Typography>
                                                            <Typography variant="caption" fontWeight="bold">
                                                                ₹{monthBills.reduce((sum, b) => sum + b.amount, 0).toFixed(0)}
                                                            </Typography>
                                                        </Box>
                                                    )}
                                                </CardContent>
                                            </Card>
                                        </Grid>
                                    );
                                })}
                            </Grid>
                        </Paper>
                    )}
                </>
            )}

            {/* Add/Edit Dialog */}
            <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                <DialogTitle>{currentBill ? 'Edit Bill' : 'Add New Bill'}</DialogTitle>
                <DialogContent>
                    <Grid container spacing={2} sx={{ mt: 1 }}>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Bill Name"
                                name="name"
                                value={formData.name}
                                onChange={handleInputChange}
                                required
                            />
                        </Grid>
                        <Grid item xs={6}>
                            <TextField
                                fullWidth
                                label="Amount"
                                name="amount"
                                type="number"
                                value={formData.amount}
                                onChange={handleInputChange}
                                required
                            />
                        </Grid>
                        <Grid item xs={6}>
                            <TextField
                                fullWidth
                                label="Category"
                                name="category"
                                value={formData.category}
                                onChange={handleInputChange}
                            />
                        </Grid>
                        <Grid item xs={6}>
                            <FormControl fullWidth>
                                <InputLabel>Recurrence</InputLabel>
                                <Select
                                    name="recurrence"
                                    value={formData.recurrence}
                                    label="Recurrence"
                                    onChange={handleInputChange}
                                >
                                    <MenuItem value="WEEKLY">Weekly</MenuItem>
                                    <MenuItem value="MONTHLY">Monthly</MenuItem>
                                    <MenuItem value="QUARTERLY">Quarterly</MenuItem>
                                    <MenuItem value="YEARLY">Yearly</MenuItem>
                                    <MenuItem value="ONE_TIME">One Time</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={6}>
                            <TextField
                                fullWidth
                                label="Due Date"
                                name="dueDate"
                                type="date"
                                value={formData.dueDate}
                                onChange={handleInputChange}
                                InputLabelProps={{ shrink: true }}
                                required
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Notes"
                                name="notes"
                                multiline
                                rows={3}
                                value={formData.notes}
                                onChange={handleInputChange}
                            />
                        </Grid>
                    </Grid>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseDialog}>Cancel</Button>
                    <Button onClick={handleSubmit} variant="contained">Save</Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
};

export default BillsPage;
