package vnua.fita.bookstore.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import vnua.fita.bookstore.bean.Book;
import vnua.fita.bookstore.bean.Cart;
import vnua.fita.bookstore.bean.CartItem;
import vnua.fita.bookstore.model.BookDAO;
import vnua.fita.bookstore.utils.Constant;
import vnua.fita.bookstore.utils.MyUtils;

@WebServlet(urlPatterns = { "/cartBook/addToCart", "/cartBook/removeFromCart", "/cartBook/viewCart" })
public class CartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BookDAO bookDAO;

	public CartServlet() {
		super();
	}

	public void init() {
		String jdbcURL = getServletContext().getInitParameter("jdbcURL");
		String jdbcPassword = getServletContext().getInitParameter("jdbcPassword");
		String jdbcUsername = getServletContext().getInitParameter("jdbcUsername");
		bookDAO = new BookDAO(jdbcURL, jdbcUsername, jdbcPassword);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession session = req.getSession();
		List<String> errors = new ArrayList<String>();
		String servletPath = req.getServletPath();
		String pathInfo = MyUtils.getPathInfoFromServletPath(servletPath);
		String bookIdStr = req.getParameter("bookId");
		String quantityPurchasedStr = req.getParameter("quantityPurchased");
		int bookId = -1;
		int quantityPurchased = -1;
		try {
			if (bookIdStr != null) { // nếu có bookId gửi tới
				bookId = Integer.parseInt(bookIdStr);
			}
		} catch (NumberFormatException e) {
			errors.add(Constant.BOOK_ID_INVALID_VALIDATE_MSG);
		}

		try {
			if (quantityPurchasedStr != null) { // nếu có quantityPurchased gửi tới
				quantityPurchased = Integer.parseInt(quantityPurchasedStr);
			}
		} catch (NumberFormatException e) {
			errors.add(Constant.QUANTITY_IN_STOCK_INVALID_VALIDATE_MSG);
		}

		if (errors.isEmpty()) {
			if ("addToCart".equals(pathInfo)) {// thêm vào giỏ hàng
				Book selectedBook = bookDAO.getBook(bookId);
				Cart cartOfCustomer = MyUtils.getCartOfCustomer(session);
				if (cartOfCustomer == null) { // chưa tồn tại giỏ hàng
					cartOfCustomer = new Cart();
				}
				cartOfCustomer.addCartItemToCart(bookId, new CartItem(selectedBook, quantityPurchased));
				MyUtils.storeCart(session, cartOfCustomer);

			} else if ("removeFromCart".equals(pathInfo)) { // xóa từ giỏ hàng
				Cart cartOfCustomer = MyUtils.getCartOfCustomer(session);
				cartOfCustomer.removeCartItemFromCart(bookId);
				MyUtils.storeCart(session, cartOfCustomer);
			}

			// trường hợp yêu cầu view cart nhảy trực tiếp đến đây
			RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/views/cartView.jsp");
			rd.forward(req, res);
		} else { // nếu có lỗi chuyển kiểu
			res.sendRedirect(req.getContextPath() + "/clientHome");
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doGet(req, res);
	}
}
