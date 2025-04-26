package org.tensorflow.lite.examples.classification.utilspackage;



import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.tensorflow.lite.examples.classification.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    String dateNowString;

    public Utils(String dateNowString) {
        this.dateNowString = dateNowString;
    }

    public Utils(){

    }

    public String getDateNowString() {

        Date currentDate = new Date();

        // Format the date as needed using SimpleDateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateNowString = dateFormat.format(currentDate);

        return dateNowString;
    }

    public String getTimeNowString() {

        Date currentDate = new Date();

        String dateTimeNowString = "";

        // Format the date as needed using SimpleDateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        dateTimeNowString = dateFormat.format(currentDate);

        return dateTimeNowString;
    }

    public void alert_prompt(Context context, String patient_alert_message){


        final Dialog dialog = new Dialog(context);

        dialog.setContentView(R.layout.layout_alert);
        Button btn_close = (Button)  dialog.findViewById(R.id.button_close);
        TextView text_alert = (TextView) dialog.findViewById(R.id.textview_prompt_alert);

        text_alert.setText(patient_alert_message);


        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();

    } //  patient_alert_prompt


    public long getDateTimeFromTodayDifference(Date date_compare){

        Date date_now = new Date();

        long millis_diff = 0;
        millis_diff = date_now.getTime() - date_compare.getTime();

        return millis_diff;
    } // getDateTimeDifference



    public String getMonthFromInt(int month) {
        String[] months = new String[] {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        if (month >= 1 && month <= months.length) {
            return months[month - 1];
        } else {
            return "Invalid month number";
        }
    }

    public void setDateNowString(String dateNowString) {
        this.dateNowString = dateNowString;
    }







    public long convertDateTimeToMillis(String inputDateTime){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date newDate = new Date();

        try{
            Date date = sdf.parse(inputDateTime);
            newDate = date;
        }catch (ParseException e){

        }

        return newDate.getTime();

    }

    public String convertDateTimeString(String date_time_raw){

        String convertedDateTime = "";
        SimpleDateFormat sdf_default = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");

        try{
            Date date_time = new Date();
            date_time = sdf_default.parse(date_time_raw);
            convertedDateTime = sdf.format(date_time);
        }catch(ParseException e){

        }

        return  convertedDateTime;
    }



    public int getYearFromDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dateString);
            return date.getYear() + 1900; // Date.getYear() returns the year since 1900
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Handle parse exception
        }
    }

    public int getDateFromDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dateString);
            return date.getDay();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Handle parse exception
        }
    }

    public int getHourFromTime(String timeString){

        ar_tubo.Utils util_package = new ar_tubo.Utils();
        String hourString = util_package.split_string_delim_get_by_index(timeString,":",0);

        int hour = Integer.parseInt(hourString);

        return hour;

    }

    public int getMinutesFromTime(String timeString){

        ar_tubo.Utils util_package = new ar_tubo.Utils();
        String minString = util_package.split_string_delim_get_by_index(timeString,":",1);

        int min = Integer.parseInt(minString);

        return min;

    }

    public int getMonthFromDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(dateString);
            return date.getMonth() + 1; // Date.getYear() returns the year since 1900
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Handle parse exception
        }
    }


    public String split_string_get_by_index(String input_string, int ndx){

        String[] array_string = input_string.split("\\s+");
        return array_string[ndx];

    } // split_string_get_by_index

    public String split_string_delim_get_by_index(String input_string, String delim, int ndx){

        String[] array_string = input_string.split(delim);
        return array_string[ndx];

    } // split_string_get_by_index


    public String convertTimeToAMPM(String raw_time){

        String ampm_string = "";

        SimpleDateFormat time_ampm_format = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        SimpleDateFormat time_format = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();

        Date convertedDate = new Date();

        try{

            convertedDate = time_format.parse(raw_time);
            ampm_string = time_ampm_format.format(convertedDate);

        }catch(Exception e){

        }

        return ampm_string;
    } // convertTimeToAMPM

    public String convertDateWithFullMonth(String rawDate){
        String convertedDateString = "";

        SimpleDateFormat date_format = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat date_raw_format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();

        Date newFormattedDate = new Date();

        try{

            newFormattedDate = date_raw_format.parse(rawDate);
            convertedDateString = date_format.format(newFormattedDate);

        }catch (Exception e){

        }

        return convertedDateString;
    }

    public String getDateNowFormatted(String format_string){

        Date currentDate = new Date();

        // Format the date as needed using SimpleDateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateNowString = dateFormat.format(currentDate);


        String convertedDateString = "";

        SimpleDateFormat date_format = new SimpleDateFormat(""+format_string+"", Locale.getDefault());
        SimpleDateFormat date_raw_format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();

        Date newFormattedDate = new Date();

        try{

            newFormattedDate = date_raw_format.parse(dateNowString);
            convertedDateString = date_format.format(newFormattedDate);

        }catch (Exception e){

        }

        return convertedDateString;


    } // getDateNowFormatted

}
