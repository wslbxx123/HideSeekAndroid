package dlmj.hideseek.Common.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/1/16.
 */
public class User implements Parcelable {
    private long mPKId;
    private String mPhone;
    private String mSessionId;
    private String mNickname;
    private String mPhotoUrl;
    private String mSmallPhotoUrl;
    private Date mRegisterDate;
    private int mRecord = 0;
    private SexEnum mSex;
    private String mRegion;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private RoleEnum mRole;
    private long mVersion;
    private String mPinyin;
    private int mBombNum;
    private boolean mHasGuide;
    private int mFriendNum;
    private String mDefaultArea;
    private String mDefaultAddress;
    private boolean mIsFriend;
    private String mAlias;
    private String mAddTime;
    private String mRequestMessage;
    private SharedPreferences mSharedPreferences =
            SharedPreferenceUtil.getSharedPreferences();

    public User(long pkId, String phone, String sessionId, String nickname,
                String registerDate, int record, RoleEnum role, long version,
                String pinyin, int bombNum, boolean hasGuide, int friendNum,
                SexEnum sex, String photoUrl, String smallPhotoUrl, String region,
                String defaultArea, String defaultAddress) throws ParseException {
        this.mPKId = pkId;
        this.mPhone = phone;
        this.mSessionId = sessionId;
        this.mNickname = nickname;
        this.mRegisterDate = mDateFormat.parse(registerDate);
        this.mRecord = record;
        this.mRole = role;
        this.mVersion = version;
        this.mPinyin = pinyin;
        this.mBombNum = bombNum;
        this.mHasGuide = hasGuide;
        this.mFriendNum = friendNum;
        this.mSex = sex;

        if(photoUrl != null) {
            this.mPhotoUrl = photoUrl;
        }

        if(smallPhotoUrl != null) {
            this.mSmallPhotoUrl = smallPhotoUrl;
        }

        if(region != null) {
            this.mRegion = region;
        }

        if(defaultArea != null) {
            this.mDefaultArea = defaultArea;
        }

        if(defaultAddress != null) {
            this.mDefaultAddress = defaultAddress;
        }
    }

    public User(long pkId, String phone, String nickname, String registerDate,
                String photoUrl, String smallPhotoUrl, SexEnum sex, String region,
                RoleEnum role, long version, String pinyin) throws ParseException {
        this.mPKId = pkId;
        this.mPhone = phone;
        this.mNickname = nickname;
        this.mRegisterDate = mDateFormat.parse(registerDate);

        if(photoUrl != null) {
            this.mPhotoUrl = photoUrl;
        }

        if(smallPhotoUrl != null) {
            this.mSmallPhotoUrl = smallPhotoUrl;
        }

        if(region != null) {
            this.mRegion = region;
        }

        mSex = sex;
        mRole = role;
        mVersion = version;
        mPinyin = pinyin;
    }

