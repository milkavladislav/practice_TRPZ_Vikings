package ratings.document.table.tableview;

import org.apache.poi.ss.usermodel.Workbook;
import com.itextpdf.text.Document;

public interface Exportable {
	Workbook toWorkbook();
	void toPDF(Document document);
}
