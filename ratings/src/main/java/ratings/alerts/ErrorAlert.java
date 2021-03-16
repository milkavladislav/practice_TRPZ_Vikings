package ratings.alerts;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import ratings.Main;

public class ErrorAlert {

	private static final int TEXT_AREA_WIDTH = 700;

	public static void show(Exception e) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR);

			alert.setTitle("Ошибка");
			alert.setHeaderText(e.getMessage());

			VBox dialogPaneContent = new VBox();

			Label label = new Label("Stack Trace:");
			TextArea textArea = new TextArea();

			textArea.setText(getStackTrace(e));
			textArea.setPrefWidth(TEXT_AREA_WIDTH);

			dialogPaneContent.getChildren().addAll(label, textArea);

			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.getIcons().add(Main.getPrimaryStage().getIcons().get(0));

			alert.getDialogPane().setContent(dialogPaneContent);
			alert.showAndWait();
		});
	}

	public static void show(String header, String content) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR);

			alert.setTitle("Ошибка");
			alert.setHeaderText(header);
			alert.setContentText(content);

			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.getIcons().add(Main.getPrimaryStage().getIcons().get(0));

			alert.showAndWait();
		});
	}

	public static void show(String content) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR);

			alert.setTitle("Ошибка");
			alert.setContentText(content);

			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.getIcons().add(Main.getPrimaryStage().getIcons().get(0));

			alert.showAndWait();
		});
	}

   	private static String getStackTrace(Exception e) {
   		StringWriter sw = new StringWriter();
   		e.printStackTrace(new PrintWriter(sw));
   		return sw.toString();
   	}
}