package codepath.com.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import codepath.com.data.TodoContract.TodoEntry;
import codepath.com.simpletodo20.MainActivity;

/**
 * Manage a local database for todo data extending Android class
 */
public class TodoDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "todos.db";
    //database version must incremented if schema changes.
    private static final int DATABASE_VERSION = 2;
    public final String TAG = TodoDbHelper.class.getSimpleName();


    //constructor
    public TodoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //build SQL query to create table
        final String SQL_CREATE_TODO_TABLE = "CREATE TABLE " + TodoEntry.TABLE_NAME + " (" +

                TodoEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TodoEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                TodoEntry.COLUMN_URGENT + " INTEGER NOT NULL);";

        try {
            sqLiteDatabase.execSQL(SQL_CREATE_TODO_TABLE);
        } catch (SQLException e) {
            Log.e(TAG, "Error creating table in database");
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //Action to do when database version increases
        //This is a small application, so in case the schema is updated, the data will be wiped
        //If that is not wanted, the next two lines can be commented out
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TodoEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    //Read contents from database and return an array of todos
    public ArrayList<Todo> readAllTodos() {

        ArrayList<Todo> todos = new ArrayList<Todo>();

        SQLiteDatabase db = getReadableDatabase();

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                TodoEntry.TABLE_NAME,// Table to query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                TodoEntry.COLUMN_ID + " ASC" // sort order (ascending)
        );


        try {
            if (cursor.moveToFirst()) {
                do {
                    //retrieve data from cursor received and add it to the list
                    long id = cursor.getLong(cursor.getColumnIndex(TodoEntry.COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndex(TodoEntry.COLUMN_TITLE));
                    boolean urgent = (cursor.getInt(cursor.getColumnIndex(TodoEntry.COLUMN_URGENT)) != 0);

                    Todo newTodo = new Todo(id, title, urgent);

                    todos.add(newTodo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get todos from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return todos;
    }


    public void cleanDatabase() {
        //wipe the data
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TodoEntry.TABLE_NAME);
        onCreate(db);
    }


    //Dummy function to add one todo to the database
    public long createDummyTodo(Todo t) {
        return todoCRUD(t, MainActivity.ACTION_CREATE);
    }


    //this function performs the typical operations:
    //creates, deletes and updates the database
    public long todoCRUD(Todo t, char action) {

        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        long operationResult = -1L;

        // wrap operation in a transaction.
        // This helps with performance and ensures consistency of the database.
        db.beginTransaction();
        try {
            switch (action) {
                case MainActivity.ACTION_DELETE:
                    operationResult = db.delete(TodoEntry.TABLE_NAME, TodoEntry.COLUMN_ID + "=" + t.getId(), null);
                    break;

                case MainActivity.ACTION_UPDATE:

                    values.put(TodoEntry.COLUMN_TITLE, t.getTitle());
                    values.put(TodoEntry.COLUMN_URGENT, t.isUrgent());

                    operationResult = db.update(TodoEntry.TABLE_NAME, values, TodoEntry.COLUMN_ID + "=" + t.getId(), null);
                    break;

                case MainActivity.ACTION_CREATE:

                    values.put(TodoEntry.COLUMN_TITLE, t.getTitle());
                    values.put(TodoEntry.COLUMN_URGENT, t.isUrgent());

                    //SQLite auto increments the primary key column
                    operationResult = db.insertOrThrow(TodoEntry.TABLE_NAME, null, values);
                    break;
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to access the database");
        } finally {
            db.endTransaction();
        }

        return operationResult;
    }
}
