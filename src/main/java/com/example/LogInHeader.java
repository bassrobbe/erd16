package com.example.vorprojekt;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import com.vaadin.server.Page;
import com.vaadin.server.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * A small form for submitting user name and password for login and to logout.
 * Updates the views based on authentication.
 */
@SuppressWarnings("serial")
public class LogInHeader extends CustomComponent implements LogInHeaderInterface, ClickListener {

	/**
	 * Field to enter the user name
	 */
	private TextField user = new TextField();

	/**
	 * Field to enter the password
	 */
	private PasswordField pass = new PasswordField();

	/**
	 * Submit user name and password
	 */
	private Button ok = new Button("LogIn");

	/**
	 * Navigate to admin page
	 */
	private Button toAdmin = new Button();

	private HorizontalLayout layout = new HorizontalLayout();

	private VerticalLayout wrapper = new VerticalLayout();

	/**
	 * Display user name if logged in
	 */
	private Label logInLabel = new Label();

	/**
	 * String representation of the current view
	 */
	private String page;

	/**
	 * Set up the default component layout and update depending on user role
	 *
	 * @param page
	 *            the current view either searchUI or adminUI
	 */
	public LogInHeader(String page) {
		this.page = page;

		ok.addClickListener(this);
		toAdmin.addClickListener(this);

		// create layout
		layout.addComponent(user);
		user.setCaption("user");
		layout.addComponent(pass);
		pass.setCaption("pass");
		layout.addComponent(logInLabel);
		layout.addComponent(toAdmin);
		layout.addComponent(ok);
		layout.setComponentAlignment(toAdmin, Alignment.BOTTOM_LEFT);
		layout.setComponentAlignment(ok, Alignment.BOTTOM_LEFT);
		layout.setComponentAlignment(logInLabel, Alignment.MIDDLE_LEFT);
		logInLabel.setVisible(false);
		// handle permission dependent layout
		updateLayout();
		layout.setSpacing(true);
		wrapper.addComponent(layout);
		wrapper.setComponentAlignment(layout, Alignment.MIDDLE_RIGHT);
		wrapper.setWidth("100%");
		setCompositionRoot(wrapper);

		user.setDescription("Enter your user name here.");
		pass.setDescription("Enter your password here.");
	}

	// Only the presenter registers one listener...
	List<LogInHeaderListener> listeners = new ArrayList<LogInHeaderListener>();

	@Override
	public void addLogInHeaderListener(LogInHeaderListener listener) {
		listeners.add(listener);
	}

	/**
	 * Handle button clicks.
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton().equals(ok)) {
			// Login button click
			if (ok.getCaption().compareTo("LogIn") == 0) {
				// if fields are empty mark fields
				if (user.getValue().compareTo("") == 0) {
					user.setComponentError(new UserError("Please enter a user name"));
				} else {
					user.setComponentError(null);
				}
				if (pass.getValue().compareTo("") == 0) {
					pass.setComponentError(new UserError("Please enter a password"));
				} else {
					pass.setComponentError(null);
				}
				// if fields are not empty, try to login
				if (!(user.getValue().compareTo("") == 0) && !(pass.getValue().compareTo("") == 0)) {
					// create token from submitted user name and password
					UsernamePasswordToken token = new UsernamePasswordToken(user.getValue(), pass.getValue());
					token.setRememberMe(true);
					Subject currentUser = SecurityUtils.getSubject();
					try {
						// authenticate current subject with token
						currentUser.login(token);
					} catch (UnknownAccountException uae) {
						Notification.show("Log In Failure", "user name and/or password incorrect",
								Notification.Type.WARNING_MESSAGE);
					} catch (IncorrectCredentialsException ice) {
						Notification.show("Log In Failure", "user name and/or password incorrect",
								Notification.Type.WARNING_MESSAGE);
					} catch (LockedAccountException lae) {
						Notification.show("Log In Failure", "your account has been locke, please contact administrator",
								Notification.Type.WARNING_MESSAGE);
					} catch (ExcessiveAttemptsException eae) {
						Notification.show("Log In Failure", "unknown error, please contact administrator",
								Notification.Type.WARNING_MESSAGE);
					}
					// update layout of header
					updateLayout();
					for (LogInHeaderListener listener : listeners) {
						// update layout of detail view
						listener.detailUpdateColumns();
						// update layout of overview
						listener.changeInsertFunctionPermission();
					}
				}
				return;
			}
			// Logout button click
			if (ok.getCaption().compareTo("LogOut") == 0) {
				Page.getCurrent().open("/erd16-0.1/logout", "");
				return;
			}
		}
		if (event.getButton().equals(toAdmin)) {
			// Admin page button click
			if (toAdmin.getCaption().compareTo("Admin Page") == 0) {
				if (SecurityUtils.getSubject().isAuthenticated()) {
					if (SecurityUtils.getSubject().hasRole("admin")) {
						// pass current SearchUI state in URI fragment
						this.getUI().getPage().open("/erd16-0.1/admin#" + Page.getCurrent().getUriFragment(), "");
					} else {
						Notification.show("Access denied", "Log in as administrator to perform this action",
								Notification.Type.WARNING_MESSAGE);
					}
				} else {
					Notification.show("Access denied", "Log in as administrator to perform this action",
							Notification.Type.WARNING_MESSAGE);
				}
			}
			// Search Page button click
			if (toAdmin.getCaption().compareTo("Search Page") == 0) {
				// go back to last SearchUI state encoded in URI fragment
				this.getUI().getPage().open("/erd16-0.1/#" + Page.getCurrent().getUriFragment(), "");
			}
		}
	}

	/**
	 * Update the layout of the header depending on user role
	 */
	public void updateLayout() {
		if (SecurityUtils.getSubject().isAuthenticated()) {
			user.setVisible(false);
			pass.setVisible(false);
			logInLabel.setVisible(true);
			logInLabel.setValue("logged in as: " + SecurityUtils.getSubject().getPrincipal().toString());
			ok.setCaption("LogOut");
			ok.setDescription("Logout of the current session.");

		} else {
			user.setVisible(true);
			pass.setVisible(true);
			logInLabel.setVisible(false);
			ok.setCaption("LogIn");
			ok.setDescription("Submit username and password for authentification.");
		}
		if (page.compareTo("searchUI") == 0) {
			toAdmin.setCaption("Admin Page");
			toAdmin.setDescription("Navigate to the administration page. "
					+ "(You must be logged in as an administrator to perform " + "this action.)");
		}
		if (page.compareTo("adminUI") == 0) {
			toAdmin.setCaption("Search Page");
			toAdmin.setDescription("Navigate to the search page.");
		}
	}

}
