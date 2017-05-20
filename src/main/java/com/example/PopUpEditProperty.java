package com.example.vorprojekt;

import java.util.ArrayList;
import java.util.List;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * A form for editing the value of a property-value pair for a specified subject
 */
@SuppressWarnings("serial")
public class PopUpEditProperty extends Window
		implements PopUpEditPropertyInterface, ValueChangeListener, ClickListener {

	private VerticalLayout layout;

	private VerticalLayout wrapper;

	/**
	 * Display of object choices
	 */
	private ComboBox objects;

	/**
	 * Set of objects that can be assigned to a property
	 */
	private IndexedContainer objectsData;

	private Label objectsLabel;

	/**
	 * The subject of the property-value pair that should be edited
	 */
	private String subject;

	/**
	 * The currently assigned value of the property-value pair that should be
	 * edited
	 */
	private String oldObject;

	/**
	 * The property of the property-value pair that should be edited
	 */
	private String property;

	private Button ok;

	private Button abort;

	/**
	 * Layout the components and instantiate data containers
	 * 
	 * @param subject
	 *            The subject to which a property-value pair should be added
	 */
	public PopUpEditProperty(String subject, String property, String oldObject) {
		super("Edit Property");

		this.subject = subject;
		this.oldObject = oldObject;
		this.property = property;

		objectsData = new IndexedContainer();
		objectsData.addContainerProperty("objValue", String.class, "");

		objectsLabel = new Label("Select the instance of the property.");

		objects = new ComboBox();
		objects.setContainerDataSource(objectsData);
		objects.setItemCaptionPropertyId("objValue");
		objects.setNullSelectionAllowed(false);
		objects.addValueChangeListener(this);

		ok = new Button("OK");
		ok.setEnabled(false);
		ok.addClickListener(this);

		abort = new Button("Cancel");
		abort.addClickListener(this);

		layout = new VerticalLayout();
		layout.addComponent(objectsLabel);
		layout.addComponent(objects);
		objects.setWidth("100%");
		HorizontalLayout subLayout = new HorizontalLayout();
		subLayout.addComponent(abort);
		subLayout.addComponent(ok);
		subLayout.setSpacing(true);
		layout.addComponent(subLayout);
		layout.setComponentAlignment(subLayout, Alignment.MIDDLE_RIGHT);
		layout.setSpacing(true);
		layout.setSizeFull();

		wrapper = new VerticalLayout();
		wrapper.addComponent(layout);
		wrapper.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
		wrapper.setMargin(true);
		wrapper.setSizeFull();
		setContent(wrapper);

		this.setHeight("350px");
		this.setWidth("500px");
		this.center();
		
		this.setClosable(false);
		this.setModal(true);
		this.setResizable(false);
	}

	// presenter will be the only listener
	List<PopUpEditPropertyListener> listeners = new ArrayList<PopUpEditPropertyListener>();

	@Override
	public void addPopUpAddPropertyListener(PopUpEditPropertyListener listener) {
		listeners.add(listener);
		fetchObjectsData(subject, property);
	}

	/**
	 * Fetch list of objects assignable to the selected property
	 */
	@Override
	public void fetchObjectsData(String subject, String property) {
		for (PopUpEditPropertyListener listener : listeners) {
			listener.updateObjectsDataEdit(this.subject, property);
		}
	}

	/**
	 * Update fetched assignable objects
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setObjectsData(ArrayList<String> objectsData) {
		this.objectsData.removeAllItems();
		for (String obj : objectsData) {
			Item item = this.objectsData.getItem(this.objectsData.addItem());
			item.getItemProperty("objValue").setValue(obj);
		}
		this.objectsData.sort(new Object[] { "objValue" }, new boolean[] { true });
	}

	/**
	 * Handle selection of ComboBox items
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		ok.setEnabled(true);
	}

	/**
	 * Handle button clicks
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		// ok button click
		if (event.getSource() == ok) {
			String subj = this.subject;
			String prop = this.property;
			String obj = objects.getItemCaption(objects.getValue());
			// insert triple into database
			for (PopUpEditPropertyListener listener : listeners) {
				listener.commitEditProperty(subj, prop, obj, this.oldObject);
			}
			this.close();
		}
		// cancel button click
		if (event.getSource() == abort) {
			this.close();
		}

	}
	/**
	 * @return the objects dropdown
	 */
	public ComboBox getObjects() {
		return this.objects;
	}

	/**
	 * @return the objectsData
	 */
	public IndexedContainer getObjectsData() {
		return objectsData;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @return the oldObject
	 */
	public String getOldObject() {
		return oldObject;
	}

	/**
	 * @return the property
	 */
	public String getProperty() {
		return property;
	}
}
