USE study;

alter table study.upload add column STATUS ENUM('UPLOADING', 'COMPLETE', 'ERRORS');

CREATE TABLE `upload_error`(
    `ID` int(11) NOT NULL,
    `UPLOAD_ID` int(11) DEFAULT NULL,
    `ERROR_MSG` varchar(256) DEFAULT NULL,
    `ROW_NUMBER` int(11) DEFAULT NULL,
    `ORIGINAL_ROW_DATA` text,
    PRIMARY KEY (`ID`),
    KEY `FK_UPLOAD_ERROR_UPLOAD` (`UPLOAD_ID`),
    CONSTRAINT `FK_UPLOAD_ERROR_UPLOAD` FOREIGN KEY (`UPLOAD_ID`)
        REFERENCES `upload` (`ID`)
        ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

