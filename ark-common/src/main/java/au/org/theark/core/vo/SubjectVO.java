/*******************************************************************************
 * 
 * Copyright (c) 2011  University of Western Australia. All rights reserved.
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
package au.org.theark.core.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import au.org.theark.core.model.study.entity.Address;
import au.org.theark.core.model.study.entity.ConsentAnswer;
import au.org.theark.core.model.study.entity.EmailAccount;
import au.org.theark.core.model.study.entity.LinkSubjectStudy;
import au.org.theark.core.model.study.entity.OtherID;
import au.org.theark.core.model.study.entity.Phone;
import au.org.theark.core.model.study.entity.Study;
import au.org.theark.core.model.study.entity.SubjectFile;
import au.org.theark.core.model.study.entity.SubjectStatus;

/**
 * @author nivedann
 * 
 */
@SuppressWarnings("serial")
public class SubjectVO implements ArkVo,Serializable {
	protected String										subjectFullName;
	protected SubjectStatus									subjectStatus;
	protected LinkSubjectStudy								linkSubjectStudy;
	protected Collection<SubjectVO>							subjectList;
	protected SubjectFile									subjectFile;
	protected ConsentAnswer									consentAnswerSelect;
	protected String										subjectPreviousLastname;
	protected String 										subjectUID;

	/** A List of phone numbers linked to this person/subject */
	protected Collection<Phone>						phoneList;
	/** A List of Address linked to this person/subject */
	protected Collection<Address>						addressList;
	/** A List of Email account linked to this person/subject */
	protected Collection<EmailAccount>				emailAccountList;

	/** A List of Files linked to this person/subject */
	protected Collection<SubjectFile>				subjectFileList;
	
	protected Collection<OtherID>					otherIDs;
	
	private List<Study>			availableChildStudies;
	private List<Study>			selectedChildStudies;

	private List<Study>			studyList;
	
	private List<String> relativeUIDs;
	
	protected boolean enableNewButton =true;
	
	private String parentType;
	
	private String message;
	
	protected boolean changingLastName=true;

	
	public SubjectVO() {
		phoneList = new ArrayList<Phone>();
		addressList = new ArrayList<Address>();
		emailAccountList = new ArrayList<EmailAccount>();
		linkSubjectStudy = new LinkSubjectStudy();
		subjectFile = new SubjectFile();
		subjectFileList = new ArrayList<SubjectFile>();
		subjectPreviousLastname = new String();
		availableChildStudies = new ArrayList<Study>();
		selectedChildStudies  = new ArrayList<Study>();
		studyList = new ArrayList<Study>();
		relativeUIDs = new ArrayList<String>();
		otherIDs = new ArrayList<OtherID>();
	}

	public String getSubjectFullName() {
		StringBuffer sb = new StringBuffer();
		
		String firstName = linkSubjectStudy.getPerson().getFirstName();
		String midName = linkSubjectStudy.getPerson().getMiddleName();
		String lastName = linkSubjectStudy.getPerson().getLastName();

		if (firstName != null) {
			sb.append(linkSubjectStudy.getPerson().getFirstName());
			sb.append(" ");
		}
		if (midName != null) {
			sb.append(linkSubjectStudy.getPerson().getMiddleName());
			sb.append(" ");
		}
		if (lastName != null) {
			sb.append(linkSubjectStudy.getPerson().getLastName());
		}
		return sb.toString();
	}

	public void setSubjectFullName(String subjectFullName) {
		this.subjectFullName = subjectFullName;
	}

	protected Collection<LinkSubjectStudy>	participants;

	public Collection<LinkSubjectStudy> getParticipants() {
		return participants;
	}

	public void setParticipants(Collection<LinkSubjectStudy> participants) {
		this.participants = participants;
	}

	public LinkSubjectStudy getLinkSubjectStudy() {
		return linkSubjectStudy;
	}

	public void setLinkSubjectStudy(LinkSubjectStudy linkSubjectStudy) {
		this.linkSubjectStudy = linkSubjectStudy;
	}

	public Collection<Phone> getPhoneList() {
		return phoneList;
	}

