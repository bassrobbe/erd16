package com.example.vorprojekt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;

import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**PopupReportButton
 * 
 * @author Marlo
 * 
 */

import com.vaadin.ui.Window;
//import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;

/*
 * Popup window with the option to report a wrong data set and submit a suggestion for the right value
 */
@SuppressWarnings("serial")
public class PopupReportButton extends Window implements PopupReportButtonInterface{
	
	/**
	 * regarded subject
	 */
	public String subject;
	/**
	 * regarded property
	 */
	public String property;
	/**
	 * regarded value
	 */
	public String value;
	/**
	 * label which shows the regarded triple 
	 */
	public Label infoText;
	/**
	 * main content from the popup window
	 */
	public VerticalLayout content;
	/**
	 * index which is needed to prevent two data files with the same name
	 */
	public int filenameIndex;
	/**
	 * Button to report a mistake
	 */
	public Button reportButton;
	/**
	 * Button to close the window
	 */
	public Button cancelButton;
	/**
	 * Button which refreshes the popup window and opens new content with the options to adjust a suggestion
	 */
	public Button activateFormula;
	/**
	 * Field to type in suggestions
	 */
	public TextField suggestion;
	/**
	 * Button which allows the user to submit his suggestion
	 */
	public Button sendSuggestionButton;
	/**
	 * List of Listeners
	 */
	//presenter will be the only listener
	ArrayList <PopupReportButtonListener> listeners = new ArrayList <PopupReportButtonListener>();
	/**
	 * Creates a new popup window with the options for the user to just report a mistake or submit an own suggestion.
	 * @param subject regarded subject
	 * @param property regarded property
	 * @param value regarded value
	 */
	
	public static final String MESSAGE_PATH = Configuration.message_path;
	
