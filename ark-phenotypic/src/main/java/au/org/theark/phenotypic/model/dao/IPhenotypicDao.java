/*******************************************************************************
 * Copyright (c) 2011  University of Western Australia. All rights reserved.
 * 
 * This file is part of The Ark.
 * 
 * The Ark is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * The Ark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package au.org.theark.phenotypic.model.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import au.org.theark.core.exception.ArkRunTimeException;
import au.org.theark.core.exception.ArkRunTimeUniqueException;
import au.org.theark.core.exception.ArkSystemException;
import au.org.theark.core.exception.ArkUniqueException;
import au.org.theark.core.exception.EntityCannotBeRemoved;
import au.org.theark.core.exception.EntityExistsException;
import au.org.theark.core.exception.EntityNotFoundException;
import au.org.theark.core.model.pheno.entity.LinkPhenoDataSetCategoryField;
import au.org.theark.core.model.pheno.entity.PhenoDataSetCategory;
import au.org.theark.core.model.pheno.entity.PhenoDataSetCollection;
import au.org.theark.core.model.pheno.entity.PhenoDataSetData;
import au.org.theark.core.model.pheno.entity.PhenoDataSetField;
import au.org.theark.core.model.pheno.entity.PhenoDataSetFieldDisplay;
import au.org.theark.core.model.pheno.entity.PhenoDataSetGroup;
import au.org.theark.core.model.pheno.entity.PickedPhenoDataSetCategory;
import au.org.theark.core.model.pheno.entity.QuestionnaireStatus;
import au.org.theark.core.model.study.entity.ArkFunction;
import au.org.theark.core.model.study.entity.ArkUser;
import au.org.theark.core.model.study.entity.AuditHistory;
import au.org.theark.core.model.study.entity.CustomFieldGroup;
import au.org.theark.core.model.study.entity.DelimiterType;
import au.org.theark.core.model.study.entity.FileFormat;
import au.org.theark.core.model.study.entity.LinkSubjectStudy;
import au.org.theark.core.model.study.entity.PhenoDataSetFieldCategoryUpload;
import au.org.theark.core.model.study.entity.PhenoFieldUpload;
import au.org.theark.core.model.study.entity.Study;
import au.org.theark.core.model.study.entity.Upload;
import au.org.theark.core.vo.CustomFieldGroupVO;
import au.org.theark.core.vo.PhenoDataCollectionVO;
import au.org.theark.core.vo.PhenoDataSetFieldGroupVO;

/**
 * Interface for all select/insert/update/delete operations on the backend database.
 * 
 */
public interface IPhenotypicDao {

	public java.util.Collection<PhenoDataSetCollection> getPhenoCollectionByStudy(Study study);

	public void deletePhenoCollection(PhenoDataSetCollection collection);

	public void createUpload(Upload upload);

	public void updateUpload(Upload upload);

	// File Formats
	public java.util.Collection<FileFormat> getFileFormats();

	// Delimiter Types
	public java.util.Collection<DelimiterType> getDelimiterTypes();

	public long getCountOfFieldsInStudy(Study study);

	public long getCountOfFieldsWithDataInStudy(Study study);


	public long getCountOfCollectionsInStudy(Study study);

	public long getCountOfCollectionsWithDataInStudy(Study study);

	public DelimiterType getDelimiterType(Long id);

	public String getDelimiterTypeByDelimiterChar(char phenotypicDelimChr);

	public FileFormat getFileFormatByName(String name);

	public Long isPhenoDataSetFieldUsed(PhenoDataSetData phenoData);

	public void createPhenoData(PhenoDataSetData phenoData);

	public void updatePhenoData(PhenoDataSetData phenoData);

	public void deletePhenoData(PhenoDataSetData phenoData);

	public PhenoDataSetCollection getPhenoCollection(Long id);

	public long getPhenoDataCount(PhenoDataSetCollection phenoCollection,PhenoDataSetCategory phenoDataSetCategory);

	public List<PhenoDataSetData> getPhenoDataList(PhenoDataSetCollection phenoCollection,PhenoDataSetCategory phenoDataSetCategory, int first, int count);
	
	public void createCustomFieldGroup(CustomFieldGroupVO customFieldGroupVO) throws EntityExistsException, ArkSystemException;
	
	public void updateCustomFieldGroup(CustomFieldGroupVO customFieldGroupVO) throws EntityExistsException,ArkSystemException;
	
