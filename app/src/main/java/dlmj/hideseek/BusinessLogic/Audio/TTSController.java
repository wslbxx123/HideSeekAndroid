package dlmj.hideseek.BusinessLogic.Audio;

import android.content.Context;
import android.os.Bundle;

import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechListener;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;


import dlmj.hideseek.R;

/**
 * Created by Two on 5/18/16.
 */
public class TTSController implements SynthesizerListener, AMapNaviListener {
    private static TTSController mInstance;
    private Context mContext;
    private SpeechSynthesizer mSpeechSynthesizer;
    private boolean mIfFinish = true;

    private SpeechListener mListener = new SpeechListener() {
        @Override
        public void onEvent(int i, Bundle bundle) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }
    };

    public static TTSController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TTSController(context);
        }
        return mInstance;
    }

    TTSController(Context context) {
        mContext = context;
    }

    public void init() {
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "=" +
                mContext.getString(R.string.audio_key));
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext, null);
        initSpeechSynthesizer();
    }

    private void initSpeechSynthesizer() {
        mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "80");
    }

    public void playText(String playText) {
        if (!mIfFinish) {
            return;
        }
        if (null == mSpeechSynthesizer) {
            // 创建合成对象.
            mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext, null);
            initSpeechSynthesizer();
        }
        mSpeechSynthesizer.startSpeaking(playText, this);

    }

    public void startSpeaking() {
        mIfFinish = true;
    }

    public void stopSpeaking() {
        if (mSpeechSynthesizer != null)
            mSpeechSynthesizer.stopSpeaking();
    }

    public void destroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stopSpeaking();
        }
    }

    @Override
    public void onSpeakBegin() {
        mIfFinish = false;
    }

    @Override
    public void onBufferProgress(int i, int i2, int i3, String s) {

    }

    @Override
    public void onSpeakPaused() {

    }

    @Override
    public void onSpeakResumed() {

    }

    @Override
    public void onSpeakProgress(int i, int i2, int i3) {

    }

    @Override
    public void onCompleted(SpeechError speechError) {
        mIfFinish = true;
    }

    @Override
    public void onEvent(int i, int i2, int i3, Bundle bundle) {

    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String str) {
        this.playText(str);
    }

    @Override
    public void onEndEmulatorNavi() {
        this.playText("导航结束");
    }

    @Override
    public void onArriveDestination() {
        this.playText("到达目的地");
    }

    @Override
    public void onCalculateRouteSuccess() {
        String calculateResult = "路径计算就绪";

        this.playText(calculateResult);
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        this.playText("路径计算失败，请检查网络或输入参数");
    }

    @Override
    public void onReCalculateRouteForYaw() {
        this.playText("您已偏航");
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        this.playText("前方路线拥堵，路线重新规划");
    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes2) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }
}
