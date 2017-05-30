package org.mmoon.editor.erd16;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


@SuppressWarnings("serial")
public class PopUpAdminPage extends Window implements PopupAdminPageInterface{
	/**
	 * main content
	 */
	VerticalLayout popupContent;
	/**
	 * list of suggested subjects
	 */
	LinkedList <String> subjectNew;
	/**
	 * list of suggested properties
	 */
	LinkedList <String> propertyNew;
	/**
	 * list of suggested values
	 */
	LinkedList <String> valueNew;
	/**
	 * list of old subjects
	 */
	LinkedList <String> subjectOld;
	/**
	 * list of old properties
	 */
	LinkedList <String> propertyOld;
	/**
	 * list of old values
	 */
	LinkedList <String> valueOld;
	/**
	 * index needed for correct data names
	 */
	int globalIndex;
	/**
	 * list of accept button ids
	 */
	//useless since new implementation
	LinkedList <String> acceptButtonIds;
	/**
	 * list of refuse button ids
	 */
	//useless since new implementation
	LinkedList <String> refuseButtonIds;
	/**
	 * list of accept buttons
	 */
	LinkedList <Button> acceptButtons;
	/**
	 * list of refuse buttons
	 */
	LinkedList <Button> refuseButtons;
	/**
	 * button to refuse all suggestions
	 */
	//not used since new implementation
	Button refuseAll;
	/**
	 * button to accept all suggestions
	 */
	//not used since new implementation
	Button acceptAll;
	/**
	 * button to accept a suggestion which was refused just before
	 */
	Button acceptButtonRefusedPopup;
	/**
	 * index needed for correct data names
	 */
	int testIndex;
	/**
	 * index needed for correct data names
	 */
	int testIndex2;
	/**
	 * index needed for correct data names
	 */
	int testIndex3;
	/**
	 * current opened suggestion file
	 */
	File currentFile;
	/**
	 * flag needed for all accept / refuse button
	 */
	//useless since new implementation
	LinkedList <Integer> positionFlag;
	/**
	 * chosen category from menu bar in AdminPage class
	 */
	final String category;
	
	public static final String MESSAGE_PATH = Configuration.messages_path;
	
