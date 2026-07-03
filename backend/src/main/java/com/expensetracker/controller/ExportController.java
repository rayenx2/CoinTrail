package com.expensetracker.controller;

import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Provides CSV export of transactions for a given date range.
 *
 * <p>GET /api/export/csv?startDate=YYYY-MM-DD&amp;endDate=YYYY-MM-DD
 *
 * <p>The exported file is compatible with Excel, Google Sheets, and accounting
 * import tools (Lexoffice, DATEV-CSV format columns).
 */
@RestController
@RequestMapping("/api/export")
@Tag(name = "Export", description = "Export transactions to CSV for use in Excel, Google Sheets, or accounting software")
@SecurityRequirement(name = "bearerAuth")
public class ExportController {

    private static final String CSV_HEADER =
            "ID,Date,Type,Category,Description,Amount (EUR),Running Balance (EUR)";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/csv", produces = "text/csv")
    @Operation(
        summary = "Export transactions to CSV",
        description = "Downloads a UTF-8 CSV file containing all transactions in the requested date range. " +
                      "Columns: ID, Date, Type, Category, Description, Amount, Running Balance. " +
                      "Compatible with Excel, Google Sheets, and accounting import tools."
    )
    @ApiResponse(responseCode = "200", description = "CSV file generated and returned as an attachment")
    public ResponseEntity<byte[]> exportCsv(
            @Parameter(description = "Start date (inclusive), format YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive), format YYYY-MM-DD")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(hidden = true) Authentication authentication) throws IOException {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetween(user.getId(), startDate, endDate);

        // Sort by date ascending for a clean ledger-style export
        transactions.sort((a, b) -> a.getTransactionDate().compareTo(b.getTransactionDate()));

        byte[] csvBytes = buildCsv(transactions);

        String filename = String.format("cointrail_%s_%s.csv", startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }

    private byte[] buildCsv(List<Transaction> transactions) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // BOM for Excel UTF-8 compatibility
        bos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(bos, StandardCharsets.UTF_8), false)) {

            writer.println(CSV_HEADER);

            java.math.BigDecimal runningBalance = java.math.BigDecimal.ZERO;

            for (Transaction tx : transactions) {
                boolean isExpense = tx.getType().name().equalsIgnoreCase("EXPENSE");
                if (isExpense) {
                    runningBalance = runningBalance.subtract(tx.getAmount());
                } else {
                    runningBalance = runningBalance.add(tx.getAmount());
                }

                String signedAmount = isExpense
                        ? "-" + tx.getAmount().toPlainString()
                        : tx.getAmount().toPlainString();

                writer.printf("%d,%s,%s,%s,%s,%s,%s%n",
                        tx.getId(),
                        tx.getTransactionDate().format(DATE_FMT),
                        tx.getType().name(),
                        escapeCsv(tx.getCategory() != null ? tx.getCategory().getName() : ""),
                        escapeCsv(tx.getDescription() != null ? tx.getDescription() : ""),
                        signedAmount,
                        runningBalance.toPlainString()
                );
            }
        }

        return bos.toByteArray();
    }

    /**
     * Wraps a CSV field in double quotes if it contains commas, quotes, or newlines.
     * Internal double quotes are escaped by doubling them (RFC 4180).
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
