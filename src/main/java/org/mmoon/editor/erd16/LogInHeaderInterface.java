package org.mmoon.editor.erd16;

public interface LogInHeaderInterface {

	interface LogInHeaderListener {
		void detailUpdateColumns();

		void changeInsertFunctionPermission();
	}

	void addLogInHeaderListener(LogInHeaderListener listener);
}
