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
 ******************************************************************************//*
package au.org.theark.lims.web.component.biospecimencustomdata;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import au.org.theark.core.service.IArkCommonService;
import au.org.theark.core.vo.BiospecimenCustomDataVO;
import au.org.theark.core.web.component.customfield.dataentry.AbstractCustomDataEditorForm;
import au.org.theark.lims.web.component.biospecimencustomdata.form.CustomDataEditorForm;


*//**
 * @author elam
 * 
 *//*
@SuppressWarnings({ "serial" })
public class BiospecimenCustomDataEditorPanel extends Panel {


	private static final long		serialVersionUID	= -1L;

	@SpringBean(name = au.org.theark.core.Constants.ARK_COMMON_SERVICE)
	private IArkCommonService		iArkCommonService;
	
	private CompoundPropertyModel<BiospecimenCustomDataVO>			cpModel;
	
	protected FeedbackPanel				feedbackPanel;
	protected AbstractCustomDataEditorForm<BiospecimenCustomDataVO>	customDataEditorForm;
	protected BiospecimenCustomDataDataViewPanel dataViewPanel;

	public BiospecimenCustomDataEditorPanel(String id, CompoundPropertyModel<BiospecimenCustomDataVO> cpModel, FeedbackPanel feedBackPanel) {
		super(id);

		this.cpModel = cpModel;
		this.feedbackPanel = feedBackPanel;
	}
	
	public BiospecimenCustomDataEditorPanel initialisePanel() {
		
		dataViewPanel = new BiospecimenCustomDataDataViewPanel("dataViewPanel", cpModel).initialisePanel(iArkCommonService.getRowsPerPage());

		customDataEditorForm = new CustomDataEditorForm("customDataEditorForm", cpModel, feedbackPanel).initialiseForm(true);
		AjaxPagingNavigator pageNavigator = new AjaxPagingNavigator("navigator", dataViewPanel.getDataView()) {
			@Override
			protected void onAjaxEvent(AjaxRequestTarget target) {
				target.add(customDataEditorForm.getDataViewWMC());
				target.add(this);
			}
		};
		pageNavigator.setOutputMarkupId(true);
		customDataEditorForm.getDataViewWMC().add(dataViewPanel);
		this.add(customDataEditorForm);
		this.add(pageNavigator);
		
		return this;
	}
	
}
*/