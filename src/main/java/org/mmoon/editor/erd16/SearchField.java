package org.mmoon.editor.erd16;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.TextField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.UserError;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

/**
 * A form to submit keywords for querying of the database and displaying all
 * entries at once
 */
@SuppressWarnings("serial")
public class SearchField extends CustomComponent implements SearchFieldInterface, ClickListener, Handler {

	private Layout layout;

	/**
	 * Field for entering a keyword
	 */
	private TextField searchField;

	/**
	 * Start search
	 */
	private Button searchButton;

	/**
	 * Show all entries of database at once
	 */
	private Button showAllButton;

	/**
	 * Shortcut to start search by pressing the enter key
	 */
	private Action action_search = new ShortcutAction("Enter", ShortcutAction.KeyCode.ENTER, null);

	/**
	 * Layout the components
	 */
	public SearchField() {
		// init search button
		searchButton = new Button("Search");
		searchButton.addClickListener(this);

		showAllButton = new Button("Show All");
		showAllButton.addClickListener(this);

		// init search field
		searchField = new TextField();

		// init search layout
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.addComponent(searchField);
		searchField.setCaption(" ");
		searchField.setWidth("100%");
		searchLayout.setExpandRatio(searchField, 1);
		searchLayout.setSpacing(true);
		searchLayout.addComponent(showAllButton);
		searchLayout.addComponent(searchButton);
		searchLayout.setComponentAlignment(showAllButton, Alignment.BOTTOM_LEFT);
		searchLayout.setComponentAlignment(searchButton, Alignment.BOTTOM_LEFT);
		searchLayout.setSizeFull();

		// init main layout
		layout = new VerticalLayout();
		layout.addComponent(searchLayout);
		Panel panel = new Panel();
		panel.setContent(layout);
		panel.addActionHandler(this);
		setCompositionRoot(panel);
		setSizeFull();

		searchField.setDescription("Enter keyword here.");
		showAllButton.setDescription("Display all entries of the database.");
		searchButton.setDescription("Start search for keyword.");
	}

	// presenter will be the only listener
	List<SearchFieldListener> listeners = new ArrayList<SearchFieldListener>();

	@Override
	// presenter will add his own
	public void addSearchFieldListener(SearchFieldListener listener) {
		listeners.add(listener);
	}

	/**
	 * Get the current value of the serach field
	 * 
	 * @return the current value of the search field
	 */
	private String getSearchFieldValue() {
		return searchField.getValue();
	}

	/**
	 * Handle button clicks
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		// click search button
		if (event.getSource() == searchButton) {
			if (getSearchFieldValue().compareTo("") != 0) {
				searchField.setComponentError(null);
				for (SearchFieldListener listener : listeners) {
					listener.search(getSearchFieldValue());
				}
			} else {
				searchField.setComponentError(new UserError("type in request"));
			}
		}
		// click show all button
		if (event.getSource() == showAllButton) {
			for (SearchFieldListener listener : listeners) {
				searchField.setComponentError(null);
				listener.showAll();
			}
		}
	}

	/**
	 * @return the actions
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		return new Action[] { action_search };
	}

	/**
	 * Handle actions
	 */
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == action_search) {
			searchButton.click();
		}

	}

	/**
	 * @return the layout
	 */
	public Layout getLayout() {
		return layout;
	}

	/**
	 * @return the searchField
	 */
	public TextField getSearchField() {
		return searchField;
	}

	/**
	 * @return the searchButton
	 */
	public Button getSearchButton() {
		return searchButton;
	}

	/**
	 * @return the listeners
	 */
	public List<SearchFieldListener> getListeners() {
		return listeners;
	}
}
