package com.example.vorprojekt;

public interface PopupMistakesInterface {
	interface PopupMistakeListener {
		void refreshWindow();
	}

	void addPopupMistakeListener(PopupMistakeListener listener);
}
