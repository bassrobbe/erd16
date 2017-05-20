package com.example.vorprojekt;

public interface PopupAdminPageInterface {
	interface PopupAdminPageListener {
		void refreshWindow();

		void updateDB(String subject, String property, String object, String oldObject);
	}

	void addPopupAdminPageListener(PopupAdminPageListener listener);
}
