package ratings.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ratings.document.table.tableview.Table;
import ratings.document.table.xlsx.WorksheetsParser;

public class Document implements Iterable<Table<?>> {

	private String name;

	private List<Table<?>> tables = new ArrayList<>();

	public Table<?> getTable(String name) {
		for (Table<?> table : tables) {
			if (table.getTableName().equals(name)) {
				return table;
			}
		}
		return null;
	}

	public void addTable(Table<?> table) {
		tables.add(table);
		table.setDocument(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int tablesCount() {
		return tables.size();
	}

	@Override
	public Iterator<Table<?>> iterator() {
		return tables.iterator();
	}

	public static Document buildDocumentByXLSX(File xlsx) throws IOException {
		WorksheetsParser parser = new WorksheetsParser(xlsx);
		return new Document() {{
			parser.getAllTables().forEach(table -> addTable(table));
			setName(xlsx.getName());
		}};
	}
}
