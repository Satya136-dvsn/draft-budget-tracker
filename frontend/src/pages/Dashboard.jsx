import { Typography, Box } from '@mui/material';
import { useAuth } from '../context/AuthContext';

const Dashboard = () => {
  const { user } = useAuth();

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Welcome, {user?.username}!
      </Typography>
      <Typography variant="body1" color="text.secondary">
        Your financial dashboard. More features coming soon.
      </Typography>
    </Box>
  );
};

export default Dashboard;
