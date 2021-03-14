package ratings.document.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import ratings.document.table.tableview.Table;

public class FileExporter implements Runnable {

	private Table<?> table;

	private File file;
	private ExportType type;

	/**
	 *
	 * @param table таблица для экспорта в файл
	 * @param file файл, куда будет экспортирована таблица
	 * @param type тип файла
	 */
	public FileExporter(Table<?> table, File file, ExportType type) {
		this.table = table;

		this.file = file;
		this.type = type;
	}

	private void saveXLSX() throws IOException {
		Workbook workbook = table.toWorkbook();

		try (FileOutputStream os = new FileOutputStream(file)) {
			workbook.write(os);
		}
	}

	private void savePDF() throws IOException {
		try {
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(file));
			table.toPDF(document);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			switch (type) {
				case PDF:
					savePDF();
					break;
				case XLSX:
					saveXLSX();
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
