package com.example.vorprojekt;

public interface LogInHeaderInterface {

	interface LogInHeaderListener {
		void detailUpdateColumns();

		void changeInsertFunctionPermission();
	}

	void addLogInHeaderListener(LogInHeaderListener listener);
}
