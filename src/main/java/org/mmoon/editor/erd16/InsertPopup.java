package org.mmoon.editor.erd16;

/** Popup
 *
 * @author Robert
 *
 * Last Changes 26.02.2016
 *
 * Insert dialog popup window
 */

import java.util.ArrayList;
import java.util.Iterator;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
* Class for insert dialog pop up window
*/
@SuppressWarnings("serial")
public class InsertPopup extends Window implements InsertPopupInterface,
											 ClickListener {
	/**
	* grid layout for the root content of the window
	*/
	private VerticalLayout subContent;
	/**
	* ok Button
	*/
	private Button ok;
	/**
	* cancel Button
	*/
	private Button cancel;
	/**
	* scroll down menu for types
	*/
	private ComboBox typeSelect;
	/**
	* textfield  for representation String
	*/
	private TextField repTextField;
	/**
	* given array containing all known types
	*/
	private ArrayList<String> allTypes;
	/**
	* assigned subwindow to choose
	*/
	InsertSelectPopup selectPopup;
	/**
	* container holding the drop down menu data source
	*/
	private IndexedContainer container;

	/**
	* constructor to create popup window
	* @param allTypes all known types
	* @param selectPopup popup Window
	*/
	@SuppressWarnings("unchecked")
	public InsertPopup(ArrayList<String> allTypes, InsertSelectPopup selectPopup) {
		super("Create new entry"); // Set window caption

		this.allTypes = allTypes;
		this.selectPopup = selectPopup;

		// Center it in the browser window
//        center();

		container = new IndexedContainer();
		container.addContainerProperty("type", String.class, "");

		for (int i=0; i<allTypes.size(); i++) {
			Item newItem = container.getItem(container.addItem());
			newItem.getItemProperty("type").setValue(allTypes.get(i));
		}

		typeSelect = new ComboBox();
		typeSelect.setContainerDataSource(container);
		typeSelect.setItemCaptionPropertyId("type");

		repTextField = new TextField();
		repTextField.setValue("");

        // Trivial logic for closing the sub-window
        ok = new Button("OK");
        ok.addClickListener(this);
        cancel = new Button("Cancel");
        cancel.addClickListener(this);



//		subContent.addComponent(new Label("Type"),0,0,1,0);
//        subContent.addComponent(new Label("Representation"),0,2,1,2);
//        subContent.addComponent(typeSelect, 2,0,3,1);
//        subContent.addComponent(repTextField,2,2,3,2);
//        subContent.addComponent(ok, 2,5,2,5);
//        subContent.addComponent(cancel,3,5,3,5);
        subContent = new VerticalLayout();
        Label typeSelectLabel = new Label("Type");
        subContent.addComponent(typeSelectLabel);
        subContent.addComponent(typeSelect);
		typeSelect.setWidth("100%");
		Label repTextFieldLabel = new Label("Representation");
        subContent.addComponent(repTextFieldLabel);
        subContent.addComponent(repTextField);
		repTextField.setWidth("100%");
        HorizontalLayout bttns = new HorizontalLayout();
        bttns.addComponent(cancel);
        bttns.addComponent(ok);
        bttns.setSpacing(true);
        subContent.addComponent(bttns);
        subContent.setComponentAlignment(bttns, Alignment.MIDDLE_RIGHT);
        subContent.setSpacing(true);
        subContent.setSizeFull();
        
		// Some basic content for the window
//		subContent = new GridLayout(4,6);
//		subContent.setMargin(true);
//		setContent(subContent);


//		subContent.setWidth("500px");
//		subContent.setHeight("350px");
		
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
	*list containing all registered InsertPopupListener
	*/
	List<InsertPopupListener> listeners = new ArrayList<InsertPopupListener>();

	/**
	* method to add new popup listener
	* @param listener new listener
	*/
	@Override
	public void addInsertPopupListener(InsertPopupListener listener) {
		listeners.add(listener);
	}

	/**
	* method beeing called during buttonClick
	* @param ClickEvent user click event
	*/
	@Override
	public void buttonClick(ClickEvent event) {
		String button = event.getButton().getCaption();

		if (button.equals("OK")) {
			String rep = repTextField.getValue();

			if (typeSelect.getValue()==typeSelect.getNullSelectionItemId() || rep.equals("")) {
				Notification.show("Invalid values!",Notification.Type.WARNING_MESSAGE);
			} else {
				String type = allTypes.get((int)typeSelect.getValue()-1);

				ArrayList<String> doubles = new ArrayList<String>();
				for (InsertPopupListener listener: listeners) {
					doubles = listener.checkDoubles(rep);

				}

				Iterator<String> iter = doubles.iterator();
				String temp;
				while(iter.hasNext()){
					temp = iter.next();
					if(!temp.contains(type)){
						iter.remove();
					}
				}

				if (doubles.size() == 0) {
					for (InsertPopupListener listener: listeners) {
						listener.insertEntry(rep, type, rep);
						// convert to hasRepresentation String
						listener.search(rep);
						// select the subject in the updated overview in order to trigger
						// display of the predicates and objects in the detail view
						listener.selectSearchItem(type + "_" + rep);
					}
				} else {
					selectPopup.setData(doubles, rep, type);
					selectPopup.setVisible(true);
				}

			}

		}
		close();
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
		return typeSelect;
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
	public Button getOKButton() {
		return ok;
	}
	
	/**
	 * @return the button
	 */
	public Button getCancelButton() {
		return cancel;
	}
	
	/**
	 * @return the listeners
	 */
	public List<InsertPopupListener> getListeners() {
		return listeners;
	}

}
