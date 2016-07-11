package dlmj.hideseek.DataAccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dlmj.hideseek.Common.Params.DatabaseParams;
import dlmj.hideseek.Common.Util.LogUtil;

/**
 * Created by Two on 5/10/16.
 */
public class DatabaseManager {
    private final static String ASSETS_NAME = "hideseek_cities.db";
    private final static String TAG = "DatabaseManager";
    private static DatabaseManager mInstance;

    private SQLiteDatabase mDatabase;
    private Context mContext;

    private DatabaseManager(Context context) {
        try{
            mContext = context;
            File dbf = new File(DatabaseParams.DB_PATH + DatabaseParams.HIDE_SEEK_DATABASE);

//            if(dbf.exists()) {
//                dbf.delete();
//            }

            if (!dbf.exists()) {
                copyCityDatabase();
            }
            mDatabase = context.openOrCreateDatabase(DatabaseParams.HIDE_SEEK_DATABASE,
                    Context.MODE_PRIVATE, null);
        } catch (IOException e) {
            throw new Error("数据库创建失败");
        }
    }

    public static DatabaseManager getInstance(Context context) {
        synchronized (DatabaseManager.class){
            if(mInstance == null){
                mInstance = new DatabaseManager(context);
            }
        }
        return mInstance;
    }

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    public void closeDatabase() {
        if(mDatabase != null) {
            mDatabase.close();
        }
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DatabaseParams.HIDE_SEEK_DATABASE, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            LogUtil.d(TAG, "The database doesn't exist");
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
    }

    private void copyCityDatabase() throws IOException {
        // Open your local db as the input stream
        InputStream myInput = mContext.getAssets().open(ASSETS_NAME);

        // Path to the just created empty db
        String outFileName = DatabaseParams.DB_PATH + DatabaseParams.HIDE_SEEK_DATABASE;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the input file to the output file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }
}
