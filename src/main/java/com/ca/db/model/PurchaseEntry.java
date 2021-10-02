package com.ca.db.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PurchaseEntry")
public class PurchaseEntry {
	public static final Integer ADD_TYPE_NEW_ENTRY = 1;
	public static final Integer ADD_TYPE_RETURNED_ENTRY = 2;

	public static final Integer ACCOUNT_TRANSFERRED_TO_NEW = 22;

	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@OneToOne
	private ProductInfo product;

	@OneToOne
	private Vendor vendor;

	@Column(name = "quantity")
	private int quantity;

	@Column(name = "rate")
	private BigDecimal rate;

	@Column(name = "purchasedate")
	private Date purchaseDate;

	@Column(name = "otherCharge")
	private BigDecimal otherCharge;

	@Column(name = "totalValue")
	private BigDecimal totalValue;

	@Column(name = "paymentStatus")
	private PaymentStatus paymentStatus;

	@Column(name = "paidAmt")
	private BigDecimal paidAmt;

	@Column(name = "pendingAmt")
	private BigDecimal pendingAmt;

	public Date getPurchaseDate() {
		return this.purchaseDate;
	}

	public void setPurchaseDate(Date purchaseDate) {
		this.purchaseDate = purchaseDate;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public int getQuantity() {
		return this.quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BigDecimal getRate() {
		return this.rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public BigDecimal getOtherCharge() {
		return otherCharge;
	}

	public void setOtherCharge(BigDecimal otherCharge) {
		this.otherCharge = otherCharge;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public ProductInfo getProduct() {
		return product;
	}

	public void setProduct(ProductInfo product) {
		this.product = product;
	}

	public BigDecimal getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(BigDecimal totalValue) {
		this.totalValue = totalValue;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public BigDecimal getPaidAmt() {
		return paidAmt;
	}

	public void setPaidAmt(BigDecimal paidAmt) {
		this.paidAmt = paidAmt;
	}

	public BigDecimal getPendingAmt() {
		return pendingAmt;
	}

	public void setPendingAmt(BigDecimal pendingAmt) {
		this.pendingAmt = pendingAmt;
	}

	public String toString() {
		String builder = "\nItem [id=" + this.id + ", rice product=" + this.product + ", " + "rate=" + this.rate
				+ ", quantity=" + this.quantity + " , purchaseDate=" + this.purchaseDate;
		return builder;
	}
}