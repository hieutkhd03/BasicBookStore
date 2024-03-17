package vnua.fita.bookstore.formbean;

import java.util.ArrayList;
import java.util.List;
import vnua.fita.bookstore.utils.Constant;

public class LoginForm {
	private String username;
	private String password;
	
	// Get/Set
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	//Constructor
	public LoginForm(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	
	//validate(Errors) 
	public List<String> validateLoginForm() {
		List<String> errors = new ArrayList<String>();
		if(username == null || username.trim().isEmpty()) {
			errors.add(Constant.USERNAME_EMPTY_VALIDATE_MSG);
		}
		if(password == null || password.trim().isEmpty()) {
			errors.add(Constant.PASSWORD_EMPTY_VALIDATE_MSG);
		}
		return errors;
	}	
}
