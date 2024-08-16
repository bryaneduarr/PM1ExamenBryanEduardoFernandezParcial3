package com.example.bryan_eduardo_fernandez_examen_parcial_3_periodo_2_programacion_movil_1.Date_Class

import android.app.DatePickerDialog
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Class_Date_Picker_Helper(
    private val activity: AppCompatActivity, private val textViewFecha: TextView
) {
    fun datePicker() {
        val c = Calendar.getInstance();

        val year = c.get(Calendar.YEAR);
        val month = c.get(Calendar.MONTH);
        val day = c.get(Calendar.DAY_OF_MONTH);

        var datePickerDialog = DatePickerDialog(
            activity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth);
                }

                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

                val formattedDate = dateFormat.format(selectedDate.time);

                textViewFecha.text = formattedDate;
            }, year, month, day
        );

        datePickerDialog.show();
    }
}