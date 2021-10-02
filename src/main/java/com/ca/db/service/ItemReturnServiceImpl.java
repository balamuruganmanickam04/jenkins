package com.ca.db.service;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.ca.db.model.PurchaseEntry;
import com.ca.db.model.PaymentType;
import com.ca.db.model.SaleCollectionInfo;
import com.ca.db.model.SaleEntry;
import com.ca.db.service.dto.ReturnedItemDTO;
import com.gt.db.BaseDAO;

public class ItemReturnServiceImpl extends BaseDAO {

	public ItemReturnServiceImpl() throws Exception {
		super();
	}

	/**
	 * FIXME: do i save returned item to Item table ?
	 */
	public static void saveReturnedItem(Map<Integer, ReturnedItemDTO> cartMap, String returnNumber) throws Exception {
		Session s = getSession();
		Transaction tx = s.beginTransaction();
		try {
			for (Entry<Integer, ReturnedItemDTO> entry : cartMap.entrySet()) {

				int itemId = entry.getKey();
				ReturnedItemDTO ret = entry.getValue();
				int qty = ret.qty;
				// TODO: return item status
				int damageStatus = ret.damageStatus;

				Criteria c = s.createCriteria(SaleEntry.class);
				c.add(Restrictions.eq("id", itemId));
				SaleEntry saleEntry = (SaleEntry) (c.list()).get(0);

				SaleCollectionInfo saleCollectionInfo = new SaleCollectionInfo();
				saleCollectionInfo.setSaleEntry(saleEntry);
				saleCollectionInfo.setAmount(null);

				saleEntry.setPendingAmt(saleCollectionInfo.getAmount());
				// add return number
				saleCollectionInfo.setPaymentType(PaymentType.CASH_CARRY);
				saleCollectionInfo.setNote("");

				PurchaseEntry itemOld = saleEntry.getPurchaseEntry();
				// should not be >item.originalquantity
				itemOld.setQuantity(itemOld.getQuantity() + qty);

				PurchaseEntry newBo = new PurchaseEntry();
				newBo.setPurchaseDate(itemOld.getPurchaseDate());
				newBo.setRate(itemOld.getRate());
				newBo.setQuantity(itemOld.getQuantity());
				newBo.setPurchaseDate(itemOld.getPurchaseDate());
				newBo.setProduct(itemOld.getProduct());

				s.update(saleEntry);
				s.save(saleCollectionInfo);
				// TODO: order table - status change

			}
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
