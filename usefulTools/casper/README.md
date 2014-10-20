INTRODUCTION:
-- The Casper project was created to migrate the existing file attachments in the Ark database to server local file system. 

-- This tool only copy the existing subject file attachments and correspondence file attachments to local file system in the execution machine.

-- Later the database administrator can permanently remove these attachments from the Ark database.

STEPS:      

(1) Update src/main/resources/casper.properties 

(2) Create casper JAR with dependencies:

mvn clean compile assembly:single

(3) Execute JAR file: 

java -jar casper-1.0-SNAPSHOT-jar-with-dependencies.jar