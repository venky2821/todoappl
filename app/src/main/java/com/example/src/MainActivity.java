package com.example.src;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.justdoit.task_database.TaskContract;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView taskListView;
    private ArrayAdapter<String> taskAdapter;
    private TaskDatabaseHelper taskHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskListView = findViewById(R.id.list_todo);
        taskHelper = new TaskDatabaseHelper(this);
        taskAdapter = new ArrayAdapter<>(this, R.layout.todo_task, R.id.title_task, new ArrayList<>());
        taskListView.setAdapter(taskAdapter);

        initializeUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_task) {
            showAddTaskDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteTask(View view) {
        EditText taskEditText = view.findViewById(R.id.title_task);
        String task = taskEditText.getText().toString().trim();

        if (!task.isEmpty()) {
            deleteTaskFromDatabase(task);
        }
    }

    private void initializeUI() {
        updateTaskList();
    }

    private void showAddTaskDialog() {
        final EditText taskEditText = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle("Add A New Task")
                .setMessage("What do you want to do next?")
                .setView(taskEditText)
                .setPositiveButton("Add", (dialog, which) -> {
                    String task = taskEditText.getText().toString().trim();
                    if (!task.isEmpty()) {
                        addTaskToDatabase(task);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void addTaskToDatabase(String task) {
        SQLiteDatabase database = taskHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.TASK_TITLE, task);

        long newRowId = database.insert(TaskContract.TaskEntry.TABLE, null, values);

        if (newRowId != -1) {
            updateTaskList();
        }

        database.close();
    }

    private void deleteTaskFromDatabase(String task) {
        SQLiteDatabase database = taskHelper.getWritableDatabase();
        String selection = TaskContract.TaskEntry.TASK_TITLE + " = ?";
        String[] selectionArgs = {task};
        database.delete(TaskContract.TaskEntry.TABLE, selection, selectionArgs);
        database.close();
        updateTaskList();
    }

    private void updateTaskList() {
        taskAdapter.clear();

        SQLiteDatabase database = taskHelper.getReadableDatabase();
        Cursor cursor = database.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry.TASK_TITLE}, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex(TaskContract.TaskEntry.TASK_TITLE);
            String task = cursor.getString(index);
            taskAdapter.add(task);
        }

        cursor.close();
        database.close();
    }
}
