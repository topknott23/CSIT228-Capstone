package doboard.common.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class InitDB {

    private static final String BASE_URL = "jdbc:mysql://localhost:3306/?allowMultiQueries=true";

    public static boolean setupDatabase() {
        try (Connection conn = DriverManager.getConnection(BASE_URL, SQLConnector.USER, SQLConnector.PASS);
             Statement stmt = conn.createStatement()) {

            System.out.println("InitDB: Creating database dorm_app...");
            stmt.executeUpdate("CREATE DATABASE dorm_app CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;");

            System.out.println("InitDB: Selecting database dorm_app...");
            stmt.execute("USE dorm_app;");

            System.out.println("InitDB: Building tables and constraints...");
            stmt.executeUpdate(getSchemaSql());

            System.out.println("InitDB: Database initialized successfully!");
            return true;

        } catch (SQLException e) {
            System.err.println("InitDB CRITICAL ERROR: Failed to initialize database: " + e.getMessage());
            return false;
        }
    }
    private static String getSchemaSql() {
                return """
        CREATE TABLE `bills` (
          `bill_id` int(11) NOT NULL,
          `dorm_id` int(11) NOT NULL,
          `title` varchar(150) NOT NULL,
          `total_amount` decimal(10,2) NOT NULL,
          `due_date` date DEFAULT NULL,
          `created_at` timestamp NOT NULL DEFAULT current_timestamp()
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        CREATE TABLE `bill_splits` (
          `split_id` int(11) NOT NULL,
          `bill_id` int(11) NOT NULL,
          `user_id` int(11) NOT NULL,
          `amount_owed` decimal(10,2) NOT NULL,
          `is_paid` tinyint(1) DEFAULT 0,
          `paid_date` datetime DEFAULT NULL
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        CREATE TABLE `chores` (
          `chore_id` int(11) NOT NULL,
          `dorm_id` int(11) NOT NULL,
          `title` varchar(150) NOT NULL,
          `description` text DEFAULT NULL,
          `frequency` enum('once','daily','weekly','monthly') DEFAULT 'once',
          `due_date` datetime DEFAULT NULL,
          `status` enum('PENDING','COMPLETE') DEFAULT 'pending'
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        CREATE TABLE `chore_assignments` (
          `chore_id` int(11) NOT NULL,
          `user_id` int(11) NOT NULL
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        CREATE TABLE `dorms` (
          `dorm_id` int(11) NOT NULL,
          `dorm_name` varchar(100) NOT NULL,
          `join_code` varchar(10) NOT NULL,
          `created_at` timestamp NOT NULL DEFAULT current_timestamp()
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        CREATE TABLE `dorm_members` (
          `dorm_id` int(11) NOT NULL,
          `user_id` int(11) NOT NULL,
          `role` enum('admin','member') DEFAULT 'member'
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        CREATE TABLE `users` (
          `user_id` int(11) NOT NULL,
          `username` varchar(50) NOT NULL,
          `email` varchar(100) NOT NULL,
          `password` varchar(255) NOT NULL,
          `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
          `full_name` varchar(255) NOT NULL,
          `last_seen` int(11) NOT NULL DEFAULT 0
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        CREATE TABLE `notifications` (
          `notification_id` int(11) NOT NULL,
          `sender_id` int(11) NOT NULL,
          `receiver_id` int(11) NOT NULL DEFAULT 0,
          `dorm_id` int(11) NOT NULL,
          `message` varchar(255) NOT NULL,
          `nudge_count` int(11) DEFAULT 1,
          `sent_at` timestamp NOT NULL DEFAULT current_timestamp()
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        CREATE TABLE `chat_messages` (
          `message_id` int(11) NOT NULL,
          `dorm_id` int(11) NOT NULL,
          `sender_id` int(11) NOT NULL,
          `message_text` text NOT NULL,
          `sent_at` timestamp NOT NULL DEFAULT current_timestamp()
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        CREATE TABLE `maintenance_requests` (
          `request_id` int(11) NOT NULL,
          `dorm_id` int(11) NOT NULL,
          `user_id` int(11) NOT NULL,
          `issue_description` text NOT NULL,
          `status` enum('PENDING','IN_PROGRESS','RESOLVED') DEFAULT 'PENDING',
          `created_at` timestamp NOT NULL DEFAULT current_timestamp()
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
        
        ALTER TABLE `bills` ADD PRIMARY KEY (`bill_id`), ADD KEY `dorm_id` (`dorm_id`);
        ALTER TABLE `bill_splits` ADD PRIMARY KEY (`split_id`), ADD KEY `bill_id` (`bill_id`), ADD KEY `user_id` (`user_id`);
        ALTER TABLE `chores` ADD PRIMARY KEY (`chore_id`), ADD KEY `dorm_id` (`dorm_id`);
        ALTER TABLE `chore_assignments` ADD PRIMARY KEY (`chore_id`,`user_id`), ADD KEY `user_id` (`user_id`);
        ALTER TABLE `dorms` ADD PRIMARY KEY (`dorm_id`), ADD UNIQUE KEY `join_code` (`join_code`);
        ALTER TABLE `dorm_members` ADD PRIMARY KEY (`dorm_id`,`user_id`), ADD KEY `user_id` (`user_id`);
        ALTER TABLE `users` ADD PRIMARY KEY (`user_id`), ADD UNIQUE KEY `email` (`email`);
        ALTER TABLE `notifications` ADD PRIMARY KEY (`notification_id`), ADD KEY `sender_id` (`sender_id`), ADD KEY `dorm_id` (`dorm_id`);
        ALTER TABLE `chat_messages` ADD PRIMARY KEY (`message_id`), ADD KEY `dorm_id` (`dorm_id`), ADD KEY `sender_id` (`sender_id`);
        ALTER TABLE `maintenance_requests` ADD PRIMARY KEY (`request_id`), ADD KEY `dorm_id` (`dorm_id`), ADD KEY `user_id` (`user_id`);
        
        ALTER TABLE `bills` MODIFY `bill_id` int(11) NOT NULL AUTO_INCREMENT;
        ALTER TABLE `bill_splits` MODIFY `split_id` int(11) NOT NULL AUTO_INCREMENT;
        ALTER TABLE `chores` MODIFY `chore_id` int(11) NOT NULL AUTO_INCREMENT;
        ALTER TABLE `dorms` MODIFY `dorm_id` int(11) NOT NULL AUTO_INCREMENT;
        ALTER TABLE `users` MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT;
        ALTER TABLE `notifications` MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT;
        ALTER TABLE `chat_messages` MODIFY `message_id` int(11) NOT NULL AUTO_INCREMENT;
        ALTER TABLE `maintenance_requests` MODIFY `request_id` int(11) NOT NULL AUTO_INCREMENT;
        
        ALTER TABLE `bills` ADD CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`dorm_id`) REFERENCES `dorms` (`dorm_id`) ON DELETE CASCADE;
        ALTER TABLE `bill_splits` ADD CONSTRAINT `bill_splits_ibfk_1` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`bill_id`) ON DELETE CASCADE, ADD CONSTRAINT `bill_splits_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
        ALTER TABLE `chores` ADD CONSTRAINT `chores_ibfk_1` FOREIGN KEY (`dorm_id`) REFERENCES `dorms` (`dorm_id`) ON DELETE CASCADE;
        ALTER TABLE `chore_assignments` ADD CONSTRAINT `chore_assignments_ibfk_1` FOREIGN KEY (`chore_id`) REFERENCES `chores` (`chore_id`) ON DELETE CASCADE, ADD CONSTRAINT `chore_assignments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
        ALTER TABLE `dorm_members` ADD CONSTRAINT `dorm_members_ibfk_1` FOREIGN KEY (`dorm_id`) REFERENCES `dorms` (`dorm_id`) ON DELETE CASCADE, ADD CONSTRAINT `dorm_members_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
        ALTER TABLE `notifications` ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE, ADD CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`dorm_id`) REFERENCES `dorms` (`dorm_id`) ON DELETE CASCADE;
        ALTER TABLE `chat_messages` ADD CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`dorm_id`) REFERENCES `dorms` (`dorm_id`) ON DELETE CASCADE, ADD CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
        ALTER TABLE `maintenance_requests` ADD CONSTRAINT `maintenance_requests_ibfk_1` FOREIGN KEY (`dorm_id`) REFERENCES `dorms` (`dorm_id`) ON DELETE CASCADE, ADD CONSTRAINT `maintenance_requests_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
        """;
    }
}