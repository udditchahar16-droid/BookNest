package com.booknest;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int orderId;
    private int userId;
    private LocalDate orderDate;
    private double amountPaid;
    private String modeOfPayment;
    private String orderStatus;
    private int quantity;
    private int bookId;
    private String bookTitle;
    @ManyToOne(cascade = CascadeType.PERSIST) private Address address;
    public Order() {}
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public String getModeOfPayment() { return modeOfPayment; }
    public void setModeOfPayment(String modeOfPayment) { this.modeOfPayment = modeOfPayment; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}
