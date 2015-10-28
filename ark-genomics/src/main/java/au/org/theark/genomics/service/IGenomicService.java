package au.org.theark.genomics.service;

import java.util.List;

import au.org.theark.core.exception.ArkSystemException;
import au.org.theark.core.model.spark.entity.Analysis;
import au.org.theark.core.model.spark.entity.Computation;
import au.org.theark.core.model.spark.entity.DataSource;
import au.org.theark.core.model.spark.entity.DataSourceType;
import au.org.theark.core.model.spark.entity.MicroService;
import au.org.theark.genomics.model.vo.DataCenterVo;
import au.org.theark.genomics.model.vo.DataSourceVo;

public interface IGenomicService {

	public void saveOrUpdate(MicroService microService);
	
	public void saveOrUpdate(DataSource dataSource);
	
	public void saveOrUpdate(Computation computation);
	
	public void saveOrUpdate(Analysis analysis);
	
	public void save(Computation computation, byte[] attachement) throws ArkSystemException;

	public void update(Computation computation,byte[] attachement, String checksum) throws ArkSystemException;

	public void delete(MicroService microService);
	
	public void delete(DataSource dataSource);
	
	public void delete(Computation computation);

	public List<MicroService> searchMicroService(final MicroService microService);
	
	public String checkServiceStatus(final MicroService microService);
	
	public List<String> searchDataCenters(MicroService microService);

	public List<DataSourceVo> searchDataSources(DataCenterVo datacenter);

	public List<Computation> searchComputations(Computation computation);
	
	public List<DataSourceType> listDataSourceTypes();
	
	public DataSource getDataSource(DataSourceVo dataSourceVo);
	
	public String executeDataSourceUpload(DataSourceVo dataSource);
	
	public String executeDataSourceUpload(DataCenterVo dataCenter, String initStatus);
	
	public void updateDataSourceStatus(final String processUID, DataSource dataSource, List<DataSource> dataSourceList, String intStatus);
	
//	public void uploadComputaion(Computation computation);
	
	public void uploadComputation(Computation computation);
	
	public String compileComputation(Computation computation);
	
	public void updateCompilationStatus(String processUID, Computation computation);
	
	public List<DataSource> searchDataSources(MicroService microService);
	
	public List<Computation> searchComputation(MicroService microService);
	
	public List<Analysis> searchAnalysis(Analysis analysis, Long studyId);
	
	public String executeAnalysis(Analysis analysis);
	
	public void updateAnalysisStatus(final String processUID, Analysis analysis);
	
	public byte[] getAnalysisResult(Analysis analysis);
	
}