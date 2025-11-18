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
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, AddCircle as ContributeIcon } from '@mui/icons-material';
import savingsGoalService from '../services/savingsGoalService';
import SavingsGoalDialog from '../components/SavingsGoalDialog';
import ContributeDialog from '../components/ContributeDialog';

const SavingsGoals = () => {
  const [goals, setGoals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [contributeDialogOpen, setContributeDialogOpen] = useState(false);
  const [selectedGoal, setSelectedGoal] = useState(null);

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
      console.error('Goals load error:', err);
      if (err.response?.status === 403 || err.response?.status === 401) {
        return;
      }
      setError('Failed to load savings goals');
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

  const handleContribute = (goal) => {
    setSelectedGoal(goal);
    setContributeDialogOpen(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this savings goal?')) return;
    
    try {
      await savingsGoalService.delete(id);
      loadGoals();
    } catch (err) {
      setError('Failed to delete goal');
    }
  };

  const handleDialogClose = (reload) => {
    setDialogOpen(false);
    setContributeDialogOpen(false);
    setSelectedGoal(null);
    if (reload) loadGoals();
  };

  const calculateProgress = (current, target) => {
    return Math.min((current / target) * 100, 100);
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
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
        <Typography variant="h4">Savings Goals</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleAdd}>
          Add Goal
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        {goals.length === 0 ? (
          <Grid item xs={12}>
            <Paper sx={{ p: 3, textAlign: 'center' }}>
              <Typography color="text.secondary">
                No savings goals yet. Click "Add Goal" to create one.
              </Typography>
            </Paper>
          </Grid>
        ) : (
          goals.map((goal) => {
            const progress = calculateProgress(goal.currentAmount, goal.targetAmount);
            const remaining = goal.targetAmount - goal.currentAmount;
            const isCompleted = goal.currentAmount >= goal.targetAmount;
            
            return (
              <Grid item xs={12} md={6} key={goal.id}>
                <Card>
                  <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="start" mb={2}>
                      <Box>
                        <Typography variant="h6">{goal.name}</Typography>
                        <Typography variant="body2" color="text.secondary">
                          Target: {formatDate(goal.targetDate)}
                        </Typography>
                      </Box>
                      <Box>
                        <IconButton size="small" onClick={() => handleContribute(goal)} disabled={isCompleted}>
                          <ContributeIcon fontSize="small" />
                        </IconButton>
                        <IconButton size="small" onClick={() => handleEdit(goal)}>
                          <EditIcon fontSize="small" />
                        </IconButton>
                        <IconButton size="small" onClick={() => handleDelete(goal.id)}>
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </Box>
                    </Box>

                    <Box mb={2}>
                      <Box display="flex" justifyContent="space-between" mb={1}>
                        <Typography variant="body2">
                          Saved: {formatCurrency(goal.currentAmount)}
                        </Typography>
                        <Typography variant="body2">
                          Target: {formatCurrency(goal.targetAmount)}
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={progress}
                        color={isCompleted ? 'success' : 'primary'}
                        sx={{ height: 8, borderRadius: 4 }}
                      />
                      <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        {isCompleted ? (
                          <>Goal Achieved! 🎉</>
                        ) : (
                          <>Remaining: {formatCurrency(remaining)}</>
                        )}
                      </Typography>
                    </Box>

                    {isCompleted && (
                      <Chip label="Completed" color="success" size="small" />
                    )}
                  </CardContent>
                </Card>
              </Grid>
            );
          })
        )}
      </Grid>

      <SavingsGoalDialog
        open={dialogOpen}
        goal={selectedGoal}
        onClose={handleDialogClose}
      />
      
      <ContributeDialog
        open={contributeDialogOpen}
        goal={selectedGoal}
        onClose={handleDialogClose}
      />
    </Container>
  );
};

export default SavingsGoals;
