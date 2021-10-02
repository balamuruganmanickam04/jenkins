package com.ca.db.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "StockBalanceInfo")
public class StockBalanceInfo {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@Column(name = "date")
	private Date date;

	@ManyToOne
	private PurchaseEntry purchaseEntry;

	@Column(name = "soldFullQuantity")
	private int soldFullQuantity;

	@Column(name = "soldPartialQuantity")
	private int soldPartialQuantity;

	@Column(name = "totalQuantity")
	private int totalQuantity;

	@Column(name = "availableFullQuantity")
	private int availableFullQuantity;

	@Column(name = "availablePartialQuantity")
	private int availablePartialQuantity;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public PurchaseEntry getPurchaseEntry() {
		return purchaseEntry;
	}

	public void setPurchaseEntry(PurchaseEntry purchaseEntry) {
		this.purchaseEntry = purchaseEntry;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getSoldFullQuantity() {
		return soldFullQuantity;
	}

	public void setSoldFullQuantity(int soldFullQuantity) {
		this.soldFullQuantity = soldFullQuantity;
	}

	public int getSoldPartialQuantity() {
		return soldPartialQuantity;
	}

	public void setSoldPartialQuantity(int soldPartialQuantity) {
		this.soldPartialQuantity = soldPartialQuantity;
	}

	public int getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public int getAvailableFullQuantity() {
		return availableFullQuantity;
	}

	public void setAvailableFullQuantity(int availableFullQuantity) {
		this.availableFullQuantity = availableFullQuantity;
	}

	public int getAvailablePartialQuantity() {
		return availablePartialQuantity;
	}

	public void setAvailablePartialQuantity(int availablePartialQuantity) {
		this.availablePartialQuantity = availablePartialQuantity;
	}

}