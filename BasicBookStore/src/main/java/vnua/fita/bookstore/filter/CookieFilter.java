package vnua.fita.bookstore.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import vnua.fita.bookstore.bean.User;
import vnua.fita.bookstore.model.UserDAO;
import vnua.fita.bookstore.utils.MyUtils;

@WebFilter(filterName = "cookieFilter", urlPatterns = {"/*"})
public class CookieFilter implements Filter {
	
	private UserDAO userDAO;
	
	public void init(FilterConfig filterConfig) {
		String jdbcURL = filterConfig.getServletContext().getInitParameter("jdbcURL");
		String jdbcPassword = filterConfig.getServletContext().getInitParameter("jdbcPassword");
		String jdbcUsername = filterConfig.getServletContext().getInitParameter("jdbcUsername");
		userDAO = new UserDAO(jdbcURL, jdbcUsername, jdbcPassword);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpSession session = request.getSession();
		User userInSession = MyUtils.getLoginedUser(session);
		if(userInSession != null) {
			session.setAttribute("CHECKED_COOKIE", "CHECKED");
			chain.doFilter(request, res);
			return;
		}
		
		String checked = (String)session.getAttribute("CHECKED_COOKIE");
		if(checked == null) {
			String userName = MyUtils.getUserNameInCookie(request);
			if(userName != null && !userName.isEmpty()) {
				User user = userDAO.findUser(userName);
				if(user != null) {
					String token = MyUtils.getTokenInCookie(request);
					if(token.equals(MyUtils.createTokenFromUserInfo(user))) {
						MyUtils.storeLoginedUser(session, user);
						session.setAttribute("CHECKED_COOKIE", "CHECKED");
					}
				}
			}
		}
		chain.doFilter(request, res);
	}
}
