package ratings.document.table.tableview.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.text.Document;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import javafx.util.Callback;

import ratings.alerts.ErrorAlert;
import ratings.document.table.tableview.Table;
import ratings.document.table.xlsx.WorksheetsParser.TableItem;
import ratings.utils.NumberUtils;

public class ImportTable extends Table<List<String>> {

	/**
	 * Рядки с бюджетниками
	 */
	private List<List<String>> boldRows = new ArrayList<>();
	private List<List<TableItem>> headItems = new ArrayList<>();

	private int numCols;

	public List<List<TableItem>> getHeadItems() {
		return headItems;
	}

	// нужно для сереализации
	private void initHeadItems(List<TableItem> list) {
		List<TableItem> row = new ArrayList<>();
		list.forEach(e -> row.add(e));
		headItems.add(row);
	}

	public ImportTable(List<List<TableItem>> rawData, String tableName) {
		this.tableName = tableName;

		initHeadItems(rawData.get(0));
		initHeadItems(rawData.get(1));

		numCols = rawData.get(0).size();

		TableColumn<List<String>, String> temp = new TableColumn<>("");
		List<TableItem> firstHeaders = rawData.get(0);

		for (int columnIndex = 0; columnIndex < numCols; columnIndex++) {
			TableColumn<List<String>, String> col = createColumn(columnIndex, "");
			setColumnRotationName(rawData.get(1).get(columnIndex).getValue(), col);

			if (!firstHeaders.get(columnIndex).getValue().isEmpty()) {
				temp = new TableColumn<>(firstHeaders.get(columnIndex).getValue());
			}

			temp.getColumns().add(col);

			if (!getColumns().contains(temp)) {
				getColumns().add(temp);
			}
		}

		// rowIndex = 2, т.к. на первых двух рядках заголовки
		for (int rowIndex = 2; rowIndex < rawData.size(); rowIndex++) {
			List<String> row = new ArrayList<>();

			for (int columnIndex = 0; columnIndex < numCols; columnIndex++) {
				List<TableItem> items = rawData.get(rowIndex);

				if (columnIndex < items.size()) {
					TableItem item = items.get(columnIndex);
					row.add(item.getValue());

					if (columnIndex == 1) { // name
						if (item.isBold()) {
							boldRows.add(row);
						}
					}
				} else {
					row.add("");
				}
			}

			getItems().add(row);
		}

		setRowFactory();
		autoResizeColumns();
		sortPolicyProperty().set(t -> {
		    FXCollections.sort(getItems(), (a, b) -> getAverageScoreByRow(a) > getAverageScoreByRow(b) ? 1 : -1);
		    FXCollections.sort(getItems(), (a, b) -> boldRows.contains(a) ? -1 : 1);

		    int i = 1;
			for (List<String> item : getItems()) {
				item.set(0, String.valueOf(i++));
			}
		    return true;
		});
	}

	private void setRowFactory() {
		setRowFactory(new Callback<TableView<List<String>>, TableRow<List<String>>>() {
			@Override
			public TableRow<List<String>> call(TableView<List<String>> table) {
				TableRow<List<String>> row = new TableRow<List<String>>() {
					@Override
                    protected void updateItem(List<String> item, boolean empty) {
						super.updateItem(item, empty);

						if (!empty) {
							if (boldRows.contains(item))
								setStyle("-fx-font-weight: bold");
							else
								setStyle("-fx-font-weight: normal");

							if (getContextMenu() != null) {
								CheckMenuItem check = (CheckMenuItem) getContextMenu().getItems().get(0);
								if (!check.isSelected()) {
									check.setSelected(boldRows.contains(item));
								}
							}
						}
					}
				};

				ContextMenu menu = new ContextMenu();

				CheckMenuItem free = new CheckMenuItem("Виділити бюджетника");
				free.setOnAction(e -> {
					if (free.isSelected()) {
						boldRows.add(row.getItem());
						row.setStyle("-fx-font-weight: bold");
					} else {
						boldRows.remove(row.getItem());
						row.setStyle("-fx-font-weight: normal");
					}
				});

				CheckMenuItem remove = new CheckMenuItem("Видалити рядок");
				remove.setOnAction(e -> {
					List<String> selectedItem = table.getSelectionModel().getSelectedItem();
					boldRows.remove(selectedItem);
				    table.getItems().remove(selectedItem);

				    int i = 1;
					for (List<String> item : getItems()) {
						item.set(0, String.valueOf(i++));
					}
				});

				menu.getItems().addAll(free, remove);

				row.setContextMenu(menu);
				return row;
			}
		});
	}

