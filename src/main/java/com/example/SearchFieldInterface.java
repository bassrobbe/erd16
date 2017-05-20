package com.example.vorprojekt;

public interface SearchFieldInterface {

	interface SearchFieldListener {
		public void search(String request);

		public void showAll();

	}

	public void addSearchFieldListener(SearchFieldListener listener); 
}
