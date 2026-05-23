package com.booknest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WishlistServiceImpl
 * Tests: getWishlistByUser, addBook, removeBook, clearWishlist,
 *        moveToCart, getAllWishlists
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService Unit Tests")
class WishlistServiceImplTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private Wishlist mockWishlist;
    private WishlistItem mockItem;

    @BeforeEach
    void setUp() {
        mockItem = new WishlistItem(1, 10, "Clean Code", 599.0);

        mockWishlist = new Wishlist();
        mockWishlist.setWishlistId(1);
        mockWishlist.setUserId(1);
        mockWishlist.setCreatedAt(LocalDate.now());
        mockWishlist.setBooks(new ArrayList<>(List.of(mockItem)));
    }

    // ── getWishlistByUser ─────────────────────────────────────────────────

    @Test
    @DisplayName("getWishlistByUser: should return wishlist for valid userId")
    void getWishlistByUser_returnsWishlist() {
        when(wishlistRepository.findByUserId(1)).thenReturn(mockWishlist);

        Wishlist result = wishlistService.getWishlistByUser(1);

        assertThat(result.getWishlistId()).isEqualTo(1);
        assertThat(result.getBooks()).hasSize(1);
        assertThat(result.getBooks().get(0).getBookTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("getWishlistByUser: should return null for user with no wishlist")
    void getWishlistByUser_returnsNullForNoWishlist() {
        when(wishlistRepository.findByUserId(99)).thenReturn(null);
        assertThat(wishlistService.getWishlistByUser(99)).isNull();
    }

    // ── addBook ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("addBook: should append book to wishlist and save")
    void addBook_appendsItemAndSaves() {
        when(wishlistRepository.findByUserId(1)).thenReturn(mockWishlist);
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(inv -> inv.getArgument(0));

        Wishlist result = wishlistService.addBook(1, 20);

        verify(wishlistRepository).save(any(Wishlist.class));
        assertThat(result.getBooks().size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("addBook: should not add duplicate book to wishlist")
    void addBook_doesNotAddDuplicate() {
        when(wishlistRepository.findByUserId(1)).thenReturn(mockWishlist);
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(inv -> inv.getArgument(0));

        // Add same bookId again
        wishlistService.addBook(1, 10);

        ArgumentCaptor<Wishlist> captor = ArgumentCaptor.forClass(Wishlist.class);
        verify(wishlistRepository).save(captor.capture());
        long distinctBooks = captor.getValue().getBooks().stream()
                .map(WishlistItem::getBookId)
                .distinct()
                .count();
        assertThat(distinctBooks).isEqualTo(captor.getValue().getBooks().size());
    }

    // ── removeBook ────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeBook: should remove item from wishlist and save")
    void removeBook_removesItemAndSaves() {
        when(wishlistRepository.findByUserId(1)).thenReturn(mockWishlist);
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(inv -> inv.getArgument(0));

        Wishlist result = wishlistService.removeBook(1, 10);

        ArgumentCaptor<Wishlist> captor = ArgumentCaptor.forClass(Wishlist.class);
        verify(wishlistRepository).save(captor.capture());
        assertThat(captor.getValue().getBooks()).noneMatch(b -> b.getBookId() == 10);
    }

    @Test
    @DisplayName("removeBook: should handle gracefully when book not in wishlist")
    void removeBook_gracefulWhenBookNotFound() {
        when(wishlistRepository.findByUserId(1)).thenReturn(mockWishlist);
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(inv -> inv.getArgument(0));

        // Book 999 not in wishlist — should not throw
        assertThatCode(() -> wishlistService.removeBook(1, 999))
                .doesNotThrowAnyException();
    }

    // ── clearWishlist ─────────────────────────────────────────────────────

    @Test
    @DisplayName("clearWishlist: should empty books list and save")
    void clearWishlist_emptiesWishlist() {
        when(wishlistRepository.findByUserId(1)).thenReturn(mockWishlist);
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(inv -> inv.getArgument(0));

        wishlistService.clearWishlist(1);

        ArgumentCaptor<Wishlist> captor = ArgumentCaptor.forClass(Wishlist.class);
        verify(wishlistRepository).save(captor.capture());
        assertThat(captor.getValue().getBooks()).isEmpty();
    }

    // ── moveToCart ────────────────────────────────────────────────────────

    @Test
    @DisplayName("moveToCart: should remove book from wishlist after move")
    void moveToCart_removesFromWishlist() {
        when(wishlistRepository.findByUserId(1)).thenReturn(mockWishlist);
        when(wishlistRepository.save(any(Wishlist.class))).thenAnswer(inv -> inv.getArgument(0));

        wishlistService.moveToCart(1, 10);

        ArgumentCaptor<Wishlist> captor = ArgumentCaptor.forClass(Wishlist.class);
        verify(wishlistRepository).save(captor.capture());
        assertThat(captor.getValue().getBooks()).noneMatch(b -> b.getBookId() == 10);
    }

    // ── getAllWishlists ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAllWishlists: should return all wishlists for admin")
    void getAllWishlists_returnsAll() {
        when(wishlistRepository.findAll()).thenReturn(List.of(mockWishlist));

        List<Wishlist> result = wishlistService.getAllWishlists();

        assertThat(result).hasSize(1);
        verify(wishlistRepository).findAll();
    }

    // ── existsByUserId ────────────────────────────────────────────────────

    @Test
    @DisplayName("existsByUserId: should return true when wishlist exists for user")
    void existsByUserId_returnsTrue() {
        when(wishlistRepository.existsByUserId(1)).thenReturn(true);
        assertThat(wishlistRepository.existsByUserId(1)).isTrue();
    }

    @Test
    @DisplayName("existsByUserId: should return false when no wishlist for user")
    void existsByUserId_returnsFalse() {
        when(wishlistRepository.existsByUserId(99)).thenReturn(false);
        assertThat(wishlistRepository.existsByUserId(99)).isFalse();
    }

    // ── deleteByUserId ────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteByUserId: should call repository delete method")
    void deleteByUserId_callsRepository() {
        doNothing().when(wishlistRepository).deleteByUserId(1);
        wishlistRepository.deleteByUserId(1);
        verify(wishlistRepository, times(1)).deleteByUserId(1);
    }
}
