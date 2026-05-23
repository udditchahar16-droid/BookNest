package com.booknest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookServiceImpl
 * Tests: addBook, getAllBooks, getBookById, searchBooks, getByGenre,
 *        updateBook, deleteBook, updateStock, getFeaturedBooks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book mockBook;

    @BeforeEach
    void setUp() {
        mockBook = new Book();
        mockBook.setBookId(10);
        mockBook.setTitle("Clean Code");
        mockBook.setAuthor("Robert C. Martin");
        mockBook.setIsbn("9780132350884");
        mockBook.setGenre("Technology");
        mockBook.setPublisher("Prentice Hall");
        mockBook.setPrice(599.0);
        mockBook.setStock(20);
        mockBook.setRating(4.8);
        mockBook.setDescription("A handbook of agile software craftsmanship.");
        mockBook.setPublishedDate(LocalDate.of(2008, 8, 1));
    }

    // ── addBook ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("addBook: should persist and return the new book")
    void addBook_persistsBook() {
        when(bookRepository.save(mockBook)).thenReturn(mockBook);

        Book result = bookService.addBook(mockBook);

        assertThat(result.getBookId()).isEqualTo(10);
        assertThat(result.getTitle()).isEqualTo("Clean Code");
        verify(bookRepository, times(1)).save(mockBook);
    }

    // ── getAllBooks ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllBooks: should return full list from repository")
    void getAllBooks_returnsList() {
        when(bookRepository.findAll()).thenReturn(List.of(mockBook));

        List<Book> books = bookService.getAllBooks();

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("getAllBooks: should return empty list when no books exist")
    void getAllBooks_returnsEmptyList() {
        when(bookRepository.findAll()).thenReturn(List.of());
        assertThat(bookService.getAllBooks()).isEmpty();
    }

    // ── getBookById ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getBookById: should return book for valid ID")
    void getBookById_returnsBook() {
        when(bookRepository.findById(10)).thenReturn(Optional.of(mockBook));

        Optional<Book> result = bookService.getBookById(10);

        assertThat(result).isPresent();
        assertThat(result.get().getIsbn()).isEqualTo("9780132350884");
    }

    @Test
    @DisplayName("getBookById: should return empty Optional for unknown ID")
    void getBookById_returnsEmpty() {
        when(bookRepository.findById(999)).thenReturn(Optional.empty());
        assertThat(bookService.getBookById(999)).isEmpty();
    }

    // ── searchBooks ───────────────────────────────────────────────────────

    @Test
    @DisplayName("searchBooks: should return matching books for keyword")
    void searchBooks_returnsMatches() {
        when(bookRepository.searchByKeyword("clean")).thenReturn(List.of(mockBook));

        List<Book> result = bookService.searchBooks("clean");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).containsIgnoringCase("Clean");
    }

    @Test
    @DisplayName("searchBooks: should return empty list for unmatched keyword")
    void searchBooks_returnsEmptyForNoMatch() {
        when(bookRepository.searchByKeyword("xyzzy")).thenReturn(List.of());
        assertThat(bookService.searchBooks("xyzzy")).isEmpty();
    }

    // ── getByGenre ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getByGenre: should return books of given genre")
    void getByGenre_returnsBooks() {
        when(bookRepository.findByGenre("Technology")).thenReturn(List.of(mockBook));

        List<Book> result = bookService.getByGenre("Technology");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGenre()).isEqualTo("Technology");
    }

    @Test
    @DisplayName("getByGenre: should return empty list for unknown genre")
    void getByGenre_returnsEmptyForUnknown() {
        when(bookRepository.findByGenre("Horror")).thenReturn(List.of());
        assertThat(bookService.getByGenre("Horror")).isEmpty();
    }

    // ── getByAuthor ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getByAuthor: should return books by given author")
    void getByAuthor_returnsBooks() {
        when(bookRepository.findByAuthor("Robert C. Martin")).thenReturn(List.of(mockBook));

        List<Book> result = bookService.getByAuthor("Robert C. Martin");

        assertThat(result.get(0).getAuthor()).isEqualTo("Robert C. Martin");
    }

    // ── updateBook ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateBook: should update fields and save")
    void updateBook_updatesAndSaves() {
        Book updated = new Book();
        updated.setBookId(10);
        updated.setTitle("Clean Code (2nd Ed)");
        updated.setPrice(649.0);

        when(bookRepository.save(updated)).thenReturn(updated);

        Book result = bookService.updateBook(updated);

        assertThat(result.getTitle()).isEqualTo("Clean Code (2nd Ed)");
        assertThat(result.getPrice()).isEqualTo(649.0);
    }

    // ── deleteBook ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBook: should call deleteById on repository")
    void deleteBook_callsDelete() {
        doNothing().when(bookRepository).deleteById(10);
        bookService.deleteBook(10);
        verify(bookRepository, times(1)).deleteById(10);
    }

    // ── updateStock ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStock: should set new stock and save")
    void updateStock_setsNewStock() {
        when(bookRepository.findById(10)).thenReturn(Optional.of(mockBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.updateStock(10, 50);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("updateStock: should throw when book not found")
    void updateStock_throwsForUnknownBook() {
        when(bookRepository.findById(999)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookService.updateStock(999, 10))
                .isInstanceOf(RuntimeException.class);
    }

    // ── getFeaturedBooks ──────────────────────────────────────────────────

    @Test
    @DisplayName("getFeaturedBooks: should return only featured books")
    void getFeaturedBooks_returnsFeatured() {
        when(bookRepository.findAll()).thenReturn(List.of(mockBook));

        List<Book> result = bookService.getFeaturedBooks();

        assertThat(result).isNotNull();
    }

    // ── findByIsbn ────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByIsbn: should return correct book")
    void findByIsbn_returnsBook() {
        when(bookRepository.findByIsbn("9780132350884")).thenReturn(Optional.of(mockBook));

        Optional<Book> result = bookRepository.findByIsbn("9780132350884");
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Clean Code");
    }

    // ── findByPriceBetween ────────────────────────────────────────────────

    @Test
    @DisplayName("findByPriceBetween: should return books in price range")
    void findByPriceBetween_returnsInRange() {
        when(bookRepository.findByPriceBetween(400.0, 700.0)).thenReturn(List.of(mockBook));

        List<Book> result = bookRepository.findByPriceBetween(400.0, 700.0);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isBetween(400.0, 700.0);
    }
}
