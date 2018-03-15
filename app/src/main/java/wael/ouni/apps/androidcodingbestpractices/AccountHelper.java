package wael.ouni.apps.androidcodingbestpractices;

/**
 * Created by wael on 15/03/18.
 */

public class AccountHelper {
    private static AccountHelper mAccountHelper;
    private UserWrapper mUserWrapper;

    private AccountHelper() {
    }

    public static AccountHelper getInstance() {
        if (mAccountHelper == null) {
            mAccountHelper = new AccountHelper();
        }
        return mAccountHelper;
    }

    public UserWrapper getUserWrapper() {
        return mUserWrapper;
    }

    public void setUserWrapper(UserWrapper userWrapper) {
        mUserWrapper = userWrapper;
    }
}
