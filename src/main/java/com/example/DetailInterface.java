package com.example.vorprojekt;

import java.util.ArrayList;

public interface DetailInterface {

	public void updateDetails(String subjectString, ArrayList<String> properties, ArrayList<String> objects);

	public void clearDetails();

	public void addMissingPropertys(ArrayList<String> properties);

	public void updateColumns();

	public void selectRow(String property, String object);

	interface DetailListener {
		void search(String request);

		void selectSearchItem(String request);

		void createButtonClick(String currentSubject);

		void updateDB(String subject, String property, String object, String oldObject);

		void openReportPopup(String subject, String property, String value);

		void editButtonClick(String currentSubject, String rowProperty, String rowObject);
	}

	public void addDetailListener(DetailListener listener);
}
