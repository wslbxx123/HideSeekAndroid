package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import dlmj.hideseek.Util.MD5Utils;
import dlmj.hideseek.Util.UiUtil;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/20 15:11
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ShopCache  {
    private static final String TAG = "ShopCache";
    private static ShopCache mInstance;

    public ShopCache(Context context) {
        super();
    }

    public static ShopCache getInstance(Context context){
        synchronized (ShopCache.class){
            if(mInstance == null){
                mInstance = new ShopCache(context);
            }
        }
        return mInstance;
    }

    public static void saveCache(String json,String url) {
        //TODO:保存缓存
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(getCacheFile(url)));
            //第一行保存时间
            writer.write(System.currentTimeMillis() + "");
            writer.write("\r\n");
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(writer!=null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static File getCacheFile(String url) {
        //dir filename
        File dir = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //has sdcard
            dir = new File(Environment.getExternalStorageDirectory(), "Android/data/" + UiUtil.getPackName() + "/json");
        }else{
            //rom
            dir = new File(UiUtil.getCacheDir(),"json");
        }
        if(!dir.exists()){
            dir.mkdirs();
        }
        //缓存的文件名，一般都是对应页面的接口（url),不能直接用url，看到路径，恶意攻击程序
        String fileName = MD5Utils.encode(url);
        File file = new File(dir, fileName);
        return file;
    }

    public static String getDataFromLocal(String url) {
        //1.找到缓存所在的文件
        File cache = getCacheFile(url);
        //2.从文件中读取json
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(cache));
            String time = reader.readLine();
            //方便演示，使用20秒
            if(System.currentTimeMillis() - Long.parseLong(time) > 200 * 1000){
                //cache is outdate
                Log.e(TAG,"缓存过期啦");
                return null;
            }
            String json = reader.readLine();
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}