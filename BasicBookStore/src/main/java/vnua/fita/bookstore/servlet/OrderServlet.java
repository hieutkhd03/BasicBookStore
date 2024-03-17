package vnua.fita.bookstore.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import vnua.fita.bookstore.bean.Cart;
import vnua.fita.bookstore.bean.CartItem;
import vnua.fita.bookstore.bean.Order;
import vnua.fita.bookstore.bean.User;
import vnua.fita.bookstore.model.OrderDAO;
import vnua.fita.bookstore.utils.Constant;
import vnua.fita.bookstore.utils.MyUtils;

@WebServlet(urlPatterns = {"/order"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
		maxFileSize = 1024 * 1024 * 10, // 10MB
		maxRequestSize = 1024 * 1024 * 20 // 20MB
)
public class OrderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OrderDAO orderDAO;

	public void init() {
		String jdbcURL = getServletContext().getInitParameter("jdbcURL");
		String jdbcPassword = getServletContext().getInitParameter("jdbcPassword");
		String jdbcUsername = getServletContext().getInitParameter("jdbcUsername");
		orderDAO = new OrderDAO(jdbcURL, jdbcUsername, jdbcPassword);
	}

	public OrderServlet() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		// Xử lý Tiếng việt cho request,reponse
		req.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");
		
		List<String> errors = new ArrayList<String>();
		String deliveryAddress = req.getParameter("deliveryAddress");
		String paymentMode = req.getParameter("paymentMode");
		Part filePart = req.getPart("file");
		
		HttpSession session = req.getSession();

		validateOrderForm(deliveryAddress, paymentMode, filePart, errors);
		if (!errors.isEmpty()) {
			req.setAttribute("errors", errors);
			RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher("/views/cartView.jsp");
			dispatcher.forward(req, res);
			return;
		}
		Order order = createOrder(deliveryAddress, paymentMode, filePart, session);
		String fowardPage;
		if (orderDAO.checkAndUpdateAvaiableBookOfOrder(order)) {
			boolean insertResult = orderDAO.insertOrder(order);
			if (insertResult) {
				req.setAttribute(Constant.CART_OF_CUSTOMER, MyUtils.getCartOfCustomer(session));
				req.setAttribute(Constant.ORDER_OF_CUSTOMER, order);
				MyUtils.deleteCart(session);
				fowardPage = "/views/detailOrderView.jsp";
			} else {
				req.setAttribute("errors", Constant.ADD_TO_CART_ACTION);
				fowardPage = "/views/cartView.jsp";
			}
		} else {
			MyUtils.updateCartOfCustomer(session, converListToMap(order.getOrderBookList()));
			fowardPage = "/views/cartView.jsp";
		}
		RequestDispatcher dispatcher = this.getServletContext().getRequestDispatcher(fowardPage);
		dispatcher.forward(req, res);
	}

	private Map<Integer, CartItem> converListToMap(List<CartItem> orderBookList) {
		Map<Integer, CartItem> cartItemList = new HashMap<Integer, CartItem>();
		for (CartItem cartItem : orderBookList) {
			cartItemList.put(cartItem.getSelectedBook().getBookId(), cartItem);
		}
		return cartItemList;
	}

	private Order createOrder(String deliveryAddress, String paymentMode, Part filePart, HttpSession session)
			throws IOException {
		Order order = new Order();
		Date date = new Date();

		Cart cartOfCustomer = MyUtils.getCartOfCustomer(session);
		String customerName = MyUtils.getLoginedUser(session).getUsername();
		User customer = new User();
		customer.setUsername(customerName);
		order.setCustomer(customer);
		order.setDeliveryAddress(deliveryAddress);
		order.setPaymentMode(paymentMode);
		order.setOrderDate(date);
		order.setStatusDate(date);
		order.setTotalCost((int) cartOfCustomer.getTotalCost());
		order.setOrderBookList(new ArrayList<CartItem>(cartOfCustomer.getCartItemList().values()));
		if (Constant.CASH_PAYMENT_MODE.equals(paymentMode)) {
			order.setOrderStatus(Constant.DELIVERING_ORDER_STATUS);
			order.setOrderApproveDate(date);
			order.setPaymentStatus(false);
		} else if (Constant.TRANSFER_PAYMENT_MODE.equals(paymentMode)) {
			String fileName = customerName + "_" + MyUtils.getTimeLabel() + MyUtils.extracFileExtension(filePart);
			String contextPath = getServletContext().getRealPath("/"); // Lấy đường dẫn thực của ứng dụng web
			String savePath = contextPath + "transfer-img-upload"; // Đường dẫn đến thư mục 'img'

			File fileSaveDir = new File(savePath);
			if (!fileSaveDir.exists()) {
			    fileSaveDir.mkdir(); // Tạo thư mục 'img' nếu nó không tồn tại
			}
			order.setOrderApproveDate(date);

			String filePath = savePath + File.separator + fileName; // Đường dẫn file cuối cùng để lưu trữ ảnh
			filePart.write(filePath); // Lưu file ảnh
			String imagePath = "transfer-img-upload" + File.separator + fileName; 
			order.setOrderStatus(Constant.WAITTING_CONFIRM_ORDER_STATUS);
			order.setPaymentStatus(false);
			order.setPaymentImagePath(imagePath);
		}
		return order;
	}

	private void validateOrderForm(String deliveryAddress, String paymentMode, Part filePart, List<String> errors) {
		if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
			errors.add(Constant.DELIVERY_ADDRESS_EMPTY_VALIDATE_MSG);
		}
		if (Constant.TRANSFER_PAYMENT_MODE.equals(paymentMode)) {
			if (filePart == null || filePart.getSize() < 0) {
				errors.add(Constant.TRANSFER_IMAGE_EMPTY_MSG);
			}
		}
	}
}
