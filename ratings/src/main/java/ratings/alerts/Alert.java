package ratings.alerts;

import java.util.Optional;

import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;

import javafx.stage.Stage;

import ratings.Main;

public class Alert {

	public static void show(String title, String header, String text) {
		javafx.scene.control.Alert alert =
				new javafx.scene.control.Alert(AlertType.INFORMATION);

		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(text);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(Main.getPrimaryStage().getIcons().get(0));

		alert.showAndWait();
	}

	public static boolean showConfirmAlert(String title, String header, String text) {
		javafx.scene.control.Alert alert =
				new javafx.scene.control.Alert(AlertType.CONFIRMATION);

		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(text);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(Main.getPrimaryStage().getIcons().get(0));

		ButtonType confirmButton = new ButtonType("Так", ButtonData.OK_DONE);
		ButtonType cancelButton = new ButtonType("Ні", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(confirmButton, cancelButton);

		Optional<ButtonType> option = alert.showAndWait();
		return option.get() == confirmButton;
	}

	public static String showTextInputDialog(String title, String header, String text, String inputText) {
		TextInputDialog input = new TextInputDialog(inputText);

		input.setTitle(title);
		input.setHeaderText(header);
		input.setContentText(text);

		Stage stage = (Stage) input.getDialogPane().getScene().getWindow();
		stage.getIcons().add(Main.getPrimaryStage().getIcons().get(0));

		Optional<String> option = input.showAndWait();
		return option.isPresent() ? (String)option.get() : null;
	}
}