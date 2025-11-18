import { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  Paper,
  Grid,
  Card,
  CardContent,
  LinearProgress,
  IconButton,
  Alert,
  CircularProgress,
  Chip,
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import budgetService from '../services/budgetService';
import BudgetDialog from '../components/BudgetDialog';

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

  const calculateProgress = (spent, limit) => {
    return Math.min((spent / limit) * 100, 100);
  };

  const getProgressColor = (percentage) => {
    if (percentage >= 100) return 'error';
    if (percentage >= 80) return 'warning';
    return 'success';
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
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
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Budgets</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleAdd}>
          Add Budget
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {budgets.length === 0 ? (
          <Grid item xs={12}>
            <Paper sx={{ p: 3, textAlign: 'center' }}>
              <Typography color="text.secondary">
                No budgets yet. Click "Add Budget" to create one.
              </Typography>
            </Paper>
          </Grid>
        ) : (
          budgets.map((budget) => {
            const progress = calculateProgress(budget.spent, budget.limitAmount);
            const remaining = budget.limitAmount - budget.spent;
            
            return (
              <Grid item xs={12} md={6} key={budget.id}>
                <Card>
                  <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="start" mb={2}>
                      <Box>
                        <Typography variant="h6">{budget.categoryName || 'General'}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          {budget.period}
                        </Typography>
                      </Box>
                      <Box>
                        <IconButton size="small" onClick={() => handleEdit(budget)}>
                          <EditIcon fontSize="small" />
                        </IconButton>
                        <IconButton size="small" onClick={() => handleDelete(budget.id)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Box>
                    </Box>

                    <Box mb={2}>
                      <Box display="flex" justifyContent="space-between" mb={1}>
                        <Typography variant="body2">
                          Spent: {formatCurrency(budget.spent)}
                        </Typography>
                        <Typography variant="body2">
                          Limit: {formatCurrency(budget.limitAmount)}
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={progress}
                        color={getProgressColor(progress)}
                        sx={{ height: 8, borderRadius: 4 }}
                      />
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        {remaining >= 0 ? (
                          <>Remaining: {formatCurrency(remaining)}</>
                        ) : (
                          <>Over budget by: {formatCurrency(Math.abs(remaining))}</>
                        )}
                      </Typography>
                    </Box>

                    {progress >= 100 && (
                      <Chip label="Over Budget" color="error" size="small" />
                    )}
                    {progress >= 80 && progress < 100 && (
                      <Chip label="Near Limit" color="warning" size="small" />
                    )}
                  </CardContent>
                </Card>
              </Grid>
            );
          })
        )}
      </Grid>

      <BudgetDialog
        open={dialogOpen}
        budget={selectedBudget}
        onClose={handleDialogClose}
      />
    </Container>
  );
};

export default Budgets;
