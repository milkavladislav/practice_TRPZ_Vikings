package ratings.document.table.tableview;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import ratings.document.Document;

public abstract class Table<T> extends TableView<T> implements Exportable {

	public static final int MIN_CELL_WIDTH = 40;

	protected String tableName;
	protected Document document;

	public Table() {
		setEditable(true);

		GridPane.setHgrow(this, Priority.ALWAYS);
		GridPane.setVgrow(this, Priority.ALWAYS);

		setCursor(Cursor.HAND);
	}

	public void autoResizeColumns() {
		setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		getColumns().forEach(column -> {
			Text text = new Text(column.getText());
			double max = text.getLayoutBounds().getWidth();

			for (int i = 0; i < getItems().size(); i++) {
				if (column.getCellData(i) != null) {
					text = new Text(column.getCellData(i).toString());

					double calcwidth = text.getLayoutBounds().getWidth();
					if (calcwidth > max) {
						max = calcwidth;
					}
				}
			}

			double width = max + 15;
	        column.setPrefWidth(width < MIN_CELL_WIDTH ? MIN_CELL_WIDTH : width);
		});
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	protected void setColumnRotationName(String text, TableColumn<?, ?> column) {
		Label label = new Label(text);
		VBox vbox = new VBox(label);
	    vbox.setRotate(-90);

	    column.setId(text);
	    column.setGraphic(new Group(vbox));
	}

	protected void setCellBorder(CellRangeAddress region, Sheet sheet) {
		RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	public abstract void addEmptyRow();

	@Override
	public String toString() {
		return tableName;
	}
}
