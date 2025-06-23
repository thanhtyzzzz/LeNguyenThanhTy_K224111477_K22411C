//package com.thanhty.lenguyenthanhty_k224111477_k22411c;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.os.Bundle;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.thanhty.connectors.SQLiteConnector;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//public class MainActivity extends AppCompatActivity {
//
//    private SQLiteConnector dbConnector;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        dbConnector = new SQLiteConnector(this);
//        copyDatabase();
//    }
//
//    private void copyDatabase() {
//        File dbFile = new File(dbConnector.getContext().getDatabasePath(dbConnector.DATABASE_NAME).getPath());
//        if (!dbFile.exists()) {
//            try {
//                copyDataBaseFromAsset();
//                Toast.makeText(this, "Database copied successfully", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                Toast.makeText(this, "Error copying database: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//
//    private void copyDataBaseFromAsset() throws IOException {
//        InputStream myInput = getAssets().open(dbConnector.DATABASE_NAME);
//        String outFileName = dbConnector.getContext().getDatabasePath(dbConnector.DATABASE_NAME).getPath();
//
//        File f = new File(dbConnector.getContext().getApplicationInfo().dataDir + dbConnector.DB_PATH_SUFFIX);
//        if (!f.exists()) {
//            f.mkdir();
//        }
//
//        OutputStream myOutput = new FileOutputStream(outFileName);
//        byte[] buffer = new byte[1024];
//        int length;
//        while ((length = myInput.read(buffer)) > 0) {
//            myOutput.write(buffer, 0, length);
//        }
//
//        myOutput.flush();
//        myOutput.close();
//        myInput.close();
//    }
//}