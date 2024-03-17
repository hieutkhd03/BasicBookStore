package vnua.fita.bookstore.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vnua.fita.bookstore.bean.Book;
import vnua.fita.bookstore.bean.CartItem;
import vnua.fita.bookstore.bean.Order;
import vnua.fita.bookstore.bean.User;
import vnua.fita.bookstore.utils.Constant;
import vnua.fita.bookstore.utils.MyUtils;

public class OrderDAO {
	private String jdbcURL;
	private String jdbcUsername;
	private String jdbcPassword;
	private Connection jdbcConnection;
	private Statement statement;
	private PreparedStatement preStatement;
	private ResultSet resultSet;

	public OrderDAO(String jdbcURL, String jdbcUsername, String jdbcPassword) {
		super();
		this.jdbcURL = jdbcURL;
		this.jdbcUsername = jdbcUsername;
		this.jdbcPassword = jdbcPassword;
	}

	// trả lại kết quả check và cập nhật số lượng có trong kho của các mặt hàng
	public boolean checkAndUpdateAvaiableBookOfOrder(Order order) {
		boolean checkAvaiable = true; // có hàng
		
		String sql;
		List<CartItem> orderBookList = order.getOrderBookList();
		try {
			
			//tạo kết nối
			jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
			for (CartItem cartItem : orderBookList) {
				
				//sql
				sql = "SELECT quantity_in_stock FROM tblbook WHERE book_id=?";
				
				//tạo đối tượng truy vấn trong database
				preStatement = jdbcConnection.prepareStatement(sql);
				Book selectedBook = cartItem.getSelectedBook();
				preStatement.setInt(1, selectedBook.getBookId());
				
				//thực hiện truy vấn database
				resultSet = preStatement.executeQuery();
				if (resultSet.next()) {
					int presentQuantityInStock = resultSet.getInt("quantity_in_stock"); // số lượng hiện có trong kho
					// nếu nhỏ hơn số lượng đặt mua
					if (presentQuantityInStock < cartItem.getQuantity()) { 
						checkAvaiable = false;
						selectedBook.setQuantityInStock(presentQuantityInStock); // cập nhật số lượng có trong kho của sách
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return checkAvaiable;
	}

	//Thêm 1 đơn hàng
	public boolean insertOrder(Order order) {
		boolean insertResult = false;
		int orderId = -1;
		String orderNo = null;
		
		//sql
		String sql1 = "INSERT INTO tblorder (customer_username,payment_mode,order_status, total_cost,payment_img,payment_status,delivery_address,order_approve_date,status_date,order_date) VALUES (?,?,?,?,?,?,?,?,?,?)";
		String sql2 = "SELECT MAX(order_id) FROM tblorder t";
		String sql3 = "UPDATE tblorder SET order_no = ? WHERE order_id =?";
		String sql4 = "INSERT INTO tblorder_book (book_id,order_id,quantity,price) VALUES (?,?,?,?)";
		String sql5 = "UPDATE tblbook t SET t.quantity_in_stock = ? WHERE t.book_id =?";

		//tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		try {
			jdbcConnection.setAutoCommit(false);

			// tạo đối tượng truy vấn trong database
			preStatement = jdbcConnection.prepareStatement(sql1);
			preStatement.setString(1, order.getCustomer().getUsername());
			preStatement.setString(2, order.getPaymentMode());
			preStatement.setByte(3, order.getOrderStatus());
			preStatement.setFloat(4, order.getTotalCost());
			preStatement.setString(5, order.getPaymentImagePath());
			preStatement.setBoolean(6, order.isPaymentStatus());
			preStatement.setString(7, order.getDeliveryAddress());
			if (order.getOrderApproveDate() != null) {
				preStatement.setString(8, MyUtils.convertDateToString(order.getOrderApproveDate()));
			} else {
				preStatement.setString(8, null);
			}
			preStatement.setString(9, MyUtils.convertDateToString(order.getStatusDate()));
			preStatement.setString(10, MyUtils.convertDateToString(order.getOrderDate()));
			
			//thực hiện truy vấn database
			insertResult = preStatement.executeUpdate() > 0;

			// Lấy order_id của hóa đơn vừa tạo
			statement = jdbcConnection.createStatement();
			resultSet = statement.executeQuery(sql2);
			if (resultSet.next()) {
				orderId = resultSet.getInt(1);

				// nếu là hình thức thanh toán khi nhận hàng, tạo luôn orderNo từ orderId vừa tạo
				if ("cash".equals(order.getPaymentMode())) {
					preStatement = jdbcConnection.prepareStatement(sql3);
					orderNo = MyUtils.createOrderNo(orderId);
					preStatement.setString(1, orderNo);
					preStatement.setInt(2, orderId);
					insertResult = preStatement.executeUpdate() > 0;
					if (!insertResult) {
						throw new SQLException();
					}
				}
				// nếu là hình thức chuyển khoản, tạo luôn orderNo từ orderId vừa tạo
				if ("transfer".equals(order.getPaymentMode())) {
					preStatement = jdbcConnection.prepareStatement(sql3);
					orderNo = MyUtils.createOrderNo(orderId);
					preStatement.setString(1, orderNo);
					preStatement.setInt(2, orderId);
					insertResult = preStatement.executeUpdate() > 0;
					if (!insertResult) {
						throw new SQLException();
					}
				}
				
				List<CartItem> orderBookList = order.getOrderBookList();
				for (CartItem cartItem : orderBookList) {
					// thêm các cuốn sách trong hóa đơn vào bảng tblorder_book
					preStatement = jdbcConnection.prepareStatement(sql4);
					preStatement.setInt(1, cartItem.getSelectedBook().getBookId());
					preStatement.setInt(2, orderId);
					preStatement.setInt(3, cartItem.getQuantity());
					preStatement.setInt(4, cartItem.getSelectedBook().getPrice());
					insertResult = preStatement.executeUpdate() > 0;
					if (!insertResult) {
						throw new SQLException();
					}

					// cập nhật số lượng trong bảng tblbook
					preStatement = jdbcConnection.prepareStatement(sql5);
					preStatement.setInt(1, cartItem.getSelectedBook().getQuantityInStock() - cartItem.getQuantity());
					preStatement.setInt(2, cartItem.getSelectedBook().getBookId());
					insertResult = preStatement.executeUpdate() > 0;
					if (!insertResult) {
						throw new SQLException();
					}
				}
				jdbcConnection.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		if (insertResult) {
			// thêm thành công, bổ sung thuộc tính vào đối tượng order và trả lại
			order.setOrderId(orderId);
			order.setOrderNo(orderNo);
		}
		return insertResult;
	}

	public List<Order> getOrderList(String customerUserName) {
		Map<Integer, Order> orderListMap = new HashMap<Integer, Order>();
		
		//sql
		String sql = "SELECT t.*, ordb.quantity ,ordb.price ,b.*,u.* FROM tblorder t\r\n"
				+ "INNER JOIN tblorder_book ordb ON t.order_id=ordb.order_id \r\n"
				+ "INNER JOIN tblbook b ON b.book_id =ordb.book_id \r\n"
				+ "INNER JOIN tbluser u ON u.username =t.customer_username \r\n" + "WHERE t.customer_username =?\r\n"
				+ "ORDER BY t.order_date DESC ";
		try {
			//tạo kết nối
			jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
			
			//tạo đối tượng truy vấn database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setString(1, customerUserName);
			
			//thực hiện truy vấn database
			ResultSet resultSet = preStatement.executeQuery();
			
			while (resultSet.next()) {
				int orderId = resultSet.getInt("order_id");
				if (!orderListMap.containsKey(orderId)) {
					Order order = new Order();
					fillOrderFromResultSet(resultSet, order);
					List<CartItem> orderBookList = new ArrayList<CartItem>();
					Book orderBook = new Book();
					fillBookFromResultSet(resultSet, orderBook);
					CartItem cartItem = new CartItem(orderBook, resultSet.getInt("ordb.quantity"));
					orderBookList.add(cartItem);
					order.setOrderBookList(orderBookList);
					orderListMap.put(orderId, order);
				} else {
					Order order = orderListMap.get(orderId);
					List<CartItem> orderBookList = order.getOrderBookList();
					Book book = new Book();
					fillBookFromResultSet(resultSet, book);
					CartItem cartItem = new CartItem(book, resultSet.getInt("ordb.quantity"));
					orderBookList.add(cartItem);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closePreparedStatement(preStatement);
			DBConnection.closeConnect(jdbcConnection);
		}
		Collection<Order> values = orderListMap.values();
		List<Order> list = new ArrayList<Order>(values);
		Collections.sort(list);
		return list;
	}
	
	// Đọc thông tin từ ResultSet và điền vào đối tượng Book
	private void fillBookFromResultSet(ResultSet resultSet, Book orderBook) throws SQLException {
		orderBook.setBookId(resultSet.getInt("b.book_id"));
		orderBook.setTitle(resultSet.getString("b.title"));
		orderBook.setAuthor(resultSet.getString("b.author"));
		orderBook.setPrice(resultSet.getInt("ordb.price"));
		orderBook.setImagePath(resultSet.getString("b.image_path"));
	}

	// Đọc thông tin từ ResultSet và điền vào đối tượng Order
	private void fillOrderFromResultSet(ResultSet resultSet, Order order) throws SQLException {
		order.setOrderId(resultSet.getInt("order_id"));
		order.setOrderNo(resultSet.getString("t.order_no"));
		order.setOrderDate(resultSet.getTimestamp("t.order_date"));
		order.setOrderApproveDate(resultSet.getTimestamp("t.order_approve_date"));
		order.setOrderStatus(resultSet.getByte("t.order_status"));
		order.setStatusDate(resultSet.getTimestamp("t.status_date"));
		order.setPaymentMode(resultSet.getString("t.payment_mode"));
		order.setPaymentStatus(resultSet.getBoolean("t.payment_status"));
		order.setTotalCost(resultSet.getInt("t.total_cost"));
		order.setDeliveryAddress(resultSet.getString("t.delivery_address"));
		order.setPaymentImagePath(resultSet.getString("t.payment_img"));

		User user = new User();
		user.setUsername(resultSet.getString("u.username"));
		user.setFullname(resultSet.getString("u.fullname"));
		user.setMobile(resultSet.getString("u.mobile"));
		order.setCustomer(user);
	}

	public List<Order> getOrderList(byte orderStatus) {
		Map<Integer, Order> orderListHashMap = new HashMap<Integer, Order>();
		
		//sql
		String sql = "SELECT t.*,ordb.quantity ,ordb.price, b.*,u.* FROM tblorder t\r\n"
				+ "INNER JOIN tblorder_book ordb ON t.order_id=ordb.order_id \r\n"
				+ "INNER JOIN tblbook b ON ordb.book_id = b.book_id\r\n"
				+ "INNER JOIN tbluser u ON t.customer_username=u.username\r\n" + "WHERE t.order_status = ?\r\n"
				+ "ORDER BY t.status_date DESC, t.order_date DESC";
		
		//tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		try {
			//tạo đối tượng truy vấn trong database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setByte(1, orderStatus);
			
			//thực hiện truy vấn database
			resultSet = preStatement.executeQuery();
			
			while (resultSet.next()) {
				int orderId = resultSet.getInt("order_id");
				if (!orderListHashMap.containsKey(orderId)) {
					Order order = new Order();
					fillOrderFromResultSet(resultSet, order);
					List<CartItem> orderBookList = new ArrayList<CartItem>();
					Book orderBook = new Book();
					fillBookFromResultSet(resultSet, orderBook);
					CartItem cartItem = new CartItem(orderBook, resultSet.getInt("ordb.quantity"));
					orderBookList.add(cartItem);
					order.setOrderBookList(orderBookList);
					orderListHashMap.put(orderId, order);
				} else {
					Order order = orderListHashMap.get(orderId);
					List<CartItem> orderBookList = order.getOrderBookList();
					Book orderBook = new Book();
					fillBookFromResultSet(resultSet, orderBook);
					CartItem cartItem = new CartItem(orderBook, resultSet.getInt("ordb.quantity"));
					orderBookList.add(cartItem);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeResultSet(resultSet);
			DBConnection.closeStatement(statement);
			DBConnection.closeConnect(jdbcConnection);
		}
		Collection<Order> values = orderListHashMap.values();
		ArrayList<Order> orderList = new ArrayList<Order>(values);
		Collections.sort(orderList);
		return orderList;
	}

	public boolean updateOrderNo(int orderId, byte orderStatus) {
		boolean updateResult = false;
		
		//sql
		String sql = "UPDATE tblorder t SET t.order_no = ?,t.order_approve_date = ?, t.order_status = ?, t.status_date = ?, t.payment_status = ?\r\n"
				+ "WHERE t.order_id = ?";
		
		//tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);
		Date now = new Date(); // lấy thời điểm hiện tại
		String orderNo = MyUtils.createOrderNo(orderId);
		try {
			//tạo đối tượng truy vấn database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setString(1, orderNo);
			preStatement.setString(2, MyUtils.convertDateToString(now));
			preStatement.setByte(3, orderStatus);
			preStatement.setString(4, MyUtils.convertDateToString(now));
			preStatement.setBoolean(5, true);
			preStatement.setInt(6, orderId);
			
			//thực hiện truy vấn database
			updateResult = preStatement.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeStatement(statement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return updateResult;
	}

	public boolean updateOrder(int orderId, byte orderStatus) {
		boolean updateResult = false;
		
		//sql
		String sql = "UPDATE tblorder SET order_status = ?, status_date = ?, payment_status = ?\r\n"
				+ "WHERE order_id = ?";
		
		//tạo kết nối
		jdbcConnection = DBConnection.createConnection(jdbcURL, jdbcUsername, jdbcPassword);	
		Date now = new Date(); // lấy thời điểm hiện tại
		String orderNo = MyUtils.createOrderNo(orderId);
		try {
			//tạo đối tượng truy vấn trong database
			preStatement = jdbcConnection.prepareStatement(sql);
			preStatement.setByte(1, orderStatus);
			preStatement.setString(2, MyUtils.convertDateToString(now));
			preStatement.setBoolean(3, Constant.DELIVERED_ORDER_STATUS == orderStatus);
			preStatement.setInt(4, orderId);
			
			//thực hiện truy vấn database
			updateResult = preStatement.executeUpdate() > 0;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBConnection.closeStatement(statement);
			DBConnection.closeConnect(jdbcConnection);
		}
		return updateResult;
	}
}
