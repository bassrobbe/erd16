package org.mmoon.editor.erd16;

public interface InsertSelectPopupInterface {

	interface InsertSelectPopupListener {
		public boolean searchForUnusedRepresentation(String representaion);

		public void insertEntry(String subject, String type, String representation);

		public void search(String request);

		public void selectSearchItem(String request);
	}

	void addInsertSelectPopupListener(InsertSelectPopupListener listener);
}
