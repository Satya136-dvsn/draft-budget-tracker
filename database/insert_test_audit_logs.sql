-- Insert test audit logs for testing
-- Run this after you have created an admin user

-- First, get your admin user ID (replace with actual ID from your database)
-- SELECT id FROM users WHERE role = 'ADMIN';

-- Insert sample audit logs (replace adminUserId with your actual admin user ID)
INSERT INTO audit_logs (admin_user_id, action_type, target_user_id, target_resource, details, ip_address, created_at)
VALUES 
(2, 'USER_VIEW', 1, 'User', 'Viewed user details', '127.0.0.1', NOW()),
(2, 'STATS_VIEW', NULL, 'System', 'Viewed system statistics', '127.0.0.1', NOW()),
(2, 'USER_LIST', NULL, 'User', 'Listed all users', '127.0.0.1', NOW());
