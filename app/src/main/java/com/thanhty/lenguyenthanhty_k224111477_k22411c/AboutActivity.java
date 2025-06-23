package com.thanhty.lenguyenthanhty_k224111477_k22411c;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Khởi tạo các thành phần giao diện
        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        TextView txtStudentId = findViewById(R.id.txtStudentId);
        TextView txtStudentName = findViewById(R.id.txtStudentName);
        TextView txtEmail = findViewById(R.id.txtEmail);
        TextView txtClassName = findViewById(R.id.txtClassName);

        // Dữ liệu sinh viên
        String studentId = "K22411477";
        String studentName = "Le Nguyen Thanh Ty";
        String email = "tylnt22411c@st.uel.edu.vn";
        String className = "K22411C";

        // Gán dữ liệu vào TextView (sử dụng setText() đúng cách)
        txtStudentId.setText("Student ID: " + studentId);
        txtStudentName.setText("Student Name: " + studentName);
        txtEmail.setText("Email: " + email);
        txtClassName.setText("Class Name: " + className);

        // Đặt avatar (sử dụng ảnh từ drawable)
        // imgAvatar.setImageResource(R.drawable.ic_avatar); // Bỏ comment nếu có ảnh ic_avatar
    }
}