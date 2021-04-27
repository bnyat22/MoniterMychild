package com.example.localiser.domains;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.TimeZone;

public class MyEditTextHourPicker  implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {
    EditText _editText;
    private int hour;
    private int minutes;
    private Context _context;

    public MyEditTextHourPicker(Context context, int editTextViewID)
    {
        Activity act = (Activity)context;
        this._editText = act.findViewById(editTextViewID);
        this._editText.setOnClickListener(this);
        this._context = context;
    }


    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance();
      TimePickerDialog  mTimePicker = new TimePickerDialog(_context,this,hour,minutes,true);
        mTimePicker.show();

    }

    // updates the date in the birth date EditText
    private void updateDisplay() {

        _editText.setText(new StringBuilder()
                // Month is 0 based so add 1
                .append(hour).append(":").append(minutes));
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
       hour = hourOfDay;
       minutes = minute;
       updateDisplay();
    }
}