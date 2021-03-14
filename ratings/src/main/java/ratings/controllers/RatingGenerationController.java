package ratings.controllers;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Cursor;
import javafx.stage.Window;
import javafx.util.Pair;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import ratings.alerts.Alert;
import ratings.document.Document;
import ratings.document.DocumentsManager;

import ratings.document.table.tableview.Table;
import ratings.document.table.tableview.impl.ImportTable;
import ratings.document.table.tableview.impl.rating.Rating;
import ratings.document.table.tableview.impl.rating.RatingTable;

/**
 * Контроллер окна генерации рейтинга по двум группам
 *
 */
public class RatingGenerationController implements Initializable {

	@FXML
	private VBox pane;

	@FXML
	private FlowPane checkBoxesPane;

	@FXML
	private Label documentName;

	@FXML
	private Button button;

	private List<CheckBox> checkBoxes = new ArrayList<>();
	private int selectedCheckBoxes;

	private Pair<ImportTable, ImportTable> selectedTables;

	private DocumentsManager documents = DocumentsManager.getInstance();

	@FXML
	public void onButtonClick() {
		if (selectedTables.getKey().getBoldRows().isEmpty() &&
			selectedTables.getValue().getBoldRows().isEmpty()) {

			Alert.show("Інформація", "Увага!", "Виділіть бюджетників");
		} else {
			Document doc = documents.getGenerateRatingDocument();
			Table<?> table = new RatingTable(getTableName(), prepareTableItems());

			doc.addTable(table);
			documents.getController().addTable(table);
		}

		Window window = button.getScene().getWindow();
		window.hide();
	}

	private static class Item {
		public String group;
		public float averageScore;
		public List<String> row;
		public boolean social;
	}

	private String getGroup(ImportTable table) {
		return table.getTableName().split(" ")[0];
	}

	private String getTableName() {
		Document doc = documents.getGenerateRatingDocument();
		String name = "Рейтинг " +
				selectedTables.getKey().getTableName() + ", " +
				selectedTables.getValue().getTableName();

		if (doc.getTable(name) == null)
			return name;

		int i = 1;
		while (true) {
			String temp = name + " [" + i++ + "]";
			if (doc.getTable(temp) == null)
				return temp;
		}
	}

	private void addAllRows(List<Item> allItems, ImportTable table) {
		for (List<String> row : table.getBoldRows()) {
			Item item = new Item();

			item.group = getGroup(table);
			item.averageScore = table.getAverageScoreByRow(row);
			item.row = row;

			allItems.add(item);
		}

		for (List<String> row : table.getSocialScholarshipRows()) {
			Item item = new Item();

			item.group = getGroup(table);
			item.averageScore = table.getAverageScoreByRow(row);
			item.row = row;
			item.social = true;

			allItems.add(item);
		}
	}

	private ObservableList<Rating> prepareTableItems() {
		List<Item> allItems = new ArrayList<>();

		addAllRows(allItems, selectedTables.getKey());
		addAllRows(allItems, selectedTables.getValue());

		ObservableList<Rating> items = FXCollections.observableArrayList();
		int i = 1;
		for (Item item : allItems) {
			Rating rating = new Rating();

			rating.setNumber(i++);
			rating.setName(item.row.get(1));
			rating.setGroup(item.group);
			rating.setAverageScore(item.averageScore);
			rating.setConsolidatedScore(item.averageScore);
			rating.setSocialScholarship(item.social);

			items.add(rating);
		}

		return items;
	}

	private void setSelectedTables() {
		Document doc = documents.getGenerateRatingDocument();

		ImportTable a = null;
		for (CheckBox check : checkBoxes) {
			if (check.isSelected()) {
				if (a == null) {
					a = (ImportTable) doc.getTable(check.getText());
				} else {
					selectedTables = new Pair<>(a, (ImportTable) doc.getTable(check.getText()));
				}
			}
		}
	}

	private void onCheckBoxClick(CheckBox check) {
		selectedCheckBoxes = check.isSelected() ? selectedCheckBoxes + 1 : selectedCheckBoxes - 1;
		for (CheckBox checkBox : checkBoxes) {
			if (selectedCheckBoxes == 2) {
				if (!checkBox.isSelected()) {
					checkBox.setDisable(true);
				}
				setSelectedTables();
			} else {
				checkBox.setDisable(false);
			}
		}

		button.setDisable(selectedCheckBoxes != 2);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Document doc = documents.getGenerateRatingDocument();
		documentName.setText(doc.getName());

		for (Table<?> table : doc) {
			if (table instanceof ImportTable) {
				CheckBox check = new CheckBox(table.getTableName());

				check.setCursor(Cursor.HAND);
				check.setOnAction(e -> onCheckBoxClick(check));

				checkBoxes.add(check);
				checkBoxesPane.getChildren().add(check);
			}
		}
	}
}
