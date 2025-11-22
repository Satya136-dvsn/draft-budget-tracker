import {
    Card,
    CardContent,
    CardActions,
    Box,
    Typography,
    CircularProgress,
    IconButton,
    Chip,
    LinearProgress,
    Tooltip,
} from '@mui/material';
import {
    Edit as EditIcon,
    Delete as DeleteIcon,
    CurrencyRupee as RupeeIcon,
    TrendingUp as TrendingUpIcon,
    EmojiEvents as TrophyIcon,
    Remove as RemoveIcon,
} from '@mui/icons-material';

const GoalCard = ({ goal, onEdit, onDelete, onContribute, onWithdraw }) => {
    const { name, targetAmount, currentAmount, deadline, category } = goal;

    const percentage = (currentAmount / targetAmount) * 100;
    const remaining = targetAmount - currentAmount;
    const isCompleted = percentage >= 100;

    // Calculate timeline progress
    const startDate = new Date(goal.startDate || Date.now());
    const endDate = new Date(deadline);
    const today = new Date();
    const totalDuration = endDate - startDate;
    const elapsed = today - startDate;
    const timePercentage = (elapsed / totalDuration) * 100;
    const daysRemaining = Math.ceil((endDate - today) / (1000 * 60 * 60 * 24));

    // Calculate required monthly savings
    const monthsRemaining = daysRemaining / 30;
    const requiredMonthly = monthsRemaining > 0 ? remaining / monthsRemaining : 0;

    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0,
        }).format(value);
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('en-IN', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
        });
    };

    // Milestones
    const milestones = [
        { label: '25%', value: 25, amount: targetAmount * 0.25 },
        { label: '50%', value: 50, amount: targetAmount * 0.5 },
        { label: '75%', value: 75, amount: targetAmount * 0.75 },
        { label: '100%', value: 100, amount: targetAmount },
    ];

    return (
        <Card
            sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                position: 'relative',
                transition: 'all 0.3s ease',
                background: isCompleted
                    ? 'linear-gradient(135deg, rgba(76,175,80,0.1) 0%, rgba(255,193,7,0.1) 100%)'
                    : 'inherit',
                '&:hover': {
                    boxShadow: (theme) => `0 8px 24px ${theme.palette.action.hover}`,
                    transform: 'translateY(-4px)',
                },
            }}
        >
            <CardContent sx={{ flexGrow: 1, pb: 1 }}>
                {/* Header */}
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                    <Box flex={1}>
                        <Typography variant="h6" sx={{ fontWeight: 600, mb: 0.5 }}>
                            {name}
                        </Typography>
                        {category && (
                            <Chip label={category} size="small" variant="outlined" sx={{ fontSize: '0.75rem' }} />
                        )}
                    </Box>
                    {isCompleted && (
                        <TrophyIcon sx={{ color: 'warning.main', fontSize: 32 }} />
                    )}
                </Box>

                {/* Circular Progress */}
                <Box display="flex" justifyContent="center" alignItems="center" my={3} position="relative">
                    <Box position="relative" display="inline-flex">
                        <CircularProgress
                            variant="determinate"
                            value={Math.min(percentage, 100)}
                            size={120}
                            thickness={6}
                            sx={{
                                color: isCompleted ? 'success.main' : percentage >= 75 ? 'warning.main' : 'primary.main',
                            }}
                        />
                        <Box
                            position="absolute"
                            top={0}
                            left={0}
                            bottom={0}
                            right={0}
                            display="flex"
                            flexDirection="column"
                            alignItems="center"
                            justifyContent="center"
                        >
                            <Typography variant="h5" sx={{ fontWeight: 700 }}>
                                {Math.min(percentage, 100).toFixed(0)}%
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                                Complete
                            </Typography>
                        </Box>
                    </Box>
                </Box>

                {/* Amount Progress */}
                <Box mb={2}>
                    <Box display="flex" justifyContent="space-between" mb={1}>
                        <Box>
                            <Typography variant="caption" color="text.secondary" display="block">
                                Current
                            </Typography>
                            <Typography variant="h6" sx={{ fontWeight: 600, color: 'success.main' }}>
                                {formatCurrency(currentAmount)}
                            </Typography>
                        </Box>
                        <Box textAlign="right">
                            <Typography variant="caption" color="text.secondary" display="block">
                                Target
                            </Typography>
                            <Typography variant="h6" sx={{ fontWeight: 600 }}>
                                {formatCurrency(targetAmount)}
                            </Typography>
                        </Box>
                    </Box>
                    {!isCompleted && (
                        <Typography variant="body2" color="text.secondary">
                            {formatCurrency(remaining)} remaining
                        </Typography>
                    )}
                </Box>

                {/* Milestones */}
                <Box mb={2}>
                    <Typography variant="caption" color="text.secondary" display="block" mb={1}>
                        Milestones
                    </Typography>
                    <Box display="flex" justifyContent="space-between" gap={1}>
                        {milestones.map((milestone) => {
                            const achieved = percentage >= milestone.value;
                            return (
                                <Tooltip key={milestone.value} title={formatCurrency(milestone.amount)}>
                                    <Box
                                        sx={{
                                            width: 40,
                                            height: 40,
                                            borderRadius: '50%',
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            bgcolor: achieved ? 'success.main' : 'action.hover',
                                            color: achieved ? 'success.contrastText' : 'text.secondary',
                                            fontSize: '0.75rem',
                                            fontWeight: 600,
                                            transition: 'all 0.3s ease',
                                        }}
                                    >
                                        {achieved ? '✓' : milestone.label}
                                    </Box>
                                </Tooltip>
                            );
                        })}
                    </Box>
                </Box>

                {/* Timeline */}
                {!isCompleted && (
                    <Box mb={2}>
                        <Box display="flex" justifyContent="space-between" mb={0.5}>
                            <Typography variant="caption" color="text.secondary">
                                Timeline
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                                {daysRemaining > 0 ? `${daysRemaining} days left` : 'Overdue'}
                            </Typography>
                        </Box>
                        <LinearProgress
                            variant="determinate"
                            value={Math.min(timePercentage, 100)}
                            color={timePercentage > percentage ? 'warning' : 'primary'}
                            sx={{ height: 4, borderRadius: 2 }}
                        />
                        <Typography variant="caption" color="text.secondary" display="block" mt={0.5}>
                            Deadline: {formatDate(deadline)}
                        </Typography>
                    </Box>
                )}

                {/* Required Monthly Savings */}
                {!isCompleted && requiredMonthly > 0 && (
                    <Box
                        p={1.5}
                        borderRadius={1}
                        bgcolor="primary.dark"
                        sx={{ opacity: 0.9 }}
                    >
                        <Typography variant="caption" color="primary.contrastText" display="block">
                            Required Monthly Savings
                        </Typography>
                        <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.contrastText' }}>
                            {formatCurrency(requiredMonthly)}
                        </Typography>
                    </Box>
                )}

                {/* Completion Message */}
                {isCompleted && (
                    <Box
                        p={1.5}
                        borderRadius={1}
                        bgcolor="success.dark"
                        display="flex"
                        alignItems="center"
                        gap={1}
                    >
                        <TrophyIcon sx={{ color: 'warning.main' }} />
                        <Typography variant="body2" sx={{ fontWeight: 600, color: 'success.contrastText' }}>
                            🎉 Goal Achieved! Congratulations!
                        </Typography>
                    </Box>
                )}
            </CardContent>

            <CardActions sx={{ justifyContent: 'space-between', p: 2, pt: 0 }}>
                {!isCompleted && (
                    <Box display="flex" gap={1}>
                        <Chip
                            label="Add"
                            icon={<RupeeIcon />}
                            onClick={() => onContribute(goal)}
                            color="primary"
                            clickable
                            sx={{ fontWeight: 600 }}
                        />
                        <Chip
                            label="Withdraw"
                            icon={<RemoveIcon />}
                            onClick={() => onWithdraw(goal)}
                            color="warning"
                            clickable
                            variant="outlined"
                            sx={{ fontWeight: 600 }}
                        />
                    </Box>
                )}
                <Box display="flex" gap={0.5} ml="auto">
                    <Tooltip title="Edit Goal">
                        <IconButton size="small" onClick={() => onEdit(goal)} color="primary">
                            <EditIcon fontSize="small" />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete Goal">
                        <IconButton size="small" onClick={() => onDelete(goal.id)} color="error">
                            <DeleteIcon fontSize="small" />
                        </IconButton>
                    </Tooltip>
                </Box>
            </CardActions>
        </Card>
    );
};

export default GoalCard;
