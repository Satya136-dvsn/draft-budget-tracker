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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material';
import { Add as AddIcon, EmojiEvents as TrophyIcon, AttachMoney as MoneyIcon } from '@mui/icons-material';
import savingsGoalService from '../services/savingsGoalService';
import SavingsGoalDialog from '../components/SavingsGoalDialog';
import GoalCard from '../components/goals/GoalCard';

const SavingsGoals = () => {
  const [goals, setGoals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedGoal, setSelectedGoal] = useState(null);

  // Contribution dialog
  const [contributeDialogOpen, setContributeDialogOpen] = useState(false);
  const [contributeGoal, setContributeGoal] = useState(null);
  const [contributeAmount, setContributeAmount] = useState('');

  // Withdraw dialog
  const [withdrawDialogOpen, setWithdrawDialogOpen] = useState(false);
  const [withdrawGoal, setWithdrawGoal] = useState(null);
  const [withdrawAmount, setWithdrawAmount] = useState('');

  useEffect(() => {
    loadGoals();
  }, []);

  const loadGoals = async () => {
    try {
      setLoading(true);
      const response = await savingsGoalService.getAll();
      setGoals(response.data);
      setError('');
    } catch (err) {
      console.error('Goal load error:', err);
      if (err.response?.status === 403 || err.response?.status === 401) {
        return;
      }
      setError('Failed to load goals');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setSelectedGoal(null);
    setDialogOpen(true);
  };

  const handleEdit = (goal) => {
    setSelectedGoal(goal);
    setDialogOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this goal?')) return;

    try {
      await savingsGoalService.delete(id);
      loadGoals();
    } catch (err) {
      setError('Failed to delete goal');
    }
  };

  const handleDialogClose = (reload) => {
    setDialogOpen(false);
    setSelectedGoal(null);
    if (reload) loadGoals();
  };

  const handleContribute = (goal) => {
    setContributeGoal(goal);
    setContributeAmount('');
    setContributeDialogOpen(true);
  };

  const handleWithdraw = (goal) => {
    setWithdrawGoal(goal);
    setWithdrawAmount('');
    setWithdrawDialogOpen(true);
  };

  const handleContributeSubmit = async () => {
    if (!contributeAmount || parseFloat(contributeAmount) <= 0) {
      setError('Please enter a valid contribution amount');
      return;
    }

    try {
      await savingsGoalService.contribute(contributeGoal.id, parseFloat(contributeAmount));
      setContributeDialogOpen(false);
      setContributeGoal(null);
      setContributeAmount('');
      loadGoals();
    } catch (err) {
      setError('Failed to add contribution');
    }
  };

  const handleWithdrawSubmit = async () => {
    if (!withdrawAmount || parseFloat(withdrawAmount) <= 0) {
      setError('Please enter a valid withdrawal amount');
      return;
    }

    if (parseFloat(withdrawAmount) > (withdrawGoal?.currentAmount || 0)) {
      setError('Insufficient funds in savings goal');
      return;
    }

    try {
      await savingsGoalService.withdraw(withdrawGoal.id, parseFloat(withdrawAmount));
      setWithdrawDialogOpen(false);
      setWithdrawGoal(null);
      setWithdrawAmount('');
      loadGoals();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to withdraw amount');
    }
  };

  // Calculate summary stats
  const totalGoalAmount = goals.reduce((sum, g) => sum + (g.targetAmount || 0), 0);
  const totalSaved = goals.reduce((sum, g) => sum + (g.currentAmount || 0), 0);
  const completedGoals = goals.filter((g) => (g.currentAmount / g.targetAmount) * 100 >= 100).length;
  const activeGoals = goals.filter((g) => (g.currentAmount / g.targetAmount) * 100 < 100).length;

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
                Savings Goals
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Track your progress towards your financial dreams
              </Typography>
            </Box>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={handleAdd}
              size="large"
            >
              Create Goal
            </Button>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
              {error}
            </Alert>
          )}

          {/* Summary Stats */}
          {goals.length > 0 && (
            <Grid container spacing={2} sx={{ mb: 4 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Box p={2} bgcolor="background.paper" borderRadius={2} textAlign="center">
                  <Typography variant="caption" color="text.secondary" display="block">
                    Total Goal Amount
                  </Typography>
                  <Typography variant="h5" sx={{ fontWeight: 700, color: 'primary.main' }}>
                    {formatCurrency(totalGoalAmount)}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Box p={2} bgcolor="background.paper" borderRadius={2} textAlign="center">
                  <Typography variant="caption" color="text.secondary" display="block">
                    Total Saved
                  </Typography>
                  <Typography variant="h5" sx={{ fontWeight: 700, color: 'success.main' }}>
                    {formatCurrency(totalSaved)}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Box p={2} bgcolor="background.paper" borderRadius={2} textAlign="center">
                  <Typography variant="caption" color="text.secondary" display="block">
                    Active Goals
                  </Typography>
                  <Typography variant="h5" sx={{ fontWeight: 700 }}>
                    {activeGoals}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Box p={2} bgcolor="background.paper" borderRadius={2} textAlign="center">
                  <Typography variant="caption" color="text.secondary" display="block">
                    Completed Goals
                  </Typography>
                  <Typography variant="h5" sx={{ fontWeight: 700, color: 'warning.main' }}>
                    🏆 {completedGoals}
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          )}

          {/* Goal Cards */}
          {goals.length === 0 ? (
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
                <TrophyIcon sx={{ fontSize: 80, color: 'text.disabled', mb: 2 }} />
                <Typography variant="h5" gutterBottom sx={{ fontWeight: 600 }}>
                  No Savings Goals Yet
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ mb: 3, maxWidth: 500 }}>
                  Set your financial goals and watch your dreams come true. Define targets,
                  track progress, and celebrate milestones along the way!
                </Typography>
                <Button
                  variant="contained"
                  startIcon={<AddIcon />}
                  onClick={handleAdd}
                  size="large"
                >
                  Create Your First Goal
                </Button>
              </Box>
            </Fade>
          ) : (
            <Grid container spacing={3}>
              {goals.map((goal, index) => (
                <Grid item xs={12} sm={6} md={4} key={goal.id}>
                  <Fade in timeout={300 + index * 100}>
                    <Box>
                      <GoalCard
                        goal={goal}
                        onEdit={handleEdit}
                        onDelete={handleDelete}
                        onContribute={handleContribute}
                        onWithdraw={handleWithdraw}
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
      {goals.length > 0 && (
        <Fab
          color="primary"
          aria-label="add goal"
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

      {/* Goal Dialog */}
      <SavingsGoalDialog
        open={dialogOpen}
        goal={selectedGoal}
        onClose={handleDialogClose}
      />

      {/* Contribution Dialog */}
      <Dialog open={contributeDialogOpen} onClose={() => setContributeDialogOpen(false)}>
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <MoneyIcon color="primary" />
            Add Contribution
          </Box>
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Adding to: <strong>{contributeGoal?.name}</strong>
          </Typography>
          <TextField
            autoFocus
            margin="dense"
            label="Contribution Amount"
            type="number"
            fullWidth
            variant="outlined"
            value={contributeAmount}
            onChange={(e) => setContributeAmount(e.target.value)}
            InputProps={{
              startAdornment: <Typography color="text.secondary" sx={{ mr: 1 }}>₹</Typography>,
            }}
            helperText={`Current: ${formatCurrency(contributeGoal?.currentAmount || 0)} / Target: ${formatCurrency(contributeGoal?.targetAmount || 0)}`}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setContributeDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleContributeSubmit} variant="contained">
            Add Contribution
          </Button>
        </DialogActions>
      </Dialog>

      {/* Withdraw Dialog */}
      <Dialog open={withdrawDialogOpen} onClose={() => setWithdrawDialogOpen(false)}>
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <MoneyIcon color="warning" />
            Withdraw Funds
          </Box>
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Withdrawing from: <strong>{withdrawGoal?.name}</strong>
          </Typography>
          <TextField
            autoFocus
            margin="dense"
            label="Withdrawal Amount"
            type="number"
            fullWidth
            variant="outlined"
            value={withdrawAmount}
            onChange={(e) => setWithdrawAmount(e.target.value)}
            InputProps={{
              startAdornment: <Typography color="text.secondary" sx={{ mr: 1 }}>₹</Typography>,
            }}
            helperText={`Available: ${formatCurrency(withdrawGoal?.currentAmount || 0)}`}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setWithdrawDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleWithdrawSubmit} variant="contained" color="warning">
            Withdraw
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default SavingsGoals;
