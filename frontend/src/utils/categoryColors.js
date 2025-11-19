// Centralized category color mapping for consistent UI
// These colors are vibrant, distinct, and accessible

const categoryColorPalette = [
    '#2196F3', // Blue
    '#4CAF50', // Green
    '#FF9800', // Orange
    '#F44336', // Red
    '#9C27B0', // Purple
    '#00BCD4', // Cyan
    '#FFEB3B', // Yellow
    '#795548', // Brown
    '#E91E63', // Pink
    '#3F51B5', // Indigo
    '#009688', // Teal
    '#FF5722', // Deep Orange
    '#8BC34A', // Light Green
    '#FFC107', // Amber
    '#673AB7', // Deep Purple
];

// Map to store category ID -> color assignments
const categoryColorMap = new Map();

/**
 * Get a consistent color for a category
 * @param {number|string} categoryId - The category ID
 * @param {number} index - Optional index for fallback
 * @returns {string} Hex color code
 */
export const getCategoryColor = (categoryId, index = 0) => {
    if (!categoryId) {
        return categoryColorPalette[index % categoryColorPalette.length];
    }

    // Check if we already have a color assigned
    if (categoryColorMap.has(categoryId)) {
        return categoryColorMap.get(categoryId);
    }

    // Assign a new color based on the number of existing assignments
    const colorIndex = categoryColorMap.size % categoryColorPalette.length;
    const color = categoryColorPalette[colorIndex];
    categoryColorMap.set(categoryId, color);

    return color;
};

/**
 * Get all category colors as an array (for charts)
 * @returns {Array<string>} Array of color codes
 */
export const getCategoryColorPalette = () => {
    return [...categoryColorPalette];
};

/**
 * Get a lighter version of a category color (for backgrounds)
 * @param {string} hexColor - Hex color code
 * @param {number} opacity - Opacity value (0-1)
 * @returns {string} RGBA color string
 */
export const getCategoryColorWithOpacity = (hexColor, opacity = 0.1) => {
    const r = parseInt(hexColor.slice(1, 3), 16);
    const g = parseInt(hexColor.slice(3, 5), 16);
    const b = parseInt(hexColor.slice(5, 7), 16);
    return `rgba(${r}, ${g}, ${b}, ${opacity})`;
};

/**
 * Reset the category color map (useful for testing)
 */
export const resetCategoryColors = () => {
    categoryColorMap.clear();
};

export default {
    getCategoryColor,
    getCategoryColorPalette,
    getCategoryColorWithOpacity,
    resetCategoryColors,
};
