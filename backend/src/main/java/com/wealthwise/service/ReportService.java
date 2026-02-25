package com.wealthwise.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.wealthwise.dto.ReportSummaryResponse;
import com.wealthwise.model.Transaction;
import com.wealthwise.model.User;
import com.wealthwise.repository.TransactionRepository;
import com.wealthwise.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired private TransactionRepository txRepo;
    @Autowired private UserRepository        userRepo;

    // ── palette (used across PDF) ──────────────────────────────────────────
    private static final Color DARK_NAVY    = new Color(18,  18,  31);
    private static final Color GOLD         = new Color(200, 169, 110);
    private static final Color INCOME_GREEN = new Color(45,  138, 94);
    private static final Color EXPENSE_RED  = new Color(192, 57,  74);
    private static final Color SAVING_BLUE  = new Color(58,  126, 213);
    private static final Color INVEST_PURP  = new Color(140, 95,  204);
    private static final Color LIGHT_BG     = new Color(248, 248, 252);
    private static final Color BORDER_GRAY  = new Color(235, 235, 245);
    private static final Color TEXT_DARK    = new Color(30,  30,  50);
    private static final Color TEXT_MUTED   = new Color(110, 110, 140);

    // ── helpers ───────────────────────────────────────────────────────────
    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private List<Transaction> monthTx(String month) {
        User user = currentUser();
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();
        return txRepo.findByUserOrderByDateDesc(user).stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    private double sum(List<Transaction> txs, Transaction.TransactionType type) {
        return txs.stream()
                .filter(t -> t.getType() == type)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
    }

    private String monthLabel(YearMonth ym) {
        String m = ym.getMonth().name();
        return m.charAt(0) + m.substring(1).toLowerCase() + " " + ym.getYear();
    }

    private String escCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    // ── public API ─────────────────────────────────────────────────────────

    public ReportSummaryResponse getSummary(String month) {
        List<Transaction> txs = monthTx(month);
        YearMonth ym = YearMonth.parse(month);

        double income      = sum(txs, Transaction.TransactionType.INCOME);
        double expenses    = sum(txs, Transaction.TransactionType.EXPENSE);
        double savings     = sum(txs, Transaction.TransactionType.SAVING);
        double investments = sum(txs, Transaction.TransactionType.INVESTMENT);
        double net         = income - expenses - investments;
        double savingsRate = income > 0 ? (net / income) * 100.0 : 0.0;

        // Top 5 expense categories
        Map<String, Double> catMap = txs.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : "Other",
                        Collectors.summingDouble(t -> t.getAmount().doubleValue())));

        List<ReportSummaryResponse.CategoryEntry> topCats = catMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(e -> new ReportSummaryResponse.CategoryEntry(
                        e.getKey(), e.getValue(),
                        expenses > 0 ? e.getValue() / expenses * 100.0 : 0.0))
                .collect(Collectors.toList());

        ReportSummaryResponse r = new ReportSummaryResponse();
        r.setMonth(month);
        r.setMonthLabel(monthLabel(ym));
        r.setTotalIncome(income);
        r.setTotalExpenses(expenses);
        r.setTotalSavings(savings);
        r.setTotalInvestments(investments);
        r.setNetSavings(net);
        r.setSavingsRate(savingsRate);
        r.setTransactionCount(txs.size());
        r.setTopCategories(topCats);
        return r;
    }

    // ── PDF ────────────────────────────────────────────────────────────────

    public byte[] generatePdf(String month) throws Exception {
        ReportSummaryResponse summary = getSummary(month);
        List<Transaction> txs = monthTx(month);

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // ── Header ──
        PdfPTable hdrTable = new PdfPTable(1);
        hdrTable.setWidthPercentage(100);
        PdfPCell hdrCell = new PdfPCell();
        hdrCell.setBackgroundColor(DARK_NAVY);
        hdrCell.setPadding(22);
        hdrCell.setBorder(Rectangle.NO_BORDER);
        Paragraph brand = new Paragraph("WealthWise",
                new Font(Font.HELVETICA, 22, Font.BOLD, GOLD));
        Paragraph sub = new Paragraph("Financial Report  —  " + summary.getMonthLabel(),
                new Font(Font.HELVETICA, 13, Font.NORMAL, new Color(180, 180, 210)));
        sub.setSpacingBefore(4);
        Paragraph gen = new Paragraph(
                "Generated " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(100, 100, 140)));
        gen.setSpacingBefore(6);
        hdrCell.addElement(brand);
        hdrCell.addElement(sub);
        hdrCell.addElement(gen);
        hdrTable.addCell(hdrCell);
        doc.add(hdrTable);
        doc.add(Chunk.NEWLINE);

        // ── Summary cards (2×2 grid) ──
        addSectionTitle(doc, "Financial Summary");

        PdfPTable row1 = new PdfPTable(2);
        row1.setWidthPercentage(100);
        row1.setSpacingAfter(6);
        addSummaryCard(row1, "Total Income",
                String.format("$%.2f", summary.getTotalIncome()), INCOME_GREEN);
        addSummaryCard(row1, "Total Expenses",
                String.format("$%.2f", summary.getTotalExpenses()), EXPENSE_RED);
        doc.add(row1);

        PdfPTable row2 = new PdfPTable(2);
        row2.setWidthPercentage(100);
        row2.setSpacingAfter(6);
        addSummaryCard(row2, "Net Savings",
                String.format("$%.2f", summary.getNetSavings()),
                summary.getNetSavings() >= 0 ? INCOME_GREEN : EXPENSE_RED);
        addSummaryCard(row2, "Savings Rate",
                String.format("%.1f%%", summary.getSavingsRate()),
                summary.getSavingsRate() >= 20 ? SAVING_BLUE : EXPENSE_RED);
        doc.add(row2);

        PdfPTable row3 = new PdfPTable(2);
        row3.setWidthPercentage(100);
        row3.setSpacingAfter(16);
        addSummaryCard(row3, "Savings & Transfers",
                String.format("$%.2f", summary.getTotalSavings()), SAVING_BLUE);
        addSummaryCard(row3, "Investments",
                String.format("$%.2f", summary.getTotalInvestments()), INVEST_PURP);
        doc.add(row3);

        // ── Category breakdown ──
        if (!summary.getTopCategories().isEmpty()) {
            addSectionTitle(doc, "Top Spending Categories");

            PdfPTable catTable = new PdfPTable(new float[]{3f, 2f, 1.5f});
            catTable.setWidthPercentage(100);
            catTable.setSpacingAfter(16);
            addTableHeader(catTable, new String[]{"Category", "Amount", "% of Expenses"});

            boolean alt = false;
            for (ReportSummaryResponse.CategoryEntry ce : summary.getTopCategories()) {
                Color bg = alt ? LIGHT_BG : Color.WHITE;
                addRow(catTable, bg, TEXT_MUTED,
                        ce.getCategory(),
                        String.format("$%.2f", ce.getAmount()),
                        String.format("%.1f%%", ce.getPct()));
                alt = !alt;
            }
            doc.add(catTable);
        }

        // ── Transactions table ──
        addSectionTitle(doc, "Transaction Details  (" + summary.getTransactionCount() + " transactions)");

        PdfPTable txTable = new PdfPTable(new float[]{1.5f, 1.5f, 1.8f, 3f, 1.8f});
        txTable.setWidthPercentage(100);
        txTable.setSpacingAfter(20);
        addTableHeader(txTable, new String[]{"Date", "Type", "Category", "Description", "Amount"});

        if (txs.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("No transactions for this period.",
                    new Font(Font.HELVETICA, 9, Font.ITALIC, TEXT_MUTED)));
            empty.setColspan(5);
            empty.setPadding(12);
            empty.setBorder(Rectangle.NO_BORDER);
            empty.setBackgroundColor(LIGHT_BG);
            txTable.addCell(empty);
        } else {
            boolean alt = false;
            for (Transaction t : txs) {
                Color bg = alt ? LIGHT_BG : Color.WHITE;
                Color amtColor = switch (t.getType()) {
                    case INCOME     -> INCOME_GREEN;
                    case EXPENSE    -> EXPENSE_RED;
                    case SAVING     -> SAVING_BLUE;
                    case INVESTMENT -> INVEST_PURP;
                };
                String prefix = t.getType() == Transaction.TransactionType.INCOME ? "+" : "-";
                addRowColored(txTable, bg,
                        new String[]{
                                t.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                t.getType().name(),
                                t.getCategory() != null ? t.getCategory() : "—",
                                t.getDescription(),
                                prefix + "$" + String.format("%.2f", t.getAmount().doubleValue())
                        },
                        new Color[]{TEXT_MUTED, TEXT_DARK, TEXT_DARK, TEXT_DARK, amtColor});
                alt = !alt;
            }
        }
        doc.add(txTable);

        // ── Footer ──
        Paragraph footer = new Paragraph(
                "Generated by WealthWise  ·  " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                new Font(Font.HELVETICA, 8, Font.NORMAL, TEXT_MUTED));
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    // ── CSV ────────────────────────────────────────────────────────────────

    public String generateCsv(String month) {
        List<Transaction> txs = monthTx(month);
        StringBuilder sb = new StringBuilder("Date,Type,Category,Description,Amount\n");
        for (Transaction t : txs) {
            sb.append(t.getDate()).append(',')
              .append(t.getType()).append(',')
              .append(escCsv(t.getCategory() != null ? t.getCategory() : "")).append(',')
              .append(escCsv(t.getDescription())).append(',')
              .append(t.getAmount()).append('\n');
        }
        return sb.toString();
    }

    // ── PDF builder helpers ────────────────────────────────────────────────

    private void addSectionTitle(Document doc, String text) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(10);
        t.setSpacingAfter(8);
        PdfPCell c = new PdfPCell(
                new Phrase(text, new Font(Font.HELVETICA, 10, Font.BOLD, GOLD)));
        c.setBackgroundColor(DARK_NAVY);
        c.setPadding(10);
        c.setBorder(Rectangle.NO_BORDER);
        t.addCell(c);
        doc.add(t);
    }

    private void addSummaryCard(PdfPTable table, String label, String value, Color valueColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_BG);
        cell.setPadding(14);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthBottom(3);
        cell.setBorderColorBottom(valueColor);
        Paragraph lp = new Paragraph(label,
                new Font(Font.HELVETICA, 8, Font.NORMAL, TEXT_MUTED));
        lp.setSpacingAfter(5);
        Paragraph vp = new Paragraph(value,
                new Font(Font.HELVETICA, 17, Font.BOLD, valueColor));
        cell.addElement(lp);
        cell.addElement(vp);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(
                    new Phrase(h, new Font(Font.HELVETICA, 9, Font.BOLD, GOLD)));
            cell.setBackgroundColor(DARK_NAVY);
            cell.setPadding(9);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }
    }

    private void addRow(PdfPTable table, Color bg, Color defaultColor, String... values) {
        for (String v : values) {
            PdfPCell cell = new PdfPCell(
                    new Phrase(v, new Font(Font.HELVETICA, 8, Font.NORMAL, defaultColor)));
            cell.setBackgroundColor(bg);
            cell.setPadding(7);
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColor(BORDER_GRAY);
            table.addCell(cell);
        }
    }

    private void addRowColored(PdfPTable table, Color bg, String[] values, Color[] colors) {
        for (int i = 0; i < values.length; i++) {
            Color c = (i < colors.length && colors[i] != null) ? colors[i] : TEXT_DARK;
            PdfPCell cell = new PdfPCell(
                    new Phrase(values[i], new Font(Font.HELVETICA, 8, Font.NORMAL, c)));
            cell.setBackgroundColor(bg);
            cell.setPadding(7);
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderColor(BORDER_GRAY);
            table.addCell(cell);
        }
    }
}
