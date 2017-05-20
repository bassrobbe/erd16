package com.example.vorprojekt;

import java.util.ArrayList;

public interface PopUpAddPropertyInterface {
	public void fetchPropertiesData();

	public void fetchObjectsData(String subject, String property);

	public void setPropertiesData(ArrayList<String> propertiesData);

	public void setObjectsData(ArrayList<String> objectsData);

	interface PopUpAddPropertyListener {
		public void createPopUpAddProperty(String subject);

		public void updatePropertiesData(String subject);

		public void updateObjectsData(String subject, String property);

		public void commitAddProperty(String subject, String property, String object);
	}

	public void addPopUpAddPropertyListener(PopUpAddPropertyListener listener);
}
