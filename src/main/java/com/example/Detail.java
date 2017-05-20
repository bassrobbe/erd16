package com.example.vorprojekt;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.CellDescriptionGenerator;
import com.vaadin.ui.Grid.CellReference;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.renderers.ImageRenderer;

/**
 * Displays all predicates and objects of a selected subject in a two column
 * table
 */
@SuppressWarnings("serial")
public class Detail extends CustomComponent
		implements DetailInterface, ItemClickListener, RendererClickListener, ClickListener {

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
	 * Button for adding properties
	 */
	private Button createButton;

	/**
	 * currently displayed subject
	 */
	private String currentSubject;

	/**
	 * property of the row for which a Renderer was clicked
	 */
	private String rowProperty;

	/**
	 * object of the row for which a Renderer was clicked
	 */
	private String rowObject;

	/**
	 * Create a new Container and Grid to store and display pairs of predicates
	 * and objects. Layout the Component.
	 */
	public Detail() {
		// create missing data button
		createButton = new Button("Add property");
		createButton.setVisible(false);
		createButton.addClickListener(this);

		// create data container and add properties
		container = new IndexedContainer();
		container.addContainerProperty("Property", String.class, "");
		container.addContainerProperty("Value", String.class, "");
		container.addContainerProperty("Search", Resource.class,
				new ThemeResource("search.png"));

		// create grid and bind it to the container
		grid = new Grid(container);
		grid.setCaption("Detail View");
		grid.getColumn("Search").setRenderer(new ImageRenderer(this));
		grid.getColumn("Property").setExpandRatio(1);
		grid.getColumn("Value").setExpandRatio(1);
		grid.getColumn("Search").setExpandRatio(0);
		// handle permission dependent columns
		updateColumns();
		grid.addItemClickListener(this);

		// create layout
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

		createButton.setDescription("Add a new property-value pair.");

		// add descriptions to the cells with Renderers
		grid.setCellDescriptionGenerator(new CellDescriptionGenerator() {

			@Override
			public String getDescription(CellReference cell) {
				String test = cell.getPropertyId().toString();
				if (test.compareTo("Search") == 0) {
					return "Use the value as a keyword in a new search. "
							+ "(e.g. Lexeme_schlafen1 -> keyword: schlafen)";
				}
				if (test.compareTo("Edit") == 0) {
					return "Edit the specified Value. (Add a new one if none " + "is specified.)";
				}
				if (test.compareTo("Report") == 0) {
					return "Report a mistake and eventually suggest a correction.";
				}
				return null;
			}

		});
	}

	// presenter will be the only listener
	List<DetailListener> listeners = new ArrayList<DetailListener>();

	/**
	 *
	 */
	@Override
	public void addDetailListener(DetailListener listener) {
		listeners.add(listener);
	}

	/**
	 * Displays all properties and objects of the selected subject. Matching
	 * properties and objects must have the same index in each ArrayList
	 * respectively.
	 * 
	 * @param subjectString
	 *            string representation of the selected subject
	 * @param properties
	 *            all predicates of the selected subject
	 * @param objects
	 *            all objects of the selected subject
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateDetails(String subjectString, ArrayList<String> properties, ArrayList<String> objects) {
		container.removeAllItems();

		for (int i = 0; i < properties.size(); i++) {
			Item newItem = container.getItem(container.addItem());
			newItem.getItemProperty("Property").setValue(properties.get(i));
			newItem.getItemProperty("Value").setValue(objects.get(i));
		}

		String[] subStrings = subjectString.split("_");
		try {
			grid.setCaption("Detail View: ".concat(subStrings[0]).concat(" ").concat(subStrings[1]));
		} catch (ArrayIndexOutOfBoundsException e) {
		}

		currentSubject = subjectString;
	}

	/**
	 * Delete the currently displayed data.
	 */
	@Override
	public void clearDetails() {
		container.removeAllItems();
	}

	/**
	 * Item click handler method (for testing purposes only)
	 * 
	 * @param event
	 *            the item click event
	 */
	@Override
	public void itemClick(ItemClickEvent event) {
		grid.setCaption(event.getItem().getItemProperty("Value").getValue().toString());
	}

	/**
	 * Click handler method for the search button, the edit button and the
	 * report button.
	 */
	@Override
	public void click(RendererClickEvent event) {
		String kind = event.getPropertyId().toString();
		if (kind.compareTo("Search") == 0) {
			// get the selected object
			String subject = container.getItem(event.getItemId()).getItemProperty("Value").getValue().toString();
			for (DetailListener listener : listeners) {
				// convert to hasRepresentation String
				listener.search(subject.split("_")[1].replaceAll("\\d+.*", ""));
				// select the subject in the updated overview in order to
				// trigger
				// display of the predicates and objects in the detail view
				listener.selectSearchItem(subject);
			}
		}
		if (kind.compareTo("Edit") == 0) {
			// get the selected object
			rowObject = container.getItem(event.getItemId()).getItemProperty("Value").getValue().toString();
			// get the selected property
			rowProperty = container.getItem(event.getItemId()).getItemProperty("Property").getValue().toString();
			// open a popup to create a new entry if object is null
			// or to edit an entry if object is already set
			for (DetailListener listener : listeners) {
				listener.editButtonClick(currentSubject, rowProperty, rowObject);
			}
			rowProperty = null;
		}
		if (kind.compareTo("Report") == 0) {
			// get the selected object
			String value = container.getItem(event.getItemId()).getItemProperty("Value").getValue().toString();
			// get the selected property
			String property = container.getItem(event.getItemId()).getItemProperty("Property").getValue().toString();
			// get the selected subject
			String subject = currentSubject;
			for (DetailListener listener : listeners) {
				listener.openReportPopup(subject, property, value);
			}
		}
	}

	/**
	 * Add the empty properties whose object is missing
	 * 
	 * @param properties
	 *            all empty predicates of the selected subject
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void addMissingPropertys(ArrayList<String> properties) {
		// Add items
		for (int i = 0; i < properties.size(); i++) {
			Item newItem = container.getItem(container.addItem());
			newItem.getItemProperty("Property").setValue(properties.get(i));
			newItem.getItemProperty("Search").setValue(null);
		}
		updateColumns();
	}

	/**
	 * Handle button clicks.
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		for (DetailListener listener : listeners) {
			listener.createButtonClick(currentSubject);
		}
	}

	/**
	 * Add/remove columns to the view depending on user role
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updateColumns() {
		// get the current subject
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated()) {
			// add edit button to all rows if subject is allowed to add
			// property-value pairs
			if (subject.isPermitted("write:add")) {
				container.addContainerProperty("Edit", Resource.class, new ThemeResource("tool.png"));
				grid.getColumn("Edit").setRenderer(new ImageRenderer(this));
				grid.getColumn("Edit").setExpandRatio(0);
				// delete edit button from all rows with non null objects if
				// subject is
				// not permitted to edit existing property-value pairs
				if (!subject.isPermitted("write:edit")) {
					for (Object item : container.getItemIds()) {
						if (container.getItem(item).getItemProperty("Value").getValue().toString().compareTo("") != 0) {
							container.getItem(item).getItemProperty("Edit").setValue(null);
						}
					}
				}
				createButton.setVisible(true);
			}
			// add report button to all rows if subject is allowed to report
			// mistakes
			if (subject.isPermitted("report")) {
				container.addContainerProperty("Report", Resource.class, new ThemeResource("web.png"));
				grid.getColumn("Report").setRenderer(new ImageRenderer(this));
				grid.getColumn("Report").setExpandRatio(0);
				createButton.setVisible(true);
			}
		}
		// hide edit and report buttons from unauthenticated subjects
		if (!subject.isAuthenticated()) {
			container.removeContainerProperty("Edit");
			container.removeContainerProperty("Report");
			createButton.setVisible(false);
		}
	}

	/**
	 * Select a row from the table and focus it into view
	 */
	@Override
	public void selectRow(String property, String object) {
		for (Object id : container.getItemIds()) {
			String conProp = container.getItem(id).getItemProperty("Property").toString();
			String conObj = container.getItem(id).getItemProperty("Value").toString();
			if (conProp.compareTo(property) == 0) {
				if (conObj.compareTo(object) == 0) {
					grid.select(id);
					grid.scrollTo(id);
				}
			}
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
	 * @return the createButton
	 */
	public Button getMissingDataButton() {
		return createButton;
	}

	/**
	 * @return the currentSubject
	 */
	public String getCurrentSubject() {
		return currentSubject;
	}

	/**
	 * @return the listeners
	 */
	public List<DetailListener> getListeners() {
		return listeners;
	}

	/**
	 * @return the rowProperty
	 */
	public String getRowProperty() {
		return rowProperty;
	}
}
