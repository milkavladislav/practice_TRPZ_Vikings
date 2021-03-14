package ratings.document.table.tableview.impl.rating;

import java.io.IOException;
import java.util.Comparator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import javafx.util.Callback;

import ratings.document.table.tableview.Table;
import ratings.utils.NumberUtils;

public class RatingTable extends Table<Rating> {

	private static final Font PDF_FONT;
	static {
			BaseFont baseFont = null;
			try {
				byte[] bytes = IOUtils.toByteArray(RatingTable.class.getResourceAsStream("/times-new-roman.ttf"));
				baseFont = BaseFont.createFont("times-new-roman.ttf", BaseFont.IDENTITY_H,
						BaseFont.EMBEDDED, true, bytes, null);
			} catch (DocumentException | IOException e) {
				e.printStackTrace();
			}

			PDF_FONT = new Font(baseFont, 10, Font.NORMAL, BaseColor.BLACK);
	}

	public RatingTable(String tableName, ObservableList<Rating> items) {
		this.tableName = tableName;

		getColumns().add(createColumn("№", "number", false, false));

		getColumns().add(createColumn("Прізвище, ім’я, по батькові", "name", true, true));
		getColumns().add(createColumn("Група", "group", true, true));
		getColumns().add(createColumn("Середній бал", "averageScore", true, false));

		getColumns().add(createActivityColumn("Спортивна діяльність", "sportActivityPercent"));
		getColumns().add(createActivityColumn("Творча діяльність", "creativeActivityPercent"));
		getColumns().add(createActivityColumn("Громадянська діяльність", "civilActivityPercent"));
		getColumns().add(createActivityColumn("Наукова діяльність", "scientificActivityPercent"));

		getColumns().add(createColumn("Всього додано (%)", "percent", true, false));
		getColumns().add(createColumn("Всього додано (бал)", "score", true, false));

		getColumns().add(createColumn("Консолідований бал", "consolidatedScore", true, false));

		setItems(items);

		autoResizeColumns();
		sortPolicyProperty().set(t -> {
		    Comparator<Rating> comparator = (a, b) -> {
		    	return a.getConsolidatedScore() < b.getConsolidatedScore() ? 1 : -1;
		    };
		    FXCollections.sort(getItems(), comparator);

		    int i = 1;
			for (Rating rating : getItems()) {
				rating.setNumber(i++);
			}
		    return true;
		});
		setRowFactory();
	}

	private void setRowFactory() {
		setRowFactory(new Callback<TableView<Rating>, TableRow<Rating>>() {
			@Override
			public TableRow<Rating> call(TableView<Rating> param) {
				TableRow<Rating> row = new TableRow<>();
				ContextMenu menu = new ContextMenu();

				CheckMenuItem remove = new CheckMenuItem("Видалити рядок");
				remove.setOnAction(e -> {
				    getItems().remove(getSelectionModel().getSelectedItem());

				    int i = 1;
					for (Rating item : getItems()) {
						item.setNumber(i++);
					}
				});

				menu.getItems().add(remove);
				row.setContextMenu(menu);

				return row;
			}
		});
	}

	private TableColumn<Rating, ?> createColumn(String text, String property, boolean rotateHeader, boolean editable) {
		TableColumn<Rating, String> column = new TableColumn<>();
		column.setCellValueFactory(new PropertyValueFactory<>(property));

		if (editable) {
			column.setCellFactory(TextFieldTableCell.<Rating>forTableColumn());
			column.setOnEditCommit(e -> {
				Rating row = getItems().get(e.getTablePosition().getRow());
				row.setField(property, e.getNewValue());

				refresh();
				autoResizeColumns();
			});
		}

		if (rotateHeader) {
			column.setId(text);
			setColumnRotationName(text, column);
		} else {
			column.setText(text);
		}

		return column;
	}

	private TableColumn<Rating, String> createActivityColumn(String text, String property) {
		TableColumn<Rating, String> activity = new TableColumn<>();
		activity.setPrefWidth(MIN_CELL_WIDTH);
		activity.setId(text);
		setColumnRotationName(text, activity);

		activity.setCellValueFactory(new PropertyValueFactory<>(property));
		activity.setSortable(false);
		activity.setCellFactory(TextFieldTableCell.<Rating>forTableColumn());
		activity.setOnEditCommit(e -> {
			Rating row = getItems().get(e.getTablePosition().getRow());

			row.setField(property, e.getNewValue());
			row.setPercent(getAllActivityPercent(row));
			row.setScore(calculateScore(row));
			row.setConsolidatedScore(calculateConsolidatedScore(row));

			refresh();
			autoResizeColumns();
		});

		return activity;
	}

	private String getAllActivityPercent(Rating row) {
		return Integer.toString(
			NumberUtils.getInteger(row.getCivilActivityPercent()) +
			NumberUtils.getInteger(row.getCreativeActivityPercent()) +
			NumberUtils.getInteger(row.getSportActivityPercent()) +
			NumberUtils.getInteger(row.getScientificActivityPercent()));
	}

