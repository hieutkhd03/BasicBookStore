package vnua.fita.bookstore.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import vnua.fita.bookstore.bean.User;
import vnua.fita.bookstore.formbean.LoginForm;
import vnua.fita.bookstore.model.UserDAO;
import vnua.fita.bookstore.utils.Constant;
import vnua.fita.bookstore.utils.MyUtils;

@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDAO userDAO;

	public LoginServlet() {
		super();
	}

	@Override
	public void init() throws ServletException {
		String jdbcURL = getServletContext().getInitParameter("jdbcURL");
		String jdbcUsername = getServletContext().getInitParameter("jdbcUsername");
		String jdbcPassword = getServletContext().getInitParameter("jdbcPassword");
		userDAO = new UserDAO(jdbcURL, jdbcUsername, jdbcPassword);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher rd = this.getServletContext()
				.getRequestDispatcher("/views/loginView.jsp");
		rd.forward(request, response);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String username = req.getParameter("username");
		String password = req.getParameter("password");
		String rememberMe = req.getParameter("rememberMe");

		LoginForm loginForm = new LoginForm(username, password);

		// Kiểm tra tính hợp lệ của dữ liệu nhập vào
		List<String> errors = loginForm.validateLoginForm();

		// Nếu không có lỗi validate
		if (errors.isEmpty()) {
			// Tìm user trong DB
			System.out.println(username+"-"+password);
			User user = userDAO.findUser(username, password);

			// Nếu sai thông tin trong db thì bổ sung vào danh sách lỗi
			if (user == null) {

				errors.add(Constant.INCORRECT_ACCOUNT_VALIDATE_MSG);
			} else { // Đăng nhập thành công

				HttpSession session = req.getSession();
				MyUtils.storeLoginedUser(session, user);
				boolean remember="Y".equals(rememberMe);
				if(remember) {
					MyUtils.storeUserCookie(res, user);
				}else {
					MyUtils.deleteUserCookie(res);
				}
				int role=user.getRole();
				if (role==Constant.CUSTOMER_ROLE) {
					res.sendRedirect(req.getContextPath()+"/clientHome");
				} else if (role == Constant.ADMIN_ROLE) {
					res.sendRedirect(req.getContextPath()+"/adminHome");
				}
			}
		}

		if (!errors.isEmpty()) {
			req.setAttribute("errors", String.join(", ", errors));
			req.setAttribute("loginForm", loginForm);

			RequestDispatcher rd = this.getServletContext()
					.getRequestDispatcher("/views/loginView.jsp");
			rd.forward(req, res);
		}
	}

}
