CREATE DATABASE  IF NOT EXISTS `db_swap` DEFAULT CHARACTER SET utf8mb3;
USE `db_swap`;
-- MySQL dump 10.13  Distrib 8.0.28, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: db_swap
-- ------------------------------------------------------
-- Server version	8.0.28-0ubuntu0.20.04.3

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
  `userAddress` varchar(80) NOT NULL,
  `tokenId` int NOT NULL,
  `serverServiceId` int NOT NULL,
  PRIMARY KEY (`idChainService`,`serverServiceId`),
  UNIQUE KEY `id_UNIQUE` (`idChainService`),
  UNIQUE KEY `serverServiceId_UNIQUE` (`serverServiceId`),
  CONSTRAINT `fk_service_id` FOREIGN KEY (`serverServiceId`) REFERENCES `Service` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=138 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  UNIQUE KEY `userWalletAddress_UNIQUE` (`userWalletAddress`)
) ENGINE=InnoDB AUTO_INCREMENT=4308 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

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