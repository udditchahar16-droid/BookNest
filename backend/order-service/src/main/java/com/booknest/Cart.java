package com.booknest;

/**
 * Cart DTO — Angular checkout se aane wala request body.
 * Fields:
 *   userId       — logged-in user
 *   totalPrice   — cart total (amountPaid ke liye)
 *   bookId       — primary book (first item)
 *   bookTitle    — book name (order record ke liye)
 *   quantity     — quantity
 *   modeOfPayment— COD / WALLET / RAZORPAY
 *   address      — delivery address (order ke saath save hogi)
 */
public class Cart {
    private int userId;
    private double totalPrice;
    private int bookId;
    private String bookTitle;
    private int quantity;
    private String modeOfPayment;
    private Address address;

    public Cart() {}

    public int getUserId()               { return userId; }
    public void setUserId(int v)         { this.userId = v; }

    public double getTotalPrice()        { return totalPrice; }
    public void setTotalPrice(double v)  { this.totalPrice = v; }

    // Angular bhejta hai amountPaid — totalPrice ka alias
    public double getAmountPaid()        { return totalPrice; }
    public void setAmountPaid(double v)  { this.totalPrice = v; }

    public int getBookId()               { return bookId; }
    public void setBookId(int v)         { this.bookId = v; }

    public String getBookTitle()         { return bookTitle; }
    public void setBookTitle(String v)   { this.bookTitle = v; }

    public int getQuantity()             { return quantity; }
    public void setQuantity(int v)       { this.quantity = v; }

    public String getModeOfPayment()     { return modeOfPayment; }
    public void setModeOfPayment(String v){ this.modeOfPayment = v; }

    public Address getAddress()          { return address; }
    public void setAddress(Address v)    { this.address = v; }
}
