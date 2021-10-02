package com.ca.db.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SaleCollectionInfo")
public class SaleCollectionInfo {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@Column(name = "date")
	private Date date;

	@ManyToOne
	private SaleEntry saleEntry;

	@Column(name = "amount")
	private BigDecimal amount;

	@Column(name = "paymentType")
	private PaymentType paymentType;

	@Column(name = "note")
	private String note;

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public SaleEntry getSaleEntry() {
		return saleEntry;
	}

	public void setSaleEntry(SaleEntry saleEntry) {
		this.saleEntry = saleEntry;
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

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}