	public void deleteCustomFieldGroup(CustomFieldGroupVO customFieldGroupVO);

	public long getPhenoCollectionCount(PhenoDataCollectionVO criteria);
	
	public List<PhenoDataSetCollection> searchPageablePhenoCollection(PhenoDataCollectionVO collectionCriteria, int first, int count);
	
	public List<PhenoDataSetField> getPhenoDataSetFieldsLinkedToPhenoDataSetFieldGroup(PhenoDataSetGroup phenoDataSetGroup);

	public List<QuestionnaireStatus> getPhenoCollectionStatusList();
	
	public Collection<PhenoDataSetFieldDisplay> getCFDLinkedToQuestionnaire(PhenoDataSetGroup phenoDataSetGroup, int first, int count);
	
	public long getCFDLinkedToQuestionnaireCount(PhenoDataSetGroup customFieldGroup);

	public void createPhenoCollection(PhenoDataSetCollection phenoCollection);

	public void updatePhenoCollection(PhenoDataSetCollection phenoCollection);
	
	public QuestionnaireStatus getPhenoCollectionStatusByName(String statusName);
	
	public java.util.Collection<Upload> searchUpload(Upload upload);
	
	public void deleteUpload(Upload studyUpload);
	public Upload getUpload(Long id);

	public Collection<CustomFieldGroup> getCustomFieldGroupList(Study study);

	public void processPhenoCollectionsWithTheirDataToInsertBatch(List<PhenoDataSetCollection> phenoCollectionsWithTheirDataToInsert, Study study);
	
	public List<List<String>> getPhenoDataAsMatrix (Study study, List<String> subjectUids, List<PhenoDataSetField> phenoDataSetFields, List<PhenoDataSetGroup> phenoDataSetGroups,PhenoDataSetCategory phenoDataSetCategory);
	
	public List<PhenoDataSetGroup> getPhenoDataSetGroupsByLinkSubjectStudy(LinkSubjectStudy linkSubjectStudy);

	public CustomFieldGroup getCustomFieldGroupByNameAndStudy(String name, Study study);

	public PhenoDataSetGroup getPhenoFieldGroupById(Long id);

	public List<PhenoDataSetCollection> getSubjectMatchingPhenoCollections(LinkSubjectStudy subject, PhenoDataSetGroup phenoDataSetGroup,Date recordDate);
	
	/**
	 * Get pheno dataset category by id.
	 * @param id
	 * @return
	 */
	public PhenoDataSetCategory getPhenoDataSetCategory(Long id) throws EntityNotFoundException;
	
	/**
	 * Get available all Pheno data set category.
	 * 
	 * @param study
	 * @param arkFunction
	 * @return
	 * @throws ArkSystemException
	 */
	public List<PhenoDataSetCategory> getAvailableAllCategoryList(Study study,ArkFunction arkFunction) throws ArkSystemException;
	
	
	/**
	 * Count all the PhenoDataSet categories.
	 * 
	 * @param phenoDataSetCategoryCriteria
	 * @return
	 */
	public long getPhenoDataSetCategoryCount(PhenoDataSetCategory phenoDataSetCategoryCriteria);
	
