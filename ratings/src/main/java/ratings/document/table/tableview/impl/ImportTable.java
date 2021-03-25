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
import ratings.alerts.Alert;
import ratings.alerts.ErrorAlert;
import ratings.document.table.tableview.Table;
import ratings.document.table.xlsx.WorksheetsParser.TableItem;
import ratings.utils.NumberUtils;

public class ImportTable extends Table<List<String>> {

	/**
	 * Рядки с бюджетниками
	 */
	private List<List<String>> boldRows = new ArrayList<>();

	/**
	 * Рядки с соц. стипендией
	 */
	private List<List<String>> socialScholarshipRows = new ArrayList<>();

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
						if (item.isSocial()) {
							socialScholarshipRows.add(row);
						} else if (item.isBold()) {
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

    private TableColumn<List<String>, String> createColumn(int index, String columnHeader) {
    	return new TableColumn<List<String>, String>(columnHeader) {{
    		setMinWidth(25);
    		setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(index).toString()));

    		if (index != 0) {
	    		setCellFactory(TextFieldTableCell.<List<String>>forTableColumn());
	    		setEditable(true);
	    		setOnEditCommit(e -> {
	    			List<String> row = e.getTableView().getItems().get(e.getTablePosition().getRow());

					if (index > 1) {
						if (isScore(e.getNewValue())) {
							row.set(e.getTablePosition().getColumn(), e.getNewValue());
						} else {
							Alert.show("Увага!", "Помилка при введенні поля " + columnHeader,
									"Поле може приймати значення в діапазоні [0, 12]");
						}
					} else {
						row.set(e.getTablePosition().getColumn(), e.getNewValue());
					}

	    			refresh();
					autoResizeColumns();
	    		});
    		}
    	}};
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
							if (socialScholarshipRows.contains(item)) {
								setStyle("-fx-text-background-color: red");
							} else if (boldRows.contains(item)) {
								setStyle("-fx-font-weight: bold");
							} else {
								setStyle("");
							}

							if (getContextMenu() != null) {
								CheckMenuItem socialCheck = (CheckMenuItem) getContextMenu().getItems().get(1);
								CheckMenuItem boldCheck = (CheckMenuItem) getContextMenu().getItems().get(0);

								socialCheck.setSelected(socialScholarshipRows.contains(item));
								boldCheck.setSelected(boldRows.contains(item));
							}
						}
					}
				};

				ContextMenu menu = new ContextMenu();

				CheckMenuItem free = new CheckMenuItem("Виділити бюджетника");
				free.setOnAction(e -> {
					menu.getItems().forEach(item -> {
						if (item instanceof CheckMenuItem) {
							CheckMenuItem checkItem = (CheckMenuItem) item;
							if (!checkItem.equals(free)) {
								checkItem.setSelected(false);
							}
						}
					});

					if (free.isSelected()) {
						boldRows.add(row.getItem());
						socialScholarshipRows.remove(row.getItem());

						row.setStyle("-fx-font-weight: bold");
					} else {
						boldRows.remove(row.getItem());
						row.setStyle("");
					}
				});

				CheckMenuItem social = new CheckMenuItem("Соціальна стипендія");
				social.setOnAction(e -> {
					menu.getItems().forEach(item -> {
						if (item instanceof CheckMenuItem) {
							CheckMenuItem checkItem = (CheckMenuItem) item;
							if (!checkItem.equals(social)) {
								checkItem.setSelected(false);
							}
						}
					});

					if (social.isSelected()) {
						socialScholarshipRows.add(row.getItem());
						boldRows.remove(row.getItem());

						row.setStyle("-fx-text-background-color: red");
					} else {
						socialScholarshipRows.remove(row.getItem());
						row.setStyle("");
					}
				});

				CheckMenuItem remove = new CheckMenuItem("Видалити рядок");
				remove.setOnAction(e -> {
					List<String> selectedItem = table.getSelectionModel().getSelectedItem();

					boldRows.remove(selectedItem);
					socialScholarshipRows.remove(selectedItem);

				    table.getItems().remove(selectedItem);

				    int i = 1;
					for (List<String> item : getItems()) {
						item.set(0, String.valueOf(i++));
					}
				});

				menu.getItems().addAll(free, social, remove);

				row.setContextMenu(menu);
				return row;
			}
		});
	}

	/**
	 * Считает средний бал по строке.
	 * Если в оценках есть 2 или 1 - не попадает в рейтинг.
	 * 
	 * @param row строка
	 * @return средний бал
	 */
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
    					return -1;
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
    	int scores = 0;
    	for (int i : indexes) {
    		String value = row.get(i);
    		
    		if (value.equalsIgnoreCase("зар"))
    			continue;
    		
    		int score = NumberUtils.getInteger(row.get(i));
    		if (score == 2 || score == 1)
    			return -1;
    		
    		averageScore += score;
    		scores++;
    	}

    	return NumberUtils.toFixed(averageScore / scores, 4);
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

    public List<List<String>> getSocialScholarshipRows() {
    	return socialScholarshipRows;
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
