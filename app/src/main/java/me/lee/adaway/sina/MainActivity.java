package me.lee.adaway.sina;

import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseAppCompatActivity implements CompoundButton.OnCheckedChangeListener {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyAuth();
        setContentView(R.layout.activity_main);
        View view = this.getWindow().getDecorView();
        List<View> allView = Childs(view ,false);
        for(View viewTemp : allView){
            if(viewTemp instanceof Switch){
                ((Switch) viewTemp).setOnCheckedChangeListener(this);
                ((Switch) viewTemp).setChecked(getBoolean(String.valueOf(viewTemp.getId())));
            }
        }
        new Thread(new Runnable(){
            @Override
            public void run() {
                HookPackage.initSurportVersion();
            }
        }).start();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        putBoolean(String.valueOf(compoundButton.getId()), b);
        switch (compoundButton.getId()){
            case R.id.hide_icon:
                changeIconStatus(!b);
                break;
            case R.id.active_module:
                break;
            default:
                if(b){
                    Toast.makeText(this, "功能暂未实现!", Toast.LENGTH_SHORT).show();
                    compoundButton.setChecked(false);
                    putBoolean(String.valueOf(compoundButton.getId()), false);
                }
                break;
        }
    }

    private void changeIconStatus(boolean isShow) {
        final ComponentName aliasName = new ComponentName(this, getClass().getCanonicalName() + "Alias");
        final PackageManager packageManager = getPackageManager();
        int status = isShow ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        if (packageManager.getComponentEnabledSetting(aliasName) != status) {
            packageManager.setComponentEnabledSetting(aliasName, status, PackageManager.DONT_KILL_APP);
        }
    }

    private void applyAuth(){
        if(Build.VERSION.SDK_INT >= 23){
            int REQUEST_CODE_CONTANCT = 101;
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            for(String str:permissions){
                if(MainActivity.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED){
                    MainActivity.this.requestPermissions(permissions,REQUEST_CODE_CONTANCT);
                    return;
                }
            }
        }
    }

    /** 获取当前View的所有子view */
    public static List<View> Childs(View view, boolean ContainsThis) {
        List<View> viewList = new ArrayList<View>();
        if (!viewList.contains(view)) viewList.add(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (!viewList.contains(child)) viewList.add(child);
                // 添加child的子节点
                List<View> subList = Childs(child, true);
                for (View v : subList) {
                    if (!viewList.contains(v)) viewList.addAll(subList);
                }
            }
        }
        if (!ContainsThis) viewList.remove(view);
        return viewList;
    }
}
