package com.apollo.kalampich;

/**
 * Created by cpu on 12/12/2017.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "wpdb";
    public static final String Word_TABLE_NAME = "Words";

    private static String TAG = "DataBaseHelper"; // Tag just for the LogCat
    // window
    // destination path (location) of our database on device
    private static String DB_PATH = "";
    private static String DB_NAME = "wpdb";// Database name
    private SQLiteDatabase mDataBase;
    private final Context mContext;

    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        // db.execSQL("create table Words "
        // +"(code integer primary key, Word text,Meaning text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS Words");
        onCreate(db);
    }

    public boolean insertRecord(String Word, String Completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("Word", Word);
        contentValues.put("Completed", Completed);

        db.insert("Words", null, contentValues);
        return true;
    }

    public Cursor GetDetails(int code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from Words where Code= '" + code
                + "'", null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils
                .queryNumEntries(db, Word_TABLE_NAME);
        return numRows;
    }

    public boolean updateRecord(Integer Code, String Word, String Completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Word", Word);
        contentValues.put("Completed", Completed);
        db.update("Words", contentValues, "code = ? ",
                new String[] { Integer.toString(Code) });
        return true;
    }

    public boolean MarkAsCompleted(String Word) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Word", Word);
        contentValues.put("Completed", 1);
        db.update("Words", contentValues, "Word = ? ", new String[] { Word });
        return true;
    }

    public Integer deleteRecord(Integer Code) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("Words", "Code = ? ",
                new String[] { Integer.toString(Code) });
    }

    public Cursor getAllWords() {
        // ArrayList array_list = new ArrayList();
        // hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(
                "select Code as _id , Word, Completed from Words", null);
        res.moveToFirst();

        return res;
    }

    public Cursor GetUncompletedWord(int InitOrderNum) {
        SQLiteDatabase db = this.getReadableDatabase();
        String SqlStr;
        if(InitOrderNum == 0)
            SqlStr= "select * from Words where Completed = 0 order by ChapterNum, OrderNum";
        else
            SqlStr= "select * from Words where Completed = 1 AND OrderNum >= " + InitOrderNum + " order by ChapterNum, OrderNum";
        Cursor res = db.rawQuery(SqlStr, null);
        res.moveToFirst();

        return res;
    }


    public Cursor GetFirstCapterOrder(int InitChapter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String SqlStr;
        SqlStr= "select * from Words where ChapterNum = " + InitChapter + " order by ChapterNum, OrderNum";
        Cursor res = db.rawQuery(SqlStr, null);
        res.moveToFirst();

        return res;
    }


    public Cursor GetChapters() {
        SQLiteDatabase db = this.getReadableDatabase();
        String SqlStr = "select distinct ChapterNum as _id from Words order by ChapterNum";
        Cursor res = db.rawQuery(SqlStr, null);
        res.moveToFirst();

        return res;
    }



    public Cursor GetOtherWords(Integer WordCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        String SqlStr = "select * from OtherWords where WordCode = " + WordCode;
        Cursor res = db.rawQuery(SqlStr, null);
        res.moveToFirst();

        return res;
    }

//    public Cursor SearchTabirsByCodes(String CodeList) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String SqlStr = "select Code as _id , Word, Meaning from Tabirs where Code IN("
//                + CodeList + ")";
//        Cursor res = db.rawQuery(SqlStr, null);
//        res.moveToFirst();
//
//        return res;
//    }



    public void createDataBase() throws IOException {
        // If database not exists copy it from the assets

        boolean mDataBaseExist = checkDataBase();
        if (!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                // Copy the database from assests
                copyDataBase();
                Log.e(TAG, "createDatabase database created");
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    // Check that the database exists here: /data/data/your package/databases/Da
    // Name
    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        // Log.v("dbFile", dbFile + "   "+ dbFile.exists());
        return dbFile.exists();
    }

    // Copy the database from assets
    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    // Open the database, so we can query it
    public boolean openDataBase() throws SQLException {
        String mPath = DB_PATH + DB_NAME;
        // Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null,
                SQLiteDatabase.CREATE_IF_NECESSARY);
        // mDataBase = SQLiteDatabase.openDatabase(mPath, null,
        // SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

}