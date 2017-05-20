package com.example.vorprojekt;

import java.util.ArrayList;

public interface PopUpEditPropertyInterface {
	public void fetchObjectsData(String subject, String property);

	public void setObjectsData(ArrayList<String> objectsData);

	interface PopUpEditPropertyListener {
		public void createPopUpEditProperty(String subject, String property, String oldObject);

		public void updateObjectsDataEdit(String subject, String property);

		public void commitEditProperty(String subject, String property, String object, String oldObject);
	}

	public void addPopUpAddPropertyListener(PopUpEditPropertyListener listener);
}
