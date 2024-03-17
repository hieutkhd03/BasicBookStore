package vnua.fita.bookstore.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import vnua.fita.bookstore.bean.Book;
import vnua.fita.bookstore.model.BookDAO;

@WebServlet(urlPatterns = {"/clientHome"})
public class ClientHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BookDAO bookDAO;

	public ClientHomeServlet() {
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
		List<Book> list = null;
		int page = 1;
		int recordsPerPage = 2;
		if (req.getParameter("page") != null) {
			page = Integer.parseInt(req.getParameter("page"));
		}

		String keyword = req.getParameter("keyword");

		list = bookDAO.listAllBooks((page - 1) * recordsPerPage, recordsPerPage, keyword);
		int noOfRecords = bookDAO.getNoOfRecords(keyword);
		int noOfPages = (int) Math.ceil(noOfRecords * 1.0 / recordsPerPage);

		// Lưu thông tin vào request attribute trước khi forward sang views
		req.setAttribute("bookList", list);
		req.setAttribute("noOfPages", noOfPages);
		req.setAttribute("currentPage", page);
		req.setAttribute("keyword", keyword);

		RequestDispatcher rd = this.getServletContext()
				.getRequestDispatcher("/views/clientHomeView.jsp");
		rd.forward(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doGet(req, res);
	}

}
