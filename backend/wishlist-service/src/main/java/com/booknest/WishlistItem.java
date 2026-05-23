package com.booknest;
import jakarta.persistence.*;
@Entity @Table(name = "wishlist_items")
public class WishlistItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int itemId;
    private int bookId;
    private String bookTitle;
    private double bookPrice;
    public WishlistItem() {}
    public WishlistItem(int itemId, int bookId, String bookTitle, double bookPrice) { this.itemId=itemId; this.bookId=bookId; this.bookTitle=bookTitle; this.bookPrice=bookPrice; }
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public double getBookPrice() { return bookPrice; }
    public void setBookPrice(double bookPrice) { this.bookPrice = bookPrice; }
}
