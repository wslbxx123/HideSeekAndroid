package dlmj.hideseek.Common.Model;

import java.util.List;

/**
 * Created by Two on 4/30/16.
 */
public class Record {
    private String mDate;

    private List<RecordItem> mRecordItems;

    public Record(String date, List<RecordItem> recordItems) {
        mDate = date;
        mRecordItems = recordItems;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public List<RecordItem> getRecordItems() {
        return mRecordItems;
    }
}
