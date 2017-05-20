package com.example.vorprojekt;

import java.io.File;

public interface AdminPageNewInterface {
	interface AdminPageNewListener{
		void selectItem (String category, String item, File file);
	}

}