	private String calculateScore(Rating row) {
		float averageScore = row.getAverageScore();
		int allActivity = NumberUtils.getInteger(getAllActivityPercent(row));

		return String.valueOf(NumberUtils.toFixed(((averageScore/(100 - allActivity)) * allActivity), 2));
	}

	private float calculateConsolidatedScore(Rating row) {
		float averageScore = row.getAverageScore();
		float score = Float.valueOf(row.getScore());

		return NumberUtils.toFixed(averageScore + score, 2);
	}

	private Cell addCellToRow(Row row, int cellIndex, String value, Sheet sheet) {
		Cell cell = row.createCell(cellIndex);
		cell.setCellValue(value);
		setCellBorder(CellRangeAddress.valueOf(cell.getAddress().formatAsString()), sheet);
		sheet.autoSizeColumn(cellIndex);
		return cell;
	}

	private Cell addCellToRow(Row row, int cellIndex, double value, Sheet sheet) {
		Cell cell = row.createCell(cellIndex);
		cell.setCellValue(value);
		setCellBorder(CellRangeAddress.valueOf(cell.getAddress().formatAsString()), sheet);
		sheet.autoSizeColumn(cellIndex);
		return cell;
	}

	@Override
	public Workbook toWorkbook() {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(tableName);

		int rowIndex = 0;
		int cellIndex = 0;

		CellStyle style = workbook.createCellStyle();
		style.setRotation((short) 90);

		Row row = sheet.createRow(rowIndex++);

		for (TableColumn<Rating, ?> column : getColumns()) {
			Cell cell = row.createCell(cellIndex++);

			if (column.getText().isEmpty())
				cell.setCellStyle(style);

			cell.setCellValue(column.getText().isEmpty() ? column.getId() : column.getText());
			setCellBorder(CellRangeAddress.valueOf(cell.getAddress().formatAsString()), sheet);
		}

		cellIndex = 0;
		for (Rating item : getItems()) {
			row = sheet.createRow(rowIndex++);

			addCellToRow(row, cellIndex++, item.getNumber(), sheet);
			addCellToRow(row, cellIndex++, item.getName(), sheet);
			addCellToRow(row, cellIndex++, item.getGroup(), sheet);
			addCellToRow(row, cellIndex++, String.valueOf(NumberUtils.toFixed(item.getAverageScore(), 2)), sheet);
			addCellToRow(row, cellIndex++, item.getSportActivityPercent(), sheet);
			addCellToRow(row, cellIndex++, item.getCreativeActivityPercent(), sheet);
			addCellToRow(row, cellIndex++, item.getCivilActivityPercent(), sheet);
			addCellToRow(row, cellIndex++, item.getScientificActivityPercent(), sheet);
			addCellToRow(row, cellIndex++, item.getPercent(), sheet);
			addCellToRow(row, cellIndex++, item.getScore(), sheet);
			addCellToRow(row, cellIndex++, String.valueOf(NumberUtils.toFixed(item.getConsolidatedScore(), 2)), sheet);

			cellIndex = 0;
		}
		return workbook;
	}

	private PdfPCell addCellPDF(PdfPTable table, String value) {
		return table.addCell(new PdfPCell(new Phrase(value, PDF_FONT)));
	}

	@Override
	public void toPDF(Document document) {
		try {
			document.open();
			document.newPage();

			PdfPTable table = new PdfPTable(11);
			table.setWidths(new int[]{
				1300, // №
				8700, // Прізвище, ім’я, по батькові
				1300, // Група
				1700, // Середній бал
				1300, // Спортивна діяльність
				1300, // Творча діяльність
				1300, // Громадянська діяльність
				1300, // Наукова діяльність
				1400, // Всього, додано балів (%)
				1400, // Всього, додано балів (бал)
				1700  // Консолідований бал
			});

			for (TableColumn<Rating, ?> column : getColumns()) {
				String text = column.getText().isEmpty() ? column.getId() : column.getText();
				PdfPCell c = new PdfPCell(new Phrase(text, PDF_FONT));

				if (column.getText().isEmpty())
					c.setRotation(90);

				c.setHorizontalAlignment(Element.ALIGN_CENTER);
				c.setVerticalAlignment(Element.ALIGN_MIDDLE);

				table.addCell(c);
			}

			table.setHeaderRows(1);

			for (Rating item : getItems()) {
				addCellPDF(table, String.valueOf(item.getNumber()));
				addCellPDF(table, item.getName());
				addCellPDF(table, item.getGroup());
				addCellPDF(table, String.valueOf(item.getAverageScore()));
				addCellPDF(table, item.getSportActivityPercent());
				addCellPDF(table, item.getCreativeActivityPercent());
				addCellPDF(table, item.getCivilActivityPercent());
				addCellPDF(table, item.getScientificActivityPercent());
				addCellPDF(table, item.getPercent());
				addCellPDF(table, item.getScore());
				addCellPDF(table, String.valueOf(item.getConsolidatedScore()));
			}

			document.add(table);
			document.close();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addEmptyRow() {
		getItems().add(new Rating());
		int i = 1;
		for (Rating rating : getItems()) {
			rating.setNumber(i++);
		}
	}
}
