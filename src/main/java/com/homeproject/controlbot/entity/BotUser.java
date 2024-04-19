package com.homeproject.controlbot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity (name = "bot_users")
@Data
public class BotUser {

    @Id
    private long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String phoneNumber;

    @OneToMany (mappedBy = "botUser",cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Spending> spendingList;

    @OneToMany (mappedBy = "botUser",cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Earning> earningList;
    private Timestamp registeredAt;

}
