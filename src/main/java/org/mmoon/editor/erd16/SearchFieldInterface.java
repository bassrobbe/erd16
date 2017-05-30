package org.mmoon.editor.erd16;

public interface SearchFieldInterface {

	interface SearchFieldListener {
		public void search(String request);

		public void showAll();

	}

	public void addSearchFieldListener(SearchFieldListener listener); 
}
