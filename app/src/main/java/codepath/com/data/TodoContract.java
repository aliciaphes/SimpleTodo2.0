package codepath.com.data;

import android.provider.BaseColumns;

/**
 * Defines table and column names for the todo database.
 */
public class TodoContract {

    //class that defines the table contents of the todos table
    public static final class TodoEntry implements BaseColumns {

        public static final String TABLE_NAME = "todos";


        // Column with the primary key
        public static final String COLUMN_ID = "_id";

        // Title, stored as string
        public static final String COLUMN_TITLE = "title";

        // boolean to mark todo as urgent
        public static final String COLUMN_URGENT = "urgent";

    }
}
