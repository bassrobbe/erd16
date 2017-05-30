package org.mmoon.editor.erd16;

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
 * A form for creating a new property-value pair for a specified subject
 */
@SuppressWarnings("serial")
public class PopUpAddProperty extends Window implements PopUpAddPropertyInterface, ValueChangeListener, ClickListener {

	private VerticalLayout layout;

	private VerticalLayout wrapper;

	/**
	 * Display of object choices
	 */
	private ComboBox objects;

	/**
	 * Display of property choices
	 */
	private ComboBox properties;

	/**
	 * Set of objects that can be assigned to a property
	 */
	private IndexedContainer objectsData;

	/**
	 * Set of properties that can be assigned to a subject
	 */
	private IndexedContainer propertiesData;

	private Label objectsLabel;

	private Label propertiesLabel;

	/**
	 * The subject to which a property-value pair should be added
	 */
	private String subject;

	private Button ok;

	private Button abort;

	/**
	 * Layout the components and instantiate data containers
	 * 
	 * @param subject
	 *            The subject to which a property-value pair should be added
	 */
	public PopUpAddProperty(String subject) {
		super("Add Property");

		this.subject = subject;

		propertiesData = new IndexedContainer();
		propertiesData.addContainerProperty("propValue", String.class, "");

		objectsData = new IndexedContainer();
		objectsData.addContainerProperty("objValue", String.class, "");

		propertiesLabel = new Label("Chosse the property you want to add for the subject");

		properties = new ComboBox();
		properties.setContainerDataSource(propertiesData);
		properties.setItemCaptionPropertyId("propValue");
		properties.setNullSelectionAllowed(false);
		properties.addValueChangeListener(this);

		objectsLabel = new Label("Select the instance of the property.");

		objects = new ComboBox();
		objects.setContainerDataSource(objectsData);
		objects.setItemCaptionPropertyId("objValue");
		objects.setNullSelectionAllowed(false);
		objects.addValueChangeListener(this);
		objects.setEnabled(false);

		ok = new Button("OK");
		ok.setEnabled(false);
		ok.addClickListener(this);

		abort = new Button("Cancel");
		abort.addClickListener(this);

		layout = new VerticalLayout();
		layout.addComponent(propertiesLabel);
		layout.addComponent(properties);
		properties.setWidth("100%");
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
		wrapper.setComponentAlignment(layout, Alignment.TOP_CENTER);
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
	List<PopUpAddPropertyListener> listeners = new ArrayList<PopUpAddPropertyListener>();

	@Override
	public void addPopUpAddPropertyListener(PopUpAddPropertyListener listener) {
		listeners.add(listener);
		fetchPropertiesData();
	}

	/**
	 * Fetch list of properties assignable to the current subject
	 */
	@Override
	public void fetchPropertiesData() {
		for (PopUpAddPropertyListener listener : listeners) {
			listener.updatePropertiesData(this.subject);
		}
	}

	/**
	 * Fetch list of objects assignable to the selected property
	 */
	@Override
	public void fetchObjectsData(String subject, String property) {
		for (PopUpAddPropertyListener listener : listeners) {
			listener.updateObjectsData(this.subject, property);
		}
	}

	/**
	 * Update fetched assignable properties
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setPropertiesData(ArrayList<String> propertiesData) {
		this.propertiesData.removeAllItems();
		for (String prop : propertiesData) {
			Item item = this.propertiesData.getItem(this.propertiesData.addItem());
			item.getItemProperty("propValue").setValue(prop);
		}
		this.propertiesData.sort(new Object[] { "propValue" }, new boolean[] { true });
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
		if (((ComboBox) event.getProperty()) == properties) {
			objects.setEnabled(true);
			fetchObjectsData(this.subject, properties.getItemCaption(event.getProperty().getValue()).toString());
		}
		if (((ComboBox) event.getProperty()) == objects) {
			ok.setEnabled(true);
		}
	}

	/**
	 * Handle button clicks
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		// ok button click
		if (event.getSource() == ok) {
			String subj = this.subject;
			String prop = properties.getItemCaption(properties.getValue());
			String obj = objects.getItemCaption(objects.getValue());
			// insert triple into database
			for (PopUpAddPropertyListener listener : listeners) {
				listener.commitAddProperty(subj, prop, obj);
			}
			this.close();
		}
		// cancel button click
		if (event.getSource() == abort) {
			this.close();
		}

	}

	/**
	 * @return the properties dropdown
	 */
	public ComboBox getProperties() {
		return this.properties;
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
	 * @return the propertiesData
	 */
	public IndexedContainer getPropertiesData() {
		return propertiesData;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}
}
