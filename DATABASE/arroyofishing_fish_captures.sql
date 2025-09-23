CREATE DATABASE  IF NOT EXISTS `arroyofishing` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `arroyofishing`;
-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: arroyofishing
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `fish_captures`
--

DROP TABLE IF EXISTS `fish_captures`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fish_captures` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `fish_type` varchar(255) NOT NULL,
  `weight` double NOT NULL,
  `capture_date` date NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `fish_captures_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fish_captures`
--

LOCK TABLES `fish_captures` WRITE;
/*!40000 ALTER TABLE `fish_captures` DISABLE KEYS */;
INSERT INTO `fish_captures` VALUES (1,1,'Carpa',4.25,'2025-09-15','Embalse de Valdeobispo','2025-09-23 10:17:06'),(2,2,'Black Bass',2.1,'2025-09-12','Río Almonte','2025-09-23 10:17:06'),(3,3,'Barbo',1.35,'2025-09-10','Arroyo de la Luz','2025-09-23 10:17:06'),(4,4,'Carpa',3.6,'2025-09-11','Embalse de Valdeobispo','2025-09-23 10:17:06'),(5,5,'Lucio',5.5,'2025-09-14','Río Almonte','2025-09-23 10:17:06'),(6,6,'Perca',0.85,'2025-09-09','Arroyo de la Luz','2025-09-23 10:17:06'),(7,7,'Carpa',6.2,'2025-09-13','Embalse de Valdeobispo','2025-09-23 10:17:06'),(8,8,'Barbo',1.2,'2025-09-08','Río Almonte','2025-09-23 10:17:06'),(9,9,'Black Bass',2.5,'2025-09-07','Embalse de Valdeobispo','2025-09-23 10:17:06'),(10,10,'Lucio',4.8,'2025-09-06','Arroyo de la Luz','2025-09-23 10:17:06');
/*!40000 ALTER TABLE `fish_captures` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-23 17:20:28