	public void setPhoneList(Collection<Phone> phoneList) {
		this.phoneList = phoneList;
	}

	public Collection<Address> getAddressList() {
		return addressList;
	}

	public void setAddressList(Collection<Address> addressList) {
		this.addressList = addressList;
	}

	public void setOtherIDList(Collection<OtherID> otherIDList) {
		this.otherIDs = otherIDList;
	}
	
	public Collection<OtherID> getOtherIDList() {
		return otherIDs;
	}
	
	public Collection<EmailAccount> getEmailAccountList() {
		return emailAccountList;
	}

	public void setEmailAccountList(Collection<EmailAccount> emailAccountList) {
		this.emailAccountList = emailAccountList;
	}

	public SubjectStatus getSubjectStatus() {
		return subjectStatus;
	}

	public void setSubjectStatus(SubjectStatus subjectStatus) {
		this.subjectStatus = subjectStatus;
	}

	public Collection<SubjectVO> getSubjectList() {
		return subjectList;
	}

	public void setSubjectList(Collection<SubjectVO> subjectList) {
		this.subjectList = subjectList;
	}

	public SubjectFile getSubjectFile() {
		return subjectFile;
	}

	public void setSubjectFile(SubjectFile subjectFile) {
		this.subjectFile = subjectFile;
	}

	public ConsentAnswer getConsentAnswerSelect() {
		return consentAnswerSelect;
	}

	public void setConsentAnswerSelect(ConsentAnswer consentAnswerSelect) {
		this.consentAnswerSelect = consentAnswerSelect;
	}

	/**
	 * @param subjectPreviousLastname
	 *           the subjectPreviousLastname to set
	 */
	public void setSubjectPreviousLastname(String subjectPreviousLastname) {
		this.subjectPreviousLastname = subjectPreviousLastname;
	}

	/**
	 * @return the subjectPreviousLastname
	 */
	public String getSubjectPreviousLastname() {
		return subjectPreviousLastname;
	}

	public Collection<SubjectFile> getSubjectFileList() {
		return subjectFileList;
	}

	public void setSubjectFileList(Collection<SubjectFile> subjectFileList) {
		this.subjectFileList = subjectFileList;
	}

	public String getSubjectUID() {
		return subjectUID;
	}

	public void setSubjectUID(String subjectUID) {
		this.subjectUID = subjectUID;
	}
	/**
	 * @param availableChildStudies the availableChildStudies to set
	 */
	public void setAvailableChildStudies(List<Study> availableChildStudies) {
		this.availableChildStudies = availableChildStudies;
	}

	/**
	 * @return the availableChildStudies
	 */
	public List<Study> getAvailableChildStudies() {
		return availableChildStudies;
	}

	/**
	 * @param selectedChildStudies the selectedChildStudies to set
	 */
	public void setSelectedChildStudies(List<Study> selectedChildStudies) {
		this.selectedChildStudies = selectedChildStudies;
	}

	/**
	 * @return the selectedChildStudies
	 */
	public List<Study> getSelectedChildStudies() {
		return selectedChildStudies;
	}
	
	/**
	 * @return the studyList
	 */
	public List<Study> getStudyList() {
		return studyList;
	}

	/**
	 * @param studyList
	 *           the studyList to set
	 */
	public void setStudyList(List<Study> studyList) {
		this.studyList = studyList;
	}
	
	public List<String> getRelativeUIDs() {
		return relativeUIDs;
	}

	public void setRelativeUIDs(List<String> relativeUIDs) {
		this.relativeUIDs = relativeUIDs;
	}

	public boolean isEnableNewButton() {
		return enableNewButton;
	}

	public void setEnableNewButton(boolean enableNewButton) {
		this.enableNewButton = enableNewButton;
	}

	public String getParentType() {
		return parentType;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "SubjectVO [subjectUID=" + subjectUID + "]";
	}

	@Override
	public String getArkVoName() {
		return "Subject";
	}

	public boolean isChangingLastName() {
		return changingLastName;
	}

	public void setChangingLastName(boolean changingLastName) {
		this.changingLastName = changingLastName;
	}

	
}
