package dlmj.hideseek.Common.Model;

/**
 * Created by Two on 5/10/16.
 */
public class DomesticCity implements Comparable<DomesticCity>{
    public String name;
    public String pinYin;

    public DomesticCity(String name, String pinYin) {
        this.name = name;
        this.pinYin = pinYin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinyin() {
        return pinYin;
    }

    public void setPinyin(String pinYin) {
        this.pinYin = pinYin;
    }

    @Override
    public int compareTo(DomesticCity city) {
        String aPinyin = this.getPinyin().substring(0, 1);
        String bPinyin = city.getPinyin().substring(0, 1);
        return aPinyin.compareTo(bPinyin);
    }
}
