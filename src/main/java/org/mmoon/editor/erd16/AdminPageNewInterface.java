package org.mmoon.editor.erd16;

import java.io.File;

public interface AdminPageNewInterface {
	interface AdminPageNewListener{
		void selectItem (String category, String item, File file);
	}

}