	/**
	 * List of all available Pheno category list for update.
	 * @param study
	 * @param arkFunction
	 * @param customFieldType
	 * @param thisCustomFieldCategory
	 * @return
	 * @throws ArkSystemException
	 */
	public List<PhenoDataSetCategory> getAvailableAllCategoryListExceptThis(Study study,ArkFunction arkFunction,PhenoDataSetCategory thisPhenoDataSetCategory) throws ArkSystemException;
	/**
	 * Search pageable pheno data set categories.
	 * @param customFieldCategoryCriteria
	 * @param first
	 * @param count
	 * @return
	 */
	public List<PhenoDataSetCategory> searchPageablePhenoDataSetCategories(PhenoDataSetCategory phenoDataSetCategoryCriteria, int first, int count);
	/**
	 * Create Pheno Data set category
	 * @throws ArkSystemException
	 * @throws ArkUniqueException
	 */
	public void createPhenoDataSetCategory(PhenoDataSetCategory phenoDataSetCategory) throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException;
	/**
	 * Update  Pheno Data set category
	 * @throws ArkSystemException
	 * @throws ArkUniqueException
	 */
	public void updatePhenoDataSetCategory(PhenoDataSetCategory phenoDataSetCategory) throws ArkSystemException, ArkUniqueException;
	/**
	 * Delete Pheno Data set category
	 * @throws ArkSystemException
	 * @throws EntityCannotBeRemoved
	 */
	public void deletePhenoDataSetCategory(PhenoDataSetCategory phenoDataSetCategory) throws ArkSystemException, EntityCannotBeRemoved;
	/**
	 * Create Audit history.
	 * @param auditHistory
	 * @param userId
	 * @param study
	 */
	public void createAuditHistory(AuditHistory auditHistory, String userId, Study study);
	/**
	 * is PhenoDataSet Category is Unique.
	 * @param phenoDataSetCategoryName
	 * @param study
	 * @param phenoDataSetCategoryToUpdate
	 * @return
	 */
	public boolean isPhenoDataSetCategoryUnique(String phenoDataSetCategoryName,Study study, PhenoDataSetCategory phenoDataSetCategoryToUpdate);
	/**
	 * Is this category is a parent of another.
	 * @param phenoDataSetCategory
	 * @return
	 */
	public boolean isPhenoDataSetCategoryAlreadyUsed(PhenoDataSetCategory phenoDataSetCategory);
	/**
	 * Get Pheno field data set by Id,
	 * @param id
	 * @return
	 */
	public PhenoDataSetField getPhenoDataSetField(Long id);
	/**
	 * Number of Pheno field count.
	 * 
	 * @param phenofieldcriteria
	 * @return
	 */
	public long getPhenoFieldCount(PhenoDataSetField phenofieldcriteria);
	
	/**
	 * getCategoriesListInPhenoDataSetField
	 * 
	 * @param study
	 * @param arkFunction
	 * @return
	 * @throws ArkSystemException
	 */
	public List<PhenoDataSetCategory> getCategoriesListInPhenoDataSetField(Study study, ArkFunction arkFunction) throws ArkSystemException;
	/**
	 * searchPageablePhenoFields
	 * 
	 * @param phenoDataSetCriteria
	 * @param first
	 * @param count
	 * @return
	 */
	public List<PhenoDataSetField> searchPageablePhenoFields(PhenoDataSetField phenoDataSetCriteria, int first, int count);
	/**
	 * getAvailableAllCategoryListInStudy
	 * 
	 * @param study
	 * @param arkFunction
	 * @return
	 * @throws ArkSystemException
	 */
	public List<PhenoDataSetCategory> getAvailableAllCategoryListInStudy(Study study, ArkFunction arkFunction)throws ArkSystemException;
	/**
	 * 
	 * @param phenoFieldName
	 * @param study
	 * @param phenoFieldToUpdate
	 * @return
	 */
	public boolean isPhenoDataSetFieldUnqiue(String phenoFieldName, Study study, PhenoDataSetField phenoFieldToUpdate);
	/**
	 * 
	 * @param phenoDataSetField
	 * @throws ArkSystemException
	 */
	public void updatePhenoDataSetField(PhenoDataSetField phenoDataSetField) throws  ArkSystemException;
	
	/**
	 * 
	 * @param phenDataSetFieldDisplay
	 * @throws ArkSystemException
	 */
	public void updatePhenoDataSetFieldDisplay(PhenoDataSetFieldDisplay phenDataSetFieldDisplay) throws  ArkSystemException;
	/**
	 * 
	 * @param CustomFieldCategory
	 * @throws ArkSystemException
	 */
	public void mergePhenoDataSetFieldCategory(PhenoDataSetCategory phenoDataSetCategory)throws ArkSystemException;
	
	/**
	 * 
	 * @param study
	 * @param arkFunction
	 * @param parentcustomFieldCategory
	 * @return
	 */
	public List<PhenoDataSetCategory> getAllSubCategoriesOfThisCategory(Study study,ArkFunction arkFunction,PhenoDataSetCategory parentcustomFieldCategory);
	/**
	 * 
	 * @param phenoDataSetField
	 * @throws ArkSystemException
	 */
	public void deletePhenoDataSetField(PhenoDataSetField phenoDataSetField) throws ArkSystemException;
	/**
	 * 
	 * @param phenoDataSetFieldDisplay
	 * @throws ArkSystemException
	 */
	public void deletePhenoDataSetFieldDisplay(PhenoDataSetFieldDisplay phenoDataSetFieldDisplay) throws ArkSystemException;
	/**
	 * 
	 * @param phenoDataSetField
	 * @throws ArkSystemException
	 */
	public void createPhenoDataSetField(PhenoDataSetField phenoDataSetField)throws ArkSystemException;
	/**
	 * 
	 * @param phenoDataSetFieldDisplay
	 * @throws ArkSystemException
	 */
	public void createPhenoDataSetFieldDisplay(PhenoDataSetFieldDisplay phenoDataSetFieldDisplay)throws ArkSystemException;
	
