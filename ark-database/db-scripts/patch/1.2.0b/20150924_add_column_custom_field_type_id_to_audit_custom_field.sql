use audit;
ALTER TABLE `audit`.`aud_custom_field` 
ADD COLUMN `CUSTOM_FIELD_TYPE_ID` BIGINT(20) NULL AFTER `UNIT_TYPE_ID`;
