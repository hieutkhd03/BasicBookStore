package vnua.fita.bookstore.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import vnua.fita.bookstore.bean.User;

public class UserDAO {
	private String jdbcURL;
	private String jdbcUsername;
	private String jdbcPassword;
	private Connection jdbcConnection;
	private PreparedStatement preStatement;
	private ResultSet resultSet;
	
	//Constructor
	public UserDAO(String jdbcURL, String jdbcUsername, String jdbcPassword) {
		this.jdbcURL = jdbcURL;
		this.jdbcUsername = jdbcUsername;
		this.jdbcPassword = jdbcPassword;
	}
	
	public User findUser(String username, String password) {
		//sql
		String sql = "SELECT * FROM tbluser WHERE username = ? AND password = ?";
		
		//tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername,jdbcPassword);

		try {
			//tạo đối tượng truy vấn database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setString(1, username);
			preStatement.setString(2, password);
			
			//thực hiện truy vấn database
			resultSet = preStatement.executeQuery();
			if (resultSet.next()) {
				return new User(resultSet.getString("username"),resultSet.getString("password"), resultSet.getInt("role"),resultSet.getString("fullname"), resultSet.getString("email"),resultSet.getString("mobile"), resultSet.getString("address"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public User findUser(String username) {
		//sql
		String sql = "SELECT * FROM tbluser WHERE username = ?";
		
		//tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername,jdbcPassword);
		
		try {
			//tạo đối tượng truy vấn database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setString(1, username);
			
			//thực hiện truy vấn database
			resultSet = preStatement.executeQuery();
			if (resultSet.next()) {
				return new User(resultSet.getString("username"),resultSet.getString("password"), resultSet.getInt("role"),resultSet.getString("fullname"), resultSet.getString("email"),resultSet.getString("mobile"), resultSet.getString("address"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
