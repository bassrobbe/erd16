package com.example.vorprojekt;

/** SearchPresenter
 *
 * @author Robert
 * @author Marc
 * @author Torben
 * @author Marlo
 *
 * Last Changes 25.03.2016
 *
 * Brief Description and Info
 */

import java.util.ArrayList;

import com.example.vorprojekt.InsertSelectPopupInterface.InsertSelectPopupListener;
import com.example.vorprojekt.LogInHeaderInterface.LogInHeaderListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * Class operating the view through Interfaces.
 */
public class SearchPresenter implements OverviewInterface.OverviewListener, InsertPopupInterface.InsertPopupListener,
		SearchFieldInterface.SearchFieldListener, DetailInterface.DetailListener,
		PopupReportButtonInterface.PopupReportButtonListener, InsertSelectPopupListener, LogInHeaderListener,
		PopUpAddPropertyInterface.PopUpAddPropertyListener, PopUpEditPropertyInterface.PopUpEditPropertyListener {

	/**
	 * Model holding the data and providing methods to query the database.
	 */
	private QueryDBSPARQL model;

	/**
	 * Detail view displaying all predicates and objects of a selected subject.
	 */
	private Detail detail;

	/**
	 * Search result view displaying a list of all subjects with a orthographic
	 * representation matching the search string.
	 */
	private Overview overview;

	/**
	 * Text field to enter a string to match with subjects in the database.
	 */
	private SearchField searchField;

	/**
	 * searchUI as root components
	 */
	private SearchUI searchUI;

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private LogInHeader login;

	/**
	 * 
	 */
	private PopUpAddProperty sub;

	/**
	 * 
	 */
	private PopUpEditProperty subEdit;

	/**
	 * Constructor that binds the model and the view components together
	 * 
	 * @param model
	 *            database
	 * @param detail
	 *            detail view
	 * @param overview
	 *            search result view
	 * @param filter
	 *            filter selection component
	 * @param searchField
	 *            text filed for search strings
	 */
	public SearchPresenter(SearchUI searchUI, QueryDBSPARQL model, Detail detail, Overview overview,
			SearchField searchField/* , PlusButton plusButton */, LogInHeader login) {
		this.model = model;
		this.detail = detail;
		this.overview = overview;
		this.searchUI = searchUI;
		this.searchField = searchField;
		this.login = login;

		// add presenter to listener lists
		overview.addOverviewListener(this);
		searchField.addSearchFieldListener(this);
		detail.addDetailListener(this);
		login.addLogInHeaderListener(this);
	}

	/**
	 * Triggers a query for property-objects pairs for a given subject selected
	 * from the overview component and updates the detail view.
	 * 
	 * @param subjectString
	 *            string representation of the selected subject
	 */
	@Override
	public void entrySelect(String subjectString) {
		String fragment = Page.getCurrent().getUriFragment().split("&")[0] + "&select=" + subjectString;
		Page.getCurrent().setUriFragment(fragment, false);
		model.searchForObject(subjectString);
		detail.updateDetails(subjectString, model.getPropertyList(), model.getObjectList());
		detail.addMissingPropertys(model.getEmptyPropertyList());
	}

	/**
	 * Query the database for subjects with orthographic representations
	 * matching the given search string
	 * 
	 * @param reques
	 *            search string
	 */
	@Override
	public void search(String request) {
		if (request.compareTo("") != 0) {
			 Page.getCurrent().setUriFragment("overview="+request,false);
			ArrayList<String> subjects = model.searchForSubject(request);
			if (subjects.size() == 0) {
				Notification.show("No results found matching keyword",
						Notification.Type.WARNING_MESSAGE);
			}
			detail.clearDetails();
			overview.updateOverview(subjects);
		}
	}

	/**
	 * Triggers a query for empty properties of a given subject and adds them to
	 * the detail view.
	 * 
	 * @param currentString
	 *            string representation of the currently displayed subject
	 */
	@Override
	public void createButtonClick(String currentSubject) {
		createPopUpAddProperty(currentSubject);
	}

	/**
	 * Select an item in order to trigger diplaying of predicates and objects in
	 * detail view
	 * 
	 * @param request
	 *            representation of the item that should become selected
	 */
	@Override
	public void selectSearchItem(String request) {
		overview.select(request);
	}

	/**
	 * Updates the DB with the given Triple
	 * 
	 * @param subject
	 *            String representation of the subject
	 * @param propertyAndObject
	 *            Item containing property and object
	 */
	@Override
	public void updateDB(String subject, String property, String object, String oldObject) {
		model.deleteValue(subject, property, oldObject);
		model.insertValue(subject, property, object);
		System.out.print(" sub: " + subject + "\n prop: " + property + "\n obj: " + object + "\n old: " + oldObject
				+ "\n delete: " + model.verifyUpdate(subject, property, oldObject) + "\n insert: "
				+ model.verifyUpdate(subject, property, object) + "\n");
		if (model.verifyUpdate(subject, property, object)) {
			if (oldObject.compareTo("") == 0) {
				Notification.show("Insertion successful", Notification.Type.TRAY_NOTIFICATION);
			}
			if (oldObject.compareTo("") != 0) {
				Notification.show("Update successful", Notification.Type.TRAY_NOTIFICATION);
			}
			String uriFragment = Page.getCurrent().getUriFragment();
			if (uriFragment != null) {
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
				if (overview != null) {
					if (overview.compareTo("*") == 0) {
						this.showAll();
					} else {
						this.search(overview);
					}
				}
				if (select != null) {
					this.entrySelect(select);
					this.selectSearchItem(select);
				}
				// searchPresenter.detailUpdateColumns();
			}
			detail.selectRow(property, object);
		} else {
			model.insertValue(subject, property, oldObject);
			Notification.show("Error", "Triple:" + "\n" + "\n" + subject + ", " + property + ", " + object + "\n" + "\n"
					+ "could not be added to database.", Notification.Type.TRAY_NOTIFICATION);
		}
	}

	/**
	 * creates a report popup window
	 * 
	 * @param subject
	 *            String of the subject
	 * @param object
	 *            String of the object
	 * @param value
	 *            String of the value
	 */
	@Override
	public void openReportPopup(String subject, String object, String value) {
		// create popup window
		PopupReportButton popup = new PopupReportButton(subject, object, value);
		// add presenter to popupListener's list
		popup.addPopupReportButtonListener(this);
		// adjust size of the popup window
		popup.setHeight("300px");
		popup.setWidth("800px");
		// Add it to the root component
		searchUI.addWindow(popup);
	}

	@Override
	public void createEntry() {
		InsertSelectPopup selectPopup = new InsertSelectPopup();
		selectPopup.setVisible(false);
		selectPopup.addInsertSelectPopupListener(this);
		InsertPopup popup = new InsertPopup(model.getAllTypes(), selectPopup);
		popup.addInsertPopupListener(this);
		searchUI.addWindow(popup);
		searchUI.addWindow(selectPopup);

	}

	/**
	 * Insert completeley new value into db
	 * 
	 * @param subject
	 *            subject to Insert
	 * @param type
	 *            of subject
	 * @param rep
	 *            representation of subject
	 */
	@Override
	public void insertEntry(String subject, String type, String rep) {
		model.insertNewSubject(subject, type, rep);
	}

	/**
	 * Checks whether the subject already exists
	 * 
	 * @param request
	 *            value to search for
	 * @return list with all values which hold the searched representation
	 */
	@Override
	public ArrayList<String> checkDoubles(String request) {
		return model.searchForSubject(request);
	}

	/**
	 * method to look for value names which are currently not assigned
	 * 
	 * @paramn representation representation of the object
	 * @return boolean value whether the name already exists
	 */
	@Override
	public boolean searchForUnusedRepresentation(String representation) {
		model.searchForObject(representation);
		if (model.getObjectList().size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * add/remove Columns from detail view
	 */
	@Override
	public void detailUpdateColumns() {
		detail.updateColumns();
	}

	/**
	 * @return the model
	 */
	public QueryDBSPARQL getModel() {
		return model;
	}

	/**
	 * @return the detail
	 */
	public Detail getDetail() {
		return detail;
	}

	/**
	 * @return the overview
	 */
	public Overview getOverview() {
		return overview;
	}

	/**
	 * @return the searchField
	 */
	public SearchField getSearchField() {
		return searchField;
	}

	/**
	 * Retrieve all entries from the database
	 */
	@Override
	public void showAll() {
		detail.clearDetails();
		Page.getCurrent().setUriFragment("overview=*", false);
		ArrayList<String> subjects = model.getAllSubjects();
		overview.updateOverview(subjects);
	}

	/**
	 * Create a new popup for adding a property-value pair
	 */
	@Override
	public void createPopUpAddProperty(String subject) {
		sub = new PopUpAddProperty(subject);
		sub.addPopUpAddPropertyListener(this);
		UI.getCurrent().addWindow(sub);

	}

	/**
	 * Update the list of assignable properties for a subject
	 * 
	 * @param subject
	 *            the subject for which a property-value pair will be added
	 */
	@Override
	public void updatePropertiesData(String subject) {
		model.searchForEmptyProperty(subject);
		ArrayList<String> properties = model.getEmptyPropertyList();
		sub.setPropertiesData(properties);
	}

	/**
	 * Update the list of assignable objects for a property when adding
	 * 
	 * @param subject
	 *            the subject for which a property-value pair will be added
	 * @param property
	 *            the property for which a object will be assigned
	 */
	@Override
	public void updateObjectsData(String subject, String property) {
		ArrayList<String> objects = model.searchForAssignableObjects(property);
		model.searchForObject(subject);
		ArrayList<String> assigned = model.getObjectList();
		for (String item : assigned) {
			objects.remove(item);
		}
		sub.setObjectsData(objects);
	}

	/**
	 * Commit a triple representing an added property-value pair for insertion
	 * into the database
	 * 
	 * @param subjcet
	 *            the subject for which a property-value pair will be added
	 * @param property
	 *            the property that will be added
	 * @param objcet
	 *            the object assigned to the property
	 */
	@Override
	public void commitAddProperty(String subject, String property, String object) {
		updateDB(subject, property, object, "");
	}

	/**
	 * Create a new popup for editing a property-value pair
	 */
	@Override
	public void createPopUpEditProperty(String subject, String property, String oldObject) {
		subEdit = new PopUpEditProperty(subject, property, oldObject);
		subEdit.addPopUpAddPropertyListener(this);
		UI.getCurrent().addWindow(subEdit);

	}

	/**
	 * Commit a triple representing an edited property-value pair for updating
	 * the database
	 * 
	 * @param subjcet
	 *            the subject for which a property-value pair will be edited
	 * @param property
	 *            the property that will be edited
	 * @param objcet
	 *            the edited object assigned to the property
	 */
	@Override
	public void commitEditProperty(String subject, String property, String object, String oldObject) {
		updateDB(subject, property, object, oldObject);
	}

	/**
	 * Open a popup for adding/editing a property value pair
	 * 
	 * @param currentSubject
	 *            subject for which a property value pari will be added/edited
	 * @param rowProperty
	 *            the property assigned to the subject
	 * @param rowObject
	 *            the object currentyl assigned to the property
	 */
	@Override
	public void editButtonClick(String currentSubject, String rowProperty, String rowObject) {
		createPopUpEditProperty(currentSubject, rowProperty, rowObject);
	}

	/**
	 * Update the list of assignable objects for a property when editing
	 * 
	 * @param subject
	 *            the subject for which a property-value pair will be added
	 * @param property
	 *            the property for which a object will be assigned
	 */
	@Override
	public void updateObjectsDataEdit(String subject, String property) {
		ArrayList<String> objects = model.searchForAssignableObjects(property);
		model.searchForObject(subject);
		ArrayList<String> assigned = model.getObjectList();
		for (String item : assigned) {
			objects.remove(item);
		}
		subEdit.setObjectsData(objects);
	}

	/**
	 * Show/Hide the create new entry button based on user role
	 */
	@Override
	public void changeInsertFunctionPermission() {
		overview.changeCreateNewEntryButtonPermission();
	}
}
