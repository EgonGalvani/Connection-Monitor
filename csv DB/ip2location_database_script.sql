CREATE TABLE `Ip2LocationTable`(
    `ip_from` INT(10),
    `ip_to` INT(10),
    `country_code` CHAR(2),
    `country_name` VARCHAR(64),
    `region_name` VARCHAR(128),
    `city_name` VARCHAR(128),
    `latitude` DOUBLE,
    `longitude` DOUBLE,
	
    PRIMARY KEY (`ip_to`)
); 