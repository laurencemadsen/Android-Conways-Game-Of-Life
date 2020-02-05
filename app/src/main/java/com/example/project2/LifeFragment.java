package com.example.project2;


import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.util.Log;

import java.io.*;
import java.io.File;
import java.io.FileOutputStream;


public class LifeFragment extends Fragment {


    private static final int ROWS = 20;
    private static final int COLS = 20;
    private static int DELAY = 2000;
    private static int DELAY_2 = 1000; //user can switch to this one
    private boolean colour_swapped = true;
    private boolean delay_swapped = true;
    private boolean start_clicked = true;

    private boolean[][] mCells = new boolean[20][20];
    //private int []mGrid = new int[400];
    private RecyclerView mRecycler;
    private RecyclerView.Adapter<CellHolder> mAdapter = new CellAdapter();

    public static @ColorInt int DEFAULT_CELL_BACKGROUND = Color.BLACK;
    public static @ColorInt int DEFAULT_CELL_ALIVE = Color.RED;
    private @ColorInt int mBackgroundColor;
    private @ColorInt int mAliveColor;
    //final ColorPicker cp = new ColorPicker(MainActivity.this);

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_life, container, false);
        mRecycler = (RecyclerView) v.findViewById(R.id.recycler_life);
        mRecycler.setLayoutManager(new GridLayoutManager(getActivity(), COLS));
        mRecycler.setAdapter(mAdapter);

        // just recreate activity when want to play again
        Button resetButton = (Button) v.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().recreate();
            }
        });

        Button startButton = (Button) v.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Handler handler = new Handler();
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        updateSimulation();
                        handler.postDelayed(this, DELAY_2);
                    }
                };
                if(start_clicked){
                    start_clicked = false;
                    handler.post(task);
                }
                else{
                    start_clicked = true;
                    handler.removeMessages(0);
                }
            }
        });

        Button colourButton = (Button) v.findViewById(R.id.colour_button);
        colourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (colour_swapped) {
                    DEFAULT_CELL_ALIVE = Color.BLUE;
                    DEFAULT_CELL_BACKGROUND = Color.GRAY;
                    mAdapter.notifyDataSetChanged();
                    colour_swapped = false;
                }
                else{
                    DEFAULT_CELL_ALIVE = Color.RED;
                    DEFAULT_CELL_BACKGROUND = Color.BLACK;
                    mAdapter.notifyDataSetChanged();
                    colour_swapped = true;
                }
            }
        });

        Button delayButton = (Button) v.findViewById(R.id.delay_button);
        delayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (delay_swapped) {
                    DELAY_2 = 3000;
                    mAdapter.notifyDataSetChanged();
                    delay_swapped = false;
                }
                else{
                    DELAY_2 = 2000;
                    mAdapter.notifyDataSetChanged();
                    delay_swapped = true;
                }
            }
        });

        Button cloneButton = (Button) v.findViewById(R.id.clone_button);
        cloneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("CELLS_EXTRA",mCells);
                startActivity(intent);
            }
        });



        return v;
    }

    private class CellHolder extends RecyclerView.ViewHolder {
        private Button mButton;
        private int mPosition;
        private int mRow;
        private int mCol;

        public CellHolder(LayoutInflater inflater, ViewGroup container) {
            super(inflater.inflate(R.layout.cell_square, container, false));

            mButton = (Button)itemView.findViewById(R.id.cell_button);
            // player makes a move when they click
            mButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               if(mCells[mRow][mCol]){
                                                   mCells[mRow][mCol] = false;
                                                   Log.i("App ","entered if statement.");
                                                   mAdapter.notifyItemChanged(mPosition); // reload ViewHolder
                                               }
                                               else{
                                                   mCells[mRow][mCol] = true;
                                                   mAdapter.notifyItemChanged(mPosition); // reload ViewHolder
                                               }
                                           }
        });}
        public void bindPosition(int p) {
            mPosition = p;
            mRow = mPosition / COLS;
            mCol = mPosition % COLS;
        }
    }

    private class CellAdapter extends RecyclerView.Adapter<CellHolder> {
        private int mRow;
        private int mCol;

        @Override
        public CellHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new CellHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(CellHolder holder, int position) {
            // tell holder which place on grid it is representing
            holder.bindPosition(position);
            mRow = position / COLS;
            mCol = position % COLS;
            // actually change image displayed
            if (mCells[mRow][mCol]) {
                ObjectAnimator anim = ObjectAnimator.ofInt(holder.mButton,"backgroundColor",DEFAULT_CELL_ALIVE,mBackgroundColor,DEFAULT_CELL_ALIVE);
                anim.setDuration(DELAY);
                anim.setEvaluator(new ArgbEvaluator());
                anim.setRepeatCount(Animation.INFINITE);
                anim.start();
                //holder.mButton.setBackgroundColor(DEFAULT_CELL_ALIVE);
                Log.i("App ","entered true if statement.");
            }
            else {
                holder.mButton.setBackgroundColor(DEFAULT_CELL_BACKGROUND);
                Log.i("App ","entered false if statement.");
            }
        }
        @Override
        public int getItemCount() {
            return ROWS*COLS;
        }
    }

    public void updateSimulation() {
        int neighbors[][] =  new int[ROWS][COLS];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int previousRow = ((row - 1) >= 0) ? row - 1 : ROWS - 1;
                int nextRow = ((row + 1) < ROWS) ? row + 1 : 0;
                int previousCol = ((col - 1) >= 0) ? col - 1 : COLS - 1;
                int nextCol = ((col + 1) < COLS) ? col + 1 : 0;
                //top left
                neighbors[row][col] += mCells[previousRow][previousCol] ? 1 : 0;
                //top
                neighbors[row][col] += mCells[previousRow][col] ? 1 : 0;
                //top right
                neighbors[row][col] += mCells[previousRow][nextCol] ? 1 : 0;
                //bottom right
                neighbors[row][col] += mCells[nextRow][nextCol] ? 1 : 0;
                //right
                neighbors[row][col] += mCells[row][nextCol] ? 1 : 0;
                //bottom left
                neighbors[row][col] += mCells[nextRow][previousCol] ? 1 : 0;
                //right
                neighbors[row][col] += mCells[nextRow][col] ? 1 : 0;
                //left
                neighbors[row][col] += mCells[row][previousCol] ? 1 : 0;

            }
        }

        for (int i = 0; i < ROWS; i++){
            for (int j = 0; j < COLS; j++){
                // If the cell has 4 or more living neighbors, it dies
                // by overcrowding.
                if (neighbors[i][j] >= 4){
                    mCells[i][j] = false;
                }

                // A cell dies by exposure if it has 0 or 1 living neighbors.
                if (neighbors[i][j] < 2){
                    mCells[i][j] = false;
                }

                // A cell is born if it has 3 living neighbors.
                if (neighbors[i][j] == 3){
                    mCells[i][j] = true;
                }
            }
        }

        mAdapter.notifyDataSetChanged();
    }

}
