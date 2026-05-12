-- Migration script to add new notification types to the database constraint
-- Run this SQL script on your PostgreSQL database

-- Drop the existing check constraint
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;

-- Add the updated check constraint with new notification types
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check 
CHECK (type IN ('BUS_REQUEST', 'CONFIRMATION', 'ETA_UPDATE', 'SYSTEM_ALERT', 'LOCATION_UPDATE', 'PAYMENT_CONFIRMED', 'TRIP_RATED'));
