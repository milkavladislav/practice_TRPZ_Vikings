package ratings.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.event.EventHandler;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import javafx.scene.control.Accordion;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseEvent;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import ratings.Main;
import ratings.RatingGenerationWindow;

import ratings.alerts.Alert;
import ratings.alerts.ErrorAlert;

import ratings.document.Document;
import ratings.document.DocumentsManager;

import ratings.document.export.ExportType;
import ratings.document.export.FileExporter;

import ratings.document.serialize.DocumentsData;
import ratings.document.serialize.data.TabData;

import ratings.document.table.tableview.Table;
import ratings.document.table.tableview.impl.ImportTable;
import ratings.document.table.tableview.impl.rating.RatingTable;
import ratings.themes.ThemeManager;
import ratings.themes.ThemeType;

public class Controller implements Initializable {

	private final ExtensionFilter xlsxFilter = new ExtensionFilter("XLSX файл (*.xlsx)", "*.xlsx");
	private final ExtensionFilter pdfFilter = new ExtensionFilter("PDF файл (*.pdf)", "*.pdf");

	@FXML
	private BorderPane pane;

	@FXML
	private ScrollPane tableScrollPane;

	@FXML
	private Accordion projectsList;

	@FXML
	private TabPane tabPane;

	@FXML
	private VBox instruments;

	@FXML
	private GridPane tablePane;
	private Table<?> openTable;

	/**
	 * Кнопки бокового меню инструментов
	 */
	private JFXButton[] buttons;

	private DocumentsData data;

	private DocumentsManager documents = DocumentsManager.getInstance();

