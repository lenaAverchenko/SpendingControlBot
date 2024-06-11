package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Spending;
import com.homeproject.controlbot.enums.TypeOfPurchase;
import com.homeproject.controlbot.repository.BotUserRepository;
import com.homeproject.controlbot.repository.SpendingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
class SpendingServiceImplTest {
     @InjectMocks
    private SpendingServiceImpl spendingService;
    @Mock
    private SpendingRepository spendingRepository;
    @Mock
    private BotUserRepository botUserRepository;

    private List<Spending> spendingList;
    private List<BotUser> botUserList;

    SpendingServiceImplTest(){

    }

    @BeforeEach
    public void init() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        this.botUserList = Arrays.asList(
                new BotUser(0, "Vasia", "Pupkin", "123456", "none", new ArrayList<>(), new ArrayList<>(), new Timestamp(System.currentTimeMillis())),
                new BotUser(1, "Kolia", "Nikolajev", "123457", "none", new ArrayList<>(), new ArrayList<>(), new Timestamp((dateFormat.parse("11-12-2006").getTime()))),
                new BotUser(2, "John", "Johnson", "123458", "none", new ArrayList<>(), new ArrayList<>(), new Timestamp((dateFormat.parse("20-03-2024").getTime()))),
                new BotUser(3, "Ala", "Allova", "654987", "none", new ArrayList<>(), new ArrayList<>(), new Timestamp((dateFormat.parse("15-03-2025").getTime()))),
                new BotUser(4, "Tosha", "Kolin", "321654", "none", new ArrayList<>(), new ArrayList<>(), new Timestamp((dateFormat.parse("02-12-2013").getTime()))));
        this.spendingList = Arrays.asList(
                new Spending(0, TypeOfPurchase.CAR_PARTS, "car_pit", "none", botUserList.get(1), new Timestamp((dateFormat.parse("12-06-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(100)),
                new Spending(1, TypeOfPurchase.CAR, "car_pit", "none", botUserList.get(1), new Timestamp((dateFormat.parse("10-03-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(400)),
                new Spending(2, TypeOfPurchase.FOOD, "DOL", "none", botUserList.get(1), new Timestamp((dateFormat.parse("15-03-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(250)),
                new Spending(3, TypeOfPurchase.CLOTHES, "car_pit", "none", botUserList.get(1), new Timestamp((dateFormat.parse("01-06-2023").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(600)),
                new Spending(4, TypeOfPurchase.ELECTRONICS, "Lux", "none", botUserList.get(1), new Timestamp((dateFormat.parse("02-12-2013").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(200)),
                new Spending(5, TypeOfPurchase.HOME_STUFF, "home", "none", botUserList.get(3), new Timestamp((dateFormat.parse("17-03-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(120)),
                new Spending(6, TypeOfPurchase.FOOD, "walm", "none", botUserList.get(2), new Timestamp((dateFormat.parse("01-03-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(150)),
                new Spending(7, TypeOfPurchase.RESORT, "e-tr", "none", botUserList.get(4), new Timestamp((dateFormat.parse("01-08-2010").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(20)),
                new Spending(8, TypeOfPurchase.FOOD, "Lux", "none", botUserList.get(2), new Timestamp((dateFormat.parse("30-05-2022").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(360))
                );
    }


    @Test
    void findAllSpending() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(5, this.spendingService.findAllSpending(1).size());
    }
    @Test
    void findAllSpendingEmpty() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(0, this.spendingService.findAllSpending(10).size());
    }

    @Test
    void findAllSpendingOfTheYear() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(3, this.spendingService.findAllSpendingOfTheYear(2024, 1).size());
    }
    @Test
    void findAllSpendingOfTheYearEmpty() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(0, this.spendingService.findAllSpendingOfTheYear(2000, 1).size());
    }

    //It depends. It was June 12, 2024
    @Test
    void findAllSpendingOfTheCurrentYear() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(3, this.spendingService.findAllSpendingOfTheCurrentYear(1).size());
    }

    @Test
    void findAllSpendingOfTheMonth() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(2, this.spendingService.findAllSpendingOfTheMonth(3, 2024, 1).size());
    }

    @Test
    void findAllSpendingOfTheMonthEmpty() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(0, this.spendingService.findAllSpendingOfTheMonth(1, 2015, 1).size());
    }

    //It depends. It was June 12, 2024
    @Test
    void findAllSpendingOfTheCurrentMonth() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(1, this.spendingService.findAllSpendingOfTheCurrentMonth(1).size());
    }

    //It depends. It was June 12, 2024
    @Test
    void findAllSpendingOfTheCurrentDay() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(1, this.spendingService.findAllSpendingOfTheCurrentDay(1).size());
    }

    @Test
    void findAllSpendingOfTheDay() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(1, this.spendingService.findAllSpendingOfTheDay(10, 3, 2024, 1).size());
   }
    @Test
    void findAllSpendingOfTheDayEmpty() {
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals(0, this.spendingService.findAllSpendingOfTheDay(5, 10, 2020, 1).size());

    }

}