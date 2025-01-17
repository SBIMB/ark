package au.org.spark.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.molgenis.genotype.Alleles;
import org.molgenis.genotype.Sample;
import org.molgenis.genotype.variant.GeneticVariant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.stereotype.Service;

import au.org.spark.factory.DefaultExecSessionFactory;
import au.org.spark.factory.ExecSession;
import au.org.spark.parser.SparkSftpBedBimFamGenotypeData;
import au.org.spark.parser.SparkSftpPedMapGenotypeData;
import au.org.spark.util.Constants;
import au.org.spark.web.view.AnalysisJobVo;
import au.org.spark.web.view.AnalysisVo;
import au.org.spark.web.view.ComputationVo;
import au.org.spark.web.view.DataCenterVo;
import au.org.spark.web.view.DataSourceVo;

import com.google.common.base.Splitter;
import com.gs.collections.impl.block.procedure.AtomicCountProcedure;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

@Service
public class SshServiceImpl implements SshService {

	@Value("${ssh.root.path}")
	private String rootPath;

	private static Pattern pattern = Pattern.compile("\\s");

	private static final Charset FILE_ENCODING = Charset.forName("UTF-8");

	@Autowired
	SftpSession sftpSession;
	
	@Autowired
	DefaultSftpSessionFactory sftpSessionFactory;

	@Autowired
	CassandraService cassandraService;

	// @Autowired
	// ExecSession execSession;

	@Autowired
	DefaultExecSessionFactory execSessionFactory;

	@Override
	public List<String> listFiles(String directory) throws Exception {
		ChannelSftp channelSftp = sftpSession.getClientInstance();

		List<String> files = new ArrayList<String>();
		channelSftp.cd(directory);
		List filelist = channelSftp.ls(directory);
		for (int i = 0; i < filelist.size(); i++) {
			files.add(filelist.get(i).toString());
		}
		return files;
	}

