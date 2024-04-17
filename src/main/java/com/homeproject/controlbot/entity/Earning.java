package com.homeproject.controlbot.entity;

import com.homeproject.controlbot.enums.TypeOfEarning;
import com.homeproject.controlbot.enums.TypeOfPurchase;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "earning")
@Data
public class Earning {
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long earningId;
        private TypeOfEarning typeOfEarning;

        @ManyToOne(cascade = CascadeType.PERSIST)
        @JoinColumn(name = "bot_user_id", referencedColumnName = "id")
        private BotUser botUser;
        private Timestamp earnedAt;
        private Timestamp registeredAt;
        private float spendingSum;
}
