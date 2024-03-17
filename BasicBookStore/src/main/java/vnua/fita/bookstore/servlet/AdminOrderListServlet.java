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

import vnua.fita.bookstore.bean.Order;
import vnua.fita.bookstore.model.OrderDAO;
import vnua.fita.bookstore.utils.Constant;
import vnua.fita.bookstore.utils.MyUtils;

@WebServlet(urlPatterns = { "/adminOrderList/waitting", "/adminOrderList/delivering", "/adminOrderList/delivered", "/adminOrderList/reject" })
public class AdminOrderListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private OrderDAO orderDAO;

	public void init() {
		String jdbcURL = getServletContext().getInitParameter("jdbcURL");
		String jdbcPassword = getServletContext().getInitParameter("jdbcPassword");
		String jdbcUsername = getServletContext().getInitParameter("jdbcUsername");
		orderDAO = new OrderDAO(jdbcURL, jdbcUsername, jdbcPassword);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String servletPath=req.getServletPath();
		String pathInfo=MyUtils.getPathInfoFromServletPath(servletPath);
		List<Order> orders=new ArrayList<Order>();
		if(Constant.WAITTING_APPROVE_ACTION.equals(pathInfo)) {
			orders=orderDAO.getOrderList(Constant.WAITTING_CONFIRM_ORDER_STATUS);
			req.setAttribute("listType", "CHỜ XÁC NHẬN");
		}else if(Constant.DELIVERING_ACTION.equals(pathInfo)) {
			orders=orderDAO.getOrderList(Constant.DELIVERING_ORDER_STATUS);
			req.setAttribute("listType", "ĐANG CHỜ GIAO");
		}else if(Constant.DELIVERED_ACTION.equals(pathInfo)) {
			orders=orderDAO.getOrderList(Constant.DELIVERED_ORDER_STATUS);
			req.setAttribute("listType", "ĐÃ GIAO");
		}else if(Constant.REJECT_ACTION.equals(pathInfo)) {
			orders=orderDAO.getOrderList(Constant.REJECT_ORDER_STATUS);
			req.setAttribute("listType", "KHÁCH TRẢ LẠI");
		}

		req.setAttribute(Constant.ORDER_LIST_OF_CUSTOMER, orders);
		RequestDispatcher dispatcher=this.getServletContext().getRequestDispatcher("/views/adminOrderListView.jsp");
		dispatcher.forward(req, res);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<String> errors=new ArrayList<String>();
		String orderIdStr=req.getParameter("orderId");
		String confirmTypeStr=req.getParameter("confirmType");
		int orderId=-1;
		try {
			orderId=Integer.parseInt(orderIdStr);
		} catch (Exception e) {
			errors.add(Constant.ORDER_ID_INVALID_VALIDATE_MSG);
		}
		byte confirmType=-1;
		try {
			confirmType=Byte.parseByte(confirmTypeStr);
		} catch (Exception e) {
			errors.add(Constant.VALUE_INVALID_VALIDATE_MSG);
		}
		if(errors.isEmpty()) {
			boolean updateResult=false;
			if(Constant.DELIVERING_ORDER_STATUS==confirmType) {
				updateResult=orderDAO.updateOrderNo(orderId,confirmType);
			}else if(Constant.DELIVERED_ORDER_STATUS==confirmType) {
				updateResult=orderDAO.updateOrder(orderId,confirmType);
			}else if(Constant.REJECT_ORDER_STATUS==confirmType) {
				updateResult=orderDAO.updateOrder(orderId,confirmType);
			}
			if(updateResult) {
				req.setAttribute("message", Constant.UPDATE_ORDER_SUCCESS);
			}else {
				errors.add(Constant.UPDATE_ORDER_FAIL);
			}
			if(!errors.isEmpty()) {
				req.setAttribute("errors", String.join(", ", errors));
			}
			doGet(req, resp);
		}
	}
}
