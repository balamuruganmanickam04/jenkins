package com.gt.common.utils;

import java.util.List;

import com.ca.db.model.PurchaseEntry;
import com.ca.db.model.ProductInfo;
import com.ca.db.service.DBUtils;
import com.gt.uilib.components.input.DataComboBox;

public class ProductComboUtil {

	public static void addProductDetails(DataComboBox cmbCategory) {
		cmbCategory.init();
		try {
			List<ProductInfo> cl = DBUtils.readAll(ProductInfo.class);
			for (ProductInfo c : cl) {
				cmbCategory.addRow(new Object[] { c.getId(), c.getName() + "_" + c.getType() + "_" + c.getWeight() });
			}
		} catch (Exception e) {
		}

	}

	public static String getProductDisplayName(PurchaseEntry item) {
		ProductInfo c = item.getProduct();
		return c.getName() + "_" + c.getType() + "_" + c.getWeight();
	}

}
