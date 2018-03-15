package wael.ouni.apps.androidcodingbestpractices;

import java.io.Serializable;

/**
 * Created by wael on 15/03/18.
 */

public class UserWrapper implements Serializable {
    private String mName;
    private String mEmail;
    private String mPicturePath;

    public UserWrapper(String name, String email, String picturePath) {
        mName = name;
        mEmail = email;
        mPicturePath = picturePath;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getPicturePath() {
        return mPicturePath;
    }

    public void setPicturePath(String picturePath) {
        mPicturePath = picturePath;
    }

    @Override
    public String toString() {
        return "UserWrapper{" +
                "mName='" + mName + '\'' +
                ", mEmail='" + mEmail + '\'' +
                ", mPicturePath='" + mPicturePath + '\'' +
                '}';
    }
}
