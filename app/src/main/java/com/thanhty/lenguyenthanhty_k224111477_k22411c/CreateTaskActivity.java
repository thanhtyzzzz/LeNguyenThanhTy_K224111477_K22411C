package com.thanhty.lenguyenthanhty_k224111477_k22411c;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thanhty.connectors.SQLiteConnector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class CreateTaskActivity extends AppCompatActivity {

    private EditText edtTaskTitle;
    private Spinner spinnerAssignTo;
    private Button btnSelectCustomer, btnCreateTask;
    private ListView lvSelectedCustomers;
    private SQLiteConnector dbConnector;
    private ArrayList<String> selectedCustomers = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        edtTaskTitle = findViewById(R.id.edtTaskTitle);
        spinnerAssignTo = findViewById(R.id.spinnerAssignTo);
        btnSelectCustomer = findViewById(R.id.btnSelectCustomer);
        btnCreateTask = findViewById(R.id.btnCreateTask);
        lvSelectedCustomers = findViewById(R.id.lvSelectedCustomers);
        dbConnector = new SQLiteConnector(this);

        // Cấu hình Spinner với danh sách nhân viên (lấy từ bảng Account)
        loadEmployees();

        // Cấu hình ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedCustomers);
        lvSelectedCustomers.setAdapter(adapter);

        // Xử lý nút Select Customer
        btnSelectCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRandomCustomers();
            }
        });

        // Xử lý nút Create Task
        btnCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTask();
            }
        });
    }

    private void loadEmployees() {
        ArrayList<String> employees = new ArrayList<>();
        SQLiteDatabase db = dbConnector.openDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT Username FROM Account WHERE TypeOfAccount = 2", null);
        while (cursor.moveToNext()) {
            employees.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, employees);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignTo.setAdapter(spinnerAdapter);
    }

    private void selectRandomCustomers() {
        selectedCustomers.clear();
        SQLiteDatabase db = dbConnector.openDatabase();
        android.database.Cursor cursor = db.rawQuery("SELECT Phone FROM Customer", null);
        ArrayList<String> allPhones = new ArrayList<>();
        while (cursor.moveToNext()) {
            allPhones.add(cursor.getString(0));
        }
        cursor.close();
        db.close();

        Random random = new Random();
        while (selectedCustomers.size() < 5 && !allPhones.isEmpty()) {
            int index = random.nextInt(allPhones.size());
            selectedCustomers.add(allPhones.remove(index));
        }
        adapter.notifyDataSetChanged();
    }

    private void createTask() {
        String taskTitle = edtTaskTitle.getText().toString().trim();
        String assignedTo = spinnerAssignTo.getSelectedItem().toString();
        if (taskTitle.isEmpty() || selectedCustomers.isEmpty()) {
            Toast.makeText(this, "Please fill task title and select customers", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbConnector.openDatabase();
        db.beginTransaction();
        try {
            // Lấy AccountID từ Username
            android.database.Cursor cursor = db.rawQuery("SELECT ID FROM Account WHERE Username = ?", new String[]{assignedTo});
            int accountId = -1;
            if (cursor.moveToNext()) {
                accountId = cursor.getInt(0);
            }
            cursor.close();

            if (accountId != -1) {
                // Thêm task vào TaskForTeleSales
                String dateAssigned = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                db.execSQL("INSERT INTO TaskForTeleSales (AccountID, TaskTitle, DateAssigned, IsCompleted) VALUES (?, ?, ?, 0)",
                        new Object[]{accountId, taskTitle, dateAssigned});

                // Lấy ID của task vừa tạo
                cursor = db.rawQuery("SELECT last_insert_rowid()", null);
                int taskId = -1;
                if (cursor.moveToNext()) {
                    taskId = cursor.getInt(0);
                }
                cursor.close();

                if (taskId != -1) {
                    // Thêm các customer vào TaskForTeleSalesDetails
                    for (String phone : selectedCustomers) {
                        cursor = db.rawQuery("SELECT ID FROM Customer WHERE Phone = ?", new String[]{phone});
                        int customerId = -1;
                        if (cursor.moveToNext()) {
                            customerId = cursor.getInt(0);
                        }
                        cursor.close();
                        if (customerId != -1) {
                            db.execSQL("INSERT INTO TaskForTeleSalesDetails (TaskForTeleSalesID, CustomerID, IsCalled) VALUES (?, ?, 0)",
                                    new Object[]{taskId, customerId});
                        }
                    }
                    db.setTransactionSuccessful();
                    Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, AdminTaskListActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error creating task: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}