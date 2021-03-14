package ratings.document.table.xlsx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ratings.document.table.tableview.Table;
import ratings.document.table.tableview.impl.ImportTable;

public class WorksheetsParser {

	private XSSFWorkbook excel;
	private List<Table<?>> tables = new ArrayList<>();

	public WorksheetsParser(File file) throws IOException {
		excel = new XSSFWorkbook(new FileInputStream(file));
		excel.forEach(sheet -> {
			tables.add(new ImportTable(parseWorksheet(sheet), sheet.getSheetName()));
		});
	}

	public static class TableItem implements Serializable {

		private static final long serialVersionUID = 3135734525412681072L;

		private boolean bold;
		private String value;

		public TableItem(boolean bold, String value) {
			this.bold = bold;
			this.value = value;
		}
		public boolean isBold() {
			return bold;
		}

		public void setBold(boolean bold) {
			this.bold = bold;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public List<Table<?>> getAllTables() {
		return tables;
	}

	private List<List<TableItem>> parseWorksheet(Sheet sheet) {
		List<List<TableItem>> rows = new ArrayList<>();

		int rowSize = 0;
		for (Row row : sheet) {
			List<TableItem> cells = new ArrayList<>();
			for (Cell cell : row) {
				String str = cell.toString();
				str = str.replaceAll(".0", "");

				boolean isBold = excel.getFontAt(cell.getCellStyle().getFontIndexAsInt()).getBold();
				cells.add(new TableItem(isBold, str));
			}

			rows.add(cells);
			if (rowSize < cells.size())
				rowSize = cells.size();
		}

		for (List<TableItem> row : rows) {
			if (row.size() < rowSize) {
				for (int i = row.size(); i < rowSize; i++) {
					row.add(new TableItem(false, ""));
				}
			}
		}

		// удаляем пустые рядки
		rows.removeIf(row -> {
			for (TableItem cell : row) {
				if (!cell.getValue().isEmpty()) {
					return false;
				}
			}
			return true;
		});

		// удаляем пустые колонки
		while (true) {
			int size = 0; // размер не пустых колонок
			for (int i = 0; i < rowSize; i++) {
				if (emptyColumn(rows, i)) {
					for (List<TableItem> row : rows) {
						row.remove(i);
					}
					rowSize--;
					break;
				}
				size++;
			}

			if (size == rowSize) break;
		}

		return rows;
	}

	private boolean emptyColumn(List<List<TableItem>> rows, int index) {
		for (List<TableItem> row : rows) {
			if (!row.get(index).getValue().isEmpty())
				return false;
		}
		return true;
	}
}
