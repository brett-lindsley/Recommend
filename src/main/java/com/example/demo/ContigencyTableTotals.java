package com.example.demo;

public class ContigencyTableTotals {
	
	private double c[][] = new double[2][2];
	private double rowTotals[];
	private double colTotals[];
	private double totalCount;
	
	
	public ContigencyTableTotals(double[][] c, double[] rowTotals, double[] colTotals, double totalCount) {
		super();
		this.c = c;
		this.rowTotals = rowTotals;
		this.colTotals = colTotals;
		this.totalCount = totalCount;
	}
	
	
	public double[][] getC() {
		return c;
	}
	public void setC(double[][] c) {
		this.c = c;
	}
	public double[] getRowTotals() {
		return rowTotals;
	}
	public void setRowTotals(double[] rowTotals) {
		this.rowTotals = rowTotals;
	}
	public double[] getColTotals() {
		return colTotals;
	}
	public void setColTotals(double[] colTotals) {
		this.colTotals = colTotals;
	}
	public double getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(double totalCount) {
		this.totalCount = totalCount;
	}
	


}
