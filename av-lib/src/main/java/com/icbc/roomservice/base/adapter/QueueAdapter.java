package com.icbc.roomservice.base.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zego.queue.QueueInfo;
import com.icbc.roomservice.base.R;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private List<QueueInfo> mQueueInfoList;

    private OnQueueClickListener mOnQueueClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_queue_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        QueueInfo queueInfo = mQueueInfoList.get(i);
        viewHolder.queueTv.setText(queueInfo.getQueueName());
        viewHolder.seatCountTv.setText(viewHolder.seatCountTv.getContext().getResources().getString(R.string.current_seat_count,
                queueInfo.getStaffCount() + ""));
        viewHolder.itemView.setTag(queueInfo);

    }

    @Override
    public int getItemCount() {
        return mQueueInfoList != null ? mQueueInfoList.size() : 0;
    }

    /**
     * 设置 队列信息列表
     *
     * @param queueInfoList 队列信息列表
     */
    public void setQueueInfoList(List<QueueInfo> queueInfoList) {
        mQueueInfoList = queueInfoList;
        notifyDataSetChanged();
    }

    /**
     * 设置 点击队列回调
     *
     * @param onQueueClickListener 点击队列回调
     */
    public void setOnQueueClickListener(OnQueueClickListener onQueueClickListener) {
        this.mOnQueueClickListener = onQueueClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // 业务名字 textView
        private TextView queueTv;
        // 坐席数 textView
        private TextView seatCountTv;

        ViewHolder(View itemView) {
            super(itemView);
            queueTv = itemView.findViewById(R.id.queue_tv);
            seatCountTv = itemView.findViewById(R.id.seat_count_tv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnQueueClickListener != null) {
                mOnQueueClickListener.onQueueClick((QueueInfo) v.getTag());
            }
        }
    }

    public interface OnQueueClickListener {
        void onQueueClick(QueueInfo queueInfo);
    }
}
