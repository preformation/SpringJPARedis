-- MySQL dump 10.16  Distrib 10.3.10-MariaDB, for osx10.14 (x86_64)
--
-- Host: 127.0.0.1    Database: springdata
-- ------------------------------------------------------
-- Server version	10.3.10-MariaDB

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
-- Dumping routines for database 'springdata'
--

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customer` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `surname` varchar(20) DEFAULT NULL,
  `sex` int(11) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=149 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer`
--

/*!40000 ALTER TABLE `customer` DISABLE KEYS */;
INSERT INTO `customer` (`id`, `name`, `surname`, `sex`, `email`, `created_time`) VALUES (79,'99x','q0',1,'220@cx.com',NULL),(80,'97u','i0',2,'220@cx.com','2019-03-27 07:53:42'),(81,'00m','u0',1,'232@cx.com','2019-03-27 07:53:42'),(82,'c00','n0',3,'208@cx.com','2019-03-27 07:53:42'),(83,'fp0','m0',2,'212@cx.com','2019-03-27 07:53:42'),(84,'119i','w0',3,'228@cx.com','2019-03-27 07:53:42'),(85,'va0','r0',2,'229@cx.com','2019-03-27 07:53:42'),(86,'00e','b0',2,'216@cx.com','2019-03-27 07:53:42'),(87,'x00','j0',3,'222@cx.com','2019-03-27 07:53:42'),(88,'uq0','r0',1,'231@cx.com','2019-03-27 07:53:42'),(109,'122x','k0',2,'201@cx.com','2019-03-27 07:53:42'),(110,'qz0','c0',2,'214@cx.com','2019-03-27 07:53:42'),(111,'00u','x0',2,'221@cx.com','2019-03-27 07:53:42'),(112,'110i','m0',1,'220@cx.com','2019-03-27 07:53:42'),(113,'vt0','g0',3,'214@cx.com','2019-03-27 07:53:42'),(114,'106b','f0',2,'220@cx.com','2019-03-27 07:53:42'),(115,'nx0','p0',2,'226@cx.com','2019-03-27 07:53:42'),(116,'00v','l0',1,'222@cx.com','2019-03-27 07:53:42'),(117,'w00','i0',3,'195@cx.com','2019-03-27 07:53:42'),(118,'iy0','n0',3,'219@cx.com','2019-03-27 07:53:42'),(119,'115o','k0',2,'220@cx.com','2019-03-27 07:53:42'),(120,'mn0','p0',2,'215@cx.com','2019-03-27 07:53:42'),(121,'00o','a0',2,'230@cx.com','2019-03-27 07:53:42'),(122,'f00','w0',1,'234@cx.com','2019-03-27 07:53:42'),(123,'oq0','j0',3,'240@cx.com','2019-03-27 07:53:42'),(124,'101y','s0',2,'237@cx.com','2019-03-27 07:53:42'),(125,'bn0','p0',1,'219@cx.com','2019-03-27 07:53:42'),(126,'00x','e0',1,'200@cx.com','2019-03-27 07:53:42'),(127,'q00','a0',2,'226@cx.com','2019-03-27 07:53:42'),(128,'cu0','u0',3,'234@cx.com','2019-03-27 07:53:42'),(129,'112k','d0',1,'202@cx.com','2019-03-27 07:53:42'),(130,'fp0','t0',2,'243@cx.com','2019-03-27 07:53:42'),(131,'00y','x0',1,'220@cx.com','2019-03-27 07:53:42'),(132,'z00','f0',3,'230@cx.com','2019-03-27 07:53:42'),(133,'di0','x0',3,'216@cx.com','2019-03-27 07:53:42'),(134,'107d','f0',3,'232@cx.com','2019-03-27 07:53:42'),(135,'iu0','d0',1,'215@cx.com','2019-03-27 07:53:42'),(136,'00v','z0',2,'215@cx.com','2019-03-27 07:53:42'),(137,'w00','w0',2,'205@cx.com','2019-03-27 07:53:42'),(138,'on0','v0',2,'212@cx.com','2019-03-27 07:53:42'),(139,'115j','m0',3,'227@cx.com','2019-03-27 07:53:42'),(140,'ru0','u0',2,'241@cx.com','2019-03-27 07:53:42'),(141,'00y','i0',2,'211@cx.com','2019-03-27 07:53:42'),(142,'m00','k0',1,'224@cx.com','2019-03-27 07:53:42'),(143,'oa0','r0',1,'215@cx.com','2019-03-27 07:53:42'),(144,'109t','l0',2,'222@cx.com','2019-03-27 07:53:55'),(145,'pt0','z0',1,'227@cx.com','2019-03-27 07:53:55'),(146,'00v','b0',2,'211@cx.com','2019-03-27 07:53:55'),(147,'o00','s0',2,'196@cx.com','2019-03-27 07:53:55'),(148,'ya0','l0',3,'219@cx.com','2019-03-27 07:53:55');
/*!40000 ALTER TABLE `customer` ENABLE KEYS */;

--
-- Dumping events for database 'springdata'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-03-27 15:56:15
