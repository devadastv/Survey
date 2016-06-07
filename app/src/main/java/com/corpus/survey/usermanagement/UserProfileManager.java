package com.corpus.survey.usermanagement;

import com.corpus.survey.R;

/**
 * Created by devadas.vijayan on 6/7/16.
 */
public class UserProfileManager {
    private static UserProfileManager instance;

    public static UserProfileManager getInstance() {
        if (null == instance) {
            instance = new UserProfileManager();
        }
        return instance;
    }

    public int getUserImageId() {
        return R.mipmap.user_image;
    }

    public String getUserName() {
        return "Volkswagen Koramangala";
    }

    public String getUserEmail() {
        return "vw.koramangala@volkswagen.in";
    }
}
