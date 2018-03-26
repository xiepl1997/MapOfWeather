package com.example.xpl.map;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private MyDatabaseHelper myDatabaseHelper;

    private EditText editText_firstname, editText_lastname, editText_phonenumber,
            editText_email, editText_password, editText_repassword;
    private Button buttonRegister = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

            myDatabaseHelper = new MyDatabaseHelper(this, "User.db", null, 1);

        buttonRegister = (Button)findViewById(R.id.button_register);
        editText_firstname = (EditText)findViewById(R.id.editText_register_firstname);
        editText_lastname = (EditText)findViewById(R.id.editText_register_lastname);
        editText_phonenumber = (EditText)findViewById(R.id.editText_register_phonenumber);
        editText_email = (EditText)findViewById(R.id.editText_register_email);
        editText_password = (EditText)findViewById(R.id.editText_register_password);
        editText_repassword = (EditText)findViewById(R.id.editText_register_repassword);

        buttonRegister.setOnClickListener(this);
    }
    //注册逻辑实现
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_register:
                if(editText_firstname.getText().toString().length()!=0&&editText_lastname.getText().toString().length()!=0
                        &&editText_phonenumber.getText().toString().length()!=0
                        &&editText_email.getText().toString().length()!=0
                        &&editText_password.getText().toString().length()!=0){
                    SQLiteDatabase database = myDatabaseHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    //组装数据
                    values.put("firstname", editText_firstname.getText().toString());
                    values.put("lastname", editText_lastname.getText().toString());
                    values.put("phonenum", editText_phonenumber.getText().toString());
                    values.put("email", editText_email.getText().toString());
                    values.put("password", editText_password.getText().toString());
                    database.insert("User", null, values);
                    values.clear();
                    Toast.makeText(getApplicationContext(), "注册成功！", Toast.LENGTH_LONG).show();

                    //转跳地图
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("firstname", editText_firstname.getText().toString());
                    bundle.putString("lastname", editText_lastname.getText().toString());
                    bundle.putString("phonenum", editText_phonenumber.getText().toString());
                    bundle.putString("email", editText_email.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                else
                    Toast.makeText(getApplicationContext(), "请将信息填写完整！", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
