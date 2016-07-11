package dlmj.hideseek.Common.Model;

/**
 * Created by Two on 5/25/16.
 */
public class ForeignCity implements Comparable<ForeignCity>{
    public String name;
    public String country;

    public ForeignCity(String name, String country) {
        this.name = name;
        this.country = country;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public int compareTo(ForeignCity city) {
        String aName = this.getName();
        String bName = city.getName();
        return aName.compareTo(bName);
    }
}
