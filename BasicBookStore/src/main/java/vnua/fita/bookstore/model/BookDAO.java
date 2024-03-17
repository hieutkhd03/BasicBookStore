package vnua.fita.bookstore.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import vnua.fita.bookstore.bean.Book;
import vnua.fita.bookstore.utils.Constant;
import vnua.fita.bookstore.utils.MyUtils;

public class BookDAO {
	private String jdbcURL;
	private String jdbcUsername;
	private String jdbcPassword;
	private Connection jdbcConnection;
	private Statement statement;
	private PreparedStatement preStatement;
	private ResultSet resultSet;

	// Constructor
	public BookDAO(String jdbcURL, String jdbcUsername, String jdbcPassword) {
		super();
		this.jdbcURL = jdbcURL;
		this.jdbcUsername = jdbcUsername;
		this.jdbcPassword = jdbcPassword;
	}

	public List<Book> ListAllBooks() {
		// danh sách chứa kết quả trả về
		List<Book> listBook = new ArrayList<Book>();

		// sql
		String sql = "SELECT * FROM tblbook";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		try {
			// tạo đối tượng truy vấn Database
			statement = jdbcConnection.createStatement();

			// thực hiện truy vấn Database
			resultSet = statement.executeQuery(sql);

			// duyệt qua danh sách ghi kết quả trả về
			while (resultSet.next()) {
				int id = resultSet.getInt("book_id");
				String title = resultSet.getString("title");
				String author = resultSet.getString("author");
				int price = resultSet.getInt("price");
				int quantityInStock = resultSet.getInt("quantity_in_stock");
				String detail = resultSet.getString("detail");
				String imagePath = resultSet.getString("image_path");

				// đóng gói các giá trị thuộc tính vào đối tượng Bean(Book)
				Book book = new Book(id, title, author, price, quantityInStock);
				book.setDetail(detail);
				book.setImagePath(imagePath);

				// thêm đối tượng Bean vào danh sách
				listBook.add(book);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closeStatement(statement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return listBook;
	}

	// Lấy danh sách theo từ khóa tìm kiếm
	public List<Book> listAllBooks(String keyword) {
		// danh sách chứa kết quả trả về
		List<Book> searchBookList = new ArrayList<Book>();

		// sql
		String sql = "SELECT * FROM tblbook WHERE title LIKE ?";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		try {
			// tạo đối tượng truy vấn trong Database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setString(1, "%" + keyword + "%");

			// thực hiện truy vấn database
			resultSet = preStatement.executeQuery();

			// duyệt qua danh sách ghi kết quả trả về
			while (resultSet.next()) {
				int id = resultSet.getInt("book_id");
				String title = resultSet.getString("title");
				String author = resultSet.getString("author");
				int price = resultSet.getInt("price");

				// đóng gói các giá trị thuộc tính vào đối tượng Bean(Book)
				Book book = new Book(id, title, author, price);

				// thêm đối tượng Bean vào danh sách
				searchBookList.add(book);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return searchBookList;
	}

	// Lấy danh sách lọc theo ngày
	public List<Book> listAllBooks(String fromDate, String toDate) {
		// danh sách chứa kết quả trả về
		List<Book> listBooks = new ArrayList<Book>();

		// sql
		String sql = "SELECT b.*, sum(obor.quantity) AS sum_quantity, sum(obor.price*obor.quantity) AS sum_price FROM tblbook b "
				+ "LEFT JOIN " + "(SELECT ob.* from tblorder_book ob INNER JOIN tblorder o ON ob.order_id = o.order_id "
				+ "WHERE o.order_status = ? AND (o.status_date BETWEEN ? AND ?)) obor " + "ON b.book_id = obor.book_id "
				+ "GROUP BY b.book_id " + "ORDER BY sum_quantity DESC, b.create_date DESC";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);

		try {
			// tạo đối tượng truy vấn trong database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setInt(1, Constant.DELIVERED_ORDER_STATUS);
			preStatement.setString(2, fromDate);
			preStatement.setString(3, toDate);

			// thực hiện truy vấn database
			resultSet = preStatement.executeQuery();

			// duyệt qua danh sách kết quả trả về
			while (resultSet.next()) {
				int id = resultSet.getInt("book_id");
				String title = resultSet.getString("title");
				String author = resultSet.getString("author");
				int price = resultSet.getInt("price");
				int quantityInStock = resultSet.getInt("quantity_in_stock");
				String detail = resultSet.getString("detail");
				String imagePath = resultSet.getString("image_path");
				Date createDate = resultSet.getTimestamp("create_date");
				int soldQuantity = resultSet.getInt("sum_quantity");
				int sumOfSoldBook = resultSet.getInt("sum_price");

				// đóng gói các giá trị thuộc tính vào đối tượng Bean(Book)
				Book book = new Book(id, title, author, price, quantityInStock);
				book.setDetail(detail);
				book.setImagePath(imagePath);
				book.setCreateDate(createDate);
				book.setSoldQuantity(soldQuantity);
				book.setSumOfSoldBook(sumOfSoldBook);

				// thêm đối tượng Bean vào danh sách
				listBooks.add(book);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closeStatement(statement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return listBooks;
	}

	// Lấy danh sách theo từ khóa tìm kiếm + lọc theo ngày
	public List<Book> listAllBooks(String keyword, String fromDate, String toDate) {

		// danh sách chứa kết quả trả về
		List<Book> searchBookList = new ArrayList<Book>();

		// sql
		String sql = "SELECT b.*, sum(obor.quantity) AS sum_quantity, sum(obor.price*obor.quantity) AS sum_price FROM tblbook b "
				+ "LEFT JOIN " + "(SELECT ob.* from tblorder_book ob INNER JOIN tblorder o ON ob.order_id = o.order_id "
				+ "WHERE o.order_status = ? AND (o.status_date BETWEEN ? AND ?)) obor " + "ON b.book_id = obor.book_id "
				+ "WHERE title LIKE ? " + "GROUP BY b.book_id " + "ORDER BY sum_quantity DESC, b.create_date DESC";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		try {
			// tạo đối tượng truy vấn trong Database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setInt(1, Constant.DELIVERED_ORDER_STATUS);
			preStatement.setString(2, fromDate);
			preStatement.setString(3, toDate);
			preStatement.setString(4, "%" + keyword + "%");

			// thực hiện truy vấn database
			resultSet = preStatement.executeQuery();

			// duyệt qua danh sách kết quả trả về
			while (resultSet.next()) {
				int id = resultSet.getInt("book_id");
				String title = resultSet.getString("title");
				String author = resultSet.getString("author");
				int price = resultSet.getInt("price");
				int quantityInStock = resultSet.getInt("quantity_in_stock");
				String detail = resultSet.getString("detail");
				String imagePath = resultSet.getString("image_path");
				Date createDate = resultSet.getTimestamp("create_date");
				int soldQuantity = resultSet.getInt("sum_quantity");
				int sumOfSoldBook = resultSet.getInt("sum_price");

				// đóng gói các giá trị thuộc tính vào đối tượng Bean(Book)
				Book book = new Book(id, title, author, price, quantityInStock);
				book.setDetail(detail);
				book.setImagePath(imagePath);
				book.setCreateDate(createDate);
				book.setSoldQuantity(soldQuantity);
				book.setSumOfSoldBook(sumOfSoldBook);

				// thêm đối tượng Bean vào danh sách
				searchBookList.add(book);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return searchBookList;
	}

	// xóa sách khỏi danh sách
	public boolean deleteBook(int bookId) {
		boolean result = false;
		// sql
		String sql = "DELETE FROM tblbook WHERE book_id = ?";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);

		try {
			// tạo đối tượng truy vấn trong database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setInt(1, bookId);

			// thực hiện truy vấn database
			int check = preStatement.executeUpdate();
			if (check > 0) {
				result = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return result;
	}

	// Lấy danh sách sách theo id
	public Book getBook(int id) {
		Book book = null;

		// sql
		String sql = "SELECT * FROM tblbook WHERE book_id = ?";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		try {
			// tạo đối tượng truy vấn trong database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setInt(1, id);

			// thực hiện truy vấn database
			resultSet = preStatement.executeQuery();
			if (resultSet.next()) {
				String title = resultSet.getString("title");
				String author = resultSet.getString("author");
				int price = resultSet.getInt("price");
				int quantityInStock = resultSet.getInt("quantity_in_stock");
				String detail = resultSet.getString("detail");
				String imagePath = resultSet.getString("image_path");

				// đóng gói các giá trị thuộc tính vào đối tượng Bean(Book)
				book = new Book(id, title, author, price, quantityInStock);
				book.setDetail(detail);
				book.setImagePath(imagePath);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return book;
	}

	// Update thông tin 1 cuốn sách
	public boolean updateBook(Book book) {
		boolean result = false;

		// sql
		String sql = "UPDATE tblbook SET title = ?, author = ?, price = ?, quantity_in_stock = ?, "
				+ "detail = ?, image_path = ? WHERE book_id = ?";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);

		try {
			// tạo đối tượng truy vấn trong database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setString(1, book.getTitle());
			preStatement.setString(2, book.getAuthor());
			preStatement.setInt(3, book.getPrice());
			preStatement.setInt(4, book.getQuantityInStock());
			preStatement.setString(5, book.getDetail());
			preStatement.setString(6, book.getImagePath());
			preStatement.setInt(7, book.getBookId());

			// thực hiện truy vấn database
			result = preStatement.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}

		return result;
	}

	// Thêm 1 cuốn sách
	public boolean insertBook(Book book) {
		boolean insertResult = false;

		// sql
		String sql = "INSERT INTO tblbook(title, author, price, quantity_in_stock, detail, image_path, create_date) VALUE (?,?,?,?,?,?,?)";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		try {
			// tạo đối tượng truy vấn trong database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setString(1, book.getTitle());
			preStatement.setString(2, book.getAuthor());
			preStatement.setInt(3, book.getPrice());
			preStatement.setInt(4, book.getQuantityInStock());
			preStatement.setString(5, book.getDetail());
			preStatement.setString(6, book.getImagePath());
			preStatement.setString(7, MyUtils.convertDateToString(book.getCreateDate()));

			// thực hiện truy vấn database
			insertResult = preStatement.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return insertResult;
	}

	// Lấy danh sách theo từ khóa + sắp xếp theo ngày tạo mới nhất xếp trước + phân
	// trang
	public List<Book> listAllBooks(int offset, int noOfRecords, String keyword) {
		// danh sách chứa kết quả trả về
		List<Book> listBook = new ArrayList<Book>();

		// sql
		String sql = "SELECT * FROM tblbook ";
		if (keyword != null && !keyword.isEmpty()) {
			sql += "WHERE title LIKE ? ";
		}
		sql += "ORDER BY create_date DESC ";
		sql += "LIMIT ?, ?";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		try {
			// tạo đối tượng truy vấn trong database
			int index = 0;
			preStatement = jdbcConnection.prepareStatement(sql);
			if (keyword != null && !keyword.isEmpty()) {
				preStatement.setString(++index, "%" + keyword + "%");
			}
			preStatement.setInt(++index, offset); // vị trí bắt đầu lấy
			preStatement.setInt(++index, noOfRecords); // số bản ghi lấy ra

			// thực hiện truy vấn database
			resultSet = preStatement.executeQuery();

			// duyệt qua danh sách kết quả trả về
			while (resultSet.next()) {
				int id = resultSet.getInt("book_id");
				String title = resultSet.getString("title");
				String author = resultSet.getString("author");
				int price = resultSet.getInt("price");
				int quantityInStock = resultSet.getInt("quantity_in_stock");
				String detail = resultSet.getString("detail");
				String imagePath = resultSet.getString("image_path");

				// đóng gói các giá trị thuộc tính vào đối tượng Bean(Book)
				Book book = new Book(id, title, author, price, quantityInStock);
				book.setDetail(detail);
				book.setImagePath(imagePath);

				// thêm đối tượng Bean vào danh sách
				listBook.add(book);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return listBook;
	}

	// Lấy danh sách theo từ khóa + sắp xếp theo ngày tạo mới nhất xếp trước
	public int getNoOfRecords(String keyword) {
		// sql
		String sql = "SELECT count(book_id) FROM tblbook ";
		int result = 0;
		if (keyword != null && !keyword.isEmpty()) {
			sql += "WHERE title LIKE ? ";
		}
		sql += "ORDER BY create_date DESC ";

		// tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		try {
			// tạo đối tượng truy vấn trong database
			preStatement = jdbcConnection.prepareStatement(sql);
			if (keyword != null && !keyword.isEmpty()) {
				preStatement.setString(1, "%" + keyword + "%");
			}

			// thực hiện truy vấn database
			resultSet = preStatement.executeQuery();
			if (resultSet.next()) {
				result = resultSet.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return result;
	}

	public int getNoOfRecords(String keyword, String todaySubtract12MonthStr, String todayStr) {
	    //sql - SELECT
	    String sql = "SELECT count(book_id) FROM tblbook WHERE create_date BETWEEN ? AND ? ";

	    // truy vấn SQL để bao gồm tìm kiếm từ khóa nếu có
	    if (keyword != null && !keyword.isEmpty()) {
	        sql += "AND title LIKE ? ";
	    }
	    
	    // mệnh đề ORDER BY để đảm bảo tính nhất quán, mặc dù nó không ảnh hưởng đến số lượng
	    sql += "ORDER BY create_date DESC";

	    int result = 0;

	    // tạo kết nối
	    jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
	    try {
	        // tạo đối tượng truy vấn trong database
	        preStatement = jdbcConnection.prepareStatement(sql);

	        preStatement.setString(1, todaySubtract12MonthStr);
	        preStatement.setString(2, todayStr);

	        // Liên kết tham số từ khóa nếu nó không rỗng hoặc trống
	        if (keyword != null && !keyword.isEmpty()) {
	            preStatement.setString(3, "%" + keyword + "%");
	        }

	        // thực hiên truy vấn database
	        resultSet = preStatement.executeQuery();
	        if (resultSet.next()) {
	            result = resultSet.getInt(1);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        DBConnection.closeResultSet(resultSet);
	        DBConnection.closePreparedStatement(preStatement);
	        DBConnection.closeConnect(jdbcConnection);
	    }
	    
	    return result;
	}

	public List<Book> listAllBooks(String keyword, String todaySubtract12MonthStr, String todayStr, int i, int recordsPerPage) {
	    List<Book> listBook = new ArrayList<Book>();

	    //sql - SELECT
	    String sql = "SELECT * FROM tblbook WHERE create_date BETWEEN ? AND ? ";

	    // truy vấn SQL để bao gồm tìm kiếm từ khóa nếu có
	    if (keyword != null && !keyword.isEmpty()) {
	        sql += "AND title LIKE ? ";
	    }

	    // mệnh đề ORDER BY để đảm bảo tính nhất quán, mặc dù nó không ảnh hưởng đến số lượng
	    sql += "ORDER BY create_date DESC LIMIT ?, ?";

	    // tạo kết nối
	    jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
	    try {
	        // tạo đối tượng truy vấn trong database
	        PreparedStatement preStatement = jdbcConnection.prepareStatement(sql);
	   
	        preStatement.setString(1, todaySubtract12MonthStr);
	        preStatement.setString(2, todayStr);

	        int index = 2;

	        // Ràng buộc từ khóa nếu nó có xuất hiện
	        if (keyword != null && !keyword.isEmpty()) {
	            preStatement.setString(++index, "%" + keyword + "%");
	        }

	        // Ràng buộc các tham số phân trang
	        preStatement.setInt(++index, i);
	        preStatement.setInt(++index, recordsPerPage);

	        // thực hiện truy vấn database
	        ResultSet resultSet = preStatement.executeQuery();
	        while (resultSet.next()) {
	            int id = resultSet.getInt("book_id");
	            String title = resultSet.getString("title");
	            String author = resultSet.getString("author");
	            int price = resultSet.getInt("price");
	            int quantityInStock = resultSet.getInt("quantity_in_stock");
	            String detail = resultSet.getString("detail");
	            String imagePath = resultSet.getString("image_path");
	            Date createDate = resultSet.getDate("create_date");

	            // đóng gói các giá trị thuộc tính vào đối tượng Bean(Book)
	            Book book = new Book(id, title, author, price, quantityInStock);
	            book.setDetail(detail);
	            book.setImagePath(imagePath);
	            book.setCreateDate(createDate);

	            // Thêm đối tượng Book(Bean) vào danh sách 
	            listBook.add(book);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        DBConnection.closeResultSet(resultSet);
	        DBConnection.closePreparedStatement(preStatement);
	        DBConnection.closeConnect(jdbcConnection);
	    }

	    return listBook;
	}

	public List<Book> listAllBooks(String todaySubtract12MonthStr, String todayStr, int offset, int recordsPerPage) {
	    List<Book> listBook = new ArrayList<>();

	    // sql - SELECT
	    String sql = "SELECT * FROM tblbook WHERE create_date BETWEEN ? AND ? ORDER BY create_date DESC LIMIT ?, ?";

	    // tạo kết nối
	    jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
	    try {
	        // tạo đối tượng để truy vấn trong database
	        PreparedStatement preStatement = jdbcConnection.prepareStatement(sql);

	        preStatement.setString(1, todaySubtract12MonthStr);
	        preStatement.setString(2, todayStr);
	        preStatement.setInt(3, offset);
	        preStatement.setInt(4, recordsPerPage);

	        // thực hiện truy vấn database
	        ResultSet resultSet = preStatement.executeQuery();

	        // Xử lý tập kết quả để điền danh sách sách
	        while (resultSet.next()) {
	            int id = resultSet.getInt("book_id");
	            String title = resultSet.getString("title");
	            String author = resultSet.getString("author");
	            int price = resultSet.getInt("price");
	            int quantityInStock = resultSet.getInt("quantity_in_stock");
	            String detail = resultSet.getString("detail");
	            String imagePath = resultSet.getString("image_path");
	            Date createDate = resultSet.getDate("create_date");

	            // đóng gói các giá trị thuộc tính vào đối tượng Bean(Book)
	            Book book = new Book(id, title, author, price, quantityInStock);
	            book.setDetail(detail);
	            book.setImagePath(imagePath);
	            book.setCreateDate(createDate);


	            // Thêm đối tượng Book(Bean) vào danh sách 
	            listBook.add(book);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        DBConnection.closeResultSet(resultSet);
	        DBConnection.closePreparedStatement(preStatement);
	        DBConnection.closeConnect(jdbcConnection);
	    }
	    return listBook;
	}
}
