package com.gt.common.utils;

import java.math.BigDecimal;

import com.ca.db.model.PaymentStatus;
import com.ca.db.model.PurchaseEntry;

public class PaymentStatusUtil {

	public static void updatePaymentStatus(PurchaseEntry purchaseEntry, BigDecimal totalPaidAmt) {
		if (purchaseEntry.getTotalValue().compareTo(totalPaidAmt) == 0) {
			purchaseEntry.setPaymentStatus(PaymentStatus.PAID);
		} else if (purchaseEntry.getPendingAmt().compareTo(purchaseEntry.getTotalValue()) == 0) {
			purchaseEntry.setPaymentStatus(PaymentStatus.NOT_PAID);
		} else if (purchaseEntry.getPendingAmt().compareTo(BigDecimal.ZERO) > 0) {
			purchaseEntry.setPaymentStatus(PaymentStatus.PARTIAL);
		} else if (purchaseEntry.getTotalValue().compareTo(totalPaidAmt) < 0) {
			purchaseEntry.setPaymentStatus(PaymentStatus.OVER_PAID);
		}
	}

}
