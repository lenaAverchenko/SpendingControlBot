package com.homeproject.controlbot.helper;

import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.entity.Spending;
import com.homeproject.controlbot.enums.TypeOfEarning;
import com.homeproject.controlbot.enums.TypeOfPurchase;
import com.homeproject.controlbot.repository.EarningRepository;
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


@ExtendWith(MockitoExtension.class)
class ProfitCalculatorTest {

    @InjectMocks
    private ProfitCalculator profitCalculator;
    @Mock
    private SpendingRepository spendingRepository;
    @Mock
    private EarningRepository earningRepository;
    private List<Spending> spendingList;
    private List<Earning> earningList;
    private List<BotUser> botUserList;


    ProfitCalculatorTest() {

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
        this.earningList = Arrays.asList(
                new Earning(1, TypeOfEarning.GIFT, botUserList.get(1), new Timestamp((dateFormat.parse("01-06-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(1000)),
                new Earning(2, TypeOfEarning.SALARY, botUserList.get(1), new Timestamp((dateFormat.parse("10-03-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(500)),
                new Earning(3, TypeOfEarning.CHILD_SUPPORT, botUserList.get(1), new Timestamp((dateFormat.parse("01-03-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(300)),
                new Earning(4, TypeOfEarning.GIFT, botUserList.get(1), new Timestamp((dateFormat.parse("20-03-2023").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(2000)),
                new Earning(5, TypeOfEarning.SALARY, botUserList.get(3), new Timestamp((dateFormat.parse("06-05-2020").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(300)),
                new Earning(6, TypeOfEarning.PRISE, botUserList.get(3), new Timestamp((dateFormat.parse("17-03-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(750)),
                new Earning(7, TypeOfEarning.CHILD_SUPPORT, botUserList.get(3), new Timestamp((dateFormat.parse("01-03-2024").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(800)),
                new Earning(8, TypeOfEarning.SALARY, botUserList.get(4), new Timestamp((dateFormat.parse("30-05-2022").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(240)));
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
    void calculateProfit() {
        Assertions.assertEquals("Your profit for that period is: 220", profitCalculator.calculateProfit(4L, new BigDecimal(240), new BigDecimal(20)));
    }

    @Test
    void calculateNegativeProfit() {
        Assertions.assertEquals("You don't have any profit for that period. You have spent more than you've earned. The difference is -220", profitCalculator.calculateProfit(4L, new BigDecimal(20), new BigDecimal(240)));
    }

    @Test
    void calculateProfitOfTheYear() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals("Your profit for that period is: 1400", profitCalculator.calculateProfitOfTheYear(1, 2023));
    }

    //Depends. Changeable. Was tested 17 June 2024
    @Test
    void calculateProfitOfTheCurrentYear() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals("Your profit for that period is: 1050", profitCalculator.calculateProfitOfTheCurrentYear(1));

    }

    @Test
    void calculateProfitOfTheMonth() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals("Your profit for that period is: 150", profitCalculator.calculateProfitOfTheMonth(1, 2024, 3));

    }

    //Depends. Changeable. Was tested 17 June 2024
    @Test
    void calculateProfitOfTheCurrentMonth() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals("Your profit for that period is: 900", profitCalculator.calculateProfitOfTheCurrentMonth(1));
    }

    @Test
    void calculateProfitOfTheSelectedPeriod() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals("Your profit for that period is: 1700", profitCalculator.calculateProfitOfTheSelectedPeriod(1, "01-03-2023", "02-03-2024"));
    }

    @Test
    void calculateNegativeProfitOfTheSelectedPeriod() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Mockito.when(this.spendingRepository.findAll()).thenReturn(this.spendingList);
        Assertions.assertEquals("You don't have any profit for that period. You have spent more than you've earned. The difference is -600", profitCalculator.calculateProfitOfTheSelectedPeriod(1, "01-05-2023", "01-07-2023"));
    }
}