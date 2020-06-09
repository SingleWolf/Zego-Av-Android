package com.icbc.roomservice.base.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Spinner;

import com.icbc.roomservice.base.R;

/**
 * 排队方式选择dialog
 */
public class QueueFormChooseDialog extends Dialog implements View.OnClickListener {

    private final static String TAG = QueueFormChooseDialog.class.getSimpleName();

    private Spinner mSpinnerVipPriority;

    private OnQueueFormChooseListener mOnQueueFormChooseListener;

    public QueueFormChooseDialog(Context context) {
        super(context, R.style.CommonDialog);
        initView(context);
    }

    public void setOnQueueFormChooseListener(OnQueueFormChooseListener onQueueFormChooseListener) {
        this.mOnQueueFormChooseListener = onQueueFormChooseListener;
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_queue_form_choose, null);
        setContentView(view);

        // 设置可以取消
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        // 设置Dialog高度位置
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        // 设置没有边框
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);

        mSpinnerVipPriority = (Spinner) findViewById(R.id.spinner_vip_priority);

        findViewById(R.id.tv_queue_form_common).setOnClickListener(this);
        findViewById(R.id.tv_queue_form_vip).setOnClickListener(this);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
            if(view.getId()==R.id.tv_queue_form_common){
                if (mOnQueueFormChooseListener != null) {
                    mOnQueueFormChooseListener.onQueueFormChoose(0);
                }
                reset();
                dismiss();
                return;
            }
            if(view.getId()==R.id.tv_queue_form_vip){
                if (mOnQueueFormChooseListener != null) {
                    mOnQueueFormChooseListener.onQueueFormChoose(Integer.parseInt((String) mSpinnerVipPriority.getSelectedItem()));
                }
                reset();
                dismiss();
                return;
            }
            if(view.getId()==R.id.tv_cancel){
                dismiss();
                return;
            }
    }

    private void reset() {
        mSpinnerVipPriority.setSelection(0);
    }

    public interface OnQueueFormChooseListener {
        /**
         * 排队方式选择回调方法
         *
         * @param vipPriority Vip 优先级
         */
        void onQueueFormChoose(int vipPriority);
    }
}