	public PopupReportButton (String subject, String property, String value){
		//window caption
		super("Do you want to report on a mistake in the following entry?");
		//setting stuff
		this.subject = subject;
		this.property = property;
		this.value = value;
		//set filenameIndex to 0, look up @createReportButton()-function to see how it works
		filenameIndex = 0;
		//center in the browser window
		center();
		//init Layout
		content = new VerticalLayout();
		content.setMargin(true);
		setContent(content);
		//create Label with the informations
		infoText = new Label("Subject: "+subject+" Property: "+property+" Value: "+value);
		//create a report button
		reportButton = createReportButton("Report");
		//create cancel button
		cancelButton = createCancelButton("Cancel");
		//create button to activate suggestion option
		activateFormula = activateSuggestionFormula("Add Suggestion");
		
		//order parts with a sublayout
		HorizontalLayout subLayout = new HorizontalLayout();
		subLayout.addComponent(reportButton);
		subLayout.setSpacing(true);
		subLayout.addComponent(cancelButton);
		
		//add components
		content.addComponent(infoText);
		content.addComponent(subLayout);
		content.addComponent(activateFormula);
		//optic
		content.setSpacing(true);
	}
	/**
	 * adds a listener to listeners ArrayList
	 * @param listener the listener to be added
	 */
	@Override
	public void addPopupReportButtonListener(PopupReportButtonListener listener){
		listeners.add(listener);
	}
	/**
	 * Creates a button which reports a wrong data set to the admin. Therefore it writes a data with the informations in a special folder.
	 * @param buttonName caption of the button
	 * @return the button with its functions
	 */
	private Button createReportButton(String buttonName){
		Button newButton = new Button (buttonName);
		newButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(Button.ClickEvent event){
				//TODO: add your own paths
				//
				try{
					//create a filename
					Date date = new Date();
					String fileName = subject + "_"+date.toString();
					fileName = fileName.replace(' ', '_');
					fileName = fileName.replace(':', '-');
					//small check if name is already taken
					//adds an index (filenameIndex) when  file already exists
					File checkFile = new File (MESSAGE_PATH+"Mistake/Unread");
					File[] checkFileArray = checkFile.listFiles();
					boolean check = false;
					//the algorithm
					while (check == false){
						check = true;
						if (filenameIndex != 0){
							fileName += "("+filenameIndex+")";
						}
						for (int index = 0; index < checkFileArray.length; index++){
							if(checkFileArray[index].toString().equals(fileName)){
								check = false;
							}
						}
						if(check == false){
							filenameIndex++;
						}
					}
					FileWriter reportFileWriter = new FileWriter (MESSAGE_PATH+"Mistake/Unread/"+fileName);
					BufferedWriter bufferedReportFileWriter = new BufferedWriter(reportFileWriter);
					//0 is for report text
					bufferedReportFileWriter.write(createReportText(0));
					bufferedReportFileWriter.close();
					Notification.show("Message sent", Notification.Type.TRAY_NOTIFICATION);
					
				}
				catch(Exception e){
					//TODO: add UI component to show that something went wrong
					e.printStackTrace();
					System.err.println("Something went wrong: "+e);
				}
				close();
			}
		});
		return newButton;
	}
	/**
	 * creates a String with the given triple and or a suggestion
	 * @param sendOrReport 0 to create a text needed for the report button -- 1 to create a text needed for the send button
	 * @return report text / suggestion text in a format which is needed for the admin page
	 */
	public String createReportText(int sendOrReport){
		//0 is for report --- 1 is for send
		String reportText = "old:";
		reportText += System.lineSeparator();
		reportText += "---";
		reportText += System.lineSeparator();
		reportText += "subject:"+subject;
		reportText += System.lineSeparator();
		reportText += "property:"+property;
		reportText += System.lineSeparator();
		reportText += "value:"+value;
		reportText += System.lineSeparator();
		reportText += "---";
		if(sendOrReport == 1){
			reportText += System.lineSeparator();
			reportText += "new:";
			reportText += System.lineSeparator();
			reportText += "---";
			reportText += System.lineSeparator();
			reportText += "subject:"+subject;
			reportText += System.lineSeparator();
			reportText += "property:"+property;
			reportText += System.lineSeparator();
			String newValue = suggestion.getValue();
			reportText += "value:"+newValue;
			reportText += System.lineSeparator();
			reportText += "---";
		}
		return reportText;
	}
	/**
	 * creates a close button
	 * @param buttonName button caption
	 * @return cancel button
	 */
	private Button createCancelButton(String buttonName){
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(Button.ClickEvent event){
				close();
			}
		});
		return newButton;
	}
	/**
	 * refreshes the popup window, so the user is allowed to write a suggestion.
	 * @param buttonName button caption
	 * @return button with all described functions
	 */
	private Button activateSuggestionFormula(String buttonName){
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener() {
			@Override
			/*
			 * refreshes the popup window
			 * @see com.vaadin.ui.Button.ClickListener#buttonClick(com.vaadin.ui.Button.ClickEvent)
			 */
			public void buttonClick(Button.ClickEvent event) {
				//disable report button
				//reportButton.setEnabled(false);
				//init suggestion Textfield
				suggestion = new TextField();
				//adjust size
				suggestion.setSizeFull();
				//enable suggestion field
				suggestion.setEnabled(true);
				//small "instruction" for the user
				suggestion.setValue("Type in your suggestion for the right value");
				suggestion.addFocusListener(new FocusListener(){
					@Override
					public void focus(FocusEvent event){
						suggestion.setValue("");
					}
				});
				//init send button
				sendSuggestionButton = createSendButton("Submit Suggestion");
				//create new content window
				content.removeAllComponents();
				content.addComponent(infoText);
				content.addComponent(suggestion);
				//just some design stuff
				HorizontalLayout subLayout = new HorizontalLayout();
				subLayout.addComponent(sendSuggestionButton);
				subLayout.setSpacing(true);
				subLayout.addComponent(cancelButton);
				//add sublayout
				content.addComponent(subLayout);
			}
		});
		return newButton;
	}
	/**
	 * very similar to @createReportButton-function, but it creates a data with a suggestion read from the Textfield, in another folder
	 * @param buttonName
	 * @return button with described functions
	 */
	private Button createSendButton(String buttonName){
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener() {
			@Override 
			public void buttonClick(Button.ClickEvent event){
				//very similar to createReportButton function
				try{
					//create a filename
					Date date = new Date();
					String fileName = subject + "_"+date.toString();
					fileName = fileName.replace(' ', '_');
					fileName = fileName.replace(':', '-');
					//small check if name is already taken
					//adds an index (filenameIndex) when  file already exists
					File checkFile = new File (MESSAGE_PATH+"New");
					File[] checkFileArray = checkFile.listFiles();
					boolean check = false;
					//the algorithm
					while (check == false){
						check = true;
						if (filenameIndex != 0){
							fileName += "("+filenameIndex+")";
						}
						for (int index = 0; index < checkFileArray.length; index++){
							if(checkFileArray[index].toString().equals(fileName)){
								check = false;
							}
						}
						if(check == false){
							filenameIndex++;
						}
					}
					/*
					 * prevent empty values
					 */
					if (suggestion.getValue().equals("") || suggestion.getValue().equals("Type in your suggestion for the right value")){
						suggestion.setValue("Please enter a value");
					}
					else if (suggestion.getValue().equals("Please enter a value")){
						//do nothing
					}
					else{
						FileWriter reportFileWriter = new FileWriter (MESSAGE_PATH+"New/"+fileName);
						BufferedWriter bufferedReportFileWriter = new BufferedWriter(reportFileWriter);
						//1 is for send text
						bufferedReportFileWriter.write(createReportText(1));
						bufferedReportFileWriter.close();
						close();
					}
					Notification.show("Message sent", Notification.Type.TRAY_NOTIFICATION);
				}
				catch(Exception e){
					//TODO: add UI component to show that something went wrong
					e.printStackTrace();
					System.err.println("Something went wrong: "+e);
				}
			}
		});
		return newButton;
	}
	
}
