CREATE TABLE `leaps`.`configuration` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `key` VARCHAR(250) NULL,
  `value` VARCHAR(250) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC));