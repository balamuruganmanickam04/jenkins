package com.ca.db.model;

import java.util.ArrayList;
import java.util.List;

public enum PaymentStatus {

	PAID(1, "Paid"), PARTIAL(2, "Partially paid"), NOT_PAID(3, "Not paid"), OVER_PAID(4, "Over paid");

	private final int id;
	private final String value;

	PaymentStatus(int id, String value) {
		this.id = id;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	public static List<PaymentStatus> getEnums() {

		List<PaymentStatus> paymentStatus = new ArrayList<>();
		paymentStatus.add(PAID);
		paymentStatus.add(PARTIAL);
		paymentStatus.add(NOT_PAID);
		paymentStatus.add(OVER_PAID);

		return paymentStatus;
	}

	public static PaymentStatus getEnum(int id) {
		for (PaymentStatus status : getEnums()) {
			if (status.getId() == id) {
				return status;
			}
		}
		return null;
	}

}
