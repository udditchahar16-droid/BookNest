package com.booknest;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Entity @Table(name = "wishlists")
public class Wishlist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int wishlistId;
    private int userId;
    private LocalDate createdAt;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "wishlist_id")
    private List<WishlistItem> books = new ArrayList<>();
    public Wishlist() { this.createdAt = LocalDate.now(); }
    public int getWishlistId() { return wishlistId; }
    public void setWishlistId(int wishlistId) { this.wishlistId = wishlistId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public List<WishlistItem> getBooks() { return books; }
    public void setBooks(List<WishlistItem> books) { this.books = books; }
}
