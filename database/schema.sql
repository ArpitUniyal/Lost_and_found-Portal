-- Lost and Found Portal Database Schema for MySQL

-- Students table
CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    password_hash VARCHAR(255) NOT NULL,
    password_reset_token VARCHAR(255),
    password_reset_expiry DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Lost items table
CREATE TABLE IF NOT EXISTS lost_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(200) NOT NULL,
    date_lost DATE NOT NULL,
    time_lost TIME,
    category VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active', -- active, claimed, expired
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Found items table
CREATE TABLE IF NOT EXISTS found_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    finder_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(200) NOT NULL,
    date_found DATE NOT NULL,
    time_found TIME,
    category VARCHAR(50),
    status VARCHAR(20) DEFAULT 'active', -- active, claimed, expired
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (finder_id) REFERENCES students(id) ON DELETE CASCADE
);


-- Categories table for better organization
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default categories
INSERT IGNORE INTO categories (name, description) VALUES
('Electronics', 'Phones, laptops, tablets, chargers, etc.'),
('Clothing', 'Jackets, bags, shoes, accessories, etc.'),
('Books', 'Textbooks, notebooks, stationery, etc.'),
('Personal Items', 'Keys, wallets, jewelry, etc.'),
('Sports Equipment', 'Sports gear, equipment, etc.'),
('Other', 'Miscellaneous items');

-- Indexes for better performance
CREATE INDEX idx_lost_items_student_id ON lost_items(student_id);
CREATE INDEX idx_lost_items_status ON lost_items(status);
CREATE INDEX idx_lost_items_date ON lost_items(date_lost);
CREATE INDEX idx_found_items_finder_id ON found_items(finder_id);
CREATE INDEX idx_found_items_status ON found_items(status);
CREATE INDEX idx_found_items_date ON found_items(date_found);


CREATE TABLE `claims` (
  `id` int NOT NULL AUTO_INCREMENT,
  `lost_item_id` int DEFAULT NULL,
  `found_item_id` int NOT NULL,
  `claimer_id` int NOT NULL,
  `status` varchar(20) DEFAULT 'requested',
  `contact_date` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `lost_item_id` (`lost_item_id`),
  KEY `idx_claims_found_item_id` (`found_item_id`),
  KEY `idx_claims_claimer_id` (`claimer_id`),
  KEY `idx_claims_status` (`status`),
  CONSTRAINT `claims_ibfk_1`
    FOREIGN KEY (`lost_item_id`)
    REFERENCES `lost_items` (`id`)
    ON DELETE SET NULL,
  CONSTRAINT `claims_ibfk_2`
    FOREIGN KEY (`found_item_id`)
    REFERENCES `found_items` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `claims_ibfk_3`
    FOREIGN KEY (`claimer_id`)
    REFERENCES `students` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;
