-- --------------------------------------------------------
-- Host:                         91.238.251.154
-- Server version:               5.7.26-0ubuntu0.16.04.1 - (Ubuntu)
-- Server OS:                    Linux
-- HeidiSQL Version:             9.5.0.5196
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Dumping database structure for leaps
CREATE DATABASE IF NOT EXISTS `leaps` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `leaps`;

-- Dumping structure for table leaps.custom_tags
CREATE TABLE IF NOT EXISTS `custom_tags` (
  `tag_id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`tag_id`),
  KEY `custom_tags_fk_idx` (`event_id`),
  CONSTRAINT `custom_tags_fk` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table leaps.custom_tags: ~0 rows (approximately)
/*!40000 ALTER TABLE `custom_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `custom_tags` ENABLE KEYS */;

-- Dumping structure for table leaps.events
CREATE TABLE IF NOT EXISTS `events` (
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
  `firebase_topic` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`event_id`),
  KEY `owner_fk_idx` (`owner_id`),
  CONSTRAINT `owner_fk` FOREIGN KEY (`owner_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=347 DEFAULT CHARSET=utf8;

-- Dumping data for table leaps.events: ~11 rows (approximately)
/*!40000 ALTER TABLE `events` DISABLE KEYS */;
INSERT INTO `events` (`event_id`, `title`, `description`, `date`, `time_from`, `time_to`, `owner_id`, `coord_lat`, `coord_lnt`, `price_from`, `address`, `free_slots`, `date_created`, `event_image_url`, `firebase_topic`) VALUES
	(327, 'Swimming', 'Refreshing curse for beginners', '1533729605169', '1533729605169', '0', 234, 42.63823765460611, 23.292279951274395, 5, 'ул. „Карл Джерси“, Bulgaria', 20, '1532087954520', 'Images/Events/1532087955372.jpg', NULL),
	(329, 'outdoor swimming', 'test event', '1532199300000', '1532199300000', '1532202900000', 235, 42.66127341700386, 23.443994410336018, 0, 'ул. „Циклама, 1532 Sofia, Bulgaria', 10, '1532109435473', 'Images/Events/1532109435657.jpg', NULL),
	(330, 'Moutain climbing', 'Refreshing training for climbers', '1533382326995', '1533382326995', '0', 234, 42.74370766480237, 23.245421424508095, 2, 'ul. "Shopska kitka", Bulgaria', 20, '1532111548955', 'Images/Events/1532111549813.jpg', NULL),
	(331, 'hiking', 'hiking test', '1532292180000', '1532292180000', '1532295780000', 235, 39.959200234645344, 23.91726717352867, 10, 'Epar.Od. Nikitis-Sartis, Sithonia 630 72, Greece', 20, '1532115904395', 'Images/Events/1532115908340.jpg', NULL),
	(332, 'Moutain', 'Climbing', '1532465363711', '1532465363711', '0', 233, 42.64147452983244, 23.293001800775528, 2, 'ul. "Kumata" 101, Sofia, Bulgaria', 22, '1532119776029', 'Images/Events/1532119776175.jpg', NULL),
	(335, 'Running', 'Running in the mountain', '1532964049000', '1532964049000', '1532967649000', 242, 42.67892952427383, 23.311816826462746, 5, 'ul. "Doctor Stefan Sarafov" 7, 1408 Ivan Vazov, Sofia, Bulgaria', 50, '1532960450354', 'Images/Events/1532960455336.jpg', NULL),
	(337, 'Асдг', 'Асдф', '1533480644026', '1533480644026', '0', 237, 42.703410312807364, 23.35097976028919, 22, 'ul. "Macgahan" 55Г Б, Sofia, Bulgaria', 22, '1533221448626', 'Images/Events/1533221448737.jpg', NULL),
	(338, 'Gg', 'Gg', '1533289976063', '1533289976063', '0', 244, 42.66608520460392, 23.287379890680313, 5, 'ul. "Lelinska chuka", Sofia, Bulgaria', 4, '1533289982070', 'Images/Events/1533289982173.jpg', NULL),
	(341, 'Swimming', 'In the city', '1533382853662', '1533382853662', '0', 232, 42.62454911876977, 23.377258703112602, 11, 'Sofia', 10, '1533296344153', 'Images/Events/1533296344426.jpg', NULL),
	(342, 'Kitesurfung lesson', 'Learn to kitesurf', '1534538520000', '1534538520000', '1534614120000', 238, 43.20190610211104, 27.92384874075651, 50, 'Любен Каравелов 1, 9002 Primorski, Varna, Bulgaria', 20, '1534362245112', NULL, NULL),
	(343, 'Kitesurfung lesson', 'Learn to kitesurf', '1534538520000', '1534538520000', '1534614180000', 238, 43.20190610211104, 27.92384874075651, 50, 'Любен Каравелов 1, 9002 Primorski, Varna, Bulgaria', 20, '1534362251508', NULL, NULL),
	(344, 'Swimming', 'At the pool', '1534920336817', '1534920336817', '0', 232, 42.646488098383, 23.395638875663284, 5, 'Capital Fort, Sofia, Bulgaria', 12, '1534833886626', 'Images/Events/1534833886921.jpg', NULL),
	(345, 'Roadrun', 'Running', '1544020307665', '1544020307665', '0', 234, 42.64044631485871, 23.294197395443916, 12, 'ul. "Boyanska" 32, Ботаническа градина, Bulgaria', 8, '1542205915348', 'Images/Events/1542205915430.jpg', NULL),
	(346, 'Ski', 'Borovec', '1554533085185', '1554533085185', '0', 232, 42.64677589341534, 23.395580872893333, 15, 'Boulevard "Tsarigradsko shose" 90, Sofia, Bulgaria', 55, '1554209097712', 'Images/Events/1554209097978.jpg', NULL);
/*!40000 ALTER TABLE `events` ENABLE KEYS */;

-- Dumping structure for table leaps.event_followers
CREATE TABLE IF NOT EXISTS `event_followers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Unique_pair` (`event_id`,`user_id`),
  KEY `event_id_idx` (`event_id`),
  KEY `user_id_idx` (`user_id`),
  CONSTRAINT `event_id` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=762 DEFAULT CHARSET=latin1;

-- Dumping data for table leaps.event_followers: ~4 rows (approximately)
/*!40000 ALTER TABLE `event_followers` DISABLE KEYS */;
INSERT INTO `event_followers` (`id`, `event_id`, `user_id`) VALUES
	(760, 327, 242),
	(758, 329, 234),
	(761, 330, 242),
	(759, 330, 248);
/*!40000 ALTER TABLE `event_followers` ENABLE KEYS */;

-- Dumping structure for table leaps.event_has_tags
CREATE TABLE IF NOT EXISTS `event_has_tags` (
  `tag_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  KEY `event_tags_kf_idx` (`event_id`),
  KEY `tag_events_fk` (`tag_id`),
  CONSTRAINT `event_tags_fk` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `tag_events_fk` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`tag_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=277 DEFAULT CHARSET=utf8;

-- Dumping data for table leaps.event_has_tags: ~11 rows (approximately)
/*!40000 ALTER TABLE `event_has_tags` DISABLE KEYS */;
INSERT INTO `event_has_tags` (`tag_id`, `event_id`, `id`) VALUES
	(126, 331, 263),
	(127, 331, 264),
	(118, 332, 265),
	(128, 332, 266),
	(129, 332, 267),
	(126, 335, 271),
	(116, 335, 272),
	(118, 335, 273),
	(128, 335, 274),
	(111, 335, 275),
	(116, 338, 276);
/*!40000 ALTER TABLE `event_has_tags` ENABLE KEYS */;

-- Dumping structure for table leaps.event_images
CREATE TABLE IF NOT EXISTS `event_images` (
  `image_id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) NOT NULL,
  `file_name` varchar(45) NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `event_images_fk_idx` (`event_id`),
  CONSTRAINT `event_images_fk` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=170 DEFAULT CHARSET=utf8;

-- Dumping data for table leaps.event_images: ~7 rows (approximately)
/*!40000 ALTER TABLE `event_images` DISABLE KEYS */;
INSERT INTO `event_images` (`image_id`, `event_id`, `file_name`) VALUES
	(157, 327, 'Images/Events/1532087956122.jpg'),
	(159, 330, 'Images/Events/1532111550693.jpg'),
	(160, 332, 'Images/Events/1532119776298.jpg'),
	(165, 341, 'Images/Events/1533296344679.jpg'),
	(166, 344, 'Images/Events/1534833887163.jpg'),
	(167, 345, 'Images/Events/1542205915483.jpg'),
	(168, 345, 'Images/Events/1542205915584.jpg'),
	(169, 346, 'Images/Events/1554209098174.jpg');
/*!40000 ALTER TABLE `event_images` ENABLE KEYS */;

-- Dumping structure for table leaps.event_rating
CREATE TABLE IF NOT EXISTS `event_rating` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `rating` int(11) NOT NULL,
  `comment` varchar(100) NOT NULL,
  `date_created` mediumtext NOT NULL,
  `rating_image_url` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `event_id_idx` (`event_id`),
  KEY `user_id_idx` (`user_id`),
  CONSTRAINT `rate_event_id` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `rate_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping data for table leaps.event_rating: ~0 rows (approximately)
/*!40000 ALTER TABLE `event_rating` DISABLE KEYS */;
/*!40000 ALTER TABLE `event_rating` ENABLE KEYS */;

-- Dumping structure for table leaps.repeating_events
CREATE TABLE IF NOT EXISTS `repeating_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) DEFAULT NULL,
  `parent_event_id` int(11) NOT NULL,
  `event_start_time` mediumtext NOT NULL,
  `event_end_time` mediumtext NOT NULL,
  `exist` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `event_id_UNIQUE` (`event_id`),
  CONSTRAINT `parent_event_id` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Dumping data for table leaps.repeating_events: ~0 rows (approximately)
