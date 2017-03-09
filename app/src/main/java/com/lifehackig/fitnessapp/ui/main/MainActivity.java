package com.lifehackig.fitnessapp.ui.main;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.lifehackig.fitnessapp.R;
import com.lifehackig.fitnessapp.ui.base.BaseActivity;
import com.lifehackig.fitnessapp.ui.day.DayActivity;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements MainContract.MvpView, View.OnClickListener{
    @Bind(R.id.date) TextView mDateTextView;
    @Bind(R.id.calories) TextView mCalories;
    @Bind(R.id.seeDetailsButton) Button mSeeDetailsButton;

    private MainPresenter mPresenter;
    private CaldroidFragment mCaldroidFragment;

    private Date mDate;
    private DateFormat mDateFormatter;
    private DateFormat mRefIdFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPresenter = new MainPresenter(this);
        mPresenter.getUser();

        mDate = new Date();
        mDateFormatter = new SimpleDateFormat("MM/dd/yyyy");
        mRefIdFormatter = new SimpleDateFormat("MMddyyyy");

        setDateTextView(mDate);
        getCalories(mDate);

        initCaldroidFragment(savedInstanceState);
        setBottomNavChecked(0);

        mSeeDetailsButton.setOnClickListener(this);
    }

    private void setDateTextView(Date date) {
        String formattedDate = mDateFormatter.format(date);
        mDateTextView.setText(formattedDate);
    }

    private void getCalories(Date date) {
        String dateRefId = mRefIdFormatter.format(date);
        mPresenter.getCalories(dateRefId);
    }

    private void initCaldroidFragment(Bundle savedInstanceState) {
        mCaldroidFragment = new CaldroidFragment();
        // If Activity is created after rotation
        if (savedInstanceState != null) {
            mCaldroidFragment.restoreStatesFromKey(savedInstanceState,
                    "CALDROID_SAVED_STATE");
        }
        // If activity is created from fresh
        else {
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
            mCaldroidFragment.setArguments(args);
        }

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, mCaldroidFragment);
        t.commit();

        setCaldroidListener();
    }

    private void setCaldroidListener() {
        mCaldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                mDate = date;

                mCaldroidFragment.clearSelectedDates();
                mCaldroidFragment.setSelectedDates(date, date);
                mCaldroidFragment.refreshView();

                setDateTextView(date);
                getCalories(date);
            }
            @Override
            public void onCaldroidViewCreated() {
                if (mCaldroidFragment.getLeftArrowButton() != null) {
                    mPresenter.getExercisedDays();
                }
            }
        });
    }

    @Override
    public void setCalendarBackgroundColors(DataSnapshot dataSnapshot) {
        ColorDrawable yellow = new ColorDrawable(getResources().getColor(R.color.colorAccent));
        for (DataSnapshot daySnapshot : dataSnapshot.getChildren()) {
            String stringDate = daySnapshot.child("date").getValue().toString();
            try {
                Date date = mRefIdFormatter.parse(stringDate);
                mCaldroidFragment.setBackgroundDrawableForDate(yellow, date);
                mCaldroidFragment.refreshView();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setCaloriesTextView(String totalCalories) {
        mCalories.setText(totalCalories);
    }

    @Override
    public void onClick(View v) {
        if (v == mSeeDetailsButton) {
            Intent intent = new Intent(MainActivity.this, DayActivity.class);
            DateFormat yearFormat = new SimpleDateFormat("yyyy");
            DateFormat monthFormat = new SimpleDateFormat("MM");
            DateFormat dayFormat = new SimpleDateFormat("dd");

            intent.putExtra("year", yearFormat.format(mDate));
            intent.putExtra("month", monthFormat.format(mDate));
            intent.putExtra("day", dayFormat.format(mDate));
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCaldroidFragment != null) {
            mCaldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detach();
        }
    }
}