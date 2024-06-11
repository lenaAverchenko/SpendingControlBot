package com.homeproject.controlbot.service;

import com.homeproject.controlbot.entity.BotUser;
import com.homeproject.controlbot.entity.Earning;
import com.homeproject.controlbot.enums.TypeOfEarning;
import com.homeproject.controlbot.repository.BotUserRepository;
import com.homeproject.controlbot.repository.EarningRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith ({MockitoExtension.class})
class EarningServiceImplTest {

    @InjectMocks
    private EarningServiceImpl earningService;
    @Mock
    private EarningRepository earningRepository;
    @Mock
    private BotUserRepository botUserRepository;

    private List<Earning> earningList;
    private List<BotUser> botUserList;

    EarningServiceImplTest(){

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
                new Earning(8, TypeOfEarning.SALARY, botUserList.get(4), new Timestamp((dateFormat.parse("30-05-2022").getTime())), new Timestamp(System.currentTimeMillis()), new BigDecimal(240))
                );
    }

    @Test
    void findAllEarning() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(4, this.earningService.findAllEarning(1).size());
    }

    @Test
    void findAllEarningEmpty() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(0, this.earningService.findAllEarning(5).size());
    }
    @Test
    void findAllEarningOfTheYear() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(3, this.earningService.findAllEarningOfTheYear(2024, 1).size());
    }

    @Test
    void findAllEarningOfTheYearEmpty() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(new ArrayList<>(), this.earningService.findAllEarningOfTheYear(1999, 1));
    }

    //depends. It was 2024
    @Test
    void findAllEarningOfTheCurrentYear() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(3, this.earningService.findAllEarningOfTheCurrentYear(1).size());
    }

    @Test
    void findAllEarningOfTheMonth() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(2, this.earningService.findAllEarningOfTheMonth(3, 2024, 1).size());
    }
    @Test
    void findAllEarningOfTheMonthEmpty() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(0, this.earningService.findAllEarningOfTheMonth(3, 2001, 1).size());
    }

    @Test
    void findAllEarningOfTheDay() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(1, this.earningService.findAllEarningOfTheDay(10,3,2024,1).size());
    }

    @Test
    void findAllEarningOfTheDayEmpty() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(0, this.earningService.findAllEarningOfTheDay(10,8,2001,1).size());
    }

    //depends. It was June 2024
    @Test
    void findAllEarningOfTheCurrentMonth() {
        Mockito.when(this.earningRepository.findAll()).thenReturn(this.earningList);
        Assertions.assertEquals(1, this.earningService.findAllEarningOfTheCurrentMonth(1).size());
    }

}