/*!40000 ALTER TABLE `repeating_events` DISABLE KEYS */;
/*!40000 ALTER TABLE `repeating_events` ENABLE KEYS */;

-- Dumping structure for table leaps.specialties
CREATE TABLE IF NOT EXISTS `specialties` (
  `specialty_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`specialty_id`),
  UNIQUE KEY `specialty_id_UNIQUE` (`specialty_id`),
  KEY `user_specialties_fk_idx` (`user_id`),
  CONSTRAINT `user_specialties_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

-- Dumping data for table leaps.specialties: ~0 rows (approximately)
/*!40000 ALTER TABLE `specialties` DISABLE KEYS */;
INSERT INTO `specialties` (`specialty_id`, `user_id`, `name`) VALUES
	(6, 234, 'football');
/*!40000 ALTER TABLE `specialties` ENABLE KEYS */;

-- Dumping structure for table leaps.tags
CREATE TABLE IF NOT EXISTS `tags` (
  `tag_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=130 DEFAULT CHARSET=utf8;

-- Dumping data for table leaps.tags: ~16 rows (approximately)
/*!40000 ALTER TABLE `tags` DISABLE KEYS */;
INSERT INTO `tags` (`tag_id`, `name`) VALUES
	(126, 'beach mountain hiking'),
	(121, 'breathe'),
	(127, 'breeze'),
	(116, 'Cardio'),
	(118, 'climbing'),
	(129, 'fresh air'),
	(125, 'freshair'),
	(122, 'gain'),
	(128, 'hiking'),
	(113, 'Jogging'),
	(114, 'Jumping'),
	(124, 'mountain'),
	(119, 'moutain'),
	(123, 'no stress'),
	(111, 'Running'),
	(120, 'swim'),
	(112, 'Swimming'),
	(115, 'Weight lifting');
/*!40000 ALTER TABLE `tags` ENABLE KEYS */;

-- Dumping structure for table leaps.users
CREATE TABLE IF NOT EXISTS `users` (
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
  `facebook_id` varchar(200) DEFAULT NULL,
  `google_id` varchar(200) DEFAULT NULL,
  `phone_number` varchar(100) DEFAULT NULL,
  `years_of_training` int(11) DEFAULT NULL,
  `session_price` int(11) DEFAULT NULL,
  `long_description` varchar(3000) DEFAULT NULL,
  `firebase_token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username_UNIQUE` (`username`),
  UNIQUE KEY `email_address_UNIQUE` (`email_address`),
  UNIQUE KEY `facebook_id_UNIQUE` (`facebook_id`),
  UNIQUE KEY `google_id_UNIQUE` (`google_id`)
) ENGINE=InnoDB AUTO_INCREMENT=258 DEFAULT CHARSET=utf8;

-- Dumping data for table leaps.users: ~24 rows (approximately)
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`user_id`, `username`, `email_address`, `password`, `age`, `gender`, `location`, `max_distance_setting`, `first_name`, `last_name`, `birthday`, `description`, `profile_image_url`, `is_trainer`, `facebook_id`, `google_id`, `phone_number`, `years_of_training`, `session_price`, `long_description`, `firebase_token`) VALUES
	(232, 'joriksbg', 'joriksbg@gmail.com', 'a60ef0b9fa6b60596f64f8c86de5ab18', 28, NULL, NULL, 20, 'Joro', 'Sotirov', '638947315834', NULL, NULL, 1, '10216540740934732', NULL, '0882432374', 0, 0, NULL, 'cFZjB7xdSm0:APA91bFl0LazOg_GZ3skvbDTKgiV9Cf-Vnw_P6iEiIlkf4-Vm8glxbfoq529X_9di3PbdbP1IKDyNYtXwppbjoDUldnndy61zmgVMjyQhGLqYeS9dko5zJI5oeDGlg-GrY4OT2eIr4cc'),
	(233, 'iwannalogin', 'iwannalogin@abv.bg', '333cb76f7254dfc20d82c62696b79da4', 36, 'f', 'Sofia\n', 20, 'Ivan', 'Ivanov', '396004660155', NULL, 'Images/Users/1532119647623.jpg', 1, '102138497382520', NULL, '0885322363', 20, 2, ' fatty', 'cV1AyHfu4xA:APA91bEl5oRvFA99yxxbxnlGBOeHoKSvnlmsXa-aBzhSlgydXOBONEqnJkHOb-YXnBW42a_fST2GDLJUz9ti0oOf2mG_8uxELQ-It0IchR_Xx7ET0mIlTeoc4FUVXY62C---bofvRPX7SpJSQRPgxxUQEG2lKU907w'),
	(234, 'big_papas', 'big_papas@abv.bg', 'fc07d9e9c4f490398c57c6955ffef6a4', 24, 'm', 'Sofia', 20, 'Ivan', 'Gachmov', '755787128024', NULL, 'Images/Users/1532511101483.jpg', 1, '1803272233060744', NULL, '0886575457', 22, 2, 'Good trainer opa', 'dpSPsXQ_5Lo:APA91bFjB-h-7dZigh5XJB7eMt0NNC7G13Apvfi37Vd-kGz2qoWft2JAlC3cfxez978Prm1lQNpMPzx9_sml3hREQ2fWLZUzw_ZOv5BHxMhRzRbEiuUNtYkgLF6JfUEDs6No9h4ETeLL9XNjWO-UjxCHuDjj6eNZNA'),
	(235, 'noexile', 'noexile@gmail.com', 'faa95453ba3967308e819f789f546b0d', 31, NULL, NULL, 20, 'Alexander', 'Zorov', '535068000000', NULL, NULL, 1, NULL, NULL, '0888123456', 5, 0, '', NULL),
	(236, 'georgi.debelyanov', 'georgi.debelyanov@abv.bg', 'd41d8cd98f00b204e9800998ecf8427e', 28, NULL, NULL, 20, 'Gope', 'Debelyanov', '642027600000', NULL, NULL, NULL, '1958191237580464', NULL, NULL, NULL, NULL, NULL, NULL),
	(237, 'ivan.gachmov', 'ivan.gachmov@abv.bg', 'fc07d9e9c4f490398c57c6955ffef6a4', 30, NULL, NULL, 20, 'Ivan', 'Gachmov', '585652926172', NULL, NULL, 1, 'drai-qEMdWk:APA91bH9Tp0YODp4GE3cJ2oQD7CDCfCaMqDyFdczninjm3ep9DA53wO706AXLpKw-bQJuEI8Lf1XWC7AjR_lAKRQvdC6x5MlZt8c1iJ6AKo3tNIbsIIk6fQc_X2jLkBstPdLhmS64W6hlGi4iADQtogm8_RbMWwpaA', NULL, '088523565', 2, 22, NULL, 'fQMLGdCMk3U:APA91bEGORr89J3GPwerR53LLzaiQt1Nw94b55qvmspQGfNs6seUlqPH4ecnjqy5ODuxXcAkJlQKaXyulOsP_nyHMUzovqlpDny3um82HDx5xu7czpW9Csc3gD9Hrm0AOgtCKsxDy5yUez69121ZzkGrtqCDSFJAsQ'),
	(238, 'waterpolo_sf', 'waterpolo_sf@yahoo.com', 'd41d8cd98f00b204e9800998ecf8427e', 33, NULL, NULL, 20, 'Todor', 'Todorov', '485643600000', NULL, NULL, 1, '10213925594427596', NULL, '866565555', 25, 20, '', NULL),
	(239, 'hazardskis', 'hazardskis@gmail.com', 'd41d8cd98f00b204e9800998ecf8427e', 29, NULL, NULL, 20, 'Peter', 'Dimitrov', '617317200000', NULL, 'Images/Users/1532343756466.jpg', 1, '10156472641853498', NULL, '096432518', 20, 20, '', NULL),
	(240, 'slav', 'slav@spiritinvoker.com', '66ed61f32365c5c66c6797609cd37839', 18, NULL, NULL, 20, 'Slav', 'Sarafski', '964126800000', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
	(242, 'tsankov.lyubomir', 'tsankov.lyubomir@gmail.com', 'd41d8cd98f00b204e9800998ecf8427e', 28, NULL, NULL, 20, 'Lyubomir', 'Tsankov', '636847200000', NULL, 'Images/Users/1532960392876.jpg', 1, NULL, '103619074244293809112', '+359889919492', 5, 5, '', NULL),
	(243, 'ivan.gachmov3', 'ivan.gachmov@gmail.com', '333cb76f7254dfc20d82c62696b79da4', 26, NULL, NULL, 20, 'ivan', 'gachmov', '711990561047', NULL, NULL, 1, NULL, '100751112153726801945', '08852699852', 2, 22, NULL, 'fNrd5ZczuzU:APA91bHHIlKnw4Zwy1gJz0vzorg5LJbZDLxfi2sHXJrP_dS9cOK4ZK0skevmoXwaWH8WzhFaafiMJtPM34Yuvs_gksxphaIGmJmOA8JKVZAhYLMNBw2ejt5G5Qb55r122HhuOcoJJHMVho7bnGnKXo7coIJ1dGHdZA'),
	(244, 'krasimir.100ev', 'krasimir.100ev@gmail.com', '4a8da5f8c0ed5c5f45c2cd46d181afe9', 31, 'm', 'Gg', 20, 'Krasimir', 'Stoev', '542894499932', NULL, 'Images/Users/1533289912973.jpg', 1, '1146498735488192', NULL, '0878627022', 10, 5, 'Gg', 'dINcdaOwmDM:APA91bFZrVrerGyeR2-G5fy6lZLuF9m3oKvhBozBGMnZGm6yO9UbQa9CxAowBCrBnAmFzjOF0BfJilYQZOg4xGL4ncpj8YRP2PbQz9tyirnZONeM-6rPwFW1UyJwa2vzO4yb2MPTYBW39eIutHPndGhHMB3_X74pBw'),
	(245, 'mop_cornholio', 'mop_cornholio@yahoo.com', 'd41d8cd98f00b204e9800998ecf8427e', 34, NULL, NULL, 20, 'Chavdar', 'Nedialkov', '459637200000', NULL, NULL, NULL, '1370102266377855', NULL, NULL, NULL, NULL, NULL, NULL),
	(246, 'aleksandar950412', 'aleksandar950412@abv.bg', '33ec7b6706afc3adbdf9f9bd99c29ddd', 22, NULL, NULL, 20, 'Aleksandar', 'Grozdanov', '829333996277', NULL, NULL, NULL, '1700531953349273', NULL, NULL, NULL, NULL, NULL, 'djwBVuQMz7o:APA91bGvl8YiIxdu2HX3198jNTy-Y_RwZuYpufQQJS1bVH3CTzuTnEI_yJlmQAUYuCklyRKGFiU5Ulxg13PyQKhqgO_H0aJ45XJz5t0YJ2WtiKoIgOImoHwlLigW09XuwWCpXRtzY5yhqX2YlnNHgEiA9BFX1gDrtQ'),
	(247, 'peterzahov', 'peterzahov@gmail.com', 'c04273820f38592d0cbf005697646686', 24, NULL, NULL, 20, 'Peter', 'Zahov', '761861111046', NULL, NULL, NULL, '10212503611206816', NULL, NULL, NULL, NULL, NULL, 'dQey2rk1jXw:APA91bEtRb0qXyc-qvrO5KjNVv9uFknPO4ZsAssWNpZXYENlEqJNJqyWanFWjWDxLi9_XPUXHPvlJOstryLlj4Unx5VYik4g_pzSdSNevBwSJbHNpvsAaKLptyzAf7MWPHB6wiKCxje3R9xgOznd4c93jgMf3C-X2A'),
	(248, 'lora.b.ivanova', 'lora.b.ivanova@gmail.com', '5575fbe4a41a2572cd54ead6dadb638a', 24, NULL, NULL, 20, 'Lora', 'Ivanova', '762938330784', NULL, NULL, NULL, '1922631077758032', NULL, NULL, NULL, NULL, NULL, 'd3rPIq084WQ:APA91bESnSqSzuIwdW3yJcPSPM26o6wLVbup1TQ61npp7lqCKixH7kWyNAJEuRbcE3GWR5CXhGCKlbyPMCHAxR-IcWiYAC_8my9vpM2Am8Zn4c4C0SBxTUU8HLnmliopRapu-X6O3s1T35Wt365-08kXuWgcumiNHg'),
	(249, 'ivangachmov93', 'ivangachmov93@gmail.com', 'd288e13ad4bc75aaa098db64d39ae51f', 14, NULL, NULL, 20, 'Ivan', 'Gachmov', '1073646771345', NULL, NULL, 1, NULL, '114378451430078882908', '0886585254', 2, 222, NULL, 'cLe6C-7dFqw:APA91bFZnUZfFa4liYpckkjVZ5jvDRx_Bbofu3edUgWYNR8MiGWVdgI87BWM42quv5OJ2B9ZOWaaASROM_-mjP4-j5fv6e0zdNB-FcWyEj7DKwGlmOIyrTX72LRZCTD27AKPj5BfPaOzevO4z88J8TQ9dS-OcwX-GA'),
	(250, 'nikola.vasilev.d', 'nikola.vasilev.d@gmail.com', '8f83892fbfc606baad8b21a2770cb0ba', 25, NULL, NULL, 20, 'Никола', 'Василев', '739689467229', NULL, NULL, 1, NULL, NULL, '0988884169', 10, 2, NULL, 'fzCWTYQVCg8:APA91bHsheR-E1EKqYN1exJfmx6jfDB33OEEtDqzft4sU5kvyrSU6ecj0SOfJByt6eKYOxKkYn3pVwj17UDM8xG56Ht9ZThki9uJGtKrZ9jWib5gfb9LhV3lxrLdMKh2i8bqynt1nbsCzcMlYAaBYg45vJmCDtzkng'),
	(251, 'radoslav.rangelov', 'radoslav.rangelov@gmail.com', 'd41d8cd98f00b204e9800998ecf8427e', 34, NULL, NULL, 20, 'Radoslav', 'Rangelov', '455317200000', NULL, NULL, 1, '10214279656679174', NULL, '00359887214917', 6, 40, '', NULL),
	(252, 'stoev.e', 'stoev.e@gmail.com', 'd41d8cd98f00b204e9800998ecf8427e', 27, 'm', 'Sofia, Bulgaria', 20, 'emko', 'stoev', '653346000000', NULL, 'Images/Users/1533841279678.jpg', 1, NULL, '107967481984714528490', '0882432374', 2, 2, '', NULL),
	(253, 'zhasmina.aleks', 'zhasmina.aleks@gmail.com', '9a37d9cbee7482b2aa76baa90ed20f2b', 25, NULL, NULL, 20, 'Жасмина', 'Александрова', '740784883068', NULL, NULL, NULL, NULL, '105613639741862654524', NULL, NULL, NULL, NULL, 'fvFR9ogAi90:APA91bF3okzIAezwO1hE3YpmnYLvh62LmLjDPOEkBB7toc3H5pTdC-dSh6qPrbS3m6iceo9ifnc8IN_MPUKre6kFLRs_EWsUhNXQFJFj6Hi7VFEUC5VMtt9PQk6mzVV1IC4hOP7jRQuQ'),
	(254, 'george', 'george@leaps.club', '0126bdb19dcb63185a12e333174d02d9', 25, NULL, NULL, 20, 'Joro', 'Sotirov', '750002417961', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'fRkEun-I-mA:APA91bG0s6zbHzt8eYmM1cku-o7UX_2x71FLrVPXXls1ABSNoUiFIZNjpjw_fc4_Ixi1auMMmpi1YJLd6etAAERU5A5mRZlOhP_j67dBUE4anr_C4NpYJNMIOnBcTksZAdXSzgoQTE-v'),
	(255, 'iakhalif', 'iakhalif@utica.edu', 'd41d8cd98f00b204e9800998ecf8427e', 20, NULL, NULL, 20, 'Ibn', 'Khalif', '891579600000', NULL, NULL, NULL, NULL, '102089008968978837595', NULL, NULL, NULL, NULL, NULL),
	(256, 'manchevphoto', 'manchevphoto@gmail.com', 'd41d8cd98f00b204e9800998ecf8427e', 18, NULL, NULL, 20, 'Ivan', 'Manchev', '971816400000', NULL, NULL, NULL, '10155942173963358', NULL, NULL, NULL, NULL, NULL, NULL),
	(257, 'jiovannyss', 'jiovannyss@gmail.com', 'd41d8cd98f00b204e9800998ecf8427e', 46, NULL, NULL, 20, 'Ivan\n', 'Ivanov', '84146400000', NULL, NULL, NULL, NULL, '116276818568815695272', NULL, NULL, NULL, NULL, NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;

-- Dumping structure for table leaps.users_attend_events
CREATE TABLE IF NOT EXISTS `users_attend_events` (
  `user_id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`event_id`),
  KEY `event_attend_fk_idx` (`event_id`),
  CONSTRAINT `event_attend_fk` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `user_attend_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dumping data for table leaps.users_attend_events: ~6 rows (approximately)
/*!40000 ALTER TABLE `users_attend_events` DISABLE KEYS */;
INSERT INTO `users_attend_events` (`user_id`, `event_id`) VALUES
	(239, 327),
	(242, 327),
	(235, 329),
	(242, 330),
	(244, 330);
/*!40000 ALTER TABLE `users_attend_events` ENABLE KEYS */;

-- Dumping structure for table leaps.user_followers
CREATE TABLE IF NOT EXISTS `user_followers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `follower` int(11) NOT NULL,
  `followed` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Unique_pair` (`follower`,`followed`),
  KEY `follower_idx` (`follower`),
  KEY `followed_idx` (`followed`),
  CONSTRAINT `followed` FOREIGN KEY (`followed`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `follower` FOREIGN KEY (`follower`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2047 DEFAULT CHARSET=latin1;

-- Dumping data for table leaps.user_followers: ~6 rows (approximately)
/*!40000 ALTER TABLE `user_followers` DISABLE KEYS */;
INSERT INTO `user_followers` (`id`, `follower`, `followed`) VALUES
	(2031, 234, 235),
	(2046, 238, 235),
	(2036, 242, 233),
	(2037, 242, 234),
	(2043, 252, 232),
	(2042, 252, 242);
/*!40000 ALTER TABLE `user_followers` ENABLE KEYS */;

-- Dumping structure for table leaps.user_images
CREATE TABLE IF NOT EXISTS `user_images` (
  `image_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `file_name` varchar(45) NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `user_images_fk_idx` (`user_id`),
  CONSTRAINT `user_images_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=102 DEFAULT CHARSET=utf8;

-- Dumping data for table leaps.user_images: ~2 rows (approximately)
/*!40000 ALTER TABLE `user_images` DISABLE KEYS */;
INSERT INTO `user_images` (`image_id`, `user_id`, `file_name`) VALUES
	(101, 234, 'Images/Users/1532508303092.jpg');
/*!40000 ALTER TABLE `user_images` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
