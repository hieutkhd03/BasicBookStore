package vnua.fita.bookstore.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import vnua.fita.bookstore.bean.Cart;
import vnua.fita.bookstore.bean.CartItem;
import vnua.fita.bookstore.bean.User;

public class MyUtils {
	public static void storeLoginedUser(HttpSession session, User loginedUser) {
		session.setAttribute(Constant.LOGINED_USER, loginedUser);
		System.out.println(loginedUser.getUsername()+" - "+loginedUser.getAddress());
	}
	
	public static User getLoginedUser(HttpSession session) {
		User user=(User) session.getAttribute(Constant.LOGINED_USER);
		return user;
	}

	// lưu trữ thông tin giỏ hàng vào Session
	public static void storeCart(HttpSession session, Cart cart) {
		// trên JSP có thể truy cập thông qua ${loginedUser}
		session.setAttribute("cartOfCustomer", cart);
	}

	// lấy thông tin giỏ hàng lưu trữ trong Session
	public static Cart getCartOfCustomer(HttpSession session) {
		Cart cartOfCustomer = (Cart) session.getAttribute("cartOfCustomer");
		return cartOfCustomer;
	}
	
	//Lấy thông tin từ 1 đường dẫn Servlet
	public static String getPathInfoFromServletPath(String path) {
		if (path == null || path.isEmpty()) {
			return ""; // Hoặc có thể ném một ngoại lệ
		}

		String[] result = path.split("/");
		if (result.length == 0) {
			return "";
		}

		return result[result.length - 1];
	}

	//thêm nhãn thời gian hiện tại cho tên ảnh
	public static String getTimeLabel() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy hh_mm");
		return sdf.format(new Date());
	}

	//phần mở rộng sau tên ảnh 
	public static String extracFileExtension(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		int indexOfDot = contentDisp.lastIndexOf(".");
		return contentDisp.substring(indexOfDot, contentDisp.length() - 1); // trả về .jpg
	}

	
	public static File getFolderUpload(String appPath, String folderName) {
		File folderUpload = new File(appPath + File.separator + folderName);
		if (!folderUpload.exists()) {
			folderUpload.mkdirs();
		}
		return folderUpload;
	}

	// Date -> String
	public static String convertDateToString(Date date) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	public static String convertDateToStringVn(Date date) {
		DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		return sdf.format(date);
	}

	//cập nhật giỏ hàng của một khách hàng trong session
	public static void updateCartOfCustomer(HttpSession session, Map<Integer, CartItem> cartItemList) {
		Cart cart = getCartOfCustomer(session);
		cart.setCartItemList(cartItemList);
		session.setAttribute(Constant.CART_OF_CUSTOMER, cart);
	}

	//xóa giỏi hàng
	public static void deleteCart(HttpSession session) {
		session.removeAttribute(Constant.CART_OF_CUSTOMER);
	}

	// công thức tạo OrderNo từ OrderId
	public static String createOrderNo(int orderId) {
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
		int code = orderId % 100;
		return sdf.format(new Date())+code;
	}

	//trừ đi 3 tháng từ ngày được cung cấp và trả về kết quả
	public static Date subtractFromDate(int months, Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, -3);
		return c.getTime();
	}

	//thêm một phần đuôi cố định vào cuối chuỗi ngày
	public static String attachTailToDate(String date) {
		return date + " 00:00:00";
	}
	
	//lưu trữ thông tin người dùng vào cookie
	public static void storeUserCookie(HttpServletResponse response, User user) {
		System.out.println("Store user cookie");
		Cookie cookieUsername=new Cookie(Constant.USERNAME_STORE_IN_COOKIE_OF_BOOKSTORE, user.getUsername());
		cookieUsername.setMaxAge(24*60*60); // age = 1 ngày
		response.addCookie(cookieUsername);
		
		Cookie cookieToken=new Cookie(Constant.TOKEN_STORE_IN_COOKIE_OF_BOOKSTORE, MyUtils.createTokenFromUserInfo(user));
		cookieUsername.setMaxAge(24*60*60);// age = 1 ngày
		response.addCookie(cookieToken);
	}
	
	//xóa cookie
	public static void deleteUserCookie(HttpServletResponse response) {
		Cookie cookieUsername=new Cookie(Constant.USERNAME_STORE_IN_COOKIE_OF_BOOKSTORE,null);
		cookieUsername.setMaxAge(0);// age về 0s (cookie này hết hiệu lực ngay lập tức )
		response.addCookie(cookieUsername);
		
		Cookie cookieToken=new Cookie(Constant.TOKEN_STORE_IN_COOKIE_OF_BOOKSTORE,null);
		cookieUsername.setMaxAge(0);// age về 0s (cookie này hết hiệu lực ngay lập tức )
		response.addCookie(cookieToken);
	}
	
	//tạo 1 chuỗi mã hóa để lưu vào cookie , dành cho xác thực về sau 
	public static String createTokenFromUserInfo(User user) {
		return user.getUsername()+Constant.SECRET_STRING+user.getPassword();
	}
	
	//lấy về username lưu trong cookie
	public static String getUserNameInCookie(HttpServletRequest request) {
		Cookie[] cookies=request.getCookies();
		if(cookies!=null) {
			for (Cookie cookie : cookies) {
				// Nếu trùng tên (key) đã lưu , trả về giá trị
				if(Constant.USERNAME_STORE_IN_COOKIE_OF_BOOKSTORE.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
	
	//Lấy ra chuỗi mã hóa lưu trong cookie
	public static  String getTokenInCookie (HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				// Nếu trùng tên (key) đã lưu , trả về giá trị
				if (Constant.TOKEN_STORE_IN_COOKIE_OF_BOOKSTORE.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	// lấy ra đường dẫn Servlet
	public static String getServletPath(String servletPathFull) {
		if (servletPathFull == null || servletPathFull.isEmpty()) {
			return ""; // Hoặc có thể ném một ngoại lệ
		}

		String[] result = servletPathFull.split("/");
		if (result.length == 0) {
			return "";
		}
		return "/"+result[1];
	}
}
