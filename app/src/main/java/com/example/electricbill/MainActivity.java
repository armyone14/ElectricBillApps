package com.example.electricbill;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Input widgets
    private Spinner spinnerMonth;
    private EditText etUnits;
    private SeekBar seekBarRebate;
    private TextView tvRebateValue;

    // Output widgets
    private TextView tvTotalCharges, tvFinalCost;
    private LinearLayout layoutResult;

    // Buttons
    private Button btnCalculate, btnSave, btnClear;

    // State
    private double calculatedTotal = 0;
    private double calculatedFinal = 0;
    private DatabaseHelper dbHelper;

    private final String[] MONTHS = {
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Bind views
        spinnerMonth   = findViewById(R.id.spinnerMonth);
        etUnits        = findViewById(R.id.etUnits);
        seekBarRebate  = findViewById(R.id.seekBarRebate);
        tvRebateValue  = findViewById(R.id.tvRebateValue);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost    = findViewById(R.id.tvFinalCost);
        layoutResult   = findViewById(R.id.layoutResult);
        btnCalculate   = findViewById(R.id.btnCalculate);
        btnSave        = findViewById(R.id.btnSave);
        btnClear       = findViewById(R.id.btnClear);

        // Spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, MONTHS);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // SeekBar: 0–5 (integer steps = 0%,1%,2%,3%,4%,5%)
        seekBarRebate.setMax(5);
        seekBarRebate.setProgress(0);
        tvRebateValue.setText("0%");
        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                tvRebateValue.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        // Buttons
        btnCalculate.setOnClickListener(v -> calculate());
        btnSave.setOnClickListener(v -> saveToDatabase());
        btnClear.setOnClickListener(v -> clearAll());

        // Hide result + save initially
        layoutResult.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
    }

    private void calculate() {
        // Validate units
        String unitsStr = etUnits.getText().toString().trim();
        if (unitsStr.isEmpty()) {
            etUnits.setError("Please enter units used (1–1000)");
            etUnits.requestFocus();
            return;
        }
        double units = Double.parseDouble(unitsStr);
        if (units < 1 || units > 1000) {
            etUnits.setError("Units must be between 1 and 1000 kWh");
            etUnits.requestFocus();
            return;
        }

        // Calculate tiered charges
        double total = 0;
        if (units <= 200) {
            total = units * 0.218;
        } else if (units <= 300) {
            total = 200 * 0.218 + (units - 200) * 0.334;
        } else if (units <= 600) {
            total = 200 * 0.218 + 100 * 0.334 + (units - 300) * 0.516;
        } else {
            total = 200 * 0.218 + 100 * 0.334 + 300 * 0.516 + (units - 600) * 0.546;
        }

        int rebatePct = seekBarRebate.getProgress();
        double finalCost = total - (total * rebatePct / 100.0);

        calculatedTotal = total;
        calculatedFinal = finalCost;

        // Show results
        tvTotalCharges.setText(String.format(Locale.getDefault(), "RM %.2f", total));
        tvFinalCost.setText(String.format(Locale.getDefault(), "RM %.2f", finalCost));
        layoutResult.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);

        Toast.makeText(this, "✅ Calculation complete!", Toast.LENGTH_SHORT).show();
    }

    private void saveToDatabase() {
        String month = spinnerMonth.getSelectedItem().toString();
        String unitsStr = etUnits.getText().toString().trim();
        if (unitsStr.isEmpty()) {
            Toast.makeText(this, "Please calculate first!", Toast.LENGTH_SHORT).show();
            return;
        }
        double units = Double.parseDouble(unitsStr);
        int rebatePct = seekBarRebate.getProgress();

        long result = dbHelper.insertBill(month, units, rebatePct, calculatedTotal, calculatedFinal);
        if (result != -1) {
            Toast.makeText(this, "✅ Bill saved successfully!", Toast.LENGTH_SHORT).show();
            btnSave.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "❌ Error saving bill.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearAll() {
        spinnerMonth.setSelection(0);
        etUnits.setText("");
        seekBarRebate.setProgress(0);
        tvRebateValue.setText("0%");
        layoutResult.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
        calculatedTotal = 0;
        calculatedFinal = 0;
        Toast.makeText(this, "Form cleared.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "📋 History").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 2, 1, "ℹ️ About").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            startActivity(new Intent(this, HistoryActivity.class));
            return true;
        } else if (item.getItemId() == 2) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}