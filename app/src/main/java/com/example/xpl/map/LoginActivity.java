package com.example.xpl.map;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Button buttonLogin = null;
    private TextView textViewRegisterTip = null;
    private EditText editText_account = null;
    private EditText editText_password = null;

    private MyDatabaseHelper myDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        myDatabaseHelper = new MyDatabaseHelper(this, "User.db", null, 1);
        //起始创建数据库
        SQLiteDatabase database = myDatabaseHelper.getReadableDatabase();

        buttonLogin = (Button)findViewById(R.id.button_login);
        textViewRegisterTip = (TextView)findViewById(R.id.textView_registertip);
        editText_account = (EditText)findViewById(R.id.editText_account);
        editText_password = (EditText)findViewById(R.id.editText_password);

        buttonLogin.setOnClickListener(this);
        textViewRegisterTip.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String firstname=null, lastname=null, phonenum=null, email=null;
        switch(v.getId()){
            case R.id.button_login:
                if(editText_account.getText().toString().length()!=0&&editText_password.getText().toString().length()!=0){
                    int flag = 0;
                    //查询User表中是否有所输入的帐号信息，有则提示并进入地图，否则提示不存在
                    SQLiteDatabase database1 = myDatabaseHelper.getReadableDatabase();
                    Cursor cursor = database1.rawQuery("select * from User where phonenum=? and password=?",
                            new String[]{editText_account.getText().toString(), editText_password.getText().toString()});
                    while(cursor.moveToNext()){
                        firstname = cursor.getString(cursor.getColumnIndex("firstname"));
                        lastname = cursor.getString(cursor.getColumnIndex("lastname"));
                        phonenum = cursor.getString(cursor.getColumnIndex("phonenum"));
                        email = cursor.getString(cursor.getColumnIndex("email"));
                        database1.close();
                        flag = 1;
                        Toast.makeText(getApplicationContext(), "登录成功！", Toast.LENGTH_LONG).show();
                    }
                    if(flag == 0){
                        Toast.makeText(getApplicationContext(), "帐号不存在！", Toast.LENGTH_LONG).show();
                    }
                    else {
                        //将成功登录的用户信息传给mainactivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("firstname", firstname);
                        bundle.putString("lastname", lastname);
                        bundle.putString("phonenum", phonenum);
                        bundle.putString("email", email);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "请完整填写帐号密码！", Toast.LENGTH_LONG).show();
                break;
            case R.id.textView_registertip:
                Intent intent1 = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent1);
                break;
        }
    }

    //按下返回键应用回到手机后台运行
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
