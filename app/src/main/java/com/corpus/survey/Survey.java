package com.corpus.survey;

import java.util.Date;

/**
 * Created by devadas.vijayan on 5/30/16.
 */
public class Survey {
    private String userName;
    private String phoneNumber;
    private long dateOfBirth;
    private long createdDate;
    private String[] categoriesInterestedIn;
    private String place;


    public Survey (String userName, String phoneNumber, long createdDate)
    {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.createdDate = createdDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
