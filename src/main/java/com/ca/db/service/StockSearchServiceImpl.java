package com.ca.db.service;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;

import com.ca.db.model.StockBalanceInfo;
import com.gt.common.utils.DateTimeUtils;
import com.gt.db.BaseDAO;

public class StockSearchServiceImpl extends BaseDAO {

	public StockSearchServiceImpl() {
		super();
	}

	@SuppressWarnings("deprecation")
	/*
	 * used by item transfer entry and stock query
	 */
	public static List<StockBalanceInfo> StockSearchQuery(int categoryId, int vendorId, Date fromDate, Date toDate) {

		Criteria c = getSession().createCriteria(StockBalanceInfo.class);
		/*
		 * read all items that has not been transferred and qty >0, the items with qty
		 * =0 is copied to new by setting transferred status
		 * Item.ACCOUNT_TRANSFERRED_TO_NEW
		 */
		c.setFetchMode("purchaseEntry", FetchMode.JOIN);
		if (categoryId > 0) {
			c.add(Restrictions.eq("purchaseEntry.id", categoryId));
		}
		if (vendorId > 0) {
			c.setFetchMode("purchaseEntry.vendor", FetchMode.JOIN);
			c.add(Restrictions.eq("purchaseEntry.vendor.id", vendorId));
		}

		if (!DateTimeUtils.isEmpty(fromDate)) {
			c.add(Restrictions.ge("purchaseEntry.purchaseDate", fromDate));
		}
		if (!DateTimeUtils.isEmpty(toDate)) {
			c.add(Restrictions.le("purchaseEntry.purchaseDate", toDate));
		}
		return c.list();
	}

	public static List<StockBalanceInfo> getAddedItems() {
		Criteria c = getSession().createCriteria(StockBalanceInfo.class);
		// c.createAlias("item", "it");
		// TODO:fiscal year
		return c.list();
	}

}
