package com.example.vorprojekt;

import java.util.ArrayList;

public interface OverviewInterface {
	public void updateOverview(ArrayList<String> subjects);

	public void select(String request);

	public void changeCreateNewEntryButtonPermission();

	interface OverviewListener {
		void entrySelect(String subjectString);

		void createEntry();
	}

	public void addOverviewListener(OverviewListener listener);
}
