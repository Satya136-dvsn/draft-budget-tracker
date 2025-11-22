import { useState } from 'react';
import {
    Container,
    Box,
    Typography,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Paper,
    IconButton,
    Chip,
    Fade,
} from '@mui/material';
import {
    Notifications as NotificationsIcon,
    TrendingUp as TrendingUpIcon,
    Warning as WarningIcon,
    Info as InfoIcon,
    CheckCircle as CheckCircleIcon,
    Delete as DeleteIcon,
} from '@mui/icons-material';

const Notifications = () => {
    const [notifications, setNotifications] = useState([
        {
            id: 1,
            type: 'info',
            title: 'Welcome to BudgetWise!',
            message: 'Start tracking your finances today.',
            date: new Date().toISOString(),
            read: false,
        },
    ]);

    const getIcon = (type) => {
        switch (type) {
            case 'success':
                return <CheckCircleIcon color="success" />;
            case 'warning':
                return <WarningIcon color="warning" />;
            case 'error':
                return <WarningIcon color="error" />;
            case 'info':
            default:
                return <InfoIcon color="info" />;
        }
    };

    const handleDelete = (id) => {
        setNotifications(notifications.filter((n) => n.id !== id));
    };

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    return (
        <Container maxWidth="lg" sx={{ pb: 4 }}>
            <Fade in timeout={300}>
                <Box>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                        <Box>
                            <Typography variant="h4" gutterBottom sx={{ fontWeight: 700, mb: 0.5 }}>
                                Notifications
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Stay updated with your financial activities
                            </Typography>
                        </Box>
                        <Chip
                            label={`${notifications.filter((n) => !n.read).length} unread`}
                            color="primary"
                            variant="outlined"
                        />
                    </Box>

                    <Paper>
                        {notifications.length === 0 ? (
                            <Box p={6} textAlign="center">
                                <NotificationsIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
                                <Typography variant="h6" color="text.secondary">
                                    No notifications
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    You're all caught up!
                                </Typography>
                            </Box>
                        ) : (
                            <List>
                                {notifications.map((notification, index) => (
                                    <ListItem
                                        key={notification.id}
                                        divider={index < notifications.length - 1}
                                        sx={{
                                            bgcolor: notification.read ? 'inherit' : 'action.hover',
                                            '&:hover': {
                                                bgcolor: 'action.selected',
                                            },
                                        }}
                                        secondaryAction={
                                            <IconButton
                                                edge="end"
                                                onClick={() => handleDelete(notification.id)}
                                                size="small"
                                            >
                                                <DeleteIcon />
                                            </IconButton>
                                        }
                                    >
                                        <ListItemIcon>{getIcon(notification.type)}</ListItemIcon>
                                        <ListItemText
                                            primary={notification.title}
                                            secondary={
                                                <>
                                                    <Typography component="span" variant="body2" color="text.primary">
                                                        {notification.message}
                                                    </Typography>
                                                    <br />
                                                    <Typography component="span" variant="caption" color="text.secondary">
                                                        {formatDate(notification.date)}
                                                    </Typography>
                                                </>
                                            }
                                        />
                                    </ListItem>
                                ))}
                            </List>
                        )}
                    </Paper>
                </Box>
            </Fade>
        </Container>
    );
};

export default Notifications;
