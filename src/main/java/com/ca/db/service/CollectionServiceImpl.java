package com.ca.db.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.ca.db.model.PurchaseEntry;
import com.ca.db.model.PurchasePaymentInfo;
import com.ca.db.model.PaymentType;
import com.ca.db.model.SaleCollectionInfo;
import com.ca.db.model.SaleEntry;
import com.ca.db.service.dto.ReturnedItemDTO;
import com.gt.common.utils.PaymentStatusUtil;
import com.gt.db.BaseDAO;

public class CollectionServiceImpl extends BaseDAO {

	public CollectionServiceImpl() throws Exception {
		super();
	}

	/**
	 * FIXME: do i save returned item to Item table ?
	 */
	public static void addCollectionDetails(int saleId, List<SaleCollectionInfo> collectionInfos) throws Exception {
		Session s = getSession();
		Transaction tx = s.beginTransaction();
		try {

			// Delete the existing payment history
			Criteria c = s.createCriteria(SaleCollectionInfo.class);
			c.setFetchMode("saleEntry", FetchMode.JOIN);
			c.add(Restrictions.eq("saleEntry.id", saleId));
			List<SaleCollectionInfo> infos = (List<SaleCollectionInfo>) (c.list());
			for (SaleCollectionInfo entry : infos) {
				s.delete(entry);
			}
			// save transfer entry
			c = s.createCriteria(SaleEntry.class);
			c.add(Restrictions.eq("id", saleId));
			SaleEntry saleEntry = (SaleEntry) (c.list()).get(0);
			BigDecimal totalReceivedAmt = new BigDecimal("0");
			for (SaleCollectionInfo info : collectionInfos) {
				SaleCollectionInfo paymentInfo = new SaleCollectionInfo();
				paymentInfo.setAmount(info.getAmount());
				totalReceivedAmt = totalReceivedAmt.add(info.getAmount());
				paymentInfo.setDate(info.getDate());
				paymentInfo.setNote(info.getNote());
				paymentInfo.setSaleEntry(saleEntry);
				s.save(paymentInfo);
			}

			saleEntry.setReceivedAmt(totalReceivedAmt);
			saleEntry.setPendingAmt(saleEntry.getTotalValue().subtract(totalReceivedAmt));
			s.update(saleEntry);

			tx.commit();

		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
			throw new Exception("Item return failed " + e.getMessage());
		} finally {

			close(s);
		}
	}

}
