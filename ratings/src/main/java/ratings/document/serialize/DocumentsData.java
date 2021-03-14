package ratings.document.serialize;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.control.Tab;

import ratings.document.Document;
import ratings.document.DocumentsManager;

import ratings.document.serialize.data.TabData;

import ratings.document.table.tableview.Table;
import ratings.document.table.tableview.TableType;
import ratings.document.table.tableview.impl.ImportTable;
import ratings.document.table.tableview.impl.rating.Rating;
import ratings.document.table.tableview.impl.rating.RatingTable;
import ratings.document.table.xlsx.WorksheetsParser.TableItem;

import ratings.themes.ThemeManager;
import ratings.themes.ThemeType;

public class DocumentsData implements Serializable {

	private static final long serialVersionUID = 8190530134047162454L;

	public static final String OBJECT_FILE_NAME = "documents.out";

	// documents data
	private DocumentData[] documents;
	private TabData[] tabs;

	// app data
	private ThemeType themeType = ThemeType.DEFAULT;

	private DocumentData parseDocument(Document doc) {
		TableData[] tables = new TableData[doc.tablesCount()];

		int i = 0;
		for (Table<?> table : doc) {
			TableData data = new TableData();

			if (table instanceof ImportTable) {
				data.tableType = TableType.IMPORT;

				ImportTable importTable = (ImportTable) table;

				List<Object> items = new ArrayList<>(importTable.getHeadItems());
				importTable.getItems().forEach(item -> {
					List<TableItem> tableItems = new ArrayList<>();

					item.forEach(e -> {
						tableItems.add(new TableItem(importTable.getBoldRows().contains(item), e));
					});

					items.add(tableItems);
				});
				data.items = items;
			} else if (table instanceof RatingTable) {
				data.tableType = TableType.RATING;

				RatingTable ratingTable = (RatingTable) table;

				List<Rating> items = new ArrayList<>();
				ratingTable.getItems().forEach(item -> items.add(item));

				data.items = items;
			}

			data.tableName = table.getTableName();
			tables[i++] = data;
		}

		return new DocumentData(tables, doc.getName());
	}

	public void setTabs(List<Tab> tabsList, Tab selectionItem) {
		int len = tabsList.size();
		tabs = new TabData[len];
		for (int i = 0; i < len; i++) {
			Table<?> table = (Table<?>) tabsList.get(i).getUserData();

			TabData data = new TabData();

			data.documentName = tabsList.get(i).getId();
			data.tableName = table.getTableName();
			data.isSelect = tabsList.get(i).equals(selectionItem);

			tabs[i] = data;
		}
	}

	public void setTheme() {
		themeType = ThemeManager.getCurrentTheme();
	}

	public void saveAllDocuments() {
		DocumentsManager docs = DocumentsManager.getInstance();

		int len = docs.getDocuments().size();
		documents = new DocumentData[len];
		for (int i = 0; i < len; i++) {
			documents[i] = parseDocument(docs.getDocuments().get(i));
		}
	}

	public TabData[] getTabs() {
		return tabs;
	}

	@SuppressWarnings("unchecked")
	public void initAllDocuments() {
		if (documents == null)
			return;

		DocumentsManager docs = DocumentsManager.getInstance();

		for (DocumentData doc : documents) {
			Document document = new Document();
			document.setName(doc.documentName);

			docs.addDocument(document);

			for (TableData table : doc.tables) {
				if (table.tableType == TableType.IMPORT) {
					List<List<TableItem>> items = new ArrayList<>();
					table.items.forEach(item -> items.add((List<TableItem>) item));

					document.addTable(new ImportTable(items, table.tableName));
				} else if (table.tableType == TableType.RATING) {
					ObservableList<Rating> items = FXCollections.observableArrayList();
					table.items.forEach(item -> items.add((Rating) item));

					document.addTable(new RatingTable(table.tableName, items));
				}
			}
		}
	}

	public void initAppSettings() {
		ThemeManager.setTheme(themeType);
	}
}

class DocumentData implements Serializable {

	private static final long serialVersionUID = -1148601345783828274L;

	public TableData[] tables;
	public String documentName;

	public DocumentData(TableData[] tables, String documentName) {
		this.tables = tables;
		this.documentName = documentName;
	}
}

class TableData implements Serializable {

	private static final long serialVersionUID = 1356335081741983849L;

	public TableType tableType;
	public String tableName;
	public List<?> items;
}
