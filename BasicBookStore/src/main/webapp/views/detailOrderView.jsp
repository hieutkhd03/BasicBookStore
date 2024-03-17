<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="vnua.fita.bookstore.utils.Constant"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Thông tin chi tiết hóa đơn</title>
</head>
<body>
	<jsp:include page="_header.jsp"></jsp:include>
	<jsp:include page="_menu.jsp"></jsp:include>
	<div align="center">
		<h3>CHI TIẾT HÓA ĐƠN</h3>
		<table border="1">
			<tr>
				<th align="left">Tài khoản</th>
				<th>${loginedUser.username }</th>
				<c:if
					test="${Constant.WAITTING_CONFIRM_ORDER_STATUS==orderOfCustomer.orderStatus }">
					<td rowspan="9"><img
						src="${orderOfCustomer.paymentImagePath }" alt="Transfer Image" width= "150" />
					</td>
				</c:if>
			</tr>
			<tr>
				<th align="left">Họ tên</th>
				<th>${loginedUser.fullname }</th>
			</tr>
			<tr>
				<th align="left">Số điện thoại</th>
				<th>${loginedUser.mobile }</th>
			</tr>
			<tr>
				<th align="left">Mã hóa đơn</th>
				<th>${orderOfCustomer.orderNo }</th>
			</tr>
			<tr>
				<th align="left">Ngày đặt mua</th>
				<th><fmt:formatDate value="${orderOfCustomer.orderDate }"
						pattern="dd-MM-yyyy HH:mm" /></th>
			</tr>
			<tr>
				<th align="left">Ngày xác nhận đơn</th>
				<th><fmt:formatDate
						value="${orderOfCustomer.orderApproveDate }"
						pattern="dd-MM-yyyy HH:mm" /></th>
			</tr>
			<tr>
				<th align="left">Địa chỉ nhận sách</th>
				<th>${orderOfCustomer.deliveryAddress }</th>
			</tr>
			<tr>
				<th align="left">Phương thức thanh toán</th>
				<td>${orderOfCustomer.paymentModeDescription }</td>
			</tr>
			<tr>
				<th align="left">Trạng thái đơn hàng</th>
				<td>${orderOfCustomer.orderStatusDescription }<c:if
						test="${orderOfCustomer.orderStatus!=Constant.WAITTING_CONFIRM_ORDER_STATUS }">
						&nbsp;-&nbsp;${orderOfCustomer.paymentStatusDescription }
					</c:if>
				</td>
			</tr>
		</table>
	</div>

	<div align="center">
		<h3>Các cuốn sách trong hóa đơn</h3>
		<table border="1">
			<tr>
				<th>Tiêu đề</th>
				<th>Tác giả</th>
				<th>Giá tiền</th>
				<th>Số lượng mua</th>
				<th>Tổng thành phần</th>
			</tr>
			<c:forEach items="${cartOfCustomer.cartItemList }" var="entry">
				<tr>
					<td>${entry.value.selectedBook.title }</td>
					<td>${entry.value.selectedBook.author }</td>
					<td><fmt:formatNumber type="number" maxFractionDigits="0"
							value="${entry.value.selectedBook.price }" /><sup>đ</sup></td>
					<td>${entry.value.quantity }</td>
					<td><fmt:formatNumber type="number" maxFractionDigits="0"
							value="${entry.value.selectedBook.price * entry.value.quantity }" /><sup>đ</sup>
					</td>
				</tr>

			</c:forEach>
		</table>

		<br> Tổng số tiền: <b> <span id="total"> <fmt:formatNumber
					type="number" maxFractionDigits="0"
					value="${cartOfCustomer.totalCost }" />
		</span> <sup>đ</sup>
		</b>
	</div>
	<br>
				<a href="${pageContext.request.contextPath }/clientHome">Trở về Trang Chủ</a>
	<jsp:include page="_footer.jsp"></jsp:include>
</body>
</html>