import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container, Grid, Typography, Box, Alert, Fade, Skeleton, List, ListItem, ListItemText, Button, Chip,
} from '@mui/material';
import {
  TrendingUp as IncomeIcon, TrendingDown as ExpenseIcon, AccountBalance as BalanceIcon, Savings as SavingsIcon,
  ShowChart as ChartIcon, PieChart as PieChartIcon, Receipt as TransactionsIcon, ArrowForward as ArrowForwardIcon,
} from '@mui/icons-material';
import { LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import StatCard from '../components/ui/StatCard';
import ProfessionalCard from '../components/ui/ProfessionalCard';
import dashboardService from '../services/dashboardService';
import { getCategoryColor } from '../utils/categoryColors';

const Dashboard = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [summary, setSummary] = useState(null);
  const [monthlyTrends, setMonthlyTrends] = useState([]);
  const [categoryBreakdown, setCategoryBreakdown] = useState([]);
  const [recentTransactions, setRecentTransactions] = useState([]);

  useEffect(() => { loadDashboardData(); }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [summaryRes, trendsRes, categoryRes, transactionsRes] = await Promise.all([
        dashboardService.getSummary(), dashboardService.getMonthlyTrends(), dashboardService.getCategoryBreakdown(), dashboardService.getRecentTransactions(),
      ]);
      setSummary(summaryRes.data);
      setMonthlyTrends(trendsRes.data);
      setCategoryBreakdown(categoryRes.data);
      setRecentTransactions(transactionsRes.data);
      setError('');
    } catch (err) {
      console.error('Dashboard load error:', err);
      if (err.response?.status === 403 || err.response?.status === 401) return;
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  const formatDate = (dateString) => new Date(dateString).toLocaleDateString('en-IN', { month: 'short', day: 'numeric', year: 'numeric' });

  if (loading) {
    return (
      <Container maxWidth="xl" sx={{ pb: 4 }}>
        <Skeleton variant="text" width={200} height={50} sx={{ mb: 3 }} />
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {[1, 2, 3, 4].map((i) => (
            <Grid item xs={12} sm={6} md={3} key={i}>
              <Skeleton variant="rectangular" height={140} sx={{ borderRadius: 1 }} />
            </Grid>
          ))}
        </Grid>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ pb: 4 }}>
      <Fade in timeout={300}>
        <Box mb={4}>
          <Typography variant="h4" gutterBottom sx={{ fontWeight: 700, mb: 0.5 }}>Dashboard</Typography>
          <Typography variant="body2" color="text.secondary">Your financial overview at a glance</Typography>
        </Box>
      </Fade>

      {error && (
        <Fade in timeout={300}>
          <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>{error}</Alert>
        </Fade>
      )}

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Fade in timeout={400}>
            <Box><StatCard title="Total Income" value={summary?.totalIncome ?? 0} subtitle="This Month" icon={<IncomeIcon />} color="success" loading={loading} /></Box>
          </Fade>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Fade in timeout={500}>
            <Box><StatCard title="Total Expenses" value={summary?.totalExpenses ?? 0} subtitle="This Month" icon={<ExpenseIcon />} color="error" loading={loading} /></Box>
          </Fade>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Fade in timeout={600}>
            <Box><StatCard title="Current Balance" value={summary?.balance ?? 0} subtitle="Available Now" icon={<BalanceIcon />} color="primary" loading={loading} /></Box>
          </Fade>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Fade in timeout={700}>
            <Box><StatCard title="Savings Rate" value={summary?.savingsRate != null ? `${summary.savingsRate.toFixed(1)}%` : '--'} subtitle="Of Total Income" icon={<SavingsIcon />} color="secondary" loading={loading} /></Box>
          </Fade>
        </Grid>
      </Grid>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={8}>
          <Fade in timeout={800}>
            <Box>
              <ProfessionalCard title="Monthly Trends" subheader="Income vs Expenses over the last 6 months" headerAction={<ChartIcon color="primary" />}>
                {monthlyTrends.length === 0 ? (
                  <Box display="flex" flexDirection="column" justifyContent="center" alignItems="center" minHeight={300}>
                    <ChartIcon sx={{ fontSize: 48, color: 'text.disabled', mb: 2 }} />
                    <Typography color="text.secondary">Not enough data yet to show trends.</Typography>
                    <Typography variant="caption" color="text.secondary">Add some transactions to see your financial trends</Typography>
                  </Box>
                ) : (
                  <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={monthlyTrends} margin={{ top: 10, right: 10, bottom: 0, left: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#333" />
                      <XAxis dataKey="month" stroke="#888" style={{ fontSize: '0.875rem' }} />
                      <YAxis stroke="#888" style={{ fontSize: '0.875rem' }} />
                      <Tooltip formatter={(value) => formatCurrency(value)} contentStyle={{ backgroundColor: '#1e1e1e', border: '1px solid #333', borderRadius: 8 }} />
                      <Legend />
                      <Line type="monotone" dataKey="income" name="Income" stroke="#4CAF50" strokeWidth={3} dot={{ fill: '#4CAF50', r: 4 }} activeDot={{ r: 6 }} />
                      <Line type="monotone" dataKey="expenses" name="Expenses" stroke="#F44336" strokeWidth={3} dot={{ fill: '#F44336', r: 4 }} activeDot={{ r: 6 }} />
                    </LineChart>
                  </ResponsiveContainer>
                )}
              </ProfessionalCard>
            </Box>
          </Fade>
        </Grid>

        <Grid item xs={12} md={4}>
          <Fade in timeout={900}>
            <Box>
              <ProfessionalCard title="Spending by Category" subheader="Current month breakdown" headerAction={<PieChartIcon color="primary" />}>
                {categoryBreakdown.length === 0 ? (
                  <Box display="flex" flexDirection="column" justifyContent="center" alignItems="center" minHeight={300}>
                    <PieChartIcon sx={{ fontSize: 48, color: 'text.disabled', mb: 2 }} />
                    <Typography color="text.secondary">No spending data yet</Typography>
                    <Typography variant="caption" color="text.secondary">Start adding expenses to see breakdown</Typography>
                  </Box>
                ) : (
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={categoryBreakdown}
                        dataKey="amount"
                        nameKey="categoryName"
                        cx="50%"
                        cy="50%"
                        outerRadius={100}
                        paddingAngle={2}
                        label={({ categoryName, percent }) => `${categoryName} (${(percent * 100).toFixed(0)}%)`}
                        labelLine={{ stroke: '#666', strokeWidth: 1 }}
                        style={{ fontSize: '13px', fontWeight: 600, fill: '#fff', textShadow: '1px 1px 2px rgba(0,0,0,0.8)' }}
                      >
                        {categoryBreakdown.map((entry, index) => (
                          <Cell key={entry.categoryId} fill={getCategoryColor(entry.categoryId, index)} stroke="#1a1a1a" strokeWidth={2} />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value, name) => [formatCurrency(value), name]}
                        contentStyle={{ backgroundColor: '#fff', border: '1px solid #ccc', borderRadius: '8px', padding: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}
                        labelStyle={{ color: '#000', fontWeight: 600, fontSize: '14px', marginBottom: '4px' }}
                        itemStyle={{ color: '#333', fontSize: '13px', padding: '4px 0' }}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </ProfessionalCard>
            </Box>
          </Fade>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Fade in timeout={1000}>
            <Box>
              <ProfessionalCard
                title="Recent Transactions"
                subheader="Latest financial activity"
                headerAction={summary && <Chip label={`${summary.transactionCount ?? 0} this month`} size="small" color="primary" variant="outlined" />}
                actions={<Button size="small" endIcon={<ArrowForwardIcon />} onClick={() => navigate('/transactions')}>View All Transactions</Button>}
              >
                {recentTransactions.length === 0 ? (
                  <Box py={4} textAlign="center">
                    <TransactionsIcon sx={{ fontSize: 48, color: 'text.disabled', mb: 2 }} />
                    <Typography color="text.secondary">No recent transactions found</Typography>
                    <Typography variant="caption" color="text.secondary">Start tracking your finances by adding transactions</Typography>
                  </Box>
                ) : (
                  <List sx={{ py: 0 }}>
                    {recentTransactions.map((tx, index) => (
                      <ListItem key={tx.id} divider={index < recentTransactions.length - 1} sx={{ px: 0, '&:hover': { backgroundColor: 'rgba(255, 255, 255, 0.03)', borderRadius: 1 } }}>
                        <ListItemText
                          primary={<Typography variant="body1" sx={{ fontWeight: 500 }}>{tx.description}</Typography>}
                          secondary={<Typography variant="body2" color="text.secondary">{formatDate(tx.transactionDate)} · {tx.categoryName || 'Uncategorized'}</Typography>}
                        />
                        <Typography variant="h6" sx={{ fontWeight: 600, color: tx.type === 'INCOME' ? 'success.main' : 'error.main' }}>
                          {tx.type === 'INCOME' ? '+' : '-'}{formatCurrency(tx.amount)}
                        </Typography>
                      </ListItem>
                    ))}
                  </List>
                )}
              </ProfessionalCard>
            </Box>
          </Fade>
        </Grid>

        <Grid item xs={12} md={4}>
          <Fade in timeout={1100}>
            <Box>
              <ProfessionalCard title="Quick Stats" subheader="Your financial snapshot">
                <Box display="flex" flexDirection="column" gap={3}>
                  <Box>
                    <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5, display: 'block' }}>Active Budgets</Typography>
                    <Typography variant="h3" sx={{ fontWeight: 700, color: 'primary.main' }}>{summary?.budgetCount ?? 0}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5, display: 'block' }}>Savings Goals</Typography>
                    <Typography variant="h3" sx={{ fontWeight: 700, color: 'secondary.main' }}>{summary?.goalCount ?? 0}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5, display: 'block' }}>Transactions (This Month)</Typography>
                    <Typography variant="h3" sx={{ fontWeight: 700 }}>{summary?.transactionCount ?? 0}</Typography>
                  </Box>
                </Box>
                <Box mt={3}>
                  <Button variant="outlined" fullWidth onClick={() => navigate('/budgets')} sx={{ mb: 1 }}>Manage Budgets</Button>
                  <Button variant="outlined" fullWidth onClick={() => navigate('/goals')}>Track Goals</Button>
                </Box>
              </ProfessionalCard>
            </Box>
          </Fade>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;
