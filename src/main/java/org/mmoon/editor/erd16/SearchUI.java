package org.mmoon.editor.erd16;

/** SearchUI
 *
 * @author Robert
 * @author Marc
 *
 * Last Changes 25.03.2016
 *
 * Brief Description and Info
 */

import com.vaadin.annotations.Theme;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

/**
 * This class is the starting point of the whole application. The class creates a
 * new Instance of the Model-View-Presenter-Architecture on a user request of the
 * page.
 */
@SuppressWarnings("serial")
@Theme("vorprojekt")
public class SearchUI extends UI {

	public static class Servlet extends VaadinServlet {
	}

	/**
	 * databaste model
	 */
	private QueryDBSPARQL model;

	/**
	 * Detail component in UI
	 */
	private Detail detail;

	/**
	 * Overview component in UI
	 */
	private Overview overview;

	/**
	 * Search field in UI
	 */
	private SearchField searchField;

	/**
	 * Login fields in UI
	 */
	private LogInHeader header;

	/**
	 * Presenter who implements communication
	 */
	private SearchPresenter searchPresenter;

	/**
	 * Initialization method. Instantiates the model and all components of the view
	 * and binds them together through the presenter. Defines the layout of the
	 * components on the page.
	 */
	@Override
	protected void init(VaadinRequest request) {

		// Create the model and the Vaadin view implementation
		model = new QueryDBSPARQL();
		detail = new Detail();
		overview = new Overview();
		searchField = new SearchField();
		header = new LogInHeader("searchUI");

		// The presenter binds the model and view together
		searchPresenter = new SearchPresenter(this, model, detail, overview, searchField, header);

		VerticalLayout wrapper = new VerticalLayout();
		GridLayout content = new GridLayout(3, 3);

		content.setHeight("95%");
		content.setWidth("95%");
		wrapper.setSizeFull();
		wrapper.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
		wrapper.addComponent(content);
		setContent(wrapper);

		content.addComponent(header, 1, 0, 2, 0);
		content.addComponent(searchField, 0, 0, 0, 0);
		content.addComponent(overview, 0, 2, 0, 2);
		content.addComponent(detail, 2, 2, 2, 2);

		content.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

		content.setRowExpandRatio(0, 1);
		content.setRowExpandRatio(1, 1);
		content.setRowExpandRatio(2, 8);
		content.setColumnExpandRatio(0, 37.5f);
		content.setColumnExpandRatio(1, 5);
		content.setColumnExpandRatio(2, 57.5f);

		getPage().addUriFragmentChangedListener(
				new UriFragmentChangedListener() {
					@Override
					public void uriFragmentChanged(UriFragmentChangedEvent event) {
						load(event.getUriFragment());
					}
				}
		);

		load(getPage().getUriFragment());
	}

	private void load(String uriFragment) {
		if (uriFragment!=null) {
			String overview;
			String select;
			String split[] = uriFragment.split("&");
			try {
				overview = split[0].split("=")[1];
			} catch (IndexOutOfBoundsException e) {
				overview = null;
			}
			try {
				select = split[1].split("=")[1];
			} catch (IndexOutOfBoundsException e) {
				select = null;
			}
			if (overview!=null) {
				if (overview.compareTo("*") == 0) {
					searchPresenter.showAll();
				} else {
					searchPresenter.search(overview);
				}
			}
			if (select!=null) {
				searchPresenter.entrySelect(select);
				searchPresenter.selectSearchItem(select);
			}
			searchPresenter.detailUpdateColumns();
		}
	}

	/**
	 * Get method for model
	 * @return database
	 */
	public QueryDBSPARQL getModel(){
	    return this.model;
	}

	/**
	 * get detail component
	 * @return detail component
	 */
	public Detail getDetail(){
	    return this.detail;
	}

	/**
	 * get overview component
	 * @return overview component
	 */
	public Overview getOverview(){
	    return this.overview;
	}

	/**
	 * get search field component
	 * @return search field component
	 */
	public SearchField getSearchField(){
	    return this.searchField;
	}

	/**
	 * get login header
	 * @ return login header component
	 */
	public LogInHeader getLogInHeader(){
	    return this.header;
	}

	/**
	 * get search presenter
	 * @return search presenter
	 */
	public SearchPresenter getSearchPresenter(){
	    return this.searchPresenter;
	}
}
