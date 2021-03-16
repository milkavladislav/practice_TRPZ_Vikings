package ratings.themes;

import javafx.collections.ObservableList;
import ratings.Main;

public class ThemeManager {

	private static ThemeType theme = ThemeType.DEFAULT;

	public static void initStylesgeets() {
		ObservableList<String> stylesheets = Main.getPrimaryStage().getScene().getStylesheets();

    	stylesheets.removeIf(style -> !style.equals(getCommonStyle()));
    	stylesheets.add(getCurrentStyle());
	}

	public static void changeTheme() {
		theme = theme == ThemeType.DARK ? ThemeType.DEFAULT : ThemeType.DARK;
		initStylesgeets();
    }

	public static void setTheme(ThemeType type) {
		theme = type;
	}

	public static ThemeType getCurrentTheme() {
		return theme;
	}

	public static String getCurrentStyle() {
		return theme.getStyle();
	}

	public static String getCommonStyle() {
		return ThemeManager.class.getResource("/themes/common.css").toExternalForm();
	}
}
