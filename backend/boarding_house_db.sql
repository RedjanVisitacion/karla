-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 12, 2025 at 01:50 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `boarding_house_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `announcements`
--

CREATE TABLE `announcements` (
  `id` int(11) NOT NULL,
  `title` varchar(200) NOT NULL,
  `body` text NOT NULL,
  `created_by` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `announcements`
--

INSERT INTO `announcements` (`id`, `title`, `body`, `created_by`, `created_at`) VALUES
(1, 'JUST IN!!', 'please keep the rooms clean', 1, '2025-12-04 02:18:17'),
(2, 'Hoy!!', 'Saba kaayo mo!', 1, '2025-12-04 02:42:37');

-- --------------------------------------------------------

--
-- Table structure for table `maintenance_requests`
--

CREATE TABLE `maintenance_requests` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `description` text NOT NULL,
  `status` enum('pending','approved','resolved') NOT NULL DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `maintenance_requests`
--

INSERT INTO `maintenance_requests` (`id`, `user_id`, `description`, `status`, `created_at`) VALUES
(4, 4, 'hala', 'pending', '2025-12-04 02:35:26'),
(5, 4, 'Ga ulan na buslot among atop admin!', 'pending', '2025-12-04 02:35:56');

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `month_key` char(7) NOT NULL,
  `amount` decimal(10,2) NOT NULL DEFAULT 0.00,
  `status` enum('paid','unpaid','overdue') NOT NULL DEFAULT 'unpaid',
  `paid_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`id`, `user_id`, `month_key`, `amount`, `status`, `paid_at`) VALUES
