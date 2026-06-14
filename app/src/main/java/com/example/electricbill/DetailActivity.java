package com.example.electricbill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private TextView tvDetailMonth, tvDetailUnits, tvDetailRebate,
            tvDetailTotal, tvDetailFinal;
    private Spinner spinnerMonth;
    private EditText etUnitsEdit;
    private SeekBar seekBarRebateEdit;
    private TextView tvRebateEditValue;
    private Button btnUpdate, btnDelete;

    private DatabaseHelper dbHelper;
    private int recordId;
    private BillRecord currentRecord;

    private final String[] MONTHS = {
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill Detail");
        }

        dbHelper = new DatabaseHelper(this);
        recordId = getIntent().getIntExtra("RECORD_ID", -1);

        // Bind views
        tvDetailMonth    = findViewById(R.id.tvDetailMonth);
        tvDetailUnits    = findViewById(R.id.tvDetailUnits);
        tvDetailRebate   = findViewById(R.id.tvDetailRebate);
        tvDetailTotal    = findViewById(R.id.tvDetailTotal);
        tvDetailFinal    = findViewById(R.id.tvDetailFinal);
        spinnerMonth     = findViewById(R.id.spinnerMonthEdit);
        etUnitsEdit      = findViewById(R.id.etUnitsEdit);
        seekBarRebateEdit= findViewById(R.id.seekBarRebateEdit);
        tvRebateEditValue= findViewById(R.id.tvRebateEditValue);
        btnUpdate        = findViewById(R.id.btnUpdate);
        btnDelete        = findViewById(R.id.btnDelete);

        // Spinner
        // WITH THIS:
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, MONTHS);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        seekBarRebateEdit.setMax(5);
        seekBarRebateEdit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                tvRebateEditValue.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        loadRecord();

        btnUpdate.setOnClickListener(v -> updateRecord());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadRecord() {
        if (recordId == -1) { finish(); return; }
        currentRecord = dbHelper.getBillById(recordId);
        if (currentRecord == null) { finish(); return; }

        // Show detail
        tvDetailMonth.setText("Month: " + currentRecord.getMonth());
        tvDetailUnits.setText(String.format(Locale.getDefault(),
                "Units: %.0f kWh", currentRecord.getUnits()));
        tvDetailRebate.setText(String.format(Locale.getDefault(),
                "Rebate: %.0f%%", currentRecord.getRebatePercent()));
        tvDetailTotal.setText(String.format(Locale.getDefault(),
                "Total Charges: RM %.2f", currentRecord.getTotalCharges()));
        tvDetailFinal.setText(String.format(Locale.getDefault(),
                "Final Cost: RM %.2f", currentRecord.getFinalCost()));

        // Pre-fill edit fields
        for (int i = 0; i < MONTHS.length; i++) {
            if (MONTHS[i].equals(currentRecord.getMonth())) {
                spinnerMonth.setSelection(i);
                break;
            }
        }
        etUnitsEdit.setText(String.format(Locale.getDefault(),
                "%.0f", currentRecord.getUnits()));
        int rebate = (int) currentRecord.getRebatePercent();
        seekBarRebateEdit.setProgress(rebate);
        tvRebateEditValue.setText(rebate + "%");
    }

    private double recalculate(double units) {
        if (units <= 200) return units * 0.218;
        else if (units <= 300) return 200*0.218 + (units-200)*0.334;
        else if (units <= 600) return 200*0.218 + 100*0.334 + (units-300)*0.516;
        else return 200*0.218 + 100*0.334 + 300*0.516 + (units-600)*0.546;
    }

    private void updateRecord() {
        String unitsStr = etUnitsEdit.getText().toString().trim();
        if (unitsStr.isEmpty()) {
            etUnitsEdit.setError("Enter units");
            return;
        }
        double units = Double.parseDouble(unitsStr);
        if (units < 1 || units > 1000) {
            etUnitsEdit.setError("Units must be 1–1000");
            return;
        }
        String month = spinnerMonth.getSelectedItem().toString();
        int rebate = seekBarRebateEdit.getProgress();
        double total = recalculate(units);
        double finalCost = total - (total * rebate / 100.0);

        dbHelper.updateBill(recordId, month, units, rebate, total, finalCost);
        Toast.makeText(this, "✅ Record updated!", Toast.LENGTH_SHORT).show();
        loadRecord(); // Refresh displayed info
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to delete this bill record?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteBill(recordId);
                    Toast.makeText(this, "🗑️ Record deleted.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}