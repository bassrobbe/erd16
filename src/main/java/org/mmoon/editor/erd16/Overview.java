package org.mmoon.editor.erd16;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.VerticalLayout;


/**
 * Displays a list of subjects in a two column table.
 */
@SuppressWarnings("serial")
public class Overview extends CustomComponent implements OverviewInterface,
														 SelectionListener,
														 ClickListener {
	/**
	 * component container, which shows the subcomponents in the order of their
	 * addition (vertically)
	 */
	private VerticalLayout layout;

	/**
	 * Grid which displays the data
	 */
	private Grid grid;

	/**
	 * Data to display
	 */
	private IndexedContainer container;

	/**
	 * Button to create new subject
	 */
	private Button createButton;

	/**
	 * Create a new Container and Grid to store and display a list of subjects.
	 * Layout the Component.
	 */
	public Overview() {
		createButton = new Button("Create new entry");
		createButton.setVisible(false);
		createButton.addClickListener(this);
		// display the button based on user role
		changeCreateNewEntryButtonPermission();
		
		// create new IndexedContainer and add some example properties
		container = new IndexedContainer();
		container.addContainerProperty("Category", String.class, "");
		container.addContainerProperty("Entity", String.class, "");

		// layout the component
		grid = new Grid(container);
		grid.setCaption("Search Results");
		grid.setColumnOrder("Category", "Entity");
		grid.getColumn("Category").setExpandRatio(1);
		grid.getColumn("Entity").setExpandRatio(1);
		grid.addSelectionListener(this);

		layout = new VerticalLayout();
		layout.addComponent(createButton);
		layout.addComponent(grid);
		layout.setComponentAlignment(createButton, Alignment.MIDDLE_RIGHT);
		// fix button height to default
		createButton.setHeight(createButton.getHeight(), createButton.getHeightUnits());
		layout.setExpandRatio(grid, 1);
		layout.setSpacing(true);
		grid.setSizeFull();
		layout.setSizeFull();
		setSizeFull();
		setCompositionRoot(layout);
		
		createButton.setDescription("Add a new entry to the database.");
	}

    // Only the presenter registers one listener...
    List<OverviewListener> listeners = new ArrayList<OverviewListener>();

	@Override
	public void addOverviewListener(OverviewListener listener) {
		listeners.add(listener);
	}

	/**
	 * Gets the values of the selected row of the grid and constructs a string
	 * representation of the subject from that. The string is used to trigger
	 * a query of the database looking for properties and objects.
	 * @param event
	 */
	@Override
	public void select(SelectionEvent event) {

		// Get selection from the selection modelcontent
	    Object selected = ((SingleSelectionModel) grid.getSelectionModel()).getSelectedRow();
	    
	    for (OverviewListener listener: listeners) {
	    	String category = grid.getContainerDataSource().getItem(selected).getItemProperty("Category").getValue().toString();
	    	String entity = grid.getContainerDataSource().getItem(selected).getItemProperty("Entity").getValue().toString();
			listener.entrySelect(category.concat("_").concat(entity));
		}
	}

	/**
	 * Display a list of subjects and clear all previous entries.
	 * @param subjects list of subjects to display
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateOverview(ArrayList<String> subjects) {
		container.removeAllItems();
		
		for (String row: subjects) {
			String[] split = row.split("_");
			Item newItem = container.getItem(container.addItem());
			newItem.getItemProperty("Category").setValue(split[0]);
			newItem.getItemProperty("Entity").setValue(split[1]);
		}
	}

	/**
	 * auto select the exact object selected from detail view that was
	 * queried as a new subject to trigger display of the predicates and
	 * objects in detail view
	 */
	@Override
	public void select(String request) {
		// iterate over all search results
		for (int i = 0; i < container.size(); i++) {
			// construct exact subject String
			String categoryStr = container.getItem(container.getIdByIndex(i)).getItemProperty("Category").getValue().toString();
			String entityStr = container.getItem(container.getIdByIndex(i)).getItemProperty("Entity").getValue().toString();
			String testStr = categoryStr.concat("_").concat(entityStr);
			// check if actual item matches the queried subject. If so select it
			if (testStr.compareTo(request)==0) {
				grid.select(container.getIdByIndex(i));
				grid.scrollTo(container.getIdByIndex(i));
			}
		}
	}

	/**
	 * Opens pop up to create a new entry.
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		for(OverviewListener listener : listeners){
			listener.createEntry();
		}
	}
	
	/**
	 * Display create new entry button base on user role
	 */
	@Override
	public void changeCreateNewEntryButtonPermission() {
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated() && subject.isPermitted("write:add")) {
			createButton.setVisible(true);
		}
		if (!subject.isAuthenticated()) {
			createButton.setVisible(false);
		}		
	}

	/**
	 * @return the layout
	 */
	public VerticalLayout getLayout() {
		return layout;
	}

	/**
	 * @return the grid
	 */
	public Grid getGrid() {
		return grid;
	}

	/**
	 * @return the container
	 */
	public IndexedContainer getContainer() {
		return container;
	}

	/**
	 * @return the listeners
	 */
	public List<OverviewListener> getListeners() {
		return listeners;
	}
	
	/**
	 * @return the button
	 */
	public Button getButton() {
		return createButton;
	}	
}