	public Controller() {
		Platform.runLater(() -> {
			readSaveDocuments();
			ThemeManager.initStylesgeets();

			Main.getPrimaryStage().setOnCloseRequest(e -> {
				boolean saveChanges = Alert.showConfirmAlert("Увага", "Зберегти зміни?", "");
				new Thread(() -> saveApp(saveChanges)).start();
			});
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initInstruments();

		documents.setController(this);
		tabPane.setOnMouseClicked(e -> openSelectTabTable());
	}

	private void readSaveDocuments() {
		File file = new File(DocumentsData.OBJECT_FILE_NAME);
		if (!file.exists())
			return;

		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DocumentsData.OBJECT_FILE_NAME))) {
			data = (DocumentsData) ois.readObject();

			data.initAllDocuments();
			data.initAppSettings();
			setTheme();

			initTablesLists();
			initTabPane(data.getTabs());
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void saveApp(boolean saveChanges) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DocumentsData.OBJECT_FILE_NAME))) {
			if (data == null)
				data = new DocumentsData();

			data.setTheme();

			if (saveChanges) {
				data.saveAllDocuments();
				data.setTabs(tabPane.getTabs(), tabPane.getSelectionModel().getSelectedItem());
			}

			oos.writeObject(data);
			oos.flush();
		} catch (IOException ex) {
			 ex.printStackTrace();
		}
	}

	private void openTable(Table<?> table) {
		if (openTable != null && openTable.equals(table))
			return;

		tablePane.getChildren().clear();
		tablePane.getChildren().add(table);

		openTable = table;

		String title = "Рейтинг – " + table.getTableName() + " [" + table.getDocument().getName() + "]";
		Main.getPrimaryStage().setTitle(title);

		setButtonsDisable();
	}

	private void closeTable() {
		tablePane.getChildren().clear();
		openTable = null;

		Main.getPrimaryStage().setTitle("Рейтинг");
		setButtonsDisable();
	}

	private void setButtonsDisable() {

		for (JFXButton button : buttons) {
			switch (button.getId()) {
				case "pdf-file-format-symbol":
					// возможность экспорта в PDF есть только у таблиц RatingTable
					button.setDisable(openTable == null || openTable instanceof ImportTable);
					break;
				case "sort":
				case "add-user":
					button.setDisable(openTable == null);
					break;

			}
		}
	}

	@SuppressWarnings("unchecked")
	public void addTable(Table<?> table) {
		String tableDocName = table.getDocument().getName();
		for (TitledPane pane : projectsList.getPanes()) {
			if (pane.getText().equals(tableDocName)) {
				ListView<String> list = (ListView<String>) pane.getContent();
				list.getItems().add(table.getTableName());

				String tabText = table.getTableName() + " [" + tableDocName + "]";

				tabPane.getSelectionModel().select(getTab(tabText, table));
				openTable(table);
				break;
			}
		}
	}

	private ContextMenu titledPaneContextMenu(Document document, TitledPane pane) {
		ContextMenu context = new ContextMenu();

		MenuItem generate = new MenuItem("Сформувати рейтинг");
		generate.setOnAction(e -> {
			documents.setGenerateRatingDocument(document);
			pane.setExpanded(true);
			RatingGenerationWindow.show();
		});

		MenuItem rename = new MenuItem("Змінити назву");
		rename.setOnAction(e -> {
			String oldName = document.getName();
			String name = Alert.showTextInputDialog(
					"Зміна назви",
					"Змінити назву документу " + oldName,
					"Нова назва документу не може збігатися з назвою других документів!",
					oldName);

			if (name == null)
				return;

			if (documents.getDocument(name) == null) {
				for (Tab tab : tabPane.getTabs()) {
					if (tab.getId().equals(oldName)) {
						Table<?> table = (Table<?>) tab.getUserData();

						tab.setId(name);
						tab.setText(table.getTableName() + " [" + name + "]");

						Main.getPrimaryStage().setTitle("Рейтинг – " + table.getTableName() + " [" + name + "]");
					}
				}

				document.setName(name);
				pane.setText(name);
				pane.getContent().setId(document.getName());
				pane.setExpanded(true);
			} else {
				if (!document.getName().equals(name))
					Alert.show("Інформація", "Помилка", "Документ з назвою " + name + " вже існує!");
			}
		});

		MenuItem delete = new MenuItem("Видалити документ");
		delete.setOnAction(e -> {
			boolean isConfirm = Alert.showConfirmAlert(
					"Видалення документу",
					"Видалити документ " + document.getName(),
					"Після видалення документу, його не можливо буде відновити!");

			if (!isConfirm)
				return;

			projectsList.getPanes().remove(pane);
			documents.removeDocument(document);

			if (openTable == null)
				return;

			// если открыта таблица - убираем
			String documentName = openTable.getDocument().getName();
			if (documentName.equals(pane.getText()))
				closeTable();

			tabPane.getTabs().removeIf(tab -> tab.getId().equals(documentName));
			openSelectTabTable();
		});

		context.getItems().addAll(generate, rename, delete);
		return context;
	}

	private void initTablesLists() {
		for (Document document : documents.getDocuments()) {
			ListView<String> list = new ListView<>();
			list.setCursor(Cursor.HAND);

			TitledPane pane = new TitledPane(document.getName(), list);
			pane.setContextMenu(titledPaneContextMenu(document, pane));

			for (Table<?> table : document) {
				list.getItems().add(table.getTableName());
				list.setId(document.getName());
			}

			setTablesListCellFactory(list);
			setTablesListDoubleClick(list);

			projectsList.getPanes().add(pane);
			pane.setExpanded(true);
		}
	}

	private void setTablesListCellFactory(ListView<String> list) {
		list.setCellFactory(e -> {
			ListCell<String> cell = new ListCell<>();

			ContextMenu contextMenu = new ContextMenu();

			MenuItem delete = new MenuItem("Видалити таблицю");
			delete.setOnAction(event -> {
				boolean isConfirm = Alert.showConfirmAlert(
						"Видалення таблиці",
						"Видалити таблицю " + cell.getItem(),
						"Після видалення таблиці, її не можливо буде відновити!");

				if (!isConfirm)
					return;

				list.getItems().remove(cell.getItem());

				if (openTable == null)
					return;

				// если открыта таблица - убираем
				tabPane.getTabs().removeIf(tab -> tab.getUserData().equals(openTable));

				String documentName = openTable.getDocument().getName();
				if (documentName.equals(list.getId()))
					closeTable();

				openSelectTabTable();
			});

			MenuItem rename = new MenuItem("Змінити назву");
			rename.setOnAction(event -> {
				String oldName = cell.getItem();
				String name = Alert.showTextInputDialog(
						"Зміна назви",
						"Змінити назву таблиці " + oldName,
						"Нова назва таблиці не може збігатися з назвою других таблиць документу!",
						oldName);

				if (name == null)
					return;

				Table<?> table = documents.getDocument(list.getId()).getTable(oldName);

				if (documents.getDocument(list.getId()).getTable(name) == null) {
					for (Tab tab : tabPane.getTabs()) {
						if (table.equals(tab.getUserData())) {
							tab.setText(name + " [" + list.getId() + "]");
							Main.getPrimaryStage().setTitle("Рейтинг – " + name + " [" + list.getId() + "]");
						}
					}

					table.setTableName(name);
					list.getItems().set(cell.getIndex(), name);
				} else {
					if (!table.getTableName().equals(name))
						Alert.show("Інформація", "Помилка", "Таблиця з назвою " + name + " вже існує!");
				}
			});

			MenuItem open = new MenuItem("Відкрити таблицю");
			open.setOnAction(event -> onOpenListDocument(cell.getListView(), cell.getItem()));

			MenuItem saveXlsx = new MenuItem("Зберегти .xlsx");
			saveXlsx.setOnAction(event -> onClickSaveXLSX(documents.getDocument(list.getId()).getTable(cell.getItem())));

			MenuItem savePdf = new MenuItem("Зберегти .pdf");
			savePdf.setOnAction(event -> {
				Table<?> table = documents.getDocument(list.getId()).getTable(cell.getItem());
				if (table instanceof RatingTable) {
					onClickSavePDF(table);
				} else {
					Alert.show("Увага", "Експорт .pdf",
						"Експортувати можна тільки таблиці рейтингу, що були згенеровані з двох груп. " +
						"Для експорту цієї таблиці в .pdf, скористайтеся експортом в .xlsx і збережіть файл як .pdf");
				}
			});

			contextMenu.getItems().addAll(open, rename, saveXlsx, savePdf, delete);

			cell.textProperty().bind(cell.itemProperty());
			cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
				cell.setContextMenu(isNowEmpty ? null : contextMenu);
            });

			return cell;
		});
	}

	private void setTablesListDoubleClick(ListView<String> list) {
		list.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				onOpenListDocument(list, list.getSelectionModel().getSelectedItem());
			}
		});
	}

	private void onOpenListDocument(ListView<String> list, String item) {
		String documentName = list.getId();

		Document doc = documents.getDocument(documentName);
		if (doc == null)
			return;

		Table<?> table = doc.getTable(item);
		if (table == null)
			return;

		// если таблица уже открыта
		if (openTable != null && openTable.equals(table))
			return;

		String tabText = item + " [" + documentName + "]";

		tabPane.getSelectionModel().select(getTab(tabText, table));
		openTable(table);
	}

	private Tab getTab(String tabText, Table<?> table) {
		for (Tab tab : tabPane.getTabs()) {
			if (tab.getText().equals(tabText)) {
				return tab;
			}
		}

		Tab tab = new Tab(tabText);
		tab.setId(table.getDocument().getName());
		tab.setUserData(table);
		tab.setOnClosed(e -> {
			if (openTable != null && openTable.equals(table))
				closeTable();

			openSelectTabTable();
		});

		tabPane.getTabs().add(tab);
		return tab;
	}

	private void openSelectTabTable() {
		Tab tab = tabPane.getSelectionModel().getSelectedItem();
		if (tab == null)
			return;

		Table<?> table = (Table<?>) tab.getUserData();
		openTable(table);
	}

	private void initTabPane(TabData[] tabs) {
		ContextMenu contextMenu = new ContextMenu();

		MenuItem item = new MenuItem("Закрити всі таблиці");
		item.setOnAction(e -> {
			tabPane.getTabs().clear();
			closeTable();
		});

		contextMenu.getItems().add(item);
		tabPane.setContextMenu(contextMenu);
		tabPane.setCursor(Cursor.HAND);

		if (tabs == null) {
			if (projectsList.getPanes().size() > 0)
				projectsList.getPanes().get(0).setExpanded(true);

			return;
		}

		for (TabData data : tabs) {
			Document doc = documents.getDocument(data.documentName);
			if (doc != null) {
				Table<?> table = doc.getTable(data.tableName);
				if (table != null) {
					String tabText = data.tableName + " [" + data.documentName + "]";
					Tab tab = getTab(tabText, table);

					if (data.isSelect) {
						tabPane.getSelectionModel().select(tab);
						openTable(table);

						for (TitledPane pane : projectsList.getPanes()) {
							if (pane.getText().equals(data.documentName)) {
								pane.setExpanded(true);
								break;
							}
						}
					}
				}
			}
		}
	}

	private void bindTooltip(Node node, Tooltip tooltip) {
		node.setOnMouseEntered(e -> {
			if (!tooltip.isShowing())
				tooltip.show(node, e.getScreenX(), e.getScreenY() + 15);
		});
		node.setOnMouseExited(e -> tooltip.hide());
	}

	private JFXButton createInstrumentsButton(String iconName, String tooltipText, boolean disable, EventHandler<MouseEvent> handler) {
		JFXButton button = new JFXButton(tooltipText, getButtonImage("/icons/" + iconName + ".png"));

		button.setId(iconName);
		button.setDisable(disable);
		button.setPrefSize(50, 50);
		button.setRipplerFill(Color.valueOf("#40E0D0"));
		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		button.setCursor(Cursor.HAND);

		bindTooltip(button, new Tooltip(tooltipText));
		button.addEventHandler(MouseEvent.MOUSE_CLICKED, handler);

        return button;
	}

	private ImageView getButtonImage(String src) {
		Image mine = new Image(getClass().getResource(src).toExternalForm());
		ImageView img = new ImageView(mine);

		img.setFitHeight(24);
		img.setFitWidth(24);

        return img;
	}

	private void onClickSaveXLSX(Table<?> table) {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Зберегти таблицю");
		fileChooser.getExtensionFilters().add(xlsxFilter);
		fileChooser.setInitialFileName(table.getTableName());

		File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
		if (file != null) {
			try {
				new Thread(new FileExporter(table, file, ExportType.XLSX)).start();
			} catch (Exception ex) {
				ErrorAlert.show("Помилка при експорті файлу .xlsx", ex.getMessage());
			}
		}
	}

	private void onClickSavePDF(Table<?> table) {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Зберегти таблицю");
		fileChooser.getExtensionFilters().add(pdfFilter);
		fileChooser.setInitialFileName(table.getTableName());

		File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
		if (file != null) {
			try {
				new Thread(new FileExporter(table, file, ExportType.PDF)).start();
			} catch (Exception ex) {
				ErrorAlert.show("Помилка при експорті файлу .pdf", ex.getMessage());
			}
		}
	}

	/**
	 * Инициализация кнопок бокового меню инструментов
	 */
	private void initInstruments() {
		buttons = new JFXButton[] {
			createInstrumentsButton("document", "Зберегти зміни", false, e -> {
				new Thread(() -> saveApp(true)).start();

				Alert.show("Інформація", "Зміни збережено", "");
			}),
			createInstrumentsButton("users", "Сформувати рейтинг", false, e -> {
				documents.setGenerateRatingDocument(null);
				RatingGenerationWindow.show();
			}),
			createInstrumentsButton("sort", "Сортувати таблицю", true, e -> {
				if (openTable != null) {
					openTable.sort();
				}
			}),
			createInstrumentsButton("add-user", "Додати рядок в кінець таблиці", true, e -> {
				if (openTable != null) {
					openTable.addEmptyRow();
				}
			}),
			createInstrumentsButton("pdf-file-format-symbol", "Зберегти таблицю .pdf", true, e -> {
				if (openTable != null) {
					// вроде должен быть всегда RatingTable, но на всякий
					if (openTable instanceof RatingTable) {
						onClickSavePDF(openTable);
					}
				}
			}),
			createInstrumentsButton("xlsx-file-format-extension", "Зберегти таблицю .xlsx", false, e -> {
				if (openTable != null) {
					onClickSaveXLSX(openTable);
				} else {
					Alert.show("Увага", "Експорт .xlsx",
							"Для експорту таблиці в .xlsx потрібно її відкрити або обрати експорт " +
							"потрібної таблиці в контекстному меню списку таблиць документу.");
				}
			}),
			createInstrumentsButton("moon", "Змінити тему", false, e -> {
				ThemeManager.changeTheme();
				setTheme();
			})
		};

		instruments.getChildren().addAll(buttons);
		instruments.setFillWidth(true);
	}

	private void setTheme() {
		// изменяем изображения кнопкам инструментов в зависимости от темы
		for (JFXButton button : buttons) {
			String src = ThemeManager.getCurrentTheme() == ThemeType.DARK ?
				"/icons/light/" + button.getId() + ".png" : "/icons/" + button.getId() + ".png";

	        button.setGraphic(getButtonImage(src));
		}
	}

	@FXML
	private void onClickNewDocument() {
		Document document = new Document();
		documents.addDocument(document);

		TitledPane pane = new TitledPane(document.getName(), new Label("Пустий документ"));
		pane.setContextMenu(titledPaneContextMenu(document, pane));

		projectsList.getPanes().add(pane);
		pane.setExpanded(true);
	}

	@FXML
	private void onClickIImportXLSX() {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Відкрити документ");
		fileChooser.getExtensionFilters().add(xlsxFilter);

		File file = fileChooser.showOpenDialog(Main.getPrimaryStage());
		if (file != null) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					try {
						Document document = Document.buildDocumentByXLSX(file);
						documents.addDocument(document);

						// добавляем документы в каталог
						ListView<String> list = new ListView<>();
						setTablesListDoubleClick(list);
						list.setCursor(Cursor.HAND);

						TitledPane pane = new TitledPane(document.getName(), list);
						pane.setContextMenu(titledPaneContextMenu(document, pane));

						for (Table<?> table : document) {
							list.getItems().add(table.getTableName());
							list.setId(document.getName());
						}

						setTablesListCellFactory(list);
						projectsList.getPanes().add(pane);

						pane.setExpanded(true);
					} catch (IOException e) {
						ErrorAlert.show(e.getMessage());
					}
				}
			});
		}
	}

	@FXML
	private void onClickExit() {
		boolean saveChanges = Alert.showConfirmAlert("Увага", "Зберегти зміни?", "");
		saveApp(saveChanges);

		System.exit(-1);
	}
}
