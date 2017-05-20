package com.example.vorprojekt;

import java.util.ArrayList;

public interface InsertPopupInterface {

	interface InsertPopupListener {
		public void insertEntry(String subject, String type, String represenatation);

		public ArrayList<String> checkDoubles(String subject);

		public boolean searchForUnusedRepresentation(String representaion);

		public void search(String request);

		public void selectSearchItem(String request);
	}

	void addInsertPopupListener(InsertPopupListener listener);
}
