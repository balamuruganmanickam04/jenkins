package com.ca.db.service;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;

import com.ca.db.model.PurchaseEntry;
import com.gt.common.utils.DateTimeUtils;
import com.gt.db.BaseDAO;

public class ItemServiceImpl extends BaseDAO {

	public ItemServiceImpl() {
		super();
	}

	@SuppressWarnings("deprecation")
	/*
	 * used by item transfer entry and stock query
	 */
	public static List<PurchaseEntry> itemStockQuery(int categoryId, int vendorId, Date fromDate, Date toDate) {

		Criteria c = getSession().createCriteria(PurchaseEntry.class);
		// c.createAlias("item", "it");
		c.add(Restrictions.gt("quantity", 0));
		/*
		 * read all items that has not been transferred and qty >0, the items with qty
		 * =0 is copied to new by setting transferred status
		 * Item.ACCOUNT_TRANSFERRED_TO_NEW
		 */
		if (categoryId > 0) {
			c.setFetchMode("category", FetchMode.JOIN);
			c.add(Restrictions.eq("category.id", categoryId));
		}
		if (vendorId > 0) {
			c.setFetchMode("vendor", FetchMode.JOIN);
			c.add(Restrictions.eq("vendor.id", vendorId));
		}

		if (!DateTimeUtils.isEmpty(fromDate)) {
			c.add(Restrictions.ge("purchaseDate", fromDate));
		}
		if (!DateTimeUtils.isEmpty(toDate)) {
			c.add(Restrictions.le("purchaseDate", toDate));
		}
		return c.list();
	}

	public static List<PurchaseEntry> getAddedItems() {
		Criteria c = getSession().createCriteria(PurchaseEntry.class);
		// c.createAlias("item", "it");
		// TODO:fiscal year
		return c.list();
	}

}
