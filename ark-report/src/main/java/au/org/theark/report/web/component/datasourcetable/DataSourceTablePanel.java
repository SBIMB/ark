package au.org.theark.report.web.component.datasourcetable;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import au.org.theark.core.Constants;
import au.org.theark.core.web.component.export.ExportToolbar;
import au.org.theark.core.web.component.export.ExportableTextColumn;
import au.org.theark.core.web.component.export.ResultSetDataProvider;

public class DataSourceTablePanel extends Panel {

	private static final long	serialVersionUID	= 1L;
	private String					dbName;
	private String					query;

	public DataSourceTablePanel(String id, String dbName, String query) {
		super(id);
		this.dbName = dbName;
		this.query = query;
		setOutputMarkupPlaceholderTag(true);
		initDataSourceTable();
	}

	@SuppressWarnings("unchecked")
	public void initDataSourceTable() {
		ResultSetDataProvider prov = new ResultSetDataProvider(dbName, query);
		prov.setQuery(query);

		List<IColumn<?>> cols = new ArrayList<IColumn<?>>();

		for (int i = 0; i < prov.getColumnCount(); i++) {
			//cols.add(new MetaDataColumn(prov, i));
			//TODO: check this still works...
			cols.add(new ExportableTextColumn<Void>(Model.of(prov.getColNames().get(i)), prov.getColNames().get(i)));
		}

		DataTable table = new DataTable("dataTable", cols, prov, Constants.ROWS_PER_PAGE);
		table.addTopToolbar(new HeadersToolbar(table, prov));
		table.addBottomToolbar(new ExportToolbar(table, prov.getColNames(), null));
		add(table);
	}

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName
	 *           the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query
	 *           the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}
}