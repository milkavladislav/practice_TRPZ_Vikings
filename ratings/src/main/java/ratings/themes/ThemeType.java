package ratings.themes;

public enum ThemeType {
	DARK {
		@Override
		public String getStyle() {
			return getClass().getResource("/themes/DarkTheme.css").toExternalForm();
		}
	},
	DEFAULT {
		@Override
		public String getStyle() {
			return getClass().getResource("/themes/DefaultTheme.css").toExternalForm();
		}
	};

	public abstract String getStyle();
}
