package ke.co.imond.fingermap.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ke.co.imond.fingermap.Data.StudentContract.Students;

/**
 * Created by imond on 10/17/17.
 */


public class StuDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Pupils.db";

    public StuDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       db.execSQL("CREATE TABLE " + Students.TABLE_STUDENT_DETAILS +
                    " (" + Students._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Students.KEY_REG_NO + " TEXT," + Students.KEY_FIRST_NAME + " TEXT,"
                    + Students.KEY_LAST_NAME + " TEXT,"
                    + Students.KEY_PARENT_PHONE + " TEXT,"
                    + Students.KEY_CLASS_LEVEL + " TEXT,"
                    + Students.KEY_FINGER1 + " TEXT,"
                    + Students.KEY_FINGER2 + " TEXT);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        First lets drop table if it exists
        db.execSQL("DROP TABLE IF EXIST " + Students.TABLE_STUDENT_DETAILS);

//        Create Table Again
        onCreate(db);
    }
//    method for adding new student
    public boolean addNewStudent(String reg_no, String first_name, String last_name, String parent_phone, String class_level, String finger1, String finger2) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(Students.KEY_REG_NO, reg_no);
        values.put(Students.KEY_FIRST_NAME, first_name);
        values.put(Students.KEY_LAST_NAME, last_name);
        values.put(Students.KEY_PARENT_PHONE, parent_phone);
        values.put(Students.KEY_CLASS_LEVEL, class_level);
        values.put(Students.KEY_FINGER1, finger1);
        values.put(Students.KEY_FINGER2, finger2);


        //Inserting data one row at a go
        long result = db.insert(Students.TABLE_STUDENT_DETAILS, null, values);
        if (result == -1)
            return false;
        else
            return true;

    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+ Students.TABLE_STUDENT_DETAILS, null);
        return res;
    }


}
