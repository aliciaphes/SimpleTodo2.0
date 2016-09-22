package codepath.com.simpletodo20;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codepath.com.data.Todo;
import codepath.com.data.TodoDbHelper;

/**
 * This is the main class and the one that will be "carrying the weight" of the application
 */
public class MainActivity extends AppCompatActivity {

    //constants for the different actions:
    public static final char ACTION_DELETE = 'd';
    public static final char ACTION_UPDATE = 'u';
    public static final char ACTION_CREATE = 'c';

    //additional constants:
    private static final String EMPTY_STRING = "";
    private static final String TAG = MainActivity.class.getSimpleName();

    //attributes:
    private static TodoDbHelper todoDBHelper;

    private ArrayList<Todo> todoList;
    private List<Map<String, String>> todoListVisible;
    private UrgentTodoAdapter todoAdapter;
    private ListView lvTodos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set layout
        setContentView(R.layout.activity_main);

        //initialize database helper
        todoDBHelper = new TodoDbHelper(this);

        //retrieve all todos
        todoList = todoDBHelper.readAllTodos();

        //build visual list of todos:
        initializeList();

        //set listener on 'Add' button:
        setButtonAction();

    }

    private void setButtonAction() {

        Button button = (Button) findViewById(R.id.btn_add_update_todo);

        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //openAddOrUpdateDialog(v, null, ACTION_CREATE);
                    openAddOrUpdateDialog(null, ACTION_CREATE);
                }
            });
        } else {
            Log.e(TAG, getString(R.string.button_not_found));
        }
    }


    private void openDeleteDialog(final int position) {

        //build dialog:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_delete)).setTitle(getString(R.string.delete_todo));

        // Add the buttons
        builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked delete button
                Todo toDelete = todoList.get(position);//retrieve todo that was clicked

                long resultOfDeletion = todoDBHelper.todoCRUD(toDelete, ACTION_DELETE);
                if (resultOfDeletion != -1L) {

                    refreshListWith(toDelete, ACTION_DELETE);
                    Toast.makeText(MainActivity.this, R.string.todo_deleted, Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog: do nothing
            }
        });

        AlertDialog deleteDialog = builder.create();
        deleteDialog.show();
    }


    // Add list of todos to visible ListView in the shape of:
    // - title
    // - subtitle --> indicates if to-do is urgent
    // use of custom adapter for the color red
    private void initializeList() {

        todoListVisible = new ArrayList<Map<String, String>>();

        for (Todo todo : todoList) {
            Map<String, String> todoInfo = new HashMap<String, String>(2);
            todoInfo.put(getString(R.string.title), todo.getTitle());
            todoInfo.put(getString(R.string.urgent), todo.isUrgent() ? getString(R.string.urgent) : EMPTY_STRING);
            todoListVisible.add(todoInfo);
        }
        todoAdapter = new UrgentTodoAdapter(this, todoListVisible,
                android.R.layout.simple_list_item_2,
                new String[]{getString(R.string.title), getString(R.string.urgent)},
                new int[]{android.R.id.text1, android.R.id.text2});


        lvTodos = (ListView) findViewById(R.id.lv_todos);
        if (lvTodos != null) {
            lvTodos.setAdapter(todoAdapter);
            setupListViewListener();//set listener for actions to perform on the todos
        }
    }

    //set actions to perform when to-do is clicked: longclick and single click are available
    private void setupListViewListener() {
        //long clicking on an to-do deletes it (and updates accordingly):
        lvTodos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {

                openDeleteDialog(pos);//send position of the to-do that was clicked
                return true;
            }
        });

        //simple click launches the update to-do action:
        lvTodos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {
                Todo todo = todoList.get(pos);
                //openAddOrUpdateDialog(lvTodos, todo, ACTION_UPDATE);
                openAddOrUpdateDialog(todo, ACTION_UPDATE);
            }
        });
    }


    //private void openAddOrUpdateDialog(View v, final Todo t, final char action) {
    private void openAddOrUpdateDialog(final Todo t, final char action) {

        //set layout to display:
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.activity_addupdate_dialog, null);

        //create 'hooks' to retrieve the information they contain to use in the listeners below
        final EditText etNewItem = (EditText) textEntryView.findViewById(R.id.todo_title);
        final CheckBox urgentCheckbox = (CheckBox) textEntryView.findViewById(R.id.checkbox_add);

        //build dialog
        AlertDialog.Builder db = new AlertDialog.Builder(this)
                .setView(textEntryView)
                .setTitle(action == ACTION_CREATE ? getString(R.string.add_todo) : getString(R.string.update_todo));

        //initialize values of the dialog depending on the action:
        String title = EMPTY_STRING;

        if (action == ACTION_CREATE) {
            title = getString(R.string.add);
            //retrieve text value
            EditText et = (EditText) findViewById(R.id.et_new_todo);
            String todoText;
            if (et != null) {
                todoText = et.getText().toString();

                etNewItem.setText(todoText);
                etNewItem.setSelection(todoText.length());
            }
        } else if (action == ACTION_UPDATE) {
            title = getString(R.string.update);

            etNewItem.setText(t.getTitle());
            etNewItem.setSelection(t.getTitle().length());

            urgentCheckbox.setChecked(t.isUrgent());
        }



        //no listener are assigned to the buttons; behavior will be handled by OnShowListener

        //set 'add' and 'cancel' buttons:
        db.setPositiveButton(title, null);
        db.setNegativeButton(R.string.cancel, null);

        //we will use the neutral button to send to-do as email using an Intent
        db.setNeutralButton(getString(R.string.email), null);

        //create dialog from builder:
        final AlertDialog addOrUpdateDialog = db.create();

        //set up listeners:
        addOrUpdateDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button positive = addOrUpdateDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {// User clicked OK button
                        long newId;
                        boolean isChecked;

                        if (action == ACTION_CREATE) {

                            String newText = etNewItem.getText().toString();

                            if (newText.length() != 0) {//make sure some text was entered
                                //retrieve value of checkbox
                                isChecked = urgentCheckbox.isChecked();
                                Todo newTodo = new Todo(-1L, newText, isChecked);

                                //add it to the database
                                newId = todoDBHelper.todoCRUD(newTodo, ACTION_CREATE);
                                if (newId != -1L) {//insertion successful
                                    newTodo.setId(newId);
                                    //refresh the visible list
                                    refreshListWith(newTodo, ACTION_CREATE);
                                    Toast.makeText(MainActivity.this, R.string.todo_created, Toast.LENGTH_LONG).show();
                                }
                                //Dismiss once everything is OK
                                addOrUpdateDialog.dismiss();

                            } else {
                                Toast.makeText(MainActivity.this, R.string.empty_todo, Toast.LENGTH_LONG).show();
                            }


                        } else if (action == ACTION_UPDATE) {

                            String newText = etNewItem.getText().toString();

                            if (newText.length() != 0) {//make sure some text was entered
                                t.setTitle(newText);
                                isChecked = urgentCheckbox.isChecked();
                                t.setUrgency(isChecked);
                                //update to-do in the database
                                newId = todoDBHelper.todoCRUD(t, ACTION_UPDATE);
                                if (newId != -1L) {//update successful
                                    //refresh the visible list
                                    refreshListWith(t, ACTION_UPDATE);
                                    Toast.makeText(MainActivity.this, R.string.todo_updated, Toast.LENGTH_LONG).show();
                                    //Dismiss once everything is OK
                                    addOrUpdateDialog.dismiss();
                                }
                            }

                            else{
                                Toast.makeText(MainActivity.this, R.string.empty_todo, Toast.LENGTH_LONG).show();
                            }
                        }
                        cleanTextArea();
                    }
                });

                Button negative = addOrUpdateDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negative.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // User cancelled the dialog
                        addOrUpdateDialog.dismiss();
                        cleanTextArea();
                    }
                });
                Button neutral = addOrUpdateDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutral.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        //retrieve current text of the to-do
                        String newTitle = etNewItem.getText().toString();

                        if (newTitle.length() != 0) {//make sure some text was entered
                            //create intent that will be launched
                            Intent emailTodoIntent = new Intent(Intent.ACTION_SENDTO);

                            emailTodoIntent.setData(Uri.parse("mailto:")); // only allow email apps

                            boolean isChecked = urgentCheckbox.isChecked();

                            String urgent = (isChecked ? getString(R.string.subject_urgent) : EMPTY_STRING);
                            emailTodoIntent.putExtra(Intent.EXTRA_SUBJECT, urgent + getString(R.string.do_not_forget));

                            //if any of the data has changed, update to-do before sending:
                            //NO BECAUSE AFTER SENDING THE DIALOG WILL STILL BE OPEN IF WE WANT TO MODIFY ANYTHING
//                        if (t.getTitle() != newTitle || t.isUrgent() != isChecked) {
//                            t.setTitle(newTitle);
//                            t.setUrgency(isChecked);
//                            todoDBHelper.todoCRUD(t, ACTION_UPDATE);
//                        }

                            //emailTodoIntent.putExtra(Intent.EXTRA_TEXT, t.getTitle());
                            emailTodoIntent.putExtra(Intent.EXTRA_TEXT, newTitle);

                            if (emailTodoIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(emailTodoIntent);
                            }
                        }

                        else{
                            Toast.makeText(MainActivity.this, R.string.empty_todo, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


        addOrUpdateDialog.show();
    }


    //clean text area
    private void cleanTextArea() {
        EditText etNewItem = (EditText) findViewById(R.id.et_new_todo);
        etNewItem.setText(EMPTY_STRING);
    }


    //this function updates the visible list of todos
    private void refreshListWith(Todo newTodo, char action) {

        int index;
        Map<String, String> todoInfo = new HashMap<String, String>(2);

        todoInfo.put(getString(R.string.title), newTodo.getTitle());
        todoInfo.put(getString(R.string.urgent), newTodo.isUrgent() ? getString(R.string.urgent) : EMPTY_STRING);

        switch (action) {
            case ACTION_CREATE:
                todoList.add(newTodo);
                todoListVisible.add(todoInfo);
                break;

            case ACTION_UPDATE:
                //modify the corresponding to-do in todoList
                index = todoList.indexOf(newTodo);
                if (index != -1) {
                    todoList.set(index, newTodo);
                    todoListVisible.set(index, todoInfo);
                }
                break;

            case ACTION_DELETE:
                //remove newTodo from todos
                index = todoList.indexOf(newTodo);
                if (index != -1) {
                    todoList.remove(index);
                    todoListVisible.remove(index);
                }
                break;

            default:
                return;
        }

        todoAdapter.notifyDataSetChanged();//update visibility of todos
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        todoDBHelper.close();
    }

}