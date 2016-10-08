package com.bupt.indooranalysis;

import com.bupt.indoorPosition.model.UserService;
import com.bupt.indoorPosition.uti.Constants;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends BaseAppCompatActivity {
	private EditText loginUserName;
	private EditText loginPassWord;
	private Button btnlogin;
	private TextView textShow;
	private TextView register;
	private LoginReceiver receiver;
	public String errorMessage;
	public String userName;
	public String password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getToolbarTitle().setText("用户登录");
		getSubTitle().setText("注册");
		// 注册广播接受器


		IntentFilter intentFilter = new IntentFilter(
				Constants.ACTIONURL.LOGIN_ACTIVITY_ACTION);
		receiver = new LoginReceiver();
		registerReceiver(receiver, intentFilter);

		// 初始化组件
		loginUserName = (EditText) findViewById(R.id.loginUserName);
		loginPassWord = (EditText) findViewById(R.id.loginPassWord);
		btnlogin = (Button) findViewById(R.id.btnlogin);
		textShow = (TextView) findViewById(R.id.textView7);
		// 删除已有的文字
		loginUserName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				loginUserName.setText("");
			}
		});
		// 删除已有的文字
		loginPassWord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				loginPassWord.setText("");
			}
		});

		btnlogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				login();
			}
		});

		register = (TextView) findViewById(R.id.toolbar_subtitle);
		register.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
			}
		});


	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_login;
	}

	@Override
	protected void onDestroy() {
		if (receiver != null)
			unregisterReceiver(receiver);
		super.onDestroy();
	}

	public void login() {
		userName = loginUserName.getText().toString();
		password = loginPassWord.getText().toString();
		new Thread() {
			@Override
			public void run() {
				// 把网络访问的代码放在这里
				UserService.userLogin(LoginActivity.this, userName, password);
			}
		}.start();

	}

	public class LoginReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent == null)
				return;
			Bundle b = intent.getExtras();
			String message = b.getString("reason");
			Log.d("receive_mess", message);
			if (message.equals("Ok")) {
				Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
				LoginActivity.this.finish();
				// startActivity(new Intent(LoginActivity.this,
				// HomeActivity.class));
			} else {
				textShow.setVisibility(View.GONE);
				textShow.setText("登录失败 ");
				textShow.setVisibility(View.VISIBLE);
			}
		}

	}
}
