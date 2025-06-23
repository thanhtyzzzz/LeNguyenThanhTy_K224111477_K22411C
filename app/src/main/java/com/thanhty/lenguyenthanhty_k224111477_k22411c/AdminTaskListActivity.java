package com.thanhty.lenguyenthanhty_k224111477_k22411c;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thanhty.connectors.SQLiteConnector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AdminTaskListActivity extends AppCompatActivity {

    private ListView lvTasks;
    private Button btnCreateTask;
    private SQLiteConnector dbConnector;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_task_list);

        lvTasks = findViewById(R.id.lvTasks);
        btnCreateTask = findViewById(R.id.btnCreateTask);
        dbConnector = new SQLiteConnector(this);

        // Lấy danh sách nhiệm vụ từ SQLite
        loadTasks();

        // Xử lý nút tạo nhiệm vụ mới
        btnCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminTaskListActivity.this, CreateTaskActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadTasks() {
        ArrayList<String> tasks = new ArrayList<>();
        SQLiteDatabase db = dbConnector.openDatabase();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        android.database.Cursor cursor = db.rawQuery(
                "SELECT TaskTitle, IsCompleted FROM TaskForTeleSales WHERE DateAssigned = ?",
                new String[]{currentDate});
        int count = cursor.getCount();
        Log.d("AdminTaskListActivity", "Number of tasks for " + currentDate + ": " + count);
        while (cursor.moveToNext()) {
            String taskTitle = cursor.getString(0);
            int isCompleted = cursor.getInt(1);
            String displayText = taskTitle + (isCompleted == 1 ? " (Completed)" : "");
            tasks.add(displayText);
            Log.d("AdminTaskListActivity", "Task: " + displayText);
        }
        cursor.close();
        db.close();

        if (tasks.isEmpty()) {
            Log.d("AdminTaskListActivity", "No tasks found for date: " + currentDate);
            Toast.makeText(this, "No tasks available for today", Toast.LENGTH_SHORT).show();
        }

        // Adapter tùy chỉnh để đổi màu nền khi IsCompleted=1
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tasks) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String item = tasks.get(position);
                if (item.contains("Completed")) {
                    view.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    view.setBackgroundColor(getResources().getColor(android.R.color.white));
                }
                return view;
            }
        };
        lvTasks.setAdapter(adapter);
    }
}