import React, { useState, useEffect, useRef } from 'react';
import {
    Container, Typography, Box, Button, Grid, Card, CardContent,
    IconButton, Dialog, DialogTitle, DialogContent, DialogActions,
    TextField, MenuItem, FormControl, InputLabel, Select,
    CircularProgress, Table, TableBody, TableCell, TableContainer,
    TableHead, TableRow, Paper, Tooltip, Switch, FormControlLabel,
    Fade, Chip
} from '@mui/material';
import {
    Add as AddIcon,
    Edit as EditIcon,
    Delete as DeleteIcon,
    TrendingUp as TrendingUpIcon,
    TrendingDown as TrendingDownIcon,
    ShowChart as ShowChartIcon,
    AutoGraph as LiveIcon
} from '@mui/icons-material';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip as RechartsTooltip, Legend } from 'recharts';
import { format } from 'date-fns';
import InvestmentService from '../services/InvestmentService';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d', '#ffc658'];

const InvestmentsPage = () => {
    const [originalInvestments, setOriginalInvestments] = useState([]); // Store real DB data
    const [investments, setInvestments] = useState([]); // Store displayed data (simulated or real)
    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isLive, setIsLive] = useState(false); // Live Market Toggle
    const [openDialog, setOpenDialog] = useState(false);
    const [openPriceDialog, setOpenPriceDialog] = useState(false);
    const [currentInvestment, setCurrentInvestment] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        type: 'STOCK',
        quantity: '',
        buyPrice: '',
        currentPrice: '',
        purchaseDate: format(new Date(), 'yyyy-MM-dd'),
        symbol: '',
        notes: ''
    });
    const [newPrice, setNewPrice] = useState('');

    // Simulation Refs
    const intervalRef = useRef(null);

    useEffect(() => {
        fetchData();
        return () => stopSimulation();
    }, []);

    // Handle Live Mode Toggle
    useEffect(() => {
        if (isLive) {
            startSimulation();
        } else {
            stopSimulation();
            // Reset to original data when stopping
            if (originalInvestments.length > 0) {
                setInvestments(JSON.parse(JSON.stringify(originalInvestments)));
                recalculateSummary(originalInvestments);
            }
        }
    }, [isLive]);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [investmentsData, summaryData] = await Promise.all([
                InvestmentService.getAllInvestments(),
                InvestmentService.getPortfolioSummary()
            ]);
            setOriginalInvestments(investmentsData);
            setInvestments(investmentsData);
            setSummary(summaryData);
        } catch (error) {
            console.error("Failed to fetch investment data", error);
        } finally {
            setLoading(false);
        }
    };

    // --- Simulation Logic ---
    const startSimulation = () => {
        intervalRef.current = setInterval(() => {
            setInvestments(prevInvestments => {
                const updatedInvestments = prevInvestments.map(inv => {
                    // Simulate price change between -1.5% and +1.5%
                    const changePercent = (Math.random() * 3) - 1.5;
                    const currentPrice = inv.currentPrice || inv.buyPrice;
                    const newPrice = currentPrice * (1 + (changePercent / 100));

                    // Recalculate derived fields
                    const currentValue = newPrice * inv.quantity;
                    const profitLoss = currentValue - inv.totalInvested;
                    const profitLossPercent = (profitLoss / inv.totalInvested) * 100;

                    return {
                        ...inv,
                        currentPrice: newPrice,
                        currentValue: currentValue,
                        profitLoss: profitLoss,
                        profitLossPercent: profitLossPercent.toFixed(2),
                        // Add a flag for animation/color
                        trend: changePercent > 0 ? 'up' : 'down'
                    };
                });

                recalculateSummary(updatedInvestments);
                return updatedInvestments;
            });
        }, 3000); // Update every 3 seconds
    };

    const stopSimulation = () => {
        if (intervalRef.current) {
            clearInterval(intervalRef.current);
        }
    };

    const recalculateSummary = (currentInvestments) => {
        const totalInvested = currentInvestments.reduce((sum, inv) => sum + inv.totalInvested, 0);
        const currentValue = currentInvestments.reduce((sum, inv) => sum + inv.currentValue, 0);
        const totalPL = currentValue - totalInvested;
        const totalPLPercent = totalInvested > 0 ? (totalPL / totalInvested) * 100 : 0;

        // Asset allocation
        const assetAllocation = {};
        currentInvestments.forEach(inv => {
            assetAllocation[inv.type] = (assetAllocation[inv.type] || 0) + inv.currentValue;
        });

        // Convert to percentages
        Object.keys(assetAllocation).forEach(key => {
            assetAllocation[key] = (assetAllocation[key] / currentValue) * 100;
        });

        setSummary({
            totalInvested,
            currentValue,
            totalProfitLoss: totalPL,
            totalProfitLossPercent: totalPLPercent.toFixed(2),
            assetAllocation,
            profitableInvestments: currentInvestments.filter(i => i.profitLoss > 0).length,
            losingInvestments: currentInvestments.filter(i => i.profitLoss < 0).length
        });
    };

    // --- CRUD Handlers ---

    const handleOpenDialog = (investment = null) => {
        if (investment) {
            setCurrentInvestment(investment);
            setFormData({
                name: investment.name,
                type: investment.type,
                quantity: investment.quantity,
                buyPrice: investment.buyPrice,
                currentPrice: investment.currentPrice || investment.buyPrice,
                purchaseDate: investment.purchaseDate,
                symbol: investment.symbol || '',
                notes: investment.notes || ''
            });
        } else {
            setCurrentInvestment(null);
            setFormData({
                name: '',
                type: 'STOCK',
                quantity: '',
                buyPrice: '',
                currentPrice: '',
                purchaseDate: format(new Date(), 'yyyy-MM-dd'),
                symbol: '',
                notes: ''
            });
        }
        setOpenDialog(true);
    };

    const handleOpenPriceDialog = (investment) => {
        setCurrentInvestment(investment);
        setNewPrice(investment.currentPrice || investment.buyPrice);
        setOpenPriceDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
        setOpenPriceDialog(false);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async () => {
        try {
            if (currentInvestment) {
                await InvestmentService.updateInvestment(currentInvestment.id, formData);
            } else {
                await InvestmentService.createInvestment(formData);
            }
            fetchData();
            handleCloseDialog();
        } catch (error) {
            console.error("Failed to save investment", error);
        }
    };

    const handlePriceUpdate = async () => {
        try {
            await InvestmentService.updateCurrentPrice(currentInvestment.id, newPrice);
            fetchData();
            handleCloseDialog();
        } catch (error) {
            console.error("Failed to update price", error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this investment?')) {
            try {
                await InvestmentService.deleteInvestment(id);
                fetchData();
            } catch (error) {
                console.error("Failed to delete investment", error);
            }
        }
    };

    // Prepare chart data
    const chartData = summary?.assetAllocation
        ? Object.entries(summary.assetAllocation).map(([name, value]) => ({ name, value }))
        : [];

    return (
        <Box>
            {/* Ticker Tape */}
            <Box sx={{
                bgcolor: 'black',
                color: '#00ff00',
                py: 1,
                overflow: 'hidden',
                whiteSpace: 'nowrap',
                fontFamily: 'monospace',
                fontSize: '0.9rem',
                borderBottom: '1px solid #333'
            }}>
                <Box sx={{
                    display: 'inline-block',
                    animation: 'ticker 20s linear infinite',
                    '@keyframes ticker': {
                        '0%': { transform: 'translateX(100%)' },
                        '100%': { transform: 'translateX(-100%)' }
                    }
                }}>
                    MARKET LIVE: &nbsp;&nbsp;
                    S&P 500 ▲ 4,783.45 (+0.45%) &nbsp;&nbsp;|&nbsp;&nbsp;
                    NASDAQ ▲ 15,123.20 (+0.82%) &nbsp;&nbsp;|&nbsp;&nbsp;
                    DOW JONES ▼ 37,401.10 (-0.12%) &nbsp;&nbsp;|&nbsp;&nbsp;
                    BTC/USD ▲ $43,250.00 (+2.10%) &nbsp;&nbsp;|&nbsp;&nbsp;
                    ETH/USD ▲ $2,340.50 (+1.85%) &nbsp;&nbsp;|&nbsp;&nbsp;
                    GOLD ▲ $2,045.30 (+0.30%) &nbsp;&nbsp;|&nbsp;&nbsp;
                    OIL ▼ $72.40 (-0.50%)
                </Box>
            </Box>

            <Container maxWidth="lg" sx={{ py: 4 }}>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
                    <Box>
                        <Typography variant="h4" fontWeight="bold" gutterBottom display="flex" alignItems="center">
                            Investment Portfolio
                            {isLive && (
                                <Fade in={true} timeout={1000}>
                                    <Chip
                                        icon={<LiveIcon sx={{ animation: 'pulse 1.5s infinite' }} />}
                                        label="LIVE MARKET"
                                        color="error"
                                        size="small"
                                        sx={{ ml: 2, fontWeight: 'bold' }}
                                    />
                                </Fade>
                            )}
                        </Typography>
                        <Typography variant="body1" color="text.secondary">
                            Track your assets and monitor performance
                        </Typography>
                    </Box>
                    <Box display="flex" alignItems="center" gap={2}>
                        <FormControlLabel
                            control={
                                <Switch
                                    checked={isLive}
                                    onChange={(e) => setIsLive(e.target.checked)}
                                    color="error"
                                />
                            }
                            label={<Typography fontWeight="bold" color={isLive ? 'error.main' : 'text.secondary'}>Live Simulator</Typography>}
                        />
                        <Button
                            variant="contained"
                            startIcon={<AddIcon />}
                            onClick={() => handleOpenDialog()}
                            sx={{ borderRadius: 2, px: 3 }}
                        >
                            Add Investment
                        </Button>
                    </Box>
                </Box>

                {loading ? (
                    <Box display="flex" justifyContent="center" py={8}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <>
                        {/* Summary Cards */}
                        <Grid container spacing={3} mb={4}>
                            <Grid item xs={12} sm={6} md={3}>
                                <Card sx={{ bgcolor: 'primary.main', color: 'primary.contrastText' }}>
                                    <CardContent>
                                        <Typography variant="subtitle2" opacity={0.8}>Total Portfolio Value</Typography>
                                        <Typography variant="h4" fontWeight="bold">₹{summary?.currentValue?.toFixed(2)}</Typography>
                                    </CardContent>
                                </Card>
                            </Grid>
                            <Grid item xs={12} sm={6} md={3}>
                                <Card>
                                    <CardContent>
                                        <Typography variant="subtitle2" color="text.secondary">Total Invested</Typography>
                                        <Typography variant="h4" fontWeight="bold">₹{summary?.totalInvested?.toFixed(2)}</Typography>
                                    </CardContent>
                                </Card>
                            </Grid>
                            <Grid item xs={12} sm={6} md={3}>
                                <Card>
                                    <CardContent>
                                        <Typography variant="subtitle2" color="text.secondary">Total Profit/Loss</Typography>
                                        <Box display="flex" alignItems="center">
                                            <Typography
                                                variant="h4"
                                                fontWeight="bold"
                                                color={summary?.totalProfitLoss >= 0 ? 'success.main' : 'error.main'}
                                            >
                                                {summary?.totalProfitLoss >= 0 ? '+' : ''}₹{summary?.totalProfitLoss?.toFixed(2)}
                                            </Typography>
                                            <Typography
                                                variant="body2"
                                                sx={{ ml: 1, color: summary?.totalProfitLoss >= 0 ? 'success.main' : 'error.main' }}
                                            >
                                                ({summary?.totalProfitLossPercent}%)
                                            </Typography>
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>
                            <Grid item xs={12} sm={6} md={3}>
                                <Card>
                                    <CardContent>
                                        <Typography variant="subtitle2" color="text.secondary">Performance</Typography>
                                        <Box display="flex" alignItems="center" mt={1}>
                                            <Box mr={2}>
                                                <Typography variant="caption" display="block">Profitable</Typography>
                                                <Typography variant="h6" color="success.main">{summary?.profitableInvestments}</Typography>
                                            </Box>
                                            <Box>
                                                <Typography variant="caption" display="block">Loss</Typography>
                                                <Typography variant="h6" color="error.main">{summary?.losingInvestments}</Typography>
                                            </Box>
                                        </Box>
                                    </CardContent>
                                </Card>
                            </Grid>
                        </Grid>

                        <Grid container spacing={3}>
                            {/* Asset Allocation Chart */}
                            <Grid item xs={12} md={4}>
                                <Paper sx={{ p: 3, height: '100%' }}>
                                    <Typography variant="h6" gutterBottom>Asset Allocation</Typography>
                                    {chartData.length > 0 ? (
                                        <Box height={300}>
                                            <ResponsiveContainer width="100%" height="100%">
                                                <PieChart>
                                                    <Pie
                                                        data={chartData}
                                                        cx="50%"
                                                        cy="50%"
                                                        innerRadius={60}
                                                        outerRadius={80}
                                                        fill="#8884d8"
                                                        paddingAngle={5}
                                                        dataKey="value"
                                                    >
                                                        {chartData.map((entry, index) => (
                                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                                        ))}
                                                    </Pie>
                                                    <RechartsTooltip />
                                                    <Legend />
                                                </PieChart>
                                            </ResponsiveContainer>
                                        </Box>
                                    ) : (
                                        <Box height={300} display="flex" alignItems="center" justifyContent="center">
                                            <Typography color="text.secondary">No data available</Typography>
                                        </Box>
                                    )}
                                </Paper>
                            </Grid>

                            {/* Investments List */}
                            <Grid item xs={12} md={8}>
                                <TableContainer component={Paper}>
                                    <Table>
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>Name</TableCell>
                                                <TableCell>Type</TableCell>
                                                <TableCell align="right">Invested</TableCell>
                                                <TableCell align="right">Current Value</TableCell>
                                                <TableCell align="right">P&L</TableCell>
                                                <TableCell align="right">Actions</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {investments.length === 0 ? (
                                                <TableRow>
                                                    <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                                                        <Typography color="text.secondary">No investments found</Typography>
                                                    </TableCell>
                                                </TableRow>
                                            ) : (
                                                investments.map((inv) => (
                                                    <TableRow key={inv.id} sx={{
                                                        transition: 'background-color 0.5s',
                                                        bgcolor: isLive && inv.trend === 'up' ? 'rgba(76, 175, 80, 0.1)' :
                                                            isLive && inv.trend === 'down' ? 'rgba(244, 67, 54, 0.1)' : 'inherit'
                                                    }}>
                                                        <TableCell>
                                                            <Typography variant="subtitle2">{inv.name}</Typography>
                                                            <Typography variant="caption" color="text.secondary">{inv.symbol}</Typography>
                                                        </TableCell>
                                                        <TableCell>
                                                            <Chip label={inv.type} size="small" variant="outlined" />
                                                        </TableCell>
                                                        <TableCell align="right">₹{inv.totalInvested.toFixed(2)}</TableCell>
                                                        <TableCell align="right" sx={{ fontWeight: isLive ? 'bold' : 'normal' }}>
                                                            ₹{inv.currentValue.toFixed(2)}
                                                        </TableCell>
                                                        <TableCell align="right">
                                                            <Box display="flex" alignItems="center" justifyContent="flex-end">
                                                                {inv.profitLoss >= 0 ? <TrendingUpIcon color="success" fontSize="small" /> : <TrendingDownIcon color="error" fontSize="small" />}
                                                                <Typography
                                                                    variant="body2"
                                                                    color={inv.profitLoss >= 0 ? 'success.main' : 'error.main'}
                                                                    ml={0.5}
                                                                >
                                                                    {inv.profitLossPercent}%
                                                                </Typography>
                                                            </Box>
                                                            <Typography variant="caption" color="text.secondary">
                                                                {inv.profitLoss >= 0 ? '+' : ''}₹{inv.profitLoss.toFixed(2)}
                                                            </Typography>
                                                        </TableCell>
                                                        <TableCell align="right">
                                                            <Tooltip title="Update Price">
                                                                <IconButton color="primary" onClick={() => handleOpenPriceDialog(inv)}>
                                                                    <ShowChartIcon />
                                                                </IconButton>
                                                            </Tooltip>
                                                            <IconButton onClick={() => handleOpenDialog(inv)}>
                                                                <EditIcon />
                                                            </IconButton>
                                                            <IconButton color="error" onClick={() => handleDelete(inv.id)}>
                                                                <DeleteIcon />
                                                            </IconButton>
                                                        </TableCell>
                                                    </TableRow>
                                                ))
                                            )}
                                        </TableBody>
                                    </Table>
                                </TableContainer>
                            </Grid>
                        </Grid>
                    </>
                )}

                {/* Add/Edit Dialog */}
                <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                    <DialogTitle>{currentInvestment ? 'Edit Investment' : 'Add New Investment'}</DialogTitle>
                    <DialogContent>
                        <Grid container spacing={2} sx={{ mt: 1 }}>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Investment Name"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleInputChange}
                                    required
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <FormControl fullWidth>
                                    <InputLabel>Type</InputLabel>
                                    <Select
                                        name="type"
                                        value={formData.type}
                                        label="Type"
                                        onChange={handleInputChange}
                                    >
                                        <MenuItem value="STOCK">Stock</MenuItem>
                                        <MenuItem value="CRYPTO">Crypto</MenuItem>
                                        <MenuItem value="MUTUAL_FUND">Mutual Fund</MenuItem>
                                        <MenuItem value="BOND">Bond</MenuItem>
                                        <MenuItem value="REAL_ESTATE">Real Estate</MenuItem>
                                        <MenuItem value="GOLD">Gold</MenuItem>
                                        <MenuItem value="OTHER">Other</MenuItem>
                                    </Select>
                                </FormControl>
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Symbol/Ticker"
                                    name="symbol"
                                    value={formData.symbol}
                                    onChange={handleInputChange}
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Quantity"
                                    name="quantity"
                                    type="number"
                                    value={formData.quantity}
                                    onChange={handleInputChange}
                                    required
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Buy Price (per unit)"
                                    name="buyPrice"
                                    type="number"
                                    value={formData.buyPrice}
                                    onChange={handleInputChange}
                                    required
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Current Price (per unit)"
                                    name="currentPrice"
                                    type="number"
                                    value={formData.currentPrice}
                                    onChange={handleInputChange}
                                    helperText="Leave empty if same as buy price"
                                />
                            </Grid>
                            <Grid item xs={6}>
                                <TextField
                                    fullWidth
                                    label="Purchase Date"
                                    name="purchaseDate"
                                    type="date"
                                    value={formData.purchaseDate}
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

                {/* Update Price Dialog */}
                <Dialog open={openPriceDialog} onClose={handleCloseDialog} maxWidth="xs" fullWidth>
                    <DialogTitle>Update Current Price</DialogTitle>
                    <DialogContent>
                        <Typography variant="body2" gutterBottom>
                            Update current market price for {currentInvestment?.name}
                        </Typography>
                        <TextField
                            fullWidth
                            label="New Price"
                            type="number"
                            value={newPrice}
                            onChange={(e) => setNewPrice(e.target.value)}
                            sx={{ mt: 2 }}
                            autoFocus
                        />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDialog}>Cancel</Button>
                        <Button onClick={handlePriceUpdate} variant="contained">Update</Button>
                    </DialogActions>
                </Dialog>
            </Container>
        </Box>
    );
};

export default InvestmentsPage;
