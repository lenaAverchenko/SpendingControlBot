package com.homeproject.controlbot.enums;

public enum TypeOfEarning {
    SALARY("Salary"),
    CHILD_SUPPORT("Child support"),
    PRISE("Prise"),
    GIFT("Gift");

    private final String name;


    TypeOfEarning(String name) {
        this.name = name;
    }
}