    public User(Parcel parcel) {
        try {
            this.mPKId = parcel.readLong();
            this.mPhone = parcel.readString();
            this.mSessionId = parcel.readString();
            this.mNickname = parcel.readString();
            this.mPhotoUrl = parcel.readString();
            this.mSmallPhotoUrl = parcel.readString();
            this.mRegisterDate = mDateFormat.parse(parcel.readString());
            this.mRecord = parcel.readInt();
            this.mSex = SexEnum.valueOf(parcel.readInt());
            this.mRegion = parcel.readString();
            this.mRole = RoleEnum.valueOf(parcel.readInt());
            this.mVersion = parcel.readLong();
            this.mPinyin = parcel.readString();
            this.mBombNum = parcel.readInt();
            this.mHasGuide = parcel.readInt() == 1;
            this.mFriendNum = parcel.readInt();
            this.mDefaultArea = parcel.readString();
            this.mDefaultAddress = parcel.readString();
            this.mIsFriend = parcel.readInt() == 1;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public long getPKId() {
        return mPKId;
    }

    public String getPhone() {
        return mPhone;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        mPhotoUrl = photoUrl;
    }

    public String getSmallPhotoUrl(){
        return mSmallPhotoUrl;
    }

    public void setSmallPhotoUrl(String smallPhotoUrl) {
        mSmallPhotoUrl = smallPhotoUrl;
    }

    public String getNickname() {
        return mNickname;
    }

    public Date getRegisterDate() {
        return mRegisterDate;
    }

    public int getRecord() {
        return mRecord;
    }

    public void setRecord(int record) throws JSONException {
        mRecord = record;

        SharedPreferenceSettings accountInfo = SharedPreferenceSettings.USER_INFO;
        String userInfoStr = mSharedPreferences.getString(accountInfo.getId(),
                accountInfo.getDefaultValue().toString());
        JSONObject userInfo = new JSONObject(userInfoStr);
        userInfo.optInt("record", record);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(accountInfo.getId(), userInfoStr);
        editor.apply();
    }

    public int getFriendNum() {
        return mFriendNum;
    }

    public SexEnum getSex() {
        return mSex;
    }

    public void setSex(SexEnum sex) {
        mSex = sex;
    }

    public void setRegion(String region) {
        mRegion = region;
    }

    public String getRegion() {
        return mRegion;
    }

    public RoleEnum getRole() {
        return mRole;
    }

    public long getVersion() {
        return mVersion;
    }

    public String getPinyin() {
        return mPinyin;
    }

    public int getBombNum() {
        return mBombNum;
    }

    public boolean getHasGuide() {
        return mHasGuide;
    }

    public boolean getIsFriend() {
        return this.mIsFriend;
    }

    public void setIsFriend(boolean isFriend) {
        this.mIsFriend = isFriend;
    }

    public String getAlias() {
        return mAlias;
    }

    public void setAlias(String alias) {
        mAlias = alias;
    }

    public int getRoleDrawableId() {
        switch(mRole) {
            case grassFairy:
                return R.drawable.grass_fairy;
            case waterMagician:
                return R.drawable.water_magician;
            case fireKnight:
                return R.drawable.fire_knight;
            case stoneMonster:
                return R.drawable.stone_monster;
            case lightningGiant:
            default:
                return R.drawable.lightning_giant;

        }
    }

    public int getRoleImageDrawableId() {
        switch(mRole) {
            case grassFairy:
                return R.drawable.grass_fairy_role;
            case waterMagician:
                return R.drawable.water_magician_role;
            case fireKnight:
                return R.drawable.fire_knight_role;
            case stoneMonster:
                return R.drawable.stone_monster_role;
            case lightningGiant:
            default:
                return R.drawable.lightning_giant_role;

        }
    }

    public String getRoleName(Context context) {
        switch(mRole) {
            case grassFairy:
                return context.getString(R.string.grassFairy);
            case waterMagician:
                return context.getString(R.string.waterMagician);
            case fireKnight:
                return context.getString(R.string.fireKnight);
            case stoneMonster:
                return context.getString(R.string.stoneMonster);
            case lightningGiant:
                return context.getString(R.string.lightningGiant);
            default:
                return "";
        }
    }

    public int getSexImageDrawableId() {
        switch(mSex) {
            case female:
                return R.drawable.female;
            case male:
                return R.drawable.male;
            default:
                return 0;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mPKId);
        parcel.writeString(mPhone);
        parcel.writeString(mSessionId);
        parcel.writeString(mNickname);
        parcel.writeString(mPhotoUrl);
        parcel.writeString(mSmallPhotoUrl);
        parcel.writeString(mDateFormat.format(mRegisterDate));
        parcel.writeInt(mRecord);
        parcel.writeInt(mSex.getValue());
        parcel.writeString((mRegion == null || mRegion.equals("null")) ? "" : mRegion);
        parcel.writeInt(mRole.getValue());
        parcel.writeLong(mVersion);
        parcel.writeString(mPinyin);
        parcel.writeInt(mBombNum);
        parcel.writeInt(mHasGuide ? 1 : 0);
        parcel.writeInt(mFriendNum);
        parcel.writeString(mDefaultArea);
        parcel.writeString(mDefaultAddress);
        parcel.writeInt(mIsFriend ? 1 : 0);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public enum SexEnum {
        notSet(0), female(1), male(2), secret(3);

        private int value = 0;

        private SexEnum(int value) {    //    必须是private的，否则编译错误
            this.value = value;
        }

        public static SexEnum valueOf(int value) {
            switch (value) {
                case 1:
                    return female;
                case 2:
                    return male;
                case 3:
                    return secret;
                case 0:
                default:
                    return notSet;
            }
        }

        public int getValue() {
            return this.value;
        }

        public String toString(Context context) {
            switch(this) {
                case female:
                    return context.getString(R.string.female);
                case male:
                    return context.getString(R.string.male);
                case secret:
                    return context.getString(R.string.secret);
                case notSet:
                default:
                    return context.getString(R.string.not_set);
            }
        }
    }

    public enum RoleEnum {
        grassFairy(0), waterMagician(1), fireKnight(2), stoneMonster(3), lightningGiant(4);

        private int value = 0;

        private RoleEnum(int value) {    //    必须是private的，否则编译错误
            this.value = value;
        }

        public static RoleEnum valueOf(int value) {
            switch (value) {
                case 0:
                    return grassFairy;
                case 1:
                    return waterMagician;
                case 2:
                    return fireKnight;
                case 3:
                    return stoneMonster;
                case 4:
                    return lightningGiant;
                default:
                    return null;
            }
        }

        public int getValue() {
            return this.value;
        }

        public String toString(Context context) {
            switch(this) {
                case grassFairy:
                    return context.getString(R.string.grassFairy);
                case waterMagician:
                    return context.getString(R.string.waterMagician);
                case fireKnight:
                    return context.getString(R.string.fireKnight);
                case stoneMonster:
                    return context.getString(R.string.stoneMonster);
                case lightningGiant:
                    return context.getString(R.string.lightningGiant);
                default:
                    return "";
            }
        }
    }
}
