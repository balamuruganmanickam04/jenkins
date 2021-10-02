package com.ca.ui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.SystemUtils;

import com.ca.db.model.SaleCollectionInfo;
import com.ca.db.model.SaleEntry;
import com.ca.db.service.CollectionServiceImpl;
import com.ca.db.service.DBUtils;
import com.ca.db.service.SaleServiceImpl;
import com.gt.common.constants.Status;
import com.gt.common.utils.DateTimeUtils;
import com.gt.common.utils.ExcelUtils;
import com.gt.common.utils.ProductComboUtil;
import com.gt.common.utils.UIUtils;
import com.gt.uilib.components.AbstractFunctionPanel;
import com.gt.uilib.components.input.DataComboBox;
import com.gt.uilib.components.table.BetterJTable;
import com.gt.uilib.components.table.EasyTableModel;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.toedter.calendar.JDateChooser;

/**
 * entry of returned items
 *
 * @author GT
 */
public class CollectionPanel extends AbstractFunctionPanel {
	private final String[] header = new String[] { "S.N.", "PRODUCT", "SOLD Date", "Sold To", "Sold Full Qty",
			"Sold Loose Qty", "Total Amt", "Received Amt", "Pending Amt", "TECH ID" };
	private final String[] returnTblHeader = new String[] { "S.No", "Date", "Collected Amt", "Note", "TECH ID" };
	private final List<Integer> editableColumnIdx = new ArrayList<>();
	private final int qtyCol = 5;
	private final int damageStatusCol = 4;
	private ReturnTable returnTable;
	private JPanel formPanel = null;
	private JPanel buttonPanel;
	private JDateChooser txtFromDate;
	private JDateChooser txtToDate;
	private JButton btnSave;
	private JPanel upperPane;
	private JPanel lowerPane;
	private BetterJTable mainSoldTable;
	private EasyTableModel dataModel;
	private EasyTableModel cartDataModel;
	private DataComboBox cmbProduct;
	private JLabel lblFrom;

	/*
	 * Some Inner classes
	 */
	private ItemReceiverPanel itemReceiverPanel;
	private JPanel addToCartPanel;
	private JSplitPane lowerPanel;
	private JPanel cartPanel;
	private JDateChooser transferDateChooser;
	private JButton btnSend;
	private JTextField txtReturnNUmber;

