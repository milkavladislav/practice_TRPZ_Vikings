package ratings;

import java.io.IOException;

import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import ratings.themes.ThemeManager;

public class RatingGenerationWindow {

	private static final int SCENE_WIDTH = 250;
	private static final int SCENE_HEIGHT = 300;

	public static void show() {
		try {
			Stage dialog = new Stage();

			Parent root = FXMLLoader.load(RatingGenerationWindow.class.getResource("/RatingGenerationWindow.fxml"));

			Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
	    	scene.getStylesheets().add(ThemeManager.getCurrentStyle());

			dialog.setScene(scene);
			dialog.setResizable(false);
			dialog.setTitle("Формування рейтингу");

			dialog.getIcons().add(Main.getPrimaryStage().getIcons().get(0));

			dialog.initOwner(Main.getPrimaryStage());
			dialog.initModality(Modality.APPLICATION_MODAL);

			dialog.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
