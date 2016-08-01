package dlmj.hideseek.Common.Model;

import android.content.Context;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import dlmj.hideseek.R;

/**
 * Created by Two on 5/1/16.
 */
public class User implements Serializable{
    private long mPKId;
    private String mPhone;
    private String mSessionId;
    private String mNickname;
    private String mPhotoUrl;
    private String mSmallPhotoUrl;
    private Date mRegisterDate;
    private SexEnum mSex;
    private String mRegion;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private RoleEnum mRole;
    private long mVersion;
    private String mPinyin;
    public int bomb_num;
    public String has_guide;

    public User(int bomb_num,String has_guide,long pkId, String phone,
                String sessionId, String nickname,
                String registerDate, RoleEnum role,
                long version, String pinyin) throws ParseException {
        mPKId = pkId;
        mPhone = phone;
        mSessionId = sessionId;
        mNickname = nickname;
        mRegisterDate = mDateFormat.parse(registerDate);
        mRole = role;
        mVersion = version;
        mPinyin = pinyin;
        this.bomb_num = bomb_num;
        this.has_guide = has_guide;
    }

    public User(long pkId, String nickname, String photoUrl, String smallPhotoUrl, SexEnum sex,
                String region, RoleEnum role, long version, String pinyin) {
        mPKId = pkId;
        mNickname = nickname;
        mPhotoUrl = photoUrl;
        mSmallPhotoUrl = smallPhotoUrl;
        mSex = sex;
        mRegion = region;
        mRole = role;
        mVersion = version;
        mPinyin = pinyin;
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
                default:
                    return "";
            }
        }
    }

    public enum RoleEnum {
        grassFairy(1), waterMagician(2), fireKnight(3), stoneMonster(4), lightningGiant(5);

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
