package widget.demo.xiaosu.dashboarddemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import xiaosu.widget.dashboard.DashboardView;

public class MainActivity extends AppCompatActivity implements DashboardView.OnValueChangedListener {

    private TextView mTextValue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextValue = (TextView) findViewById(R.id.text_value);
        DashboardView dashboard = (DashboardView) findViewById(R.id.rulerView);
        dashboard.setOnValueChangedListener(this);
    }

    @Override
    public void valueChange(float value, DashboardView dashboard) {
        mTextValue.setText(String.valueOf(value));
    }
}
