package com.homeproject.controlbot.entity;

import com.homeproject.controlbot.enums.TypeOfEarning;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity(name = "earning")
@Data
public class Earning {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long earningId;

        @Enumerated(EnumType.STRING)
        private TypeOfEarning typeOfEarning;

//        @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
@ManyToOne
        @JoinColumn(name = "bot_user_id", referencedColumnName = "id")
        private BotUser botUser;
        private Timestamp earnedAt;
        private Timestamp registeredAt;
        private BigDecimal earningSum;

        @Override
        public String toString() {
                return "Earning: \n" +
                        "Earning id: " + earningId + "\n" +
                        "Type of earning: " + typeOfEarning + "\n" +
                        "User name: " + botUser.getUserName() + "\n" +
                        "Earned at: " + earnedAt +"\n" +
                        "Registered at: " + registeredAt + "\n" +
                        "Earning sum: " + earningSum + "\n\n";
        }
}