	/**
	 * Creates a popup window with different working operation, depending on the category.
	 * @param category chosen category from menu bar in AdminPage class
	 * @param item chosen suggestion file from a category from menu bar in string format
	 * @param path the chosen suggestion file
	 */
	public PopUpAdminPage(String category, String item, File path){
		//window caption
		super("Suggestion");
		//init indices
		testIndex = 0;
		testIndex2 = 0;
		testIndex3 = 0;
		//set category
		this.category = category;
		//set filePath
		currentFile = path;
		//initialize LinkedLists
		subjectNew = new LinkedList <String> ();
		propertyNew = new LinkedList <String>();
		valueNew = new LinkedList <String>();
		subjectOld = new LinkedList <String> ();
		propertyOld = new LinkedList <String>();
		valueOld = new LinkedList <String>();
		acceptButtonIds = new LinkedList <String>();
		refuseButtonIds = new LinkedList <String>();
		acceptButtons = new LinkedList <Button>();
		refuseButtons = new LinkedList <Button>();
		positionFlag = new LinkedList <Integer>();
		//center popup window
		center();
		//create popup window with some basics
		popupContent = new VerticalLayout();
		popupContent.setMargin(true);
		setContent(popupContent);

		//standard procedure to read from a file
		try{
			FileReader fileReader = new FileReader(path);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			StringBuffer stringBuffer = new StringBuffer();
			//read the whole file and append it
			while((line = bufferedReader.readLine()) != null){
				stringBuffer.append(line);
			}
			//close the reader
			bufferedReader.close();
			//save it by call the toString from the StringBuffer class
			String fullText = stringBuffer.toString();
			//algorithm for the specific file format
			//1st step:
			String [] entries = fullText.split("---");
			//create lists to save machine-unreadable entries in local lists
			LinkedList <String> oldEntries = new LinkedList <String>();
			LinkedList <String> newEntries = new LinkedList <String>();
			/*
			 * small algorithm to save old / new entries in linked lists
			 */
			int counter1 = 0;
			int counter2 = 2;
			for (int index = 1; index < entries.length; index++){
				if(counter1 == 0 ){
					oldEntries.add(entries[index]);
					counter1=4;
				}
				if(counter2 == 0){
					newEntries.add(entries[index]);
					counter2=4;
				}
				counter1--;
				counter2--;
			}
			
			/*
			 * split entry strings to a machine-readable format
			 */
			for (int index = 0; index < oldEntries.size(); index++){
				/*
				 * file format specific split operations
				 */
				globalIndex = index;
				String tempOld = oldEntries.get(index);
				String [] temp1Old = tempOld.split("subject:");
				String [] temp2Old = temp1Old[1].split("property:");
				String [] temp3Old = temp2Old[1].split("value:");
				
				String tempNew = newEntries.get(index);
				String [] temp1New = tempNew.split("subject:");
				String [] temp2New = temp1New[1].split("property:");
				String [] temp3New = temp2New[1].split("value:");
				//extracted old subject 
				String subjectOld = temp2Old[0];
				this.subjectOld.add(subjectOld);
				//extracted old property 
				String propertyOld = temp3Old[0];
				this.propertyOld.add(propertyOld);
				//extracted old value
				String valueOld = temp3Old[1];
				this.valueOld.add(valueOld);
				//extracted new subject and add it to list
				String subjectNew = temp2New[0];
				this.subjectNew.add(subjectNew);
				//extracted new property and add it to list
				String propertyNew = temp3New[0];
				this.propertyNew.add(propertyNew);
				//extracted new value and add it to list
				String valueNew = temp3New[1];
				this.valueNew.add(valueNew);
				//add labels
					//choose preferred label
				//popupContent.addComponent(new Label("Subject: " +subjectOld+ "  \t  Property: " +propertyOld+ "  \t  Value: " +valueOld));
				//popupContent.addComponent(new Label("Subject: " +subjectNew+ "  \t  Property: " +propertyNew+ "  \t  Value: " +valueNew));
				popupContent.addComponent(new Label("Old:"));
				popupContent.addComponent(new Label(subjectOld+"   \t "+propertyOld+"   \t "+valueOld));
				popupContent.addComponent(new Label("---"));
				popupContent.addComponent(new Label("New:"));
				popupContent.addComponent(new Label(subjectNew+"  \t  "+propertyNew+" \t   "+valueNew));
				//place holder
				popupContent.addComponent(new Label(""));
				/*
				 * following window content is category specific
				 */
				if (category.equals("Refused")){
					//by pressing the Edit button, you can accept an already refused suggestion
					Button edit = new Button ("Edit");
					//create click listener, which enables the accept button
					edit.addClickListener(new Button.ClickListener(){
						@Override
						public void buttonClick (Button.ClickEvent event){
							acceptButtonRefusedPopup.setEnabled(true);
							event.getButton().setEnabled(false);
						}
					});
					//init buttons
					acceptButtonRefusedPopup = createAcceptButtonRefusedPopup("Accept");
					Button delete = createDeleteButton("Delete this Notification");
					//layout stuff
					HorizontalLayout subContent = new HorizontalLayout();
					subContent.addComponent(edit);
					subContent.addComponent(acceptButtonRefusedPopup);
					subContent.addComponent(delete);
					subContent.setSpacing(true);
					popupContent.addComponent(subContent);
				}
				else if (category.equals("Accepted")){
					//create buttons
					Button restore = createRestoreButton("Restore");
					Button delete = createDeleteButton("Delete this Notification");
					//layout stuff
					HorizontalLayout subContent = new HorizontalLayout();
					subContent.addComponent(restore);
					subContent.addComponent(delete);
					subContent.setSpacing(true);
					//add buttons
					popupContent.addComponent(subContent);
					//popupContent.addComponent(delete);
				}
				//else case == "New Suggestions" category or "Unedit"
				else{
					//create buttons to accept or refuse suggestions
					Button accept = createAcceptButton("Accept");
					Button refuse = createRefuseButton("Refuse");
					//layout stuff
					HorizontalLayout subContent = new HorizontalLayout();
					subContent.addComponent(accept);
					subContent.addComponent(refuse);
					subContent.setSpacing(true);
					//add buttons
					popupContent.addComponent(subContent);
					//popupContent.addComponent(accept);
					//popupContent.addComponent(refuse);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Something went wrong (init PopupAdminPage): "+e);
		}
		/*
		 * changing implementation makes all-buttons become useless
		 */
		/*
		if (category.equals("Refused")){
			
		}
		else if (category.equals("Accepted")){
			
		}
		else{
			//create buttons to refuse / accept all suggestions
			refuseAll = createRefuseAllButton("Refuse All");
			acceptAll = createAcceptAllButton("Accept All");
			//add buttons
			popupContent.addComponent(acceptAll);
			popupContent.addComponent(refuseAll);
		}*/
		/*
		 * close listener checks if a new suggestion was edited
		 */
		addCloseListener(new Window.CloseListener(){
			@Override
			public void windowClose(CloseEvent e){
				//when suggestion was opened but not accepted or refused its moved to unedit folder
				String temp = getCategory();
				if(temp.contains("New")){
					writeUneditSuggestion();
					currentFile.delete();
				}
				for(PopupAdminPageListener listener : listeners){
					listener.refreshWindow();
				}
			}
		});
	}
	/**
	 * list of all listener
	 */
	List<PopupAdminPageListener> listeners = new ArrayList<PopupAdminPageListener>();
	/**
	 * adds a PopAdminPageListener
	 */
	@Override
	public void addPopupAdminPageListener(PopupAdminPageListener listener) {
		listeners.add(listener);		
	}
	/**
	 * function to parse new suggestion which were not edited and move them to Unedit folder 
	 */
	public void writeUneditSuggestion(){
		//if (true) -> some suggestions were neither accepted nor refused 
		if (positionFlag.size() < subjectNew.size()){
			//algorithm to cope with that case 
			try{
				/*
				 *first part is only for correct names 
				 */
				File tempFile = new File (MESSAGE_PATH+"Unedit");
				File[] fileArray = tempFile.listFiles();
				//check if data already exists
				String suggestionName = MESSAGE_PATH+"Unedit/suggestion"+testIndex3;
				boolean check = false;
				while(check == false){
					check = true;
					suggestionName = MESSAGE_PATH+"Unedit/suggestion"+testIndex3;
					for(int index = 0; index < fileArray.length; index++){
						if(fileArray.length > 0){
							if(fileArray[index].toString().equals(suggestionName)){
								check = false;
							}
						}	
					}
					if(check == false){
						testIndex3++;
					}
				}	
				/*
				 * second part writes unedited suggestion down in a file in the unedited folder
				 */
				FileWriter fw = new FileWriter(MESSAGE_PATH+"Unedit/suggestion"+testIndex3);
				BufferedWriter bw = new BufferedWriter(fw);
				//list with all item ids
				LinkedList <Integer> allIds = new LinkedList <Integer> ();
				for(int element = 0; element < subjectNew.size(); element++){
					allIds.add(element);
				}
				for(Integer temp : positionFlag){
					allIds.remove(temp);
				}
				for(Integer temp : allIds){
					bw.write(getCurrentElement(temp));
				}
				
				bw.close();
			}
			catch (Exception e){
				e.printStackTrace();
				System.err.println("Something went wrong: (accepButton refusedpopup)"+e);
			}
		}
	}
	/**
	 * creates a button that allows the admin to accept a previous refused suggestion
	 * @param buttonName caption of the button
	 * @return button with all described functions
	 */
	public Button createAcceptButtonRefusedPopup(String buttonName){
		Button newButton = new Button(buttonName);
		newButton.setEnabled(false);
		newButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				try{
					/*
					 * first part is only for correct file names
					 */
					File tempFile = new File (MESSAGE_PATH+"Accepted");
					File[] fileArray = tempFile.listFiles();
					//check if data already exists
					String suggestionName = MESSAGE_PATH+"Accepted/suggestion"+testIndex2;
					boolean check = false;
					while(check == false){
						check = true;
						suggestionName = MESSAGE_PATH+"Accepted/suggestion"+testIndex2;
						for(int index = 0; index < fileArray.length; index++){
							if(fileArray.length > 0){
								if(fileArray[index].toString().equals(suggestionName)){
									check = false;
								}
							}	
						}
						if(check == false){
							testIndex2++;
						}
					}	
					/*
					 * second part writes down the suggestion in Accepted folder
					 */
					FileWriter fw = new FileWriter(MESSAGE_PATH+"Accepted/suggestion"+testIndex2);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.append(getCurrentElement(0));
					bw.close();
					currentFile.delete();
					for(PopupAdminPageListener listener : listeners){
						listener.refreshWindow();
					}
					close();
				}
				catch (Exception e){
					e.printStackTrace();
					System.err.println("Something went wrong: (accepButton refusedpopup)"+e);
				}
				
				for(PopupAdminPageListener listener : listeners){
					listener.updateDB(subjectNew.get(0), propertyNew.get(0), valueNew.get(0), valueOld.get(0));
				}
			}
		});
		return newButton;
	}
	/**
	 * creates a button to accept all suggestions
	 * @param buttonName button caption
	 * @return button with all described functions
	 */
	//useless since new implementation
	public Button createAcceptAllButton(String buttonName){
		Button newButton = new Button (buttonName);
		newButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(Button.ClickEvent event){
				//button just presses all accept buttons
				for(Button acceptButton : acceptButtons){
					acceptButton.click();	
				}
				for(PopupAdminPageListener listener : listeners){
					listener.refreshWindow();
				}
			refuseAll.setEnabled(false);
			acceptAll.setEnabled(false);
			}
		});
		return newButton;
	}
	/**
	 * same as @createAcceptAllButton just with refuse function
	 * @param buttonName button caption
	 * @return button with all described functions
	 */
	//useless since new implementation
	public Button createRefuseAllButton(String buttonName){
		Button newButton = new Button (buttonName);
		newButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(Button.ClickEvent event){
				for(Button refuseButton : refuseButtons){
					refuseButton.click();
				}
				for(PopupAdminPageListener listener : listeners){
					listener.refreshWindow();
				}
			refuseAll.setEnabled(false);
			acceptAll.setEnabled(false);
			}
		});
		return newButton;
	}
	/**
	 * creates a button which delete the current file
	 * @param buttonName button caption
	 * @return button with all described functions
	 */
	public Button createDeleteButton(String buttonName){
		Button newButton = new Button (buttonName);
		newButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(Button.ClickEvent event){
				currentFile.delete();
				close();
				for(PopupAdminPageListener listener : listeners){
					listener.refreshWindow();
				}
				
			}
		});
		return newButton;
	}
	/**
	 * creates a button which allows the admin to accept a suggestion
	 * @param buttonName button caption
	 * @return button with all described functions
	 */
	public Button createAcceptButton(String buttonName){
		//create button
		Button newButton = new Button (buttonName);
		//add id to the list
		acceptButtonIds.add(newButton.toString());
		//add button to the list
		acceptButtons.add(newButton);
		//performed action when admin presses the button
		newButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(Button.ClickEvent event){
				/*
				 * most parts of the algorithm used to be important, when there was a Accept All button
				 */
				for (String temp : acceptButtonIds){
					if(temp.equals(event.getButton().toString())){
						event.getButton().setEnabled(false);
						//identify which accept button was pressed
						int position=0;
						for (String temp_2 : acceptButtonIds){
							if (event.getButton().toString().equals(temp_2.toString())){
								position = acceptButtonIds.indexOf(temp_2);
								//add position to a flag list to see which suggestion were edited
								positionFlag.add(position);
								//disable refuse button
								refuseButtons.get(position).setEnabled(false);
							}
						}
						/*
						 * first part is only for correct file name
						 */
						try{
							File tempFile = new File (MESSAGE_PATH+"Accepted");
							File[] fileArray = tempFile.listFiles();
							//check if data already exists
							String suggestionName = MESSAGE_PATH+"Accepted/suggestion"+testIndex;
							boolean check = false;
							while(check == false){
								check = true;
								suggestionName = MESSAGE_PATH+"Accepted/suggestion"+testIndex;
								for(int index = 0; index < fileArray.length; index++){
									if(fileArray.length > 0){
										if(fileArray[index].toString().equals(suggestionName)){
											check = false;
										}
									}	
								}
								if(check == false){
									testIndex++;
								}
							}	
							/*
							 * second part writes it down in a file in the Accepted folder
							 */
							FileWriter fw = new FileWriter(MESSAGE_PATH+"Accepted/suggestion"+testIndex);
							BufferedWriter bw = new BufferedWriter(fw);
							bw.append(getCurrentElement(acceptButtonIds.indexOf(temp)));
							bw.close();
						}
						catch (Exception e){
							e.printStackTrace();
							System.err.println("Something went wrong: "+e);
						}
						//ask to refresh admin page window
						for(PopupAdminPageListener listener : listeners){
							listener.refreshWindow();
						}
						
						for(PopupAdminPageListener listener : listeners){
							listener.updateDB(subjectNew.get(0), propertyNew.get(0), valueNew.get(0), valueOld.get(0));
						}
					}
				}
				if (acceptButtons.size()==1){
						currentFile.delete();
						close();
				}
				//would have been important when there would still be an Accept All button
				else{
				}	
			}
		});
				/*if (acceptButtons.size()==1){
					if(!category.equals("Unedit")){
						currentFile.delete();
					}
				}
				//would have been important when there would still be an Accept All button
				else{
				}*/
		return newButton;
	}
	
	/**
	 * creates a button which allows the admin to refuse a suggestion. very similar to @createAcceptButton
	 * @param buttonName button caption
	 * @return button with all described functions
	 */
	public Button createRefuseButton(String buttonName){
		Button newButton = new Button (buttonName);
		refuseButtonIds.add(newButton.toString());
		refuseButtons.add(newButton);
		newButton.addClickListener(new Button.ClickListener(){
			@Override
			public void buttonClick(Button.ClickEvent event){
				for (String temp : refuseButtonIds){
					if(temp.equals(event.getButton().toString())){
						event.getButton().setEnabled(false);
						int position=0;
						for (String temp_2 : refuseButtonIds){
							if (event.getButton().toString().equals(temp_2.toString())){
								position = refuseButtonIds.indexOf(temp_2);
								//add position to a flag list to see which suggestion were edited
								positionFlag.add(position);
								//disable accept button
								acceptButtons.get(position).setEnabled(false);
							}
						}
						try{
							File tempFile = new File (MESSAGE_PATH+"Refused");
							File[] fileArray = tempFile.listFiles();
							//check if data already exists
							String suggestionName = MESSAGE_PATH+"Refused/suggestion"+testIndex;
							boolean check = false;
							while(check == false){
								check = true;
								suggestionName = MESSAGE_PATH+"Refused/suggestion"+testIndex;
								for(int index = 0; index < fileArray.length; index++){
									if(fileArray.length > 0){
										if(fileArray[index].toString().equals(suggestionName)){
											check = false;
										}
									}	
								}
								if(check == false){
									testIndex++;
								}
							}	
							FileWriter fw = new FileWriter(MESSAGE_PATH+"Refused/suggestion"+testIndex);
							BufferedWriter bw = new BufferedWriter(fw);
							bw.append(getCurrentElement(refuseButtonIds.indexOf(temp)));
							bw.close();
						}
						catch (Exception e){
							e.printStackTrace();
							System.err.println("Something went wrong: "+e);
						}
						
						for(PopupAdminPageListener listener : listeners){
							listener.refreshWindow();
						}
					}
				}
				if (acceptButtons.size()==1){
						currentFile.delete();
						close();
				}
				//would have been important when there would still be an Accept All button
				else{
				}
			}
		});
		/*if (acceptButtons.size()==1){
			if(!category.equals("Unedit")){
				currentFile.delete();
			}
		}
		else{
		}*/
		return newButton;
	}
	/**
	 * button that allows the admin to restore accepted suggestion
	 * @param buttonName button caption
	 * @return button with described functions
	 */
	public Button createRestoreButton(String buttonName){
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				try{
					File tempFile = new File (MESSAGE_PATH+"Accepted");
					File[] fileArray = tempFile.listFiles();
					//check if data already exists
					String suggestionName = MESSAGE_PATH+"Accepted/suggestion"+testIndex;
					boolean check = false;
					while(check == false){
						check = true;
						suggestionName = MESSAGE_PATH+"Accepted/suggestion"+testIndex;
						for(int index = 0; index < fileArray.length; index++){
							if(fileArray.length > 0){
								if(fileArray[index].toString().equals(suggestionName)){
									check = false;
								}
							}	
						}
						if(check == false){
							testIndex++;
						}
					}	
					/*
					 * second part writes it down in a file in the Accepted folder
					 */
					FileWriter fw = new FileWriter(MESSAGE_PATH+"Accepted/suggestion"+testIndex);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.append(getCurrentElementInverted(0));
					bw.close();
					
					currentFile.delete();
					close();
				}
				catch (Exception e){
					e.printStackTrace();
					System.err.println("Something went wrong: "+e);
				}
				//ask to refresh admin page window
				for(PopupAdminPageListener listener : listeners){
					listener.refreshWindow();
				}
				
				for(PopupAdminPageListener listener : listeners){
					listener.updateDB(subjectNew.get(0), propertyNew.get(0), valueOld.get(0), valueNew.get(0));
				}
			}
		});
		return newButton;
	}
	/**
	 * all needed information of the current element
	 * @param index position of the element in the corresponding list 
	 * @return String with all needed information in a specific machine-readable file format 
	 */
	public String getCurrentElement(int index){
		String fullTextNewElement = "old:";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "---";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "subject:";
		fullTextNewElement += subjectOld.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "property:";
		fullTextNewElement += propertyOld.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "value:";
		fullTextNewElement += valueOld.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "---";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "new:";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "---";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "subject:";
		fullTextNewElement += subjectNew.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "property:";
		fullTextNewElement += propertyNew.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "value:";
		fullTextNewElement += valueNew.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "---";
		return fullTextNewElement;
	}
	/**
	 * all needed information of the current element
	 * @param index position of the element in the corresponding list 
	 * @return String with all needed information in a specific machine-readable file format 
	 */
	public String getCurrentElementInverted(int index){
		String fullTextNewElement = "old:";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "---";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "subject:";
		fullTextNewElement += subjectOld.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "property:";
		fullTextNewElement += propertyOld.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "value:";
		fullTextNewElement += valueNew.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "---";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "new:";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "---";
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "subject:";
		fullTextNewElement += subjectNew.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "property:";
		fullTextNewElement += propertyNew.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "value:";
		fullTextNewElement += valueOld.get(index);
		fullTextNewElement += System.lineSeparator();
		fullTextNewElement += "---";
		return fullTextNewElement;
	}
	/**
	 * getter 
	 * @return globalIndex
	 */
	public int getGlobalIndex(){
		return globalIndex;
	}
	/**
	 * getter
	 * @return category
	 */
	public String getCategory(){
		return category;
	}
}
