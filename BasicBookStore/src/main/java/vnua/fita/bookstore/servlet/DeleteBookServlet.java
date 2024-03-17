package vnua.fita.bookstore.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import vnua.fita.bookstore.model.BookDAO;

@WebServlet(urlPatterns = {"/deleteBook"})
public class DeleteBookServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BookDAO bookDAO;

	public DeleteBookServlet() {
		super();
	}

	public void init() {
		String jdbcURL = getServletContext().getInitParameter("jdbcURL");
		String jdbcPassword = getServletContext().getInitParameter("jdbcPassword");
		String jdbcUsername = getServletContext().getInitParameter("jdbcUsername");
		bookDAO = new BookDAO(jdbcURL, jdbcUsername, jdbcPassword);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String bookId = req.getParameter("bookId");
		int id = Integer.parseInt(bookId);
		boolean checkBookId = bookDAO.deleteBook(id);
		res.sendRedirect(req.getContextPath() + "/adminHome");
	}
}
