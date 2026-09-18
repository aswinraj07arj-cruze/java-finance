package com.aswin.financetracker.service;

import com.aswin.financetracker.dto.IncomeRequestDto;
import com.aswin.financetracker.model.Income;
import com.aswin.financetracker.model.User;
import com.aswin.financetracker.model.enums.IncomeSource;
import com.aswin.financetracker.repository.IncomeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserService userService;

    public IncomeService(IncomeRepository incomeRepository, UserService userService) {
        this.incomeRepository = incomeRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Income> getIncomes(LocalDate startDate, LocalDate endDate, IncomeSource source) {
        User user = userService.getDefaultUser();
        if (startDate != null && endDate != null && source != null) {
            return incomeRepository.findByUserAndSourceAndDateBetweenOrderByDateDescIdDesc(user, source, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            return incomeRepository.findByUserAndDateBetweenOrderByDateDescIdDesc(user, startDate, endDate);
        } else if (source != null) {
            return incomeRepository.findByUserAndSourceOrderByDateDescIdDesc(user, source);
        } else {
            return incomeRepository.findByUserOrderByDateDescIdDesc(user);
        }
    }

    @Transactional(readOnly = true)
    public Income getIncomeById(Long id) {
        return incomeRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Income entry not found with id: " + id));
    }

    public Income createIncome(IncomeRequestDto dto) {
        User user = userService.getDefaultUser();
        Income income = new Income(
            dto.getAmount(),
            dto.getSource(),
            dto.getDate(),
            dto.getNote(),
            user
        );
        return incomeRepository.save(income);
    }

    public Income updateIncome(Long id, IncomeRequestDto dto) {
        Income income = getIncomeById(id);
        income.setAmount(dto.getAmount());
        income.setSource(dto.getSource());
        income.setDate(dto.getDate());
        income.setNote(dto.getNote());
        return incomeRepository.save(income);
    }

    public void deleteIncome(Long id) {
        if (!incomeRepository.existsById(id)) {
            throw new NoSuchElementException("Income entry not found with id: " + id);
        }
        incomeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalIncome() {
        User user = userService.getDefaultUser();
        return incomeRepository.sumTotalIncomeByUser(user);
    }

    @Transactional(readOnly = true)
    public BigDecimal getIncomeBetween(LocalDate startDate, LocalDate endDate) {
        User user = userService.getDefaultUser();
        return incomeRepository.sumIncomeByUserAndDateBetween(user, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getSourceTotals(LocalDate startDate, LocalDate endDate) {
        User user = userService.getDefaultUser();
        return incomeRepository.findSourceTotalsByUserAndDateBetween(user, startDate, endDate);
    }
}
