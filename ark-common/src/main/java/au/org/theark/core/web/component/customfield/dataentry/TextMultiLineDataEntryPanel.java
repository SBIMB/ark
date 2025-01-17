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
package au.org.theark.core.web.component.customfield.dataentry;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidator;

/**
 * TextField based Data entry panel for text
 * @author elam
 * @author cellis
 */
public class TextMultiLineDataEntryPanel extends AbstractDataEntryPanel<String> {

	private static final long		serialVersionUID	= 1L;
	protected TextArea<String>	dataValueTxtArea;

	/**
	 * TextDataEntryPanel Constructor
	 * 
	 * @param id
	 *           - component id
	 * @param dataModel
	 *           - must be a model a String dataValue
	 * @param labelModel
	 *           - field-specific String label model to be used for feedback
	 */
	public TextMultiLineDataEntryPanel(String id, IModel<String> dataModel, IModel<String> labelModel) {
		super(id, labelModel);
		dataValueModel = dataModel;

		dataValueTxtArea = new TextArea<String>("textDataValue", dataValueModel);
		dataValueTxtArea.setLabel(fieldLabelModel); // set the ${label} for feedback messages
		this.add(dataValueTxtArea);
	}

	public TextArea<String> getDataValueTxtFld() {
		return dataValueTxtArea;
	}

	@Override
	public void setRequired(boolean required) {
		dataValueTxtArea.setRequired(required);
	}

	@Override
	public void addValidator(IValidator<String> aValidator) {
		dataValueTxtArea.add(aValidator);
	}

	@Override
	protected DataEntryType getDataEntryType() {
		return DataEntryType.TEXTMULTILINE;
	}
	
	/**
	 * Set the text field size
	 * @param size
	 */
	public void setTextFieldSizeInPixel(final int widthInPixel,final int heightInPixel){
		this.dataValueTxtArea.add(new AttributeModifier("style", new Model<String>("width:"+widthInPixel+"px;height:"+heightInPixel+"px;")));
	}
}