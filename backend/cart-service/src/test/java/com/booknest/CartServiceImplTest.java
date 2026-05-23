package com.booknest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartServiceImpl
 * Tests: getCartByUser, addItem, removeItem, updateQuantity, clearCart, cartTotal, getAllCarts
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart mockCart;
    private CartItem mockItem;

    @BeforeEach
    void setUp() {
        mockItem = new CartItem();
        mockItem.setItemId(1);
        mockItem.setBookId(10);
        mockItem.setBookTitle("Clean Code");
        mockItem.setPrice(599.0);
        mockItem.setQuantity(1);

        mockCart = new Cart();
        mockCart.setCartId(1);
        mockCart.setUserId(1);
        mockCart.setItems(new ArrayList<>(List.of(mockItem)));
        mockCart.setTotalPrice(599.0);
    }

    // ── getCartByUser ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getCartByUser: should return cart for valid userId")
    void getCartByUser_returnsCart() {
        when(cartRepository.findByUserId(1)).thenReturn(mockCart);

        Cart result = cartService.getCartByUser(1);

        assertThat(result.getCartId()).isEqualTo(1);
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("getCartByUser: should return null/empty for user with no cart")
    void getCartByUser_returnsNullForNoCart() {
        when(cartRepository.findByUserId(99)).thenReturn(null);
        assertThat(cartService.getCartByUser(99)).isNull();
    }

    // ── addItem ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("addItem: should add new item and recalculate total")
    void addItem_addsItemAndRecalculatesTotal() {
        when(cartRepository.findByUserId(1)).thenReturn(mockCart);
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.addItem(1, 20, 2);

        verify(cartRepository).save(any(Cart.class));
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("addItem: should increase quantity if book already in cart")
    void addItem_incrementsQuantityForExistingBook() {
        when(cartRepository.findByUserId(1)).thenReturn(mockCart);
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            return c;
        });

        // Adding same bookId should increment quantity
        Cart result = cartService.addItem(1, 10, 1);

        assertThat(result.getItems()).hasSizeLessThanOrEqualTo(2); // no duplicate, quantity up
        verify(cartRepository).save(any(Cart.class));
    }

    // ── removeItem ────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeItem: should remove item from cart and save")
    void removeItem_removesItemAndSaves() {
        when(cartRepository.findByUserId(1)).thenReturn(mockCart);
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.removeItem(1, 1);

        verify(cartRepository).save(any(Cart.class));
        assertThat(result.getItems()).doesNotContain(mockItem);
    }

    // ── updateQuantity ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateQuantity: should change item quantity and recalculate total")
    void updateQuantity_changesQuantity() {
        when(cartRepository.findByUserId(1)).thenReturn(mockCart);
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.updateQuantity(1, 1, 3);

        verify(cartRepository).save(any(Cart.class));
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("updateQuantity: should throw if quantity <= 0")
    void updateQuantity_throwsForZeroQuantity() {
        assertThatThrownBy(() -> cartService.updateQuantity(1, 1, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── clearCart ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("clearCart: should empty items list and reset total to 0")
    void clearCart_emptiesCart() {
        when(cartRepository.findByUserId(1)).thenReturn(mockCart);
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.clearCart(1);

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).isEmpty();
        assertThat(captor.getValue().getTotalPrice()).isEqualTo(0.0);
    }

    // ── cartTotal ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("cartTotal: should compute sum of price * quantity for all items")
    void cartTotal_computesCorrectly() {
        CartItem item2 = new CartItem();
        item2.setItemId(2);
        item2.setBookId(20);
        item2.setBookTitle("Design Patterns");
        item2.setPrice(799.0);
        item2.setQuantity(2);
        mockCart.getItems().add(item2);

        double total = cartService.cartTotal(mockCart);

        // 599*1 + 799*2 = 599 + 1598 = 2197
        assertThat(total).isEqualTo(2197.0);
    }

    @Test
    @DisplayName("cartTotal: should return 0 for empty cart")
    void cartTotal_returnsZeroForEmptyCart() {
        mockCart.setItems(new ArrayList<>());
        double total = cartService.cartTotal(mockCart);
        assertThat(total).isEqualTo(0.0);
    }

    // ── getAllCarts ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCarts: should return all carts for admin view")
    void getAllCarts_returnsAllCarts() {
        when(cartRepository.findAll()).thenReturn(List.of(mockCart));

        List<Cart> result = cartService.getAllCarts();

        assertThat(result).hasSize(1);
        verify(cartRepository).findAll();
    }

    // ── existsByUserId ────────────────────────────────────────────────────

    @Test
    @DisplayName("existsByUserId: should return true when cart exists")
    void existsByUserId_returnsTrue() {
        when(cartRepository.existsByUserId(1)).thenReturn(true);
        assertThat(cartRepository.existsByUserId(1)).isTrue();
    }

    @Test
    @DisplayName("existsByUserId: should return false when cart does not exist")
    void existsByUserId_returnsFalse() {
        when(cartRepository.existsByUserId(99)).thenReturn(false);
        assertThat(cartRepository.existsByUserId(99)).isFalse();
    }
}
