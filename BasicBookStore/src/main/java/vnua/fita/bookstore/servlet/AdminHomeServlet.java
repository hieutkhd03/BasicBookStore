package vnua.fita.bookstore.servlet;

import java.io.IOException;
import java.util.Date;
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
import vnua.fita.bookstore.utils.MyUtils;

@WebServlet(urlPatterns = { "/adminHome" })
public class AdminHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private BookDAO bookDAO;

	public void init() {
		String jdbcURL = getServletContext().getInitParameter("jdbcURL");
		String jdbcPassword = getServletContext().getInitParameter("jdbcPassword");
		String jdbcUsername = getServletContext().getInitParameter("jdbcUsername");
		bookDAO = new BookDAO(jdbcURL, jdbcUsername, jdbcPassword);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
	    String errors = null;
	    int page = 1;
	    int recordsPerPage = 2; // số sản phẩm / 1 trang
	    if(request.getParameter("page") != null) {
	        page = Integer.parseInt(request.getParameter("page"));
	    }
	    
	    String keyword = request.getParameter("keyword");
	    Date today = new Date();
	    Date todaySubtract12Month = MyUtils.subtractFromDate(12, today);
	    String todaySubtract12MonthStr = MyUtils.convertDateToString(todaySubtract12Month);
	    String todayStr = MyUtils.convertDateToString(today);
	    
	    // phân trang
	    List<Book> list;
	    if(keyword != null && !keyword.isEmpty()) {
	        list = bookDAO.listAllBooks(keyword, todaySubtract12MonthStr, todayStr, (page-1) * recordsPerPage, recordsPerPage);
	    } else {
	        list = bookDAO.listAllBooks(todaySubtract12MonthStr, todayStr, (page-1) * recordsPerPage, recordsPerPage);
	    }
	    
	    int noOfRecords = bookDAO.getNoOfRecords(keyword, todaySubtract12MonthStr, todayStr); // điều chỉnh phạm vi ngày và từ khóa.
	    int noOfPages = (int) Math.ceil(noOfRecords * 1.0 / recordsPerPage);
	    
	    if (list.isEmpty()) {
	        errors = Constant.GET_BOOK_FAIL;
	    }
	    
	    request.setAttribute("errors", errors);
	    request.setAttribute("turnover", calSumOfMoney(list)); // tính doanh thu
	    request.setAttribute("bookList", list);
	    request.setAttribute("noOfPages", noOfPages);
	    request.setAttribute("currentPage", page);
	    request.setAttribute("keyword", keyword);
	    
	    RequestDispatcher rd = this.getServletContext().getRequestDispatcher("/views/adminHomeView.jsp");
	    rd.forward(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String errors = null;
		String forwardPage;
		List<Book> list = null;
		
		String fromDateParam = request.getParameter("fromDate");
		String toDateParam = request.getParameter("toDate");
		if(validateDate(fromDateParam, toDateParam)) {
			String fromDate = MyUtils.attachTailToDate(fromDateParam);
			String toDate = MyUtils.attachTailToDate(toDateParam);
			list = bookDAO.listAllBooks(fromDate, toDate);
			if(list.isEmpty()) {
				errors = Constant.GET_BOOK_FAIL;
			}
			
			request.setAttribute("errors", errors);
			request.setAttribute("fromDate", fromDateParam);
			request.setAttribute("toDate", toDateParam);
			request.setAttribute("turnover", calSumOfMoney(list));
			request.setAttribute("bookList", list);
			forwardPage = "/views/adminHomeView.jsp";
		}else {
			forwardPage = "adminHome";
		}
		RequestDispatcher rd = this.getServletContext()
				.getRequestDispatcher(forwardPage);
		rd.forward(request, response);
	}

	private boolean validateDate(String fromDate, String toDate) {
		if(fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
			return true;
		}
		return false;
	}
	
	private int calSumOfMoney(List<Book> list) {
		int sum = 0;
		for(Book book: list) {
			sum+=book.getSumOfSoldBook();
		}
		return sum;
	}
}
