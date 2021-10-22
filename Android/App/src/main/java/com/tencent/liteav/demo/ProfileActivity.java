package com.tencent.liteav.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.tencent.liteav.basic.AvatarConstant;
import com.tencent.liteav.basic.ImageLoader;
import com.tencent.liteav.basic.UserModelManager;
import com.tencent.liteav.demo.login.HttpLogicRequest;
import com.tencent.liteav.demo.login.ModifyUserAvatarDialog;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private ImageView mImageAvatar;
    private EditText  mEditUserName;
    private Button    mButtonRegister;
    private TextView  mTvInputTips;
    private String    mAvatarUrl;

    //自定义随机登录名
    private static final int CUSTOM_NAME_ARRAY[] = {
            R.string.app_custom_name_1,
            R.string.app_custom_name_2,
            R.string.app_custom_name_3,
            R.string.app_custom_name_4,
            R.string.app_custom_name_5,
            R.string.app_custom_name_6,
            R.string.app_custom_name_7,
            R.string.app_custom_name_8,
            R.string.app_custom_name_9,
            R.string.app_custom_name_10,
            R.string.app_custom_name_11,
            R.string.app_custom_name_12,
    };

    private void startMainActivity() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initStatusBar();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        mImageAvatar = (ImageView) findViewById(R.id.iv_user_avatar);
        mEditUserName = (EditText) findViewById(R.id.et_user_name);
        mButtonRegister = (Button) findViewById(R.id.tv_register);
        mTvInputTips = (TextView) findViewById(R.id.tv_tips_user_name);
        String[] avatarArr = AvatarConstant.USER_AVATAR_ARRAY;
        int index = new Random().nextInt(avatarArr.length);
        mAvatarUrl = avatarArr[index];
        ImageLoader.loadImage(this, mImageAvatar, mAvatarUrl, R.drawable.ic_head);

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProfile();
            }
        });
        int customNameIndex = new Random().nextInt(CUSTOM_NAME_ARRAY.length);
        mEditUserName.setText(getString(CUSTOM_NAME_ARRAY[customNameIndex]));
        String text = mEditUserName.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            mEditUserName.setSelection(text.length());
        }
        mEditUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                mButtonRegister.setEnabled(text.length() != 0);
                String editable = mEditUserName.getText().toString();
                //匹配字母,数字,中文,下划线,以及限制输入长度为2-20.
                Pattern p = Pattern.compile("^[a-z0-9A-Z\\u4e00-\\u9fa5\\_]{2,20}$");
                Matcher m = p.matcher(editable);
                if (!m.matches()) {
                    mTvInputTips.setTextColor(getResources().getColor(R.color.app_color_input_no_match));
                } else {
                    mTvInputTips.setTextColor(getResources().getColor(R.color.text_color_hint));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        findViewById(R.id.iv_user_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModifyUserAvatarDialog dialog = new ModifyUserAvatarDialog(ProfileActivity.this, new ModifyUserAvatarDialog.ModifySuccessListener() {
                    @Override
                    public void onSuccess() {
                        String userAvatar = UserModelManager.getInstance().getUserModel().userAvatar;
                        ImageLoader.loadImage(getApplicationContext(), mImageAvatar, userAvatar, R.drawable.ic_head);
                    }
                });
                dialog.show();
            }
        });
    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void setProfile() {
        String userName = mEditUserName.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            ToastUtils.showLong(getString(R.string.app_toast_set_username));
            return;
        }
        String reg = "^[a-z0-9A-Z\\u4e00-\\u9fa5\\_]{2,20}$";
        if (!userName.matches(reg)) {
            mTvInputTips.setTextColor(getResources().getColor(R.color.app_color_input_no_match));
            return;
        }
        mTvInputTips.setTextColor(getResources().getColor(R.color.text_color_hint));
        HttpLogicRequest.getInstance().userUpdate(userName, mAvatarUrl, new HttpLogicRequest.ActionCallback() {
            @Override
            public void onSuccess() {
                ToastUtils.showLong(getString(R.string.app_toast_register_success_and_logging_in));
                startMainActivity();
                finish();
            }

            @Override
            public void onFailed(int code, String msg) {
                ToastUtils.showLong(getString(R.string.app_toast_failed_to_set_username, msg));
            }
        });
    }
}
