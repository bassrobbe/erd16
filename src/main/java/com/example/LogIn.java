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

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Login Page to authenticate subject for admin page access
 */
@SuppressWarnings("serial")
public class LogIn extends CustomComponent implements LogInInterface, 
													  ClickListener {

	/**
	 * Field to enter the user name
	 */
	private TextField user = new TextField("user name: ");

	/**
	 * Field to enter the password
	 */
	private PasswordField pass = new PasswordField("password: ");

	/**
	 * Submit user name and password
	 */
	private Button ok = new Button("LogIn");

	/**
	 * continue without logIn
	 */
	private Link guest = new Link("continue as guest", new ExternalResource("/erd16Bitbucket/"));

	private VerticalLayout content = new VerticalLayout();

	private FormLayout form = new FormLayout();

	private Panel panel = new Panel();

	/**
	 * collect user name and password and redirect to admin page on succesful
	 * authentication
	 */
	public LogIn() {
		// add listeners to components
		ok.addClickListener(this);
		// style components
		guest.setStyleName("link");
		panel.setHeight("300px");
		panel.setWidth("300px");
		// layout the components
		form.addComponent(user);
		form.addComponent(pass);
		form.addComponent(ok);
		form.addComponent(guest);
		panel.setContent(form);
		content.addComponent(panel);
		content.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
		content.setSizeFull();
		setCompositionRoot(content);
		this.setSizeFull();
	}

	// Only the presenter registers one listener...
	List<LogInListener> listeners = new ArrayList<LogInListener>();

	@Override
	public void addLogInListener(LogInListener listener) {
		listeners.add(listener);
	}

	/**
	 * Invokes user name and the password from the fields if LogIn button is
	 * clicked.
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		// 'LogIn' button clicked
		if (event.getButton().getCaption().compareTo(ok.getCaption()) == 0) {
			// create toke from submitted user name and password
			UsernamePasswordToken token = new UsernamePasswordToken(user.getValue(), pass.getValue());
			token.setRememberMe(true);
			Subject currentUser = SecurityUtils.getSubject();
			try {
				// authenticate current subject with token
				currentUser.login(token);
				Page.getCurrent().open("/erd16Bitbucket/admin", "");
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
		}
	}
}