    private TableColumn<List<String>, String> createColumn(int index, String columnHeader) {
    	return new TableColumn<List<String>, String>(columnHeader) {{
    		setCellFactory(TextFieldTableCell.<List<String>>forTableColumn());
    		setMinWidth(25);
    		setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(index).toString()));
    		setEditable(true);
    		setOnEditCommit(e -> {
    			List<String> row = e.getTableView().getItems().get(e.getTablePosition().getRow());
    			row.set(e.getTablePosition().getColumn(), e.getNewValue());
    		});
    	}};
    }

    public float getAverageScoreByRow(List<String> row) {
    	// получаем индексы колонок, которые попадают под подсчет
    	// пропускаем колонки с 12-ти бальными оценкамаи
    	List<Integer> indexes = new ArrayList<>();
    	int inc = 2;

    	for (int i = 2; i < getColumns().size(); i++) {
    		int len = getColumns().get(i).getColumns().size();
    		for (int j = 0; j < len; j++) {
    			TableColumn<List<String>, ?> col = getColumns().get(i).getColumns().get(j);
    			if (j + 1 < len) {
    				TableColumn<List<String>, ?> nextCol =
    						getColumns().get(i).getColumns().get(j + 1);

    				if (col.getId().isEmpty() && nextCol.getId().isEmpty()) {
    					ErrorAlert.show("Table Exception",
    							"Неможливо порахувати середній бал. " +
    							"Перевірте правильність введення даних в .xlsx");
    					return 0;
    				}

    				if (nextCol.getId().isEmpty()) {
    					indexes.add(inc + 1);
    				} else {
    					if (!indexes.contains(inc))
    						indexes.add(inc);
    				}
    			} else {
    				indexes.add(inc);
    			}
    			inc++;
    		}
    	}

    	float averageScore = 0;
    	for (int i : indexes) {
    		averageScore += NumberUtils.getInteger(row.get(i));
    	}

    	return NumberUtils.toFixed(averageScore / indexes.size(), 2);
    }

    @Override
	public Workbook toWorkbook() {
    	Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet(tableName);

		int rowIndex = 0;
		int cellIndex = 0;

		CellStyle style = workbook.createCellStyle();
		style.setRotation((short) 90);

		Row firstRow = sheet.createRow(rowIndex++);
		Row row = sheet.createRow(rowIndex++);

		for (TableColumn<List<String>, ?> column : getColumns()) {
			Cell cell = firstRow.createCell(cellIndex);
			cell.setCellValue(column.getText());

			setCellBorder(CellRangeAddress.valueOf(cell.getAddress().formatAsString()), sheet);

			int startIndex = cellIndex;
			for (int i = 0; i < column.getColumns().size(); i++) {
				TableColumn<List<String>, ?> col = column.getColumns().get(i);

				cell = row.createCell(cellIndex);

				cell.setCellStyle(style);
				cell.setCellValue(col.getId());

				CellRangeAddress cellAdress = CellRangeAddress.valueOf(cell.getAddress().formatAsString());
				setCellBorder(cellAdress, sheet);

				cellIndex++;
			}

			// обьеденяем ячейки, если их количество > 1
			if (startIndex != cellIndex - 1) {
				CellRangeAddress cellAdress = new CellRangeAddress(0, 0, startIndex, cellIndex - 1);

				sheet.addMergedRegion(cellAdress);
				setCellBorder(cellAdress, sheet);
			}
		}

		Font font = workbook.createFont();
		font.setBold(true);

		CellStyle bold = workbook.createCellStyle();
		bold.setFont(font);

		for (List<?> item : getItems()) {
			row = sheet.createRow(rowIndex++);
			cellIndex = 0;

			int i = 0;
			for (Object cellData : item) {
				Cell cell = row.createCell(cellIndex);

				if (i == 1 && boldRows.contains(item)) // ФИО
					cell.setCellStyle(bold);

				String strData = cellData.toString();

				if (NumberUtils.isNumeric(strData))
					cell.setCellValue(Integer.valueOf(strData));
				else
					cell.setCellValue(strData);

				CellRangeAddress cellAdress = CellRangeAddress.valueOf(cell.getAddress().formatAsString());
				setCellBorder(cellAdress, sheet);

				sheet.autoSizeColumn(cellIndex++);
				i++;
			}
		}

		return workbook;
	}

    public List<List<String>> getBoldRows() {
    	return boldRows;
    }

	@Override
	public void toPDF(Document document) {
		// нет смысла что-то делать, таблицы широкие для pdf
	}

	@Override
	public void addEmptyRow() {
		List<String> newItem = new ArrayList<>();
		for (int i = 0; i < numCols; i++) {
			newItem.add("");
		}
		getItems().add(newItem);

		int i = 1;
		for (List<String> item : getItems()) {
			item.set(0, String.valueOf(i++));
		}
	}
}
