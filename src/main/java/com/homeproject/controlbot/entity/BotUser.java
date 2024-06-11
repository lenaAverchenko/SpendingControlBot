package com.homeproject.controlbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity (name = "bot_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotUser {

    @Id
    private long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String phoneNumber;

    @OneToMany (mappedBy = "botUser",cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    private List<Spending> spendingList;

    @OneToMany (mappedBy = "botUser",cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    private List<Earning> earningList;
    private Timestamp registeredAt;

    @Override
    public String toString() {
        return "Your personal information:\n\n" +
                "id: " + id + "\n" +
                "First name: " + firstName + "\n" +
                "Last name: " + lastName + "\n" +
                "Username: " + userName + "\n" +
                "Registered at: " + registeredAt + "\n";
    }
}
