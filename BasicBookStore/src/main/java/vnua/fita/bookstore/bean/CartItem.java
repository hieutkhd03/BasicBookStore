package vnua.fita.bookstore.bean;

public class CartItem {
	private Book selectedBook; // cuốn sách chọn mua
	private int quantity; // số lượng mua
	
	//Constructor
	public CartItem(Book selectedBook, int quantity) {
		super();
		this.selectedBook = selectedBook;
		this.quantity = quantity;
	}

	//Get/Set
	public Book getSelectedBook() {
		return selectedBook;
	}

	public void setSelectedBook(Book selectedBook) {
		this.selectedBook = selectedBook;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
