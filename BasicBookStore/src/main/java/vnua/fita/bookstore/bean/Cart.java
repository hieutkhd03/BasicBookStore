package vnua.fita.bookstore.bean;

import java.util.Map;
import java.util.HashMap;

public class Cart {
	
	//Danh sách sách chọn đưa vào giỏi hàng
	private Map<Integer, CartItem> cartItemList; //hashmap
	private float totalCost; // tổng số tiền của giỏ hàng
	private String paymentMode; // hình thức thanh toán
	private boolean paymentStatus; // trạng thái thánh toán
	
	public Cart() {
		cartItemList = new HashMap<Integer, CartItem>();
		totalCost = 0;
	}
	
	// Thêm 1 mặt hàng vào giỏi hàng ( nếu trùng thì sẽ thay thế )
	public void addCartItemToCart (int bookId, CartItem cartItem) {
		CartItem oldCartItem = cartItemList.get(bookId);
		
		// Nếu trừng lặp mặt hàng sẽ thay thế ở dưới (bớt tiền cũ trong giỏi hàng đi)
		if(oldCartItem != null) {
			totalCost -= oldCartItem.getQuantity() * oldCartItem.getSelectedBook().getPrice();			
		}
		cartItemList.put(bookId, cartItem); //Thay thế cái cũ
		
		//Cập nhật tổng số tiền giỏ hàng
		totalCost += cartItem.getQuantity()*cartItem.getSelectedBook().getPrice();
	}
	
	// Xóa hàng trong giỏ
	public void removeCartItemFromCart(int bookId) {
		CartItem cartItem = cartItemList.get(bookId); // Lấy mặt hàng cần xóa = Id
		cartItemList.remove(bookId);
		
		//Cập nhật tổng số tiền trong giỏi hàng
		totalCost -= cartItem.getQuantity()*cartItem.getSelectedBook().getPrice();
		}

	// Get/Set
	public float getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(float totalCost) {
		this.totalCost = totalCost;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public boolean isPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(boolean paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public Map<Integer, CartItem> getCartItemList() {
		return cartItemList;
	}

	public void setCartItemList(Map<Integer, CartItem> cartItemList) {
		this.cartItemList = cartItemList;
	}
}
