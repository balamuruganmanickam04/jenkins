package com.ca.db.service;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.ca.db.model.PurchaseEntry;
import com.ca.db.model.PurchasePaymentInfo;
import com.ca.db.model.SaleEntry;
import com.ca.db.model.StockBalanceInfo;
import com.gt.common.exception.QuantityException;
import com.gt.common.utils.PaymentStatusUtil;
import com.gt.db.BaseDAO;

public class PurchasePaymentServiceImpl extends BaseDAO {

	public PurchasePaymentServiceImpl() {
		super();
	}

	public static void savePaymentDetails(int purchaseTechId, List<PurchasePaymentInfo> paymentInfos) throws Exception {
		Session s = getSession();
		Transaction tx = s.beginTransaction();

		try {

			// Delete the existing payment history
			Criteria c = s.createCriteria(PurchasePaymentInfo.class);
			c.setFetchMode("purchaseEntry", FetchMode.JOIN);
			c.add(Restrictions.eq("purchaseEntry.id", purchaseTechId));
			List<PurchasePaymentInfo> purchasePaymentInfos = (List<PurchasePaymentInfo>) (c.list());
			for (PurchasePaymentInfo entry : purchasePaymentInfos) {
				s.delete(entry);
			}
			// save transfer entry
			c = s.createCriteria(PurchaseEntry.class);
			c.add(Restrictions.eq("id", purchaseTechId));
			PurchaseEntry purchaseEntry = (PurchaseEntry) (c.list()).get(0);
			BigDecimal totalPaidAmt = new BigDecimal("0");
			for (PurchasePaymentInfo info : paymentInfos) {
				PurchasePaymentInfo paymentInfo = new PurchasePaymentInfo();
				paymentInfo.setAmount(info.getAmount());
				totalPaidAmt = totalPaidAmt.add(info.getAmount());
				paymentInfo.setDate(info.getDate());
				paymentInfo.setNote(info.getNote());
				paymentInfo.setPurchaseEntry(purchaseEntry);
				s.save(paymentInfo);
			}

			purchaseEntry.setPaidAmt(totalPaidAmt);
			purchaseEntry.setPendingAmt(purchaseEntry.getTotalValue().subtract(totalPaidAmt));
			PaymentStatusUtil.updatePaymentStatus(purchaseEntry, totalPaidAmt);
			s.update(purchaseEntry);

			tx.commit();
		} catch (Exception er) {
			tx.rollback();
			er.printStackTrace();
			throw new Exception("Purchase payment detail failed " + er.getMessage());
		} finally {

			close(s);
		}
	}

	public static void modifyStockQty(int purchaseTechId, int modifiedQty) throws Exception {
		Session s = getSession();
		Transaction tx = s.beginTransaction();

		try {

			Criteria c = s.createCriteria(SaleEntry.class);
			c.setFetchMode("purchaseEntry", FetchMode.JOIN);
			c.add(Restrictions.eq("purchaseEntry.id", purchaseTechId));
			if (c.list() == null || c.list().isEmpty()) {

				c = s.createCriteria(StockBalanceInfo.class);
				c.setFetchMode("purchaseEntry", FetchMode.JOIN);
				c.add(Restrictions.eq("purchaseEntry.id", purchaseTechId));

				StockBalanceInfo balanceInfo = (StockBalanceInfo) (c.list().get(0));
				balanceInfo.setTotalQuantity(modifiedQty);
				balanceInfo.setAvailableFullQuantity(modifiedQty);
				s.update(balanceInfo);
				tx.commit();
			} else {
				throw new QuantityException("This product is already started selling");
			}

		} catch (QuantityException er) {
			tx.rollback();
			er.printStackTrace();
			throw new Exception("Modify Purchase Failed " + er.getMessage());
		} catch (Exception er) {
			tx.rollback();
			er.printStackTrace();
			throw new Exception("Modify Purchase Failed " + er.getMessage());
		} finally {

			close(s);
		}
	}

	/**
	 * accessed by PurchasedEntryPanel
	 */
	public static List<PurchasePaymentInfo> getPaymentDetails(int purchaseTechId) throws Exception {
		Session s = getSession();
		try {
			Criteria c = s.createCriteria(PurchasePaymentInfo.class);
			c.createAlias("purchaseEntry", "purchase");
			c.add(Restrictions.le("purchase.id", purchaseTechId));
			return c.list();
		} finally {
			close(s);
		}
	}

	public static void deletePurchase(int purchaseTechId) throws Exception {
		Session s = getSession();
		Transaction tx = s.beginTransaction();
		PurchaseEntry purchaseEntry = null;
		try {

			Criteria c = s.createCriteria(StockBalanceInfo.class);
			c.setFetchMode("purchaseEntry", FetchMode.JOIN);
			c.add(Restrictions.eq("purchaseEntry.id", purchaseTechId));

			StockBalanceInfo balanceInfo = (StockBalanceInfo) (c.list().get(0));
			s.delete(balanceInfo);
			tx.commit();
			tx = s.beginTransaction();
			c = s.createCriteria(PurchaseEntry.class);
			c.add(Restrictions.eq("id", purchaseTechId));
			purchaseEntry = (PurchaseEntry) (c.list()).get(0);
			s.delete(purchaseEntry);
			tx.commit();

		} catch (Exception er) {
			tx.rollback();
			er.printStackTrace();
			throw new Exception("Modify Purchase Failed " + er.getMessage());
		} finally {

			close(s);
		}
	}

}
