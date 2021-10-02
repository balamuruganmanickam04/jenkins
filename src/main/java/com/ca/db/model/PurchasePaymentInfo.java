package com.ca.db.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PurchasePaymentInfo")
public class PurchasePaymentInfo {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@Column(name = "date")
	private Date date;

	@ManyToOne
	private PurchaseEntry purchaseEntry;

	@Column(name = "amount")
	private BigDecimal amount;

	@Column(name = "note")
	private String note;

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

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}