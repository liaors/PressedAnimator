package com.rs.pressanimator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rs.pressanim.PressAnimator;

import java.util.ArrayList;
import java.util.List;

public class PressDemoActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressdemo);
        initView();
        initData();
    }

    private void initView() {
        recyclerView = findViewById(R.id.recycler);
    }

    private void initData() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setData(getRecyclerData());
    }

    public List<Integer> getRecyclerData() {
        List<Integer> dataList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            if (i % 3 == 0) {
                dataList.add(R.drawable.aaa);
            } else if (i % 3 == 1) {
                dataList.add(R.drawable.bbb);
            } else {
                dataList.add(R.drawable.ccc);
            }
        }
        return dataList;
    }

    public void onClick(View view) {
        finish();
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.DelayPressViewHolder> {
        private List<Integer> data;

        public void setData(List<Integer> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerAdapter.DelayPressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delaypress, parent, false);
            return new RecyclerAdapter.DelayPressViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerAdapter.DelayPressViewHolder holder, int position) {
            holder.img.setImageDrawable(holder.img.getContext().getDrawable(data.get(position)));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private class DelayPressViewHolder extends RecyclerView.ViewHolder {
            public ImageView img;
            public TextView titleTv,bottomTv;

            public DelayPressViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTv = itemView.findViewById(R.id.title_tv);
                bottomTv = itemView.findViewById(R.id.bottom_tv);
                img = itemView.findViewById(R.id.img);
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);

              // 如果onTouchListener业务层有使用，则使用这种方式
//                PressAnimator pressAnimator = new PressAnimator.Builder().build()
//                        .addTargetAnimatorView(img)
//                        .addTargetAnimatorView(titleTv)
//                        .addTargetAnimatorView(bottomTv)
//                        .init();
//                img.setOnTouchListener((v, event) -> {
//                    pressAnimator.getOnTouchListener().onTouch(v,event);
//                    return false;
//                });

                new PressAnimator.Builder().build()
                        .addTargetAnimatorView(img,true)
                        .addTargetAnimatorView(titleTv)
                        .addTargetAnimatorView(bottomTv)
                        .setOnTouchListener(img)
                        .init();
            }
        }
    }
}
