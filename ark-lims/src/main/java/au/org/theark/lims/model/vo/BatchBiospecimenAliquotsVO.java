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
package au.org.theark.lims.model.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import au.org.theark.core.model.lims.entity.Biospecimen;

/**
 * Represents a Batch of Biospecimen Aliquots in one VO.
 * 
 * @author cellis
 * 
 */
public class BatchBiospecimenAliquotsVO implements Serializable {
	private static final long	serialVersionUID	= 1L;
	protected Integer		numberToCreate;
	protected Biospecimen	parentBiospecimen;
	protected List<Biospecimen> aliquots;
	
	public BatchBiospecimenAliquotsVO(Biospecimen parentBiospecimen) {
		this.parentBiospecimen = parentBiospecimen;
		aliquots = new ArrayList<Biospecimen>(0);
		Biospecimen biospecimen= new Biospecimen();
		aliquots.add(biospecimen);
	}
	/**
	 * @return the numberToCreate
	 */
	public Integer getNumberToCreate() {
		return numberToCreate;
	}
	/**
	 * @param numberToCreate the numberToCreate to set
	 */
	public void setNumberToCreate(Integer numberToCreate) {
		this.numberToCreate = numberToCreate;
	}
	/**
	 * @return the parentBiospecimen
	 */
	public Biospecimen getParentBiospecimen() {
		return parentBiospecimen;
	}
	/**
	 * @param parentBiospecimen the parentBiospecimen to set
	 */
	public void setParentBiospecimen(Biospecimen parentBiospecimen) {
		this.parentBiospecimen = parentBiospecimen;
	}
	/**
	 * @return the aliquots
	 */
	public List<Biospecimen> getAliquots() {
		return aliquots;
	}
	/**
	 * @param aliquots the aliquots to set
	 */
	public void setAliquots(List<Biospecimen> aliquots) {
		this.aliquots = aliquots;
	}
	
	
}
