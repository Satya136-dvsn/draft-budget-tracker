import { useState, useEffect, useMemo } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  TableSortLabel,
  IconButton,
  Checkbox,
  Chip,
  Alert,
  Toolbar,
  Tooltip,
  Fade,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  MoreVert as MoreVertIcon,
  FileDownload as ExportIcon,
  Category as CategoryIcon,
  DeleteSweep as BulkDeleteIcon,
} from '@mui/icons-material';
import transactionService from '../services/transactionService';
import categoryService from '../services/categoryService';
import exportService from '../services/exportService';
import ExportMenu from '../components/common/ExportMenu';
import TransactionDialog from '../components/TransactionDialog';
import TransactionFilters from '../components/transactions/TransactionFilters';

const Transactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState(null);

  // Table state
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [orderBy, setOrderBy] = useState('transactionDate');
  const [order, setOrder] = useState('desc');
  const [selected, setSelected] = useState([]);

  // Filter state
  const [filters, setFilters] = useState({
    startDate: null,
    endDate: null,
    categories: [],
    type: 'ALL',
    minAmount: 0,
    maxAmount: 100000,
    searchText: '',
  });

  // Action menu state
  const [anchorEl, setAnchorEl] = useState(null);
  const [menuTransaction, setMenuTransaction] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [transactionsRes, categoriesRes] = await Promise.all([
        transactionService.getAll({ page: 0, size: 1000 }),
        categoryService.getAll(),
      ]);

      const transactionData = transactionsRes.data;
      const items = Array.isArray(transactionData)
        ? transactionData
        : transactionData?.content || [];
      setTransactions(items);

      const categoryData = categoriesRes.data;
      const categoryItems = Array.isArray(categoryData)
        ? categoryData
        : categoryData?.content || [];
      setCategories(categoryItems);

      setError('');
    } catch (err) {
      console.error('Data load error:', err);
      if (err.response?.status === 403 || err.response?.status === 401) {
        return;
      }
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  // Filter and sort transactions
  const filteredTransactions = useMemo(() => {
    return transactions.filter((tx) => {
      // Date filter
      if (filters.startDate && new Date(tx.transactionDate) < filters.startDate) return false;
      if (filters.endDate && new Date(tx.transactionDate) > filters.endDate) return false;

      // Category filter
      if (filters.categories.length > 0 && !filters.categories.includes(tx.categoryId)) return false;

      // Type filter
      if (filters.type !== 'ALL' && tx.type !== filters.type) return false;

      // Amount filter
      if (tx.amount < filters.minAmount || tx.amount > filters.maxAmount) return false;

      // Search filter
      if (filters.searchText && !tx.description.toLowerCase().includes(filters.searchText.toLowerCase())) {
        return false;
      }

      return true;
    });
  }, [transactions, filters]);

  const sortedTransactions = useMemo(() => {
    const comparator = (a, b) => {
      let aVal = a[orderBy];
      let bVal = b[orderBy];

      if (orderBy === 'transactionDate') {
        aVal = new Date(aVal);
        bVal = new Date(bVal);
      }

      if (bVal < aVal) return order === 'asc' ? 1 : -1;
      if (bVal > aVal) return order === 'asc' ? -1 : 1;
      return 0;
    };

    return [...filteredTransactions].sort(comparator);
  }, [filteredTransactions, order, orderBy]);

  const paginatedTransactions = useMemo(() => {
    return sortedTransactions.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
  }, [sortedTransactions, page, rowsPerPage]);

  const handleRequestSort = (property) => {
    const isAsc = orderBy === property && order === 'asc';
    setOrder(isAsc ? 'desc' : 'asc');
    setOrderBy(property);
  };

  const handleSelectAllClick = (event) => {
    if (event.target.checked) {
      const newSelected = paginatedTransactions.map((tx) => tx.id);
      setSelected(newSelected);
    } else {
      setSelected([]);
    }
  };

  const handleSelectClick = (id) => {
    const selectedIndex = selected.indexOf(id);
    let newSelected = [];

    if (selectedIndex === -1) {
      newSelected = newSelected.concat(selected, id);
    } else if (selectedIndex === 0) {
      newSelected = newSelected.concat(selected.slice(1));
    } else if (selectedIndex === selected.length - 1) {
      newSelected = newSelected.concat(selected.slice(0, -1));
    } else if (selectedIndex > 0) {
      newSelected = newSelected.concat(
        selected.slice(0, selectedIndex),
        selected.slice(selectedIndex + 1)
      );
    }

    setSelected(newSelected);
  };

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleAdd = () => {
    setSelectedTransaction(null);
    setDialogOpen(true);
  };

  const handleEdit = (transaction) => {
    setSelectedTransaction(transaction);
    setDialogOpen(true);
    handleMenuClose();
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this transaction?')) return;

    try {
      await transactionService.delete(id);
      loadData();
      setSelected(selected.filter((selectedId) => selectedId !== id));
      handleMenuClose();
    } catch (err) {
      setError('Failed to delete transaction');
    }
  };

  const handleBulkDelete = async () => {
    if (!window.confirm(`Delete ${selected.length} selected transactions?`)) return;

    try {
      await Promise.all(selected.map((id) => transactionService.delete(id)));
      loadData();
      setSelected([]);
    } catch (err) {
      setError('Failed to delete transactions');
    }
  };

  const handleExportCSV = () => {
    const dataToExport = selected.length > 0
      ? transactions.filter((tx) => selected.includes(tx.id))
      : filteredTransactions;

    const csv = [
      ['Date', 'Description', 'Category', 'Type', 'Amount'].join(','),
      ...dataToExport.map((tx) =>
        [
          formatDate(tx.transactionDate),
          `"${tx.description}"`,
          tx.categoryName || 'Uncategorized',
          tx.type,
          tx.amount,
        ].join(',')
      ),
    ].join('\n');

    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `transactions_${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    window.URL.revokeObjectURL(url);
    setSelected([]);
  };

  const handleDialogClose = (reload) => {
    setDialogOpen(false);
    setSelectedTransaction(null);
    if (reload) loadData();
  };

  const handleMenuOpen = (event, transaction) => {
    setAnchorEl(event.currentTarget);
    setMenuTransaction(transaction);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setMenuTransaction(null);
  };

  const formatAmount = (amount, type) => {
    const formatted = new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(Math.abs(amount));
    return type === 'EXPENSE' ? `-${formatted}` : formatted;
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      timeZone: 'Asia/Kolkata',
    });
  };

  const isSelected = (id) => selected.indexOf(id) !== -1;
  const numSelected = selected.length;
  const rowCount = paginatedTransactions.length;

  return (
    <Container maxWidth="xl" sx={{ pb: 4 }}>
      {/* Page Header */}
      <Fade in timeout={300}>
        <Box>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Box>
              <Typography variant="h4" gutterBottom sx={{ fontWeight: 700, mb: 0.5 }}>
                Transactions
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Manage all your income and expenses
              </Typography>
            </Box>
            <Box display="flex" gap={1}>
              <ExportMenu
                formats={['csv', 'excel', 'pdf']}
                onExport={(format) => exportService.exportTransactions(
                  filters.startDate,
                  filters.endDate,
                  format
                )}
              />
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={handleAdd}
                size="large"
              >
                Add Transaction
              </Button>
            </Box>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
              {error}
            </Alert>
          )}

          {/* Filters */}
          <TransactionFilters
            categories={categories}
            onFilterChange={setFilters}
            activeFilters={filters}
          />

          <Box>

            {/* Bulk Actions Toolbar */}
            {numSelected > 0 && (
              <Fade in>
                <Paper sx={{ mb: 2, p: 2, bgcolor: 'primary.main', color: 'primary.contrastText' }}>
                  <Toolbar disableGutters>
                    <Typography sx={{ flex: '1 1 100%' }} variant="subtitle1">
                      {numSelected} selected
                    </Typography>
                    <Tooltip title="Delete selected">
                      <IconButton color="inherit" onClick={handleBulkDelete}>
                        <BulkDeleteIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Export selected">
                      <IconButton color="inherit" onClick={handleExportCSV}>
                        <ExportIcon />
                      </IconButton>
                    </Tooltip>
                  </Toolbar>
                </Paper>
              </Fade>
            )}

            {/* Data Table */}
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox
                        color="primary"
                        indeterminate={numSelected > 0 && numSelected < rowCount}
                        checked={rowCount > 0 && numSelected === rowCount}
                        onChange={handleSelectAllClick}
                      />
                    </TableCell>
                    <TableCell sortDirection={orderBy === 'transactionDate' ? order : false}>
                      <TableSortLabel
                        active={orderBy === 'transactionDate'}
                        direction={orderBy === 'transactionDate' ? order : 'asc'}
                        onClick={() => handleRequestSort('transactionDate')}
                      >
                        Date
                      </TableSortLabel>
                    </TableCell>
                    <TableCell sortDirection={orderBy === 'description' ? order : false}>
                      <TableSortLabel
                        active={orderBy === 'description'}
                        direction={orderBy === 'description' ? order : 'asc'}
                        onClick={() => handleRequestSort('description')}
                      >
                        Description
                      </TableSortLabel>
                    </TableCell>
                    <TableCell>Category</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell align="right" sortDirection={orderBy === 'amount' ? order : false}>
                      <TableSortLabel
                        active={orderBy === 'amount'}
                        direction={orderBy === 'amount' ? order : 'asc'}
                        onClick={() => handleRequestSort('amount')}
                      >
                        Amount
                      </TableSortLabel>
                    </TableCell>
                    <TableCell align="center">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedTransactions.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} align="center">
                        <Typography color="text.secondary" py={4}>
                          {loading ? 'Loading transactions...' : 'No transactions found. Try adjusting your filters or add a new transaction.'}
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    paginatedTransactions.map((transaction) => {
                      const isItemSelected = isSelected(transaction.id);

                      return (
                        <TableRow
                          key={transaction.id}
                          hover
                          selected={isItemSelected}
                          sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                        >
                          <TableCell padding="checkbox">
                            <Checkbox
                              color="primary"
                              checked={isItemSelected}
                              onChange={() => handleSelectClick(transaction.id)}
                            />
                          </TableCell>
                          <TableCell>{formatDate(transaction.transactionDate)}</TableCell>
                          <TableCell>
                            <Typography variant="body2" sx={{ fontWeight: 500 }}>
                              {transaction.description}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={transaction.categoryName || 'Uncategorized'}
                              size="small"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={transaction.type}
                              color={transaction.type === 'INCOME' ? 'success' : 'error'}
                              size="small"
                            />
                          </TableCell>
                          <TableCell
                            align="right"
                            sx={{
                              color: transaction.type === 'INCOME' ? 'success.main' : 'error.main',
                              fontWeight: 600,
                              fontSize: '0.95rem',
                            }}
                          >
                            {formatAmount(transaction.amount, transaction.type)}
                          </TableCell>
                          <TableCell align="center">
                            <IconButton
                              size="small"
                              onClick={(e) => handleMenuOpen(e, transaction)}
                            >
                              <MoreVertIcon fontSize="small" />
                            </IconButton>
                          </TableCell>
                        </TableRow>
                      );
                    })
                  )}
                </TableBody>
              </Table>
              <TablePagination
                rowsPerPageOptions={[10, 25, 50, 100]}
                component="div"
                count={sortedTransactions.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={handleChangePage}
                onRowsPerPageChange={handleChangeRowsPerPage}
              />
            </TableContainer>

            {/* Results Summary */}
            <Box mt={2} display="flex" justifyContent="space-between" alignItems="center">
              <Typography variant="body2" color="text.secondary">
                Showing {paginatedTransactions.length} of {sortedTransactions.length} transactions
                {sortedTransactions.length !== transactions.length && ` (filtered from ${transactions.length} total)`}
              </Typography>
            </Box>
          </Box>
        </Box>
      </Fade>

      {/* Action Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => handleEdit(menuTransaction)}>
          <ListItemIcon>
            <EditIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Edit</ListItemText>
        </MenuItem>
        <MenuItem onClick={() => handleDelete(menuTransaction?.id)}>
          <ListItemIcon>
            <DeleteIcon fontSize="small" color="error" />
          </ListItemIcon>
          <ListItemText>Delete</ListItemText>
        </MenuItem>
      </Menu>

      {/* Transaction Dialog */}
      <TransactionDialog
        open={dialogOpen}
        transaction={selectedTransaction}
        onClose={handleDialogClose}
      />
    </Container>
  );
};

export default Transactions;
