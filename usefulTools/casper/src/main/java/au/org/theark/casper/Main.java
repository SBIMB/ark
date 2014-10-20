package au.org.theark.casper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;



public class Main {
		
	private  static final String ARK_SUBJECT_ATTACHEMENT_DIR="attachments";
	
	private static final String ARK_SUBJECT_CORRESPONDENCE_DIR="correspondence";
	
	private static final String SELECT_FILE_ATTACHMENT ="select sf.ID, sf.FILENAME, lss.STUDY_ID, lss.SUBJECT_UID,sf.PAYLOAD \n" + 
			"from study.subject_file sf \n" + 
			"	inner join study.link_subject_study lss on lss.ID = sf.LINK_SUBJECT_STUDY_ID \n" + 
			"where sf.id > ? \n" + 
			"	and sf.PAYLOAD is not null \n" + 
			"order by sf.ID limit 1"; 
	
	private static final String UPDATE_FILE_ATTACHMENT = "update study.subject_file sf \n" + 
			"set sf.file_id = ? \n" + 
			"where sf.id = ? ";
	
	private static final String SELECT_CORRESPONDANCE_ATTACHMENT ="select c.ID, c.ATTACHMENT_FILENAME, lss.STUDY_ID, lss.SUBJECT_UID,c.ATTACHMENT_PAYLOAD \n" + 
			"from study.correspondences c \n" + 
			"	inner join study.link_subject_study lss on lss.ID = c.LINK_SUBJECT_STUDY_ID \n" + 
			"where c.id > ? \n" + 
			"	and c.ATTACHMENT_PAYLOAD is not null \n" + 
			"order by c.ID limit 1";
	
	private static final String UPDATE_CORRESPONDANCE_ATTACHMENT="update study.correspondences c \n" + 
			"set c.ATTACHMENT_FILE_ID= ? \n" + 
			"where c.ID = ?";
	
	private static final String CASPER_PROPERTIES_FILE="casper.properties";
	
	private static Logger log = Logger.getLogger(Main.class.getName()) ;
	
	private static String dbUrl = "";
	private static String dbClass = "";
	private static String username = "";
	private static String password = "";
	
	private static String fileAttachmentDir="/tmp";

	public static void main(String[] args) throws Exception {
		Main m = new Main();	
		Connection con =getConnection();
		m.migrateSubjectFiles(con);
		m.migrateCorrespondenceFiles(con);
		closeConnection(con);
	}
	
	public Main() throws Exception{
		Properties prop =new Properties();
		InputStream input = getClass().getClassLoader().getResourceAsStream(Main.CASPER_PROPERTIES_FILE);
		prop.load(input);
		
		Main.dbUrl = prop.getProperty("db.url");
		Main.dbClass = prop.getProperty("db.class");
		Main.username = prop.getProperty("db.username");
		Main.password = prop.getProperty("db.password");
		Main.fileAttachmentDir = prop.getProperty("file.attachment.dir");
		
	}

	private static Connection getConnection() {
		Connection con = null;
		try {
			Class.forName(dbClass);
			con = DriverManager.getConnection(dbUrl, username, password);
		} catch (ClassNotFoundException e) {
			log.severe(e.toString());
		} catch (SQLException e) {
			log.severe(e.toString());
		}
		return con;
	}
	
	private static void closeConnection(Connection con) {
		try{
			con.close();
		}catch(Exception e){
			log.severe(e.toString());
		}
	}

//	public void testConnect() {
//		
//		String query = "Select distinct(table_name) from INFORMATION_SCHEMA.TABLES";
//		
//		try {
//
//			Class.forName(dbClass);
//			Connection connection = DriverManager.getConnection(dbUrl, username, password);
//			Statement statement = connection.createStatement();
//			ResultSet resultSet = statement.executeQuery(query);
//			while (resultSet.next()) {
//				String tableName = resultSet.getString(1);
//				System.out.println("Table name : " + tableName);
//			}
//			connection.close();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
	
	
	public void migrateSubjectFiles(Connection con)throws Exception{
		int id = 0;
		PreparedStatement selectPS = con.prepareStatement(Main.SELECT_FILE_ATTACHMENT);
		PreparedStatement updatePS = con.prepareStatement(Main.UPDATE_FILE_ATTACHMENT);
		migrate(id, selectPS, updatePS, Main.ARK_SUBJECT_ATTACHEMENT_DIR);
	}
	
	public void migrateCorrespondenceFiles(Connection con)throws Exception{
		int id = 0;
		PreparedStatement selectPS = con.prepareStatement(Main.SELECT_CORRESPONDANCE_ATTACHMENT);
		PreparedStatement updatePS = con.prepareStatement(Main.UPDATE_CORRESPONDANCE_ATTACHMENT);
		migrate(id, selectPS, updatePS, Main.ARK_SUBJECT_CORRESPONDENCE_DIR);
	}

	private void migrate(int id, PreparedStatement selectPS, PreparedStatement updatePS, String baseDir) throws Exception {
		
		int previousId = id;
		String fileName = null;
		long studyId = 0;
		String subjectUID=null;
		byte[] payload = null;

		String fileId=null;
		
		selectPS.setInt(1, id);
		ResultSet rs = selectPS.executeQuery();

		if (rs.next()) {
			id = rs.getInt(1);
			fileName = rs.getString(2);
			studyId = rs.getInt(3);
			subjectUID = rs.getString(4);
			payload = rs.getBytes(5);
			
			fileId = generateArkFileId(fileName);
			
			saveArkFileAttachment(studyId, subjectUID, baseDir, fileName, payload, fileId);
			
			//Clear payload 
			payload = null;
			
			updatePS.setString(1, fileId);
			updatePS.setInt(2, id);
			updatePS.executeUpdate();
		}
		if(previousId != id){
			migrate(id, selectPS, updatePS,baseDir);
		}
	}
	
	private void saveArkFileAttachment(final Long studyId, final String subjectUID, final String directoryType, final String fileName, final byte[] payload, final String fileId) {

		String directoryName = getArkFileDirName(studyId, subjectUID, directoryType);

		File fileDir = new File(directoryName);

		if (!fileDir.exists()) {
			boolean result = false;
			try {
				fileDir.mkdirs();
				result = true;
			}
			catch (SecurityException se) {
				log.severe("Do not have the sufficient permission to access the file directory");
			}
			if (result) {
				log.info("DIR created successfully " + directoryName);
			}
		}
		createFile(directoryName, fileId, payload);
	}
	
	private void createFile(final String directory, final String fileId, final byte[] payload) {
		try {
			File file = new File(directory + File.separator + fileId);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(payload);
			fos.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	private String getArkFileDirName(final Long studyId, final String subjectUID, final String directoryType) {
		String directoryName = this.fileAttachmentDir + File.separator + studyId + java.io.File.separator + directoryType +  File.separator + subjectUID ;
		return directoryName;
	}
	
	private String generateArkFileId(String fileName) {
		return System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + fileName;
	}
	
}