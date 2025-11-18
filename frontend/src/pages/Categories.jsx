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
  CardActions,
  IconButton,
  Alert,
  CircularProgress,
  Chip,
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import categoryService from '../services/categoryService';
import CategoryDialog from '../components/CategoryDialog';

const Categories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      setLoading(true);
      const response = await categoryService.getAll();
      setCategories(response.data);
      setError('');
    } catch (err) {
      console.error('Category load error:', err);
      if (err.response?.status === 403 || err.response?.status === 401) {
        // Will be handled by axios interceptor - redirect to login
        return;
      }
      const errorMsg = err.response?.status === 404
        ? 'Categories endpoint not found'
        : 'Failed to load categories';
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setSelectedCategory(null);
    setDialogOpen(true);
  };

  const handleEdit = (category) => {
    setSelectedCategory(category);
    setDialogOpen(true);
  };

  const handleDelete = async (id, isSystem) => {
    if (isSystem) {
      setError('Cannot delete system categories');
      return;
    }
    
    if (!window.confirm('Delete this category?')) return;
    
    try {
      await categoryService.delete(id);
      loadCategories();
    } catch (err) {
      setError('Failed to delete category');
    }
  };

  const handleDialogClose = (reload) => {
    setDialogOpen(false);
    setSelectedCategory(null);
    if (reload) loadCategories();
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  const systemCategories = categories.filter((c) => c.isSystem);
  const userCategories = categories.filter((c) => !c.isSystem);

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Categories</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleAdd}>
          Add Category
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        System Categories
      </Typography>
      <Grid container spacing={2} sx={{ mb: 4 }}>
        {systemCategories.map((category) => (
          <Grid item xs={12} sm={6} md={4} key={category.id}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} mb={1}>
                  <Typography variant="h4">{category.icon}</Typography>
                  <Typography variant="h6">{category.name}</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  {category.type}
                </Typography>
                <Chip label="System" size="small" sx={{ mt: 1 }} />
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
        My Categories
      </Typography>
      <Grid container spacing={2}>
        {userCategories.length === 0 ? (
          <Grid item xs={12}>
            <Paper sx={{ p: 3, textAlign: 'center' }}>
              <Typography color="text.secondary">
                No custom categories yet. Click "Add Category" to create one.
              </Typography>
            </Paper>
          </Grid>
        ) : (
          userCategories.map((category) => (
            <Grid item xs={12} sm={6} md={4} key={category.id}>
              <Card>
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    <Typography variant="h4">{category.icon}</Typography>
                    <Typography variant="h6">{category.name}</Typography>
                  </Box>
                  <Typography variant="body2" color="text.secondary">
                    {category.type}
                  </Typography>
                </CardContent>
                <CardActions>
                  <IconButton size="small" onClick={() => handleEdit(category)}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                  <IconButton
                    size="small"
                    onClick={() => handleDelete(category.id, category.isSystem)}
                  >
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </CardActions>
              </Card>
            </Grid>
          ))
        )}
      </Grid>

      <CategoryDialog
        open={dialogOpen}
        category={selectedCategory}
        onClose={handleDialogClose}
      />
    </Container>
  );
};

export default Categories;
