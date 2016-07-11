package dlmj.hideseek.UI.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;

import java.util.List;

import dlmj.hideseek.Common.Model.DomesticCity;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.DataAccess.DomesticCityTableManager;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/11/16.
 */
public class PersonListAdapter extends BaseAdapter implements AMapLocationListener{
    private final static String TAG = "PersonListAdapter";
    private final static int VIEW_TYPE_COUNT = 5;
    private Context mContext;
    private List<DomesticCity> mCityList;
    private List<DomesticCity> mRecentList;
    private List<DomesticCity> mHotList;
    private View.OnClickListener mLocationButtonClickListener;
    private LocationProcessEnum mLocationProcess = LocationProcessEnum.locating;
    private DomesticCityTableManager mDomesticCityTableManager;
    private Intent mIntent;
    private String mRegion;
    private AMapLocationClient mLocationClient;

    public PersonListAdapter(Context context, List<DomesticCity> cityList, List<DomesticCity> recentList,
                             List<DomesticCity> hotList, Intent intent) {
        this.mContext = context;
        this.mCityList = cityList;
        this.mRecentList = recentList;
        this.mHotList = hotList;
        this.mDomesticCityTableManager = DomesticCityTableManager.getInstance(mContext);
        this.mIntent = intent;

        mLocationButtonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mLocationProcess == LocationProcessEnum.located) {
                    DomesticCity city = new DomesticCity(mRegion, "0");
                    mDomesticCityTableManager.insertRecentCity(city);

                    mIntent.putExtra(IntentExtraParam.REGION_NAME, city.getName());
                    ((Activity)mContext).setResult(Activity.RESULT_OK, mIntent);
                    ((Activity)mContext).finish();
                } else {
                    mLocationClient.stopLocation();
                    mLocationClient.startLocation();
                }
            }
        };
    }

    @Override
    public int getCount() {
        if(mCityList != null) {
            return mCityList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mCityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return position < 4 ? position : 4;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        int viewType = getItemViewType(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);

        switch(viewType) {
            case 0:
                convertView = inflater.inflate(R.layout.location_item, null);
                TextView locationTextView = (TextView) convertView.findViewById(R.id.locationTextView);
                locationTextView.setText(mContext.getString(R.string.locating));
                Button locationButton = (Button) convertView.findViewById(R.id.locationButton);
                locationButton.setOnClickListener(mLocationButtonClickListener);
                ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

                switch(mLocationProcess) {
                    case locating:
                        locationButton.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        locationTextView.setText(mContext.getString(R.string.locating));
                        break;
                    case located:
                        locationButton.setVisibility(View.VISIBLE);
                        locationButton.setText(mRegion);
                        progressBar.setVisibility(View.GONE);
                        locationTextView.setText(mContext.getString(R.string.current_city));
                        break;
                    case failedLocated:
                    default:
                        locationButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        locationButton.setText(mContext.getString(R.string.re_locate));
                        locationTextView.setText(mContext.getString(R.string.error_location_failed ));
                        break;
                }

                break;
            case 1:
                convertView = inflater.inflate(R.layout.grid_city, null);
                GridView recentGridView = (GridView) convertView.findViewById(R.id.cityGridView);
                recentGridView.setAdapter(new GridCityAdapter(mContext, this.mRecentList));
                recentGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        DomesticCity city = mRecentList.get(position);
                        mDomesticCityTableManager.insertRecentCity(city);

                        mIntent.putExtra(IntentExtraParam.REGION_NAME, city.getName());
                        ((Activity)mContext).setResult(Activity.RESULT_OK, mIntent);
                        ((Activity)mContext).finish();
                    }
                });
                break;
            case 2:
                convertView = inflater.inflate(R.layout.grid_city, null);
                GridView hotGridView = (GridView) convertView.findViewById(R.id.cityGridView);
                TextView hotCityHint = (TextView) convertView.findViewById(R.id.cityHint);
                hotCityHint.setText(mContext.getString(R.string.hot_cities));
                hotGridView.setAdapter(new GridCityAdapter(mContext, this.mHotList));
                hotGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        DomesticCity city = mHotList.get(position);
                        mDomesticCityTableManager.insertRecentCity(city);

                        mIntent.putExtra(IntentExtraParam.REGION_NAME, city.getName());
                        ((Activity)mContext).setResult(Activity.RESULT_OK, mIntent);
                        ((Activity)mContext).finish();
                    }
                });
                break;
            case 3:
                convertView = inflater.inflate(R.layout.total_city_item, null);
                break;
            default:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.city_item, null);
                    viewHolder = new ViewHolder();
                    viewHolder.mAlphaTextView = (TextView) convertView.findViewById(R.id.alphaTextView);
                    viewHolder.mNameTextView = (TextView) convertView.findViewById(R.id.nameTextVIew);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                viewHolder.mNameTextView.setText(mCityList.get(position).getName());
                String currentStr = PinYinUtil.getAlpha(mCityList.get(position).getPinyin());
                String previewStr = (position - 1) >= 0 ? PinYinUtil.getAlpha(mCityList
                        .get(position - 1).getPinyin()) : " ";
                if (!previewStr.equals(currentStr)) {
                    viewHolder.mAlphaTextView.setVisibility(View.VISIBLE);
                    viewHolder.mAlphaTextView.setText(currentStr);
                } else {
                    viewHolder.mAlphaTextView.setVisibility(View.GONE);
                }
                break;
        }

        return convertView;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(aMapLocation != null) {
            if(aMapLocation.getErrorCode() ==  0) {
                if(!aMapLocation.getCity().equals(mRegion)) {
                    mRegion = aMapLocation.getCity();

                    if(mRegion.indexOf("市") == mRegion.length() - 1) {
                        mRegion = mRegion.substring(0, mRegion.length() - 1);
                    }
                    mLocationProcess = LocationProcessEnum.located;
                    LogUtil.d(TAG, mRegion);
                }
            }
            else{
                String errText = "定位失败，" + aMapLocation.getErrorCode() + ": " +
                        aMapLocation.getErrorInfo();

                mLocationProcess = LocationProcessEnum.failedLocated;
                LogUtil.d(TAG, errText);
            }
        } else {
            mLocationProcess = LocationProcessEnum.failedLocated;
        }

        notifyDataSetChanged();
    }

    public void setLocationClient(AMapLocationClient locationClient) {
        locationClient.setLocationListener(this);
        mLocationClient = locationClient;
    }

    private class ViewHolder {
        TextView mAlphaTextView;
        TextView mNameTextView;
    }

    public enum LocationProcessEnum {
        locating(1), located(2), failedLocated(3);

        private int value = 0;

        private LocationProcessEnum(int value) {    //    必须是private的，否则编译错误
            this.value = value;
        }

        public static LocationProcessEnum valueOf(int value) {
            switch (value) {
                case 1:
                    return locating;
                case 2:
                    return located;
                case 3:
                default:
                    return failedLocated;
            }
        }

        public int value() {
            return this.value;
        }
    }
}
