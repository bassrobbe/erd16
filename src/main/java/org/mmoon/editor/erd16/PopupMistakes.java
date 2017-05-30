package org.mmoon.editor.erd16;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

//import org.mmoon.editor.erd16.PopupMistakesInterface.PopupMistakeListener;
import com.google.gwt.thirdparty.guava.common.io.Files;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class PopupMistakes extends Window implements PopupMistakesInterface{
	/**
	 * main content
	 */
	VerticalLayout content;
	/**
	 * current item
	 */
	File item;
	/**
	 * current category
	 */
	String category;
	/**
	 * subject name of current item
	 */
	String subject;
	/**
	 * flag needed for moving system between Unread and Read mistakes
	 */
	boolean moveFlag;
	/**
	 * index needed for correct file names
	 */
	int filenameIndex;
	public static final String MESSAGE_PATH = Configuration.message_path;
	/**
	 * creates a popup window which shows the reported mistake
	 * @param category current category
	 * @param item current item
	 */
	//TODO: add option to move to the marked entry
	public PopupMistakes(String category, File item){
		//window caption
		super("Mistake");
		//basic stuff
		center();
		content = new VerticalLayout();
		content.setMargin(true);
		setContent(content);
		//init 
		moveFlag = true;
		/*
		 * moves opened mistake from Unread to Read folder
		 */
		addCloseListener(new Window.CloseListener(){
			@Override
			public void windowClose(CloseEvent e){
				if(moveFlag){
					if(getCategory().equals("Unread")){
						try{
							File destination = new File(createFileName());
							Files.move(getCurrentItem(), destination);
							for (PopupMistakeListener listener : listeners){
								listener.refreshWindow();
							}
						}
						catch(Exception exception){
							exception.printStackTrace();
							System.err.println("Something went wrong: "+exception);
						}
					}
				}

			}
		});
		//set variables
		this.item = item;
		this.category = category;
		filenameIndex = 0;
		//create delete button
		Button deleteButton = createDeleteButton("Delete this Notification");
		//create info label
		Label triple = new Label(readContent());
		//add parts to content
		content.addComponent(triple);
		//place holder
		content.addComponent(new Label(""));
		content.addComponent(deleteButton);
	}
	/**
	 * creates a button which allows the admin to delete a reportet mistake
	 * @param buttonName button caption
	 * @return button with described functions
	 */
	private Button createDeleteButton(String buttonName){
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener(){
			@Override 
			public void buttonClick(Button.ClickEvent event){
				item.delete();
				moveFlag = false;
				close();
				for (PopupMistakeListener listener : listeners){
					listener.refreshWindow();
				}
			}
		});
		return newButton;
	}
	/**
	 * reads content of a mistake file and returns it as a String
	 * @return formatted String
	 */
	private String readContent(){
		String content = "";
		//standard procedure to read from a  file
		try{
			FileReader fileReader = new FileReader(item);
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
			//1st step:
			String [] entries = fullText.split("---");
			String [] temp1Old = entries[1].split("subject:");
			String [] temp2Old = temp1Old[1].split("property:");
			String [] temp3Old = temp2Old[1].split("value:");
			String subjectOld = temp2Old[0];
			subject = subjectOld;
			String propertyOld = temp3Old[0];
			String valueOld = temp3Old[1];
			content += /*"Subject: "+*/subjectOld;
			content += "\t";
			content += /*"Property: "+*/propertyOld;
			content += "\t";
			content += /*"Value: "+*/valueOld;
		}
		catch(Exception e){
			e.printStackTrace();
			System.err.println("Something went wrong: "+e);
		}
		return content;
	}
	/**
	 * PopupMistakeListner list
	 */
	ArrayList <PopupMistakeListener> listeners = new ArrayList <PopupMistakeListener>();
	
	/**
	 * adds a PopupMistakeListener
	 * @param listener listener to be added
	 */
	@Override
	public void addPopupMistakeListener(PopupMistakeListener listener){
		listeners.add(listener);
	}
	/**
	 * creates a correct file name
	 * @return filename
	 */
	public String createFileName(){
		String filename = "";
		
		try{
			File tempFile = new File (MESSAGE_PATH+"Mistake/Read");
			File[] fileArray = tempFile.listFiles();
			//check if data already exists
			filename += MESSAGE_PATH+"Mistake/Read/mistake"+filenameIndex;
			boolean check = false;
			while(check == false){
				check = true;
				filename = MESSAGE_PATH+"Mistake/Read/mistake"+filenameIndex;
				for(int index = 0; index < fileArray.length; index++){
					if(fileArray.length > 0){
						if(fileArray[index].toString().equals(filename)){
							check = false;
						}
					}	
				}
				if(check == false){
					filenameIndex++;
				}
			}
		}
		catch(Exception exception){
			exception.printStackTrace();
			System.err.println("Something went wrong: "+exception);
		}
		
		return filename;
	}
	/**
	 * getter
	 * @return category
	 */
	public String getCategory(){
		return category;
	}
	/**
	 * getter
	 * @return item
	 */
	public File getCurrentItem(){
		return item;
	}

}
