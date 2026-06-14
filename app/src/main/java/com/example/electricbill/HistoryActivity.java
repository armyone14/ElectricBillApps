package com.example.electricbill;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseHelper dbHelper;
    private List<BillRecord> billList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bill History");
        }

        dbHelper = new DatabaseHelper(this);
        listView = findViewById(R.id.listViewBills);
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Refresh when returning from DetailActivity
    }

    private void loadData() {
        billList = dbHelper.getAllBills();

        if (billList.isEmpty()) {
            Toast.makeText(this, "No records yet. Calculate and save a bill first!",
                    Toast.LENGTH_LONG).show();
        }

        // Build display strings: "January  →  RM 32.00"
        ArrayList<String> displayList = new ArrayList<>();
        for (BillRecord r : billList) {
            displayList.add(r.getMonth() + "   →   RM " +
                    String.format(Locale.getDefault(), "%.2f", r.getFinalCost()));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.list_item_bill, R.id.tvListItem, displayList);
        listView.setAdapter(adapter);

        // Click → DetailActivity
        listView.setOnItemClickListener((parent, view, position, id) -> {
            BillRecord selected = billList.get(position);
            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("RECORD_ID", selected.getId());
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}