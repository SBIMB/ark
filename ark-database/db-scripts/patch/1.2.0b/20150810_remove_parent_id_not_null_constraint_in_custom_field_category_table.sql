ALTER TABLE `study`.`custom_field_category` 
DROP FOREIGN KEY `FK_CUSTOMFIELDCATEGORY_PARENT_ID`;
ALTER TABLE `study`.`custom_field_category` 
CHANGE COLUMN `PARENT_ID` `PARENT_ID` INT(11) NULL ;
ALTER TABLE `study`.`custom_field_category` 
ADD CONSTRAINT `FK_CUSTOMFIELDCATEGORY_PARENT_ID`
  FOREIGN KEY (`PARENT_ID`)
  REFERENCES `study`.`custom_field_category` (`ID`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
