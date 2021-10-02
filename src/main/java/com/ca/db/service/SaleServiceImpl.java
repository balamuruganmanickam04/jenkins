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

import com.ca.db.model.CustomerInfo;
import com.ca.db.model.PurchaseEntry;
import com.ca.db.model.SaleEntry;
import com.ca.db.model.StockBalanceInfo;
import com.ca.db.service.dto.SaleItemDTO;
import com.ca.ui.panels.ItemReceiverPanel.ReceiverType;
import com.gt.common.exception.QuantityException;
import com.gt.common.utils.DateTimeUtils;
import com.gt.db.BaseDAO;

public class SaleServiceImpl extends BaseDAO {

	public SaleServiceImpl() {
		super();
	}

	public static void saveTransfer(Map<Integer, SaleItemDTO> cartMap, Date transferDate, ReceiverType type, int id,
			String requestNumber) throws Exception {
		Session s = getSession();
		Transaction tx = s.beginTransaction();

		for (Entry<Integer, SaleItemDTO> entry : cartMap.entrySet()) {
			try {
				int itemId = entry.getKey();
				SaleItemDTO dto = entry.getValue();

				// get stock info
				Criteria c = s.createCriteria(StockBalanceInfo.class);
				c.add(Restrictions.eq("id", itemId));
				StockBalanceInfo info = (StockBalanceInfo) (c.list()).get(0);

				// save transfer entry
				SaleEntry transfer = new SaleEntry();
				c = s.createCriteria(PurchaseEntry.class);
				c.add(Restrictions.eq("id", info.getPurchaseEntry().getId()));
				PurchaseEntry item = (PurchaseEntry) (c.list()).get(0);

				switch (type) {
				case OFFICIAL:

					Criteria cb = s.createCriteria(CustomerInfo.class);
					cb.add(Restrictions.eq("id", id));
					CustomerInfo br = (CustomerInfo) (cb.list()).get(0);

					transfer.setCustomerInfo(br);
					break;
				default:
					break;
				}
				transfer.setPurchaseEntry(item);
				transfer.setFullQuantity(dto.getFullqty());
				transfer.setPartialQuantity(dto.getPartialqty());
				double totalSaleValue = item.getProduct().getWeight() * dto.getFullqty() * dto.getPrice();
				if (dto.getFullqty() > 0) {
					totalSaleValue = totalSaleValue + dto.getPartialqty() * dto.getPrice();
				}
				transfer.setTotalValue(new BigDecimal(totalSaleValue));
				transfer.setReceivedAmt(null);
				transfer.setPendingAmt(null);
				transfer.setSoldDate(transferDate);
				// save/update tables
				s.update(item);
				s.save(transfer);

				info.setSoldFullQuantity(info.getSoldFullQuantity() + dto.getFullqty());
				info.setAvailableFullQuantity(info.getAvailableFullQuantity() - dto.getFullqty());

				if (dto.getPartialqty() > 0) {

					if (info.getAvailablePartialQuantity() > dto.getPartialqty()) {
						info.setAvailablePartialQuantity(info.getAvailablePartialQuantity() - dto.getPartialqty());
						info.setSoldPartialQuantity(info.getSoldPartialQuantity() + dto.getPartialqty());
					} else if (info.getAvailablePartialQuantity() == dto.getPartialqty()) {
						info.setAvailablePartialQuantity(0);
						info.setSoldPartialQuantity(0);
						info.setSoldFullQuantity(info.getSoldFullQuantity() + 1);
					} else if (info.getAvailablePartialQuantity() < dto.getPartialqty()) {
						info.setAvailablePartialQuantity(
								(item.getProduct().getWeight().intValue() + info.getAvailablePartialQuantity())
										- dto.getPartialqty());
						info.setSoldPartialQuantity(
								item.getProduct().getWeight().intValue() - info.getAvailablePartialQuantity());
						info.setSoldFullQuantity(info.getSoldFullQuantity() + 1);
						info.setAvailableFullQuantity(info.getAvailableFullQuantity() - 1);

					}
				}
				s.update(info);

				if (info.getAvailableFullQuantity() < 0 || info.getAvailablePartialQuantity() < 0) {
					throw new QuantityException(
							"Item cannot be sold more than available quantity. Check the available quantity");
				}
				tx.commit();
			} catch (QuantityException er) {
				tx.rollback();
				throw new QuantityException(er.getMessage());
			} catch (Exception er) {
				tx.rollback();
				er.printStackTrace();
				throw new Exception("Item transfer failed " + er.getMessage());
			} finally {

				close(s);
			}
		}
	}

	/**
	 * accessed by ItemReturnPanel
	 */
	public static List<SaleEntry> getSaleEntries(int categoryId, int receiverId, Date fromDate, Date toDate)
			throws Exception {
		Criteria c = getSession().createCriteria(SaleEntry.class);
		if (receiverId > 0) {
			c.setFetchMode("customerInfo", FetchMode.JOIN);
			c.add(Restrictions.eq("customerInfo.id", receiverId));
			// if any other
		}
		c.createAlias("purchaseEntry", "purchase");
		c.createAlias("purchase.product", "product");
		// only returnable
		if (categoryId > 0) {
			// http://stackoverflow.com/questions/8726396/hibernate-criteria-join-with-3-tables
			c.add(Restrictions.eq("product.id", categoryId));
		}

		if (!DateTimeUtils.isEmpty(fromDate)) {
			c.add(Restrictions.ge("transferDate", fromDate));
		}
		if (!DateTimeUtils.isEmpty(toDate)) {
			c.add(Restrictions.le("transferDate", toDate));
		}

		return c.list();
	}

	public static boolean isProductStartedSelling(int purchaseTechId) throws Exception {
		Session s = getSession();
		Transaction tx = s.beginTransaction();

		try {
			Criteria c = s.createCriteria(SaleEntry.class);
			c.setFetchMode("purchaseEntry", FetchMode.JOIN);
			c.add(Restrictions.eq("purchaseEntry.id", purchaseTechId));
			return c.list() != null && !c.list().isEmpty();
		} catch (Exception er) {
			tx.rollback();
			er.printStackTrace();
			throw new Exception("Search sale Failed " + er.getMessage());
		} finally {
			close(s);
		}
	}

}
