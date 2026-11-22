CREATE TABLE IF NOT EXISTS `transactions` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `type` VARCHAR(10) NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `note` VARCHAR(200),
    `transaction_date` DATE NOT NULL
);
