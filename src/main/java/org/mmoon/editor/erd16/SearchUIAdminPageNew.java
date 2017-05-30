package org.mmoon.editor.erd16;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

@SuppressWarnings("serial")
@Theme("vorprojekt")
/**
 * This class creates a new instance of SearchUIAdminPageNew. It allows an authorized admin to manage User suggestions and reports
 * about wrong data sets.
 * @author Marlo
 *
 */
public class SearchUIAdminPageNew extends UI {
	
	
//	@WebServlet(value = "/*", asyncSupported = true)
//	@VaadinServletConfiguration(productionMode = false, ui = SearchUIAdminPageNew.class)
	public static class Servlet extends VaadinServlet {
	}
	
	@Override
	protected void init(VaadinRequest request) {
		 
		AdminPageNew adminPage = new AdminPageNew();
		QueryDBSPARQL model = new QueryDBSPARQL();
		LogInHeader header = new LogInHeader("adminUI");
		new SearchPresenterAdminPageNew(adminPage, model);

	    VerticalLayout wrapper = new VerticalLayout();
	    GridLayout content = new GridLayout(1,3);
	    
	    content.setHeight("95%");
	    content.setWidth("95%");
		content.addComponent(header,0,0,0,0);
		content.addComponent(adminPage,0,2,0,2);
	    content.setRowExpandRatio(0, 1);
	    content.setRowExpandRatio(1, 1);
	    content.setRowExpandRatio(2, 8);

		wrapper.setSizeFull();
		wrapper.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
	    wrapper.addComponent(content);
	    setContent(wrapper);
	    
	}
}
