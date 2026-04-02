-- ============================================================
-- Secure Password Manager - Database Schema
-- MySQL Database Setup Script
-- ============================================================

-- Create the database
CREATE DATABASE IF NOT EXISTS password_manager_db;
USE password_manager_db;

-- ============================================================
-- Table: users
-- Stores registered user accounts with hashed passwords
-- failed_attempts tracks consecutive bad logins for lockout
-- ============================================================
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,          -- SHA-256 produces 64 hex chars
    failed_attempts INT DEFAULT 0,
    account_locked BOOLEAN DEFAULT FALSE,
    last_login DATETIME DEFAULT NULL,             -- Track last successful login
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table: vault
-- Stores encrypted credentials for each user
-- encrypted_password uses AES encryption (never plain text)
-- ============================================================
CREATE TABLE vault (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    website VARCHAR(255) NOT NULL,
    site_username VARCHAR(255) NOT NULL,
    encrypted_password TEXT NOT NULL,              -- AES-encrypted password
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table: logs
-- Audit trail for all security-relevant user actions
-- Includes IP address for forensic analysis
-- ============================================================
CREATE TABLE logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) DEFAULT NULL,          -- IPv4 or IPv6 address
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Indexes for performance optimization
-- ============================================================
CREATE INDEX idx_vault_user_id ON vault(user_id);
CREATE INDEX idx_logs_user_id ON logs(user_id);
CREATE INDEX idx_logs_timestamp ON logs(timestamp);
CREATE INDEX idx_users_username ON users(username);

-- ============================================================
-- Sample Test Data
-- Password for all test users: "Test@1234"
-- SHA-256 hash of "Test@1234" = the value below
-- ============================================================
INSERT INTO users (username, email, password_hash, failed_attempts, account_locked)
VALUES 
    ('john_doe', 'john@example.com', 'b16d668a88f76a0ceba4739b92a4cac0cf0b4e4896a8a7fa66f3810a0ce90f95', 0, FALSE),
    ('jane_smith', 'jane@example.com', 'b16d668a88f76a0ceba4739b92a4cac0cf0b4e4896a8a7fa66f3810a0ce90f95', 0, FALSE),
    ('locked_user', 'locked@example.com', 'b16d668a88f76a0ceba4739b92a4cac0cf0b4e4896a8a7fa66f3810a0ce90f95', 5, TRUE);
