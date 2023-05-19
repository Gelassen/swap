CREATE DATABASE  IF NOT EXISTS `test_db_swap` DEFAULT CHARACTER SET utf8mb3;
USE `test_db_swap`;
-- MySQL dump 10.13  Distrib 8.0.32, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: test_db_swap
-- ------------------------------------------------------
-- Server version	8.0.32-0ubuntu0.22.04.2

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
-- Table structure for table `ChainServices`
--

DROP TABLE IF EXISTS `ChainServices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ChainServices` (
  `idChainService` int NOT NULL AUTO_INCREMENT,
  `userWalletAddress` varchar(80) NOT NULL,
  `tokenId` int NOT NULL,
  `serverServiceId` int NOT NULL,
  PRIMARY KEY (`idChainService`,`serverServiceId`),
  UNIQUE KEY `id_UNIQUE` (`idChainService`),
  UNIQUE KEY `serverServiceId_UNIQUE` (`serverServiceId`),
  CONSTRAINT `fk_service_id` FOREIGN KEY (`serverServiceId`) REFERENCES `Service` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ChainServices`
--

LOCK TABLES `ChainServices` WRITE;
/*!40000 ALTER TABLE `ChainServices` DISABLE KEYS */;
/*!40000 ALTER TABLE `ChainServices` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PotentialMatches`
--

DROP TABLE IF EXISTS `PotentialMatches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `PotentialMatches` (
  `id` int NOT NULL AUTO_INCREMENT,
  `userFirstProfileId` int NOT NULL,
  `userSecondProfileId` int NOT NULL,
  `userFirstServiceId` int NOT NULL,
  `userSecondServiceId` int NOT NULL,
  `approvedByFirstUser` tinyint NOT NULL,
  `approvedBySecondUser` tinyint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  CONSTRAINT `fk_first_service_id` FOREIGN KEY (`userFirstServiceId`) REFERENCES `Service` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_second_service_id` FOREIGN KEY (`userSecondServiceId`) REFERENCES `Service` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=138 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PotentialMatches`
--

LOCK TABLES `PotentialMatches` WRITE;
/*!40000 ALTER TABLE `PotentialMatches` DISABLE KEYS */;
INSERT INTO `PotentialMatches` VALUES (124,4235,4234,13806,13804,0,0),(125,4237,4236,13810,13808,1,0),(126,4239,4238,13814,13812,1,1),(127,4241,4240,13818,13816,0,0),(128,4243,4242,13822,13820,0,0),(129,4245,4244,13826,13824,1,0),(130,4247,4246,13830,13828,1,1),(131,4264,4263,13845,13843,0,0),(132,4295,4294,14033,14031,1,0),(133,4297,4296,14037,14035,1,1),(134,4301,4300,14043,14041,1,0),(135,4303,4302,14047,14045,1,0),(136,4305,4304,14051,14049,1,0),(137,4307,4306,14055,14053,1,0);
/*!40000 ALTER TABLE `PotentialMatches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Profile`
--

DROP TABLE IF EXISTS `Profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Profile` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `contact` varchar(45) NOT NULL,
  `secret` varchar(45) NOT NULL,
  `userWalletAddress` varchar(75) NOT NULL,
  PRIMARY KEY (`id`,`contact`,`secret`),
  UNIQUE KEY `userAddressOnChain_UNIQUE` (`userWalletAddress`)
) ENGINE=InnoDB AUTO_INCREMENT=4308 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Profile`
--

LOCK TABLES `Profile` WRITE;
/*!40000 ALTER TABLE `Profile` DISABLE KEYS */;
/*!40000 ALTER TABLE `Profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Service`
--

DROP TABLE IF EXISTS `Service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Service` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(320) NOT NULL,
  `date` bigint NOT NULL,
  `offer` tinyint NOT NULL,
  `index` varchar(450) DEFAULT NULL,
  `profileId` int NOT NULL,
  PRIMARY KEY (`id`,`profileId`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_id` (`profileId`),
  CONSTRAINT `fk_id` FOREIGN KEY (`profileId`) REFERENCES `Profile` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14057 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Service`
--

LOCK TABLES `Service` WRITE;
/*!40000 ALTER TABLE `Service` DISABLE KEYS */;
/*!40000 ALTER TABLE `Service` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-02-09 17:58:50
