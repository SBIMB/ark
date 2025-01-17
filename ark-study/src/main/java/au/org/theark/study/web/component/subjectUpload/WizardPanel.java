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
package au.org.theark.study.web.component.subjectUpload;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;

import au.org.theark.core.vo.ArkCrudContainerVO;
import au.org.theark.core.web.component.worksheet.ArkExcelWorkSheetAsGrid;
import au.org.theark.study.web.component.subjectUpload.form.ContainerForm;
import au.org.theark.study.web.component.subjectUpload.form.WizardForm;

public class WizardPanel extends Panel {

	private static final long			serialVersionUID	= -3052058152472704291L;
	private ArkCrudContainerVO			arkCrudContainerVO;
	private WizardForm					wizardForm;
	private FeedbackPanel				feedBackPanel;
	private ContainerForm				containerForm;
	private ArkExcelWorkSheetAsGrid	arkExcelWorkSheetAsGrid;

	public WizardPanel(String id, FeedbackPanel feedBackPanel, ContainerForm containerForm, ArkCrudContainerVO arkCrudContainerVO) {
		super(id);
		this.feedBackPanel = feedBackPanel;
		this.containerForm = containerForm;
		this.arkCrudContainerVO = arkCrudContainerVO;
	}

	public void initialisePanel() {
		wizardForm = new WizardForm("wizardForm", feedBackPanel, containerForm, arkCrudContainerVO);
		wizardForm.initialiseDetailForm();
		add(wizardForm);
		setOutputMarkupPlaceholderTag(true);
	}

	public WizardForm getWizardForm() {
		return wizardForm;
	}

	public void setWizardForm(WizardForm wizardForm) {
		this.wizardForm = wizardForm;
	}

	/**
	 * @param arkExcelWorkSheetAsGrid
	 *           the arkExcelWorkSheetAsGrid to set
	 */
	public void setArkExcelWorkSheetAsGrid(ArkExcelWorkSheetAsGrid arkExcelWorkSheetAsGrid) {
		this.arkExcelWorkSheetAsGrid = arkExcelWorkSheetAsGrid;
	}

	/**
	 * @return the arkExcelWorkSheetAsGrid
	 */
	public ArkExcelWorkSheetAsGrid getArkExcelWorkSheetAsGrid() {
		return arkExcelWorkSheetAsGrid;
	}
}
