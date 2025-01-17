/*******************************************************************************s
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
package au.org.theark.lims.model.dao;

import java.util.Collection;
import java.util.List;

import au.org.theark.core.exception.ArkSystemException;
import au.org.theark.core.exception.EntityNotFoundException;
import au.org.theark.core.model.lims.entity.BioCollection;
import au.org.theark.core.model.lims.entity.BioCollectionCustomFieldData;
import au.org.theark.core.model.lims.entity.BioSampletype;
import au.org.theark.core.model.study.entity.ArkFunction;
import au.org.theark.core.model.study.entity.CustomFieldCategory;
import au.org.theark.core.model.study.entity.CustomFieldType;
import au.org.theark.core.model.study.entity.LinkSubjectStudy;
import au.org.theark.core.model.study.entity.Study;

public interface IBioCollectionDao {
	/**
	 * Look up a LIMS Collection based on the supplied Long id that represents the primary key
	 * 
	 * @param id
	 * @return au.org.theark.core.model.lims.entity.Collection
	 * @throws EntityNotFoundException
	 * @throws ArkSystemException
	 */
	public au.org.theark.core.model.lims.entity.BioCollection getBioCollection(Long id) throws EntityNotFoundException;

	/**
	 * Look up a List of LIMS Collection(s) based on the supplied limsCollection object
	 * 
	 * @param limsCollection
	 * @return List<au.org.theark.core.model.lims.entity.Collection>
	 * @throws EntityNotFoundException
	 * @throws ArkSystemException
	 */
	public List<au.org.theark.core.model.lims.entity.BioCollection> searchBioCollection(au.org.theark.core.model.lims.entity.BioCollection limsCollection) throws ArkSystemException;

	/**
	 * Create a LIMS collection based on the supplied limsCollection
	 * 
	 * @param limsCollection
	 */
	public BioCollection createBioCollection(au.org.theark.core.model.lims.entity.BioCollection limsCollection)  throws ArkSystemException;

	/**
	 * Update a LIMS collection based on the supplied limsCollection
	 * 
	 * @param limsCollection
	 * @throws ArkSystemException 
	 */
	public void updateBioCollection(au.org.theark.core.model.lims.entity.BioCollection limsCollection) throws ArkSystemException;

	/**
	 * Delete a LIMS collection based on the supplied limsCollection
	 * 
	 * @param limsCollection
	 */
	public void deleteBioCollection(au.org.theark.core.model.lims.entity.BioCollection limsCollection);

	/**
	 * Get a list of all sampleTypes
	 * 
	 * @return List
	 */
	public List<BioSampletype> getSampleTypes();

	/**
	 * Determine if provided linkSubjectStudy has any BioCollections associated
	 * 
	 * @return true if provided linkSubjectStudy has one or more BioCollections
	 */
	public Boolean hasBioCollections(LinkSubjectStudy linkSubjectStudy);

	/**
	 * Determine if provided bioCollection has any biospecimens associated
	 * 
	 * @return true if provided bioCollection has one or more Biospecimens
	 */
	public Boolean hasBiospecimens(BioCollection bioCollection);

	/**
	 * Get count of the BioCollections given the criteria
	 * 
	 * @param BioCollection
	 *           criteria
	 * @return counts
	 */
	public long getBioCollectionCount(BioCollection bioCollectionCriteria);

	/**
	 * A generic interface that will return a list BioCollections specified by a particular criteria, and a paginated reference point
	 * 
	 * @param BioCollection
	 *           criteria
	 * @return Collection of SubjectVO
	 */
	public List<BioCollection> searchPageableBioCollections(BioCollection bioCollectionCriteria, int first, int count);

	public long getBioCollectionCustomFieldDataCount(BioCollection bioCollectionCriteria, ArkFunction arkFunction);
	
	public List<BioCollectionCustomFieldData> getBioCollectionCustomFieldDataList(BioCollection bioCollectionCriteria, ArkFunction arkFunction,CustomFieldCategory customFieldCategory,CustomFieldType customFieldType, int first, int count);

	public void createBioCollectionCustomFieldData(BioCollectionCustomFieldData bioCollectionCFData);

	public void updateBioCollectionCustomFieldData(BioCollectionCustomFieldData bioCollectionCFData);

	public void deleteBioCollectionCustomFieldData(BioCollectionCustomFieldData bioCollectionCFData);

	public Long isCustomFieldUsed(BioCollectionCustomFieldData bioCollectionCFData);
	
	public BioCollectionCustomFieldData getBioCollectionCustomFieldData(BioCollection bioCollectionCriteria, ArkFunction arkFunction, String customFieldName);

	public BioCollection getBioCollectionByUID(String biocollectionUid,Long studyId, String subjectUID);

	public List<String> getAllBiocollectionUIDs(Study study);
	
	public BioCollection getBioCollectionForStudySubjectByUID(String biocollectionUid, Study study, LinkSubjectStudy linkSubjectStudy);
	
	public void batchInsertBiocollections(Collection<BioCollection> insertBioCollections);
	
	public void batchUpdateBiocollections(Collection<BioCollection> updateBioCollections);
	
	public boolean hasBiocllectionGotCustomFieldData(BioCollection bioCollection);

}
