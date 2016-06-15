package com.corpus.survey;

/**
 * Created by devadas.vijayan on 5/30/16.
 */
public class Survey {

    private String userName;
    private String phoneNumber;
    private int gender;
    private long dateOfBirth;
    private long createdDate;
    private String contactGroup;
    private String place;
    private String email;
//    private String[] categoriesInterestedIn; // TODO: Later after discussion


    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;
    public static final int GENDER_OTHER = 3;

    // Constructor with mandatory fields
    public Survey (String userName, String phoneNumber, int gender, long createdDate, String contactGroup)
    {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.createdDate = createdDate;
        this.contactGroup = contactGroup;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public long getDateOfBirth() {
        return dateOfBirth;
    }

    public int getGender() {
        return gender;
    }

    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public String getContactGroup() {
        return contactGroup;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
