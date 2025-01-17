CREATE TABLE `spark`.`data_source` (
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(45) NULL,
  `DESCRIPTION` VARCHAR(500) NULL,
  `PATH` VARCHAR(100) NULL,
  `DATA_CENTER` VARCHAR(45) NULL,
  `TYPE` VARCHAR(10) NULL,
  `STATUS` VARCHAR(25) NULL,
  `MICRO_SERVICE_ID` INT(11) NULL,
  PRIMARY KEY (`ID`),
  INDEX `fk_micro_service_id_idx` (`MICRO_SERVICE_ID` ASC),
  CONSTRAINT `fk_micro_service_id`
    FOREIGN KEY (`MICRO_SERVICE_ID`)
    REFERENCES `spark`.`micro_service` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE CASCADE)
DEFAULT CHARACTER SET = latin1;
