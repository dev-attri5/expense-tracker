-- Users table for authentication
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions table linked to a user
CREATE TABLE IF NOT EXISTS `transactions` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT,
    `type` VARCHAR(10) NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `note` VARCHAR(200),
    `transaction_date` DATE NOT NULL
);
