import React from 'react';
import {
    Box,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography,
    LinearProgress,
    Chip,
    Skeleton
} from '@mui/material';
import { TrendingUp, TrendingDown, Remove } from '@mui/icons-material';

const PredictionsCard = ({ predictions, loading }) => {
    if (loading) {
        return (
            <Box sx={{ width: '100%' }}>
                <Skeleton height={50} />
                <Skeleton height={50} />
                <Skeleton height={50} />
            </Box>
        );
    }

    if (!predictions || predictions.length === 0) {
        return (
            <Box p={3} textAlign="center">
                <Typography color="text.secondary">
                    Not enough data to generate predictions yet.
                </Typography>
            </Box>
        );
    }

    const getTrendIcon = (trend) => {
        switch (trend) {
            case 'INCREASING': return <TrendingUp color="error" />;
            case 'DECREASING': return <TrendingDown color="success" />;
            default: return <Remove color="action" />;
        }
    };

    const getTrendLabel = (trend) => {
        switch (trend) {
            case 'INCREASING': return 'Increasing';
            case 'DECREASING': return 'Decreasing';
            default: return 'Stable';
        }
    };

    const getTrendColor = (trend) => {
        switch (trend) {
            case 'INCREASING': return 'error';
            case 'DECREASING': return 'success';
            default: return 'default';
        }
    };

    return (
        <TableContainer>
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>Category</TableCell>
                        <TableCell align="right">Predicted</TableCell>
                        <TableCell align="right">Avg (6M)</TableCell>
                        <TableCell align="center">Trend</TableCell>
                        <TableCell align="center">Confidence</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {predictions.map((pred) => (
                        <TableRow key={pred.categoryId}>
                            <TableCell component="th" scope="row">
                                {pred.categoryName}
                            </TableCell>
                            <TableCell align="right">
                                ₹{pred.predictedAmount.toFixed(2)}
                            </TableCell>
                            <TableCell align="right">
                                ₹{pred.historicalAverage.toFixed(2)}
                            </TableCell>
                            <TableCell align="center">
                                <Chip
                                    icon={getTrendIcon(pred.trend)}
                                    label={getTrendLabel(pred.trend)}
                                    size="small"
                                    color={getTrendColor(pred.trend)}
                                    variant="outlined"
                                />
                            </TableCell>
                            <TableCell align="center" sx={{ width: '20%' }}>
                                <Box display="flex" alignItems="center">
                                    <Box width="100%" mr={1}>
                                        <LinearProgress
                                            variant="determinate"
                                            value={pred.confidenceScore * 100}
                                            color={pred.confidenceScore > 0.7 ? "success" : "warning"}
                                        />
                                    </Box>
                                    <Box minWidth={35}>
                                        <Typography variant="body2" color="text.secondary">
                                            {Math.round(pred.confidenceScore * 100)}%
                                        </Typography>
                                    </Box>
                                </Box>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </TableContainer>
    );
};

export default PredictionsCard;
