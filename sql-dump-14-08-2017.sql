-- MySQL dump 10.13  Distrib 5.7.12, for Win64 (x86_64)
--
-- Host: ec2-52-43-44-185.us-west-2.compute.amazonaws.com    Database: leaps
-- ------------------------------------------------------
-- Server version	5.6.35

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
-- Table structure for table `custom_tags`
--

DROP TABLE IF EXISTS `custom_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `custom_tags` (
  `tag_id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`tag_id`),
  KEY `custom_tags_fk_idx` (`event_id`),
  CONSTRAINT `custom_tags_fk` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `custom_tags`
--

LOCK TABLES `custom_tags` WRITE;
/*!40000 ALTER TABLE `custom_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `custom_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event_has_tags`
--

DROP TABLE IF EXISTS `event_has_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_has_tags` (
  `tag_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `event_tags_kf_idx` (`event_id`),
  KEY `tag_events_fk` (`tag_id`),
  CONSTRAINT `event_tags_fk` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `tag_events_fk` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`tag_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=129 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_has_tags`
--

LOCK TABLES `event_has_tags` WRITE;
/*!40000 ALTER TABLE `event_has_tags` DISABLE KEYS */;
INSERT INTO `event_has_tags` VALUES (1,2,1),(2,2,2),(3,2,3),(4,2,4),(5,2,5),(6,2,6),(7,2,7),(8,2,8),(9,2,9),(10,2,10),(8,3,11),(7,3,12),(6,3,13),(10,3,14),(2,3,15),(9,3,16),(1,3,17),(11,3,18),(12,3,19),(1,4,20),(13,4,21),(14,4,22),(13,5,23),(14,5,24),(1,5,25),(13,6,26),(14,6,27),(1,6,28),(13,7,29),(14,7,30),(1,7,31),(13,8,32),(14,8,33),(1,8,34),(3,15,35),(2,15,36),(1,15,37),(3,16,38),(1,16,39),(3,17,40),(2,17,41),(1,17,42),(1,18,43),(16,18,44),(4,19,45),(17,20,46),(2,21,47),(1,21,48),(1,22,49),(8,23,50),(7,23,51),(2,24,52),(1,24,53),(3,25,54),(2,25,55),(1,25,56),(3,26,57),(2,26,58),(1,26,59),(3,27,60),(2,27,61),(1,27,62),(3,28,63),(2,28,64),(1,28,65),(16,29,66),(2,30,67),(1,30,68),(3,31,69),(2,31,70),(1,31,71),(3,32,72),(1,32,73),(2,33,74),(1,33,75),(3,34,76),(1,34,77),(3,35,78),(2,35,79),(1,35,80),(3,36,81),(2,36,82),(1,36,83),(3,37,84),(2,37,85),(1,37,86),(3,38,87),(2,38,88),(1,38,89),(3,39,90),(2,39,91),(1,39,92),(2,40,93),(1,40,94),(3,41,95),(2,41,96),(1,41,97),(3,42,98),(2,42,99),(1,42,100),(3,43,101),(2,43,102),(3,44,103),(2,44,104),(1,44,105),(2,46,106),(1,46,107),(4,47,108),(5,47,109),(11,47,110),(16,48,111),(1,48,112),(10,49,113),(11,49,114),(18,49,115),(3,50,116),(19,51,117),(3,52,118),(4,53,119),(5,53,120),(1,53,121),(20,55,122),(21,55,123),(22,55,124),(5,56,125),(10,56,126),(11,56,127),(1,56,128);
/*!40000 ALTER TABLE `event_has_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event_images`
--

DROP TABLE IF EXISTS `event_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_images` (
  `image_id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) NOT NULL,
  `file_name` varchar(45) NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `event_images_fk_idx` (`event_id`),
  CONSTRAINT `event_images_fk` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_images`
--

LOCK TABLES `event_images` WRITE;
/*!40000 ALTER TABLE `event_images` DISABLE KEYS */;
INSERT INTO `event_images` VALUES (1,15,'Images/Events/1501245987234.jpeg'),(2,15,'Images/Events/1501245988386.jpeg'),(3,15,'Images/Events/1501245988938.png'),(4,16,'Images/Events/1501248277567.gif'),(5,16,'Images/Events/1501248279158.gif'),(6,1,'Images/Events/1501270811238.png'),(7,17,'Images/Events/1501321135023.gif'),(8,17,'Images/Events/1501321137746.gif'),(9,17,'Images/Events/1501321139907.gif'),(10,24,'Images/Events/1501454587216.jpg'),(11,25,'Images/Events/1501530450139.gif'),(12,25,'Images/Events/1501530450751.gif'),(13,25,'Images/Events/1501530452823.gif'),(14,26,'Images/Events/1501530871572.gif'),(15,26,'Images/Events/1501530873490.gif'),(16,27,'Images/Events/1501531104610.gif'),(17,27,'Images/Events/1501531105968.gif'),(18,28,'Images/Events/1501531532546.png'),(19,28,'Images/Events/1501531533206.jpeg'),(20,29,'Images/Events/1501576296133.jpg'),(21,30,'Images/Events/1501606387307.jpeg'),(22,30,'Images/Events/1501606387864.jpeg'),(23,31,'Images/Events/1501606924533.jpeg'),(24,31,'Images/Events/1501606926888.gif'),(25,32,'Images/Events/1501607020302.jpeg'),(26,32,'Images/Events/1501607020850.jpeg'),(27,33,'Images/Events/1501607232291.gif'),(28,33,'Images/Events/1501607232904.jpeg'),(29,34,'Images/Events/1501607360918.jpeg'),(30,35,'Images/Events/1501607764746.gif'),(31,36,'Images/Events/1501608128676.jpeg'),(32,37,'Images/Events/1501608172955.jpeg'),(33,38,'Images/Events/1501608221468.jpeg'),(34,39,'Images/Events/1501608354528.jpeg'),(35,40,'Images/Events/1501608394077.jpeg'),(36,41,'Images/Events/1501608553300.jpeg'),(37,42,'Images/Events/1501610877624.jpeg'),(38,43,'Images/Events/1501611178679.jpeg'),(39,44,'Images/Events/1501612212167.jpeg'),(40,46,'Images/Events/1501706970699.jpg'),(41,48,'Images/Events/1502044829833.jpg'),(42,48,'Images/Events/1502044831704.jpg'),(43,49,'Images/Events/1502103406359.jpg'),(44,49,'Images/Events/1502103409752.jpg'),(45,50,'Images/Events/1502104033695.jpg'),(46,51,'Images/Events/1502107116003.jpg'),(47,52,'Images/Events/1502126970259.jpg'),(48,56,'Images/Events/1502183137772.png');
/*!40000 ALTER TABLE `event_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `events`
--

DROP TABLE IF EXISTS `events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `events` (
  `event_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `date` mediumtext NOT NULL,
  `time_from` mediumtext NOT NULL,
  `time_to` mediumtext,
  `owner_id` int(11) NOT NULL,
  `coord_lat` double NOT NULL,
  `coord_lnt` double NOT NULL,
  `price_from` int(11) NOT NULL,
  `address` varchar(100) NOT NULL,
  `free_slots` int(11) NOT NULL,
  `date_created` mediumtext NOT NULL,
  `event_image_url` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`event_id`),
  KEY `owner_fk_idx` (`owner_id`),
  CONSTRAINT `owner_fk` FOREIGN KEY (`owner_id`) REFERENCES `users` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `events`
--

LOCK TABLES `events` WRITE;
/*!40000 ALTER TABLE `events` DISABLE KEYS */;
INSERT INTO `events` VALUES (1,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',1,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1500331751174',NULL),(2,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',2,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1500332158238',NULL),(3,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',2,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1500332194928',NULL),(4,'title','dec\n','1501372804630','1501372804630','0',19,0,0,20,'Sofia',50,'1500669315383',NULL),(5,'title','desc','1501419624687','1501419624687','0',20,0,0,20,'Sofia',50,'1500669509793',NULL),(6,'title','desc','1501376443592','1501376443592','0',21,0,0,20,'Sofia',50,'1500669948010',NULL),(7,'title','desc','1501416010716','1501416010716','0',22,0,0,20,'Sofia',50,'1500670215947',NULL),(8,'thx','slaughter','1501416016606','1501416016606','0',22,0,0,20,'Sofia',50,'1500670341220',NULL),(9,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',1,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1500920707711',NULL),(10,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',1,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1500920763336',NULL),(11,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',1,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1500921319171',NULL),(12,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',1,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1500921375571',NULL),(13,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',1,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1500921448766',NULL),(14,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',1,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1500921603515',NULL),(15,'title','desc','1501524021981','1501524021981','0',37,0,0,20,'Sofia',50,'1501245985556',NULL),(16,'tren','Jarrod','1501372834973','1501372834973','0',37,0,0,20,'Sofia',50,'1501248275266',NULL),(17,'zaglavie','desc','1501513248355','1501513248355','0',43,44.111794,27.2640555,20,'Sofia',50,'1501321131358',NULL),(18,'ludo party','only beasts allowed','1501362000000','1501446600000','946762200000',40,0,0,5,'middle of nowhere',10,'1501362174701',NULL),(19,'titulo','something','1501794000000','1501795200000','946678800000',46,0,0,5,'panagiurishte',10,'1501363318911',NULL),(20,'shxhhd','hxhxhdhdhd','1501362000000','1501404960000','946720560000',47,0,0,12,'hddhhdhd',12,'1501405032931',NULL),(21,'lud event','24 tichane v borisivata','1501534800000','1501539900000','946682700000',48,0,0,5,'something',10,'1501453459975',NULL),(22,'dtdgdvv','vzhdgdh','1506718800000','1506724200000','946683000000',48,0,0,5,'hxgdgd',10,'1501453953430',NULL),(23,'hyper workout','55+','1504040400000','1504046400000','946683600000',48,0,0,5,'hdhdhd',10,'1501454173292',NULL),(24,'bdhdh','chxhh','1504126800000','1504133100000','946683900000',48,0,0,5,'dvdhhd',10,'1501454525948','Images/Events/1501454597636.jpg'),(25,'event','desc','1501593325783','1501593325783','0',37,0,0,20,'Sofia',50,'1501530448938',NULL),(26,'zaglavie','desc','1501589727500','1501589727500','0',37,0,0,20,'Sofia',50,'1501530869807','Images/Events/1501530870943.gif'),(27,'of full','PhD','1501636519199','1501636519199','0',37,0,0,50,'Sofia',20,'1501531101417','Images/Events/1501531103485.gif'),(28,'title','tren','1501491624151','1501491624151','0',37,0,0,20,'Sofia',50,'1501531527249','Images/Events/1501531530255.png'),(29,'Crossfit Saturday','Crazy workout, seriously.','1501880400000','1501921800000','946719000000',48,0,0,5,'near you',10,'1501576265005','Images/Events/1501576280963.jpg'),(30,'IV DC','no fee','1501634703482','1501634703482','0',37,0,0,20,'Sofia',50,'1501606386056','Images/Events/1501606386797.jpeg'),(31,'emo','echo','1501714800785','1501714800785','0',37,42.6580033,23.3424373,20,'Sofia',8,'1501606923132','Images/Events/1501606923977.jpeg'),(32,'Pro','Odell','1501552175182','1501552175182','0',37,42.6577169,23.3421616,20,'Sofia',5,'1501607018921','Images/Events/1501607019739.jpeg'),(33,'pro','tren','1501595404866','1501595404866','0',37,42.6580033,23.3424373,20,'Sofia',5,'1501607228957','Images/Events/1501607231116.gif'),(34,'title','event','1501658109055','1501658109055','0',37,42.6577169,23.3421616,2,'Sofia',2,'1501607359513','Images/Events/1501607360347.jpeg'),(35,'Wii','qty','1501634749392','1501634749392','0',37,42.6580033,23.3424373,50,'Sofia',20,'1501607757695','Images/Events/1501607762039.gif'),(36,'QC','desc','1501631704241','1501631704241','0',37,42.6580033,23.3424373,20,'Sofia',50,'1501608127250','Images/Events/1501608128077.jpeg'),(37,'qqq','qqq','1501638349921','1501638349921','0',37,42.6580033,23.3424373,2,'Sofia',2,'1501608171541','Images/Events/1501608172394.jpeg'),(38,'week','eek\n','1501661436295','1501661436295','0',37,42.6580033,23.3424373,20,'Sofia',50,'1501608220039','Images/Events/1501608220895.jpeg'),(39,'of','Web','1501725051366','1501725051366','0',37,42.6580033,23.3424373,2,'Sofia',2,'1501608353172','Images/Events/1501608353998.jpeg'),(40,'U','so\n','1501717287978','1501717287978','0',37,42.6580033,23.3424373,50,'Sofia',20,'1501608392730','Images/Events/1501608393518.jpeg'),(41,'qty','Web','1501807506894','1501807506894','0',37,42.6580033,23.3424373,20,'Sofia',50,'1501608551902','Images/Events/1501608552716.jpeg'),(42,'en','du','1501804313718','1501804313718','0',37,42.6577169,23.3421616,20,'Sofia',50,'1501610875960','Images/Events/1501610876948.jpeg'),(43,'eh','it\'s','1501890954396','1501890954396','0',37,42.6577169,23.3421616,20,'Sofia',50,'1501611176912','Images/Events/1501611177927.jpeg'),(44,'??','??','1501894208259','1501894208259','0',37,42.6580033,23.3424373,50,'Sofia',20,'1501612210364','Images/Events/1501612211397.jpeg'),(45,'katerene','fafla','1501794000000','1501837620000','946721220000',53,0,0,9,'dobrinishte',66,'1501694304357','Images/Events/1501694307062.jpg'),(46,'Event 1','Event 1 description','1535835600000','1535921280000','946763280000',54,0,0,20,'Sofia',40,'1501706965978','Images/Events/1501706968037.jpg'),(47,'super hard','super hard training','1502874030857','1502874030857','0',56,0,0,20,'Sofia',40,'1501766149716',NULL),(48,'Body and soul','Impress yourself','1503046821932','1503046821932','0',63,0,0,20,'Sofia',20,'1502044821612','Images/Events/1502044826002.jpg'),(49,'Event 2','Event 2 description','1502794839512','1502794839512','0',63,0,0,20,'Sofia',20,'1502103402422','Images/Events/1502103404788.jpg'),(50,'Test event','Some description','1502384421279','1502384421279','0',64,0,0,3,'Sofia',24,'1502103988574','Images/Events/1502104009750.jpg'),(51,'cycling class','Super hard ','1502571600000','1502607600000','946713600000',63,0,0,20,'sofia',20,'1502107113236','Images/Events/1502107115535.jpg'),(52,'Joro event','Some desc','1502978419434','1502978419434','0',64,0,0,3,'Sofia',5,'1502126965446','Images/Events/1502126968346.jpg'),(53,'?????????? ?????','??? ????? ??????????','1502139600000','1502209740000','946747740000',66,0,0,5,'?????',50,'1502126997026','Images/Events/1502127000820.jpg'),(54,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','622662342253221131','62266234225323212','622662342253231231',68,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1502132622766',NULL),(55,'trenirovchitsa','Nai-ultra mega huper yakata trenirovka ever izmislyana','6226623422532','6226623422532','6226623422532',68,42.693351,23.340381,10,'??. „?????“ 34-36, 1504 ?????',50,'1502132651191','Images/Events/1502133075028.png'),(56,'???????','?? ??????? ????? ? ????? ????','1502272827327','1502272827327','0',74,0,0,5,'Sofia',15,'1502183132651','Images/Events/1502183136074.png');
/*!40000 ALTER TABLE `events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `specialties`
--

DROP TABLE IF EXISTS `specialties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `specialties` (
  `specialty_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`specialty_id`),
  UNIQUE KEY `specialty_id_UNIQUE` (`specialty_id`),
  KEY `user_specialties_fk_idx` (`user_id`),
  CONSTRAINT `user_specialties_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `specialties`
--

LOCK TABLES `specialties` WRITE;
/*!40000 ALTER TABLE `specialties` DISABLE KEYS */;
/*!40000 ALTER TABLE `specialties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tags`
--

DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tags` (
  `tag_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tags`
--

LOCK TABLES `tags` WRITE;
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
INSERT INTO `tags` VALUES (8,'bobec'),(4,'cardio'),(19,'cycling'),(13,'exciting'),(7,'fafla'),(14,'fitness'),(3,'muha'),(17,'outdoor'),(6,'rakia'),(12,'running'),(5,'rusensko vareno'),(18,'sila'),(10,'slavei'),(2,'sokol'),(11,'squats'),(16,'strength'),(20,'tag1'),(21,'tag2'),(22,'tag3'),(9,'trash'),(1,'yoga'),(15,'тест на кирилица');
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_images`
--

DROP TABLE IF EXISTS `user_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_images` (
  `image_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `file_name` varchar(45) NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `user_images_fk_idx` (`user_id`),
  CONSTRAINT `user_images_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_images`
--

LOCK TABLES `user_images` WRITE;
/*!40000 ALTER TABLE `user_images` DISABLE KEYS */;
INSERT INTO `user_images` VALUES (10,37,'Images/Users/1501184201159.gif'),(15,43,'Images/Users/1501318676024.jpg'),(19,48,'Images/Users/1501448552944.jpg'),(21,63,'Images/Users/1502107286034.jpg'),(22,66,'Images/Users/1502124717249.jpg'),(23,67,'Images/Users/1502131747207.png'),(24,66,'Images/Users/1502295347173.jpg');
/*!40000 ALTER TABLE `user_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `email_address` varchar(100) NOT NULL,
  `password` varchar(50) NOT NULL,
  `age` int(11) DEFAULT NULL,
  `gender` varchar(1) DEFAULT NULL,
  `location` varchar(100) DEFAULT NULL COMMENT 'city',
  `max_distance_setting` int(11) DEFAULT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `birthday` mediumtext NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `profile_image_url` varchar(300) DEFAULT NULL,
  `is_trainer` tinyint(1) DEFAULT NULL,
  `facebook_id` varchar(100) DEFAULT NULL,
  `google_id` varchar(100) DEFAULT NULL,
  `phone_number` varchar(100) DEFAULT NULL,
  `years_of_training` int(11) DEFAULT NULL,
  `session_price` int(11) DEFAULT NULL,
  `long_description` varchar(3000) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_address_UNIQUE` (`email_address`),
  UNIQUE KEY `facebook_id_UNIQUE` (`facebook_id`),
  UNIQUE KEY `google_id_UNIQUE` (`google_id`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'mishka','mishka@mail.bg','1f2e3bea8bcb0c4b1d6e2b4d6d1cae9f',NULL,NULL,NULL,NULL,'zai','bai','2264545662345632',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,'zaiko','zaiko@mail.bg','1f2e3bea8bcb0c4b1d6e2b4d6d1cae9f',NULL,NULL,NULL,NULL,'zai','bai','2264545662345632',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,'torbalan','torbalan@mail.bg','1f2e3bea8bcb0c4b1d6e2b4d6d1cae9f',NULL,NULL,NULL,NULL,'zai','bai','2264545662345632',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,'mail','mail@mail.com','a9b5723dba225331f6c1c9118384c9b6',NULL,NULL,NULL,NULL,'name','lastName','634683485697',NULL,NULL,NULL,'0',NULL,NULL,NULL,NULL,NULL),(5,'testche','testche@mail.bg','1f2e3bea8bcb0c4b1d6e2b4d6d1cae9f',NULL,NULL,NULL,NULL,'zai','bai','2264545662345632',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,'poshta','poshta@posh.ta','a9b5723dba225331f6c1c9118384c9b6',NULL,NULL,NULL,NULL,'ime','drugoIme','633993874679',NULL,NULL,NULL,'123456',NULL,NULL,NULL,NULL,NULL),(19,'my123','my123@mail.com','a9b5723dba225331f6c1c9118384c9b6',NULL,NULL,NULL,NULL,'nMe','nMe','634685672952',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(20,'mailqwerty','mailqwerty@mail.com','a9b5723dba225331f6c1c9118384c9b6',NULL,NULL,NULL,NULL,'name','name','634685877103',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(21,'mailytrewq','mailytrewq@mail.com','a9b5723dba225331f6c1c9118384c9b6',NULL,NULL,NULL,NULL,'nMe','namr','634686313432',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(22,'mail12','mail12@mail.com','a9b5723dba225331f6c1c9118384c9b6',NULL,NULL,NULL,NULL,'name','name','634686580607',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(23,'asd','asd@asd.asd','4243d92b0780f3bb47003a383b4390d9',NULL,NULL,NULL,NULL,'adsad','asdadsada','504050400000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(24,'asd7','asd@asd.asdd','4243d92b0780f3bb47003a383b4390d9',NULL,NULL,NULL,NULL,'asdadsa','asdaasd','535500000000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(35,'name','name@name.name','a9b5723dba225331f6c1c9118384c9b6',27,NULL,NULL,NULL,'name','name','634665141796',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(36,'fafla','fafla@mail.bg','72df6b51df73e50311627d711335e9ea',-68,NULL,NULL,20,'Mityo','Krika','2264545662345632',NULL,NULL,NULL,'1233211','3211232',NULL,NULL,NULL,NULL),(37,'nikolovgeorgi86','nikolovgeorgi86@gmail.com','eeb77bacc710b525397bdd761c5e5f00',47,'m','aaa',88,'Georgi','Nikolov','0',NULL,'Images/Users/1501528896363.gif',1,'1612873225398158',NULL,'122',60,50,'about'),(38,'fafla1','fafla1@mail.bg','72df6b51df73e50311627d711335e9ea',-68,NULL,NULL,20,'Mityo','Krika','2264545662345632',NULL,NULL,NULL,'12332121','32121232',NULL,NULL,NULL,NULL),(39,'zaiko_bai123ko123','zaiko_bai123ko123@gmail.bg','72df6b51df73e50311627d711335e9ea',30,NULL,NULL,20,'zai','bai','535081601637',NULL,NULL,NULL,'125235345','513134241',NULL,NULL,NULL,NULL),(40,'asd4','asd@asd@asd','a8f5f167f44f4964e6c998dee827110c',26,'m',NULL,78,'???','aaaaa','661989600000','dhhdhfh',NULL,1,'1370102266377855',NULL,'54548454545484',422,122,''),(41,'muhozol','tralala@mail.bg','4ca3d6e9954740538a64015b05c99507',-68,'m','??. „????? ??????“ 44, 1505 ?????',8,'muhata','cece','163462243562472','????? ??? ????? - ?????? ? ???','Images/Users/1501411175259.png',1,'564785474','5647854741','234234225633',4,10,'fafa'),(42,'madted32','madted32@gmail.com','2959a2b032d514f70c4708485dc9b935',27,NULL,NULL,20,'georgi','nikolov','634641518747',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(43,'madted2','madted2@gmail.com','a9b5723dba225331f6c1c9118384c9b6',27,'m','aaa',46,'georgi','nikolov','636285465257',NULL,NULL,1,NULL,NULL,'123',20,50,'aaa'),(44,'chavdar.nedialkov','chavdar.nedialkov@gmail.com','b1964e826b37068a23729ef4f8d03421',24,NULL,NULL,43,'Chavdar','Nedialkov','725493600000',NULL,NULL,1,NULL,'102470371818292026078','78545545455',8,12,''),(45,'qq','qq@qq.qq','343b1c4a3ea721b2d640fc8700db0f36',25,NULL,NULL,67,'qqqqq\n','aaaaa','693439200000',NULL,NULL,1,NULL,NULL,'5784845454',2,3,''),(46,'aa','aa@aa.aa','0b4e7a0e5fe84ad35fb5f95b9ceeac79',37,NULL,NULL,0,'Ch','N','314834400000',NULL,NULL,1,NULL,NULL,'125484845454',12,5,''),(47,'zz','zz@zz.zz','453e41d218e071ccfb2d1c99ce23906a',25,NULL,NULL,0,'zz','zzz','693439200000',NULL,NULL,1,NULL,NULL,'87545454',5,5,''),(48,'ww','ww@ww.ww','d785c99d298a4e9e6e13fe99e602ef42',26,NULL,NULL,62,'ww','ww','661989600000',NULL,'Images/Users/1501621316675.jpg',1,NULL,NULL,'5785787575456',10,5,''),(49,'torbalan1','torbalan1@mail.bg','72df6b51df73e50311627d711335e9ea',-68,NULL,NULL,20,'ttt','rr','6264545662345632',NULL,NULL,NULL,'112233445566','665544332211',NULL,NULL,NULL,NULL),(50,'hristo.ivanov','hristo.ivanov@gmail.com','343b1c4a3ea721b2d640fc8700db0f36',37,NULL,NULL,20,'Hristo ','Ivanov','314748000000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(51,'noexile','noexile@gmail.com','2ac48611c3f92cdaabdb985e7367f7d6',-68,NULL,NULL,20,'fafla','faflena','7264545662345632',NULL,NULL,NULL,'888999','999888',NULL,NULL,NULL,NULL),(52,'chavdar.nedialkov.cr','chavdar.nedialkov.cr@gmail.com','d41d8cd98f00b204e9800998ecf8427e',21,NULL,NULL,20,'Chavdar','Nedialkov','819842400000',NULL,NULL,NULL,NULL,'106796897718041336982',NULL,NULL,NULL,NULL),(53,'faflat','faflat@gmail.coma','285eecca420a40fbbcc3bc682f772176',38,'m','home',1,'sasho','powera','283384800000',NULL,'Images/Users/1501694103418.jpg',1,NULL,NULL,'08888888888',5,9,''),(54,'kallzark','kallzark@gmail.com','215b8d59727401ac48a3bd4cda95d75d',31,NULL,NULL,0,'K','Z','504136800000','??',NULL,1,NULL,NULL,'123',5,20,''),(55,'l4o','l4o@abv.bg','22d31e8e44022e602833da72024e4c15',29,'m',NULL,0,'????','????','567640800000',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,''),(56,'kaloyan','kaloyan@hotmail.com','0a0d24c6e7fcdb0c7daf86f322f654fd',30,'m','sofia',0,'kal','z','533743586892',NULL,'Images/Users/1501765959129.jpg',1,NULL,NULL,'6464282828',5,20,'ye'),(57,'l4o4','l4o@abv.bg1','22d31e8e44022e602833da72024e4c15',23,NULL,NULL,150,'lll','llllachezar1petrov@gmail.com','756943200000',NULL,'Images/Users/1501766261910.jpg',1,NULL,NULL,'0889378890',5,10,''),(58,'mop_cornholio','mop_cornholio@yahoo.com','d785c99d298a4e9e6e13fe99e602ef42',20,NULL,NULL,20,'aaa','aaa','851292000000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(59,'krasi','krasi@abv.bg','317818700c6f16370f7e1ebec547c744',30,'m','aaa',20,'krasi','stoev','542922965098',NULL,NULL,1,NULL,NULL,'123',20,50,'aaa'),(60,'mailche','mailche@poshta.mail','a9b5723dba225331f6c1c9118384c9b6',27,NULL,NULL,20,'name','ime','635073694364',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(61,'name1','name@mail123.com','a9b5723dba225331f6c1c9118384c9b6',27,NULL,NULL,20,'name','name','621791150668',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(62,'maila1234','maila1234@mail.com','a9b5723dba225331f6c1c9118384c9b6',27,NULL,NULL,20,'name1','name2','634677855676',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(63,'kalo','azz_holler@yahoo.com','44912c33e5280729cb76a03025b12118',30,'m','sofia',0,'k','z','533763435000','yea','Images/Users/1502107400678.jpg',1,NULL,NULL,'088888888',5,20,'yea'),(64,'joriksbg','joriksbg@gmail.com','d49bb7c135e72bf07f3b6116a53b53f8',27,'m','Sofia ',0,'George','Sotirov','638966479817',NULL,'Images/Users/1502126808469.jpg',1,NULL,NULL,'0882432374',4,3,'Info'),(65,'bhzhs','bhzhs@hshs.com','dcf877b18896c5fa70fbbd4dc9c8adb0',27,'m','sofia\n',53,'todor','todorov','635087890939',NULL,NULL,1,NULL,NULL,'08484848484',36,5,'bla bla'),(66,'lebeaumire','lebeaumire@gmail.com','d41d8cd98f00b204e9800998ecf8427e',27,'m','??',0,'Lyubomir','Tsankov','636847200000','???','Images/Users/1502295347011.jpg',1,NULL,'118135321880299968665','8558885',5,5,''),(67,'traktorist11','mashinata_na_selo@mail.bg','4ca3d6e9954740538a64015b05c99507',-68,'m','ul. Zemedelska',4,'Mityo','Krika','6121341515151','male male','Images/Users/1502132205567.png',1,NULL,NULL,'23423423',4,42,'ole male'),(68,'mashterka','mashterka@gmail.bg','72df6b51df73e50311627d711335e9ea',-68,NULL,NULL,20,'fafla','faflena','7264545662345632',NULL,'Images/Users/1502132253172.png',NULL,NULL,NULL,NULL,NULL,NULL,NULL),(69,'mashterka132132','mashterka132132@gmail.bg','72df6b51df73e50311627d711335e9ea',-68,NULL,NULL,20,'fafla','faflena','7264545662345632',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(70,'name7','name@name.bg','6a4e23a44ed6a8ee037b6a2306a8bff5',30,'m','aaa',20,'name','name','542922965098',NULL,NULL,1,NULL,NULL,NULL,20,50,'aaa'),(71,'name10','name1@name.bg','6a4e23a44ed6a8ee037b6a2306a8bff5',27,NULL,NULL,0,'nMe','nMe','634682988814',NULL,NULL,1,NULL,NULL,'123',50,20,NULL),(72,'evil','evil@echo.over','0a7ffc563cd989b134d06aef728cd674',27,NULL,NULL,20,'hhh','fgg','634683169714',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(73,'nMe','nMe@name.comec','84873407c4f1a32472b32f75d1f7e9c2',27,NULL,NULL,20,'name','nMe','634686782656',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(74,'mangoosefx','mangoosefx@gmail.com','3a128d60073403ab962c41e8105cbc64',26,NULL,NULL,11,'KONY','Trendafilove','661166283911',NULL,NULL,1,NULL,NULL,'0889686846',14,4,NULL),(75,'ivan.gachmov','ivan.gachmov@gmail.com','fc07d9e9c4f490398c57c6955ffef6a4',23,NULL,NULL,150,'Ivan','Gachmov','755767047625',NULL,NULL,1,NULL,NULL,'0885242123',5,10,NULL),(76,'slav','slav@spiritinvoker.com','66ed61f32365c5c66c6797609cd37839',29,NULL,NULL,0,'????','????????','567640800000',NULL,NULL,0,NULL,NULL,NULL,NULL,NULL,''),(77,'joriksbg9','joriksbg@gmai.com','b46cfe70ccedca93ed4f06f5e5901fea',27,NULL,NULL,0,'George','Sotirov','634673328369',NULL,NULL,1,NULL,NULL,'852364',2,0,NULL),(78,'test_user','test_user@gmail.bg','9da1f8e0aecc9d868bad115129706a77',-68,NULL,NULL,20,'fafla','faflena','7264545662345632',NULL,NULL,NULL,'111222333','333222111',NULL,NULL,NULL,NULL),(79,'test_trainer','test_trainer@gmail.bg','cab1b1beaee3cc6ee11af3548403e88d',-68,NULL,NULL,20,'golyama','muha','7264545662345632',NULL,NULL,1,'444555666','666555444',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_attend_events`
--

DROP TABLE IF EXISTS `users_attend_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users_attend_events` (
  `user_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`event_id`),
  KEY `event_attend_fk_idx` (`event_id`),
  CONSTRAINT `event_attend_fk` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `user_attend_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_attend_events`
--

LOCK TABLES `users_attend_events` WRITE;
/*!40000 ALTER TABLE `users_attend_events` DISABLE KEYS */;
INSERT INTO `users_attend_events` VALUES (43,1),(48,1),(59,1),(66,1),(74,1),(43,2),(48,2),(57,2),(59,2),(43,3),(58,3),(47,20),(40,22),(48,23),(48,24),(57,25),(37,26),(40,26),(48,26),(57,26),(59,26),(65,26),(57,31),(66,31),(66,35),(63,49);
/*!40000 ALTER TABLE `users_attend_events` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-14 21:30:11
