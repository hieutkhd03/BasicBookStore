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

import vnua.fita.bookstore.bean.Book;
import vnua.fita.bookstore.model.BookDAO;
import vnua.fita.bookstore.utils.Constant;

@WebServlet(urlPatterns = {"/detailBook"})
public class DetailBookServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BookDAO bookDAO;

	public DetailBookServlet() {
		super();
	}

	public void init() {
		String jdbcURL = getServletContext().getInitParameter("jdbcURL");
		String jdbcPassword = getServletContext().getInitParameter("jdbcPassword");
		String jdbcUsername = getServletContext().getInitParameter("jdbcUsername");
		bookDAO = new BookDAO(jdbcURL, jdbcUsername, jdbcPassword);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		List<String> errors = new ArrayList<String>();
		String bookIdStr = req.getParameter("bookId");
		int bookId = -1;
		try {
			bookId = Integer.parseInt(bookIdStr);
		} catch (Exception e) {
			errors.add(Constant.BOOK_ID_INVALID_VALIDATE_MSG);
		}
		if (errors.isEmpty()) {
			Book book = bookDAO.getBook(bookId);
			if (book == null) {
				errors.add(Constant.GET_BOOK_FAIL);
			} else {
				req.setAttribute("book", book);
				RequestDispatcher rd = req.getServletContext()
						.getRequestDispatcher("/views/detailBookView.jsp");
				rd.forward(req, res);
			}
		}
		
		if(!errors.isEmpty()) {
			req.setAttribute("errors", String.join(", ", errors));
			RequestDispatcher rd = req.getServletContext()
					.getRequestDispatcher("/clientHome");
			rd.forward(req, res);
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doGet(req, res);
	}

}
