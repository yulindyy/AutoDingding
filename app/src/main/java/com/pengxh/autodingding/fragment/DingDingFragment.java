package com.pengxh.autodingding.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.CountDownTimer;

import com.blankj.utilcode.constant.MemoryConstants;
import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.pengxh.androidx.lite.base.AndroidxBaseFragment;
import com.pengxh.androidx.lite.utils.ColorUtil;
import com.pengxh.androidx.lite.utils.TimeOrDateUtil;
import com.pengxh.androidx.lite.widget.EasyToast;
import com.pengxh.autodingding.bean.MailInfo;
import com.pengxh.autodingding.databinding.FragmentDingdingBinding;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.DingDingUtil;
import com.pengxh.autodingding.utils.MailInfoUtil;
import com.pengxh.autodingding.utils.MailSender;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class DingDingFragment extends AndroidxBaseFragment<FragmentDingdingBinding> {

    private static final String TAG = "AutoDingDingFragment";
    private CountDownTimer amCountDownTimer, pmCountDownTimer;


    @Override
    protected void setupTopBarLayout() {

    }

    @Override
    protected void initData() {
        viewBinding.overTime.setText("使用有效期至:"+Constant.overTime);
    }

    @Override
    protected void initEvent() {
        viewBinding.startLayoutView.setOnClickListener(v -> {
            //设置上班时间
            new TimePickerDialog.Builder().setThemeColor(ColorUtil.randomColor())
                    .setWheelItemTextSize(15)
                    .setCyclic(false)
                    .setMinMillseconds(System.currentTimeMillis())
                    .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
                    .setType(Type.ALL)
                    .setCallBack((timePickerView, millSeconds) -> {
                        viewBinding.amTime.setText(TimeOrDateUtil.timestampToDate(millSeconds));
                        //计算时间差
                        onDuty(millSeconds);
                    }).build().show(getChildFragmentManager(), "year_month_day_hour_minute");
        });
        viewBinding.endLayoutView.setOnClickListener(v -> {
            //设置下班时间
            new TimePickerDialog.Builder().setThemeColor(ColorUtil.randomColor())
                    .setWheelItemTextSize(15)
                    .setCyclic(false)
                    .setMinMillseconds(System.currentTimeMillis())
                    .setMaxMillseconds(System.currentTimeMillis() + Constant.ONE_WEEK)
                    .setType(Type.ALL)
                    .setCallBack((timePickerView, millSeconds) -> {
                        viewBinding.pmTime.setText(TimeOrDateUtil.timestampToDate(millSeconds));
                        //计算时间差
                        offDuty(millSeconds);
                    }).build().show(getChildFragmentManager(), "year_month_day_hour_minute");
        });
        viewBinding.endAmDuty.setOnClickListener(v -> {
            if (amCountDownTimer != null) {
                amCountDownTimer.cancel();
                viewBinding.startTimeView.setText("--");
            }
        });
        viewBinding.endPmDuty.setOnClickListener(v -> {
            if (pmCountDownTimer != null) {
                pmCountDownTimer.cancel();
                viewBinding.endTimeView.setText("--");
            }
        });
    }

    private void onDuty(long millSeconds) {
        long deltaTime = deltaTime(millSeconds / 1000);
        if (deltaTime == 0) {
            return;
        }
        //显示倒计时
        String text = viewBinding.startTimeView.getText().toString();

        if (text.equals("--")) {
            amCountDownTimer = new CountDownTimer(deltaTime * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    viewBinding.startTimeView.setText(String.valueOf((int) (l / 1000)));
                }

                @Override
                public void onFinish() {
                    viewBinding.startTimeView.setText("--");
                    DingDingUtil.openDingDing(Constant.DINGDING);

                    nextGo();
                }
            };
            amCountDownTimer.start();
        } else {
            EasyToast.show(requireContext(), "已有任务在进行中");
        }
    }
    private void nextGo(){
        new CountDownTimer(10 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                long nextTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000 - 10000);
                viewBinding.amTime.setText(TimeOrDateUtil.timestampToDate(nextTime));
                //计算时间差
                onDuty(nextTime);
            }
        }.start();

    }

    private void offDuty(long millSeconds) {
        long deltaTime = deltaTime(millSeconds / 1000);
        if (deltaTime == 0) {
            return;
        }
        //显示倒计时
        String text = viewBinding.endTimeView.getText().toString();
        if (text.equals("--")) {
            pmCountDownTimer = new CountDownTimer(deltaTime * 1000, 1000) {
                @Override
                public void onTick(long l) {
                    viewBinding.endTimeView.setText(String.valueOf((int) (l / 1000)));
                }

                @Override
                public void onFinish() {
                    viewBinding.endTimeView.setText("--");
                    DingDingUtil.openDingDing(Constant.DINGDING);
                }
            };
            pmCountDownTimer.start();
        } else {
            EasyToast.show(requireContext(), "已有任务在进行中");
        }
    }

    /**
     * 计算时间差
     *
     * @param fixedTime 结束时间
     */
    private long deltaTime(long fixedTime) {
        long currentTime = (System.currentTimeMillis() / 1000);
        if (fixedTime > currentTime) {
            return (fixedTime - currentTime);
        } else {
            EasyToast.show(requireContext(), "时间设置异常");
        }
        return 0L;
    }
}