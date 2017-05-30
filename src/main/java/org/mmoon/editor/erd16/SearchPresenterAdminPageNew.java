package org.mmoon.editor.erd16;

import java.io.File;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;

/**
 * This class serve as the presenter of the Admin User Interface. It handles
 * all interactions, reacts to them and initiate updates on the model.
 * @author Marlo
 *
 */
public class SearchPresenterAdminPageNew implements AdminPageNewInterface.AdminPageNewListener,
												PopupAdminPageInterface.PopupAdminPageListener,
												PopupMistakesInterface.PopupMistakeListener{
	/**
	 * the controlled admin page
	 */
	AdminPageNew adminPage;
	/**
	 * the controlled model
	 */
	QueryDBSPARQL model;
	/**
	 * init controlled admin page
	 * @param adminPage admin page which will be supervised
	 */
	public SearchPresenterAdminPageNew(AdminPageNew adminPage, QueryDBSPARQL model){
		//set admin page
		this.adminPage = adminPage;
		//set model
		this.model = model;
		//add itself as a listener
		adminPage.addAdminPageNewListener(this);
	}
	
	@Override
	/**
	 * creates different popup windows depending on chosen item
	 * @param category selected category
	 * @param item selected item
	 * @param file corresponding file to chosen item
	 */
	public void selectItem (String category, String item, File file){
			//opens specific Reported Mistake Pop up
			if (category.equals("Unread") || category.equals("Read")){
				PopupMistakes popup = new PopupMistakes(category, file);
				SearchUIAdminPageNew.getCurrent().addWindow(popup);
				popup.addPopupMistakeListener(this);
			}
			//opens different suggestion popups with various options
			else{
				PopUpAdminPage popup = new PopUpAdminPage(category, item, file);
				popup.addPopupAdminPageListener(this);
				// Add it to the root component
				SearchUIAdminPageNew.getCurrent().addWindow(popup);	
			}
	}
	
	/**
	 * refreshes the admin page's window
	 */
	@Override
	public void refreshWindow() {
		try{
		adminPage.updateGrid("Suggestions");
		}
		catch(Exception e){
			
		}
		try{
		adminPage.updateGrid("Mistakes");
		}
		catch(Exception e){
			
		}
		try{
		adminPage.updateGrid("Read");
		}
		catch(Exception e){
			
		}
		try{
		adminPage.updateGrid("Accepted Suggestions");
		}
		catch(Exception e){
			
		}
		try{
		adminPage.updateGrid("Refused Suggestions");
		}
		catch(Exception e){
			
		}
		try{
		adminPage.updateGrid("Unedit Suggestions");
		}
		catch(Exception e){
			
		}
	}

	/**
	 * Updates the DB with the given Triple
	 * @param subject String representation of the subject
	 * @param propertyAndObject Item containing property and object
	 */
	@Override
	public void updateDB(String subject, String property, String object, String oldObject) {
//		model.deleteValue(subject, property, oldObject);
//		System.out.println("delete: "+model.verifyUpdate(subject, property, object));
//		model.insertValue(subject, property, object);
//		System.out.println("insert: "+model.verifyUpdate(subject, property, object));
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
		} else {
			model.insertValue(subject, property, oldObject);
			Notification.show("Error", "Triple:" + "\n" + "\n" + subject + ", " + property + ", " + object + "\n" + "\n"
					+ "could not be added to database.", Notification.Type.TRAY_NOTIFICATION);
		}
	}
}
