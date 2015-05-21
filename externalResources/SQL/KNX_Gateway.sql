CREATE DATABASE  IF NOT EXISTS `KNXGateway` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `KNXGateway`;
-- MySQL dump 10.13  Distrib 5.5.21, for osx10.6 (i386)
--
-- Host: localhost    Database: KNXGateway
-- ------------------------------------------------------
-- Server version	5.5.31-0+wheezy1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `DPClient`
--

DROP TABLE IF EXISTS `DPClient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DPClient` (
  `CL_PK_Client` int(11) NOT NULL AUTO_INCREMENT,
  `CL_Referer` varchar(100) NOT NULL,
  `CL_Days` smallint(6) NOT NULL,
  `CL_FK_DPStorage` int(11) NOT NULL,
  PRIMARY KEY (`CL_PK_Client`),
  KEY `FK_DPStorage_idx` (`CL_FK_DPStorage`),
  CONSTRAINT `FK_DPStorage` FOREIGN KEY (`CL_FK_DPStorage`) REFERENCES `DPStorage` (`DPS_PK_DPStorage`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DPStorage`
--

DROP TABLE IF EXISTS `DPStorage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DPStorage` (
  `DPS_PK_DPStorage` int(11) NOT NULL AUTO_INCREMENT,
  `DPS_GroupAddress` int(11) NOT NULL,
  `DPS_DPT` varchar(12) NOT NULL,
  `DPS_LastRead` datetime DEFAULT NULL,
  `DPS_DPName` varchar(30) NOT NULL,
  PRIMARY KEY (`DPS_PK_DPStorage`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DPValue`
--

DROP TABLE IF EXISTS `DPValue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DPValue` (
  `DPD_PK_DPValue` int(11) NOT NULL AUTO_INCREMENT,
  `DPD_FK_DPStorage` int(11) NOT NULL,
  `DPD_Value` varchar(150) NOT NULL,
  `DPD_Date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`DPD_PK_DPValue`),
  KEY `FK_DPStorage_DPValue_idx` (`DPD_FK_DPStorage`),
  KEY `DPD_Datetime` (`DPD_Date`),
  CONSTRAINT `FK_DPStorage_DPValue` FOREIGN KEY (`DPD_FK_DPStorage`) REFERENCES `DPStorage` (`DPS_PK_DPStorage`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-05-21 10:23:43