	public CollectionPanel() {
		/*
		 * all gui components added from here;
		 */
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.1);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);
		splitPane.setLeftComponent(getUpperSplitPane());
		splitPane.setRightComponent(getLowerPanel());
		/*
		 * never forget to call after setting up UI
		 */
		init();
	}

	public static void main(String[] args) throws Exception {
		if (SystemUtils.IS_OS_WINDOWS) {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		EventQueue.invokeLater(() -> {
			try {
				JFrame jf = new JFrame();
				CollectionPanel panel = new CollectionPanel();
				jf.setBounds(panel.getBounds());
				jf.getContentPane().add(panel);
				jf.setVisible(true);
				jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private JSplitPane getLowerPanel() {
		if (lowerPanel == null) {
			lowerPanel = new JSplitPane();
			lowerPanel.setContinuousLayout(true);
			lowerPanel.setResizeWeight(0.8);
			lowerPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);

			lowerPanel.setLeftComponent(getLowerSplitPane());

			mainSoldTable.getSelectionModel().addListSelectionListener(e -> {
				int selRow = mainSoldTable.getSelectedRow();
				if (selRow != -1) {
					/*
					 * if second column doesnot have primary id info, then
					 */
					int selectedId = (Integer) dataModel.getValueAt(selRow, 9);
					populatePaymentDetailsInCartTable(selectedId);
				}
			});

			JPanel panel_1 = new JPanel();
			lowerPanel.setRightComponent(panel_1);
			panel_1.setLayout(new BorderLayout(0, 0));

			cartPanel = new JPanel();
			cartPanel.setBorder(new TitledBorder(null, "Sale Collection Details", TitledBorder.LEADING,
					TitledBorder.TOP, null, null));
			panel_1.add(cartPanel, BorderLayout.CENTER);
			cartPanel.setLayout(new FormLayout(
					new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(45dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(27dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(15dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(24dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(9dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(124dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(59dlu;default)"), },
					new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:max(31dlu;default)"),
							FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:max(15dlu;default)"), }));

			JPanel panel_3 = new JPanel();
			cartPanel.add(panel_3, "2, 2, fill, fill");
			panel_3.setLayout(
					new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
							new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
									FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JButton btnAddItem = new JButton("Add Collection");
			panel_3.add(btnAddItem, "2, 2");

			JButton btnDelete = new JButton("Remove Collection");
			btnDelete.addActionListener(e -> {
				if (returnTable.getRowCount() > 0) {
					int selRow = returnTable.getSelectedRow();
					if (selRow != -1) {
						/*
						 * if second column doesnot have primary id info, then
						 */

						int selectedId = (Integer) cartDataModel.getValueAt(selRow, 4);
						System.out.println("Selected ID : " + selectedId + "_  >>  row " + selRow);

						removeSelectedRowInCartTable(selectedId, selRow);

					}
				}
			});
			panel_3.add(btnDelete, "2, 4");
			btnAddItem.addActionListener(e -> {
				if (mainSoldTable.getRowCount() > 0) {
					int selRow = mainSoldTable.getSelectedRow();
					if (selRow != -1) {

						int selectedId = (Integer) dataModel.getValueAt(selRow, 9);
						addSelectedRowInCartTable(selectedId);
					}
				}
			});

			cartPanel.add(getAddToCartPane(), "4, 2, 13, 1, fill, top");

			JLabel lblSentDate = new JLabel("Date");
			cartPanel.add(lblSentDate, "10, 4, default, top");

			transferDateChooser = new JDateChooser();
			transferDateChooser.setDate(new Date());
			cartPanel.add(transferDateChooser, "14, 4, fill, top");

			btnSend = new JButton("Collect");
			btnSend.addActionListener(e -> {

				btnSend.setEnabled(false);

				SwingWorker worker = new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() {
						if (DataEntryUtils.confirmDBSave())
							addCollectionDetails();
						return null;
					}

				};
				worker.addPropertyChangeListener(evt -> {
					System.out
							.println("Event " + evt + " name" + evt.getPropertyName() + " value " + evt.getNewValue());
					if ("DONE".equals(evt.getNewValue().toString())) {
						btnSend.setEnabled(true);
						// task.setText("Test");
					}
				});

				worker.execute();
			});
			cartPanel.add(btnSend, "16, 4, default, top");
		}

		return lowerPanel;
	}

	private void addCollectionDetails() {
		try {
			CollectionServiceImpl.addCollectionDetails(getPurchaseTechId(), getPaymentDetailsFromTable());
			handleTransferSuccess();

		} catch (Exception er) {
			handleDBError(er);
		}

		btnSend.setEnabled(true);
	}

	public int getPurchaseTechId() {
		int rows = returnTable.getRowCount();
		for (int i = 0; i < rows;) {
			return Integer.parseInt(returnTable.getValueAt(i, 4).toString());

		}
		return 0;
	}

	public List<SaleCollectionInfo> getPaymentDetailsFromTable() throws ParseException {
		int rows = returnTable.getRowCount();
		List<SaleCollectionInfo> infos = new ArrayList<>();
		SaleCollectionInfo paymentInfo = null;
		for (int i = 0; i < rows; i++) {
			paymentInfo = new SaleCollectionInfo();
			BigDecimal amount = new BigDecimal(returnTable.getValueAt(i, 2).toString());
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
			String dateInString = returnTable.getValueAt(i, 1).toString();
			Date date = formatter.parse(dateInString);
			String note = returnTable.getValueAt(i, 3).toString();
			paymentInfo.setDate(date);
			paymentInfo.setAmount(amount);
			paymentInfo.setNote(note);
			infos.add(paymentInfo);
		}
		return infos;
	}

	private void handleTransferSuccess() {
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(null, "Saved Successfully");
			cartDataModel.resetModel();
			cartDataModel.fireTableDataChanged();
			UIUtils.clearAllFields(cartPanel);
			itemReceiverPanel.clearAll();
			dataModel.resetModel();
			dataModel.fireTableDataChanged();
		});
	}

	private void removeSelectedRowInCartTable(int selectedId, int selRow) {
		cartDataModel.removeRowWithKey(selRow);
		cartDataModel.fireTableDataChanged();
	}

	private void addSelectedRowInCartTable(int selectedId) {
		try {
			int sn = cartDataModel.getRowCount();

			String pattern = "dd-MM-yyyy";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			String date = simpleDateFormat.format(new Date());
			cartDataModel.addRow(new Object[] { ++sn, date + "", "", "", selectedId });
			returnTable.setModel(cartDataModel);
			cartDataModel.fireTableDataChanged();

		} catch (

		Exception e) {
			System.out.println("populateSelectedRowInForm");
			handleDBError(e);
		}
	}

	@Override
	public final void init() {
		/* never forget to call super.init() */
		super.init();
		UIUtils.clearAllFields(upperPane);
		changeStatus(Status.NONE);
		intCombo();
		editableColumnIdx.add(1);
		editableColumnIdx.add(2);
		editableColumnIdx.add(3);
	}

	private void intCombo() {
		try {
			/* Category Combo */
			ProductComboUtil.addProductDetails(cmbProduct);
		} catch (Exception e) {
			handleDBError(e);
		}
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();

			JButton btnSaveToExcel = new JButton("Save to Excel");
			btnSaveToExcel.addActionListener(e -> {
				JFileChooser jf = new JFileChooser();
				jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jf.showDialog(CollectionPanel.this, "Select Save location");
				String fileName = jf.getSelectedFile().getAbsolutePath();
				try {
					ExcelUtils.writeExcelFromJTable(mainSoldTable, fileName + ".xls");
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Could not save" + e1.getMessage());
				}
			});

			JButton btnPrev = new JButton("<");
			buttonPanel.add(btnPrev);

			JButton btnNext = new JButton(">");
			buttonPanel.add(btnNext);
			buttonPanel.add(btnSaveToExcel);
		}
		return buttonPanel;
	}

	@Override
	public final void enableDisableComponents() {
		switch (status) {
		case NONE:
			UIUtils.clearAllFields(formPanel);
			mainSoldTable.setEnabled(true);
			btnSave.setEnabled(true);
			break;

		case READ:
			UIUtils.clearAllFields(formPanel);
			mainSoldTable.clearSelection();
			mainSoldTable.setEnabled(true);
			break;

		default:
			break;
		}
	}

	@Override
	public void handleSaveAction() {

	}

	private JPanel getUpperFormPanel() {
		if (formPanel == null) {
			formPanel = new JPanel();

			formPanel.setBorder(new TitledBorder(null, "Sales Amount Collection", TitledBorder.LEADING,
					TitledBorder.TOP, null, null));
			formPanel.setBounds(10, 49, 474, 135);
			formPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("left:max(128dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("left:max(26dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(125dlu;default)"),
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC, },
					new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JLabel lblN = new JLabel("Category");
			formPanel.add(lblN, "4, 4");

			cmbProduct = new DataComboBox();
			formPanel.add(cmbProduct, "8, 4, fill, default");

			JLabel lblReceiver = new JLabel("Receiver :");
			formPanel.add(lblReceiver, "12, 4, default, center");

			JPanel receiverHolder = new JPanel();
			itemReceiverPanel = new ItemReceiverPanel();
			receiverHolder.add(itemReceiverPanel);
			formPanel.add(receiverHolder, "16, 4, fill, fill");

			lblFrom = new JLabel("From");
			formPanel.add(lblFrom, "4, 6");

			txtFromDate = new JDateChooser();
			formPanel.add(txtFromDate, "8, 6, fill, default");

			JLabel lblTo = new JLabel("To");
			formPanel.add(lblTo, "12, 6");

			txtToDate = new JDateChooser();
			txtToDate.setDate(new Date());
			formPanel.add(txtToDate, "16, 6, fill, default");

			btnSave = new JButton("Search");
			btnSave.addActionListener(e -> handleSearchQuery());

			formPanel.add(btnSave, "18, 8, default, bottom");

			JButton btnReset = new JButton("Reset");
			formPanel.add(btnReset, "20, 8, default, bottom");
			btnReset.addActionListener(e -> {
				UIUtils.clearAllFields(formPanel);
				cmbProduct.selectDefaultItem();
				itemReceiverPanel.clearAll();
			});
		}
		return formPanel;
	}

	private void handleSearchQuery() {
		readAndShowAll();
	}

	private void readAndShowAll() {
		try {
			List<SaleEntry> brsL;
			int returnStatus = -1;

			brsL = SaleServiceImpl.getSaleEntries(cmbProduct.getSelectedId(), itemReceiverPanel.getSelectedId(),
					txtFromDate.getDate(), txtToDate.getDate());

			if (brsL == null || brsL.size() == 0) {
				JOptionPane.showMessageDialog(null, "No Records Found");
				dataModel.resetModel();
				dataModel.fireTableDataChanged();
				mainSoldTable.adjustColumns();
				return;
			}
			showListInGrid(brsL);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	private void showListInGrid(List<SaleEntry> brsL) {
		dataModel.resetModel();
		int sn = 0;
		String sentTo;
		for (SaleEntry bo : brsL) {
			sentTo = bo.getCustomerInfo().getName() + "  " + bo.getCustomerInfo().getAddress();

			// TODO: add person/office name, specs in column
			dataModel.addRow(new Object[] { ++sn, ProductComboUtil.getProductDisplayName(bo.getPurchaseEntry()),
					DateTimeUtils.getCvDateMMMddyyyy(bo.getSoldDate()), sentTo, bo.getFullQuantity(),
					bo.getPartialQuantity(), bo.getTotalValue(), bo.getReceivedAmt(), bo.getPendingAmt(), bo.getId() });
		}
		mainSoldTable.setModel(dataModel);
		dataModel.fireTableDataChanged();
		mainSoldTable.adjustColumns();
	}

	@Override
	public final String getFunctionName() {
		return "Collection";
	}

	private JPanel getUpperSplitPane() {
		if (upperPane == null) {
			upperPane = new JPanel();
			upperPane.setLayout(new BorderLayout(0, 0));
			upperPane.add(getUpperFormPanel(), BorderLayout.CENTER);
			upperPane.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return upperPane;
	}

	private JPanel getLowerSplitPane() {
		if (lowerPane == null) {
			lowerPane = new JPanel();
			lowerPane.setLayout(new BorderLayout());
			dataModel = new EasyTableModel(header);

			mainSoldTable = new BetterJTable(dataModel);
			mainSoldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane sp = new JScrollPane(mainSoldTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

			lowerPane.add(sp, BorderLayout.CENTER);
		}
		return lowerPane;
	}

	private JPanel getAddToCartPane() {
		if (addToCartPanel == null) {
			addToCartPanel = new JPanel();
			addToCartPanel.setLayout(new BorderLayout());
			cartDataModel = new EasyTableModel(returnTblHeader);

			returnTable = new ReturnTable(cartDataModel);
			returnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			returnTable.setRowSorter(null);

			JScrollPane sp = new JScrollPane(returnTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			// TODO: number of rows into scrl pane
			addToCartPanel.add(sp, BorderLayout.CENTER);
		}
		return addToCartPanel;
	}

	static class CartTableQuantityCellEditor extends AbstractCellEditor implements TableCellEditor {
		final JComponent component = new JTextField();
		int maxQuantity;

		CartTableQuantityCellEditor(int maxQuantity) {
			this.maxQuantity = maxQuantity;
		}

		public final Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex,
				int vColIndex) {

			// Configure the component with the specified value
			((JTextField) component).setText(value.toString());

			// Return the configured component
			return component;
		}

		/**
		 * This method is called when editing is completed.<br>
		 * It must return the new value to be stored in the cell.
		 */
		public final Object getCellEditorValue() {
			int retQty;
			try {
				retQty = Integer.parseInt(((JTextField) component).getText());
				// if max
				if (retQty > maxQuantity) {
					JOptionPane.showMessageDialog(null, "The maximum qty remaining to return is " + maxQuantity,
							"Max Qty Exceed", JOptionPane.INFORMATION_MESSAGE);
					retQty = 0;
				}

			} catch (Exception e) {
				retQty = 0;
			}
			return retQty <= 0 ? "0" : retQty;
		}
	}

	class ReturnTable extends BetterJTable {

		/**
		 * ID at sec col, Qty at 6th
		 */

		ReturnTable(TableModel dm) {
			super(dm);
		}

		public final boolean isCellEditable(int row, int column) {
			return editableColumnIdx.contains(column);
		}

		// Determine editor to be used by row
		public final TableCellEditor getCellEditor(int row, int column) {
			return super.getCellEditor(row, column);
		}
	}

	private void populatePaymentDetailsInCartTable(int purchaseTechId) {

		cartDataModel.resetModel();
		int sn = 0;
		try {
			List<SaleCollectionInfo> paymentInfos = (List<SaleCollectionInfo>) DBUtils
					.getPaymentDetailsByPurchaseId(SaleCollectionInfo.class, purchaseTechId, "saleEntry");
			for (SaleCollectionInfo paymentInfo : paymentInfos) {
				cartDataModel.addRow(new Object[] { ++sn, DateTimeUtils.getCvDateMMMddyyyy(paymentInfo.getDate()),
						paymentInfo.getAmount(), paymentInfo.getNote(), purchaseTechId });
			}

			returnTable.setModel(cartDataModel);
			cartDataModel.fireTableDataChanged();

		} catch (Exception e) {
			System.out.println("populateSelectedRowInForm");
			handleDBError(e);
		}
	}
}
