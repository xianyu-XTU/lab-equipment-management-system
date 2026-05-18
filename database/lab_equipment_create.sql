DROP DATABASE IF EXISTS lab_equipment;
CREATE DATABASE lab_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lab_equipment;

CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(50) NOT NULL
) COMMENT='role table';

CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(30),
    email VARCHAR(100),
    role_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES role(id)
) COMMENT='user table';

CREATE TABLE device_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT='device category table';

CREATE TABLE device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT,
    device_no VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    model VARCHAR(100),
    location VARCHAR(100),
    status TINYINT DEFAULT 1 COMMENT '1 available, 2 borrowed, 3 repairing, 4 disabled',
    purchase_date DATE,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES device_category(id)
) COMMENT='device table';

CREATE TABLE borrow_apply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    apply_reason VARCHAR(255),
    expected_return_time DATETIME NOT NULL,
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 0 COMMENT '0 pending, 1 approved, 2 rejected',
    approve_user_id BIGINT,
    approve_time DATETIME,
    approve_remark VARCHAR(255),
    FOREIGN KEY (device_id) REFERENCES device(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (approve_user_id) REFERENCES user(id)
) COMMENT='borrow application table';

CREATE TABLE borrow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    apply_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    borrow_time DATETIME,
    return_time DATETIME,
    status TINYINT DEFAULT 1 COMMENT '1 borrowing, 2 returned, 3 overdue unreturned',
    is_overdue TINYINT DEFAULT 0 COMMENT '0 no, 1 yes',
    remark VARCHAR(255),
    FOREIGN KEY (apply_id) REFERENCES borrow_apply(id),
    FOREIGN KEY (device_id) REFERENCES device(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
) COMMENT='borrow record table';

CREATE TABLE repair_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    fault_desc VARCHAR(500) NOT NULL,
    report_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 0 COMMENT '0 pending, 1 processing, 2 finished',
    handle_user_id BIGINT,
    handle_time DATETIME,
    handle_result VARCHAR(500),
    FOREIGN KEY (device_id) REFERENCES device(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (handle_user_id) REFERENCES user(id)
) COMMENT='repair record table';

CREATE TABLE notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    publish_user_id BIGINT,
    publish_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '1 published, 0 hidden',
    FOREIGN KEY (publish_user_id) REFERENCES user(id)
) COMMENT='notice table';

INSERT INTO role(id, role_code, role_name) VALUES
(1, 'ADMIN', 'Administrator'),
(2, 'LAB_ADMIN', 'Lab Administrator'),
(3, 'STUDENT', 'Student');

INSERT INTO user(username, password, real_name, phone, email, role_id, status) VALUES
('admin', '$2a$10$ttpjN4M.8KJ3LQWOcZ4P2OWVXfs.C4JkffMBDNuia7rFVNzAbDDIy', 'Admin', '10000000000', 'admin@example.com', 1, 1),
('lab', '$2a$10$ttpjN4M.8KJ3LQWOcZ4P2OWVXfs.C4JkffMBDNuia7rFVNzAbDDIy', 'Lab Admin', '10000000001', 'lab@example.com', 2, 1),
('student', '$2a$10$ttpjN4M.8KJ3LQWOcZ4P2OWVXfs.C4JkffMBDNuia7rFVNzAbDDIy', 'Student', '10000000002', 'student@example.com', 3, 1);

INSERT INTO device_category(id, name, description) VALUES
(1, 'Computer', 'Computer equipment'),
(2, 'Instrument', 'Experiment instruments'),
(3, 'Tool', 'Common tools');

INSERT INTO device(category_id, device_no, name, model, location, status, purchase_date, remark) VALUES
(1, 'PC-001', 'Desktop Computer', 'Dell OptiPlex', 'Lab A101', 1, '2024-09-01', 'Good'),
(1, 'PC-002', 'Laptop', 'ThinkPad E14', 'Lab A102', 1, '2024-10-01', 'Good'),
(2, 'OSC-001', 'Oscilloscope', 'DS1054Z', 'Lab B201', 1, '2023-05-10', 'Good'),
(2, 'MUL-001', 'Multimeter', 'Fluke 15B+', 'Lab B202', 1, '2023-06-12', 'Good'),
(3, 'TOOL-001', 'Screwdriver Set', 'Standard', 'Storage C301', 1, '2022-03-20', 'Good');

INSERT INTO notice(title, content, publish_user_id, status) VALUES
('Welcome', 'Welcome to use the lab equipment management system.', 1, 1),
('Borrow Rules', 'Please return borrowed equipment before the expected return time.', 1, 1);
