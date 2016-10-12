package com.bupt.indooranalysis;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bupt.indoorPosition.bean.Inspector;
import com.bupt.indoorPosition.model.UserService;

public class UserCenterActivity extends BaseAppCompatActivity {


    private TextView name;
    private TextView company;
    private TextView province;
    private TextView city;
    private Button logout;
    private Inspector inspector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbarTitle().setText("用户中心");
        getSubTitle().setText("");


        name = (TextView) findViewById(R.id.Name);
        company = (TextView) findViewById(R.id.company);
        province = (TextView) findViewById(R.id.province);
        city = (TextView) findViewById(R.id.city);
        logout = (Button) findViewById(R.id.logout);

        inspector = UserService.selectAllInspector(UserCenterActivity.this);

        if (inspector != null) {
//            name.setText(inspector.getUsername());
//            company.setText(inspector.getCompanyName());
//            province.setText(inspector.getProvince());
//            city.setText(inspector.getCity());

            name.setText("罗明");
            company.setText("中国联通");
            province.setText("北京");
            city.setText("北京市");
        }

        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                new Thread() {
                    @Override
                    public void run() {
                        UserService.userLogout(UserCenterActivity.this);
                        Intent intent = new Intent();
                        intent.putExtra("TYPE","LOGOUT");
                        UserCenterActivity.this.setResult(100,intent);
                        UserCenterActivity.this.finish();
                    }

                }.start();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_center;
    }
}
