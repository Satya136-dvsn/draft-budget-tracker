import { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  Grid,
  Alert,
  Fade,
  Fab,
  CircularProgress,
  Chip,
} from '@mui/material';
import { Add as AddIcon, TrendingUp as TrendingUpIcon } from '@mui/icons-material';
import budgetService from '../services/budgetService';
import exportService from '../services/exportService';
import ExportMenu from '../components/common/ExportMenu';
import BudgetDialog from '../components/BudgetDialog';
import BudgetCard from '../components/budgets/BudgetCard';

const Budgets = () => {
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedBudget, setSelectedBudget] = useState(null);

  useEffect(() => {
    loadBudgets();
  }, []);

  const loadBudgets = async () => {
    try {
      setLoading(true);
      const response = await budgetService.getAll();
      setBudgets(response.data);
      setError('');
    } catch (err) {
      console.error('Budget load error:', err);
      if (err.response?.status === 403 || err.response?.status === 401) {
        return;
      }
      setError('Failed to load budgets');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setSelectedBudget(null);
    setDialogOpen(true);
  };

  const handleEdit = (budget) => {
    setSelectedBudget(budget);
    setDialogOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this budget?')) return;

    try {
      await budgetService.delete(id);
      loadBudgets();
    } catch (err) {
      setError('Failed to delete budget');
    }
  };

  const handleDialogClose = (reload) => {
    setDialogOpen(false);
    setSelectedBudget(null);
    if (reload) loadBudgets();
  };

  // Calculate summary stats
  const totalBudgeted = budgets.reduce((sum, b) => sum + (b.amount || 0), 0);
  const totalSpent = budgets.reduce((sum, b) => sum + (b.spent || 0), 0);
  const onTrackCount = budgets.filter((b) => {
    const percentage = (b.spent / b.amount) * 100;
    return percentage < 80;
  }).length;
  const nearLimitCount = budgets.filter((b) => {
    const percentage = (b.spent / b.amount) * 100;
    return percentage >= 80 && percentage < 100;
  }).length;
  const exceededCount = budgets.filter((b) => {
    const percentage = (b.spent / b.amount) * 100;
    return percentage >= 100;
  }).length;

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(amount);
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ pb: 4 }}>
      {/* Page Header */}
      <Fade in timeout={300}>
        <Box>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Box>
              <Typography variant="h4" gutterBottom sx={{ fontWeight: 700, mb: 0.5 }}>
                Budget Management
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Track your spending limits and stay on budget
              </Typography>
            </Box>
            <Box display="flex" gap={1}>
              <ExportMenu
                formats={['excel', 'pdf']}
                onExport={(format) => exportService.exportBudgets(format)}
              />
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={handleAdd}
                size="large"
              >
                Create Budget
              </Button>
            </Box>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
              {error}
            </Alert>
          )}

          {/* Summary Stats */}
          {budgets.length > 0 && (
            <Grid container spacing={2} sx={{ mb: 4 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Box p={2} bgcolor="background.paper" borderRadius={2} textAlign="center">
                  <Typography variant="caption" color="text.secondary" display="block">
                    Total Budgeted
                  </Typography>
                  <Typography variant="h5" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    {formatCurrency(totalBudgeted)}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Box p={2} bgcolor="background.paper" borderRadius={2} textAlign="center">
                  <Typography variant="caption" color="text.secondary" display="block">
                    Total Spent
                  </Typography>
                  <Typography variant="h5" sx={{ fontWeight: 700, color: 'error.main' }}>
                    {formatCurrency(totalSpent)}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={2}>
                <Box p={2} bgcolor="background.paper" borderRadius={2} textAlign="center">
                  <Typography variant="caption" color="text.secondary" display="block">
                    On Track
                  </Typography>
                  <Chip label={onTrackCount} color="success" sx={{ mt: 1, fontWeight: 600 }} />
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={2}>
                <Box p={2} bgcolor="background.paper" borderRadius={2} textAlign="center">
                  <Typography variant="caption" color="text.secondary" display="block">
                    Near Limit
                  </Typography>
                  <Chip label={nearLimitCount} color="warning" sx={{ mt: 1, fontWeight: 600 }} />
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={2}>
                <Box p={2} bgcolor="background.paper" borderRadius={2} textAlign="center">
                  <Typography variant="caption" color="text.secondary" display="block">
                    Exceeded
                  </Typography>
                  <Chip label={exceededCount} color="error" sx={{ mt: 1, fontWeight: 600 }} />
                </Box>
              </Grid>
            </Grid>
          )}

          {/* Budget Cards */}
          {budgets.length === 0 ? (
            <Fade in timeout={500}>
              <Box
                display="flex"
                flexDirection="column"
                alignItems="center"
                justifyContent="center"
                minHeight="40vh"
                textAlign="center"
                p={4}
              >
                <TrendingUpIcon sx={{ fontSize: 80, color: 'text.disabled', mb: 2 }} />
                <Typography variant="h5" gutterBottom sx={{ fontWeight: 600 }}>
                  No Budgets Created Yet
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ mb: 3, maxWidth: 500 }}>
                  Start managing your finances by creating budgets for different spending categories.
                  Set limits and track your progress to stay on top of your financial goals.
                </Typography>
                <Button
                  variant="contained"
                  startIcon={<AddIcon />}
                  onClick={handleAdd}
                  size="large"
                >
                  Create Your First Budget
                </Button>
              </Box>
            </Fade>
          ) : (
            <Grid container spacing={3}>
              {budgets.map((budget, index) => (
                <Grid item xs={12} sm={6} md={4} key={budget.id}>
                  <Fade in timeout={300 + index * 100}>
                    <Box>
                      <BudgetCard
                        budget={budget}
                        onEdit={handleEdit}
                        onDelete={handleDelete}
                      />
                    </Box>
                  </Fade>
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      </Fade>

      {/* Floating Action Button */}
      {budgets.length > 0 && (
        <Fab
          color="primary"
          aria-label="add budget"
          onClick={handleAdd}
          sx={{
            position: 'fixed',
            bottom: 32,
            right: 32,
          }}
        >
          <AddIcon />
        </Fab>
      )}

      {/* Budget Dialog */}
      <BudgetDialog
        open={dialogOpen}
        budget={selectedBudget}
        onClose={handleDialogClose}
      />
    </Container>
  );
};

export default Budgets;
