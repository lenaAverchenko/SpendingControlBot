package com.homeproject.controlbot.helper;

import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.entity.Spending;
import com.homeproject.controlbot.repository.EarningRepository;
import com.homeproject.controlbot.repository.SpendingRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
@Slf4j
@Data
public class ProfitCalculator {
    @Autowired
    private SpendingRepository spendingRepository;
    @Autowired
    private EarningRepository earningRepository;
    private BigDecimal currentProfit;

    public String calculateProfit(long chatId, BigDecimal earnedSum, BigDecimal spentSum) {
        BigDecimal resultedSum = earnedSum.subtract(spentSum);
        if (resultedSum.signum() < 0) {
            return ("You don't have any profit for that period. " +
                    "You have spent more than you've earned. The difference is " + resultedSum);
        }
        currentProfit = resultedSum;
        return ("Your profit for that period is: " + resultedSum);
    }

    public String calculateProfitOfTheYear(long chatId, int year) {
        log.info("calculateProfitOfTheYear started");
        BigDecimal allSpentMoney = spendingRepository.findAll().stream()
                .filter(sp -> sp.getBotUser().getId() == chatId)
                .filter(sp -> sp.getSpentAt().toLocalDateTime().getYear() == year)
                .map(Spending::getSpendingSum)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allEarnedMoney = earningRepository.findAll().stream()
                .filter(e -> e.getBotUser().getId() == chatId)
                .filter(e -> e.getEarnedAt().toLocalDateTime().getYear() == year)
                .map(Earning::getEarningSum)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return calculateProfit(chatId, allEarnedMoney, allSpentMoney);
    }

    public String calculateProfitOfTheCurrentYear(long chatId) {
        return calculateProfitOfTheYear(chatId, LocalDate.now().getYear());
    }

    public String calculateProfitOfTheMonth(long chatId, int year, int month) {
        BigDecimal allSpentMoney = spendingRepository.findAll().stream()
                .filter(sp -> sp.getBotUser().getId() == chatId)
                .filter(sp -> sp.getSpentAt().toLocalDateTime().getYear() == year)
                .filter(sp -> sp.getSpentAt().toLocalDateTime().getMonth().getValue() == month)
                .map(Spending::getSpendingSum)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allEarnedMoney = earningRepository.findAll().stream()
                .filter(e -> e.getBotUser().getId() == chatId)
                .filter(e -> e.getEarnedAt().toLocalDateTime().getYear() == year)
                .filter(e -> e.getEarnedAt().toLocalDateTime().getMonth().getValue() == month)
                .map(Earning::getEarningSum)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return calculateProfit(chatId, allEarnedMoney, allSpentMoney);
    }

    public String calculateProfitOfTheCurrentMonth(long chatId) {
        LocalDate currentDate = LocalDate.now();
        return calculateProfitOfTheMonth(chatId, currentDate.getYear(), currentDate.getMonth().getValue());
    }

    public String calculateProfitOfTheSelectedPeriod(long chatId, String startDate, String endDate) {
//        BigDecimal resultedSum = null;
        String datePattern = "\\d{2}-\\d{2}-\\d{4}";
        String pattern = "dd-MM-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Timestamp start = null;
        Timestamp end = null;
        String result = null;
        if (Pattern.matches(datePattern, startDate) && Pattern.matches(datePattern, endDate)) {
            try {
                start = new Timestamp(dateFormat.parse(startDate).getTime());
                end = new Timestamp(dateFormat.parse(endDate).getTime());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            if (start.after(end)) {
                Timestamp temporaryStorage = start;
                start = end;
                end = temporaryStorage;
            }
            Timestamp finalEnd = end;
            Timestamp finalStart = start;
            BigDecimal allSpentMoney = spendingRepository.findAll().stream()
                    .filter(sp -> sp.getBotUser().getId() == chatId)
                    .filter(sp -> sp.getSpentAt().before(finalEnd))
                    .filter(sp -> sp.getSpentAt().after(finalStart))
                    .map(Spending::getSpendingSum)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal allEarnedMoney = earningRepository.findAll().stream()
                    .filter(e -> e.getBotUser().getId() == chatId)
                    .filter(e -> e.getEarnedAt().before(finalEnd))
                    .filter(e -> e.getEarnedAt().after(finalStart))
                    .map(Earning::getEarningSum)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result = calculateProfit(chatId, allEarnedMoney, allSpentMoney);
        } else {
            return "Provided date for calculations have wrong format. Please, check both of them, " +
                    "and make sure they are provided in format: DD-MM-YYYY. " +
                    "You can start again by pressing /calculateprofit";
        }
        return result;
    }
}
