-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 04, 2025 at 04:04 AM
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
(2, 2, 'Can I go home?', 'resolved', '2025-12-04 02:03:28'),
(3, 2, 'Our bed has been slain', 'pending', '2025-12-04 02:08:51'),
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
(1, 2, '2025-12', 600.00, 'unpaid', NULL),
(2, 4, '2025-12', 600.00, 'paid', '2025-12-04 01:27:17'),
(58, 5, '2025-12', 600.00, 'unpaid', NULL);

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
(16, 'Room 16', 2, 'occupied', NULL);

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
(16, 2, '2025-12-04 00:34:03'),
(16, 4, '2025-12-03 23:53:49');

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
(1, 'Karla', 'bhms_00;', 'admin', '2025-12-03 12:30:44'),
(2, 'Redjan Phil', '123456', 'tenant', '2025-12-03 12:31:19'),
(4, 'Karla09', 'karlita123', 'tenant', '2025-12-03 14:40:21'),
(5, 'RpsvCodes', '123456', 'tenant', '2025-12-04 02:52:44');

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=61;

--
-- AUTO_INCREMENT for table `rooms`
--
ALTER TABLE `rooms`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

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
