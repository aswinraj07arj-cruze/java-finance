package com.aswin.financetracker.service;

import com.aswin.financetracker.dto.TransactionFilterDto;
import com.aswin.financetracker.dto.TransactionViewDto;
import com.aswin.financetracker.model.Expense;
import com.aswin.financetracker.model.Income;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public TransactionService(IncomeService incomeService, ExpenseService expenseService) {
        this.incomeService = incomeService;
        this.expenseService = expenseService;
    }

    public Page<TransactionViewDto> getUnifiedTransactions(TransactionFilterDto filter) {
        List<TransactionViewDto> unifiedList = new ArrayList<>();

        boolean includeIncome = filter.getType() == null || filter.getType().isBlank()
            || "ALL".equalsIgnoreCase(filter.getType()) || "INCOME".equalsIgnoreCase(filter.getType());

        boolean includeExpense = filter.getType() == null || filter.getType().isBlank()
            || "ALL".equalsIgnoreCase(filter.getType()) || "EXPENSE".equalsIgnoreCase(filter.getType());

        if (includeIncome) {
            List<Income> incomes = incomeService.getIncomes(filter.getStartDate(), filter.getEndDate(), null);
            for (Income inc : incomes) {
                // filter by source if specified
                if (filter.getSource() != null && !filter.getSource().isBlank()) {
                    if (!inc.getSource().name().equalsIgnoreCase(filter.getSource())) {
                        continue;
                    }
                }
                unifiedList.add(new TransactionViewDto(
                    inc.getId(),
                    "INCOME",
                    inc.getAmount(),
                    inc.getDate(),
                    inc.getSource().getDisplayName(),
                    inc.getSource().name(),
                    inc.getNote() != null ? inc.getNote() : "",
                    inc.getSource().getBadgeClass(),
                    "#10B981"
                ));
            }
        }

        if (includeExpense) {
            List<Expense> expenses = expenseService.getExpenses(filter.getStartDate(), filter.getEndDate(), null);
            for (Expense exp : expenses) {
                // filter by category if specified
                if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                    if (!exp.getCategory().name().equalsIgnoreCase(filter.getCategory())) {
                        continue;
                    }
                }
                unifiedList.add(new TransactionViewDto(
                    exp.getId(),
                    "EXPENSE",
                    exp.getAmount(),
                    exp.getDate(),
                    exp.getCategory().getDisplayName(),
                    exp.getCategory().name(),
                    exp.getDescription(),
                    exp.getCategory().getBadgeClass(),
                    exp.getCategory().getColorHex()
                ));
            }
        }

        // Search text filter
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            String query = filter.getSearch().trim().toLowerCase();
            unifiedList = unifiedList.stream()
                .filter(t -> (t.getDescriptionOrNote() != null && t.getDescriptionOrNote().toLowerCase().contains(query))
                    || (t.getCategoryOrSource() != null && t.getCategoryOrSource().toLowerCase().contains(query)))
                .toList();
        }

        // Sort descending by date, then id
        List<TransactionViewDto> sorted = unifiedList.stream()
            .sorted(Comparator.comparing(TransactionViewDto::getDate, Comparator.reverseOrder())
                .thenComparing(TransactionViewDto::getId, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();

        int pageSize = filter.getSize() > 0 ? filter.getSize() : 10;
        int pageNumber = Math.max(0, filter.getPage());
        int start = pageNumber * pageSize;

        if (start >= sorted.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(pageNumber, pageSize), sorted.size());
        }

        int end = Math.min(start + pageSize, sorted.size());
        List<TransactionViewDto> pageContent = sorted.subList(start, end);

        return new PageImpl<>(pageContent, PageRequest.of(pageNumber, pageSize), sorted.size());
    }
}
