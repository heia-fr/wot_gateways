CREATE DATABASE  IF NOT EXISTS `enocean` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `enocean`;
-- MySQL dump 10.13  Distrib 5.5.21, for osx10.6 (i386)
--
-- Host: localhost    Database: enocean
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
-- Table structure for table `client`
--

DROP TABLE IF EXISTS `client`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `client` (
  `pk_client` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `cl_referer` varchar(45) NOT NULL,
  `cl_days` smallint(5) unsigned NOT NULL,
  `cl_fk_measure` mediumint(8) unsigned NOT NULL,
  PRIMARY KEY (`pk_client`),
  KEY `cl_fk_measure_idx` (`cl_fk_measure`),
  CONSTRAINT `cl_fk_measure` FOREIGN KEY (`cl_fk_measure`) REFERENCES `measure` (`pk_measure`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `data_sensor`
--

DROP TABLE IF EXISTS `data_sensor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_sensor` (
  `pk_data_sensor` mediumint(9) unsigned NOT NULL AUTO_INCREMENT,
  `data_value` float NOT NULL,
  `data_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `data_fk_measure` mediumint(8) unsigned NOT NULL,
  PRIMARY KEY (`pk_data_sensor`),
  KEY `data_sensor_fk_measure_measure_idx` (`data_fk_measure`),
  CONSTRAINT `data_fk_measure_measure` FOREIGN KEY (`data_fk_measure`) REFERENCES `measure` (`pk_measure`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=17473 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group`
--

DROP TABLE IF EXISTS `group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group` (
  `pk_group` smallint(6) unsigned NOT NULL AUTO_INCREMENT,
  `gr_name` varchar(45) NOT NULL,
  `gr_description` varchar(255) DEFAULT NULL,
  `gr_fk_user_mod` smallint(6) unsigned NOT NULL,
  `gr_date_of_modification` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`pk_group`),
  KEY `gr_fk_user_mod_user_idx` (`gr_fk_user_mod`),
  CONSTRAINT `gr_fk_user_mod_user` FOREIGN KEY (`gr_fk_user_mod`) REFERENCES `user` (`pk_user`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_sensor`
--

DROP TABLE IF EXISTS `group_sensor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_sensor` (
  `fk_group` smallint(6) unsigned NOT NULL,
  `fk_sensor` smallint(6) unsigned NOT NULL,
  PRIMARY KEY (`fk_group`,`fk_sensor`),
  KEY `fk_group_sensor_group_idx` (`fk_group`),
  KEY `fk_group_sensor_sensor_idx` (`fk_sensor`),
  CONSTRAINT `fk_group_sensor_group` FOREIGN KEY (`fk_group`) REFERENCES `group` (`pk_group`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_group_sensor_sensor` FOREIGN KEY (`fk_sensor`) REFERENCES `sensor` (`pk_sensor`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `location`
--

DROP TABLE IF EXISTS `location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location` (
  `pk_location` smallint(6) unsigned NOT NULL AUTO_INCREMENT,
  `loc_parent_location` smallint(6) unsigned DEFAULT NULL,
  `loc_name` varchar(45) NOT NULL,
  `loc_fk_type` smallint(6) unsigned NOT NULL,
  `loc_path` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`pk_location`),
  KEY `loc_fk_type_location_type_idx` (`loc_fk_type`),
  KEY `loc_parent_location_location_idx` (`loc_parent_location`),
  CONSTRAINT `loc_fk_type_location_type` FOREIGN KEY (`loc_fk_type`) REFERENCES `location_type` (`pk_location_type`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `loc_parent_location_location` FOREIGN KEY (`loc_parent_location`) REFERENCES `location` (`pk_location`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `location_type`
--

DROP TABLE IF EXISTS `location_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `location_type` (
  `pk_location_type` smallint(6) unsigned NOT NULL AUTO_INCREMENT,
  `loc_type_name` varchar(45) NOT NULL,
  `loc_type_img_url` varchar(45) NOT NULL,
  PRIMARY KEY (`pk_location_type`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `manufacturer`
--

DROP TABLE IF EXISTS `manufacturer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `manufacturer` (
  `pk_manufacturer` smallint(5) unsigned NOT NULL,
  `man_name` varchar(45) NOT NULL,
  PRIMARY KEY (`pk_manufacturer`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `measure`
--

DROP TABLE IF EXISTS `measure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `measure` (
  `pk_measure` mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
  `mea_unit` varchar(45) DEFAULT NULL,
  `mea_scale_max` float DEFAULT NULL,
  `mea_scale_min` float DEFAULT NULL,
  `mea_eep_shortcut` varchar(20) NOT NULL,
  `mea_last_read` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `mea_fk_sensor` smallint(5) unsigned NOT NULL,
  `mea_fk_data_sensor` mediumint(8) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_measure`),
  KEY `mea_fk_sensor_sensor_idx` (`mea_fk_sensor`),
  KEY `mea_fk_data_sensor_data_sensor_idx` (`mea_fk_data_sensor`),
  CONSTRAINT `mea_fk_data_sensor_data_sensor` FOREIGN KEY (`mea_fk_data_sensor`) REFERENCES `data_sensor` (`pk_data_sensor`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `mea_fk_sensor_sensor` FOREIGN KEY (`mea_fk_sensor`) REFERENCES `sensor` (`pk_sensor`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sensor`
--

DROP TABLE IF EXISTS `sensor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sensor` (
  `pk_sensor` smallint(6) unsigned NOT NULL AUTO_INCREMENT,
  `sens_name` varchar(45) NOT NULL,
  `sens_description` varchar(255) NOT NULL,
  `sens_fk_location` smallint(6) unsigned NOT NULL,
  `sens_fk_manufacturer` smallint(5) unsigned NOT NULL,
  `sens_eep_rorg` smallint(6) unsigned DEFAULT NULL,
  `sens_eep_function` smallint(6) unsigned DEFAULT NULL,
  `sens_eep_type` smallint(6) unsigned DEFAULT NULL,
  `sens_date_of_modification` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `sens_fk_user_mod` smallint(6) unsigned NOT NULL,
  `sens_is_actuator` bit(1) NOT NULL,
  `sens_is_in_hybrid_mode` bit(1) NOT NULL,
  `sens_address` int(10) unsigned NOT NULL,
  PRIMARY KEY (`pk_sensor`),
  UNIQUE KEY `sens_address_UNIQUE` (`sens_address`),
  KEY `sens_fk_user_mod_user_idx` (`sens_fk_user_mod`),
  KEY `sens_fk_location_location_idx` (`sens_fk_location`),
  KEY `sens_fk_manufacturer_manufacturer_idx` (`sens_fk_manufacturer`),
  CONSTRAINT `sens_fk_location_location` FOREIGN KEY (`sens_fk_location`) REFERENCES `location` (`pk_location`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sens_fk_manufacturer_manufacturer` FOREIGN KEY (`sens_fk_manufacturer`) REFERENCES `manufacturer` (`pk_manufacturer`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sens_fk_user_mod_user` FOREIGN KEY (`sens_fk_user_mod`) REFERENCES `user` (`pk_user`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `pk_user` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `usr_username` varchar(45) NOT NULL,
  `usr_password` varchar(45) NOT NULL,
  `usr_first_name` varchar(45) NOT NULL,
  `usr_last_name` varchar(45) NOT NULL,
  `usr_email` varchar(45) NOT NULL,
  `usr_is_admin` bit(1) NOT NULL DEFAULT b'0',
  `usr_is_active` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`pk_user`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;

INSERT INTO `user` VALUES (10,'admin','d033e22ae348aeb5660fc2140aec35850c4da997','admin','admin','admin@exemple.com','','');

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-05-21 10:24:07
