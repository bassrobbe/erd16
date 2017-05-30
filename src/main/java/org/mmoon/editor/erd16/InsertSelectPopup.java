package org.mmoon.editor.erd16;


import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


/**
* class for selecting an entry to insert
*/
@SuppressWarnings("serial")
public class InsertSelectPopup extends Window implements ClickListener, InsertSelectPopupInterface {

	/**
	* grid layout as root component of the window
	*/
	private VerticalLayout subContent;
	/**
	* drop down menu to select entry
	*/
	private ComboBox entrySelect;
	/**
	* ok button
	*/
	private Button ok;
	/**
	 * 
	 */
	private Button cancel;
	/**
	* existing entries
	*/
	private ArrayList<String> entries;
	/**
	* container holding the drop down menu data source
	*/
	private IndexedContainer container;
	/**
	* given representation String
	*/
	private String representation;
	/**
	* given type String
	*/
	private String type;

/**
* constructor to build popup window
*/
	public InsertSelectPopup() {
		super("Create new entry"); // Set window caption

	    ok = new Button("OK");
	    ok.addClickListener(this);
	    cancel = new Button("Cancel");
	    cancel.addClickListener(this);

		container = new IndexedContainer();
		container.addContainerProperty("entry", String.class, "");

		entrySelect = new ComboBox();
		entrySelect.setContainerDataSource(container);
		entrySelect.setItemCaptionPropertyId("entry");
	    
		// Some basic content for the window
		subContent = new VerticalLayout();
//		subContent.setMargin(true);
		subContent.setSizeFull();
		subContent.setSpacing(true);

		
		subContent.addComponent(new Label("Entry with this representation already exists. Choose an existing entry or create a new one."));
		subContent.addComponent(entrySelect);
		entrySelect.setWidth("100%");
		HorizontalLayout bttns = new HorizontalLayout();
		bttns.addComponent(cancel);
		bttns.addComponent(ok);
		bttns.setSpacing(true);
		subContent.addComponent(bttns);
		subContent.setComponentAlignment(bttns, Alignment.MIDDLE_RIGHT);
		
		VerticalLayout wrapper = new VerticalLayout();
		wrapper.addComponent(subContent);
		wrapper.setComponentAlignment(subContent, Alignment.TOP_CENTER);
		wrapper.setMargin(true);
		wrapper.setSizeFull();
		setContent(wrapper);
		
		this.setHeight("350px");
		this.setWidth("500px");
		this.center();
        
        // Disable the close button
        setClosable(false);
        setModal(true);
        setResizable(false);
	}




    // Only the presenter registers one listener...
		/**
		*list containing all registered InsertSelectPopupListener
		*/
		List<InsertSelectPopupListener> listeners = new ArrayList<InsertSelectPopupListener>();

		/**
		* method to add new popup listener
		* @param listener new InsertSelectPopupListener
		*/
	@Override
	public void addInsertSelectPopupListener(InsertSelectPopupListener listener) {
		listeners.add(listener);

	}

	/**
	* method being called during buttonClick
	* @param event user click event
	*/
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getSource().equals(ok)) {
			if (entrySelect.getValue()==entrySelect.getNullSelectionItemId()) {
				Notification.show("Invalid value!", Notification.Type.WARNING_MESSAGE);
			} else {
				if ((int)entrySelect.getValue()==1) {
					boolean representationAlreadyInUse = true;
					int i = 0;
	
					while(representationAlreadyInUse) {
						i++;
						for (InsertSelectPopupListener listener: listeners) {
							representationAlreadyInUse=listener.searchForUnusedRepresentation(type + "_" + representation + String.valueOf(i));
						}
						System.out.println(type + "_" + representation + String.valueOf(i));
						System.out.println(i);
					}
	
					for (InsertSelectPopupListener listener: listeners) {
						System.out.println(representation + String.valueOf(i));
						listener.insertEntry(representation + String.valueOf(i), type, representation);
						// convert to hasRepresentation String
						listener.search(representation);
						// select the subject in the updated overview in order to trigger
						// display of the predicates and objects in the detail view
						listener.selectSearchItem(type + "_" +representation + String.valueOf(i));
					}
	
				} else {
					int i = (int)entrySelect.getValue() -2;
					for (InsertSelectPopupListener listener: listeners) {
						// convert to hasRepresentation String
						listener.search(entries.get(i).split("_")[1].replaceAll("\\d+.*", ""));
						// select the subject in the updated overview in order to trigger
						// display of the predicates and objects in the detail view
						listener.selectSearchItem(entries.get(i));
					}
				}
			}
	
			close();
		}
		if (event.getSource().equals(cancel)) {
			close();
		}
	}

	/**
	* set data source of the class
	* @param entries entries to show in drop down menu
	* @param representation given representaion string
	* @param type given type
	*/
	@SuppressWarnings("unchecked")
	public void setData(ArrayList<String> entries, String representation, String type) {
	    this.entries = entries;
	    this.representation = representation;
	    this.type = type;

		Item newItem = container.getItem(container.addItem());
		newItem.getItemProperty("entry").setValue("new");

		for (int i=0; i<entries.size(); i++) {
			Item existingItem = container.getItem(container.addItem());
			existingItem.getItemProperty("entry").setValue(entries.get(i));
		}

	}
	
	/**
	 * @return the layout
	 */
	public VerticalLayout getLayout() {
		return subContent;
	}

	/**
	 * @return the ComboBox
	 */
	public ComboBox getComboBox() {
		return entrySelect;
	}

	/**
	 * @return the container
	 */
	public IndexedContainer getContainer() {
		return container;
	}

	/**
	 * @return the button
	 */
	public Button getButton() {
		return ok;
	}
	
	/**
	 * @return the listeners
	 */
	public List<InsertSelectPopupListener> getListeners() {
		return listeners;
	}

}
