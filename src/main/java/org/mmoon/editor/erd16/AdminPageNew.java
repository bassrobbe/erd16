package org.mmoon.editor.erd16;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup.CommitEvent;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.FieldGroup.CommitHandler;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.renderers.ImageRenderer;

@SuppressWarnings("serial")
public class AdminPageNew extends CustomComponent
		implements AdminPageNewInterface, RendererClickListener, ClickListener, CommitHandler, ItemClickListener {
	/**
	 * main layout
	 */
	VerticalLayout layout;
	/**
	 * number of new suggestions
	 */
	int newSuggestionsCount;
	/**
	 * number of accepted suggestions
	 */
	int acceptedSuggestionsCount;
	/**
	 * number of refused suggestion
	 */
	int refusedSuggestionsCount;
	/**
	 * number of unedited suggestions
	 */
	int uneditSuggestionCount;
	/**
	 * number of read mistakes
	 */
	int readMistakesCount;
	/**
	 * number of unread mistakes
	 */
	int unreadMistakesCount;
	/**
	 * button which shows all news
	 */
	Button showAllNewsButton;
	/**
	 * button which shows all old reported mistakes
	 */
	Button oldReportedMistakesButton;
	/**
	 * button which shows all old suggestions
	 */
	Button acceptedSuggestionsButton;
	/**
	 * button which shows all refused suggestions
	 */
	Button refusedSuggestionsButton;
	/**
	 * button which shows all unedited suggestions
	 */
	Button uneditSuggestionsButton;
	/**
	 * label with information about new messages
	 */
	Label information;
	/**
	 * list of listeners
	 */
	List<AdminPageNewListener> listeners = new ArrayList<AdminPageNewListener>();
	/**
	 * grid table with all new user suggestions as its elements
	 */
	Grid newSuggestion;
	/**
	 * grid table with all unread reported mistakes as its elements
	 */
	Grid newReportedMistakes;
	/**
	 * grid table with all read reported mistakes as its elements
	 */
	Grid oldMistakes;
	/**
	 * grid table with all accepted suggestions as its elements
	 */
	Grid acceptedSuggestions;
	/**
	 * grid table with all refused suggestions as its elements
	 */
	Grid refusedSuggestions;
	/**
	 * grid table with all unedited suggestions as its elements
	 */
	Grid uneditSuggestions;
	/**
	 * container source of the new suggestions grid
	 */
	IndexedContainer newSuggestionFiles;
	/**
	 * container source of the unread mistakes grid
	 */
	IndexedContainer newReportedMistakesFiles;
	/**
	 * 
	 */
	IndexedContainer oldMistakesFiles;
	/**
	 * container source of the accepted suggestions grid
	 */
	IndexedContainer acceptedSuggestionFiles;
	/**
	 * container source of the refused suggestions grid
	 */
	IndexedContainer refusedSuggestionFiles;
	/**
	 * container source of the unedited suggestions grid
	 */
	IndexedContainer uneditSuggestionFiles;

	public static final String MESSAGE_PATH = Configuration.messages_path;

	/**
	 * creates an admin page with various operations for the admin to choose
	 * from
	 */
	public AdminPageNew() {
		// init page with function
		initAdminPage();

	}

	/**
	 * adds an listener to the listener list
	 * 
	 * @param listener
	 *            the listener to be added
	 */
	public void addAdminPageNewListener(AdminPageNewListener listener) {
		listeners.add(listener);
	}

	/**
	 * inits the admin page with all its elements. also basis for the
	 * "back to overwiev" button
	 */
	public void initAdminPage() {
		// init counters
		newSuggestionsCount = getNumbersOfFiles("New");
		acceptedSuggestionsCount = getNumbersOfFiles("Accepted");
		refusedSuggestionsCount = getNumbersOfFiles("Refused");
		uneditSuggestionCount = getNumbersOfFiles("Unedit");
		readMistakesCount = getNumbersOfFiles("Mistake/Read");
		unreadMistakesCount = getNumbersOfFiles("Mistake/Unread");

		// init information Label
		information = new Label("You have got " + (newSuggestionsCount + unreadMistakesCount) + " new Message(s).");

		// init layout
		layout = new VerticalLayout();
		setCompositionRoot(layout);

		// init button
		showAllNewsButton = createShowAllNewsButton("Show New Messages");
		showAllNewsButton.setDescription("Show all new reported mistakes and suggestions.");
		oldReportedMistakesButton = createOldMistakesButton("Show Read Reported Mistakes");
		oldReportedMistakesButton.setDescription("Show all read mistake report massages");
		acceptedSuggestionsButton = createAcceptedSuggestionsButton("Show Accepted Suggestions");
		acceptedSuggestionsButton.setDescription("Show all accepted suggestions");
		refusedSuggestionsButton = createRefusedSuggestionsButton("Show Refused Suggestions");
		refusedSuggestionsButton.setDescription("Show all refused suggestions");
		uneditSuggestionsButton = createUneditSuggestionsButton("Show Pending Suggestions");
		uneditSuggestionsButton.setDescription("Show all pending suggestions");
		Button refreshButton = createRefreshButton("");

		// layout stuff
		layout.setSpacing(true);

		HorizontalLayout subContent = new HorizontalLayout();
		subContent.addComponent(information);
		subContent.addComponent(refreshButton);
		subContent.setSpacing(true);
		layout.addComponent(subContent);

		layout.addComponent(showAllNewsButton);
		HorizontalLayout sublayout = new HorizontalLayout();
		sublayout.setSpacing(true);
		sublayout.addComponent(oldReportedMistakesButton);
		sublayout.addComponent(acceptedSuggestionsButton);
		sublayout.addComponent(refusedSuggestionsButton);
		sublayout.addComponent(uneditSuggestionsButton);
		layout.addComponent(sublayout);
	}

	/**
	 * calculates numbers of files in a specific folder
	 * 
	 * @param folder
	 *            name of the folder which files are needed to be counted
	 * @return number of files a the folder
	 */
	public int getNumbersOfFiles(String folder) {
		File tempFile = new File(MESSAGE_PATH + folder);
		int numbersOfFiles = tempFile.listFiles().length;
		return numbersOfFiles;
	}

	/**
	 * creates a refresh button
	 * 
	 * @param buttonName
	 *            caption of the button
	 * @return completed button
	 */
	public Button createRefreshButton(String buttonName) {
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				initAdminPage();
			}
		});
		newButton.setIcon(new ThemeResource("refresh.png"));
		return newButton;
	}

	/**
	 * creates a accepted suggestions button
	 * 
	 * @param buttonName
	 *            caption of the button
	 * @return completed button
	 */
	public Button createAcceptedSuggestionsButton(String buttonName) {
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				acceptedSuggestionButtonFunction();
			}
		});
		return newButton;
	}

	/**
	 * creates a refused suggestions button
	 * 
	 * @param buttonName
	 *            caption of the button
	 * @return completed button
	 */
	public Button createRefusedSuggestionsButton(String buttonName) {
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				refusedSuggestionButtonFunction();
			}
		});
		return newButton;
	}

	/**
	 * creates a unedited suggestion button
	 * 
	 * @param buttonName
	 *            caption of the button
	 * @return completed button
	 */
	public Button createUneditSuggestionsButton(String buttonName) {
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				uneditSuggestionButtonFunction();
			}
		});
		return newButton;
	}

	/**
	 * declares functionality of the refused button
	 */
	public void refusedSuggestionButtonFunction() {
		layout.removeAllComponents();

		refusedSuggestions = new Grid();
		refusedSuggestions.setCaption("Refused Suggestions");
		refusedSuggestionFiles = new IndexedContainer();
		refusedSuggestionFiles.addContainerProperty("Subject", String.class, "");
		refusedSuggestionFiles.addContainerProperty("Show    ", Resource.class, new ThemeResource("search.png"));
		updateGrid("Refused Suggestions");
		refusedSuggestions.setContainerDataSource(refusedSuggestionFiles);
		refusedSuggestions.getColumn("Subject").setEditable(false);
		refusedSuggestions.getColumn("Show    ").setRenderer(new ImageRenderer(this));
		refusedSuggestions.addItemClickListener(this);
		refusedSuggestions.getEditorFieldGroup().addCommitHandler(this);
		refusedSuggestions.getColumn("Subject").setSortable(false);

		layout.addComponent(refusedSuggestions);

		// create back button
		Button backButton = createBackToOverviewButton("Back to Overview");
		backButton.setIcon(new ThemeResource("arrow-backward.png"));

		// add back button
		layout.addComponent(backButton);
	}

	/**
	 * declares functionality of the unedited button
	 */
	public void uneditSuggestionButtonFunction() {
		layout.removeAllComponents();

		uneditSuggestions = new Grid();
		uneditSuggestions.setCaption("Unedit Suggestions");
		uneditSuggestionFiles = new IndexedContainer();
		uneditSuggestionFiles.addContainerProperty("Subject", String.class, "");
		uneditSuggestionFiles.addContainerProperty("Show     ", Resource.class, new ThemeResource("search.png"));
		updateGrid("Unedit Suggestions");
		uneditSuggestions.setContainerDataSource(uneditSuggestionFiles);
		uneditSuggestions.getColumn("Subject").setEditable(false);
		uneditSuggestions.getColumn("Show     ").setRenderer(new ImageRenderer(this));
		uneditSuggestions.addItemClickListener(this);
		uneditSuggestions.getEditorFieldGroup().addCommitHandler(this);
		uneditSuggestions.getColumn("Subject").setSortable(false);

		layout.addComponent(uneditSuggestions);

		// create back button
		Button backButton = createBackToOverviewButton("Back to Overview");
		backButton.setIcon(new ThemeResource("arrow-backward.png"));

		// add back button
		layout.addComponent(backButton);
	}

	/**
	 * declares functionality of the accepted button
	 */
	public void acceptedSuggestionButtonFunction() {
		layout.removeAllComponents();

		acceptedSuggestions = new Grid();
		acceptedSuggestionFiles = new IndexedContainer();
		acceptedSuggestionFiles.addContainerProperty("Subject", String.class, "");
		acceptedSuggestionFiles.addContainerProperty("Show   ", Resource.class, new ThemeResource("search.png"));
		updateGrid("Accepted Suggestions");
		acceptedSuggestions.setContainerDataSource(acceptedSuggestionFiles);
		acceptedSuggestions.getColumn("Subject").setEditable(false);
		acceptedSuggestions.getColumn("Show   ").setRenderer(new ImageRenderer(this));
		acceptedSuggestions.addItemClickListener(this);
		acceptedSuggestions.getEditorFieldGroup().addCommitHandler(this);
		acceptedSuggestions.getColumn("Subject").setSortable(false);

		layout.addComponent(acceptedSuggestions);

		// create back button
		Button backButton = createBackToOverviewButton("Back to Overview");
		backButton.setIcon(new ThemeResource("arrow-backward.png"));

		// add back button
		layout.addComponent(backButton);
	}

	/**
	 * creates the old mistakes button
	 * 
	 * @param buttonName
	 *            caption of the button
	 * @return completed button
	 */
	public Button createOldMistakesButton(String buttonName) {
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				oldMistakesButtonFunction();
			}
		});
		return newButton;
	}

	/**
	 * declares functionality of the old mistakes button
	 */
	public void oldMistakesButtonFunction() {
		layout.removeAllComponents();

		oldMistakes = new Grid();
		oldMistakes.setCaption("Read Mistakes");
		oldMistakesFiles = new IndexedContainer();
		oldMistakesFiles.addContainerProperty("Subject", String.class, "");
		oldMistakesFiles.addContainerProperty("Show  ", Resource.class, new ThemeResource("search.png"));
		updateGrid("Read");
		oldMistakes.setContainerDataSource(oldMistakesFiles);
		oldMistakes.getColumn("Subject").setEditable(false);
		oldMistakes.getColumn("Show  ").setRenderer(new ImageRenderer(this));
		oldMistakes.addItemClickListener(this);
		oldMistakes.getEditorFieldGroup().addCommitHandler(this);
		oldMistakes.getColumn("Subject").setSortable(false);

		layout.addComponent(oldMistakes);

		// create back button
		Button backButton = createBackToOverviewButton("Back to Overview");

		backButton.setIcon(new ThemeResource("arrow-backward.png"));

		// add back button
		layout.addComponent(backButton);
	}

	/**
	 * creates a button which shows new mistakes + new suggestions
	 * 
	 * @param buttonName
	 *            button caption
	 * @return completed button
	 */
	public Button createShowAllNewsButton(String buttonName) {
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				showAllNewsButtonFunction();
			}
		});
		return newButton;
	}

	/**
	 * declares functionality of the show all news button
	 */
	public void showAllNewsButtonFunction() {
		layout.removeAllComponents();
		// order elements
		HorizontalLayout subContent = new HorizontalLayout();
		// create Grids
		newSuggestion = new Grid();
		newSuggestion.setCaption("New Suggestions");
		// init files
		newSuggestionFiles = new IndexedContainer();
		newSuggestionFiles.addContainerProperty("Subject", String.class, "");

		newSuggestionFiles.addContainerProperty("Show", Resource.class, new ThemeResource("search.png"));
		updateGrid("Suggestions");
		newSuggestion.setContainerDataSource(newSuggestionFiles);
		newSuggestion.getColumn("Subject").setEditable(false);
		newSuggestion.getColumn("Show").setRenderer(new ImageRenderer(this));
		newSuggestion.addItemClickListener(this);
		newSuggestion.getEditorFieldGroup().addCommitHandler(this);
		newSuggestion.getColumn("Subject").setSortable(false);

		newReportedMistakes = new Grid();
		newReportedMistakes.setCaption("New Reported Mistakes");
		newReportedMistakesFiles = new IndexedContainer();
		newReportedMistakesFiles.addContainerProperty("Subject", String.class, "");

		newReportedMistakesFiles.addContainerProperty("Show ", Resource.class, new ThemeResource("search.png"));
		updateGrid("Mistakes");
		newReportedMistakes.setContainerDataSource(newReportedMistakesFiles);
		newReportedMistakes.getColumn("Subject").setEditable(false);
		newReportedMistakes.getColumn("Show ").setRenderer(new ImageRenderer(this));
		newReportedMistakes.addItemClickListener(this);
		newReportedMistakes.getEditorFieldGroup().addCommitHandler(this);
		newReportedMistakes.getColumn("Subject").setSortable(false);

		// add Grids
		subContent.addComponent(newSuggestion);
		subContent.addComponent(newReportedMistakes);
		subContent.setSpacing(true);

		// add subContent to main layout
		layout.addComponent(subContent);

		// create back button
		Button backButton = createBackToOverviewButton("Back to Overview");

		backButton.setIcon(new ThemeResource("arrow-backward.png"));

		// add back button
		layout.addComponent(backButton);
	}

	/**
	 * updates the file counters
	 */
	public void updateCounters() {
		// update counters
		newSuggestionsCount = getNumbersOfFiles("New");
		acceptedSuggestionsCount = getNumbersOfFiles("Accepted");
		refusedSuggestionsCount = getNumbersOfFiles("Refused");
		uneditSuggestionCount = getNumbersOfFiles("Unedit");
		readMistakesCount = getNumbersOfFiles("Mistake/Read");
		unreadMistakesCount = getNumbersOfFiles("Mistake/Unread");
	}

	/**
	 * updates the container sources of the grids
	 * 
	 * @param container
	 *            the container that needs to be updated
	 */
	@SuppressWarnings("unchecked")
	public void updateGrid(String container) {
		updateCounters();
		// update suggestion container
		if (container.equals("Suggestions")) {
			newSuggestionFiles.removeAllItems();
			File tempFile = new File(MESSAGE_PATH + "New");
			File[] allTempFiles = tempFile.listFiles();
			for (int i = 0; i < newSuggestionsCount; i++) {
				Item newItem = newSuggestionFiles.getItem(newSuggestionFiles.addItem());
				newItem.getItemProperty("Subject").setValue(fileToSubjectName(allTempFiles[i]));
			}
		} else if (container.equals("Mistakes")) {
			updateCounters();
			newReportedMistakesFiles.removeAllItems();
			File tempFile = new File(MESSAGE_PATH + "Mistake/Unread");
			File[] allTempFiles = tempFile.listFiles();
			for (int i = 0; i < unreadMistakesCount; i++) {
				Item newItem = newReportedMistakesFiles.getItem(newReportedMistakesFiles.addItem());
				newItem.getItemProperty("Subject").setValue(fileToSubjectName(allTempFiles[i]));
			}
		} else if (container.equals("Read")) {
			updateCounters();
			oldMistakesFiles.removeAllItems();
			File tempFile = new File(MESSAGE_PATH + "Mistake/Read");
			File[] allTempFiles = tempFile.listFiles();
			for (int i = 0; i < readMistakesCount; i++) {
				Item newItem = oldMistakesFiles.getItem(oldMistakesFiles.addItem());
				newItem.getItemProperty("Subject").setValue(fileToSubjectName(allTempFiles[i]));
			}
		} else if (container.equals("Accepted Suggestions")) {
			acceptedSuggestionFiles.removeAllItems();
			File tempFile = new File(MESSAGE_PATH + "Accepted");
			File[] allTempFiles = tempFile.listFiles();
			for (int i = 0; i < acceptedSuggestionsCount; i++) {
				Item newItem = acceptedSuggestionFiles.getItem(acceptedSuggestionFiles.addItem());
				newItem.getItemProperty("Subject").setValue(fileToSubjectName(allTempFiles[i]));
			}
		} else if (container.equals("Unedit Suggestions")) {
			uneditSuggestionFiles.removeAllItems();
			File tempFile = new File(MESSAGE_PATH + "Unedit");
			File[] allTempFiles = tempFile.listFiles();
			for (int i = 0; i < uneditSuggestionCount; i++) {
				Item newItem = uneditSuggestionFiles.getItem(uneditSuggestionFiles.addItem());
				newItem.getItemProperty("Subject").setValue(fileToSubjectName(allTempFiles[i]));
			}
		} else if (container.equals("Refused Suggestions")) {
			refusedSuggestionFiles.removeAllItems();
			File tempFile = new File(MESSAGE_PATH + "Refused");
			File[] allTempFiles = tempFile.listFiles();
			for (int i = 0; i < refusedSuggestionsCount; i++) {
				Item newItem = refusedSuggestionFiles.getItem(refusedSuggestionFiles.addItem());
				newItem.getItemProperty("Subject").setValue(fileToSubjectName(allTempFiles[i]));
			}
		}
	}

	/**
	 * creates the back to overview button
	 * 
	 * @param buttonName
	 *            button caption
	 * @return completed button
	 */
	public Button createBackToOverviewButton(String buttonName) {
		Button newButton = new Button(buttonName);
		newButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				initAdminPage();
			}
		});
		return newButton;
	}

	/**
	 * filter the subject name of an report file
	 * 
	 * @param file
	 *            regarded file
	 * @return subject name
	 */
	public String fileToSubjectName(File file) {
		try {
			// standard procedure to read from a file
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			StringBuffer stringBuffer = new StringBuffer();
			// read the whole file and append it
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
			}
			// close the reader
			bufferedReader.close();
			// save it by call the toString from the StringBuffer class
			String fullText = stringBuffer.toString();
			// split it to get the subjects name
			String[] firstSplit = fullText.split("subject:");
			String[] secondSplit = firstSplit[1].split("property");
			String subjectName = secondSplit[0];

			return subjectName;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Something went wrong: " + e);
			return "Something went wrong";
		}
	}

	/**
	 * informs the presenter which entry was chosen
	 */
	@Override
	public void click(RendererClickEvent event) {
		if (event.getPropertyId().toString().equals("Show")) {
			String subject = newSuggestionFiles.getItem(event.getItemId()).getItemProperty("Subject").getValue()
					.toString();
			int position = newSuggestionFiles.indexOfId(event.getItemId());
			File file = (new File(MESSAGE_PATH + "New")).listFiles()[position];
			for (AdminPageNewListener listener : listeners) {
				listener.selectItem("New", subject, file);
			}
		}

		if (event.getPropertyId().toString().equals("Show ")) {
			String subject = newReportedMistakesFiles.getItem(event.getItemId()).getItemProperty("Subject").getValue()
					.toString();
			int position = newReportedMistakesFiles.indexOfId(event.getItemId());
			File file = (new File(MESSAGE_PATH + "Mistake/Unread")).listFiles()[position];
			for (AdminPageNewListener listener : listeners) {
				listener.selectItem("Unread", subject, file);
			}
		}

		if (event.getPropertyId().toString().equals("Show  ")) {
			String subject = oldMistakesFiles.getItem(event.getItemId()).getItemProperty("Subject").getValue()
					.toString();
			int position = oldMistakesFiles.indexOfId(event.getItemId());
			File file = (new File(MESSAGE_PATH + "Mistake/Read")).listFiles()[position];
			for (AdminPageNewListener listener : listeners) {
				listener.selectItem("Read", subject, file);
			}
		}
		if (event.getPropertyId().toString().equals("Show   ")) {
			String subject = acceptedSuggestionFiles.getItem(event.getItemId()).getItemProperty("Subject").getValue()
					.toString();
			int position = acceptedSuggestionFiles.indexOfId(event.getItemId());
			File file = (new File(MESSAGE_PATH + "Accepted")).listFiles()[position];
			for (AdminPageNewListener listener : listeners) {
				listener.selectItem("Accepted", subject, file);
			}
		}
		if (event.getPropertyId().toString().equals("Show     ")) {
			String subject = uneditSuggestionFiles.getItem(event.getItemId()).getItemProperty("Subject").getValue()
					.toString();
			int position = uneditSuggestionFiles.indexOfId(event.getItemId());
			File file = (new File(MESSAGE_PATH + "Unedit")).listFiles()[position];
			for (AdminPageNewListener listener : listeners) {
				listener.selectItem("Unedit", subject, file);
			}
		}
		if (event.getPropertyId().toString().equals("Show    ")) {
			String subject = refusedSuggestionFiles.getItem(event.getItemId()).getItemProperty("Subject").getValue()
					.toString();
			int position = refusedSuggestionFiles.indexOfId(event.getItemId());
			File file = (new File(MESSAGE_PATH + "Refused")).listFiles()[position];
			for (AdminPageNewListener listener : listeners) {
				listener.selectItem("Refused", subject, file);
			}
		}

	}

	@Override
	public void itemClick(ItemClickEvent event) {
		// not needed
	}

	@Override
	public void preCommit(CommitEvent commitEvent) throws CommitException {
		// not needed
	}

	@Override
	public void postCommit(CommitEvent commitEvent) throws CommitException {
		// not needed
	}

	@Override
	public void buttonClick(ClickEvent event) {
		// not needed
	}

}