	public void createPhenoDataSetFieldGroup(PhenoDataSetFieldGroupVO phenoDataSetFieldGroupVO) throws EntityExistsException, ArkSystemException;
	
	public void updatePhenoDataSetFieldGroup(PhenoDataSetFieldGroupVO phenoDataSetFieldGroupVO) throws EntityExistsException,ArkSystemException;
	
	public void deletePhenoDataSetFieldGroup(PhenoDataSetFieldGroupVO phenoDataSetFieldGroupVO);

	public PhenoDataSetFieldDisplay getPhenoDataSetFieldDisplayByPhenoDataSetFieldAndGroup(PhenoDataSetField phenoDataSetField,PhenoDataSetGroup phenoDataSetGroup);
	
	public long getPhenoDataSetFieldGroupCount(PhenoDataSetGroup phenoDataSetGroup);
	
	public List<PhenoDataSetField> getPhenoDataSetFieldList(PhenoDataSetField phenoDataSetFieldCriteria);
	
	public List<PhenoDataSetGroup> getPhenoDataSetGroups(PhenoDataSetGroup phenoDataSetGroup, int first, int count);
	
	public void createPickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory) throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException,EntityExistsException;
	
	public void deletePickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory) throws ArkSystemException, EntityCannotBeRemoved;
		
	public List<PickedPhenoDataSetCategory> getPickedPhenoDataSetCategories(Study study,ArkFunction arkFunction,ArkUser arkUser);
	
	public List<PhenoDataSetCategory> getAvailablePhenoCategoryListNotPicked(Study study,ArkFunction arkFunctionPhenoCat,ArkFunction arkFunctionPhenoCollection,ArkUser arkUser) throws ArkSystemException;
	
	public PickedPhenoDataSetCategory getPickedPhenoDataSetCategoryFromPhenoDataSetCategory(Study study,ArkFunction arkFunction,ArkUser arkUser,PhenoDataSetCategory phenoDataSetCategory);
	
	public void deleteLinkPhenoDataSetCategoryField(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField) throws ArkSystemException, EntityCannotBeRemoved;
	
	public void  createLinkPhenoDataSetCategoryField(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField) throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException,EntityExistsException;
	
	public List<PhenoDataSetField> getAvailablePhenoFieldListNotInLinked(Study study,ArkFunction arkFunctionPhenoCat,ArkFunction arkFunctionPhenoCollection,ArkUser arkUser) throws ArkSystemException;
	
	public List<LinkPhenoDataSetCategoryField> getLinkPhenoDataSetCategoryFieldLst(Study study, ArkFunction arkFunction,ArkUser arkUser);
	
	public void updatePickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory)throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException;
	
	public List<PhenoDataSetField> getLinkedPhenoDataSetFieldsForSelectedCategories(Study study, ArkFunction arkFunction,ArkUser arkUser,List<PhenoDataSetCategory> phenoDataSetCategory);
	
	public LinkPhenoDataSetCategoryField getLinkPhenoDataSetCategoryField(Study study, ArkFunction arkFunction,ArkUser arkUser,PhenoDataSetCategory phenoDataSetCategory,PhenoDataSetField phenoDataSetField);
	
	public boolean isSelectedCategoriesAlreadyAssignedToFields(Study study, ArkFunction arkFunction,ArkUser arkUser,List<PhenoDataSetCategory> phenoDataSetCategories);
	
	public Long getNextAvailbleNumberForPickedCategory(Study study, ArkFunction arkFunction,ArkUser arkUser);	
	
	public PickedPhenoDataSetCategory getSwapOverPickedPhenoDataSetCategoryForUpButton(PickedPhenoDataSetCategory pickedPhenoDataSetCategory);
	
	public PickedPhenoDataSetCategory getSwapOverPickedPhenoDataSetCategoryForDownButton(PickedPhenoDataSetCategory pickedPhenoDataSetCategory);
	
	public Long getNextAvailbleNumberForAssignedField(Study study, ArkFunction arkFunction,ArkUser arkUser,PhenoDataSetCategory phenoDataSetCategory);
	
	public LinkPhenoDataSetCategoryField getSwapOverPhenoDataSetFieldForUpButton(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField);
	
	public LinkPhenoDataSetCategoryField getSwapOverPhenoDataSetFieldForDownButton(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField);
	
	public void updateLinkPhenoDataSetCategoryField(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField)throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException;
	
	public PhenoDataSetCategory getPhenoDataSetCategoryForAssignedPhenoDataSetField(Study study, ArkFunction arkFunction,ArkUser arkUser,PhenoDataSetField phenoDataSetField);
	
	public Boolean isPickedPhenoDataSetCategoryIsAParentOfAnotherCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory);
	
	public List<PickedPhenoDataSetCategory> getChildrenOfPickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory);
	
	public List<PickedPhenoDataSetCategory> getAllParentPickedPhenoDataSetCategories(Study study, ArkFunction arkFunction,ArkUser arkUser);
	
	public List<LinkPhenoDataSetCategoryField> getLinkPhenoDataSetCategoryFieldsForPickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory);
	
	public PhenoDataSetCategory getPhenoDataFieldCategoryByNameStudyAndArkFunction(String name,Study study,ArkFunction arkFunction);
	
	public PhenoDataSetField getPhenoDataSetFieldByNameStudyArkFunction(String name,Study study,ArkFunction arkFunction);

	public void createPhenoDataSetFieldCategoryUpload(PhenoDataSetFieldCategoryUpload phenoDataSetFieldCategoryUpload);
	
	public void createPhenoDataSetFieldUpload(PhenoFieldUpload phenoFieldUpload);
	
	public List<PhenoDataSetFieldDisplay> getPhenoDataSetFieldDisplayForPhenoDataSetFieldGroup(PhenoDataSetGroup phenoDataSetGroup);
	
	public void deletePickedCategoriesAndAllTheirChildren(Study study, ArkFunction arkFunction,ArkUser arkUser);

	public List<PhenoDataSetFieldDisplay> getPhenoFieldDisplaysIn(List<String> fieldNameCollection, Study study, ArkFunction arkFunction, PhenoDataSetGroup phenoDataSetGroup);

	public List<PhenoDataSetFieldDisplay> getPhenoFieldDisplaysIn(Study study, ArkFunction arkFunction);

	public long getPhenoFieldGroupCount(Study study,ArkFunction arkFunction,Boolean status);
	
	public PhenoDataSetField getPhenoDataSetFieldByNameStudyPFG(String FieldName, Study study, ArkFunction arkFunction, PhenoDataSetGroup phenoDataSetGroup)throws ArkRunTimeException,ArkSystemException;
	
	public List<PhenoDataSetGroup> getPhenoDataSetFieldGroups(PhenoDataSetGroup phenoDataSetGroup, int first, int count);
	
	public List<PhenoDataSetFieldDisplay> getPhenoDataSetFieldDisplayForPhenoDataSetFieldGroupOrderByPhenoDataSetCategory(PhenoDataSetGroup phenoDataSetGroup);
	
	public List<PhenoDataSetField> getPhenoDataSetFieldsLinkedToPhenoDataSetFieldGroupAndPhenoDataSetCategory(PhenoDataSetGroup phenoDataSetGroupCriteria,PhenoDataSetCategory phenoDataSetCategory);
	
	public List<Boolean> getPublishedSatusLst(Study study,ArkFunction arkFunction);
	
	public PhenoDataSetCategory getPhenoDataSetCategoryById(Long id);
	
	public boolean isPhenoDataSetFieldCategoryBeingUsed(PhenoDataSetCategory phenoDataSetCategory);
	
	public List<PhenoDataSetField> getAllPhenoDataSetFieldsLinkedToPhenoDataSetFieldGroup(PhenoDataSetGroup phenoDataSetGroupCriteria);
	
	public boolean isInEncodedValues(PhenoDataSetField phenoDataSetField, String value);

	public boolean isSameNameFieldGroupExsistsForTheStudy(String name,Study study,ArkFunction arkFunction);
	
	public void deletePhenoDatasetData(PhenoDataSetCollection phenoDataSetCollection);
}
