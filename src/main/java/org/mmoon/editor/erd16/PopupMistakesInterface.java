package org.mmoon.editor.erd16;

public interface PopupMistakesInterface {
	interface PopupMistakeListener {
		void refreshWindow();
	}

	void addPopupMistakeListener(PopupMistakeListener listener);
}
