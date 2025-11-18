import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Alert,
  Typography,
} from '@mui/material';
import savingsGoalService from '../services/savingsGoalService';

const ContributeDialog = ({ open, goal, onClose }) => {
  const [amount, setAmount] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open) {
      setAmount('');
      setError('');
    }
  }, [open]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await savingsGoalService.contribute(goal.id, parseFloat(amount));
      onClose(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add contribution');
    } finally {
      setLoading(false);
    }
  };

  if (!goal) return null;

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  };

  return (
    <Dialog open={open} onClose={() => onClose(false)} maxWidth="xs" fullWidth>
      <DialogTitle>Add Contribution</DialogTitle>
      <form onSubmit={handleSubmit}>
        <DialogContent>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}
          
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Goal: {goal.name}
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Current: {formatCurrency(goal.currentAmount)} / {formatCurrency(goal.targetAmount)}
          </Typography>
          
          <TextField
            fullWidth
            label="Contribution Amount"
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
            inputProps={{ min: 0.01, step: 0.01 }}
            sx={{ mt: 2 }}
            autoFocus
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => onClose(false)}>Cancel</Button>
          <Button type="submit" variant="contained" disabled={loading}>
            {loading ? 'Adding...' : 'Add Contribution'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default ContributeDialog;
