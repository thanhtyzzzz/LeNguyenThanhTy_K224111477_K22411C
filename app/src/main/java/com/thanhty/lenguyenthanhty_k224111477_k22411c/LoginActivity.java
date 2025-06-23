package com.thanhty.lenguyenthanhty_k224111477_k22411c;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.thanhty.connectors.AccountConnector;
import com.thanhty.connectors.SQLiteConnector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private RadioGroup radioGroupRole;
    private RadioButton radioAdmin, radioEmployee;
    private Button btnLogin;
    private SQLiteConnector dbConnector;
    private static final String DATABASE_NAME = "TelesalesDatabase.db";
    private static final String DB_PATH_SUFFIX = "/databases/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Khởi tạo các thành phần giao diện
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioEmployee = findViewById(R.id.radioEmployee);
        btnLogin = findViewById(R.id.btnLogin);
        dbConnector = new SQLiteConnector(this);

        // Sao chép cơ sở dữ liệu từ assets khi khởi động
        copyDatabase();

        // Xử lý sự kiện đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                int selectedRole = radioGroupRole.getCheckedRadioButtonId();

                if (selectedRole == -1) {
                    Toast.makeText(LoginActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
                    return;
                }

                int role = (selectedRole == R.id.radioAdmin) ? 1 : 2;
                AccountConnector accountConnector = new AccountConnector();
                if (accountConnector.authenticate(dbConnector.openDatabase(), username, password, role)) {
                    SQLiteDatabase db = dbConnector.openDatabase();
                    android.database.Cursor cursor = db.rawQuery("SELECT ID FROM Account WHERE Username = ? AND Password = ? AND TypeOfAccount = ?",
                            new String[]{username, password, String.valueOf(role)});
                    int accountId = -1;
                    if (cursor.moveToNext()) {
                        accountId = cursor.getInt(0);
                    }
                    cursor.close();
                    db.close();

                    if (role == 1) {
                        Intent intent = new Intent(LoginActivity.this, AdminTaskListActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(LoginActivity.this, EmployeeTaskActivity.class);
                        intent.putExtra("accountId", accountId);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void copyDatabase() {
        File dbFile = new File(getDatabasePath(DATABASE_NAME).getPath());
        Log.d("LoginActivity", "Database file path: " + dbFile.getAbsolutePath());
        if (!dbFile.exists()) {
            try {
                Log.d("LoginActivity", "Copying database from assets...");
                InputStream myInput = getAssets().open(DATABASE_NAME);
                String outFileName = getDatabasePath(DATABASE_NAME).getPath();

                File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
                if (!f.exists()) {
                    f.mkdir();
                    Log.d("LoginActivity", "Created directory: " + f.getAbsolutePath());
                }

                OutputStream myOutput = new FileOutputStream(outFileName);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }

                myOutput.flush();
                myOutput.close();
                myInput.close();
                Log.d("LoginActivity", "Database copied successfully to: " + outFileName);
                Toast.makeText(this, "Database copied successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("LoginActivity", "Error copying database: " + e.getMessage(), e);
                Toast.makeText(this, "Error copying database: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d("LoginActivity", "Database already exists at: " + dbFile.getAbsolutePath());
            Toast.makeText(this, "Database already exists", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean authenticateUser(String username, String password, int role) {
        SQLiteDatabase db = dbConnector.openDatabase();
        String[] columns = {"ID"};
        String selection = "Username = ? AND Password = ? AND TypeOfAccount = ?";
        String[] selectionArgs = {username, password, String.valueOf(role)};
        android.database.Cursor cursor = db.query("Account", columns, selection, selectionArgs, null, null, null);
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isValid;
    }
}