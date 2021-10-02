package com.gt.uilib.components.table;

import javax.swing.table.DefaultTableModel;

/**
 * IMP: by default- second column contains key, but it is adjustable by
 * setKeyColumn()
 *
 * @author gtiwari333@gmail.com
 */
public class EasyTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 8952987986047661236L;
	protected String[] header;
	private int KEY_ID_COLUMN = 1;

	protected EasyTableModel() {
	}

	public EasyTableModel(String[] header) {
		this.header = header;
		for (String aHeader : header) {
			addColumn(aHeader);
		}
	}

	public final Integer getKeyAtRow(int row) {
		return (Integer) getValueAt(row, KEY_ID_COLUMN);
	}

	public final Integer getKeyAtRow(int row, int column) {
		return (Integer) getValueAt(row, column);
	}

	public final void setKeyColumn(int keyCol) {
		this.KEY_ID_COLUMN = keyCol;
	}

	/**
	 * on the assumption that second column contains key
	 *
	 * @param key
	 * @return
	 */
	public final boolean containsKey(Integer key, Integer idValue) {

		int rowC = getRowCount();
		for (int i = 0; i < rowC; i++) {
			Integer keyAtRowI = (Integer) getValueAt(i, key);
			if (keyAtRowI.equals(idValue)) {
				return true;
			}
		}
		return false;
	}

	public final boolean containsKey(Integer key) {

		int rowC = getRowCount();
		for (int i = 0; i < rowC; i++) {
			Integer keyAtRowI = getKeyAtRow(i, key);
			if (keyAtRowI.equals(key)) {
				return true;
			}
		}
		return false;
	}

	public final void removeRowWithKey(Integer key) {
		if (key == null || key < 0) {
			return;
		}
		removeRow(key);
	}

	public final void addRow(Object[] values) {
		super.addRow(values);
//		System.out.println("EasyTableModel.addRow() >>  "+getRowCount());
	}

	public final void resetModel() {
		super.setRowCount(0);
	}

}
