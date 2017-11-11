package ke.co.imond.fingermap.Data;

import android.provider.BaseColumns;

/**
 * Created by root on 10/19/17.
 */

public class StudentContract  {
        public static final class Students implements BaseColumns {
            public static final String TABLE_STUDENT_DETAILS = "students";
            //    Table columns
            public static final String KEY_REG_NO = "REG_NO";
            public static final String KEY_FIRST_NAME = "FIRST_NAME";
            public static final String KEY_LAST_NAME = "LAST_NAME";
            public static final String KEY_PARENT_PHONE = "PARENT_PHONE";
            public  static final String KEY_CLASS_LEVEL = "CLASS_LEVEL";
            public static final String KEY_FINGER1 = "FINGER1";
            public static final String KEY_FINGER2 = "FINGER2";
        }
}
