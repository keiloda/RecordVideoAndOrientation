package com.javacodegeeks.androidvideocaptureexample;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 *
 */
public class OrientationView extends TableLayout {

    private Context mContext;
    private String[] mTitles = new String[]{"Yaw", "Pitch", "Roll"};
    private boolean mDegrees = false;

    public OrientationView(Context context) {
        this(context, null);
    }

    public OrientationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        int dip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 1, getResources().getDisplayMetrics());

        for(int i = 0; i < mTitles.length; i++) {

            final TableRow tableRow = new TableRow(context);


            final TextView titleTextView = new TextView(context);
            titleTextView.setWidth(35 * dip);
            titleTextView.setText(mTitles[i]);
            titleTextView.setTextColor(Color.BLACK);


            final TextView dataTextView = new TextView(context);
            titleTextView.setWidth(115 * dip);
            dataTextView.setId(i);

            tableRow.addView(titleTextView);
            tableRow.addView(dataTextView);

            this.addView(tableRow);
        }
    }

    public void setTitle(String title) {
        TextView tableTitle = new TextView(mContext);
        tableTitle.setTextColor(Color.BLACK);
        tableTitle.setText(title);

        TableRow tableRow = new TableRow(mContext);
        tableRow.addView(tableTitle);

        this.addView(tableRow, 0);

    }

    public boolean setOrientation(float[] orientation) {
        boolean success = false;
        if(orientation != null) {

            for(int i = 0; i < 3; i++) {
                TextView dataTV = (TextView) findViewById(i);
                if (mDegrees) {
                    dataTV.setText(String.valueOf((Math.toDegrees(orientation[i]) + 360) % 360));
                }
                else {
                    dataTV.setText(String.valueOf(orientation[i]));
                }
                success = true;
            }

        }
        else {
            success = false;
        }
        return success;
    }

    public void degrees(boolean setDegrees) {
        mDegrees = setDegrees;
    }
}
