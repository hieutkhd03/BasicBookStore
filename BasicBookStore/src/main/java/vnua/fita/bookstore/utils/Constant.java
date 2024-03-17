package vnua.fita.bookstore.utils;

public class Constant {
	//Order
	public static final byte WAITTING_CONFIRM_ORDER_STATUS = 1;
	public static final byte DELIVERING_ORDER_STATUS = 2;
	public static final byte DELIVERED_ORDER_STATUS = 3;
	public static final byte CANCEL_ORDER_STATUS = 4;
	public static final byte REJECT_ORDER_STATUS = 5;
	public static final byte NOT_AVAILABLE_ORDER_STATUS = 6;
	public static final String PAYMENT_STATUS = "Đã trả tiền";
	public static final String UNPAYMENT_STATUS = "Chưa trả tiền";
	//validate(Errors) - BookForm
	public static final String TITLE_EMPTY_VALIDATE_MSG = "Tên sách không được để trống";
	public static final String AUTHOR_EMPTY_VALIDATE_MSG = "Tên tác giả không được để trống";
	public static final String PRICE_EMPTY_VALIDATE_MSG = "Giá tiền không được để trống";
	public static final String PRICE_INVALID_VALIDATE_MSG = "Giá tiền không hợp lệ";
	public static final String QUANTITY_IN_STOCK_EMPTY_VALIDATE_MSG = "Số lượng trong kho không được để trống";
	public static final String QUANTITY_IN_STOCK_INVALID_VALIDATE_MSG = "Số lượng trong kho không hợp lệ";
	public static final String DETAIL_EMPTY_VALIDATE_MSG = "Nội dung không được để trống";
	public static final String FILE_PATH_VALIDATE_MSG = "File tải lên bị lỗi";
	public static final String BOOK_ID_EMPTY_VALIDATE_MSG = "bookId không được để trống";
	public static final String BOOK_ID_INVALID_VALIDATE_MSG = "bookId không hợp lệ";
	//validate(Errors) - LoginForm
	public static final String USERNAME_EMPTY_VALIDATE_MSG = "Tên người dùng không được để trống";
	public static final String PASSWORD_EMPTY_VALIDATE_MSG = "Mật khẩu không được để trống";
	//utils.MyUtils
	public static final String LOGINED_USER = "loginedUser";
	public static final String CART_OF_CUSTOMER = "cartOfCustomer";
	public static final String USERNAME_STORE_IN_COOKIE_OF_BOOKSTORE = "username";
	public static final String TOKEN_STORE_IN_COOKIE_OF_BOOKSTORE = "token";
	public static final String SECRET_STRING = "DuongHieu";
	//servlet.AdminOrderListServlet
	public static final String WAITTING_APPROVE_ACTION = "waitting";
	public static final String DELIVERING_ACTION = "delivering";
	public static final String DELIVERED_ACTION = "delivered";
	public static final String REJECT_ACTION = "reject";
	public static final String ORDER_LIST_OF_CUSTOMER = "orderListOfCustomer";
	public static final String ORDER_ID_INVALID_VALIDATE_MSG = "OrderId không hợp lệ";
	public static final String VALUE_INVALID_VALIDATE_MSG = "OrderStatus không hợp lệ";
	public static final Object UPDATE_ORDER_SUCCESS = "Cập nhật đơn hàng THÀNH CÔNG";
	public static final String UPDATE_ORDER_FAIL = "Cập nhật đơn hàng THẤT BẠI";
	//servlet.LoginServlet
	public static final String INCORRECT_ACCOUNT_VALIDATE_MSG = "Nhập sai thông tin tài khoản";
	public static final Byte CUSTOMER_ROLE = 0;
	public static final Byte ADMIN_ROLE = 1;
	//servlet.OrderServlet
	public static final String ORDER_OF_CUSTOMER = "orderOfCustomer";
	public static final String ADD_TO_CART_ACTION = "addToCart";
	public static final String CASH_PAYMENT_MODE = "cash";
	public static final String TRANSFER_PAYMENT_MODE = "transfer";
	public static final String TRANSFER_IMAGE_EMPTY_MSG = "ảnh chuyển khoản không được trống";
	public static final String DELIVERY_ADDRESS_EMPTY_VALIDATE_MSG = "địa chỉ nhận hàng không dược trống";
	//servlet.AdminOrderServlet
	public static final String GET_BOOK_FAIL = "Không có sách";
}
