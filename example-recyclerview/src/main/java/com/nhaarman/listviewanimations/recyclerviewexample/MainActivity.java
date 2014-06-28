package com.nhaarman.listviewanimations.recyclerviewexample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.simple.AlphaAnimationRecyclerViewAdapter;
import com.nhaarman.listviewanimations.appearance.AnimationRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private final List<Integer> mIntegerList = new ArrayList<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerview);
        final MyAdapter adapter = new MyAdapter();

        AnimationRecyclerViewAdapter<MyViewHolder> animAdapter = new AlphaAnimationRecyclerViewAdapter<>(adapter);
        animAdapter.setRecyclerView(recyclerView);

        recyclerView.setAdapter(animAdapter);
        final LinearLayoutManager layout = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layout);

        for (int i = 0; i < 100; i++) {
            mIntegerList.add(i);
        }

    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {

        CardView mCardView;
        TextView tv;

        MyViewHolder(final CardView view) {
            super(view);
            tv = (TextView) view.findViewById(R.id.view_cardrow_tv);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
            System.out.println("Create: " + i);
            CardView cardView = (CardView) LayoutInflater.from(MainActivity.this).inflate(R.layout.view_cardrow, viewGroup, false);
            return new MyViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder myViewHolder, final int i) {
            System.out.println("Bind: " + i);
            myViewHolder.tv.setText("This is row number: " + i);
        }

        @Override
        public int getItemCount() {
            return mIntegerList.size();
        }
    }
}
