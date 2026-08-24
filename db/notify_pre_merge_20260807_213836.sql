-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: etour_notify
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `etour_notify`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `etour_notify` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `etour_notify`;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `attempts` int NOT NULL,
  `body` text NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `idempotency_key` varchar(200) DEFAULT NULL,
  `last_error` varchar(1000) DEFAULT NULL,
  `max_attempts` int NOT NULL,
  `next_attempt_at` datetime(6) DEFAULT NULL,
  `recipient` varchar(320) NOT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` enum('FAILED','PENDING','SENT') NOT NULL,
  `subject` varchar(500) NOT NULL,
  `template_code` varchar(64) NOT NULL,
  `transport` varchar(20) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `variables_json` text,
  PRIMARY KEY (`notification_id`),
  UNIQUE KEY `uk_notification_idempotency` (`idempotency_key`),
  KEY `ix_notification_due` (`status`,`next_attempt_at`),
  KEY `ix_notification_created` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_attachment`
--

DROP TABLE IF EXISTS `notification_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_attachment` (
  `attachment_id` bigint NOT NULL AUTO_INCREMENT,
  `content` mediumblob NOT NULL,
  `content_type` varchar(120) NOT NULL,
  `filename` varchar(255) NOT NULL,
  `size_bytes` bigint NOT NULL,
  `notification_id` bigint NOT NULL,
  PRIMARY KEY (`attachment_id`),
  KEY `FK23ehk4v86waa6oi9fg2l9t16r` (`notification_id`),
  CONSTRAINT `FK23ehk4v86waa6oi9fg2l9t16r` FOREIGN KEY (`notification_id`) REFERENCES `notification` (`notification_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_attachment`
--

LOCK TABLES `notification_attachment` WRITE;
/*!40000 ALTER TABLE `notification_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_template`
--

DROP TABLE IF EXISTS `notification_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_template` (
  `template_id` bigint NOT NULL AUTO_INCREMENT,
  `body` text NOT NULL,
  `code` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `subject` varchar(500) NOT NULL,
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_template_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_template`
--

LOCK TABLES `notification_template` WRITE;
/*!40000 ALTER TABLE `notification_template` DISABLE KEYS */;
INSERT INTO `notification_template` VALUES (1,'Hi {{customerName}},\n\nYour booking for {{tourTitle}} is confirmed.\n\nBooking reference: {{bookingRef}}\nDeparture: {{departureDate}}\nPassengers: {{passengers}}\nTotal paid: {{totalAmount}}\n\nYou can download your receipt from your eTour dashboard.\n\nSafe travels,\nThe eTour team\n','BOOKING_CONFIRMED','Sent when a booking is confirmed and paid','Your eTour booking {{bookingRef}} is confirmed'),(2,'Hi {{customerName}},\n\nYour booking {{bookingRef}} for {{tourTitle}} has been cancelled.\n\nAny refund due will be processed to your original payment method.\n\nThe eTour team\n','BOOKING_CANCELLED','Sent when a booking is cancelled','Your eTour booking {{bookingRef}} has been cancelled'),(3,'Hi {{customerName}},\n\nA quick reminder that {{tourTitle}} departs on {{departureDate}}.\n\nBooking reference: {{bookingRef}}\n\nPlease re-check your documents and arrive at the meeting point on time.\n\nThe eTour team\n','DEPARTURE_REMINDER','Reminder ahead of the departure date','{{tourTitle}} departs on {{departureDate}}'),(4,'Hi {{customerName}},\n\nThe payment for booking {{bookingRef}} was declined and the booking\nis not yet confirmed.\n\nYou can retry the payment from your eTour dashboard.\n\nThe eTour team\n','PAYMENT_FAILED','Sent when a card payment is declined','Payment for booking {{bookingRef}} was declined'),(5,'{{message}}\n\n- sent by the eTour notification service\n','TEST_MESSAGE','Free-text message, used to try the queue from the admin screen','{{subject}}'),(6,'Hi {{customerName}},\n\nThank you for booking {{tourTitle}} with us. Your payment was successful and your booking is confirmed (order {{orderNumber}}).\n\nYour receipt is attached as a PDF.\n\n- TourIndia Travels\n','BOOKING_RECEIPT','Payment receipt, sent with the invoice PDF attached. Enqueued by both backends after a successful payment.','Your eTour booking receipt - {{invoiceNumber}}');
/*!40000 ALTER TABLE `notification_template` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-07 16:08:37
