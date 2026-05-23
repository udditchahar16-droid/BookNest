package com.booknest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WalletServiceImpl
 * Tests: addWallet, addMoney, update (payMoney), getById, getStatementsById,
 *        getStatements, deleteById, getWallets
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Unit Tests")
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private StatementsRepository statementsRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet mockWallet;
    private Statement mockStatement;

    @BeforeEach
    void setUp() {
        mockStatement = new Statement();
        mockStatement.setStatementId(1);
        mockStatement.setTransactionType("DEPOSIT");
        mockStatement.setAmount(500.0);
        mockStatement.setDateTime(LocalDateTime.now());
        mockStatement.setTransactionRemarks("Top-up via UPI");

        mockWallet = new Wallet();
        mockWallet.setWalletId(1);
        mockWallet.setCurrentBalance(1000.0);
        mockWallet.setStatements(new ArrayList<>(List.of(mockStatement)));
    }

    // ── getWallets ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getWallets: should return all wallets")
    void getWallets_returnsList() {
        when(walletRepository.getAllWallets()).thenReturn(List.of(mockWallet));

        List<Wallet> result = walletService.getWallets();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCurrentBalance()).isEqualTo(1000.0);
    }

    // ── addWallet ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("addWallet: should persist new wallet with zero balance")
    void addWallet_persistsWallet() {
        Wallet newWallet = new Wallet(1, 0.0, new ArrayList<>());
        when(walletRepository.save(newWallet)).thenReturn(newWallet);

        Wallet result = walletService.addWallet(newWallet);

        assertThat(result.getCurrentBalance()).isEqualTo(0.0);
        verify(walletRepository).save(newWallet);
    }

    // ── addMoney (deposit) ────────────────────────────────────────────────

    @Test
    @DisplayName("addMoney: should increase wallet balance and create DEPOSIT statement")
    void addMoney_increasesBalanceAndCreatesStatement() {
        when(walletRepository.findByWalletId(1)).thenReturn(mockWallet);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.addMoney(mockWallet, 300.0, "Birthday gift");

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentBalance()).isEqualTo(1300.0);

        // Statement should be created
        assertThat(captor.getValue().getStatements())
                .anyMatch(s -> s.getTransactionType().equals("DEPOSIT") && s.getAmount() == 300.0);
    }

    @Test
    @DisplayName("addMoney: should reject negative amount")
    void addMoney_rejectsNegativeAmount() {
        assertThatThrownBy(() -> walletService.addMoney(mockWallet, -100.0, "Invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── update / payMoney (debit) ─────────────────────────────────────────

    @Test
    @DisplayName("update: should deduct from balance and create WITHDRAW statement")
    void update_deductsBalanceAndCreatesStatement() {
        when(walletRepository.findByWalletId(1)).thenReturn(mockWallet);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        walletService.update(mockWallet, 400.0, "Order #100", 100);

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentBalance()).isEqualTo(600.0);
        assertThat(captor.getValue().getStatements())
                .anyMatch(s -> s.getTransactionType().equals("WITHDRAW") && s.getAmount() == 400.0);
    }

    @Test
    @DisplayName("update: should throw when balance is insufficient")
    void update_throwsForInsufficientBalance() {
        assertThatThrownBy(() -> walletService.update(mockWallet, 9999.0, "Big purchase", 999))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient balance");
    }

    // ── getById ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById: should return wallet for valid walletId")
    void getById_returnsWallet() {
        when(walletRepository.findByWalletId(1)).thenReturn(mockWallet);

        Wallet result = walletService.getById(1);

        assertThat(result.getWalletId()).isEqualTo(1);
        assertThat(result.getCurrentBalance()).isEqualTo(1000.0);
    }

    @Test
    @DisplayName("getById: should throw for unknown walletId")
    void getById_throwsForUnknown() {
        when(walletRepository.findByWalletId(999)).thenReturn(null);
        assertThatThrownBy(() -> walletService.getById(999))
                .isInstanceOf(RuntimeException.class);
    }

    // ── getStatementsById ─────────────────────────────────────────────────

    @Test
    @DisplayName("getStatementsById: should return all statements for wallet")
    void getStatementsById_returnsStatements() {
        when(statementsRepository.findByWalletId(1)).thenReturn(List.of(mockStatement));

        List<Statement> result = walletService.getStatementsById(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransactionType()).isEqualTo("DEPOSIT");
    }

    @Test
    @DisplayName("getStatementsById: should return empty list for wallet with no transactions")
    void getStatementsById_returnsEmptyForNoTransactions() {
        when(statementsRepository.findByWalletId(99)).thenReturn(List.of());
        assertThat(walletService.getStatementsById(99)).isEmpty();
    }

    // ── getStatements ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getStatements: should return all statements across all wallets")
    void getStatements_returnsAll() {
        when(statementsRepository.findAll()).thenReturn(List.of(mockStatement));

        List<Statement> result = walletService.getStatements();

        assertThat(result).hasSize(1);
    }

    // ── deleteById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteById: should delete wallet and cascade statements")
    void deleteById_deletesWallet() {
        doNothing().when(walletRepository).deleteByWalletId(1);
        walletService.deleteById(1);
        verify(walletRepository, times(1)).deleteByWalletId(1);
    }

    // ── findByTransactionType ─────────────────────────────────────────────

    @Test
    @DisplayName("findByTransactionType: should return DEPOSIT statements only")
    void findByTransactionType_returnsDeposits() {
        when(statementsRepository.findByTransactionType("DEPOSIT")).thenReturn(List.of(mockStatement));

        List<Statement> deposits = statementsRepository.findByTransactionType("DEPOSIT");
        assertThat(deposits).allMatch(s -> s.getTransactionType().equals("DEPOSIT"));
    }

    // ── findByOrderId ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findByOrderId: should return statement linked to order")
    void findByOrderId_returnsStatement() {
        mockStatement.setOrderId(100);
        when(statementsRepository.findByOrderId(100)).thenReturn(List.of(mockStatement));

        List<Statement> result = statementsRepository.findByOrderId(100);
        assertThat(result.get(0).getOrderId()).isEqualTo(100);
    }
}
