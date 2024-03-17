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
import javax.servlet.http.HttpServletResponse;

import vnua.fita.bookstore.bean.User;
import vnua.fita.bookstore.config.SercurityConfig;
import vnua.fita.bookstore.utils.MyUtils;

@WebFilter(filterName = "sercurityFilter", urlPatterns = {"/*"})
public class SercurityFilter implements Filter{

	
	public SercurityFilter() {
		
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		
	}
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		String servletPathFull = request.getServletPath();
		String servletPath = MyUtils.getServletPath(servletPathFull);
		
		if(!SercurityConfig.checkDenyUrlPattern(servletPath)) {
			chain.doFilter(request, response);
			return;
			
		}
		User loginedUser = MyUtils.getLoginedUser(request.getSession());
		boolean isPermission = false;
		if (loginedUser != null) {
			int role = loginedUser.getRole();
			isPermission = SercurityConfig.checkPermission((byte) role, servletPath);
		}
		if(!isPermission) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}
		chain.doFilter(request, response);
	}
	
	@Override
	public void destroy() {
		
	}
}
