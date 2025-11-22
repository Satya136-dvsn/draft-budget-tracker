import React, { useState } from 'react';
import {
    IconButton,
    Menu,
    MenuItem,
    ListItemIcon,
    ListItemText,
    CircularProgress,
    Tooltip
} from '@mui/material';
import {
    FileDownload as DownloadIcon,
    Description as CsvIcon,
    TableChart as ExcelIcon,
    PictureAsPdf as PdfIcon
} from '@mui/icons-material';

/**
 * Reusable export menu component for exporting data in different formats
 * @param {Function} onExport - Callback function (format) => Promise
 * @param {Array} formats - Array of supported formats ['csv', 'excel', 'pdf']
 * @param {Boolean} iconOnly - Show icon button instead of text button
 */
const ExportMenu = ({ onExport, formats = ['excel', 'pdf'], iconOnly = true }) => {
    const [anchorEl, setAnchorEl] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleClick = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleExport = async (format) => {
        setLoading(true);
        handleClose();
        try {
            await onExport(format);
        } catch (error) {
            console.error('Export failed:', error);
        } finally {
            setLoading(false);
        }
    };

    const formatOptions = {
        csv: {
            label: 'CSV File',
            icon: <CsvIcon />,
            extension: '.csv'
        },
        excel: {
            label: 'Excel Spreadsheet',
            icon: <ExcelIcon />,
            extension: '.xlsx'
        },
        pdf: {
            label: 'PDF Document',
            icon: <PdfIcon />,
            extension: '.pdf'
        }
    };

    return (
        <>
            <Tooltip title="Export Data">
                <IconButton
                    color="primary"
                    onClick={handleClick}
                    disabled={loading}
                    sx={{
                        '&:hover': {
                            backgroundColor: 'rgba(99, 102, 241, 0.08)'
                        }
                    }}
                >
                    {loading ? <CircularProgress size={24} /> : <DownloadIcon />}
                </IconButton>
            </Tooltip>

            <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleClose}
                anchorOrigin={{
                    vertical: 'bottom',
                    horizontal: 'right',
                }}
                transformOrigin={{
                    vertical: 'top',
                    horizontal: 'right',
                }}
            >
                {formats.map((format) => (
                    <MenuItem
                        key={format}
                        onClick={() => handleExport(format)}
                        sx={{
                            minWidth: 220,
                            '&:hover': {
                                backgroundColor: 'rgba(99, 102, 241, 0.08)'
                            }
                        }}
                    >
                        <ListItemIcon>
                            {formatOptions[format]?.icon}
                        </ListItemIcon>
                        <ListItemText
                            primary={formatOptions[format]?.label}
                            secondary={formatOptions[format]?.extension}
                        />
                    </MenuItem>
                ))}
            </Menu>
        </>
    );
};

export default ExportMenu;
