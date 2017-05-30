package org.mmoon.editor.erd16;

import org.apache.shiro.SecurityUtils;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

/**
 * Page handling the logout. The user is redirected to the main view based on
 * default shiro.ini configuration
 */
@SuppressWarnings("serial")
@Theme("vorprojekt")
public class LogOut extends UI {

	public static class Servlet extends VaadinServlet {

	}

	/**
	 * Logout current subject
	 */
	@Override
	protected void init(VaadinRequest request) {
		SecurityUtils.getSubject().logout();
	}
}
