package ratings.document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ratings.controllers.Controller;

public class DocumentsManager implements Iterable<Document> {

	private static final DocumentsManager INSTANCE = new DocumentsManager();

	private Controller controller;

	public static DocumentsManager getInstance() {
		return INSTANCE;
	}

	private List<Document> documents = new ArrayList<>();
	private int inc = 1;

	private Document generateRatingDocument;

	/**
	 * Возвращает список со всеми документами
	 * @return Список всех документов
	 */
	public List<Document> getDocuments() {
		return documents;
	}

	public void addDocument(Document document) {
		document.setName("Document " + inc++);
		documents.add(document);
	}

	public void removeDocument(Document document) {
		documents.remove(document);
	}

	public Document getDocument(String name) {
		for (Document document : documents) {
			if (document.getName().equals(name))
				return document;
		}
		return null;
	}

	public Document getGenerateRatingDocument() {
		return generateRatingDocument;
	}

	public void setGenerateRatingDocument(Document generateRatingDocument) {
		this.generateRatingDocument = generateRatingDocument;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public Controller getController() {
		return controller;
	}

	@Override
	public Iterator<Document> iterator() {
		return documents.iterator();
	}
}
