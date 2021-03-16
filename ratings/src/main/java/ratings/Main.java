package ratings;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import ratings.themes.ThemeManager;

public class Main extends Application {

	private static final int SCENE_WIDTH = 1200;
	private static final int SCENE_HEIGHT = 700;

	private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
		Main.primaryStage = primaryStage;

		Parent root = FXMLLoader.load(getClass().getResource("/ratings.fxml"));

		Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
		scene.getStylesheets().add(ThemeManager.getCommonStyle());

		primaryStage.getIcons().add(new Image(getClass().getResource("/logo.png").toExternalForm()));
		primaryStage.setTitle("Рейтинг");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

    public static void main(String[] args) {
        launch(args);
    }
}