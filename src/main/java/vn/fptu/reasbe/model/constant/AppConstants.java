package vn.fptu.reasbe.model.constant;

public class AppConstants {
    // pagination
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_DIRECTION = "asc";
    public static final String DEFAULT_SORT_BY= "id";

    // regex
    public static final String EMAIL_REGEX = "^[^\\.][a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$";
    public static final String PHONE_REGEX = "^\\d{10,12}$";
    public static final String GENDER_REGEX = "^(Male|Female|Other)$";

    // auth
    public static final String AUTH_ATTR_NAME = "Authorization";
    public static final String AUTH_VALUE_PREFIX = "Bearer ";
    public static final String ROLE_RESIDENT = "ROLE_RESIDENT";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_STAFF = "ROLE_STAFF";

    //item
    public static final int EXPIRED_TIME_WEEKS = 2;
    public static final int EXPIRED_TIME_WEEKS_PREMIUM = 6;
    public static final int MAX_ITEM_UPLOADED = 5;
    public static final int MAX_ITEM_UPLOADED_PREMIUM = 15;
    public static final String LIST_ITEM_LIMIT = "5";

    //exchange
    public static final int NUM_OF_OFFER = 3;
}