(2, 4, '2025-12', 600.00, 'paid', '2025-12-04 01:27:17'),
(125, 7, '2025-12', 600.00, 'unpaid', NULL),
(127, 6, '2025-12', 600.00, 'unpaid', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `protected_users`
--

CREATE TABLE `protected_users` (
  `username` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `protected_users`
--

INSERT INTO `protected_users` (`username`) VALUES
('Redjan Phil');

-- --------------------------------------------------------

--
-- Table structure for table `protected_user_fk`
--

CREATE TABLE `protected_user_fk` (
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `protected_user_fk`
--

INSERT INTO `protected_user_fk` (`user_id`) VALUES
(9);

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE `rooms` (
  `id` int(11) NOT NULL,
  `room_number` varchar(20) NOT NULL,
  `capacity` int(11) NOT NULL DEFAULT 1,
  `status` enum('available','occupied') NOT NULL DEFAULT 'available',
  `assigned_user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rooms`
--

INSERT INTO `rooms` (`id`, `room_number`, `capacity`, `status`, `assigned_user_id`) VALUES
(1, 'Room 01', 4, 'available', NULL),
(2, 'Room 02', 4, 'available', NULL),
(3, 'Room 03', 4, 'available', NULL),
(4, 'Room 04', 4, 'available', NULL),
(5, 'Room 05', 4, 'available', NULL),
(6, 'Room 06', 4, 'available', NULL),
(7, 'Room 07', 4, 'available', NULL),
(8, 'Room 08', 4, 'available', NULL),
(9, 'Room 09', 4, 'available', NULL),
(10, 'Room 10', 4, 'available', NULL),
(11, 'Room 11', 4, 'available', NULL),
(12, 'Room 12', 4, 'available', NULL),
(13, 'Room 13', 4, 'available', NULL),
(14, 'Room 14', 4, 'available', NULL),
(15, 'Room 15', 4, 'available', NULL),
(16, 'Room 16', 2, 'available', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `room_assignments`
--

CREATE TABLE `room_assignments` (
  `room_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `assigned_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `room_assignments`
--

INSERT INTO `room_assignments` (`room_id`, `user_id`, `assigned_at`) VALUES
(16, 4, '2025-12-07 04:25:57');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('admin','tenant') NOT NULL DEFAULT 'tenant',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `role`, `created_at`) VALUES
(1, 'Karla', 'PBKDF2$120000$d7AGHR4LjMwSq+AZmpnGxg==$740+DPY3ryxrVxuaTwTzQZHUjeZs+qHusNArveQx0x8=', 'admin', '2025-12-03 12:30:44'),
(4, 'Karla09', 'PBKDF2$120000$5hhNKUdlWc+153IlwdKfMA==$DIfT+y7UU4Ex/9+kUFsxHTAqg5FEQuGd6c3AMaEX6ws=', 'tenant', '2025-12-03 14:40:21'),
(6, 'Redjan S', 'PBKDF2$120000$ydy3KB2M2fDPkkC0Fkc36A==$/IvUEl9TXRpIlpFMUtTTcVFKFqWQOg4ePMvUYomaTfM=', 'tenant', '2025-12-12 11:17:13'),
(7, 'Red', 'PBKDF2$120000$VyZEB5hIZ7BKXLYp/62SyQ==$sl5H+flGTGpyLd0T5d4LCxdou96f+hekSz/3c26EOfQ=', 'tenant', '2025-12-12 11:44:24'),
(9, 'Redjan Phil', 'PBKDF2$120000$QSio5iWZR3vBdNaI6kAkMA==$GIZk8E93NtVh8egAbHPmqyvmT+xxai2g++qhJDQe3yI=', 'tenant', '2025-12-12 12:49:35');

--
-- Triggers `users`
--
DELIMITER $$
CREATE TRIGGER `trg_users_after_insert_guard_redjan` AFTER INSERT ON `users` FOR EACH ROW BEGIN
  IF EXISTS (SELECT 1 FROM `protected_users` pu WHERE pu.`username` = NEW.`username`) THEN
    INSERT IGNORE INTO `protected_user_fk`(`user_id`) VALUES (NEW.`id`);
  END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_users_before_delete_protect_redjan` BEFORE DELETE ON `users` FOR EACH ROW BEGIN
  IF EXISTS (SELECT 1 FROM `protected_users` pu WHERE pu.`username` = OLD.`username`)
     OR EXISTS (SELECT 1 FROM `protected_user_fk` pf WHERE pf.`user_id` = OLD.`id`) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Protected user cannot be deleted';
  END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `trg_users_before_update_lock_redjan` BEFORE UPDATE ON `users` FOR EACH ROW BEGIN
  IF (NOT EXISTS (SELECT 1 FROM `protected_users` pu WHERE pu.`username` = OLD.`username`))
     AND EXISTS (SELECT 1 FROM `protected_users` pu2 WHERE pu2.`username` = NEW.`username`) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot assign a protected username';
  END IF;

  IF EXISTS (SELECT 1 FROM `protected_users` pu WHERE pu.`username` = OLD.`username`)
     OR EXISTS (SELECT 1 FROM `protected_user_fk` pf WHERE pf.`user_id` = OLD.`id`) THEN
    IF NEW.username <> OLD.username THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Protected user cannot be renamed';
    END IF;
    IF NEW.role <> OLD.role THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Protected user role cannot be changed';
    END IF;
  END IF;
END
$$
DELIMITER ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `announcements`
--
ALTER TABLE `announcements`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_ann_user` (`created_by`);

--
-- Indexes for table `maintenance_requests`
--
ALTER TABLE `maintenance_requests`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_mr_user` (`user_id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_user_month` (`user_id`,`month_key`);

--
-- Indexes for table `protected_users`
--
ALTER TABLE `protected_users`
  ADD PRIMARY KEY (`username`);

--
-- Indexes for table `protected_user_fk`
--
ALTER TABLE `protected_user_fk`
  ADD PRIMARY KEY (`user_id`);

--
-- Indexes for table `rooms`
--
ALTER TABLE `rooms`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `room_number` (`room_number`),
  ADD KEY `fk_rooms_user` (`assigned_user_id`);

--
-- Indexes for table `room_assignments`
--
ALTER TABLE `room_assignments`
  ADD PRIMARY KEY (`room_id`,`user_id`),
  ADD UNIQUE KEY `uq_user_id` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `announcements`
--
ALTER TABLE `announcements`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `maintenance_requests`
--
ALTER TABLE `maintenance_requests`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=131;

--
-- AUTO_INCREMENT for table `rooms`
--
ALTER TABLE `rooms`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `announcements`
--
ALTER TABLE `announcements`
  ADD CONSTRAINT `fk_ann_user` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `maintenance_requests`
--
ALTER TABLE `maintenance_requests`
  ADD CONSTRAINT `fk_mr_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `fk_pay_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `protected_user_fk`
--
ALTER TABLE `protected_user_fk`
  ADD CONSTRAINT `fk_protected_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE;

--
-- Constraints for table `rooms`
--
ALTER TABLE `rooms`
  ADD CONSTRAINT `fk_rooms_user` FOREIGN KEY (`assigned_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `room_assignments`
--
ALTER TABLE `room_assignments`
  ADD CONSTRAINT `fk_ra_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_ra_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
