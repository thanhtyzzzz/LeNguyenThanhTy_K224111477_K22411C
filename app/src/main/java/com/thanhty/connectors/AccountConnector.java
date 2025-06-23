package com.thanhty.connectors;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AccountConnector {
    public boolean authenticate(SQLiteDatabase database, String username, String password, int role) {
        Cursor cursor = database.rawQuery(
                "SELECT * FROM Account WHERE Username = ? AND Password = ? AND TypeOfAccount = ?",
                new String[]{username, password, String.valueOf(role)});
        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        return isValid;
    }
}