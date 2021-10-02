package com.ca.db.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ChargeInfo")
public class ChargeInfo {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@Column(name = "chageCategory")
	private Double chageCategory;

	@Column(name = "chargeValue")
	private Double chargeValue;

	@Column(name = "appliedToAll")
	private Boolean appliedToAll;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Double getChageCategory() {
		return chageCategory;
	}

	public void setChageCategory(Double chageCategory) {
		this.chageCategory = chageCategory;
	}

	public Double getChargeValue() {
		return chargeValue;
	}

	public void setChargeValue(Double chargeValue) {
		this.chargeValue = chargeValue;
	}

	public Boolean getAppliedToAll() {
		return appliedToAll;
	}

	public void setAppliedToAll(Boolean appliedToAll) {
		this.appliedToAll = appliedToAll;
	}

}