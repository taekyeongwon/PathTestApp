package co.kr.emgram.mobilpackfieldtest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.savvi.rangedatepicker.CalendarCellDecorator;
import com.savvi.rangedatepicker.CalendarCellView;
import com.savvi.rangedatepicker.CalendarPickerView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomDialog extends AlertDialog {
    public CustomDialog(Context context) {
        super(context);
    }

    public void show() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.alert_calendar, null);
        Builder builder = new Builder(getContext(), R.style.CustomDialogTheme);
        builder.setView(view);
        builder.setCancelable(false);
        final CalendarPickerView calendar = view.findViewById(R.id.calendar_pv);
        final AlertDialog dialog = builder.create();
        Button button = view.findViewById(R.id.ok_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                List<Date> dates = calendar.getSelectedDates();
                for (int i = 0; i < dates.size(); i++) {
                    Log.d("Calendar", dates.get(i) + "");
                }
            }
        });

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.YEAR, 1);

        final Calendar day = Calendar.getInstance();
        final Calendar day2 = Calendar.getInstance();
        day2.add(Calendar.DAY_OF_MONTH, 3);

        calendar.init(cal.getTime(), cal2.getTime(), new SimpleDateFormat("YYYY MMMM", Locale.getDefault()))
                .inMode(CalendarPickerView.SelectionMode.RANGE);

        calendar.scrollToDate(day.getTime());
        CalendarCellDecorator deco = new CalendarCellDecorator() {
            @Override
            public void decorate(CalendarCellView cellView, Date date) {
                if (day.getTime().getTime() <= date.getTime() && date.getTime() <= day2.getTime().getTime()) {
                    cellView.setBackground(getContext().getResources().getDrawable(R.drawable.color_selector));
                }
            }
        };

        List<CalendarCellDecorator> decoList = Arrays.asList(deco);
        calendar.setDecorators(decoList);

        dialog.show();
    }
}
