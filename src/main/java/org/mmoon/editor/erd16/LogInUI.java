package org.mmoon.editor.erd16;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

/**
 * A LogIn Form to authenticate for access to the admin page
 */
@SuppressWarnings("serial")
@Theme("vorprojekt")
public class LogInUI extends UI {

	public static class Servlet extends VaadinServlet {
	}

	/**
	 * Initialize the login view
	 */
	@Override
	protected void init(VaadinRequest request) {

		LogIn logIn = new LogIn();
		setContent(logIn);

	}
}
