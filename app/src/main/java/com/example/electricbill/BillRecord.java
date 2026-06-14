package com.example.electricbill;

public class BillRecord {
    private int id;
    private String month;
    private double units;
    private double rebatePercent;
    private double totalCharges;
    private double finalCost;

    public BillRecord() {}

    public BillRecord(int id, String month, double units, double rebatePercent,
                      double totalCharges, double finalCost) {
        this.id = id;
        this.month = month;
        this.units = units;
        this.rebatePercent = rebatePercent;
        this.totalCharges = totalCharges;
        this.finalCost = finalCost;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getUnits() { return units; }
    public void setUnits(double units) { this.units = units; }

    public double getRebatePercent() { return rebatePercent; }
    public void setRebatePercent(double rebatePercent) { this.rebatePercent = rebatePercent; }

    public double getTotalCharges() { return totalCharges; }
    public void setTotalCharges(double totalCharges) { this.totalCharges = totalCharges; }

    public double getFinalCost() { return finalCost; }
    public void setFinalCost(double finalCost) { this.finalCost = finalCost; }
}