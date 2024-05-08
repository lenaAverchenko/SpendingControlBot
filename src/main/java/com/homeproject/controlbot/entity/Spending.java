package com.homeproject.controlbot.entity;

import com.homeproject.controlbot.enums.TypeOfPurchase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity (name = "spending")
@Data
public class Spending {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(strategy = GenerationType.AUTO)

    private long spendingId;

    @Enumerated(EnumType.STRING)
    private TypeOfPurchase typeOfPurchase;
    private String shopName;
    private String description;

//    @ManyToOne(cascade = CascadeType.PERSIST)
    @ManyToOne
    @JoinColumn(name = "bot_user_id", referencedColumnName = "id")
    private BotUser botUser;
    private Timestamp spentAt;
    private Timestamp registeredAt;
    private BigDecimal spendingSum;

    @Override
    public String toString() {
        return "\n\nYour Spending information: " +
                "\nSpending Id: " + spendingId +
                "\nType of purchase: " + typeOfPurchase +
                "\nShop name: " + shopName +
                "\nDescription: " + description +
                "\nSpent at: " + spentAt +
                "\nRegistered at: " + registeredAt +
                "\nSpent sum=" + spendingSum + "\n";
    }
}
