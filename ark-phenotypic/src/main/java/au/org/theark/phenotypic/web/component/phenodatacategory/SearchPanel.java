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
package au.org.theark.phenotypic.web.component.phenodatacategory;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;

import au.org.theark.core.vo.ArkCrudContainerVO;
import au.org.theark.core.vo.CustomFieldCategoryVO;
import au.org.theark.core.vo.PhenoDataSetCategoryVO;
import au.org.theark.phenotypic.web.component.phenodatacategory.form.SearchForm;


/**
 * @author elam
 * 
 */
@SuppressWarnings("serial")
public class SearchPanel extends Panel {
	
	private CompoundPropertyModel<PhenoDataSetCategoryVO> cpModel;

	private FeedbackPanel			feedbackPanel;
	private ArkCrudContainerVO		arkCrudContainerVO;
	

	/* Constructor */
	public SearchPanel(String id, CompoundPropertyModel<PhenoDataSetCategoryVO> cpModel, 
			ArkCrudContainerVO arkCrudContainerVO, FeedbackPanel feedBackPanel) {
		super(id);
		this.cpModel = cpModel;
		this.arkCrudContainerVO = arkCrudContainerVO;
		this.feedbackPanel = feedBackPanel;
	}

	public void initialisePanel() {
		SearchForm searchForm = new SearchForm(au.org.theark.core.Constants.SEARCH_FORM, cpModel, feedbackPanel, arkCrudContainerVO);
		add(searchForm);
	}
}
