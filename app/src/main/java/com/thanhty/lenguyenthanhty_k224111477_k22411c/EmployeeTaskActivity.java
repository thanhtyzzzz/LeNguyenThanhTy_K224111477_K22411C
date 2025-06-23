package com.thanhty.lenguyenthanhty_k224111477_k22411c;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thanhty.connectors.SQLiteConnector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class EmployeeTaskActivity extends AppCompatActivity {

    private ListView lvCustomerTasks;
    private SQLiteConnector dbConnector;
    private ArrayAdapter<String> adapter;
    private int currentAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_task);

        lvCustomerTasks = findViewById(R.id.lvCustomerTasks);
        dbConnector = new SQLiteConnector(this);

        // Lấy AccountID từ đăng nhập (giả lập từ Intent)
        currentAccountId = getIntent().getIntExtra("accountId", -1);
        if (currentAccountId == -1) {
            Toast.makeText(this, "Invalid user session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load danh sách khách hàng
        loadCustomerTasks();

        // Xử lý click để gọi điện
        lvCustomerTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String phone = adapter.getItem(position).split("\n")[1].trim();
                updateCallStatus(phone);
            }
        });
    }

    private void loadCustomerTasks() {
        ArrayList<String> tasks = new ArrayList<>();
        SQLiteDatabase db = dbConnector.openDatabase();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d("EmployeeTaskActivity", "Loading tasks for AccountID: " + currentAccountId + ", Date: " + currentDate);
        android.database.Cursor cursor = db.rawQuery(
                "SELECT c.Name, c.Phone, tfd.IsCalled " +
                        "FROM TaskForTeleSales tfs " +
                        "JOIN TaskForTeleSalesDetails tfd ON tfs.ID = tfd.TaskForTeleSalesID " +
                        "JOIN Customer c ON tfd.CustomerID = c.ID " +
                        "WHERE tfs.AccountID = ? AND tfs.DateAssigned = ?",
                new String[]{String.valueOf(currentAccountId), currentDate});
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String phone = cursor.getString(1);
            int isCalled = cursor.getInt(2);
            String displayText = name + "\n" + phone + (isCalled == 0 ? " (Not Called)" : " (Called)");
            tasks.add(displayText);
            Log.d("EmployeeTaskActivity", "Found customer: " + displayText);
        }
        cursor.close();
        db.close();

        if (tasks.isEmpty()) {
            Log.w("EmployeeTaskActivity", "No tasks found for AccountID: " + currentAccountId);
            Toast.makeText(this, "No tasks assigned today", Toast.LENGTH_SHORT).show();
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tasks) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String item = tasks.get(position);
                if (item.contains("Not Called")) {
                    view.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                } else {
                    view.setBackgroundColor(getResources().getColor(android.R.color.white));
                }
                return view;
            }
        };
        lvCustomerTasks.setAdapter(adapter);
    }

    private void updateCallStatus(String phone) {
        SQLiteDatabase db = dbConnector.openDatabase();
        db.beginTransaction();
        try {
            android.database.Cursor cursor = db.rawQuery(
                    "SELECT tfd.ID, tfd.TaskForTeleSalesID, tfs.AccountID " +
                            "FROM TaskForTeleSalesDetails tfd " +
                            "JOIN TaskForTeleSales tfs ON tfd.TaskForTeleSalesID = tfs.ID " +
                            "JOIN Customer c ON tfd.CustomerID = c.ID " +
                            "WHERE c.Phone = ? AND tfs.AccountID = ?",
                    new String[]{phone, String.valueOf(currentAccountId)});
            if (cursor.moveToNext()) {
                int detailId = cursor.getInt(0);
                int taskId = cursor.getInt(1);
                db.execSQL("UPDATE TaskForTeleSalesDetails SET IsCalled = 1 WHERE ID = ?", new Object[]{detailId});

                // Kiểm tra và cập nhật IsCompleted nếu tất cả đã gọi
                cursor = db.rawQuery(
                        "SELECT COUNT(*) FROM TaskForTeleSalesDetails WHERE TaskForTeleSalesID = ? AND IsCalled = 0",
                        new String[]{String.valueOf(taskId)});
                int uncalledCount = cursor.moveToNext() ? cursor.getInt(0) : 0;
                cursor.close();
                if (uncalledCount == 0) {
                    db.execSQL("UPDATE TaskForTeleSales SET IsCompleted = 1 WHERE ID = ?", new Object[]{taskId});
                }
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Call marked as completed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating call status", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("EmployeeTaskActivity", "Error in updateCallStatus: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}