package com.corpus.survey;

import java.util.Date;

/**
 * Created by devadas.vijayan on 5/30/16.
 */
public class Survey {
    private String userName;
    private String phoneNumber;
    private Date dateOfBirth;

    public Survey (String userName, String phoneNumber)
    {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