	public List<DataSourceVo> listFilesAndDirectories(String directory, String fileName) throws Exception {

		// TODO Enable this code block when resolving re-creating a new session
//		 ChannelSftp channelSftp = null;
//		 if(sftpSession.isOpen()){
//			 channelSftp = sftpSession.getClientInstance();
//		 }else{
//			 throw new Exception();
//		 }
		
		SftpSession session = sftpSessionFactory.getSession();

//		ChannelSftp channelSftp = sftpSession.getClientInstance();
		ChannelSftp channelSftp = session.getClientInstance();
		List<DataSourceVo> files = new ArrayList<DataSourceVo>();
		Vector<String> filest = null;

		if (directory == null) {
//			channelSftp.cd(rootPath);
//			filest = channelSftp.ls(rootPath);
			channelSftp.cd(rootPath+File.separator+"data");
			filest = channelSftp.ls(rootPath+File.separator+"data");
		} else {
//			channelSftp.cd(rootPath + directory);
//			filest = channelSftp.ls(rootPath + directory);
			channelSftp.cd(rootPath+File.separator+"data" + directory);
			filest = channelSftp.ls(rootPath+File.separator+"data" + directory);
		}

		for (int i = 0; i < filest.size(); i++) {
			Object obj = filest.elementAt(i);
			if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
				LsEntry entry = (LsEntry) obj;

				if (true && !entry.getAttrs().isDir()) {
					files.add(new DataSourceVo(entry.getFilename(), "No", entry.getLongname(), "Offline"));
				}
				if (true && entry.getAttrs().isDir()) {
					if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
						files.add(new DataSourceVo(entry.getFilename(), "Yes", entry.getLongname(), "Offline"));
					}
				}
			}
		}

		if (fileName != null && fileName.trim().length() > 0) {

			for (int i = 0; i < files.size(); ++i) {
				DataSourceVo source = files.get(i);
				if (!source.getFileName().toLowerCase().contains(fileName.toLowerCase())) {
					files.remove(i);
					--i;
				}
			}
		}
		
		session.close();

		return files;
	}

	public void readFile(String directory, String fileName) throws Exception {
		ChannelSftp channelSftp = sftpSession.getClientInstance();

		if (directory == null) {
			channelSftp.cd(rootPath);
		} else {
			channelSftp.cd(rootPath + File.separator + directory);
		}

		InputStream stream = channelSftp.get(fileName);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} finally {
			stream.close();
		}

	}

	public void processMapFile(DataSourceVo dataSource) throws Exception {
		ChannelSftp channelSftp = sftpSession.getClientInstance();

		String directory = dataSource.getDirectoryName();

		String fileName = dataSource.getFileName();

		if (directory == null) {
			channelSftp.cd(rootPath);
		} else {
			channelSftp.cd(rootPath + File.separator + directory);
		}

		InputStream stream = channelSftp.get(fileName);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String line = null;
			String mapValues[] = null;
			int rowId = 0;
			while ((line = br.readLine()) != null) {
				mapValues = pattern.split(line);
				cassandraService.insertMap(Constants.dataSourceTableName(dataSource), ++rowId, Integer.parseInt(mapValues[0]), mapValues[1], Double.parseDouble(mapValues[2]), Integer.parseInt(mapValues[3]));
			}
		} finally {
			stream.close();
		}

	}

	public void processPlinkDataSource(DataCenterVo dataCenter) throws Exception {
		ChannelSftp channelSftp = sftpSession.getClientInstance();

		String directory = dataCenter.getDirectory();

		if (directory == null) {
			channelSftp.cd(rootPath);
		} else {
			channelSftp.cd(rootPath + File.separator + directory);
		}

		String mapFile = null;

		String pedFile = null;

		String bedFile = null;

		String bimFile = null;

		String famFile = null;

		Vector<String> filest = null;

		filest = channelSftp.ls(".");

		for (int i = 0; i < filest.size(); i++) {
			Object obj = filest.elementAt(i);
			if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
				LsEntry entry = (LsEntry) obj;

				if (true && !entry.getAttrs().isDir()) {
					String fileName = entry.getFilename();
					String[] extList = fileName.split("\\.");
					if (extList.length > 1) {
						String extension = extList[1];
						if ("map".equalsIgnoreCase(extension)) {
							mapFile = fileName;
						} else if ("ped".equalsIgnoreCase(extension)) {
							pedFile = fileName;
						} else if ("bed".equalsIgnoreCase(extension)) {
							bedFile = fileName;
						} else if ("bim".equalsIgnoreCase(extension)) {
							bimFile = fileName;
						} else if ("fam".equalsIgnoreCase(extension)) {
							famFile = fileName;
						}
					}
				}

			}
		}

		if (mapFile != null && pedFile != null) {

			SparkSftpPedMapGenotypeData genotypeData = null;

			try {
				genotypeData = new SparkSftpPedMapGenotypeData(pedFile, mapFile, channelSftp);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			int id = 0;
			for (GeneticVariant variant : genotypeData) {

				String snp = variant.getVariantId().getPrimairyId();
				String chromosome = variant.getSequenceName();

				List<Alleles> alleles = genotypeData.getSampleVariants(variant);

				List<String> alleleList = new ArrayList<String>();
				for (Alleles allele : alleles) {
					alleleList.addAll(allele.getAllelesAsString());
				}

				cassandraService.insertPlinkGenomicData(dataCenter, ++id, snp, chromosome, alleleList);
			}

			for (Sample sample : genotypeData.getSamples()) {
				cassandraService.insertPlinkSampleData(dataCenter, sample);
			}
		} else if (bedFile != null && bimFile != null && famFile != null) {
			SparkSftpBedBimFamGenotypeData genotypeData = null;
			genotypeData = new SparkSftpBedBimFamGenotypeData(channelSftp, bimFile, famFile, 100);
			int id = 0;
			for (GeneticVariant variant : genotypeData) {
				String snp = variant.getVariantId().getPrimairyId();
				String chromosome = variant.getSequenceName();

				List<Alleles> alleles = genotypeData.getSampleVariants(variant);
				List<String> alleleList = new ArrayList<String>();

				for (Alleles allele : alleles) {
					alleleList.addAll(allele.getAllelesAsString());
				}
				cassandraService.insertPlinkGenomicData(dataCenter, ++id, snp, chromosome, alleleList);
				System.out.println();
			}

			for (Sample sample : genotypeData.getSamples()) {
				cassandraService.insertPlinkSampleData(dataCenter, sample);
			}
		}
	}

	public void onlinePlinkDataSource1(DataCenterVo dataCenter) throws Exception {
		ChannelSftp channelSftp = sftpSession.getClientInstance();

		String directory = dataCenter.getDirectory();

		if (directory == null) {
//			channelSftp.cd(rootPath);
			channelSftp.cd(rootPath+File.separator+"data");
		} else {
//			channelSftp.cd(rootPath + File.separator + directory);
			channelSftp.cd(rootPath + File.separator+"data"+File.separator + directory);
		}

		String mapFile = null;

		String pedFile = null;

		String bedFile = null;

		String bimFile = null;

		String famFile = null;

		Vector<String> filest = null;

		filest = channelSftp.ls(".");

		for (int i = 0; i < filest.size(); i++) {
			Object obj = filest.elementAt(i);
			if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
				LsEntry entry = (LsEntry) obj;

				if (true && !entry.getAttrs().isDir()) {
					String fileName = entry.getFilename();
					String[] extList = fileName.split("\\.");
					if (extList.length > 1) {
						String extension = extList[1];
						if ("map".equalsIgnoreCase(extension)) {
							mapFile = fileName;
						} else if ("ped".equalsIgnoreCase(extension)) {
							pedFile = fileName;
						} else if ("bed".equalsIgnoreCase(extension)) {
							bedFile = fileName;
						} else if ("bim".equalsIgnoreCase(extension)) {
							bimFile = fileName;
						} else if ("fam".equalsIgnoreCase(extension)) {
							famFile = fileName;
						}
					}
				}
			}
		}

		byte[] header = { 0b01101100, 0b00011011, 0b00000001 };

		String bedTableName = "";

		if (mapFile != null && pedFile != null) {

			// SparkSftpPedMapGenotypeData genotypeData = null;
			//
			// try {
			// genotypeData = new SparkSftpPedMapGenotypeData(pedFile, mapFile,
			// channelSftp);
			// } catch (IOException ex) {
			// ex.printStackTrace();
			// }
			//
			// int id = 0;
			// for (GeneticVariant variant : genotypeData) {
			//
			// String snp = variant.getVariantId().getPrimairyId();
			// String chromosome = variant.getSequenceName();
			//
			// List<Alleles> alleles = genotypeData.getSampleVariants(variant);
			//
			// List<String> alleleList = new ArrayList<String>();
			// for (Alleles allele : alleles) {
			// alleleList.addAll(allele.getAllelesAsString());
			// }
			//
			// cassandraService.insertPlinkGenomicData(dataCenter, ++id, snp,
			// chromosome, alleleList);
			// }
			//
			// for (Sample sample : genotypeData.getSamples()) {
			// cassandraService.insertPlinkSampleData(dataCenter, sample);
			// }

			String dataLine = null;
			String mapValues[] = null;

			InputStream inputStream = null;
			Scanner sc = null;

			// inputStream = new FileInputStream(pedFileName);
			// inputStream = new InputStreamReader(channelSftp.get(famFile),
			// FILE_ENCODING)
			inputStream = channelSftp.get(pedFile);
			sc = new Scanner(inputStream, "UTF-8");

			int cols = 0;
			boolean access = false;

			int rows = 0;

			// TODO Create fam table

			cassandraService.createPlinkFamTable(dataCenter);
			cassandraService.createPlinkBimTable(dataCenter);

			while (sc.hasNextLine()) {
				dataLine = sc.nextLine();
				mapValues = pattern.split(dataLine);

				if (!access) {
					cols = mapValues.length;
					access = true;
				}
				++rows;

				String[] famArray = Arrays.copyOfRange(mapValues, 0, 6);

				cassandraService.insertPlinkFamTable(rows, famArray, dataCenter);

			}

			inputStream.close();
			sc.close();

			boolean isCreated = false;
			for (int i = 6, snpId = 0; i < cols; i = i + 2) {
				inputStream = channelSftp.get(pedFile); // new
														// FileInputStream(pedFileName);
				sc = new Scanner(inputStream, "UTF-8");

				String[] snpArray = new String[rows * 2];
				for (int j = 0; sc.hasNextLine(); ++j) {
					dataLine = sc.nextLine();
					mapValues = pattern.split(dataLine);

					snpArray[j] = mapValues[i];
					snpArray[++j] = mapValues[i + 1];
				}

				inputStream.close();
				sc.close();

				HashMap<String, Byte> alleleMap = alleleMap(snpArray, ++snpId, channelSftp, mapFile, dataCenter);

				for (String s : snpArray) {
					System.out.print(s);
				}
				System.out.println();

				int noAlleles = snpArray.length;
				int noSnps = noAlleles / 2;

				int noInts = 0;

				if (noSnps % 16 == 0) {
					noInts = noSnps / 16;
				} else {
					noInts = (noSnps / 16) + 1;
				}

				int[] data = new int[noInts];

				for (int pos = 0, k = 0; pos < noAlleles; pos += 32, ++k) {
					data[k] = prepareInt(pos, snpArray, alleleMap);
				}

				System.out.println(data.length);

				if (!isCreated) {
					// TODO remove print after production
					System.out.println("-----------------Create table ------------------");
					cassandraService.createPlinkBedTable(data, dataCenter);
					isCreated = true;
				}
				// TODO remove print after production success
				System.out.println(" ----------------------- " + data.length + " ---------------------  ");
				cassandraService.insertPlinkBedTable(snpId, data, dataCenter);
				// writeFile(data, "sparksnpmjc.bed");
			}

		} else if (bedFile != null && bimFile != null && famFile != null) {

			cassandraService.createPlinkFamTable(dataCenter);
			cassandraService.createPlinkBimTable(dataCenter);

			int rows = 0;

			String dataLine = null;
			String mapValues[] = null;

			InputStream inputStream = null;
			Scanner sc = null;

			inputStream = channelSftp.get(famFile);
			sc = new Scanner(inputStream, "UTF-8");

			while (sc.hasNextLine()) {
				dataLine = sc.nextLine();
				mapValues = pattern.split(dataLine);
				++rows;

				cassandraService.insertPlinkFamTable(rows, mapValues, dataCenter);

			}
			inputStream.close();
			sc.close();

			inputStream = channelSftp.get(bimFile);
			sc = new Scanner(inputStream, "UTF-8");

			int bimId=0;
			while (sc.hasNextLine()) {
				dataLine = sc.nextLine();
				mapValues = pattern.split(dataLine);
				cassandraService.insertPlinkBimTable(++bimId, mapValues, dataCenter);
			}
			inputStream.close();
			sc.close();

			// Read the binary bed file
			inputStream = channelSftp.get(bedFile);
			byte[] bedData = IOUtils.toByteArray(inputStream);
			int byteLength = bedData.length - 3;
			byte type = bedData[2];

			int noOfColmns = 0;

			if (rows % 16 == 0) {
				noOfColmns = rows / 16;
			} else {
				noOfColmns = (rows / 16) + 1;
			}

			int[] snpData = new int[noOfColmns];

			// CAlculate bytes per snp

			int noOfBytes = 0;

			if (rows % 4 == 0) {
				noOfBytes = rows / 4;
			} else {
				noOfBytes = (rows / 4) + 1;
			}
			
			
//			System.out.println("No of Bytes per File = "+bedData.length);
//			System.out.println("No of Bytes per SNP = "+noOfBytes);

			cassandraService.createPlinkBedTable(snpData, dataCenter);

			if (type == 1) {				
				for(int bedPOS=3,id=1;bedPOS < bedData.length;++id){
					byte[] snpBytes= new byte[noOfBytes];
					for(int byteCount=0;byteCount<snpBytes.length;++byteCount,++bedPOS){
//						System.out.println("ID - "+id);
//						System.out.println("Byte Count - "+byteCount);
//						System.out.println(toByteBineryFormat(bedData[bedPOS]));
						snpBytes[byteCount]=bedData[bedPOS];
					}
					
					int[] colInts=new int[noOfColmns];
					
					int finalInt = Integer.parseInt("00", 2);
					int count=0;
					int colIntPos=0;
//					System.out.println("-------------------- Read bytes -----------------------------");
					for(int snpByteCount=0;snpByteCount<snpBytes.length;++snpByteCount){
						String s = toByteBineryFormat(snpBytes[snpByteCount]);
//						System.out.println(s);
						int i = Integer.parseInt(s,2);
						finalInt = (finalInt | ( i << (count*8)));
						if(++count==4){
							colInts[colIntPos] = finalInt;
							count=0;
							++colIntPos;
							finalInt = Integer.parseInt("00", 2);
						}
					}
					if(count >0){
						colInts[colIntPos] = finalInt;
					}
					cassandraService.insertPlinkBedTable(id, colInts, dataCenter);
				}
			}
		}
	}
	
	public void onlinePlinkDataSource(DataCenterVo dataCenter) throws Exception {
		
		SftpSession session = sftpSessionFactory.getSession();
		
//		ChannelSftp channelSftp = sftpSession.getClientInstance();
		
		ChannelSftp channelSftp = session.getClientInstance();
		
		String directory = dataCenter.getDirectory();

		if (directory == null) {
//			channelSftp.cd(rootPath);
			channelSftp.cd(rootPath+File.separator+"data");
		} else {
//			channelSftp.cd(rootPath + File.separator + directory);
			channelSftp.cd(rootPath + File.separator+"data"+File.separator + directory);
		}

		String mapFile = null;

		String pedFile = null;

		String bedFile = null;

		String bimFile = null;

		String famFile = null;

		Vector<String> filest = null;

		filest = channelSftp.ls(".");

		for (int i = 0; i < filest.size(); i++) {
			Object obj = filest.elementAt(i);
			if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
				LsEntry entry = (LsEntry) obj;

				if (true && !entry.getAttrs().isDir()) {
					String fileName = entry.getFilename();
					String[] extList = fileName.split("\\.");
					if (extList.length > 1) {
						String extension = extList[1];
						if ("map".equalsIgnoreCase(extension)) {
							mapFile = fileName;
						} else if ("ped".equalsIgnoreCase(extension)) {
							pedFile = fileName;
						} else if ("bed".equalsIgnoreCase(extension)) {
							bedFile = fileName;
						} else if ("bim".equalsIgnoreCase(extension)) {
							bimFile = fileName;
						} else if ("fam".equalsIgnoreCase(extension)) {
							famFile = fileName;
						}
					}
				}
			}
		}

		byte[] header = { 0b01101100, 0b00011011, 0b00000001 };

		String bedTableName = "";

		if (mapFile != null && pedFile != null) {

//			String dataLine = null;
//			String mapValues[] = null;

			InputStream inputStream = null;
//			Scanner sc = null;
			BufferedReader reader;

			inputStream = channelSftp.get(pedFile);
//			sc = new Scanner(inputStream, "UTF-8");

			reader = new BufferedReader(new InputStreamReader(inputStream));
			
//			int cols = 0;
//			boolean access = false;
//			int rows = 0;
			
			AtomicInteger cols = new AtomicInteger(0);
			AtomicBoolean access = new AtomicBoolean(false);
			AtomicInteger rows = new AtomicInteger(0);
			// TODO Create fam table

			cassandraService.createPlinkFamTable(dataCenter);
			cassandraService.createPlinkBimTable(dataCenter);

//			while (sc.hasNextLine()) {
//				dataLine = sc.nextLine();
//				mapValues = pattern.split(dataLine);
//
//				if (!access) {
//					cols = mapValues.length;
//					access = true;
//				}
//				++rows;
//
//				String[] famArray = Arrays.copyOfRange(mapValues, 0, 6);
//
//				cassandraService.insertPlinkFamTable(rows, famArray, dataCenter);
//
//			}
			
			 reader.lines().forEach(line -> {
		            // process liness
				 String mapValues[] = pattern.split(line);

					if (!access.get()) {
						cols.set(mapValues.length);
						access.set(true);
					}
					rows.addAndGet(1);

					String[] famArray = Arrays.copyOfRange(mapValues, 0, 6);

					cassandraService.insertPlinkFamTable(rows.get(), famArray, dataCenter);
		      });
			

			inputStream.close();
//			sc.close();
			reader.close();

			boolean isCreated = false;
			for (AtomicInteger i = new AtomicInteger(6), snpId = new AtomicInteger(0); i.get() < cols.get(); i.addAndGet(2)) {
				inputStream = channelSftp.get(pedFile); // new
														// FileInputStream(pedFileName);
//				sc = new Scanner(inputStream, "UTF-8");
				reader = new BufferedReader(new InputStreamReader(inputStream));

				String[] snpArray = new String[rows.get() * 2];
//				for (int j = 0; sc.hasNextLine(); ++j) {
//					dataLine = sc.nextLine();
//					mapValues = pattern.split(dataLine);
//
//					snpArray[j] = mapValues[i];
//					snpArray[++j] = mapValues[i + 1];
//				}
//				 int j=0;
				AtomicInteger j = new AtomicInteger(0);
				 reader.lines().forEach(line -> {
			            // process liness
					 String mapValues[] = pattern.split(line);

					 	snpArray[j.get()] = mapValues[i.get()];
					 	snpArray[j.addAndGet(1)] = mapValues[i.get()+1];
					 	j.getAndIncrement();
			    });

				inputStream.close();
//				sc.close();
				reader.close();
				
				HashMap<String, Byte> alleleMap = alleleMap(snpArray, snpId.addAndGet(1), channelSftp, mapFile, dataCenter);

//				for (String s : snpArray) {
//					System.out.print(s);
//				}
//				System.out.println();

				int noAlleles = snpArray.length;
				int noSnps = noAlleles / 2;

				int noInts = 0;

				if (noSnps % 16 == 0) {
					noInts = noSnps / 16;
				} else {
					noInts = (noSnps / 16) + 1;
				}

				int[] data = new int[noInts];

				for (int pos = 0, k = 0; pos < noAlleles; pos += 32, ++k) {
					data[k] = prepareInt(pos, snpArray, alleleMap);
				}

				//System.out.println(data.length);

				if (!isCreated) {
					// TODO remove print after production
					//System.out.println("-----------------Create table ------------------");
					cassandraService.createPlinkBedTable(data, dataCenter);
					isCreated = true;
				}
				// TODO remove print after production success
				System.out.println(" ----------------------- " + data.length + " ---------------------  ");
				cassandraService.insertPlinkBedTable(snpId.get(), data, dataCenter);
				// writeFile(data, "sparksnpmjc.bed");
			}

		} else if (bedFile != null && bimFile != null && famFile != null) {

			cassandraService.createPlinkFamTable(dataCenter);
			cassandraService.createPlinkBimTable(dataCenter);

			AtomicInteger rows = new AtomicInteger(0);

//			String dataLine = null;
//			String mapValues[] = null;

			InputStream inputStream = null;
//			Scanner sc = null;
			BufferedReader reader;

			inputStream = channelSftp.get(famFile);
//			sc = new Scanner(inputStream, "UTF-8");
			reader = new BufferedReader(new InputStreamReader(inputStream));

//			while (sc.hasNextLine()) {
//				dataLine = sc.nextLine();
//				mapValues = pattern.split(dataLine);
//				++rows;
//
//				cassandraService.insertPlinkFamTable(rows, mapValues, dataCenter);
//
//			}
			
			reader.lines().forEach(line -> {
				String[] mapValues = pattern.split(line);
				rows.addAndGet(1);
				cassandraService.insertPlinkFamTable(rows.get(), mapValues, dataCenter);

			
			});

			
			
			inputStream.close();
//			sc.close();
			reader.close();

			inputStream = channelSftp.get(bimFile);
//			sc = new Scanner(inputStream, "UTF-8");
			reader = new BufferedReader(new InputStreamReader(inputStream));
			
			AtomicInteger bimId= new AtomicInteger(0);
//			while (sc.hasNextLine()) {
//				dataLine = sc.nextLine();
//				mapValues = pattern.split(dataLine);
//				cassandraService.insertPlinkBimTable(++bimId, mapValues, dataCenter);
//			}
			
			reader.lines().forEach(line -> {
				String[] mapValues = pattern.split(line);
				cassandraService.insertPlinkBimTable(bimId.addAndGet(1), mapValues, dataCenter);
			
			});
			inputStream.close();
//			sc.close();
			reader.close();

			// Read the binary bed file
			inputStream = channelSftp.get(bedFile);
			byte[] bedData = IOUtils.toByteArray(inputStream);
			int byteLength = bedData.length - 3;
			byte type = bedData[2];

			int noOfColmns = 0;

			if (rows.get() % 16 == 0) {
				noOfColmns = rows.get() / 16;
			} else {
				noOfColmns = (rows.get() / 16) + 1;
			}

			int[] snpData = new int[noOfColmns];

			// CAlculate bytes per snp

			int noOfBytes = 0;

			if (rows.get() % 4 == 0) {
				noOfBytes = rows.get() / 4;
			} else {
				noOfBytes = (rows.get() / 4) + 1;
			}
			
			
//			System.out.println("No of Bytes per File = "+bedData.length);
//			System.out.println("No of Bytes per SNP = "+noOfBytes);

			cassandraService.createPlinkBedTable(snpData, dataCenter);

			if (type == 1) {				
				for(int bedPOS=3,id=1;bedPOS < bedData.length;++id){
					byte[] snpBytes= new byte[noOfBytes];
					for(int byteCount=0;byteCount<snpBytes.length;++byteCount,++bedPOS){
//						System.out.println("ID - "+id);
//						System.out.println("Byte Count - "+byteCount);
//						System.out.println(toByteBineryFormat(bedData[bedPOS]));
						snpBytes[byteCount]=bedData[bedPOS];
					}
					
					int[] colInts=new int[noOfColmns];
					
					int finalInt = Integer.parseInt("00", 2);
					int count=0;
					int colIntPos=0;
//					System.out.println("-------------------- Read bytes -----------------------------");
					for(int snpByteCount=0;snpByteCount<snpBytes.length;++snpByteCount){
						String s = toByteBineryFormat(snpBytes[snpByteCount]);
//						System.out.println(s);
						int i = Integer.parseInt(s,2);
						finalInt = (finalInt | ( i << (count*8)));
						if(++count==4){
							colInts[colIntPos] = finalInt;
							count=0;
							++colIntPos;
							finalInt = Integer.parseInt("00", 2);
						}
					}
					if(count >0){
						colInts[colIntPos] = finalInt;
					}
					cassandraService.insertPlinkBedTable(id, colInts, dataCenter);
				}
			}
		}
		
		session.close();
	}

	
	private String toByteBineryFormat(byte b1){
		String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
		return s1; 
	}

	private HashMap<String, Byte> alleleMap(String[] snpArray, int snpId, ChannelSftp channelSftp, String mapFile, DataCenterVo dataCenter) throws IOException, SftpException {
		HashMap<String, Byte> alleleMap = new HashMap<String, Byte>();

		// Collections.frequency(c, o) Arrays.asList(snpArray).re

		char allele1 = '0';
		char allele2 = '0';

		int count = 0;
		for (int i = 0; i < snpArray.length; ++i) {
			if ("0".equalsIgnoreCase(snpArray[i])) {
				continue;
			}
			++count;
		}

		String[] values = new String[count];
		for (int i = 0, j = 0; i < snpArray.length; ++i) {
			String s = snpArray[i];
			if (!"0".equalsIgnoreCase(s)) {
				values[j] = s;
				++j;
			}
		}

		if (values.length > 0) {
			char tmp1 = values[0].charAt(0);
			char tmp2 = '0';
			int match = 0;
			for (String s : values) {
				if (tmp1 == s.charAt(0)) {
					++match;
				} else {
					tmp2 = s.charAt(0);
				}
			}

			int diff = values.length - match;

			if (diff == 0) {
				allele2 = tmp1;
			} else if (diff > values.length / 2) {
				allele1 = tmp1;
				allele2 = tmp2;
			} else if (diff < values.length / 2) {
				allele1 = tmp2;
				allele2 = tmp1;
			} else if (diff == values.length / 2) {
				int comp = Character.compare(tmp1, tmp2);
				if (comp < 0) {
					allele1 = tmp1;
					allele2 = tmp2;
				} else {
					allele1 = tmp2;
					allele2 = tmp1;
				}
			}

		}

		// FileInputStream inputStream = null;
		InputStream inputStream = null;
		Scanner sc = null;
		// String mapFileName= "data/5SNPx3/3SAMPLE.map";

		// inputStream = new FileInputStream(mapFileName);
		inputStream = channelSftp.get(mapFile);
		sc = new Scanner(inputStream, "UTF-8");
		String dataLine = null;

		for (int i = 1; sc.hasNextLine(); ++i) {
			dataLine = sc.nextLine();
			if (i == snpId) {
				String bimLine = dataLine + "\t" + allele1 + "\t" + allele2 + "\n";
				// writeFile(bimLine.getBytes(), "sparksnpmjc1.bim");
				cassandraService.insertPlinkBimTable(snpId, bimLine.split("\t"), dataCenter);
				break;
			}
		}

		sc.close();
		inputStream.close();

		alleleMap.put(allele1 + "" + allele1, new Byte((byte) 0b00000000)); // Homozygote
																			// "1"/"1"
		alleleMap.put(allele1 + "" + allele2, ((byte) 0b00000010)); // Heterozygote
		alleleMap.put(allele2 + "" + allele1, new Byte((byte) 0b00000010)); // Heterozygote
		alleleMap.put(allele2 + "" + allele2, new Byte((byte) 0b00000011)); // Homozygote
																			// "2"/"2"

		return alleleMap;
	}

	private HashMap<String, Byte> alleleMap1(String[] snpArray, int snpId, ChannelSftp channelSftp, String mapFile, DataCenterVo dataCenter) throws IOException, SftpException {
		HashMap<String, Byte> alleleMap = new HashMap<String, Byte>();

		// Collections.frequency(c, o) Arrays.asList(snpArray).re

		char allele1 = '0';
		char allele2 = '0';
		
		

		int count = 0;
		for (int i = 0; i < snpArray.length; ++i) {
			if ("0".equalsIgnoreCase(snpArray[i])) {
				continue;
			}
			++count;
		}

		String[] values = new String[count];
		for (int i = 0, j = 0; i < snpArray.length; ++i) {
			String s = snpArray[i];
			if (!"0".equalsIgnoreCase(s)) {
				values[j] = s;
				++j;
			}
		}

		if (values.length > 0) {
			char tmp1 = values[0].charAt(0);
			char tmp2 = '0';
			int match = 0;
			for (String s : values) {
				if (tmp1 == s.charAt(0)) {
					++match;
				} else {
					tmp2 = s.charAt(0);
				}
			}

			int diff = values.length - match;

			if (diff == 0) {
				allele2 = tmp1;
			} else if (diff > values.length / 2) {
				allele1 = tmp1;
				allele2 = tmp2;
			} else if (diff < values.length / 2) {
				allele1 = tmp2;
				allele2 = tmp1;
			} else if (diff == values.length / 2) {
				int comp = Character.compare(tmp1, tmp2);
				if (comp < 0) {
					allele1 = tmp1;
					allele2 = tmp2;
				} else {
					allele1 = tmp2;
					allele2 = tmp1;
				}
			}

		}

		// FileInputStream inputStream = null;
		InputStream inputStream = null;
		//Scanner sc = null;
		BufferedReader reader=null;
		// String mapFileName= "data/5SNPx3/3SAMPLE.map";

		// inputStream = new FileInputStream(mapFileName);
		inputStream = channelSftp.get(mapFile);
		//sc = new Scanner(inputStream, "UTF-8");
		reader = new BufferedReader(new InputStreamReader(inputStream));
		String dataLine = null;

//		for (int i = 1; sc.hasNextLine(); ++i) {
//			dataLine = sc.nextLine();
//			if (i == snpId) {
//				String bimLine = dataLine + "\t" + allele1 + "\t" + allele2 + "\n";
//				// writeFile(bimLine.getBytes(), "sparksnpmjc1.bim");
//				cassandraService.insertPlinkBimTable(snpId, bimLine.split("\t"), dataCenter);
//				break;
//			}
//		}
		
		AtomicInteger i = new AtomicInteger(1);
		String s1=Character.toString(allele1);
		String s2=Character.toString(allele2);
		reader.lines().forEach(line -> {
			if(i.get() == snpId){
//				String bimLine = line + "\t" + allele1 + "\t" + allele2 + "\n";
				String bimLine = line + "\t" + s1 + "\t" + s2 + "\n";
				cassandraService.insertPlinkBimTable(snpId, bimLine.split("\t"), dataCenter);
			}
			i.getAndIncrement();
		});

//		sc.close();
		inputStream.close();
		reader.close();

		alleleMap.put(allele1 + "" + allele1, new Byte((byte) 0b00000000)); // Homozygote
																			// "1"/"1"
		alleleMap.put(allele1 + "" + allele2, ((byte) 0b00000010)); // Heterozygote
		alleleMap.put(allele2 + "" + allele1, new Byte((byte) 0b00000010)); // Heterozygote
		alleleMap.put(allele2 + "" + allele2, new Byte((byte) 0b00000011)); // Homozygote
																			// "2"/"2"

		return alleleMap;
	}

	
	private int prepareInt(int pos, String[] mapValues, HashMap<String, Byte> alleleMap) throws IOException {
		int finalInt = Integer.parseInt("00", 2);

		int length = mapValues.length;

		for (int i = 0; i < 32; i += 2) {
			if (pos + i > length - 1) {
				break;
			}
			byte a = snp2byte(mapValues[pos + i], mapValues[pos + i + 1], alleleMap);
			finalInt = (finalInt | (a << i));
		}

		return finalInt;
	}

	private byte snp2byte(String a1, String a2, HashMap<String, Byte> alleleMap) {
		Byte result = alleleMap.get(a1 + a2);
		return result != null ? result : 0b00000010;
	}

	public void uploadProgram(String destinationDir, String name) {

		try {
			String path = destinationDir + File.separator + name;
			// List<String> fileNames = new ArrayList<String>();

			ChannelSftp channelSftp = sftpSession.getClientInstance();
			System.out.println("Print PWD :" + channelSftp.pwd());
			channelSftp.cd(rootPath + File.separator + "spark");
			channelSftp.mkdir(name);
			channelSftp.cd(name);

			this.createFilesAndDirectories(new File(path).toPath(), channelSftp);

			// Clear the temporary directory
			// channelSftp.rm(rootPath+"spark/"+name);

			// for(String s:fileNames){
			// System.out.println(s);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createFilesAndDirectories(Path dir, ChannelSftp channel) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path path : stream) {
				if (path.toFile().isDirectory()) {
					String dirName = path.getFileName().toString();
					channel.mkdir(dirName);
					channel.cd(dirName);
					createFilesAndDirectories(path, channel);
				} else {
					channel.put(new FileInputStream(path.toFile()), path.getFileName().toString());
				}
			}
			channel.cd("..");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String compileProgram(ComputationVo computationVo) {

		Map<String, String> map = new HashMap<String, String>();
		ExecSession execSession = null;
		ChannelExec channelExec = null;
		InputStream in = null;
		BufferedReader reader = null;
		String command = null;

		StringBuffer results = new StringBuffer();

		try {
			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();
			in = channelExec.getInputStream();

			String path = "spark/" + computationVo.getProgramId() + "/" + computationVo.getProgram();
//			String path = rootPath+ "/" + computationVo.getProgramId() + "/" + computationVo.getProgram();

			command = "cat " + path + "/spark.info";

			System.out.println("Command: " + command);
			channelExec.setCommand(command);
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			int index = 0;

			while ((line = reader.readLine()) != null) {
				System.out.println(++index + " : " + line);
				String array[] = line.split("[:]");
				if (array.length > 1) {
					map.put(array[0], array[1]);
				}
			}
			
			in.close();
			reader.close();

			int exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}

			if (!Constants.SPARK_INFO_AUTOMATIC_MODE.equalsIgnoreCase(map.get(Constants.SPARK_INFO_MODE))) {

				execSession = execSessionFactory.getSession();
				channelExec = execSession.getClientInstance();
				in = channelExec.getInputStream();

				String outputDir = map.get(Constants.SPARK_OUTPUT);

				String parameters = map.get(Constants.SPARK_INFO_MAKEPARAMETERS);

				if ("out".equalsIgnoreCase(outputDir)) {
					String output = parameters != null ? parameters.split("[.]")[0] : "";
					command = "mkdir " + path + "/program/out; " + appendCheckStatement(map.get(Constants.SPARK_INFO_MAKETYPE) + " " + path + "/program/" + map.get(Constants.SPARK_INFO_MAKEPARAMETERS) + " -o " + path + "/program/out/" + output + ";");
				} else {
					command = appendCheckStatement(map.get(Constants.SPARK_INFO_MAKETYPE) + " " + path + "/program/" + (parameters != null ? parameters : "") + ";");
				}
				System.out.println("Command: " + command);

				channelExec.setCommand(command);
				channelExec.connect();

				reader = new BufferedReader(new InputStreamReader(in));

				while ((line = reader.readLine()) != null) {
					System.out.println(line);
					results.append(line);
				}
				
				in.close();
				reader.close();

				exitStatus = channelExec.getExitStatus();

				if (exitStatus < 0) {
					System.out.println("Done, but exit status not set!");
				} else if (exitStatus > 0) {
					System.out.println("Done, but with error!");
				} else {
					System.out.println("Done!");
				}

			}

		} catch (Exception e) {
			System.err.println("Error: " + e);
			return "System Error";
		}
		return results.toString();
	}

	private String appendCheckStatement(String command) {
		return "if " + command + " then echo 'Compiled'; else echo 'Compile Failure'; fi;";
	}

	public void executeAnalysis(AnalysisVo analysisVo) {
		Map<String, String> map = new HashMap<String, String>();
		ExecSession execSession = null;
		ChannelExec channelExec = null;
		InputStream in = null;
		BufferedReader reader = null;
		String command = null;

		StringBuffer results = new StringBuffer();

		try {

			// read spark info and store the program instructions
			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();
			in = channelExec.getInputStream();

			String path = "spark/" + analysisVo.getProgramId() + "/" + analysisVo.getProgramName();

			command = "cat " + path + "/spark.info";

			System.out.println("Command: " + command);
			channelExec.setCommand(command);
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			int index = 0;

			while ((line = reader.readLine()) != null) {
				System.out.println(++index + " : " + line);
				String array[] = line.split("[:]");
				if (array.length > 1) {
					map.put(array[0], array[1]);
				}
			}
			
			in.close();
			reader.close();

			int exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}

			String resultDir = null;
			// Move the data set to the output directory.
			if (Constants.DATA_CENTERS.SSH_HPC.toString().equals(analysisVo.getSourceDataCenter())) {
				ChannelSftp channelSftp = sftpSession.getClientInstance();
				channelSftp.cd(rootPath + File.separator + "spark");
				channelSftp.cd(analysisVo.getProgramId() + "/" + analysisVo.getProgramName() + "/program");
				resultDir = "result" + analysisVo.getAnalysisId();
				channelSftp.mkdir(resultDir);
				channelSftp.cd(resultDir);

				execSession = execSessionFactory.getSession();
				channelExec = execSession.getClientInstance();
				String source = rootPath + File.separator + analysisVo.getSourcePath();

				String destination = path + "/program" + "/" + resultDir;

				if (BooleanUtils.isTrue(analysisVo.isSourceDir())) {
					command = "cp -dr " + source + " " + destination;
					System.out.println("Command: " + command);
				} else {
					command = "cp " + source + " " + destination;
					System.out.println("Command: " + command);
				}
				channelExec.setCommand(command);
				channelExec.connect();
			}

			// execute the program for given data set
			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();
			in = channelExec.getInputStream();

			String program = map.get("makefile").split("[.]")[0];
			String output = map.get("output");
			String result = map.get("result");
			String mode = map.get("mode");
			String run = map.get("run");

			if ("automatic".equalsIgnoreCase(mode)) {
				String options = analysisVo.getParameters() != null ? analysisVo.getParameters() + " " : " ";
				command = "cd " + path + "/program/; " + "chmod 777 " + run + "; cd " + resultDir + "; ./../" + run + " " + options + "> " + result + ";";
			} else {
				command = "./" + path + "/program/" + output + "/" + program + ">" + path + "/program/" + resultDir + "/" + result + ";";
			}
			System.out.println("Command: " + command);

			channelExec.setCommand(command);
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));

			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				results.append(line);
			}
			
			in.close();
			reader.close();

			exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e);
		}
	}

	public String getAnalysisResults(AnalysisVo analysisVo) {
		Map<String, String> map = new HashMap<String, String>();
		ExecSession execSession = null;
		ChannelExec channelExec = null;
		InputStream in = null;
		BufferedReader reader = null;
		String command = null;

		StringBuffer results = new StringBuffer();

		try {
			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();
			in = channelExec.getInputStream();

			String path = "spark/" + analysisVo.getProgramId() + "/" + analysisVo.getProgramName();

			command = "cat " + path + "/spark.info";

			System.out.println("Command: " + command);
			channelExec.setCommand(command);
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			int index = 0;

			while ((line = reader.readLine()) != null) {
				System.out.println(++index + " : " + line);
				String array[] = line.split("[:]");
				if (array.length > 1) {
					map.put(array[0], array[1]);
				}
			}
			
			in.close();
			reader.close();

			int exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}

			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();
			in = channelExec.getInputStream();

			String result = null;
			String resultDir = null;

			// if (StringUtils.isEmpty(analysisVo.getJobId())) {
			// result = map.get("result");
			//
			// resultDir = "result" + analysisVo.getAnalysisId();
			// } else {
			// resultDir = "job" + analysisVo.getAnalysisId();
			// }

			resultDir = "job" + analysisVo.getAnalysisId();

			if (analysisVo.getResult() != null) {
				command = "cat " + path + "/program/" + resultDir + "/" + analysisVo.getResult() + ";";
			} else {
				command = "cat " + path + "/program/" + resultDir + "/" + result + ";";
			}

			System.out.println("Command: " + command);

			channelExec.setCommand(command);
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));

			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				if (line.contains("\n")) {
					results.append(line);
				} else {
					results.append(line + "\n");
				}
			}
			
			in.close();
			reader.close();

			exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e);
		} finally {
			channelExec.disconnect();
		}
		return results.toString();
	}

	private String getPathToCompileDir(String dirName) {
		String path = "";
		try {
			ChannelSftp channelSftp = sftpSession.getClientInstance();
			channelSftp.cd(rootPath);
			channelSftp.cd("spark");
			channelSftp.cd(dirName);

			Vector<String> filest = null;

			filest = channelSftp.ls(".");
			for (int i = 0; i < filest.size(); i++) {
				Object obj = filest.elementAt(i);
				if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
					LsEntry entry = (LsEntry) obj;

					if (true && entry.getAttrs().isDir()) {
						path = entry.getFilename();
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return path;
	}

	public void uploadJobScript(AnalysisJobVo job, String srcPath) {
		// String path = destinationDir + File.separator + name;
		// List<String> fileNames = new ArrayList<String>();
		try {

			ChannelSftp channelSftp = sftpSession.getClientInstance();
			System.out.println("Print PWD :" + channelSftp.pwd());

			String analysisDirPath = rootPath + File.separator + "spark" + File.separator + job.getProgramId() + File.separator + job.getProgramName() + "/program";
			// channelSftp.cd(rootPath + File.separator + "spark");
			// channelSftp.cd(job.getProgramId() + "/" + job.getProgramName() +
			// "/program");
			channelSftp.cd(analysisDirPath);
			String jobDir = "job" + job.getAnalysisId();
			channelSftp.mkdir(jobDir);
			channelSftp.cd(jobDir);
			channelSftp.put(new FileInputStream(srcPath), job.getScriptName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String jobSubmission1(final AnalysisVo analysisVo) {
		String jobId = null;

		Map<String, String> map = new HashMap<String, String>();
		ExecSession execSession = null;
		ChannelExec channelExec = null;
		InputStream in = null;
		BufferedReader reader = null;
		String command = null;

		StringBuffer results = new StringBuffer();

		try {
			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();
			in = channelExec.getInputStream();

			String path = "spark/" + analysisVo.getProgramId() + "/" + analysisVo.getProgramName();

			command = "cat " + path + "/spark.info";

			System.out.println("Command: " + command);
			channelExec.setCommand(command);
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			int index = 0;

			while ((line = reader.readLine()) != null) {
				System.out.println(++index + " : " + line);
				String array[] = line.split("[:]");
				if (array.length > 1) {
					map.put(array[0], array[1]);
				}
			}

			int exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}

			String resultDir = null;
			// Move the data set to the output directory.
			if (Constants.DATA_CENTERS.SSH_HPC.toString().equals(analysisVo.getSourceDataCenter())) {
				ChannelSftp channelSftp = sftpSession.getClientInstance();
				channelSftp.cd(rootPath + File.separator + "spark");
				channelSftp.cd(analysisVo.getProgramId() + "/" + analysisVo.getProgramName() + "/program");
				resultDir = "result" + analysisVo.getAnalysisId();
				channelSftp.mkdir(resultDir);
				channelSftp.cd(resultDir);

				execSession = execSessionFactory.getSession();
				channelExec = execSession.getClientInstance();
				String source = rootPath + File.separator + analysisVo.getSourcePath();

				String destination = path + "/program" + "/" + resultDir;

				if (BooleanUtils.isTrue(analysisVo.isSourceDir())) {
					command = "cp -dr " + source + " " + destination;
					System.out.println("Command: " + command);
				} else {
					command = "cp " + source + " " + destination;
					System.out.println("Command: " + command);
				}
				channelExec.setCommand(command);
				channelExec.connect();
			}

			// execute the program for given data set
			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();
			in = channelExec.getInputStream();

			// String program = map.get("makefile").split("[.]")[0];
			// String output = map.get("output");
			// String result = map.get("result");
			// String mode = map.get("mode");
			// String run = map.get("run");
			//
			// if ("automatic".equalsIgnoreCase(mode)) {
			// String options = analysisVo.getParameters() != null ?
			// analysisVo.getParameters() + " " : " ";
			// command = "cd " + path + "/program/; " + "chmod 777 " + run +
			// "; cd " + resultDir + "; ./../" + run + " " + options + "> " +
			// result + ";";
			// } else {
			// command = "./" + path + "/program/" + output + "/" + program +
			// ">" + path + "/program/" + resultDir + "/" + result + ";";
			// }

			String batchScript = analysisVo.getScriptName();
			command = "cd " + path + "/program/" + "job" + analysisVo.getAnalysisId().toString() + "; sbatch " + batchScript + ";";
			System.out.println("Command: " + command);

			channelExec.setCommand(command);
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));

			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				results.append(line);
			}

			exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e);
		}

		String resultArray[] = results.toString().split("Submitted batch job");

		if (resultArray.length > 0) {
			jobId = resultArray[1].trim();
		}
		return jobId;
	}

	public String jobSubmission(final AnalysisVo analysisVo) {
		String jobId = "0";

		Map<String, String> map = new HashMap<String, String>();
		ExecSession execSession = null;
		ChannelExec channelExec = null;
		InputStream in = null;
		BufferedReader reader = null;
		String command = null;

		StringBuffer results = new StringBuffer();

		try {
			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();
			in = channelExec.getInputStream();

			String path = "spark/" + analysisVo.getProgramId() + "/" + analysisVo.getProgramName();

			command = "cat " + path + "/spark.info";

			System.out.println("Command: " + command);
			channelExec.setCommand(command);
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			int index = 0;

			while ((line = reader.readLine()) != null) {
				System.out.println(++index + " : " + line);
				String array[] = line.split("[:]");
				if (array.length > 1) {
					map.put(array[0], array[1]);
				}
			}

			int exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}

			// String resultDir = null;
			// // Move the data set to the output directory.
			// if
			// (Constants.DATA_CENTERS.SSH_TEST.toString().equals(analysisVo.getSourceDataCenter()))
			// {
			// ChannelSftp channelSftp = sftpSession.getClientInstance();
			// channelSftp.cd(rootPath + File.separator + "spark");
			// channelSftp.cd(analysisVo.getProgramId() + "/" +
			// analysisVo.getProgramName() + "/program");
			// resultDir = "result" + analysisVo.getAnalysisId();
			// channelSftp.mkdir(resultDir);
			// channelSftp.cd(resultDir);
			//
			// execSession = execSessionFactory.getSession();
			// channelExec = execSession.getClientInstance();
			// String source = rootPath + File.separator +
			// analysisVo.getSourcePath();
			//
			// String destination = path + "/program" + "/" + resultDir;
			//
			// if (BooleanUtils.isTrue(analysisVo.isSourceDir())) {
			// command = "cp -dr " + source + " " + destination;
			// System.out.println("Command: " + command);
			// } else {
			// command = "cp " + source + " " + destination;
			// System.out.println("Command: " + command);
			// }
			// channelExec.setCommand(command);
			// channelExec.connect();
			// }

			// execute the program for given data set
			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();
			in = channelExec.getInputStream();

			String batchScript = analysisVo.getScriptName();
			// command = "cd " + path + "/program/" + "job" +
			// analysisVo.getAnalysisId().toString() + "; sbatch " + batchScript
			// + ";";
			String jobDir = "job" + analysisVo.getAnalysisId().toString();
			StringBuffer commandBuffer = new StringBuffer("cd " + path + "/program;" + "rm -rf " + jobDir + "; mkdir " + jobDir + ";");
			commandBuffer.append("cp sparkbatch.sh " + jobDir + "/;");

			if (Constants.DATA_CENTERS.SSH_HPC.toString().equals(analysisVo.getSourceDataCenter())) {
//				String source = rootPath + File.separator + analysisVo.getSourcePath();
				String source = rootPath + File.separator +"data" + File.separator +analysisVo.getSourcePath();
				if (BooleanUtils.isTrue(analysisVo.isSourceDir())) {
					commandBuffer.append("cp -dr " + source + " " + jobDir + ";");
				} else {
					commandBuffer.append("cp " + source + " " + jobDir + ";");
				}
			}

			commandBuffer.append("cd " + jobDir + ";");
			commandBuffer.append("echo '#SBATCH --job-name=\"spark-job" + analysisVo.getAnalysisId().toString() + "\"' >> sparkbatch.sh;");
			commandBuffer.append("echo '#SBATCH --output=\"" + analysisVo.getResult() + "\"' >> sparkbatch.sh;");
			commandBuffer.append("echo \"" + map.get("run") + "\" >> sparkbatch.sh;");
			commandBuffer.append("sbatch sparkbatch.sh;");
			System.out.println("Command: " + commandBuffer.toString());

			channelExec.setCommand(commandBuffer.toString());
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));

			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				results.append(line);
			}

			exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}

			// ChannelSftp channelSftp = sftpSession.getClientInstance();
			//
			// if(!channelSftp.isConnected()){
			// channelSftp.connect();
			// }

			// channelSftp.cd(rootPath + File.separator + "spark");
			// channelSftp.cd(analysisVo.getProgramId() + "/" +
			// analysisVo.getProgramName() + "/program/"+jobDir);
			// InputStream obj_InputStream = new
			// ByteArrayInputStream("A\nB\nC".getBytes());
			// channelSftp.put(obj_InputStream, "test.sh");
			// channelSftp.exit();
			// obj_InputStream.close();

			// channelSftp.disconnect();
			// channelSftp.rmdir(jobDir);
			// channelSftp.mkdir(jobDir);

			String resultArray[] = results.toString().split("Submitted batch job");

			if (resultArray.length > 0) {
				jobId = resultArray[1].trim();
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error: " + e);
		} finally {
			if (channelExec != null) {
				channelExec.disconnect();
			}
		}

		return jobId;
	}

	// public static void main(String[] args) {
	// // String s = "JobState=RUNNING Reason=None Dependency=(null)";
	// String s = "UserId=thilinaranaweera(4176) GroupId=VR0304(3560)";
	// Map<String, String> map = Splitter.on(' ').withKeyValueSeparator('=')
	// .split(s);
	// System.out.println(map.get("JobState"));
	//
	//
	// }

	public String getJobStatus(String jobId) {
		ExecSession execSession = null;
		ChannelExec channelExec = null;
		InputStream in = null;
		BufferedReader reader = null;
		String command = null;

		String status = null;
		Map<String, String> jobMap = new HashMap<String, String>();
		try {

			execSession = execSessionFactory.getSession();
			channelExec = execSession.getClientInstance();

			in = channelExec.getInputStream();

			// command= "squeue -j "+jobId;
			command = "scontrol show job " + jobId;

			System.out.println("Command: " + command);

			channelExec.setCommand(command);
			channelExec.connect();

			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				// results.append(line);

				if (line.trim().startsWith("JobState")) {
					Map<String, String> map = Splitter.on(' ').withKeyValueSeparator('=').split(line.trim());
					jobMap.putAll(map);
					break;
				}
			}
			
			in.close();
			reader.close();

			int exitStatus = channelExec.getExitStatus();

			if (exitStatus < 0) {
				System.out.println("Done, but exit status not set!");
			} else if (exitStatus > 0) {
				System.out.println("Done, but with error!");
			} else {
				System.out.println("Done!");
			}

		} catch (Exception e) {
			e.printStackTrace();
			// status ="Failed";
			jobMap.put("JobState", "Failed");
		} finally {
			if (channelExec != null) {
				channelExec.disconnect();
			}
		}

		status = jobMap.get("JobState");

		return status;
	}

	public String queryResult(DataCenterVo dataCenterVo) {
		String result = null;

		int famTableId = cassandraService.queryResultFamilyTablePosition(dataCenterVo);

		String bedColumn = "h";

		if (famTableId <= 16) {
			bedColumn = bedColumn + "1";
		} else {
			int remain = famTableId % 16;
			if (remain == 0) {
				bedColumn = bedColumn + (famTableId / 16);
			} else {
				bedColumn = bedColumn + ((famTableId / 16) + 1);
			}
		}

		long snpCount = cassandraService.tableSnpCount(dataCenterVo);

		System.out.println("SNP count  -- " + snpCount);

		int noOfColumns = 0;

		if (snpCount % 16 == 0) {
			noOfColumns = ((int) snpCount) / 16;
		} else {
			noOfColumns = ((int) snpCount) / 16 + 1;
		}

		cassandraService.createPlinkIndividualResultTable(dataCenterVo, noOfColumns);

		int[] data = new int[noOfColumns];

		int snpData = Integer.parseInt("00", 2);

		int k = 0;

		StringBuffer sb = new StringBuffer();
		for (int i = 1, j = 0; i <= snpCount; ++i) {
			int snp = cassandraService.getSnpNumber(dataCenterVo, bedColumn, i);

			// System.out.println("SNP id == "+snp);
			String binary = Integer.toBinaryString(snp);
			System.out.println(binary);
			String alleles = null;

			String[] alleleArray = cassandraService.getSnpAlleles(i, dataCenterVo);

			// System.out.println("Alleles: "+alleleArray[0]+" : "+alleleArray[1]);

			int remain = famTableId % 16;

			String select = binary.substring((binary.length() - remain * 2), (binary.length() - remain * 2) + 2);
			// System.out.println("Select: "+select);
			alleles = getPedString(alleleArray, select);

			byte a = Byte.parseByte(select, 2);
			System.out.println("select A:" + a);
			if (j < 32) {
				snpData = (snpData | (a << j));
				// System.out.println("----"+i+" "+Integer.toBinaryString(snpData));
				j = j + 2;
			} else {

				data[k] = snpData;
				++k;
				j = 0;
				snpData = Integer.parseInt("00", 2);
				snpData = (snpData | (a << j));
				j = j + 2;
			}

			if (i == 1) {
				sb.append(alleles);
			} else {
				sb.append(" " + alleles);
			}
		}

		System.out.println("position " + k + "   " + Integer.toBinaryString(snpData));
		data[k] = snpData;

		if (cassandraService.isResultExists(dataCenterVo)) {
			cassandraService.dropIndividualResult(dataCenterVo);
		}
		cassandraService.insertSnpIndividualResult(data, dataCenterVo);

		result = sb.toString();

		return result;
	}

	private String getPedString(String[] alleles, String binaryTxt) {
		String pedString = "";

		HashMap<String, String> alleleMap = new HashMap<String, String>();

		// map.put((byte) 0b00000000, " " + alleleOne + " " + alleleOne);
		// map.put((byte) 0b00000011, " " + alleleTwo + " " + alleleTwo);
		// map.put((byte) 0b00000010, " " + alleleOne + " " + alleleTwo);
		// map.put((byte) 0b00000001, " " + "0" + " " + "0");

		// System.out.println("Alleles: "+alleles[0]+" "+alleles[1]);

		alleleMap.put("00", alleles[0] + " " + alleles[0]); // Homozygote
															// "1"/"1"
		alleleMap.put("11", alleles[1] + " " + alleles[1]); // Homozygote
															// "2"/"2"
		alleleMap.put("10", alleles[0] + " " + alleles[1]); // Heterozygote
		alleleMap.put("01", "0" + " " + "0"); // Homozygote "2"/"2"

		pedString = alleleMap.get(binaryTxt);
		return pedString;
	}

	public String getQueryResults(DataCenterVo dataCenterVo) {
		String result = null;

		long snpCount = cassandraService.tableSnpCount(dataCenterVo);

		System.out.println("SNP count  -- " + snpCount);

		int noOfColumns = 0;

		if (snpCount % 16 == 0) {
			noOfColumns = ((int) snpCount) / 16;
		} else {
			noOfColumns = ((int) snpCount) / 16 + 1;
		}

		int[] results = cassandraService.getIndividualResult(dataCenterVo, noOfColumns);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < snpCount;) {
			int position = 0;
			String alleles = null;
			int remain = (int) snpCount % 16;

			if (remain == 0) {
				position = (int) (snpCount / 16) - 1;
			} else {
				position = (int) (snpCount / 16);
			}
			String snpBlock = Integer.toBinaryString(results[position]);

			String[] alleleArray = cassandraService.getSnpAlleles(++i, dataCenterVo);

			String select = snpBlock.substring((snpBlock.length() - remain * 2), (snpBlock.length() - remain * 2) + 2);

			alleles = getPedString(alleleArray, select);

			if (i == 1) {
				sb.append(alleles);
			} else {
				sb.append(" " + alleles);
			}

		}

		String[] familyDetails = cassandraService.getIndividualFamilyDetails(dataCenterVo);
		StringBuffer family = new StringBuffer();
		for (int i = 0; i < familyDetails.length; ++i) {
			family.append(familyDetails[i] + " ");
		}

		result = family.toString() + sb.toString();

		return result;
	}

	// TODO Need to remove in the code clean phase
	// public static void main(String[] args) {
	// String datasetPath =
	// "~/plink-1.07-x86_64/data/expr_data/100SNP/100/100SAMPLE";
	//
	// SparkSftpPedMapGenotypeData genotypeData = null;
	// try {

	// genotypeData = new SparkSftpPedMapGenotypeData(datasetPath);
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	//
	// for (GeneticVariant variant : genotypeData) {
	// System.out.print(variant.getVariantId() + " ");
	// System.out.println(variant.getSequenceName());
	//
	// List<Alleles> alleles = genotypeData.getSampleVariants(variant);
	//
	// for (Alleles allele : alleles) {
	// List<String> alleleList = allele.getAllelesAsString();
	// for (String s : alleleList) {
	// System.out.print(s);
	// }
	// }
	// System.out.println();
	// }
	//
	// for (Sample sample : genotypeData.getSamples()) {
	// System.out.println("Sample ID: " + sample.getId() + " sex: " +
	// sample.getSex() + " Pheno " +
	// sample.getAnnotationValues().get(GenotypeData.DOUBLE_PHENOTYPE_SAMPLE_ANNOTATION_NAME));
	// }
	// }
	//
	// public static void main1(String[] args) {
	// String datasetPath =
	// "~/plink-1.07-x86_64/data/expr_data/100SNP/100/100SAMPLE";
	//
	// // Create a binary plink genotype data object
	// SparkSftpBedBimFamGenotypeData genotypeData = null;
	// try {
	// genotypeData = new SparkSftpBedBimFamGenotypeData(datasetPath);
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	//
	// for (GeneticVariant variant : genotypeData) {
	// System.out.print(variant.getVariantId() + " ");
	// System.out.println(variant.getSequenceName());
	//
	// List<Alleles> alleles = genotypeData.getSampleVariants(variant);
	//
	// for (Alleles allele : alleles) {
	// List<String> alleleList = allele.getAllelesAsString();
	// for (String s : alleleList) {
	// System.out.print(s);
	// }
	// }
	// System.out.println();
	// }
	//
	// for (Sample sample : genotypeData.getSamples()) {
	// System.out.println("Sample ID: " + sample.getId() + " sex: " +
	// sample.getSex() + " Pheno " +
	// sample.getAnnotationValues().get(GenotypeData.DOUBLE_PHENOTYPE_SAMPLE_ANNOTATION_NAME));
	// }
	// }

	public static void main(String[] args) {
		// int snp = 536302;
		//
		// // ByteBuffer dbuf = ByteBuffer.allocate(32);
		// // dbuf.putInt(snp);
		// // byte[] bytes = dbuf.array();
		//
		// String s =
		// "11101010101010101110111111101000101011101010101010101010101011101011101110111111101000101111111010111111101010111010111110101011101010101010101110101110100010101010101010101010101010111011111110101011";
		//
		// System.out.println(s.length());

		// int finalInt = Integer.parseInt("10", 2);
		// System.out.println(finalInt);
		//
		// int a =Integer.parseInt("11", 2);
		// int i=1;
		// finalInt = (finalInt | (a << i));
		//
		// System.out.println(finalInt);

//		int noInts = 0;
//		int rows = 10;
//		if (rows % 16 == 0) {
//			noInts = rows / 16;
//		} else {
//			noInts = (rows / 16) + 1;
//		}
//		System.out.println(noInts);
		
		int[] colInts= new int[1];
		byte[] snpBytes= new byte[]{0b00101000,};
		
		int finalInt = Integer.parseInt("00", 2);
		int count=0;
		int colIntPos=0;
		System.out.println("-------------------- Read bytes -----------------------------");
		for(int snpByteCount=0;snpByteCount<snpBytes.length;++snpByteCount){
			byte b = snpBytes[snpByteCount];
//			printByteBineryFormat(b);
			finalInt = (finalInt | ( b << (count*8)));
			if(++count==4){
				colInts[colIntPos] = finalInt;
				count=0;
				++colIntPos;
				finalInt = Integer.parseInt("00", 2);
			}
		}
		if(count >0){
			colInts[colIntPos] = finalInt;
		}
		
		System.out.println(Integer.toBinaryString(colInts[0]));
	}

}
