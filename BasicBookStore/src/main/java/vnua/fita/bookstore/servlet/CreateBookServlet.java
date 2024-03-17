package vnua.fita.bookstore.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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
import vnua.fita.bookstore.utils.MyUtils;

@WebServlet(urlPatterns = {"/createBook"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
		maxFileSize = 1024 * 1024 * 10, // 10MB
		maxRequestSize = 1024 * 1024 * 20 // 20MB
)
public class CreateBookServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BookDAO bookDAO;

	public CreateBookServlet() {
		super();	
	}

	@Override
	public void init() throws ServletException {
		String jdbcURL = getServletContext().getInitParameter("jdbcURL");
		String jdbcUsername = getServletContext().getInitParameter("jdbcUsername");
		String jdbcPassword = getServletContext().getInitParameter("jdbcPassword");
		bookDAO = new BookDAO(jdbcURL, jdbcUsername, jdbcPassword);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		RequestDispatcher rd = req.getServletContext().getRequestDispatcher("/views/createBookView.jsp");
		rd.forward(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		// Xử lý Tiếng việt cho request,reponse
		req.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");
				
		String title = req.getParameter("title");
		String author = req.getParameter("author");
		String priceStr = req.getParameter("price");
		String quantityInStockStr = req.getParameter("quantityInStock");
		String detail = req.getParameter("detail");
		Part filePart = req.getPart("file");
		String imagePath;

		BookForm bookForm = new BookForm(title, author, priceStr, quantityInStockStr,detail, filePart);
		List<String> errors = bookForm.validateCreateBookForm();
		if (errors.isEmpty()) {
			int price = Integer.parseInt(priceStr);
			int quantityInStock = Integer.parseInt(quantityInStockStr);

			// lưu ảnh vào thư mục 'book-img' nếu có
			String fileName = title + "_" + MyUtils.getTimeLabel() + MyUtils.extracFileExtension(filePart);
			String contextPath = getServletContext().getRealPath("/"); // Lấy đường dẫn thực của web
			String savePath = contextPath + "book-img"; // Đường dẫn đến thư mục 'book-img'
			File fileSaveDir = new File(savePath);
			if (!fileSaveDir.exists()) {
				fileSaveDir.mkdir(); // Tạo thư mục 'book-img' nếu nó không tồn tại
			}

			String filePath = savePath + File.separator + fileName; // Đường dẫn file cuối cùng để lưu trữ ảnh
			filePart.write(filePath); // Lưu file ảnh
			imagePath = "book-img" + File.separator + fileName; // Đường dẫn tương đối để lưu trong database
			
			Book book = new Book(title, author, price, quantityInStock, detail,imagePath);
			book.setCreateDate(new Date());

			boolean insertResult = bookDAO.insertBook(book);
			if (!insertResult) {
				errors.add("Thêm sách không thành công");
			} else {
				res.sendRedirect(req.getContextPath() + "/adminHome");
			}
		}

		if (!errors.isEmpty()) {
			req.setAttribute("errors", String.join(", ", errors));
			req.setAttribute("book", bookForm);
			RequestDispatcher rd = req.getServletContext()
					.getRequestDispatcher("/views/createBookView.jsp");
			rd.forward(req, res);
		}
	}
}
