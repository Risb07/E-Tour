-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: etour
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
-- Current Database: `etour`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `etour` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `etour`;

--
-- Table structure for table `ad_banner`
--

DROP TABLE IF EXISTS `ad_banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ad_banner` (
  `ad_id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(500) DEFAULT NULL,
  `link_url` varchar(500) DEFAULT NULL,
  `position` varchar(20) NOT NULL,
  `status` bit(1) NOT NULL,
  `title` varchar(200) NOT NULL,
  PRIMARY KEY (`ad_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ad_banner`
--

LOCK TABLES `ad_banner` WRITE;
/*!40000 ALTER TABLE `ad_banner` DISABLE KEYS */;
INSERT INTO `ad_banner` VALUES (1,'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=600&q=80','/tours','LEFT',_binary '','Summer escapes — save up to 20%'),(2,'https://images.unsplash.com/photo-1533421396259-0d2e83f6f6f1?auto=format&fit=crop&w=600&q=80','/tours','RIGHT',_binary '\0','Festival journeys are back');
/*!40000 ALTER TABLE `ad_banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `add_on`
--

DROP TABLE IF EXISTS `add_on`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `add_on` (
  `add_on_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `name` varchar(150) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`add_on_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `add_on`
--

LOCK TABLES `add_on` WRITE;
/*!40000 ALTER TABLE `add_on` DISABLE KEYS */;
INSERT INTO `add_on` VALUES (1,_binary '','One-way or return chauffeur pick-up and drop at the airport.','Private airport transfers',3500.00),(2,_binary '','Extend your stay with a pre or post tour night at the same hotel.','Extra hotel night',4500.00),(3,_binary '','Comprehensive medical and trip-cancellation cover for the full duration.','Travel insurance',1999.00),(4,_binary '','Move up to a premium or lake-view room category for the whole tour.','Premium room upgrade',7500.00),(5,_binary '','A professional local photographer for a full day of your trip.','Personal photographer',5000.00),(6,_binary '','End-to-end visa filing, documentation and follow-up support.','Visa assistance',2500.00),(7,_binary '','A dedicated English-speaking guide for one full day.','Private local guide',3000.00),(8,_binary '','A special candlelight dinner for anniversaries and birthdays.','Celebration dinner',2500.00);
/*!40000 ALTER TABLE `add_on` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking`
--

DROP TABLE IF EXISTS `booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking` (
  `booking_id` bigint NOT NULL AUTO_INCREMENT,
  `total_amount` decimal(38,2) NOT NULL,
  `booking_status` enum('CANCELLED','COMPLETED','CONFIRMED','PENDING') NOT NULL,
  `customer_id` bigint NOT NULL,
  `schedule_id` bigint NOT NULL,
  `booking_date` date NOT NULL,
  `order_number` varchar(40) DEFAULT NULL,
  `number_of_passengers` int NOT NULL,
  `adult_count` int DEFAULT NULL,
  `child_count` int DEFAULT NULL,
  PRIMARY KEY (`booking_id`),
  KEY `FKlnnelfsha11xmo2ndjq66fvro` (`customer_id`),
  KEY `FKi6bht551tm4hq20vj4dn1rhpw` (`schedule_id`),
  CONSTRAINT `FKi6bht551tm4hq20vj4dn1rhpw` FOREIGN KEY (`schedule_id`) REFERENCES `tour_schedule` (`schedule_id`),
  CONSTRAINT `FKlnnelfsha11xmo2ndjq66fvro` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking`
--

LOCK TABLES `booking` WRITE;
/*!40000 ALTER TABLE `booking` DISABLE KEYS */;
INSERT INTO `booking` VALUES (5,146500.00,'CANCELLED',9,12,'2026-08-03','ORD-5-47F8166B',1,NULL,NULL),(6,40400.00,'CANCELLED',10,18,'2026-08-03','ORD-6-C53AEA41',1,NULL,NULL),(7,38900.00,'COMPLETED',10,18,'2026-08-03','ORD-7-60D52DBD',1,NULL,NULL),(8,111500.00,'CONFIRMED',8,20,'2026-08-03','ORD-8-54DFD450',1,NULL,NULL),(9,111500.00,'CONFIRMED',8,20,'2026-08-04','ORD-9-4A0507CA',1,NULL,NULL),(10,40400.00,'CONFIRMED',8,18,'2026-08-04','ORD-10-2CD315CC',1,NULL,NULL),(14,9000.00,'PENDING',8,24,'2026-08-04',NULL,1,NULL,NULL),(17,31999.00,'CONFIRMED',8,24,'2026-08-04','ORD-17-D1C776CC',1,NULL,NULL),(18,98900.00,'CONFIRMED',12,13,'2026-08-04','ORD-18-BB0E8B87',1,NULL,NULL),(19,19450.00,'CONFIRMED',12,18,'2026-08-04','ORD-19-F244202A',1,NULL,NULL),(20,30499.00,'CONFIRMED',13,22,'2026-08-04','ORD-20-07963101',1,NULL,NULL),(21,94900.00,'CONFIRMED',14,17,'2026-08-05','ORD-21-E1F1C6D4',1,1,0);
/*!40000 ALTER TABLE `booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_add_on`
--

DROP TABLE IF EXISTS `booking_add_on`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_add_on` (
  `booking_add_on_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL,
  `add_on_id` bigint NOT NULL,
  `booking_id` bigint NOT NULL,
  PRIMARY KEY (`booking_add_on_id`),
  KEY `FKc39jyp43xhv1mb9hyq1139ohm` (`add_on_id`),
  KEY `FK2a6b80jvmy190ir2iyubrncbo` (`booking_id`),
  CONSTRAINT `FK2a6b80jvmy190ir2iyubrncbo` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`),
  CONSTRAINT `FKc39jyp43xhv1mb9hyq1139ohm` FOREIGN KEY (`add_on_id`) REFERENCES `add_on` (`add_on_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_add_on`
--

LOCK TABLES `booking_add_on` WRITE;
/*!40000 ALTER TABLE `booking_add_on` DISABLE KEYS */;
/*!40000 ALTER TABLE `booking_add_on` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_addon`
--

DROP TABLE IF EXISTS `booking_addon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_addon` (
  `booking_addon_id` bigint NOT NULL AUTO_INCREMENT,
  `addon_name` varchar(150) NOT NULL,
  `price_type` enum('PER_BOOKING','PER_PERSON','PER_ROOM') NOT NULL,
  `quantity` int NOT NULL,
  `total_addon_cost` decimal(10,2) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `addon_id` bigint NOT NULL,
  `booking_id` bigint NOT NULL,
  PRIMARY KEY (`booking_addon_id`),
  KEY `FK1fh6ir112y8f9v2vq8g5wwo7b` (`booking_id`),
  KEY `FKpdxh8ius6cmxp2wgyhkq6dqyn` (`addon_id`),
  CONSTRAINT `FK1fh6ir112y8f9v2vq8g5wwo7b` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `FKpdxh8ius6cmxp2wgyhkq6dqyn` FOREIGN KEY (`addon_id`) REFERENCES `tour_addon` (`addon_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_addon`
--

LOCK TABLES `booking_addon` WRITE;
/*!40000 ALTER TABLE `booking_addon` DISABLE KEYS */;
INSERT INTO `booking_addon` VALUES (1,'Travel Insurance','PER_PERSON',1,1500.00,1500.00,9,5),(2,'Travel Insurance','PER_PERSON',1,1500.00,1500.00,15,6),(3,'Travel Insurance','PER_PERSON',1,1500.00,1500.00,16,8),(4,'Travel Insurance','PER_PERSON',1,1500.00,1500.00,16,9),(5,'Travel Insurance','PER_PERSON',1,1500.00,1500.00,15,10),(6,'Travel Insurance','PER_PERSON',1,1500.00,1500.00,17,20),(7,'Travel Insurance','PER_PERSON',1,1500.00,1500.00,14,21);
/*!40000 ALTER TABLE `booking_addon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `cart_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `estimated_amount` decimal(10,2) DEFAULT NULL,
  `pax_summary` varchar(100) DEFAULT NULL,
  `status` enum('ABANDONED','ACTIVE','CONVERTED_TO_BOOKING') DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `schedule_id` bigint NOT NULL,
  `adult_count` int DEFAULT NULL,
  `child_count` int DEFAULT NULL,
  PRIMARY KEY (`cart_id`),
  KEY `FKdebwvad6pp1ekiqy5jtixqbaj` (`customer_id`),
  KEY `FKsl0f1ty8ga0gmsr411340mqid` (`schedule_id`),
  CONSTRAINT `FKdebwvad6pp1ekiqy5jtixqbaj` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `FKsl0f1ty8ga0gmsr411340mqid` FOREIGN KEY (`schedule_id`) REFERENCES `tour_schedule` (`schedule_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` VALUES (3,'2026-08-03 18:40:23.272620',38900.00,'1 passenger(s)','CONVERTED_TO_BOOKING','2026-08-03 18:40:29.710180',10,18,NULL,NULL),(5,'2026-08-04 04:10:35.947902',23999.00,'1 passenger(s)','ACTIVE','2026-08-04 04:10:36.040645',8,22,NULL,NULL);
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_addon`
--

DROP TABLE IF EXISTS `cart_addon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_addon` (
  `cart_addon_id` bigint NOT NULL AUTO_INCREMENT,
  `estimated_cost` decimal(10,2) DEFAULT NULL,
  `quantity` int NOT NULL,
  `addon_id` bigint NOT NULL,
  `cart_id` bigint NOT NULL,
  PRIMARY KEY (`cart_addon_id`),
  KEY `FKl9dpqsriigeye7vkpjabcqg59` (`cart_id`),
  KEY `FKmxtl3dgs9lbncyl5fpcx5yto1` (`addon_id`),
  CONSTRAINT `FKl9dpqsriigeye7vkpjabcqg59` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`cart_id`) ON DELETE CASCADE,
  CONSTRAINT `FKmxtl3dgs9lbncyl5fpcx5yto1` FOREIGN KEY (`addon_id`) REFERENCES `tour_addon` (`addon_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_addon`
--

LOCK TABLES `cart_addon` WRITE;
/*!40000 ALTER TABLE `cart_addon` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_addon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `category_code` varchar(10) NOT NULL,
  `category_name` varchar(100) NOT NULL,
  `description` text,
  `image_url` varchar(255) DEFAULT NULL,
  `is_featured` varchar(1) NOT NULL,
  `status` bit(1) NOT NULL,
  `parent_category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`category_id`),
  KEY `FKs2ride9gvilxy2tcuv7witnxc` (`parent_category_id`),
  CONSTRAINT `FKs2ride9gvilxy2tcuv7witnxc` FOREIGN KEY (`parent_category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'DOM','India','','https://images.pexels.com/photos/19094764/pexels-photo-19094764.jpeg','Y',_binary '',NULL),(2,'INT','International','','https://images.pexels.com/photos/11762493/pexels-photo-11762493.jpeg','Y',_binary '',NULL),(3,'INT','Events','','https://images.pexels.com/photos/30271349/pexels-photo-30271349.jpeg','N',_binary '',NULL),(5,'INT','Europe','European destinations','https://images.pexels.com/photos/10733379/pexels-photo-10733379.jpeg','N',_binary '',2),(6,'INT','Honeymoon','Enjoy your honeymoon with Etour \n','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcShSFxi3Re1NTkQUp9d6_1NEwfkTtej4V9eCrBi6H0VVTFP8fs2QWz37iw&s=10','Y',_binary '',2),(7,'DOM','Yatra','','https://images.pexels.com/photos/16786632/pexels-photo-16786632.jpeg','Y',_binary '',1),(8,'DOM','Others','','https://images.pexels.com/photos/5480518/pexels-photo-5480518.jpeg','N',_binary '',1);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contact_enquiry`
--

DROP TABLE IF EXISTS `contact_enquiry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contact_enquiry` (
  `enquiry_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(190) NOT NULL,
  `message` varchar(4000) NOT NULL,
  `name` varchar(150) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `status` enum('IN_PROGRESS','NEW','RESOLVED') NOT NULL,
  `subject` varchar(200) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`enquiry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contact_enquiry`
--

LOCK TABLES `contact_enquiry` WRITE;
/*!40000 ALTER TABLE `contact_enquiry` DISABLE KEYS */;
/*!40000 ALTER TABLE `contact_enquiry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `content`
--

DROP TABLE IF EXISTS `content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `content` (
  `content_id` bigint NOT NULL AUTO_INCREMENT,
  `content_key` varchar(100) NOT NULL,
  `content_value` text NOT NULL,
  `display_order` int DEFAULT NULL,
  `language_code` varchar(10) DEFAULT NULL,
  `link_url` varchar(500) DEFAULT NULL,
  `media_url` varchar(500) DEFAULT NULL,
  `page_name` varchar(50) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  PRIMARY KEY (`content_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `content`
--

LOCK TABLES `content` WRITE;
/*!40000 ALTER TABLE `content` DISABLE KEYS */;
INSERT INTO `content` VALUES (2,'site.brand','eTour',0,'en',NULL,NULL,'global',_binary ''),(3,'home.hero','Home hero background',0,'en',NULL,'/uploads/hero/hero.jpg','home',_binary ''),(4,'home.hero.eyebrow','Plan less, wander more',1,'en',NULL,NULL,'home',_binary ''),(5,'home.hero.title','Every great journey starts with one honest plan.',2,'en',NULL,NULL,'home',_binary ''),(6,'home.hero.subtitle','Browse curated tours, compare transparent pricing, and book with confidence.',3,'en',NULL,NULL,'home',_binary ''),(7,'home.search.placeholder','Where do you want to go?',4,'en',NULL,NULL,'home',_binary ''),(8,'home.why.eyebrow','Why eTour',0,'en',NULL,NULL,'home',_binary ''),(9,'home.why.title','Built for travellers who plan ahead',1,'en',NULL,NULL,'home',_binary ''),(10,'home.why.01','Verified tours :: Every itinerary is reviewed before it goes live.',2,'en',NULL,NULL,'home',_binary ''),(11,'home.why.02','Transparent pricing :: See the full cost breakdown before you book, always.',3,'en',NULL,NULL,'home',_binary ''),(12,'home.why.03','Flexible booking :: Cancel or adjust your plans from your dashboard.',4,'en',NULL,NULL,'home',_binary ''),(13,'home.why.04','Real support :: Reach a real person when your trip needs attention.',5,'en',NULL,NULL,'home',_binary ''),(14,'home.faq.eyebrow','Good to know',0,'en',NULL,NULL,'home',_binary ''),(15,'home.faq.title','Frequently asked questions',1,'en',NULL,NULL,'home',_binary ''),(16,'home.faq.01','How do I book a tour? :: Open any tour, choose a departure date, add passenger details, and confirm.',2,'en',NULL,NULL,'home',_binary ''),(17,'home.faq.02','Can I cancel a booking? :: Yes, from My Bookings in your dashboard, as long as it is not already completed.',3,'en',NULL,NULL,'home',_binary ''),(18,'home.faq.03','How do add-ons work? :: Optional extras are added during booking and their price is locked in at that time.',4,'en',NULL,NULL,'home',_binary ''),(19,'footer.tagline','Curated tours, transparent pricing, and a booking experience built for travellers who plan ahead.',0,'en',NULL,NULL,'footer',_binary ''),(20,'footer.links.heading','Explore',1,'en',NULL,NULL,'footer',_binary ''),(21,'footer.contact.heading','Contact',2,'en',NULL,NULL,'footer',_binary ''),(22,'footer.contact.phone','+91 22 1234 5678',3,'en',NULL,NULL,'footer',_binary ''),(23,'footer.contact.email','hello@tourindia.example',4,'en',NULL,NULL,'footer',_binary ''),(24,'footer.contact.address','H.O: 111, L J Road, Dadar, Mumbai 400028',5,'en',NULL,NULL,'footer',_binary ''),(25,'footer.newsletter.heading','Stay in the loop',6,'en',NULL,NULL,'footer',_binary ''),(26,'footer.newsletter.blurb','Get new tours and offers in your inbox.',7,'en',NULL,NULL,'footer',_binary ''),(27,'footer.newsletter.placeholder','you@example.com',8,'en',NULL,NULL,'footer',_binary ''),(28,'footer.newsletter.cta','Subscribe',9,'en',NULL,NULL,'footer',_binary ''),(29,'footer.copyright','┬® 2026 TourIndia Travels Pvt Ltd. All rights reserved.',10,'en',NULL,NULL,'footer',_binary ''),(30,'footer.policy.01','Contact Us',0,'en','/contact',NULL,'footer',_binary ''),(31,'footer.policy.02','Site map',1,'en','/sitemap',NULL,'footer',_binary ''),(32,'footer.policy.03','Careers',2,'en','/careers',NULL,'footer',_binary '');
/*!40000 ALTER TABLE `content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `crawling_text`
--

DROP TABLE IF EXISTS `crawling_text`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crawling_text` (
  `crawling_text_id` bigint NOT NULL AUTO_INCREMENT,
  `is_active` bit(1) NOT NULL,
  `sort_order` int DEFAULT NULL,
  `text` text NOT NULL,
  PRIMARY KEY (`crawling_text_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `crawling_text`
--

LOCK TABLES `crawling_text` WRITE;
/*!40000 ALTER TABLE `crawling_text` DISABLE KEYS */;
INSERT INTO `crawling_text` VALUES (1,_binary '',1,'Welcome to IndiaTour - journeys worth remembering since 1998'),(2,_binary '',2,'Book early for Diwali & Christmas special departures'),(3,_binary '',3,'Small groups of max 14 travellers, local hosts and no hidden costs'),(4,_binary '',4,'New: Ladakh stargazing road trips launching this season'),(5,_binary '',5,'Use code WONDER10 for 10% off your first journey'),(7,_binary '',1,'Monsoon offers now live ÔÇö up to 20% off selected domestic tours.'),(8,_binary '',2,'New: Leh-Ladakh departures added for the summer season.');
/*!40000 ALTER TABLE `crawling_text` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `customer_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `phone` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `UKj7ja2xvrxudhvssosd4nu1o92` (`user_id`),
  CONSTRAINT `FKra1cb3fu95r1a0m7aksow0nk4` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

LOCK TABLES `customer` WRITE;
/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` VALUES (1,'admin@test.com','admin sad','9876543560',13),(8,'shrikant@gmail.com','shrikant Tiwari','9892365645',23),(9,'verify.tester@etour.dev','Verify Tester','9876500002',25),(10,'harsh@test.com','Harsh Gupta','9638527415',26),(11,'audit.customer@test.com','Audit Customer','9999999999',27),(12,'angurerishabh2002@gmail.com','Rishabh Angure','1234567890',28),(13,'Aditya@gmail.com','Aditya Mali','9638527413',29),(14,'scrvss@gmail.com','Sarvesh Saraf','0000000000',30);
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `excel_upload_batch`
--

DROP TABLE IF EXISTS `excel_upload_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `excel_upload_batch` (
  `batch_id` bigint NOT NULL AUTO_INCREMENT,
  `failed_rows` int DEFAULT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `status` enum('COMPLETED','COMPLETED_WITH_ERRORS','FAILED','PROCESSING','UPLOADED') DEFAULT NULL,
  `success_rows` int DEFAULT NULL,
  `total_rows` int DEFAULT NULL,
  `uploaded_at` datetime(6) DEFAULT NULL,
  `uploaded_by` bigint NOT NULL,
  PRIMARY KEY (`batch_id`),
  KEY `FKntjyor2hugv9uhriswfkdqshr` (`uploaded_by`),
  CONSTRAINT `FKntjyor2hugv9uhriswfkdqshr` FOREIGN KEY (`uploaded_by`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `excel_upload_batch`
--

LOCK TABLES `excel_upload_batch` WRITE;
/*!40000 ALTER TABLE `excel_upload_batch` DISABLE KEYS */;
/*!40000 ALTER TABLE `excel_upload_batch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice`
--

DROP TABLE IF EXISTS `invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice` (
  `invoice_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `discount_amount` decimal(12,2) NOT NULL,
  `invoice_date` datetime(6) NOT NULL,
  `invoice_number` varchar(255) NOT NULL,
  `invoice_status` enum('CANCELLED','GENERATED') DEFAULT NULL,
  `payment_id` bigint DEFAULT NULL,
  `pdf_url` varchar(500) DEFAULT NULL,
  `sub_total` decimal(12,2) NOT NULL,
  `tax_amount` decimal(12,2) NOT NULL,
  `total_amount` decimal(12,2) NOT NULL,
  `booking_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`invoice_id`),
  UNIQUE KEY `UKt6xkdjx1qtd5whp2iljdfn2yj` (`invoice_number`),
  KEY `FK5e32ukwo9uknwhylogvta4po6` (`customer_id`),
  KEY `FK4jd6uuk7w0d72riyre2w14fl7` (`booking_id`),
  KEY `FKbaxa82hce5x7dqj0sotnc1cxf` (`payment_id`),
  CONSTRAINT `FK4jd6uuk7w0d72riyre2w14fl7` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`) ON DELETE CASCADE,
  CONSTRAINT `FK5e32ukwo9uknwhylogvta4po6` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `FKbaxa82hce5x7dqj0sotnc1cxf` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`payment_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
INSERT INTO `invoice` VALUES (3,'2026-08-03 16:50:32.409762',0.00,'2026-08-03 16:50:32.409762','INV-5-1785756032409','GENERATED',3,NULL,146500.00,7325.00,153825.00,5,9,NULL),(4,'2026-08-03 18:17:07.729081',0.00,'2026-08-03 18:17:07.729081','INV-6-1785761227729','GENERATED',4,NULL,40400.00,2020.00,42420.00,6,10,NULL),(5,'2026-08-03 18:40:55.659472',0.00,'2026-08-03 18:40:55.659472','INV-7-1785762655659','GENERATED',5,NULL,38900.00,1945.00,40845.00,7,10,NULL),(6,'2026-08-03 16:41:40.271355',0.00,'2026-08-03 16:41:40.271355','INV-8-1785775300271','GENERATED',6,NULL,111500.00,5575.00,117075.00,8,8,NULL),(7,'2026-08-03 20:17:56.102950',0.00,'2026-08-03 20:17:56.102950','INV-9-1785788276102','GENERATED',7,NULL,111500.00,5575.00,117075.00,9,8,NULL),(8,'2026-08-03 20:40:21.362057',0.00,'2026-08-03 20:40:21.362057','INV-10-1785789621362','GENERATED',8,NULL,40400.00,2020.00,42420.00,10,8,NULL),(9,'2026-08-04 03:38:09.635700',0.00,'2026-08-04 03:38:09.635700','INV-17-1785814689635','GENERATED',9,NULL,31999.00,1599.95,33598.95,17,8,NULL),(10,'2026-08-04 05:16:23.253476',0.00,'2026-08-04 05:16:23.253476','INV-18-1785820583253','GENERATED',10,NULL,98900.00,4945.00,103845.00,18,12,NULL),(11,'2026-08-04 05:24:05.361595',0.00,'2026-08-04 05:24:05.361595','INV-19-1785821045361','GENERATED',11,NULL,19450.00,972.50,20422.50,19,12,NULL),(12,'2026-08-04 05:35:56.504434',0.00,'2026-08-04 05:35:56.504434','INV-20-1785821756504','GENERATED',12,NULL,30499.00,1524.95,32023.95,20,13,NULL),(13,'2026-08-05 19:49:54.950223',0.00,'2026-08-05 19:49:54.950202','INV-21-1785959394950','GENERATED',13,NULL,94900.00,4745.00,99645.00,21,14,NULL);
/*!40000 ALTER TABLE `invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `itinerary`
--

DROP TABLE IF EXISTS `itinerary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `itinerary` (
  `itinerary_id` bigint NOT NULL AUTO_INCREMENT,
  `day_number` int NOT NULL,
  `day_type` varchar(50) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `icon_name` varchar(50) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`itinerary_id`),
  KEY `FK412aybnynrvmjtt4aknad4q6l` (`tour_id`),
  CONSTRAINT `FK412aybnynrvmjtt4aknad4q6l` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `itinerary`
--

LOCK TABLES `itinerary` WRITE;
/*!40000 ALTER TABLE `itinerary` DISABLE KEYS */;
INSERT INTO `itinerary` VALUES (2,1,NULL,'Arrive and check in; orientation walk around The Rajasthan Edit.',NULL,'Arrival',1),(3,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',1),(4,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',1),(5,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',1),(6,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',1),(7,1,NULL,'Arrive and check in; orientation walk around Japan, in Good Company.',NULL,'Arrival',2),(8,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',2),(9,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',2),(10,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',2),(11,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',2),(12,1,NULL,'Arrive and check in; orientation walk around Backwaters & Spice Roads.',NULL,'Arrival',3),(13,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',3),(14,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',3),(15,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',3),(16,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',3),(17,1,NULL,'Arrive and check in; orientation walk around Vietnam: North to South.',NULL,'Arrival',4),(18,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',4),(19,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',4),(20,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',4),(21,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',4),(22,1,NULL,'Arrive and check in; orientation walk around Christmas Markets, Slowly.',NULL,'Arrival',5),(23,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',5),(24,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',5),(25,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',5),(26,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',5),(27,1,NULL,'Arrive and check in; orientation walk around Ladakh, Above the Clouds.',NULL,'Arrival',6),(28,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',6),(29,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',6),(30,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',6),(31,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',6),(32,1,NULL,'Arrive and check in; orientation walk around Goa, Sunkissed & Slow.',NULL,'Arrival',7),(33,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',7),(34,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',7),(35,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',7),(36,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',7),(37,1,NULL,'Airport pickup and check-in, evening Thames river walk.',NULL,'Arrival in London',8),(38,2,NULL,'Buckingham Palace, Big Ben, London Eye and Westminster Abbey.',NULL,'City Highlights',8),(39,3,NULL,'British Museum in the morning, Camden Market in the afternoon.',NULL,'Museums & Markets',8),(40,1,NULL,'Arrive and check in; orientation walk around Dubai & the Desert.',NULL,'Arrival',17),(41,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',17),(42,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',17),(43,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',17),(44,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',17),(45,1,NULL,'Arrive and check in; orientation walk around Thailand Island Hopper.',NULL,'Arrival',18),(46,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',18),(47,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',18),(48,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',18),(49,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',18),(50,1,NULL,'Arrive and check in; orientation walk around Paris & Provence.',NULL,'Arrival',19),(51,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',19),(52,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',19),(53,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',19),(54,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',19),(55,1,NULL,'Arrive and check in; orientation walk around Andaman Islands Escape.',NULL,'Arrival',20),(56,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',20),(57,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',20),(58,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',20),(59,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',20),(60,1,NULL,'Arrive and check in; orientation walk around Bali Honeymoon Escape.',NULL,'Arrival',21),(61,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',21),(62,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',21),(63,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',21),(64,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',21),(65,1,NULL,'Arrive and check in; orientation walk around Mumbai T20 Cricket Weekend.',NULL,'Arrival',22),(66,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',22),(67,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',22),(68,4,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',22),(70,1,NULL,'Arrive and check in; orientation walk around Bangkok.',NULL,'Arrival',28),(71,2,NULL,'Guided sightseeing and local experiences, day 2 of the tour.',NULL,'Sightseeing & Local Experiences',28),(72,3,NULL,'Guided sightseeing and local experiences, day 3 of the tour.',NULL,'Sightseeing & Local Experiences',28),(73,4,NULL,'Guided sightseeing and local experiences, day 4 of the tour.',NULL,'Sightseeing & Local Experiences',28),(74,5,NULL,'Free time for last-minute shopping, then transfer for departure.',NULL,'Departure',28),(75,1,NULL,'Pickup from Haridwar or Rishikesh.\nDrive through Devprayag, Rudraprayag, and scenic Himalayan roads.\nCheck in to the hotel at Guptkashi.\nDinner and overnight stay.',NULL,'Haridwar/Rishikesh → Guptkashi',29),(76,2,NULL,'Early morning drive to Sonprayag and Gaurikund.\nBegin the 16 km trek to Kedarnath (or opt for pony/helicopter services).\nVisit the Kedarnath Temple and attend the evening aarti.\nOvernight stay near Kedarnath.',NULL,'Guptkashi → Gaurikund → Kedarnath',29),(77,3,NULL,'Attend the morning darshan at Kedarnath Temple.\nTrek back to Gaurikund.\nDrive to Guptkashi.\nCheck in to the hotel, dinner, and overnight stay.',NULL,'Kedarnath → Guptkashi',29),(78,4,NULL,'Breakfast and hotel check-out.\nDrive back to Haridwar or Rishikesh.\nDrop-off at the railway station or bus stand with unforgettable memories.',NULL,'Guptkashi → Haridwar/Rishikesh',29);
/*!40000 ALTER TABLE `itinerary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `journey_detail`
--

DROP TABLE IF EXISTS `journey_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `journey_detail` (
  `journey_id` bigint NOT NULL AUTO_INCREMENT,
  `mode_of_travel` enum('CRUISE','FLIGHT','OTHER','ROAD','TRAIN') DEFAULT NULL,
  `notes` text,
  `sequence_no` int NOT NULL,
  `from_location_id` bigint DEFAULT NULL,
  `to_location_id` bigint DEFAULT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`journey_id`),
  KEY `FKhptr5j1whsekhw764m6008haq` (`to_location_id`),
  KEY `FKlg3akvuffc8jgd6bnkxhvaner` (`from_location_id`),
  KEY `FKnqw071gc7deutnnfvlrevv5cv` (`tour_id`),
  CONSTRAINT `FKhptr5j1whsekhw764m6008haq` FOREIGN KEY (`to_location_id`) REFERENCES `location` (`location_id`) ON DELETE CASCADE,
  CONSTRAINT `FKlg3akvuffc8jgd6bnkxhvaner` FOREIGN KEY (`from_location_id`) REFERENCES `location` (`location_id`) ON DELETE CASCADE,
  CONSTRAINT `FKnqw071gc7deutnnfvlrevv5cv` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `journey_detail`
--

LOCK TABLES `journey_detail` WRITE;
/*!40000 ALTER TABLE `journey_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `journey_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `location` (
  `location_id` bigint NOT NULL AUTO_INCREMENT,
  `country` varchar(100) NOT NULL,
  `country_code` varchar(2) DEFAULT NULL,
  `location_name` varchar(150) NOT NULL,
  `state_province` varchar(100) DEFAULT NULL,
  `status` bit(1) NOT NULL,
  PRIMARY KEY (`location_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `location`
--

LOCK TABLES `location` WRITE;
/*!40000 ALTER TABLE `location` DISABLE KEYS */;
/*!40000 ALTER TABLE `location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nav_menu_item`
--

DROP TABLE IF EXISTS `nav_menu_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nav_menu_item` (
  `nav_menu_item_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `label` varchar(100) NOT NULL,
  `link` varchar(255) NOT NULL,
  `sort_order` int DEFAULT NULL,
  `parent_item_id` bigint DEFAULT NULL,
  PRIMARY KEY (`nav_menu_item_id`),
  UNIQUE KEY `uk_nav_label_link` (`label`,`link`),
  KEY `FKe473vh7gtu7wis4lcgxdoj6xq` (`parent_item_id`),
  CONSTRAINT `FKe473vh7gtu7wis4lcgxdoj6xq` FOREIGN KEY (`parent_item_id`) REFERENCES `nav_menu_item` (`nav_menu_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nav_menu_item`
--

LOCK TABLES `nav_menu_item` WRITE;
/*!40000 ALTER TABLE `nav_menu_item` DISABLE KEYS */;
INSERT INTO `nav_menu_item` VALUES (1,_binary '\0','Destinations','/tours',1,NULL),(2,_binary '','India','/tours?q=India',1,1),(3,_binary '','International','/tours?q=Japan',2,1),(4,_binary '','Honeymoon','/tours?q=Honeymoon',3,1),(5,_binary '','Adventure','/tours?q=Ladakh',4,1),(7,_binary '','Our story','/about',3,NULL),(8,_binary '','Contact','/contact',4,NULL),(9,_binary '','Search tours','/search',3,NULL),(10,_binary '','Home','/home',1,NULL);
/*!40000 ALTER TABLE `nav_menu_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `newsletter_subscriber`
--

DROP TABLE IF EXISTS `newsletter_subscriber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `newsletter_subscriber` (
  `subscriber_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `email` varchar(190) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `subscribed_at` datetime(6) NOT NULL,
  `unsubscribed_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`subscriber_id`),
  UNIQUE KEY `UKjmyiin4onxy5rh5bskafkxrgl` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `newsletter_subscriber`
--

LOCK TABLES `newsletter_subscriber` WRITE;
/*!40000 ALTER TABLE `newsletter_subscriber` DISABLE KEYS */;
/*!40000 ALTER TABLE `newsletter_subscriber` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `passenger`
--

DROP TABLE IF EXISTS `passenger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `passenger` (
  `passenger_id` bigint NOT NULL AUTO_INCREMENT,
  `booking_id` bigint NOT NULL,
  `dob` date DEFAULT NULL,
  `full_name` varchar(150) NOT NULL,
  `gender` varchar(1) DEFAULT NULL,
  `id_proof_number` varchar(50) NOT NULL,
  `id_proof_type` varchar(255) DEFAULT NULL,
  `nationality` varchar(2) DEFAULT NULL,
  `needs_extra_bed` bit(1) DEFAULT NULL,
  `address_line1` varchar(200) DEFAULT NULL,
  `address_line2` varchar(200) DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `pincode` varchar(20) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `occupancy` enum('CHILD_WITHOUT_BED','CHILD_WITH_BED','EXTRA_BED','SINGLE','TRIPLE','TWIN') DEFAULT NULL,
  `room_charge` decimal(10,2) DEFAULT NULL,
  `passenger_price` decimal(12,2) DEFAULT NULL,
  `passenger_type` enum('ADULT','CHILD','INFANT') DEFAULT NULL,
  PRIMARY KEY (`passenger_id`),
  UNIQUE KEY `UK3i7td714f7a2k1340degxe8dy` (`id_proof_number`),
  KEY `FKtco0omesfld1qi5sw76eomvt4` (`booking_id`),
  CONSTRAINT `FKtco0omesfld1qi5sw76eomvt4` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `passenger`
--

LOCK TABLES `passenger` WRITE;
/*!40000 ALTER TABLE `passenger` DISABLE KEYS */;
INSERT INTO `passenger` VALUES (18,5,'1990-05-15','Verify Tester','M','PA1234567','','IN',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(19,6,'2000-01-25','harsh','M','963852741852','adhar','IN',_binary '',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(20,7,'2000-01-22','harsh','M','123456789963','Adharr','IN',_binary '',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(21,8,'1990-01-25','shrikant','M','9638563416','adhaar','IN',_binary '','juhu circle ','gandu','mumbau','India','444303','maharashtra',NULL,NULL,NULL,NULL),(22,9,'2000-01-22','shrikant','M','963852471','adhar ','IN',_binary '','juhu','','mumbai','India','422008','Maharashtra',NULL,NULL,NULL,NULL),(23,10,'2000-04-25','shrikant','M','963852741','Adharr','IN',_binary '','mumbai','','mumbai','','442008','','SINGLE',0.00,NULL,NULL),(27,14,'2000-02-02','Aditya Mali','M','9638527415','Adhaar','IN',_binary '','Juhu ','','Mumbai','India','422005','Maharashtra','SINGLE',8000.00,NULL,NULL),(30,17,'2000-02-02','shrikant','','9638574152','adhaar','',_binary '','Andheri','','Mumbai','India','422005','Maharashtra','SINGLE',8000.00,NULL,NULL),(31,18,'2002-01-07','Rishabh Angure','M','45520006555','Aadhaar','IN',_binary '','Indrapur','Vile parle','Mumbaiu','India','460222','Maharashtra','SINGLE',9000.00,NULL,NULL),(32,19,'2018-06-04','Rishabh Angure','M','1234567890','Aadhaar','IN',_binary '','Akola Bazar Rd','Baradari','Khamgaon','India','444303','Maharashtra','TWIN',0.00,NULL,NULL),(33,20,'2000-03-02','Aditya Mali','','963852741963','Adhaar','IN',_binary '','Juhu ','Mumbai','Mumbau','India','422005','Maharashtra','TWIN',5000.00,NULL,NULL),(34,21,'2003-04-25','Sarvesh Saraf','M','960000','adhaar','IN',_binary '','juhu circle mumbai','','Mumbai','India','442005','Maharashtra','TWIN',10500.00,93400.00,'ADULT');
/*!40000 ALTER TABLE `passenger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(10,2) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `payment_method` varchar(30) DEFAULT NULL,
  `payment_status` enum('FAILED','PENDING','REFUNDED','SUCCESS') DEFAULT NULL,
  `transaction_ref` varchar(100) DEFAULT NULL,
  `booking_id` bigint NOT NULL,
  `card_summary` varchar(40) DEFAULT NULL,
  `gateway` varchar(30) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `FKqewrl4xrv9eiad6eab3aoja65` (`booking_id`),
  CONSTRAINT `FKqewrl4xrv9eiad6eab3aoja65` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (3,146500.00,'2026-08-03 16:50:32.403029','CARD','SUCCESS','TXN-b613ce75-b6cf-484f-bb11-6421cbb768d9',5,NULL,NULL,NULL),(4,40400.00,'2026-08-03 18:17:07.724080','CARD','SUCCESS','TXN-82eaf48d-9c9c-44f9-893a-f0db4a074aa7',6,NULL,NULL,NULL),(5,38900.00,'2026-08-03 18:40:55.655019','CARD','SUCCESS','TXN-5ddad30c-025f-4e3b-bf4a-b4c148f60f05',7,NULL,NULL,NULL),(6,111500.00,'2026-08-03 16:41:40.267300','CARD','SUCCESS','TXN-50cdbae1-1d5b-4798-94eb-7ba37a380d2e',8,NULL,NULL,NULL),(7,111500.00,'2026-08-03 20:17:56.090883','CREDIT_CARD','SUCCESS','TXN-bcc58012-455d-4ec0-bdb2-d5a1e0336564',9,'VISA ****4242','SIMULATED','2026-08-03 20:17:56.090883'),(8,40400.00,'2026-08-03 20:40:21.351927','CREDIT_CARD','SUCCESS','TXN-709d9caa-0abb-4b2c-92b8-f2ba8d13cc6c',10,'VISA ****4242','SIMULATED','2026-08-03 20:40:21.351927'),(9,31999.00,'2026-08-04 03:38:09.610121','CREDIT_CARD','SUCCESS','TXN-cf62b1da-aa6d-4f79-87bd-7dcd501c193b',17,'VISA ****4242','SIMULATED','2026-08-04 03:38:09.610121'),(10,98900.00,'2026-08-04 05:16:23.245437','CREDIT_CARD','SUCCESS','TXN-8460a31a-c720-4338-9aa0-28cd7834d06e',18,'CARD ****0012','SIMULATED','2026-08-04 05:16:23.245437'),(11,19450.00,'2026-08-04 05:24:05.341598','CREDIT_CARD','SUCCESS','TXN-6cd62f7b-115a-4974-963d-ec6f09bffe94',19,'VISA ****4242','SIMULATED','2026-08-04 05:24:05.341598'),(12,30499.00,'2026-08-04 05:35:56.498319','CREDIT_CARD','SUCCESS','TXN-18915869-5f55-4396-b384-fd71374af0b3',20,'VISA ****4242','SIMULATED','2026-08-04 05:35:56.498319'),(13,99645.00,'2026-08-05 19:49:54.936502','CREDIT_CARD','SUCCESS','TXN-36d68852-bc7f-4570-9cda-132d9779846d',21,'VISA ****4242','SIMULATED','2026-08-05 19:49:54.936539');
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `review_id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) NOT NULL,
  `rating` int DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`review_id`),
  KEY `FKgce54o0p6uugoc2tev4awewly` (`customer_id`),
  KEY `FK2yxuruefnrj0xan64vi2gg7ag` (`tour_id`),
  CONSTRAINT `FK2yxuruefnrj0xan64vi2gg7ag` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE,
  CONSTRAINT `FKgce54o0p6uugoc2tev4awewly` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `review_chk_1` CHECK (((`rating` >= 1) and (`rating` <= 5)))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (2,'Very devotional tour.',5,8,29);
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `role_id` bigint NOT NULL AUTO_INCREMENT,
  `description` text,
  `role_name` varchar(100) NOT NULL,
  `status` bit(1) NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `UK716hgxp60ym1lifrdgp67xt5k` (`role_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'Normal User','CUSTOMER',_binary ''),(2,'Administrator','ADMIN',_binary '');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room_charge`
--

DROP TABLE IF EXISTS `room_charge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_charge` (
  `room_charge_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `charge` decimal(10,2) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  `occupancy` enum('CHILD_WITHOUT_BED','CHILD_WITH_BED','EXTRA_BED','SINGLE','TRIPLE','TWIN') NOT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`room_charge_id`),
  UNIQUE KEY `uk_room_charge_tour_occupancy` (`tour_id`,`occupancy`),
  CONSTRAINT `FKoa3kj53ngodvdok4mj17kg3kf` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room_charge`
--

LOCK TABLES `room_charge` WRITE;
/*!40000 ALTER TABLE `room_charge` DISABLE KEYS */;
INSERT INTO `room_charge` VALUES (1,_binary '',3500.00,'private rooms','SINGLE',1),(2,_binary '',1000.00,NULL,'TRIPLE',1),(3,_binary '',1200.00,NULL,'TWIN',1),(4,_binary '',9000.00,'Private room','SINGLE',2),(5,_binary '',3500.00,NULL,'TWIN',2),(6,_binary '',7500.00,'Extra Futon in exisitng bed','EXTRA_BED',2),(7,_binary '',2000.00,NULL,'SINGLE',3),(8,_binary '',1000.00,NULL,'TWIN',3),(9,_binary '',500.00,NULL,'EXTRA_BED',3),(10,_binary '',3500.00,NULL,'SINGLE',4),(11,_binary '',3200.00,NULL,'TWIN',4),(12,_binary '',1200.00,NULL,'EXTRA_BED',4),(13,_binary '',12000.00,NULL,'SINGLE',5),(14,_binary '',9000.00,NULL,'TWIN',5),(15,_binary '',6000.00,NULL,'EXTRA_BED',5),(16,_binary '',5200.00,NULL,'SINGLE',6),(17,_binary '',3500.00,NULL,'TWIN',6),(18,_binary '',400.00,NULL,'EXTRA_BED',6),(19,_binary '',6000.00,NULL,'SINGLE',7),(20,_binary '',2000.00,NULL,'TWIN',7),(21,_binary '',1500.00,NULL,'EXTRA_BED',7),(22,_binary '',15000.00,NULL,'SINGLE',8),(23,_binary '',8000.00,NULL,'TWIN',8),(24,_binary '',3000.00,NULL,'EXTRA_BED',8),(25,_binary '',9000.00,NULL,'SINGLE',17),(26,_binary '',7000.00,NULL,'TWIN',17),(27,_binary '',3000.00,NULL,'EXTRA_BED',17),(28,_binary '',6500.00,NULL,'SINGLE',18),(29,_binary '',4500.00,NULL,'TWIN',18),(30,_binary '',3000.00,NULL,'EXTRA_BED',18),(31,_binary '',17000.00,NULL,'SINGLE',19),(32,_binary '',12000.00,NULL,'TWIN',19),(33,_binary '',9000.00,NULL,'EXTRA_BED',19),(34,_binary '',5000.00,NULL,'SINGLE',20),(35,_binary '',3000.00,NULL,'TWIN',20),(36,_binary '',1500.00,NULL,'EXTRA_BED',20),(37,_binary '',12500.00,NULL,'SINGLE',21),(38,_binary '',10500.00,NULL,'TWIN',21),(39,_binary '',8000.00,NULL,'EXTRA_BED',21),(40,_binary '',4300.00,NULL,'SINGLE',22),(41,_binary '',1000.00,NULL,'TRIPLE',22),(42,_binary '',500.00,NULL,'EXTRA_BED',22),(43,_binary '',19200.00,NULL,'SINGLE',28),(44,_binary '',12600.00,NULL,'TWIN',28),(45,_binary '',8000.00,NULL,'EXTRA_BED',28),(46,_binary '',8000.00,'Temple View','SINGLE',29),(47,_binary '',5000.00,NULL,'TWIN',29),(48,_binary '',800.00,NULL,'EXTRA_BED',29);
/*!40000 ALTER TABLE `room_charge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sector`
--

DROP TABLE IF EXISTS `sector`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sector` (
  `sector_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `description` text,
  `icon_url` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `sort_order` int DEFAULT NULL,
  `tour_count` int DEFAULT NULL,
  PRIMARY KEY (`sector_id`),
  UNIQUE KEY `UKgt10yoxplooy0sueukkysbjrr` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sector`
--

LOCK TABLES `sector` WRITE;
/*!40000 ALTER TABLE `sector` DISABLE KEYS */;
INSERT INTO `sector` VALUES (1,_binary '','Explore Europe, Dubai, Thailand & beyond.','https://unpkg.com/lucide@latest/icons/globe-2.svg','https://images.unsplash.com/photo-1507525428034-b723cf961d3e','International',1,126),(2,_binary '','Discover Incredible India.','https://unpkg.com/lucide@latest/icons/map-pinned.svg','https://images.unsplash.com/photo-1524492412937-b28074a5d7da','Domestic',2,248),(3,_binary '','Romantic escapes for couples.','https://unpkg.com/lucide@latest/icons/heart.svg','https://images.unsplash.com/photo-1720515111657-b3c0a7f4367d?q=80&w=1170&auto=format&fit=crop','Honeymoon',3,54),(4,_binary '','Celebrate India\'s vibrant festivals.','https://unpkg.com/lucide@latest/icons/party-popper.svg','https://images.unsplash.com/photo-1514525253161-7a46d19cd819','Festivals',4,36),(5,_binary '','Cricket, football and sporting events.','https://unpkg.com/lucide@latest/icons/trophy.svg','https://images.unsplash.com/photo-1517649763962-0c623066013b','Sports',5,28),(6,_binary '','Trekking, rafting and mountain adventures.','https://unpkg.com/lucide@latest/icons/mountain.svg','https://images.unsplash.com/photo-1464822759023-fed622ff2c3b','Adventure',6,84),(7,_binary '','Spiritual journeys across India.','https://unpkg.com/lucide@latest/icons/landmark.svg','https://images.unsplash.com/photo-1587474260584-136574528ed5','Cultural',7,41),(8,_binary '','Safari and wildlife experiences.','https://unpkg.com/lucide@latest/icons/trees.svg','https://images.unsplash.com/photo-1472396961693-142e6e269027','Wildlife',8,22);
/*!40000 ALTER TABLE `sector` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stay_meal`
--

DROP TABLE IF EXISTS `stay_meal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stay_meal` (
  `stay_meal_id` bigint NOT NULL AUTO_INCREMENT,
  `breakfast` bit(1) DEFAULT NULL,
  `day_number` int NOT NULL,
  `dinner` bit(1) DEFAULT NULL,
  `hotel_name` varchar(150) DEFAULT NULL,
  `lunch` bit(1) DEFAULT NULL,
  `location_id` bigint DEFAULT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`stay_meal_id`),
  KEY `FKas3f6umr6s8p5bcmnbeiub4mq` (`tour_id`),
  KEY `FKb7mfgyp3gvb9oxh50xeiv56n6` (`location_id`),
  CONSTRAINT `FKas3f6umr6s8p5bcmnbeiub4mq` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE,
  CONSTRAINT `FKb7mfgyp3gvb9oxh50xeiv56n6` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stay_meal`
--

LOCK TABLES `stay_meal` WRITE;
/*!40000 ALTER TABLE `stay_meal` DISABLE KEYS */;
/*!40000 ALTER TABLE `stay_meal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sub_sector`
--

DROP TABLE IF EXISTS `sub_sector`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sub_sector` (
  `sub_sector_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `description` text,
  `icon_url` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `sort_order` int DEFAULT NULL,
  `sector_id` bigint NOT NULL,
  PRIMARY KEY (`sub_sector_id`),
  KEY `FKn5hr2fxb4gic51wwpqksjbqft` (`sector_id`),
  CONSTRAINT `FKn5hr2fxb4gic51wwpqksjbqft` FOREIGN KEY (`sector_id`) REFERENCES `sector` (`sector_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sub_sector`
--

LOCK TABLES `sub_sector` WRITE;
/*!40000 ALTER TABLE `sub_sector` DISABLE KEYS */;
INSERT INTO `sub_sector` VALUES (1,_binary '','Valleys, shikaras and apple orchards.','https://unpkg.com/lucide@latest/icons/snowflake.svg','https://images.pexels.com/photos/25786566/pexels-photo-25786566.jpeg','Kashmir',1,2),(2,_binary '','Fortresses, palaces and desert nights.','https://unpkg.com/lucide@latest/icons/landmark.svg','https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=900&q=80','Rajasthan',2,2),(3,_binary '','Backwaters, tea gardens and spice roads.','https://unpkg.com/lucide@latest/icons/waves.svg','https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=900&q=80','Kerala',3,2),(4,_binary '','High passes, monasteries and vast skies.','https://unpkg.com/lucide@latest/icons/mountain.svg','https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=900&q=80','Ladakh',4,2),(5,_binary '','Beaches, cafés and slow evenings.','https://unpkg.com/lucide@latest/icons/sun.svg','https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=900&q=80','Goa',5,2),(6,_binary '','Islands, corals and turquoise water.','https://unpkg.com/lucide@latest/icons/palmtree.svg','https://images.unsplash.com/photo-1509233725247-49e657c54213?auto=format&fit=crop&w=900&q=80','Andaman',6,2),(7,_binary '','Old cities, rail journeys and markets.','https://unpkg.com/lucide@latest/icons/globe-2.svg','https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=900&q=80','Europe',1,1),(8,_binary '','Tea houses, temples and neon nights.','https://unpkg.com/lucide@latest/icons/cherry.svg','https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=900&q=80','Japan',2,1),(9,_binary '','Lanterns, rice fields and street food.','https://unpkg.com/lucide@latest/icons/sailboat.svg','https://images.unsplash.com/photo-1528181304800-259b08848526?auto=format&fit=crop&w=900&q=80','Vietnam',3,1),(10,_binary '','Skyline, desert safaris and souks.','https://unpkg.com/lucide@latest/icons/building-2.svg','https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=900&q=80','Dubai',4,1),(11,_binary '','Islands, temples and night markets.','https://unpkg.com/lucide@latest/icons/island.svg','https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?auto=format&fit=crop&w=900&q=80','Thailand',5,1),(12,_binary '','Lagoons, reefs and luxury resorts.','https://unpkg.com/lucide@latest/icons/sunset.svg','https://images.unsplash.com/photo-1546422904-90eab23c3d7e?auto=format&fit=crop&w=900&q=80','Mauritius',6,1),(13,_binary '','Treks, rivers and mountain camps.','https://unpkg.com/lucide@latest/icons/tent.svg','https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=80','Uttarakhand',1,6),(14,_binary '','The Himalayas in their quietest form.','https://unpkg.com/lucide@latest/icons/cloud.svg','https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=900&q=80','Sikkim',2,6),(15,_binary '','Cold desert trails and old monasteries.','https://unpkg.com/lucide@latest/icons/compass.svg','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=80','Spiti',3,6),(16,_binary '','Temples, villas and sunset cliffs.','https://unpkg.com/lucide@latest/icons/heart.svg','https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=900&q=80','Bali',1,3),(17,_binary '','Private beaches for two.','https://unpkg.com/lucide@latest/icons/heart-handshake.svg','https://images.unsplash.com/photo-1509233725247-49e657c54213?auto=format&fit=crop&w=900&q=80','Andaman & Nicobar',2,3),(18,_binary '','Test matches, T20 nights and stadium tours.','https://unpkg.com/lucide@latest/icons/circle-dot.svg','https://images.unsplash.com/photo-1531415074968-036ba1b575da?auto=format&fit=crop&w=900&q=80','Cricket',1,5),(19,_binary '','European nights, derbies and fan experiences.','https://unpkg.com/lucide@latest/icons/circle.svg','https://images.unsplash.com/photo-1489944440615-453fc2b6a9a9?auto=format&fit=crop&w=900&q=80','Football',2,5);
/*!40000 ALTER TABLE `sub_sector` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour`
--

DROP TABLE IF EXISTS `tour`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour` (
  `tour_id` bigint NOT NULL AUTO_INCREMENT,
  `base_price` decimal(10,2) NOT NULL,
  `description` text,
  `duration_days` int NOT NULL,
  `status` enum('ACTIVE','DRAFT','INACTIVE') NOT NULL,
  `title` varchar(200) NOT NULL,
  `tour_code` enum('ADV','DEV','DOM','INT') NOT NULL,
  `blurb` text,
  `country` varchar(100) DEFAULT NULL,
  `dates` text,
  `extra_bed_charge` decimal(10,2) DEFAULT NULL,
  `images` text,
  `includes` text,
  `itinerary` text,
  `old_price` decimal(10,2) DEFAULT NULL,
  `place` varchar(200) DEFAULT NULL,
  `rating` double DEFAULT NULL,
  `reviews` int NOT NULL DEFAULT '0',
  `single_supplement` decimal(10,2) DEFAULT NULL,
  `tags` text,
  PRIMARY KEY (`tour_id`),
  CONSTRAINT `tour_chk_1` CHECK ((`duration_days` >= 1))
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour`
--

LOCK TABLES `tour` WRITE;
/*!40000 ALTER TABLE `tour` DISABLE KEYS */;
INSERT INTO `tour` VALUES (1,38900.00,'A sun-washed journey through royal cities, blue lanes and lakeside evenings.',8,'ACTIVE','The Rajasthan Edit','DOM','A sun-washed journey through royal cities, blue lanes and lakeside evenings.','India','[\"12 Oct 2024\",\"02 Nov 2024\",\"21 Dec 2024\"]',5000.00,'[\"https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=1400&q=85\"]','[\"7 nights boutique hotels\",\"Daily breakfast + 3 local meals\",\"Private air-conditioned transfers\",\"Local hosts and entrance fees\"]','[\"Arrive in Jaipur, pink city welcome walk\",\"Amber Fort at golden hour · block printing studio\",\"Drive to Jodhpur via Pushkar\",\"Blue City rooftops and Mehrangarh Fort\",\"Jodhpur to Udaipur, a desert-to-lake day\",\"Udaipur palaces, boat ride and local kitchen\",\"A final lakeside breakfast and farewell\"]',44900.00,'Jaipur · Jodhpur · Udaipur',4.9,84,15000.00,'[\"Palaces\",\"Slow travel\",\"Small group\"]'),(2,124500.00,'Tea houses, tiny ramen bars and the quiet beauty between the big moments.',10,'ACTIVE','Japan, in Good Company','INT','Tea houses, tiny ramen bars and the quiet beauty between the big moments.','Japan','[\"06 Oct 2024\",\"03 Nov 2024\",\"18 Mar 2025\"]',12000.00,'[\"https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1528164344705-47542687000d?auto=format&fit=crop&w=1400&q=85\"]','[\"9 nights design hotels\",\"Rail pass and airport transfers\",\"Breakfast daily\",\"Tea ceremony and food walk\"]','[\"Tokyo neighbourhoods and teamLab evening\",\"Tsukiji market breakfast and Shibuya\",\"Bullet train to Kyoto\",\"Fushimi Inari before the crowds\",\"Nara deer park and a countryside lunch\",\"Arashiyama bamboo grove and tea ceremony\",\"Kyoto free day, with our handpicked map\"]',139000.00,'Tokyo · Kyoto · Nara',4.8,61,35000.00,'[\"Food\",\"Culture\",\"Autumn\"]'),(3,31900.00,'A gentler India: misty tea gardens, warm kitchens and a night on the water.',7,'ACTIVE','Backwaters & Spice Roads','DOM','A gentler India: misty tea gardens, warm kitchens and a night on the water.','India','[\"19 Jul 2024\",\"16 Aug 2024\",\"11 Jan 2025\"]',4000.00,'[\"https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1593693397690-362cb9666fc2?auto=format&fit=crop&w=1400&q=85\"]','[\"6 nights handpicked stays\",\"1 night private houseboat\",\"All breakfasts and 4 dinners\",\"Experiences as listed\"]','[\"Fort Kochi galleries and spice market\",\"Munnar tea country at dawn\",\"Tea estate picnic and sunset\",\"Drive through cardamom hills\",\"Houseboat check-in and village canoe ride\",\"Backwater sunrise and Alleppey kitchen\",\"Farewell brunch by the sea\"]',NULL,'Kochi · Munnar · Alleppey',4.9,112,10000.00,'[\"Nature\",\"Wellness\",\"Monsoon\"]'),(4,69900.00,'A bright, flavourful trail from old Hanoi alleys to Hoi An lantern light.',9,'ACTIVE','Vietnam: North to South','INT','A bright, flavourful trail from old Hanoi alleys to Hoi An lantern light.','Vietnam','[\"14 Sep 2024\",\"09 Nov 2024\",\"08 Feb 2025\"]',8000.00,'[\"https://images.unsplash.com/photo-1528181304800-259b08848526?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=1400&q=85\"]','[\"8 nights hotels\",\"Domestic flights and transfers\",\"7 breakfasts + food walk\",\"Cooking class and local guides\"]','[\"Hanoi old quarter food walk\",\"Ninh Binh limestone valleys\",\"Fly to Da Nang and lantern-lit Hoi An\",\"Cycling through rice fields\",\"Beach morning and cooking class\",\"Saigon cafés and hidden courtyards\",\"Mekong Delta day trip\"]',76900.00,'Hanoi · Hoi An · Saigon',4.7,47,20000.00,'[\"Street food\",\"Lanterns\",\"Coast\"]'),(5,109900.00,'Three luminous cities, one beautifully paced winter week.',8,'ACTIVE','Christmas Markets, Slowly','INT','Three luminous cities, one beautifully paced winter week.','Europe','[\"29 Nov 2024\",\"06 Dec 2024\",\"13 Dec 2024\"]',10000.00,'[\"https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1519677100203-a0e668c92439?auto=format&fit=crop&w=1400&q=85\"]','[\"7 nights central hotels\",\"Rail between cities\",\"Concert tickets and local hosts\",\"Breakfast daily\"]','[\"Vienna welcome and evening concert\",\"Christmas markets and coffeehouses\",\"Train to Prague\",\"Old Town walk and market evening\",\"Budapest thermal baths\",\"Danube lights and ruin-bar supper\",\"A slow final morning\"]',NULL,'Vienna · Prague · Budapest',4.8,39,28000.00,'[\"Festive\",\"Markets\",\"Music\"]'),(6,45900.00,'High passes, monastery bells and vast skies on a small-group Himalayan road trip.',6,'ACTIVE','Ladakh, Above the Clouds','DOM','High passes, monastery bells and vast skies on a small-group Himalayan road trip.','India','[\"05 Sep 2024\",\"19 Sep 2024\",\"03 Oct 2024\"]',5000.00,'[\"https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=1400&q=85\"]','[\"5 nights stays\",\"4x4 mountain transport\",\"Breakfast + dinner\",\"Permits and local guide\"]','[\"Leh acclimatisation and sunset market\",\"Khardung La to Nubra Valley\",\"Sand dunes and monastery morning\",\"Drive to Pangong Lake\",\"Lake sunrise and return to Leh\",\"Farewell café and departure\"]',NULL,'Leh · Nubra · Pangong',4.9,76,12000.00,'[\"Mountains\",\"Road trip\",\"Stargazing\"]'),(7,35900.00,'Golden sands, cafés by the sea and Portuguese-era lanes at a gentle, unhurried pace.',6,'ACTIVE','Goa, Sunkissed & Slow','DOM','Golden sands, cafés by the sea and Portuguese-era lanes at a gentle, unhurried pace.','India','[\"28 Nov 2024\",\"19 Dec 2024\",\"09 Jan 2025\"]',4000.00,'[\"https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1400&q=85\"]','[\"5 nights beach stays\",\"Daily breakfast + 2 dinners\",\"Private air-conditioned transfers\",\"Boat trip and local hosts\"]','[\"Panjim Latin quarter walk and sunset cruise\",\"North Goa beach morning at Candolim\",\"Old Goa churches and spice plantation\",\"Drive to Palolem, southern sands\",\"Boat trip and beach shack lunch\",\"Farewell brunch and departure\"]',41900.00,'Goa · Panjim · Palolem',4.8,42,9000.00,'[\"Beaches\",\"Slow travel\",\"Cafes\"]'),(8,145000.00,'River light, West End nights and a Test match at Lord\'s in one unhurried week.',8,'ACTIVE','London, the Seasoned Classic','INT','River light, West End nights and a Test match at Lord\'s in one unhurried week.','United Kingdom','[\"05 Dec 2024\",\"20 Mar 2025\",\"11 Jul 2025\"]',15000.00,'[\"https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1520986606214-8b456906c813?auto=format&fit=crop&w=1400&q=85\"]','[\"7 nights central hotels\",\"Test match tickets at Lord\'s\",\"West End show ticket\",\"Daily breakfast + 2 dinners\",\"Oyster card and local hosts\"]','[\"Arrive in London, South Bank river walk\",\"Westminster, the City and a Thames cruise\",\"Full day at Lord\'s - a Test match experience\",\"Windsor Castle and a countryside pub lunch\",\"West End show night and late supper\",\"Markets of London: Borough and Camden\",\"Free day with our handpicked museum map\",\"Farewell brunch and departure\"]',162000.00,'London · Windsor · Lord\'s',4.9,58,40000.00,'[\"Culture\",\"Cricket\",\"Theatre\"]'),(17,89900.00,'Skyline, golden dunes and old souks - a city of contrasts in one bright week.',6,'ACTIVE','Dubai & the Desert','INT','Skyline, golden dunes and old souks - a city of contrasts in one bright week.','United Arab Emirates','[\"10 Nov 2024\",\"08 Dec 2024\",\"12 Jan 2025\"]',8000.00,'[\"https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1518684079-3c830dcef090?auto=format&fit=crop&w=1400&q=85\"]','[\"5 nights hotels\",\"Desert safari and dinner\",\"Daily breakfast\",\"Private transfers and local hosts\"]','[\"Burj Khalifa sunset and fountain show\",\"Old Dubai souks and an abra ride\",\"Dune drive and desert camp evening\",\"Jumeirah beach morning and marina\",\"Al Fahidi district and coffee morning\",\"Grand mosque and farewell brunch\"]',99000.00,'Dubai · Al Marmoom · Jumeirah',4.7,34,22000.00,'[\"Skyline\",\"Desert\",\"Souks\"]'),(18,64900.00,'Long-tail boats, night markets and temple mornings across the Andaman Sea.',8,'ACTIVE','Thailand Island Hopper','INT','Long-tail boats, night markets and temple mornings across the Andaman Sea.','Thailand','[\"15 Nov 2024\",\"27 Dec 2024\",\"18 Jan 2025\"]',7000.00,'[\"https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1528181304800-259b08848526?auto=format&fit=crop&w=1400&q=85\"]','[\"7 nights resorts\",\"Ferries and speedboat transfers\",\"Daily breakfast\",\"Boat day and local guides\"]','[\"Phuket old town and street food evening\",\"Phi Phi island boat day\",\"Krabi railay beach and caves\",\"Koh Samui transfer day\",\"Fisherman\'s village night market\",\"Angthong marine park day trip\",\"Temple morning and spa afternoon\",\"Farewell beach brunch\"]',NULL,'Phuket · Krabi · Koh Samui',4.8,51,18000.00,'[\"Islands\",\"Night markets\",\"Temples\"]'),(19,139000.00,'Museums, lavender hills and slow village lunches through the heart of France.',9,'ACTIVE','Paris & Provence','INT','Museums, lavender hills and slow village lunches through the heart of France.','France','[\"28 Mar 2025\",\"09 May 2025\",\"20 Jun 2025\"]',14000.00,'[\"https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1400&q=85\"]','[\"8 nights boutique hotels\",\"Rail between cities\",\"Daily breakfast + 3 lunches\",\"Louvre tickets and local hosts\"]','[\"Paris arrival and Seine walk\",\"Louvre morning and Marais afternoon\",\"High-speed train to Avignon\",\"Pont du Gard and village lunch\",\"Lavender fields and hill towns\",\"Aix-en-Provence market morning\",\"Cézanne trail and café evening\",\"Châteauneuf-du-Pape wine afternoon\",\"Farewell breakfast and departure\"]',154000.00,'Paris · Avignon · Aix-en-Provence',4.8,44,38000.00,'[\"Museums\",\"Lavender\",\"Slow travel\"]'),(20,52900.00,'Turquoise water, coral reefs and the quietest beaches in India.',6,'ACTIVE','Andaman Islands Escape','DOM','Turquoise water, coral reefs and the quietest beaches in India.','India','[\"20 Nov 2024\",\"18 Dec 2024\",\"15 Jan 2025\"]',6000.00,'[\"https://images.unsplash.com/photo-1509233725247-49e657c54213?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1400&q=85\"]','[\"5 nights island stays\",\"Ferry transfers\",\"Daily breakfast\",\"Snorkelling and local guides\"]','[\"Port Blair arrival and Cellular Jail evening\",\"Ferry to Havelock and Radhanagar sunset\",\"Snorkelling at Elephant Beach\",\"Kalapathar sunrise and kayaking\",\"Neil Island and Natural Bridge\",\"Return to Port Blair and departure\"]',58900.00,'Port Blair · Havelock · Neil Island',4.9,67,14000.00,'[\"Islands\",\"Snorkelling\",\"Beaches\"]'),(21,82900.00,'Temple sunrises, villa pools and sunset cliffs made for two.',7,'ACTIVE','Bali Honeymoon Escape','INT','Temple sunrises, villa pools and sunset cliffs made for two.','Indonesia','[\"02 Feb 2025\",\"14 Feb 2025\",\"28 Feb 2025\"]',9000.00,'[\"https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1552733407-5d5c46c3bb3f?auto=format&fit=crop&w=1400&q=85\"]','[\"6 nights villas and resorts\",\"Private transfers\",\"Daily breakfast + 2 candlelight dinners\",\"Couples spa and local hosts\"]','[\"Ubud arrival and rice terrace sunset\",\"Tegalalang sunrise and temple morning\",\"Monkey forest and couples spa\",\"Seminyak beach club afternoon\",\"Uluwatu cliffs and Kecak fire dance\",\"Nusa Penida day trip\",\"Farewell breakfast and departure\"]',92900.00,'Ubud · Seminyak · Uluwatu',4.9,73,25000.00,'[\"Romance\",\"Villas\",\"Temples\"]'),(22,38900.00,'Wankhede roar, sea-facing dawns and street-food nights - a four-day cricket break.',4,'ACTIVE','Mumbai T20 Cricket Weekend','DOM','Wankhede roar, sea-facing dawns and street-food nights - a four-day cricket break.','India','[\"07 Dec 2024\",\"25 Jan 2025\",\"22 Mar 2025\"]',4000.00,'[\"https://images.unsplash.com/photo-1531415074968-036ba1b575da?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1524492412937-b28074a5d7da?auto=format&fit=crop&w=1400&q=85\"]','[\"3 nights city hotel\",\"Match tickets at Wankhede\",\"Daily breakfast + food walk\",\"Local host and transfers\"]','[\"Arrival and Marine Drive sunset walk\",\"Match day at Wankhede - full T20 experience\",\"Gateway of India and Colaba food crawl\",\"Farewell breakfast and departure\"]',44900.00,'Mumbai · Wankhede · Marine Drive',4.6,29,10000.00,'[\"Cricket\",\"Weekend\",\"Stadium\"]'),(28,110000.00,'Enjoy your Exotic Bachelors Trip Boom Boom',10,'ACTIVE','Bangkok','INT',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL),(29,23999.00,'Kedarnath Yatra is a sacred pilgrimage to the famous Kedarnath Temple, one of the twelve Jyotirlingas of Lord Shiva, located in the Himalayas of Uttarakhand. The tour offers a spiritual experience combined with breathtaking mountain views, scenic trekking, and visits to holy sites, making it an unforgettable journey for devotees and nature lovers alike.\n',4,'ACTIVE','Kedarnath Yatra','DEV',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL);
/*!40000 ALTER TABLE `tour` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_addon`
--

DROP TABLE IF EXISTS `tour_addon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_addon` (
  `addon_id` bigint NOT NULL AUTO_INCREMENT,
  `addon_name` varchar(150) NOT NULL,
  `description` text,
  `display_order` int DEFAULT NULL,
  `is_optional` bit(1) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `price_type` enum('PER_BOOKING','PER_PERSON','PER_ROOM') NOT NULL,
  `status` bit(1) NOT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`addon_id`),
  KEY `FKqaawi39yew35ncpl2grv1ha6g` (`tour_id`),
  CONSTRAINT `FKqaawi39yew35ncpl2grv1ha6g` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_addon`
--

LOCK TABLES `tour_addon` WRITE;
/*!40000 ALTER TABLE `tour_addon` DISABLE KEYS */;
INSERT INTO `tour_addon` VALUES (2,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',1),(3,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',2),(4,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',3),(5,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',4),(6,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',5),(7,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',6),(8,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',7),(9,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',8),(10,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',17),(11,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',18),(12,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',19),(13,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',20),(14,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',21),(15,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',22),(16,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',28),(17,'Travel Insurance','Comprehensive travel insurance for the duration of the trip.',1,_binary '',1500.00,'PER_PERSON',_binary '',29);
/*!40000 ALTER TABLE `tour_addon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_category`
--

DROP TABLE IF EXISTS `tour_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_category` (
  `tour_id` bigint NOT NULL,
  `category_id` bigint NOT NULL,
  PRIMARY KEY (`tour_id`,`category_id`),
  KEY `FKdy6eldt453mi4tyv4gc1np7ag` (`category_id`),
  CONSTRAINT `FKdy6eldt453mi4tyv4gc1np7ag` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE CASCADE,
  CONSTRAINT `FKk99gxgifscof3vuga1hvannk2` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_category`
--

LOCK TABLES `tour_category` WRITE;
/*!40000 ALTER TABLE `tour_category` DISABLE KEYS */;
INSERT INTO `tour_category` VALUES (1,1),(3,1),(6,1),(7,1),(20,1),(22,1),(29,1),(4,2),(8,2),(17,2),(18,2),(19,2),(21,2),(28,2),(5,3),(8,3),(22,3),(8,5),(29,7),(1,8),(6,8),(7,8),(20,8),(22,8);
/*!40000 ALTER TABLE `tour_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_content`
--

DROP TABLE IF EXISTS `tour_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_content` (
  `tour_content_id` bigint NOT NULL AUTO_INCREMENT,
  `content_text` text NOT NULL,
  `content_type` enum('DOS_DONTS','PASSPORT_VISA','TERMS_CONDITIONS','WEATHER') NOT NULL,
  `language_code` varchar(10) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`tour_content_id`),
  KEY `FKbrpyfe787p7piwqrxt0qlcyva` (`tour_id`),
  CONSTRAINT `FKbrpyfe787p7piwqrxt0qlcyva` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_content`
--

LOCK TABLES `tour_content` WRITE;
/*!40000 ALTER TABLE `tour_content` DISABLE KEYS */;
/*!40000 ALTER TABLE `tour_content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_cost`
--

DROP TABLE IF EXISTS `tour_cost`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_cost` (
  `cost_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `base_price` decimal(12,2) NOT NULL,
  `child_with_bed_cost` decimal(12,2) DEFAULT NULL,
  `child_without_bed_cost` decimal(12,2) DEFAULT NULL,
  `extra_person_cost` decimal(12,2) DEFAULT NULL,
  `single_person_cost` decimal(12,2) DEFAULT NULL,
  `valid_from` date NOT NULL,
  `valid_to` date NOT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`cost_id`),
  KEY `FK65xgv1yxuyj2479iuwg38olhw` (`tour_id`),
  CONSTRAINT `FK65xgv1yxuyj2479iuwg38olhw` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_cost`
--

LOCK TABLES `tour_cost` WRITE;
/*!40000 ALTER TABLE `tour_cost` DISABLE KEYS */;
/*!40000 ALTER TABLE `tour_cost` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_details`
--

DROP TABLE IF EXISTS `tour_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_details` (
  `tour_details_id` bigint NOT NULL AUTO_INCREMENT,
  `add_ons` text,
  `cost_excludes` text,
  `cost_includes` text,
  `cost_notes` text,
  `departure_dates` text,
  `do_list` text,
  `dont_list` text,
  `gallery` text,
  `itinerary` text,
  `journey_plan` text,
  `meal_details` text,
  `passport_visa` text,
  `stay_details` text,
  `terms` text,
  `video_url` varchar(500) DEFAULT NULL,
  `weather_info` text,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`tour_details_id`),
  KEY `FK27g89or55p5ovep89frmbyva2` (`tour_id`),
  CONSTRAINT `FK27g89or55p5ovep89frmbyva2` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_details`
--

LOCK TABLES `tour_details` WRITE;
/*!40000 ALTER TABLE `tour_details` DISABLE KEYS */;
INSERT INTO `tour_details` VALUES (1,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"7 nights boutique hotels\",\"Daily breakfast + 3 local meals\",\"Private air-conditioned transfers\",\"Local hosts and entrance fees\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"12 Oct 2024\",\"02 Nov 2024\",\"21 Dec 2024\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=1400&q=85\"]','[\"Arrive in Jaipur, pink city welcome walk\",\"Amber Fort at golden hour · block printing studio\",\"Drive to Jodhpur via Pushkar\",\"Blue City rooftops and Mehrangarh Fort\",\"Jodhpur to Udaipur, a desert-to-lake day\",\"Udaipur palaces, boat ride and local kitchen\",\"A final lakeside breakfast and farewell\"]','[\"Arrive in Jaipur, pink city welcome walk\",\"Amber Fort at golden hour · block printing studio\",\"Drive to Jodhpur via Pushkar\",\"Blue City rooftops and Mehrangarh Fort\",\"Jodhpur to Udaipur, a desert-to-lake day\",\"Udaipur palaces, boat ride and local kitchen\",\"A final lakeside breakfast and farewell\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"No visa required for Indian nationals\",\"Carry a valid Government-issued photo ID\",\"Foreign nationals must carry a valid tourist visa\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Oct - Mar: crisp and sunny, ideal for touring\",\"Apr - Jun: warm afternoons, plan mornings outdoors\",\"Jul - Sep: monsoon showers, lush green landscapes\"]',1),(2,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"9 nights design hotels\",\"Rail pass and airport transfers\",\"Breakfast daily\",\"Tea ceremony and food walk\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"06 Oct 2024\",\"03 Nov 2024\",\"18 Mar 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1528164344705-47542687000d?auto=format&fit=crop&w=1400&q=85\"]','[\"Tokyo neighbourhoods and teamLab evening\",\"Tsukiji market breakfast and Shibuya\",\"Bullet train to Kyoto\",\"Fushimi Inari before the crowds\",\"Nara deer park and a countryside lunch\",\"Arashiyama bamboo grove and tea ceremony\",\"Kyoto free day, with our handpicked map\"]','[\"Tokyo neighbourhoods and teamLab evening\",\"Tsukiji market breakfast and Shibuya\",\"Bullet train to Kyoto\",\"Fushimi Inari before the crowds\",\"Nara deer park and a countryside lunch\",\"Arashiyama bamboo grove and tea ceremony\",\"Kyoto free day, with our handpicked map\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"Passport valid for at least 6 months beyond travel dates\",\"At least 2 blank pages for visa stamps\",\"Tourist visa / e-visa required - our team assists with the process\",\"Travel insurance with medical cover is mandatory\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Best in spring and autumn, with pleasant daytime highs\",\"Summer months are sunny; pack light layers\",\"Winter departures are cold - bring warm layers\"]',2),(3,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"6 nights handpicked stays\",\"1 night private houseboat\",\"All breakfasts and 4 dinners\",\"Experiences as listed\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"19 Jul 2024\",\"16 Aug 2024\",\"11 Jan 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1593693397690-362cb9666fc2?auto=format&fit=crop&w=1400&q=85\"]','[\"Fort Kochi galleries and spice market\",\"Munnar tea country at dawn\",\"Tea estate picnic and sunset\",\"Drive through cardamom hills\",\"Houseboat check-in and village canoe ride\",\"Backwater sunrise and Alleppey kitchen\",\"Farewell brunch by the sea\"]','[\"Fort Kochi galleries and spice market\",\"Munnar tea country at dawn\",\"Tea estate picnic and sunset\",\"Drive through cardamom hills\",\"Houseboat check-in and village canoe ride\",\"Backwater sunrise and Alleppey kitchen\",\"Farewell brunch by the sea\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"No visa required for Indian nationals\",\"Carry a valid Government-issued photo ID\",\"Foreign nationals must carry a valid tourist visa\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Oct - Mar: crisp and sunny, ideal for touring\",\"Apr - Jun: warm afternoons, plan mornings outdoors\",\"Jul - Sep: monsoon showers, lush green landscapes\"]',3),(4,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"8 nights hotels\",\"Domestic flights and transfers\",\"7 breakfasts + food walk\",\"Cooking class and local guides\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"14 Sep 2024\",\"09 Nov 2024\",\"08 Feb 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1528181304800-259b08848526?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=1400&q=85\"]','[\"Hanoi old quarter food walk\",\"Ninh Binh limestone valleys\",\"Fly to Da Nang and lantern-lit Hoi An\",\"Cycling through rice fields\",\"Beach morning and cooking class\",\"Saigon cafés and hidden courtyards\",\"Mekong Delta day trip\"]','[\"Hanoi old quarter food walk\",\"Ninh Binh limestone valleys\",\"Fly to Da Nang and lantern-lit Hoi An\",\"Cycling through rice fields\",\"Beach morning and cooking class\",\"Saigon cafés and hidden courtyards\",\"Mekong Delta day trip\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"Passport valid for at least 6 months beyond travel dates\",\"At least 2 blank pages for visa stamps\",\"Tourist visa / e-visa required - our team assists with the process\",\"Travel insurance with medical cover is mandatory\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Best in spring and autumn, with pleasant daytime highs\",\"Summer months are sunny; pack light layers\",\"Winter departures are cold - bring warm layers\"]',4),(5,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"7 nights central hotels\",\"Rail between cities\",\"Concert tickets and local hosts\",\"Breakfast daily\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"29 Nov 2024\",\"06 Dec 2024\",\"13 Dec 2024\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1519677100203-a0e668c92439?auto=format&fit=crop&w=1400&q=85\"]','[\"Vienna welcome and evening concert\",\"Christmas markets and coffeehouses\",\"Train to Prague\",\"Old Town walk and market evening\",\"Budapest thermal baths\",\"Danube lights and ruin-bar supper\",\"A slow final morning\"]','[\"Vienna welcome and evening concert\",\"Christmas markets and coffeehouses\",\"Train to Prague\",\"Old Town walk and market evening\",\"Budapest thermal baths\",\"Danube lights and ruin-bar supper\",\"A slow final morning\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"Passport valid for at least 6 months beyond travel dates\",\"At least 2 blank pages for visa stamps\",\"Tourist visa / e-visa required - our team assists with the process\",\"Travel insurance with medical cover is mandatory\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Best in spring and autumn, with pleasant daytime highs\",\"Summer months are sunny; pack light layers\",\"Winter departures are cold - bring warm layers\"]',5),(6,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"5 nights stays\",\"4x4 mountain transport\",\"Breakfast + dinner\",\"Permits and local guide\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"05 Sep 2024\",\"19 Sep 2024\",\"03 Oct 2024\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1548013146-72479768bada?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=1400&q=85\"]','[\"Leh acclimatisation and sunset market\",\"Khardung La to Nubra Valley\",\"Sand dunes and monastery morning\",\"Drive to Pangong Lake\",\"Lake sunrise and return to Leh\",\"Farewell café and departure\"]','[\"Leh acclimatisation and sunset market\",\"Khardung La to Nubra Valley\",\"Sand dunes and monastery morning\",\"Drive to Pangong Lake\",\"Lake sunrise and return to Leh\",\"Farewell café and departure\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"No visa required for Indian nationals\",\"Carry a valid Government-issued photo ID\",\"Foreign nationals must carry a valid tourist visa\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Oct - Mar: crisp and sunny, ideal for touring\",\"Apr - Jun: warm afternoons, plan mornings outdoors\",\"Jul - Sep: monsoon showers, lush green landscapes\"]',6),(7,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"5 nights beach stays\",\"Daily breakfast + 2 dinners\",\"Private air-conditioned transfers\",\"Boat trip and local hosts\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"28 Nov 2024\",\"19 Dec 2024\",\"09 Jan 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1400&q=85\"]','[\"Panjim Latin quarter walk and sunset cruise\",\"North Goa beach morning at Candolim\",\"Old Goa churches and spice plantation\",\"Drive to Palolem, southern sands\",\"Boat trip and beach shack lunch\",\"Farewell brunch and departure\"]','[\"Panjim Latin quarter walk and sunset cruise\",\"North Goa beach morning at Candolim\",\"Old Goa churches and spice plantation\",\"Drive to Palolem, southern sands\",\"Boat trip and beach shack lunch\",\"Farewell brunch and departure\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"No visa required for Indian nationals\",\"Carry a valid Government-issued photo ID\",\"Foreign nationals must carry a valid tourist visa\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Oct - Mar: crisp and sunny, ideal for touring\",\"Apr - Jun: warm afternoons, plan mornings outdoors\",\"Jul - Sep: monsoon showers, lush green landscapes\"]',7),(8,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"7 nights central hotels\",\"Test match tickets at Lord\'s\",\"West End show ticket\",\"Daily breakfast + 2 dinners\",\"Oyster card and local hosts\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"05 Dec 2024\",\"20 Mar 2025\",\"11 Jul 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1520986606214-8b456906c813?auto=format&fit=crop&w=1400&q=85\"]','[\"Arrive in London, South Bank river walk\",\"Westminster, the City and a Thames cruise\",\"Full day at Lord\'s - a Test match experience\",\"Windsor Castle and a countryside pub lunch\",\"West End show night and late supper\",\"Markets of London: Borough and Camden\",\"Free day with our handpicked museum map\",\"Farewell brunch and departure\"]','[\"Arrive in London, South Bank river walk\",\"Westminster, the City and a Thames cruise\",\"Full day at Lord\'s - a Test match experience\",\"Windsor Castle and a countryside pub lunch\",\"West End show night and late supper\",\"Markets of London: Borough and Camden\",\"Free day with our handpicked museum map\",\"Farewell brunch and departure\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"Passport valid for at least 6 months beyond travel dates\",\"At least 2 blank pages for visa stamps\",\"Tourist visa / e-visa required - our team assists with the process\",\"Travel insurance with medical cover is mandatory\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Best in spring and autumn, with pleasant daytime highs\",\"Summer months are sunny; pack light layers\",\"Winter departures are cold - bring warm layers\"]',8),(25,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"5 nights hotels\",\"Desert safari and dinner\",\"Daily breakfast\",\"Private transfers and local hosts\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"10 Nov 2024\",\"08 Dec 2024\",\"12 Jan 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1518684079-3c830dcef090?auto=format&fit=crop&w=1400&q=85\"]','[\"Burj Khalifa sunset and fountain show\",\"Old Dubai souks and an abra ride\",\"Dune drive and desert camp evening\",\"Jumeirah beach morning and marina\",\"Al Fahidi district and coffee morning\",\"Grand mosque and farewell brunch\"]','[\"Burj Khalifa sunset and fountain show\",\"Old Dubai souks and an abra ride\",\"Dune drive and desert camp evening\",\"Jumeirah beach morning and marina\",\"Al Fahidi district and coffee morning\",\"Grand mosque and farewell brunch\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"Passport valid for at least 6 months beyond travel dates\",\"At least 2 blank pages for visa stamps\",\"Tourist visa / e-visa required - our team assists with the process\",\"Travel insurance with medical cover is mandatory\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Best in spring and autumn, with pleasant daytime highs\",\"Summer months are sunny; pack light layers\",\"Winter departures are cold - bring warm layers\"]',17),(26,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"7 nights resorts\",\"Ferries and speedboat transfers\",\"Daily breakfast\",\"Boat day and local guides\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"15 Nov 2024\",\"27 Dec 2024\",\"18 Jan 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1528181304800-259b08848526?auto=format&fit=crop&w=1400&q=85\"]','[\"Phuket old town and street food evening\",\"Phi Phi island boat day\",\"Krabi railay beach and caves\",\"Koh Samui transfer day\",\"Fisherman\'s village night market\",\"Angthong marine park day trip\",\"Temple morning and spa afternoon\",\"Farewell beach brunch\"]','[\"Phuket old town and street food evening\",\"Phi Phi island boat day\",\"Krabi railay beach and caves\",\"Koh Samui transfer day\",\"Fisherman\'s village night market\",\"Angthong marine park day trip\",\"Temple morning and spa afternoon\",\"Farewell beach brunch\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"Passport valid for at least 6 months beyond travel dates\",\"At least 2 blank pages for visa stamps\",\"Tourist visa / e-visa required - our team assists with the process\",\"Travel insurance with medical cover is mandatory\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Best in spring and autumn, with pleasant daytime highs\",\"Summer months are sunny; pack light layers\",\"Winter departures are cold - bring warm layers\"]',18),(27,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"8 nights boutique hotels\",\"Rail between cities\",\"Daily breakfast + 3 lunches\",\"Louvre tickets and local hosts\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"28 Mar 2025\",\"09 May 2025\",\"20 Jun 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1400&q=85\"]','[\"Paris arrival and Seine walk\",\"Louvre morning and Marais afternoon\",\"High-speed train to Avignon\",\"Pont du Gard and village lunch\",\"Lavender fields and hill towns\",\"Aix-en-Provence market morning\",\"Cézanne trail and café evening\",\"Châteauneuf-du-Pape wine afternoon\",\"Farewell breakfast and departure\"]','[\"Paris arrival and Seine walk\",\"Louvre morning and Marais afternoon\",\"High-speed train to Avignon\",\"Pont du Gard and village lunch\",\"Lavender fields and hill towns\",\"Aix-en-Provence market morning\",\"Cézanne trail and café evening\",\"Châteauneuf-du-Pape wine afternoon\",\"Farewell breakfast and departure\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"Passport valid for at least 6 months beyond travel dates\",\"At least 2 blank pages for visa stamps\",\"Tourist visa / e-visa required - our team assists with the process\",\"Travel insurance with medical cover is mandatory\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Best in spring and autumn, with pleasant daytime highs\",\"Summer months are sunny; pack light layers\",\"Winter departures are cold - bring warm layers\"]',19),(28,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"5 nights island stays\",\"Ferry transfers\",\"Daily breakfast\",\"Snorkelling and local guides\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"20 Nov 2024\",\"18 Dec 2024\",\"15 Jan 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1509233725247-49e657c54213?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1400&q=85\"]','[\"Port Blair arrival and Cellular Jail evening\",\"Ferry to Havelock and Radhanagar sunset\",\"Snorkelling at Elephant Beach\",\"Kalapathar sunrise and kayaking\",\"Neil Island and Natural Bridge\",\"Return to Port Blair and departure\"]','[\"Port Blair arrival and Cellular Jail evening\",\"Ferry to Havelock and Radhanagar sunset\",\"Snorkelling at Elephant Beach\",\"Kalapathar sunrise and kayaking\",\"Neil Island and Natural Bridge\",\"Return to Port Blair and departure\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"No visa required for Indian nationals\",\"Carry a valid Government-issued photo ID\",\"Foreign nationals must carry a valid tourist visa\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Oct - Mar: crisp and sunny, ideal for touring\",\"Apr - Jun: warm afternoons, plan mornings outdoors\",\"Jul - Sep: monsoon showers, lush green landscapes\"]',20),(29,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"6 nights villas and resorts\",\"Private transfers\",\"Daily breakfast + 2 candlelight dinners\",\"Couples spa and local hosts\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"02 Feb 2025\",\"14 Feb 2025\",\"28 Feb 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1552733407-5d5c46c3bb3f?auto=format&fit=crop&w=1400&q=85\"]','[\"Ubud arrival and rice terrace sunset\",\"Tegalalang sunrise and temple morning\",\"Monkey forest and couples spa\",\"Seminyak beach club afternoon\",\"Uluwatu cliffs and Kecak fire dance\",\"Nusa Penida day trip\",\"Farewell breakfast and departure\"]','[\"Ubud arrival and rice terrace sunset\",\"Tegalalang sunrise and temple morning\",\"Monkey forest and couples spa\",\"Seminyak beach club afternoon\",\"Uluwatu cliffs and Kecak fire dance\",\"Nusa Penida day trip\",\"Farewell breakfast and departure\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"Passport valid for at least 6 months beyond travel dates\",\"At least 2 blank pages for visa stamps\",\"Tourist visa / e-visa required - our team assists with the process\",\"Travel insurance with medical cover is mandatory\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Best in spring and autumn, with pleasant daytime highs\",\"Summer months are sunny; pack light layers\",\"Winter departures are cold - bring warm layers\"]',21),(30,'[\"Private airport transfers (one-way or return)\",\"Extra nights before or after the tour\",\"Upgrade to premium room category\",\"Travel insurance and medical cover\",\"Personal photographer / local guide for a day\"]','[\"International or domestic flights\",\"Travel insurance\",\"Personal expenses, drinks and tips\",\"Visa fees and passport formalities\"]','[\"3 nights city hotel\",\"Match tickets at Wankhede\",\"Daily breakfast + food walk\",\"Local host and transfers\"]','Prices are per person on twin-sharing basis and include all taxes. Flexible easy-payment plans are available on request.','[\"07 Dec 2024\",\"25 Jan 2025\",\"22 Mar 2025\"]','[\"Carry a refillable water bottle and sunscreen\",\"Dress modestly when visiting temples and holy sites\",\"Keep small change for local markets and tips\",\"Listen to your local host - they know the hidden gems\"]','[\"Avoid single-use plastics on the road\",\"Do not photograph locals without asking\",\"Skip the crowds - mornings are the best time to explore\",\"Don\'t overpack - comfortable shoes matter most\"]','[\"https://images.unsplash.com/photo-1531415074968-036ba1b575da?auto=format&fit=crop&w=1400&q=85\",\"https://images.unsplash.com/photo-1524492412937-b28074a5d7da?auto=format&fit=crop&w=1400&q=85\"]','[\"Arrival and Marine Drive sunset walk\",\"Match day at Wankhede - full T20 experience\",\"Gateway of India and Colaba food crawl\",\"Farewell breakfast and departure\"]','[\"Arrival and Marine Drive sunset walk\",\"Match day at Wankhede - full T20 experience\",\"Gateway of India and Colaba food crawl\",\"Farewell breakfast and departure\"]','[\"Daily breakfast included throughout the tour\",\"Select local dinners and tasting experiences\",\"Vegetarian, vegan and Jain meals readily available\",\"Special celebration dinners for anniversaries and birthdays\"]','[\"No visa required for Indian nationals\",\"Carry a valid Government-issued photo ID\",\"Foreign nationals must carry a valid tourist visa\"]','[\"Handpicked boutique hotels and heritage stays\",\"Centrally located for easy exploring\",\"Daily housekeeping and Wi-Fi included\",\"Upgrade to premium or lake-view rooms on request\"]','[\"Booking is confirmed upon receipt of full or part payment\",\"Free cancellation up to 30 days before departure\",\"50% refund between 15 and 30 days before departure\",\"No refund within 15 days of departure\",\"Prices are subject to change until full payment is received\",\"Travel insurance is strongly recommended\"]','','[\"Oct - Mar: crisp and sunny, ideal for touring\",\"Apr - Jun: warm afternoons, plan mornings outdoors\",\"Jul - Sep: monsoon showers, lush green landscapes\"]',22);
/*!40000 ALTER TABLE `tour_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_media`
--

DROP TABLE IF EXISTS `tour_media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_media` (
  `media_id` bigint NOT NULL AUTO_INCREMENT,
  `display_order` int DEFAULT NULL,
  `file_path` varchar(500) NOT NULL,
  `media_type` enum('AUDIO','DOCUMENT','IMAGE','MAP','PDF','VIDEO') NOT NULL,
  `mime_type` varchar(100) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `tab_context` enum('BROCHURE','GALLERY','MAP','VIDEO') DEFAULT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`media_id`),
  KEY `FKtrh5da8ihlear0m2phcdc5tdo` (`tour_id`),
  CONSTRAINT `FKtrh5da8ihlear0m2phcdc5tdo` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_media`
--

LOCK TABLES `tour_media` WRITE;
/*!40000 ALTER TABLE `tour_media` DISABLE KEYS */;
INSERT INTO `tour_media` VALUES (2,0,'https://images.pexels.com/photos/12931430/pexels-photo-12931430.jpeg','IMAGE',NULL,_binary '','GALLERY',1),(3,1,'https://www.pexels.com/download/video/30555669/','VIDEO',NULL,_binary '','VIDEO',1),(4,0,'https://images.pexels.com/photos/17258243/pexels-photo-17258243.jpeg','IMAGE','jpeg',_binary '','GALLERY',2),(5,0,'https://images.pexels.com/photos/32276362/pexels-photo-32276362.jpeg','IMAGE','jpeg',_binary '','GALLERY',2),(6,0,'https://images.pexels.com/photos/17928231/pexels-photo-17928231.jpeg','IMAGE','jpeg',_binary '','GALLERY',3),(7,0,'https://images.pexels.com/photos/14172615/pexels-photo-14172615.jpeg','IMAGE','jpeg',_binary '','GALLERY',3),(8,0,'https://media.istockphoto.com/id/1153561755/photo/beautiful-landscape-halong-bay-view-from-adove-the-bo-hon-island-halong-bay-vietnam.jpg?b=1&s=612x612&w=0&k=20&c=k0ye3VHf6PRQCu67ZNFNznRLjJ9BAQVGisl-FnHnVhQ=','IMAGE','jpeg',_binary '','GALLERY',4),(9,0,'https://media.istockphoto.com/id/2166041064/photo/aerial-view-of-hoi-an-ancient-town-at-twilight-vietnam.jpg?b=1&s=612x612&w=0&k=20&c=_37aWpMig_sjM007vs-70lcnGG9n3CB-Mkx1DTq7N4I=','IMAGE','jpeg',_binary '','GALLERY',4),(10,0,'https://media.istockphoto.com/id/1764863971/photo/holiday-flags.jpg?b=1&s=612x612&w=0&k=20&c=KT12nfJg_jAIQG3TqSl9Kq0NnRn6_mYqlzJopdQGIRc=','IMAGE','jpeg',_binary '','GALLERY',5),(11,0,'https://images.pexels.com/photos/30552674/pexels-photo-30552674.jpeg','IMAGE','jpeg',_binary '','GALLERY',5),(12,0,'https://images.pexels.com/photos/27593915/pexels-photo-27593915.jpeg','IMAGE','jpeg',_binary '','GALLERY',6),(13,0,'https://images.pexels.com/photos/5125855/pexels-photo-5125855.jpeg','IMAGE','jpeg',_binary '','GALLERY',6),(14,0,'https://images.pexels.com/photos/14665858/pexels-photo-14665858.jpeg','IMAGE','jpeg',_binary '','GALLERY',7),(15,0,'https://images.pexels.com/photos/20717175/pexels-photo-20717175.jpeg','IMAGE','jpeg',_binary '','GALLERY',7),(16,0,'https://images.pexels.com/photos/16771428/pexels-photo-16771428.png','IMAGE','jpeg',_binary '','GALLERY',8),(17,0,'https://images.pexels.com/photos/24739980/pexels-photo-24739980.jpeg','IMAGE','jpeg',_binary '','GALLERY',8),(18,0,'https://images.pexels.com/photos/823696/pexels-photo-823696.jpeg','IMAGE','jpeg',_binary '','GALLERY',17),(19,0,'https://images.pexels.com/photos/28720826/pexels-photo-28720826.jpeg','IMAGE','jpeg',_binary '','GALLERY',17),(20,0,'https://images.pexels.com/photos/3992155/pexels-photo-3992155.jpeg','IMAGE','jpeg',_binary '','GALLERY',18),(21,0,'https://media.istockphoto.com/id/507913346/photo/wat-arun-temple-at-sunset-in-bangkok-thailand.jpg?b=1&s=612x612&w=0&k=20&c=G_yeHldlCtLcsjmYpiefF85YWHcq_oav8JA5q2ErFlE=','IMAGE','jpeg',_binary '','GALLERY',18),(22,0,'https://images.pexels.com/photos/23938418/pexels-photo-23938418.jpeg','IMAGE','jpeg',_binary '','GALLERY',19),(23,0,'https://images.pexels.com/photos/30173039/pexels-photo-30173039.jpeg','IMAGE','jpeg',_binary '','GALLERY',19),(24,0,'https://media.istockphoto.com/id/1185953092/photo/the-main-attraction-of-paris-and-all-of-europe-is-the-eiffel-tower-in-the-rays-of-the-setting.jpg?b=1&s=612x612&w=0&k=20&c=Zbku2ZmZvxljs5EyWJOsW91rYLFtpZVi_klejWdE2RA=','IMAGE','jpeg',_binary '','GALLERY',19),(25,0,'https://images.pexels.com/photos/31833429/pexels-photo-31833429.jpeg','IMAGE','jpeg',_binary '','GALLERY',20),(26,0,'https://images.pexels.com/photos/32159232/pexels-photo-32159232.jpeg','IMAGE','jpeg',_binary '','GALLERY',20),(27,0,'https://images.pexels.com/photos/37761998/pexels-photo-37761998.jpeg','IMAGE',NULL,_binary '','GALLERY',21),(28,0,'https://media.istockphoto.com/id/1159885788/photo/kisses-to-build-a-summer-dream-on.jpg?b=1&s=612x612&w=0&k=20&c=VueM1O2_oTLCFXmUYeSggVHFnlOwQE1JonSvBLS6eJ4=','IMAGE','jpeg',_binary '','GALLERY',21),(29,0,'https://media.istockphoto.com/id/1226342325/photo/lovers-at-the-waterfall-rear-view-couple-admiring-a-beautiful-waterfall-in-indonesia-couple.jpg?b=1&s=612x612&w=0&k=20&c=ba8vWVuVy2OUgY0yXCpuVMa40NdYS-e1xBHMuTKViCg=','IMAGE','jpeg',_binary '','GALLERY',21),(30,0,'https://images.pexels.com/photos/19764951/pexels-photo-19764951.jpeg','IMAGE','jpeg',_binary '','GALLERY',22),(31,0,'https://images.pexels.com/photos/38179301/pexels-photo-38179301.jpeg','IMAGE','jpeg',_binary '','GALLERY',22),(32,0,'https://images.pexels.com/photos/32272882/pexels-photo-32272882.jpeg','IMAGE','jpeg',_binary '','GALLERY',28),(33,0,'https://images.pexels.com/photos/2611495/pexels-photo-2611495.jpeg','IMAGE','jpeg',_binary '','GALLERY',28),(34,0,'https://media.istockphoto.com/id/539105384/photo/kedarnath-in-india.jpg?b=1&s=612x612&w=0&k=20&c=b022I7LBVwivpeyWhwG1BZdaDxshWInq-uaRpL6CGPc=','IMAGE','jpeg',_binary '','GALLERY',29),(35,0,'https://media.istockphoto.com/id/2227491349/photo/kaalbhairava-temple-patron-protector-of-kedarnath-offering-a-picturesque-view-in-kedarnath.jpg?b=1&s=612x612&w=0&k=20&c=BUtKRTzca5mLiW8R1aWH0Q4Uw30ILNz-2GlZAbJhlaM=','IMAGE','jpeg',_binary '','GALLERY',29),(36,0,'https://images.pexels.com/photos/11974834/pexels-photo-11974834.jpeg','IMAGE','jpeg',_binary '','GALLERY',29),(37,0,'https://www.pexels.com/download/video/17383159/','VIDEO',NULL,_binary '','VIDEO',29);
/*!40000 ALTER TABLE `tour_media` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_product`
--

DROP TABLE IF EXISTS `tour_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_product` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `base_cost` decimal(10,2) NOT NULL,
  `description` text,
  `duration_days` int NOT NULL,
  `duration_nights` int NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `sort_order` int DEFAULT NULL,
  `sub_sector_id` bigint NOT NULL,
  `end_date` date DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `tour_code` varchar(20) DEFAULT NULL,
  `tour_id` bigint DEFAULT NULL,
  PRIMARY KEY (`product_id`),
  KEY `FK2qb5toxx5hcin3x3o8lfg8uss` (`sub_sector_id`),
  KEY `FK8l5pa3foahvtpvxaeybeeyadn` (`tour_id`),
  CONSTRAINT `FK2qb5toxx5hcin3x3o8lfg8uss` FOREIGN KEY (`sub_sector_id`) REFERENCES `sub_sector` (`sub_sector_id`),
  CONSTRAINT `FK8l5pa3foahvtpvxaeybeeyadn` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`),
  CONSTRAINT `tour_product_chk_1` CHECK ((`duration_days` >= 1)),
  CONSTRAINT `tour_product_chk_2` CHECK ((`duration_nights` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_product`
--

LOCK TABLES `tour_product` WRITE;
/*!40000 ALTER TABLE `tour_product` DISABLE KEYS */;
INSERT INTO `tour_product` VALUES (1,_binary '',64900.00,'Srinagar, Gulmarg and the Doodhpathri meadows.',10,9,'https://images.unsplash.com/photo-1589375020837-1c60a6bb9fe3?auto=format&fit=crop&w=900&q=80','Kashmir Unveiled',1,1,'2024-10-21','2024-10-12','KSM-01',NULL),(2,_binary '',44900.00,'Nights on the dal lake and Mughal gardens.',7,6,'https://images.unsplash.com/photo-1596618739320-b2b6f93cf75a?auto=format&fit=crop&w=900&q=80','Srinagar Houseboat Escape',2,1,'2024-11-08','2024-11-02','KSM-02',NULL),(3,_binary '',32900.00,'Snowbound slopes and gondola rides.',5,4,'https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=900&q=80','Gulmarg Winter Retreat',3,1,'2024-12-25','2024-12-21','KSM-03',NULL),(4,_binary '',38900.00,'Pine forests, rivers and high meadows.',6,5,'https://images.unsplash.com/photo-1506947411487-a56738267384?auto=format&fit=crop&w=900&q=80','Pahalgam & Sonamarg',4,1,'2025-01-09','2025-01-04','KSM-04',NULL),(5,_binary '',78900.00,'The full valley loop with houseboat finale.',12,11,'https://images.unsplash.com/photo-1600799658076-909f731f697d?auto=format&fit=crop&w=900&q=80','Grand Kashmir Circuit',5,1,'2025-01-29','2025-01-18','KSM-05',NULL),(6,_binary '',57900.00,'Candlelit dinners and private shikara rides.',9,8,'https://images.unsplash.com/photo-1469474968028-56623f02e42e?auto=format&fit=crop&w=900&q=80','Kashmir Honeymoon Special',6,1,'2025-02-16','2025-02-08','KSM-06',NULL),(7,_binary '',49900.00,'Golden chinar season across the valley.',8,7,'https://images.unsplash.com/photo-1509248625317-328ae317b4f5?auto=format&fit=crop&w=900&q=80','Autumn Chinar Trail',7,1,'2024-11-08','2024-11-01','KSM-07',NULL),(8,_binary '',72900.00,'Kashmir to Ladakh via the high passes.',11,10,'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=900&q=80','Leh Overland Return',8,1,'2025-03-04','2025-02-22','KSM-08',NULL),(9,_binary '',189000.00,'Paris, Rome and the Alps in one sweep.',14,13,'https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=900&q=80','European Grand Tour',1,7,'2025-04-18','2025-04-05','EUR-01',NULL),(10,_binary '',109900.00,'Three luminous capitals by rail.',8,7,'https://images.unsplash.com/photo-1544982503-9f984c14501a?auto=format&fit=crop&w=900&q=80','Vienna Prague Budapest',2,7,'2024-12-13','2024-12-06','EUR-02',NULL),(11,_binary '',134500.00,'Glacier trains and alpine villages.',9,8,'https://images.unsplash.com/photo-1530122037265-a5f1f91d3b99?auto=format&fit=crop&w=900&q=80','Swiss Alps Escape',3,7,'2025-06-22','2025-06-14','EUR-03',NULL),(12,_binary '',127000.00,'Madrid, Seville and the coasts of Spain.',10,9,'https://images.unsplash.com/photo-1583422409516-2895a77efded?auto=format&fit=crop&w=900&q=80','Iberian Sun Trail',4,7,'2025-05-12','2025-05-03','EUR-04',NULL),(13,_binary '',149900.00,'Lapland snow villages and aurora nights.',7,6,'https://images.unsplash.com/photo-1483347756197-71ef80e95f73?auto=format&fit=crop&w=900&q=80','Northern Lights Winter',5,7,'2025-01-21','2025-01-15','EUR-05',NULL),(14,_binary '',118900.00,'Santorini, Naxos and Aegean ferries.',11,10,'https://images.unsplash.com/photo-1533105079780-92b9be482077?auto=format&fit=crop&w=900&q=80','Greek Islands Slow',6,7,'2025-06-30','2025-06-20','EUR-06',NULL),(15,_binary '',35900.00,'Golden sands, caf├®s by the sea and Portuguese-era lanes at a gentle, unhurried pace.',6,5,'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=900&q=80','Goa, Sunkissed & Slow',1,5,'2024-12-10','2024-12-05','GOA-01',NULL);
/*!40000 ALTER TABLE `tour_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_schedule`
--

DROP TABLE IF EXISTS `tour_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_schedule` (
  `schedule_id` bigint NOT NULL AUTO_INCREMENT,
  `available_seats` int NOT NULL,
  `departure_date` date NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `return_date` date NOT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`schedule_id`),
  KEY `FK4pywqoa7g8xmv2yv4816wonrs` (`tour_id`),
  CONSTRAINT `FK4pywqoa7g8xmv2yv4816wonrs` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE,
  CONSTRAINT `tour_schedule_chk_1` CHECK ((`available_seats` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_schedule`
--

LOCK TABLES `tour_schedule` WRITE;
/*!40000 ALTER TABLE `tour_schedule` DISABLE KEYS */;
INSERT INTO `tour_schedule` VALUES (5,18,'2026-10-03',38900.00,'2026-10-11',1),(6,20,'2026-10-03',124500.00,'2026-10-13',2),(7,20,'2026-10-03',31900.00,'2026-10-10',3),(8,20,'2026-10-03',69900.00,'2026-10-12',4),(9,20,'2026-10-03',109900.00,'2026-10-11',5),(10,20,'2026-10-03',45900.00,'2026-10-09',6),(11,20,'2026-10-03',35900.00,'2026-10-09',7),(12,20,'2026-10-03',145000.00,'2026-10-11',8),(13,19,'2026-10-03',89900.00,'2026-10-09',17),(14,20,'2026-10-03',64900.00,'2026-10-11',18),(15,20,'2026-10-03',139000.00,'2026-10-12',19),(16,20,'2026-10-03',52900.00,'2026-10-09',20),(17,19,'2026-10-03',82900.00,'2026-10-10',21),(18,17,'2026-10-03',38900.00,'2026-10-07',22),(19,100,'2026-08-04',38000.00,'2026-08-15',1),(20,118,'2026-08-04',110000.00,'2026-08-14',28),(21,120,'2026-08-14',100000.00,'2026-08-26',28),(22,100,'2026-08-04',23999.00,'2026-08-08',29),(23,111,'2026-08-10',23999.00,'2026-08-14',29),(24,149,'2026-08-15',23999.00,'2026-08-20',29);
/*!40000 ALTER TABLE `tour_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_sub_sector`
--

DROP TABLE IF EXISTS `tour_sub_sector`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_sub_sector` (
  `tour_id` bigint NOT NULL,
  `sub_sector_id` bigint NOT NULL,
  PRIMARY KEY (`tour_id`,`sub_sector_id`),
  KEY `FKgrdfl5of1n792sm4kjer09to9` (`sub_sector_id`),
  CONSTRAINT `FKaymbe6u70936bgq191pmloqbw` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE,
  CONSTRAINT `FKgrdfl5of1n792sm4kjer09to9` FOREIGN KEY (`sub_sector_id`) REFERENCES `sub_sector` (`sub_sector_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_sub_sector`
--

LOCK TABLES `tour_sub_sector` WRITE;
/*!40000 ALTER TABLE `tour_sub_sector` DISABLE KEYS */;
INSERT INTO `tour_sub_sector` VALUES (1,2),(3,3),(6,4),(7,5),(20,6),(5,7),(8,7),(19,7),(2,8),(4,9),(17,10),(18,11),(21,16),(8,18),(22,18);
/*!40000 ALTER TABLE `tour_sub_sector` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tour_tag_rule`
--

DROP TABLE IF EXISTS `tour_tag_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tour_tag_rule` (
  `rule_id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `match_field` enum('ALL','BASE_PRICE','DURATION_DAYS','TITLE','TOUR_CODE') NOT NULL,
  `match_operator` enum('ANY','BETWEEN','CONTAINS','EQUALS','GREATER_THAN','LESS_THAN') NOT NULL,
  `match_value` varchar(120) DEFAULT NULL,
  `match_value_to` varchar(120) DEFAULT NULL,
  `name` varchar(120) NOT NULL,
  `priority` int DEFAULT NULL,
  `target_category_id` bigint NOT NULL,
  `target_sub_sector_id` bigint DEFAULT NULL,
  PRIMARY KEY (`rule_id`),
  KEY `FKa69aclngxpklvjtlqxb8oap7e` (`target_category_id`),
  KEY `FK5wng8c5823vs7sjsk0qgn0yfh` (`target_sub_sector_id`),
  CONSTRAINT `FK5wng8c5823vs7sjsk0qgn0yfh` FOREIGN KEY (`target_sub_sector_id`) REFERENCES `sub_sector` (`sub_sector_id`),
  CONSTRAINT `FKa69aclngxpklvjtlqxb8oap7e` FOREIGN KEY (`target_category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tour_tag_rule`
--

LOCK TABLES `tour_tag_rule` WRITE;
/*!40000 ALTER TABLE `tour_tag_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `tour_tag_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tourcost`
--

DROP TABLE IF EXISTS `tourcost`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tourcost` (
  `cost_id` bigint NOT NULL AUTO_INCREMENT,
  `base_price` decimal(12,2) NOT NULL,
  `child_with_bed_cost` decimal(12,2) DEFAULT NULL,
  `child_without_bed_cost` decimal(12,2) DEFAULT NULL,
  `extra_person_cost` decimal(12,2) DEFAULT NULL,
  `single_person_cost` decimal(12,2) DEFAULT NULL,
  `status` int NOT NULL,
  `tour_id` bigint NOT NULL,
  `valid_from` date NOT NULL,
  `valid_to` date NOT NULL,
  PRIMARY KEY (`cost_id`),
  KEY `FK839gwatfjg6go96pe06nya2fw` (`tour_id`),
  CONSTRAINT `FK839gwatfjg6go96pe06nya2fw` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tourcost`
--

LOCK TABLES `tourcost` WRITE;
/*!40000 ALTER TABLE `tourcost` DISABLE KEYS */;
/*!40000 ALTER TABLE `tourcost` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(150) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `preferred_language` varchar(10) DEFAULT NULL,
  `status` bit(1) NOT NULL,
  `role_id` bigint NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `auth_provider` varchar(20) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `google_sub` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  KEY `FKp56c1712k691lhsyewcssf40f` (`role_id`),
  CONSTRAINT `FKp56c1712k691lhsyewcssf40f` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'sarvesh@gmail.com','Sarvesh','Saraf','$2a$10$emM62oNaZ1Cyw6aiDZA/.O.6A6KRjBECHmhIW0Yh7hSd5ZUrqaMhO','9876543210','en',_binary '',1,NULL,'2026-07-31 09:10:39.215630',NULL,NULL,NULL),(2,'john@test.com','John','Doe','$2a$10$RwaXH8u4JAI2bHmTRcm1aOqa3wsxVyb7uxJOrujjKTwbxYGPyRBPS','9876543210','en',_binary '',1,NULL,'2026-07-31 09:10:39.215630',NULL,NULL,NULL),(3,'rish@gmail.com','rishabh','angure','$2a$10$/IFhx59w8Q7FiQtuiof2/.wEpzrkjJS3yDedxeQq8w/DYeiSh1Pee','8569231145','en',_binary '',1,NULL,'2026-07-31 09:10:39.215630',NULL,NULL,NULL),(4,'admin@gmail.com','admin','admin','$2a$10$0poAS./bs6/QilRH/WYYqelNYvS6EJjTiwSlEKBL9FFNsSKdLWmNC','8569239995','en',_binary '',2,NULL,'2026-07-31 09:10:39.215630',NULL,NULL,NULL),(5,'shrikant@test.com','Shrikant','Pawar','$2a$10$lCtVPuEYzInWRSv//NTSZeru/wRM7IasgDCYXLqofeyHYedTAF5ce','','en',_binary '',2,NULL,'2026-07-31 09:10:39.215630',NULL,NULL,NULL),(6,'test@example.com','Test','User','$2a$10$q5t2GpmRGtzWdEC6SgEhsuC3uesr9BVWa0hX.eoEQKM5Sn0yap3W2','9876543210','en',_binary '',2,NULL,'2026-07-31 09:10:39.215630',NULL,NULL,NULL),(7,'adnan@tour.com','Adnan','','$2a$10$QfAQnRzB.wxzhuqBL2Bu0.lnSA8VcUFAAcYP7rkqQWxFu1U5KXhxu','','en',_binary '',1,NULL,'2026-07-31 09:10:39.215630',NULL,NULL,NULL),(8,'preet@test.com','preet','','$2a$10$N/jPoK8WzRzNuaTbDxDXiuGEcNO.7U48mOlFEVWb.NN1Sl7IpS3Fi','9638527410','en',_binary '',1,NULL,'2026-07-31 09:10:39.215630',NULL,NULL,NULL),(9,'testflow@example.com','Test','User','$2a$10$JcAFXqbCAACZ7Wpr3SBhU.8iYrg5vPB9mWSwa.UP.lv3B0HakrTFm','9999999999','en',_binary '',2,NULL,'2026-07-31 12:05:50.112127',NULL,NULL,NULL),(10,'adminflow@example.com','Admin','Flow','$2a$10$9cD2309s99UuFerPxA5zm.s1iJruZJzyXtUPTryHjn4aPCP/9Cx76','8888888888','en',_binary '',1,NULL,'2026-07-31 12:18:06.087280',NULL,NULL,NULL),(11,'riya.sharma@example.com','Riya','Sharma','$2a$10$fzJ57VEoyiKxMZN5AkgOOObQkVKZmHSq7dsavkv6yNRh9UVHvikre','+91 90000 11111','en',_binary '',1,NULL,'2026-07-31 13:26:36.664721',NULL,NULL,NULL),(12,'smoketest@etour.dev','Smoke','Test','$2a$10$UlDDAOtrdidE43YQKPdOLePh5Ha3FX8VBXIZwbZiwGHtS4E83u8gS',NULL,'en',_binary '',1,NULL,'2026-08-02 15:48:12.973230',NULL,NULL,NULL),(13,'admin@test.com','admin','sad','$2a$10$fxj.YkbPe/Oij3rymrSLd.CGMAN3cBMlG25LkVpIBU8SPaGQo7wEy','9876543560','en',_binary '',1,NULL,'2026-08-03 11:28:03.016700',NULL,NULL,NULL),(23,'shrikant@gmail.com','shrikant','Tiwari','$2a$10$XolZXq1oe6Dvbl0JplbB3OMbm1q4AXHgBSwaHVe1UzeGHmBxEeAjK','9892365645','en',_binary '',1,NULL,'2026-08-03 15:46:06.431815',NULL,NULL,NULL),(24,'admin.seed@etour.com','Demo','Admin','$2a$10$ic9rAN6E3Jji1ws5q88KteUAEgIGHAiNWnc2OI7UhxKey3H5DpZn.',NULL,'en',_binary '',2,NULL,'2026-08-03 16:33:25.128017',NULL,NULL,NULL),(25,'verify.tester@etour.dev','Verify','Tester','$2a$10$h2SsnV9BPjRvYGGxgKEPKO7pHUVv0JW2cP.hpg6cAZeAzvlssGBca','9876500001','en',_binary '',1,NULL,'2026-08-03 16:42:56.411874',NULL,NULL,NULL),(26,'harsh@test.com','Harsh','Gupta','$2a$10$hC7Gx3razoPkk4TMD2QO6O79Lnx/jw75FaGC0yohkTpzJrXhg7iQ6','9638527415','en',_binary '',1,NULL,'2026-08-03 18:15:35.608996',NULL,NULL,NULL),(27,'audit.customer@test.com','Audit','Customer','$2a$10$LFtbh14g1.pWw1Wg0frG.e//bU2kqjD8a/Rgp88Y.aIDsB4hvsS32','9999999999','en',_binary '',1,NULL,'2026-08-03 21:24:13.979202',NULL,NULL,NULL),(28,'angurerishabh2002@gmail.com','Rishabh','Angure','$2a$10$2oFyTnIcZUFeRQ8S4MXtQuHs050Q72PwU46OmmCJk.fxsqh/L/gkS','1234567890','en',_binary '',1,NULL,'2026-08-04 10:43:18.633870',NULL,NULL,NULL),(29,'Aditya@gmail.com','Aditya','Mali','$2a$10$iakQklrn848hF0W63KV24eqDh907YK3NOiTccTSeAv4jj9BmL5oWe','9638527413','en',_binary '',1,NULL,'2026-08-04 11:02:02.678806',NULL,NULL,NULL),(30,'scrvss@gmail.com','Sarvesh','Saraf','$2a$10$TD0oObjgMgbEAcZmsQDPROdnT.T.bSEOfpbO1INVIvFEG8nScxJYe',NULL,'en',_binary '',1,NULL,'2026-08-05 19:48:25.142429','GOOGLE','https://lh3.googleusercontent.com/a/ACg8ocINX3p7kgcPNYglpIJgaqoBRQeSa-7jRE5bjQLEHGqc6IG2cw=s96-c','117656879438465728911');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist_item`
--

DROP TABLE IF EXISTS `wishlist_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlist_item` (
  `wishlist_item_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `customer_id` bigint NOT NULL,
  `tour_id` bigint NOT NULL,
  PRIMARY KEY (`wishlist_item_id`),
  UNIQUE KEY `uk_wishlist_customer_tour` (`customer_id`,`tour_id`),
  KEY `FKhmtow5c79d5mpwlfr4s3tvoh9` (`tour_id`),
  CONSTRAINT `FKhlayjwpqqq6md817g2xhnreyp` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `FKhmtow5c79d5mpwlfr4s3tvoh9` FOREIGN KEY (`tour_id`) REFERENCES `tour` (`tour_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist_item`
--

LOCK TABLES `wishlist_item` WRITE;
/*!40000 ALTER TABLE `wishlist_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `wishlist_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'etour'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-07 16:08:36
