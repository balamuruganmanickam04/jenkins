package com.ca.db.service.dto;

public class SaleItemDTO {

	private final int fullqty;
	private final int partialqty;
	private final double price;

	public SaleItemDTO(int fullqty, int partialqty, double price) {
		super();
		this.fullqty = fullqty;
		this.partialqty = partialqty;
		this.price = price;
	}

	public int getFullqty() {
		return fullqty;
	}

	public int getPartialqty() {
		return partialqty;
	}

	public double getPrice() {
		return price;
	}

}
