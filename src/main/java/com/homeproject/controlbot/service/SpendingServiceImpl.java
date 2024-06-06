package com.homeproject.controlbot.service;

import com.homeproject.controlbot.comparator.SpendingDateComparator;
import com.homeproject.controlbot.configuration.BotConfig;
import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Spending;
import com.homeproject.controlbot.enums.Marker;
import com.homeproject.controlbot.enums.TypeOfPurchase;
import com.homeproject.controlbot.repository.SpendingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class SpendingServiceImpl implements SpendingService{
    @Autowired
    private SpendingRepository spendingRepository;

    @Override
    public String deleteSpending(long id, long chatIdCheck) {
        Spending spendingToDelete = spendingRepository.findById(id).orElse(null);
        long chatId = 0;
        if (spendingToDelete != null) {
            chatId = spendingToDelete.getBotUser().getId();
        }
        if (chatId == chatIdCheck) {
            spendingRepository.deleteById(id);
            if (spendingRepository.findById(id).orElse(null) == null) {
                return "Spending data with the id: " + id + " has been successfully deleted";
            }
        } else {
            return "You are trying to delete not your spending. You can check " +
                    "saved data for your account and try to delete again";
        }
        return "Couldn't delete the spending";
    }
    @Override
    public List<Spending> findAllSpending(long chatId) {
        return spendingRepository.findAll().stream()
                .filter(sp -> sp.getBotUser().getId() == chatId)
                .sorted(new SpendingDateComparator()).toList();
    }

    @Override
    public List<Spending> findAllSpendingOfTheYear(int year, long chatId) {
        return findAllSpending(chatId).stream()
                .filter(x -> (int) x.getSpentAt().toLocalDateTime().getYear() == year)
                .toList();
    }

    @Override
    public List<Spending> findAllSpendingOfTheCurrentYear(long chatId) {
        return findAllSpendingOfTheYear(LocalDate.now().getYear(), chatId);
    }

    @Override
    public List<Spending> findAllSpendingOfTheMonth(int monthNumber, int year, long chatId) {
        return findAllSpending(chatId).stream()
                .filter(x -> ((int) x.getSpentAt().toLocalDateTime().getYear() == year))
                .filter(y -> (y.getSpentAt().toLocalDateTime().getMonth().getValue() == monthNumber))
                .toList();
    }

    @Override
    public List<Spending> findAllSpendingOfTheCurrentMonth(long chatId) {
        return findAllSpendingOfTheMonth(LocalDate.now().getMonth().getValue(), LocalDate.now().getYear(), chatId);
    }

    @Override
    public List<Spending> findAllSpendingOfTheCurrentDay(long chatId) {
        return findAllSpendingOfTheDay(
                LocalDate.now().getDayOfMonth(),
                LocalDate.now().getMonth().getValue(),
                LocalDate.now().getYear(), chatId);
    }

    @Override
    public List<Spending> findAllSpendingOfTheDay(int spentDay, int spentMonth, int spentYear, long chatId) {
        return findAllSpending(chatId).stream()
                .filter(x -> ((int) x.getSpentAt().toLocalDateTime().getYear() == spentYear))
                .filter(y -> (y.getSpentAt().toLocalDateTime().getMonth().getValue() == spentMonth))
                .filter(y -> (y.getSpentAt().toLocalDateTime().getDayOfMonth() == spentDay))
                .toList();
    }

    @Override
    public String saveSpending(long chatId, BotUser botUser, TypeOfPurchase typeOfPurchase, String shopName,
                              String descriptionOfPurchase, BigDecimal spentSum, Timestamp date) {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        return saveSpendingByDate(chatId, botUser, typeOfPurchase, shopName, descriptionOfPurchase,
                spentSum, date);
    }
    @Override
    public String saveSpendingByDate(long chatId, BotUser botUser, TypeOfPurchase typeOfPurchase, String shopName,
                                     String descriptionOfPurchase, BigDecimal sum, Timestamp date) {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        Spending spending = new Spending();
        spending.setRegisteredAt(currentTime);
        spending.setBotUser(botUser);
        spending.setSpentAt(date);
        spending.setTypeOfPurchase(typeOfPurchase);
        spending.setShopName(shopName);
        spending.setDescription(descriptionOfPurchase);
        spending.setSpendingSum(sum);
        spendingRepository.save(spending);
        if (spendingRepository.findById(spending.getSpendingId()).isPresent()) {
            return  "The provided data about your recent spending has been successfully saved.";
        }
        return "The data hasn't been saved. Try again.";
    }

}
