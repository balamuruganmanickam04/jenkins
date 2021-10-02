package com.ca.db.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author BALA MURUGAN
 *
 */
@Entity
@Table(name = "SaleEntry")
public class SaleEntry {
	public static final int STATUS_RETURNED_ALL = 2;
	public static final int STATUS_NOT_RETURNED = 1;

	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@OneToOne
	private CustomerInfo customerInfo; // lend to another office

	@Column(name = "soldDate")
	private Date soldDate;

	@OneToOne
	private PurchaseEntry purchaseEntry;

	@Column(name = "fullQuantity")
	private Integer fullQuantity;

	@Column(name = "partialQuantity")
	private Integer partialQuantity;

	@Column(name = "totalValue")
	private BigDecimal totalValue;

	@Column(name = "receivedAmt")
	private BigDecimal receivedAmt;

	@Column(name = "pendingAmt")
	private BigDecimal pendingAmt;

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

	public Integer getFullQuantity() {
		return fullQuantity;
	}

	public void setFullQuantity(Integer fullQuantity) {
		this.fullQuantity = fullQuantity;
	}

	public Integer getPartialQuantity() {
		return partialQuantity;
	}

	public void setPartialQuantity(Integer partialQuantity) {
		this.partialQuantity = partialQuantity;
	}

	public CustomerInfo getCustomerInfo() {
		return customerInfo;
	}

	public void setCustomerInfo(CustomerInfo customerInfo) {
		this.customerInfo = customerInfo;
	}

	public Date getSoldDate() {
		return soldDate;
	}

	public void setSoldDate(Date soldDate) {
		this.soldDate = soldDate;
	}

	public BigDecimal getTotalValue() {
		return totalValue;
	}

	public void setTotalValue(BigDecimal totalValue) {
		this.totalValue = totalValue;
	}

	public BigDecimal getReceivedAmt() {
		return receivedAmt;
	}

	public void setReceivedAmt(BigDecimal receivedAmt) {
		this.receivedAmt = receivedAmt;
	}

	public BigDecimal getPendingAmt() {
		return pendingAmt;
	}

	public void setPendingAmt(BigDecimal pendingAmt) {
		this.pendingAmt = pendingAmt;
	}

	public String toString() {

		String builder = "\nTransfer [id=" + this.id + ", customer=" + this.customerInfo + ", soldDate=" + this.soldDate
				+ ", fullQuantity=" + this.fullQuantity + ", totalValue=" + this.totalValue + ", amtReceived="
				+ this.receivedAmt + ", amtPending=" + this.pendingAmt + "]";
		return builder;
	}
}