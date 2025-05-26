package com.ktt.dto;

import lombok.Data;

@Data
public class SearchPassengerRef {
    private String code;
    private String bookingTravelerRef;
    private String Age;

    // Constructor for code and bookingTravelerRef only
    public SearchPassengerRef(String code, String bookingTravelerRef) {
        this.code = code;
        this.bookingTravelerRef = bookingTravelerRef;
    }

    // Constructor for code, bookingTravelerRef, and age
    public SearchPassengerRef(String code, String bookingTravelerRef, String Age) {
        this.code = code;
        this.bookingTravelerRef = bookingTravelerRef;
        this.Age = Age;
    }

    // Smart toString (skips age if null)
    @Override
    public String toString() {
        return "SearchPassengerRef{" +
                "code='" + code + '\'' +
                ", bookingTravelerRef='" + bookingTravelerRef + '\'' +
                (Age != null ? ", age='" + Age + '\'' : "") +
                '}';
    }

    // Explicit toString without age
    public String toStringWithoutAge() {
        return "SearchPassengerRef{" +
                "code='" + code + '\'' +
                ", bookingTravelerRef='" + bookingTravelerRef + '\'' +
                '}';
    }

    // Explicit toString with age
    public String toStringWithAge() {
        return "SearchPassengerRef{" +
                "code='" + code + '\'' +
                ", bookingTravelerRef='" + bookingTravelerRef + '\'' +
                ", age='" + Age + '\'' +
                '}';
    }
}
