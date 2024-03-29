package vnua.fita.bookstore.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import vnua.fita.bookstore.bean.Book;
import vnua.fita.bookstore.formbean.BookForm;
import vnua.fita.bookstore.model.BookDAO;
import vnua.fita.bookstore.utils.Constant;
import vnua.fita.bookstore.utils.MyUtils;

@WebServlet(urlPatterns = {"/editBook"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
		maxFileSize = 1024 * 1024 * 10, // 10MB
		maxRequestSize = 1024 * 1024 * 20 // 20MB
)
public class EditBookServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BookDAO bookDAO;

	public EditBookServlet() {
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

		String bookIdStr = (String) req.getParameter("bookId");
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
						.getRequestDispatcher("/views/editBookView.jsp");
				rd.forward(req, res);
			}
		}
		if (!errors.isEmpty()) {
			req.setAttribute("errors", String.join(", ", errors));
			RequestDispatcher rd = req.getServletContext()
					.getRequestDispatcher("/adminHome");
			rd.forward(req, res);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Xử lý Tiếng việt cho request,reponse
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String bookIdStr = request.getParameter("bookId");
		String title = request.getParameter("title");
		String author = request.getParameter("author");
		String priceStr = request.getParameter("price");
		String quantityInStockStr = request.getParameter("quantityInStock");
		String detail = request.getParameter("detail");
		Part filePart = request.getPart("file");
		String imagePath = request.getParameter("imagePath");

		BookForm bookForm = new BookForm(bookIdStr, title, author, priceStr,
				quantityInStockStr, detail, filePart, imagePath);
		List<String> errors = bookForm.validateEditBookForm();
		if (errors.isEmpty()) {
			int bookId = Integer.parseInt(bookIdStr);
			int price = Integer.parseInt(priceStr);
			int quantityInStock = Integer.parseInt(quantityInStockStr);

			//Sửa ảnh 
			if (filePart != null && filePart.getSize() > 0) {
				String fileName = title + "_" + MyUtils.getTimeLabel()
						+ MyUtils.extracFileExtension(filePart);
				String appPath = getServletContext().getRealPath("");
				filePart.write(
						MyUtils.getFolderUpload(appPath, "book-img").getAbsolutePath() + File.separator + fileName);
				imagePath = "book-img" + File.separator + fileName;
			}

			Book book = new Book(bookId, title, author, price, quantityInStock);
			book.setDetail(detail);
			book.setImagePath(imagePath);
			boolean resultUpdate = bookDAO.updateBook(book);
			if (!resultUpdate) {
				errors.add("Update thất bại");
			} else {
				response.sendRedirect(request.getContextPath() + "/adminHome");
			}
		}

		if (!errors.isEmpty()) {
			request.setAttribute("errors", String.join(", ", errors));
			request.setAttribute("book", bookForm);
			RequestDispatcher rd = request.getServletContext()
					.getRequestDispatcher("/views/editBookView.jsp");
			rd.forward(request, response);
		}
	}
}

