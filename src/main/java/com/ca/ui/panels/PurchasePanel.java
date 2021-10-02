package com.ca.ui.panels;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.SystemUtils;

import com.ca.db.model.PaymentStatus;
import com.ca.db.model.ProductInfo;
import com.ca.db.model.PurchaseEntry;
import com.ca.db.model.PurchasePaymentInfo;
import com.ca.db.model.StockBalanceInfo;
import com.ca.db.model.Vendor;
import com.ca.db.service.DBUtils;
import com.ca.db.service.ItemServiceImpl;
import com.ca.db.service.PurchasePaymentServiceImpl;
import com.ca.db.service.SaleServiceImpl;
import com.gt.common.constants.Status;
import com.gt.common.exception.QuantityException;
import com.gt.common.utils.DateTimeUtils;
import com.gt.common.utils.ProductComboUtil;
import com.gt.common.utils.StringUtils;
import com.gt.common.utils.UIUtils;
import com.gt.uilib.components.AbstractFunctionPanel;
import com.gt.uilib.components.input.DataComboBox;
import com.gt.uilib.components.input.NumberTextField;
import com.gt.uilib.components.table.BetterJTable;
import com.gt.uilib.components.table.EasyTableModel;
import com.gt.uilib.inputverifier.Validator;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.toedter.calendar.JDateChooser;

public class PurchasePanel extends AbstractFunctionPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String[] header = new String[] { "S.No", "Purchase Date", "Vendor Name", "Product", "Qty",
			"Product price (Per Qty)", "Other charges", "Total Purchase Value", "TECH ID" };
	private final String[] returnTblHeader = new String[] { "S.No", "Payment Date", "Paid Amt", "Note", "TECH ID" };
	private JPanel formPanel = null;
	private final List<Integer> editableColumnIdx = new ArrayList<>();
	private JPanel buttonPanel;
	private ReturnTable returnTable;
	private Validator v;
	private JButton btnReadAll;
	private JButton btnNew;
	private JButton btnSave;
	private JPanel upperPane;
	private JPanel lowerPane;
	private JPanel paymentDetailPanel;
	private BetterJTable table;
	private EasyTableModel dataModel;
	private int editingPrimaryId = 0;
	private JButton btnCancel;
	private DataComboBox cmbCategory;
	private DataComboBox cmbVendor;
	private DataComboBox cmbPaymentStatus;
	private JDateChooser purchaseDate;
	private NumberTextField quantity;
	private NumberTextField ratePerQty;
	private NumberTextField otherCharges;
	private NumberTextField totalValue;
	private NumberTextField totalPaidAmount;
	private NumberTextField PendingAmount;
	private EasyTableModel paymentDetailsModel;
	private JSplitPane lowerPanel;
	private JPanel addToCartPanel;
	private JButton paidButton;

	private final KeyListener priceCalcListener = new KeyListener() {

		public void keyPressed(KeyEvent e) {
			totalValue.setText(getPrice());
			PendingAmount.setText(getPendingAmount());
		}

		public void keyTyped(KeyEvent e) {
			totalValue.setText(getPrice());
			PendingAmount.setText(getPendingAmount());
		}

		public void keyReleased(KeyEvent e) {
			totalValue.setText(getPrice());
			PendingAmount.setText(getPendingAmount());
		}

	};

	public PurchasePanel() {
		/*
		 * all gui components added from here;
		 */
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.0);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);
		splitPane.setLeftComponent(getUpperSplitPane());
		splitPane.setRightComponent(getPaymentDetailsPanel());
		/*
		 * never forget to call after setting up UI
		 */
		v = new Validator(mainApp, true);
		init();
	}

	public static void main(String[] args) throws Exception {
		if (SystemUtils.IS_OS_WINDOWS) {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		EventQueue.invokeLater(() -> {
			try {
				JFrame jf = new JFrame();
				PurchasePanel panel = new PurchasePanel();
				jf.setBounds(panel.getBounds());
				jf.getContentPane().add(panel);
				jf.setVisible(true);
				jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public final void init() {
		/* never forget to call super.init() */
		super.init();
		UIUtils.clearAllFields(upperPane);
		changeStatus(Status.NONE);
		intCombo();
		totalValue.setText("0");
		quantity.setText("0");
		ratePerQty.setText("0");
		otherCharges.setText("0");
		editableColumnIdx.add(1);
		editableColumnIdx.add(2);
		editableColumnIdx.add(3);
	}

	private void intCombo() {
		try {
			initCmbCategory();

			initCmbVendor();

			initCmbPaymentStatus();

		} catch (Exception e) {
			handleDBError(e);
		}

	}

	private void initCmbCategory() throws Exception {
		/* Category Combo */
		ProductComboUtil.addProductDetails(cmbCategory);
	}

	private void initCmbVendor() throws Exception {
		/* Vendor Combo */
		cmbVendor.init();
		List<Vendor> vl = DBUtils.readAll(Vendor.class);
		for (Vendor v : vl) {
			cmbVendor.addRow(new Object[] { v.getId(), v.getName(), v.getAddress() });
		}
	}

	private void initCmbPaymentStatus() throws Exception {
		/* payment Combo */
		cmbPaymentStatus.init();

		for (PaymentStatus status : PaymentStatus.getEnums()) {
			cmbPaymentStatus.addRow(new Object[] { status.getId(), status.getValue() });
		}
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			btnReadAll = new JButton("Search");
			btnReadAll.addActionListener(e -> {
				readAndShowAll(true);
				changeStatus(Status.READ);
			});
			buttonPanel.add(btnReadAll);

			btnNew = new JButton("New Purchase");
			btnNew.addActionListener(e -> changeStatus(Status.CREATE));
			buttonPanel.add(btnNew);

			JButton btnDeleteThis = new JButton("Delete Purchase");
			btnDeleteThis.addActionListener(e -> {
				if (editingPrimaryId > 0)
					handleDeleteAction();
			});

			JButton btnModify = new JButton("Modify Purchase");
			btnModify.addActionListener(e -> {
				if (editingPrimaryId > 0)
					changeStatus(Status.MODIFY);
			});
			buttonPanel.add(btnModify);
			buttonPanel.add(btnDeleteThis);

			btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(e -> changeStatus(Status.READ));
			buttonPanel.add(btnCancel);
		}
		return buttonPanel;
	}

	private void handleDeleteAction() {
		if (status == Status.READ) {
			if (DataEntryUtils.confirmDBDelete())
				deleteSelectedItem();
		}

	}

	private void deleteSelectedItem() {
		try {
			if (SaleServiceImpl.isProductStartedSelling(editingPrimaryId)) {
				JOptionPane.showMessageDialog(null, "Selected Product is already started selling, You cannot delete!!!");
				return;
			}
			PurchasePaymentServiceImpl.deletePurchase(editingPrimaryId);
			changeStatus(Status.READ);
			JOptionPane.showMessageDialog(null, "Deleted");
			readAndShowAll(false);
		} catch (Exception e) {
			System.out.println("deleteSelectedBranchOffice");
			handleDBError(e);
		}
	}

	@Override
	public final void enableDisableComponents() {
		v.resetErrors();
		switch (status) {
		case NONE:
			UIUtils.toggleAllChildren(buttonPanel, false);
			UIUtils.toggleAllChildren(formPanel, false);
			UIUtils.clearAllFields(formPanel);
			btnReadAll.setEnabled(true);
			btnNew.setEnabled(true);
			table.setEnabled(true);
			purchaseDate.getDateEditor().setEnabled(false);
			break;
		case CREATE:
			UIUtils.toggleAllChildren(buttonPanel, false);
			UIUtils.toggleAllChildren(formPanel, true);
			purchaseDate.setDate(new Date());
			table.setEnabled(false);
			btnCancel.setEnabled(true);
			cmbPaymentStatus.setEnabled(false);
			btnSave.setEnabled(true);
			purchaseDate.getDateEditor().setEnabled(false);
			break;
		case MODIFY:
			UIUtils.toggleAllChildren(formPanel, true);
			UIUtils.toggleAllChildren(buttonPanel, false);
			cmbCategory.setEnabled(false);
			cmbVendor.setEnabled(false);
			cmbPaymentStatus.setEnabled(false);

			btnCancel.setEnabled(true);
			btnSave.setEnabled(true);
			table.setEnabled(false);
			purchaseDate.getDateEditor().setEnabled(false);
			break;

		case READ:
			UIUtils.toggleAllChildren(formPanel, false);
			UIUtils.toggleAllChildren(buttonPanel, true);
			UIUtils.clearAllFields(formPanel);
			table.clearSelection();
			table.setEnabled(true);
			editingPrimaryId = -1;

			intCombo();
			break;

		default:
			break;
		}
	}

	@Override
	public final void handleSaveAction() {
		switch (status) {
		case CREATE:
			// create new

			save(false);
			break;
		case MODIFY:
			// modify
			save(true);
			break;

		default:
			break;
		}
	}

	private void initValidator() {

		if (v != null) {
			v.resetErrors();
		}
		v = new Validator(mainApp, true);
		// TODO: confirm the fields to validate
		v.addTask(cmbCategory, "required", null, true);
		v.addTask(cmbVendor, "required", null, true);
		v.addTask(purchaseDate, "required", null, true, true);
		v.addTask(quantity, "required", null, true);
		v.addTask(ratePerQty, "required", null, true);

	}

	/**
	 * current date not added to object ( for the case of modified data)
	 *
	 * @return
	 */
	private PurchaseEntry getModelFromForm() {
		PurchaseEntry purchaseEntry = new PurchaseEntry();
		try {
			purchaseEntry.setProduct((ProductInfo) DBUtils.getById(ProductInfo.class, cmbCategory.getSelectedId()));
			purchaseEntry.setVendor((Vendor) DBUtils.getById(Vendor.class, cmbVendor.getSelectedId()));
		} catch (Exception e) {
			e.printStackTrace();
			handleDBError(e);
		}
		purchaseEntry.setQuantity(Integer.parseInt(quantity.getText().trim()));
		purchaseEntry.setRate(new BigDecimal(ratePerQty.getText().trim()));
		purchaseEntry.setPurchaseDate(purchaseDate.getDate());
		purchaseEntry.setOtherCharge(new BigDecimal(otherCharges.getText().trim()));
		purchaseEntry.setTotalValue(purchaseEntry.getRate().multiply(new BigDecimal(purchaseEntry.getQuantity()))
				.add(purchaseEntry.getOtherCharge()));
		purchaseEntry.setPaidAmt(new BigDecimal(0));
		purchaseEntry.setPendingAmt(purchaseEntry.getRate().multiply(new BigDecimal(purchaseEntry.getQuantity()))
				.add(purchaseEntry.getOtherCharge()));
		purchaseEntry.setPaymentStatus(PaymentStatus.NOT_PAID);

		return purchaseEntry;
	}

	private void setModelIntoForm(PurchaseEntry purchaseEntry) {
		cmbVendor.selectItem(purchaseEntry.getVendor().getId());
		cmbCategory.selectItem(purchaseEntry.getProduct().getId());
		purchaseDate.setDate(purchaseEntry.getPurchaseDate());
		quantity.setText(purchaseEntry.getQuantity() + "");
		ratePerQty.setText(purchaseEntry.getRate().toString());
		totalValue.setText(purchaseEntry.getTotalValue().toString());
		otherCharges.setText(purchaseEntry.getOtherCharge().toString());
		totalPaidAmount.setText(purchaseEntry.getPaidAmt().toString());
		PendingAmount.setText(purchaseEntry.getPendingAmt().toString());
		cmbPaymentStatus.selectItem(purchaseEntry.getPaymentStatus().getId());
	}

	private void save(boolean isModified) {
		initValidator();

		if (!v.validate()) {
			JOptionPane.showMessageDialog(null, "Feed the data properly");
			return;
		}

		if (v.validate()) {

			if (!isModified) {
				if (!DataEntryUtils.confirmDBSave()) {
					return;
				}
			} else {
				if (!DataEntryUtils.confirmDBUpdate()) {
					return;
				}
			}
			try {

				PurchaseEntry newBo = getModelFromForm();
				if (isModified) {
					if (SaleServiceImpl.isProductStartedSelling(editingPrimaryId)) {
						JOptionPane.showMessageDialog(null, "Selected Product is already started selling, You cannot modify!!!");
						return;
					}

					PurchaseEntry bo = (PurchaseEntry) DBUtils.getById(PurchaseEntry.class, editingPrimaryId);
					System.out.println("is MODIFIED..........");
					bo.setRate(newBo.getRate());
					bo.setQuantity(newBo.getQuantity());
					bo.setPurchaseDate(newBo.getPurchaseDate());
					bo.setOtherCharge(newBo.getOtherCharge());
					bo.setTotalValue(newBo.getTotalValue());
					bo.setPaymentStatus(newBo.getPaymentStatus());
					DBUtils.saveOrUpdate(bo);
					PurchasePaymentServiceImpl.modifyStockQty(editingPrimaryId, newBo.getQuantity());
				} else {
					// save new
					StockBalanceInfo balanceInfo = new StockBalanceInfo();
					balanceInfo.setPurchaseEntry(newBo);
					balanceInfo.setTotalQuantity(newBo.getQuantity());
					balanceInfo.setAvailableFullQuantity(newBo.getQuantity());
					balanceInfo.setAvailablePartialQuantity(0);
					balanceInfo.setSoldFullQuantity(0);
					balanceInfo.setSoldPartialQuantity(0);
					DBUtils.saveOrUpdate(newBo);
					DBUtils.saveOrUpdate(balanceInfo);

				}
				JOptionPane.showMessageDialog(null, "Saved Successfully");
				changeStatus(Status.READ);
				UIUtils.clearAllFields(upperPane);
				readAndShowAll(false);
			} catch (QuantityException e) {
				System.out.println("modify purchase--" + e.getMessage());
				handleQuantityException(e);
			} catch (Exception e) {
				System.out.println("save--" + e.getMessage());
				handleDBError(e);
			}
		}
	}

	private JPanel getUpperFormPanel() {
		if (formPanel == null) {
			formPanel = new JPanel();

			formPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "New purchase order",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
			formPanel.setBounds(10, 49, 474, 135);
			formPanel.setLayout(new FormLayout(
					new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(90dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(137dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(49dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(56dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(137dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(29dlu;default)"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(26dlu;default):grow"), },
					new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(11dlu;default)"),
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:max(15dlu;default)"),
							FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(55dlu;default)"),
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JLabel lblCategory = new JLabel("Product");
			formPanel.add(lblCategory, "4, 4, default, top");

			cmbCategory = new DataComboBox();
			formPanel.add(cmbCategory, "8, 4, fill, default");

			JLabel lblQuantity = new JLabel("Quantity");
			formPanel.add(lblQuantity, "12, 4");

			quantity = new NumberTextField(6, true);
			quantity.addKeyListener(priceCalcListener);
			formPanel.add(quantity, "16, 4, fill, default");

			JLabel lblPurchaseDate = new JLabel("Purchase Date");
			formPanel.add(lblPurchaseDate, "4, 6");

			purchaseDate = new JDateChooser();
			purchaseDate.setEnabled(false);
			formPanel.add(purchaseDate, "8, 6, fill, default");

			JLabel lblOtherCharges = new JLabel("Other Charges");
			formPanel.add(lblOtherCharges, "12, 6");

			otherCharges = new NumberTextField(true);
			otherCharges.setDecimalPlace(2);
			otherCharges.addKeyListener(priceCalcListener);
			formPanel.add(otherCharges, "16, 6, fill, default");

			JLabel lblPhoneNumber = new JLabel("Vendor");
			formPanel.add(lblPhoneNumber, "4, 8");

			cmbVendor = new DataComboBox();
			formPanel.add(cmbVendor, "8, 8, fill, default");

			JLabel lblRate = new JLabel("Rate per Qty");
			formPanel.add(lblRate, "12, 8");

			ratePerQty = new NumberTextField(true);
			ratePerQty.setDecimalPlace(2);
			ratePerQty.addKeyListener(priceCalcListener);
			formPanel.add(ratePerQty, "16, 8, fill, default");

			JLabel paymentStatusLbl = new JLabel("Payment Status");
			formPanel.add(paymentStatusLbl, "4, 10");

			cmbPaymentStatus = new DataComboBox();
			cmbPaymentStatus.setEnabled(false);
			formPanel.add(cmbPaymentStatus, "8, 10, fill, default");

			JLabel lblTotal = new JLabel("Total Purchase Value");
			formPanel.add(lblTotal, "12, 10");

			totalValue = new NumberTextField();
			totalValue.setEditable(false);
			totalValue.setFocusable(false);
			totalValue.setDecimalPlace(2);
			formPanel.add(totalValue, "16, 10, fill, default");

			JLabel lblTotalPaidAmt = new JLabel("Total Paid Amt");
			formPanel.add(lblTotalPaidAmt, "4, 12");

			totalPaidAmount = new NumberTextField();
			totalPaidAmount.setEditable(false);
			totalPaidAmount.setFocusable(false);
			totalPaidAmount.setDecimalPlace(2);
			formPanel.add(totalPaidAmount, "8, 12, fill, default");

			JLabel lblTotalPendingAmt = new JLabel("Pending Amt");
			formPanel.add(lblTotalPendingAmt, "12, 12");

			PendingAmount = new NumberTextField();
			PendingAmount.setEditable(false);
			PendingAmount.setFocusable(false);
			PendingAmount.setDecimalPlace(2);
			formPanel.add(PendingAmount, "16, 12, fill, default");

			btnSave = new JButton("Save");
			btnSave.addActionListener(e -> {
				btnSave.setEnabled(false);
				SwingWorker worker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() {
						handleSaveAction();
						return null;
					}

				};
				worker.addPropertyChangeListener(evt -> {
					if ("DONE".equals(evt.getNewValue().toString())) {
						btnSave.setEnabled(true);
					}
				});

				worker.execute();
			});
			formPanel.add(btnSave, "16, 14");

		}

		return formPanel;
	}

	private JSplitPane getPaymentDetailsPanel() {
		if (lowerPanel == null) {
			lowerPanel = new JSplitPane();
			lowerPanel.setContinuousLayout(true);
			lowerPanel.setResizeWeight(0.8);
			lowerPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
			lowerPanel.setLeftComponent(getLowerSplitPane());
			JPanel panel_1 = new JPanel();
			lowerPanel.setRightComponent(panel_1);
			panel_1.setLayout(new BorderLayout(0, 0));
			paymentDetailPanel = new JPanel();
			paymentDetailPanel.setBorder(
					new TitledBorder(null, "Payment Details", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel_1.add(paymentDetailPanel, BorderLayout.CENTER);
			paymentDetailPanel.setLayout(new FormLayout(
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
			paymentDetailPanel.add(panel_3, "2, 2, fill, fill");
			panel_3.setLayout(
					new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
							new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
									FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JButton btnAddItem = new JButton("Add payment");
			panel_3.add(btnAddItem, "2, 2");

			JButton btnDelete = new JButton("Remove payment");
			btnDelete.addActionListener(e -> {
				if (returnTable.getRowCount() > 0) {
					int selRow = returnTable.getSelectedRow();
					if (selRow != -1) {
						/*
						 * if second column doesnot have primary id info, then
						 */

						int selectedId = (Integer) paymentDetailsModel.getValueAt(selRow, 4);
						System.out.println("Selected ID : " + selectedId + "_  >>  row " + selRow);
						removeSelectedRowInCartTable(selectedId, selRow);

					}
				}
			});
			panel_3.add(btnDelete, "2, 4");
			btnAddItem.addActionListener(e -> {
				if (table.getRowCount() > 0) {
					int selRow = table.getSelectedRow();
					if (selRow != -1) {

						int selectedId = (Integer) dataModel.getValueAt(selRow, 8);
						addSelectedRowInCartTable(selectedId);
					}
				}
			});

			paymentDetailPanel.add(getAddToCartPane(), "4, 2, 13, 1, fill, top");

			paidButton = new JButton("Paid");
			paidButton.addActionListener(e -> {

//				if (!isValidCart()) {
//					JOptionPane.showMessageDialog(null, "Please fill the required data");
//					return;
//				}
				paidButton.setEnabled(false);
				SwingWorker worker = new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() {
						if (DataEntryUtils.confirmDBSave())
							savePaymentDetails();
						return null;
					}

				};
				worker.addPropertyChangeListener(evt -> {
					System.out
							.println("Event " + evt + " name" + evt.getPropertyName() + " value " + evt.getNewValue());
					if ("DONE".equals(evt.getNewValue().toString())) {
						paidButton.setEnabled(true);
					}
				});

				worker.execute();

			});
			paymentDetailPanel.add(paidButton, "16, 4, default, bottom");

		}
		return lowerPanel;
	}

	private void addSelectedRowInCartTable(int selectedId) {
		try {
			int sn = paymentDetailsModel.getRowCount();
			String pattern = "dd-MM-yyyy";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			String date = simpleDateFormat.format(new Date());
			paymentDetailsModel.addRow(new Object[] { ++sn, date + "", "", "", selectedId });
			returnTable.setModel(paymentDetailsModel);
			paymentDetailsModel.fireTableDataChanged();

		} catch (Exception e) {
			System.out.println("populateSelectedRowInForm");
			handleDBError(e);
		}
	}

	private void populatePaymentDetailsInCartTable(int purchaseTechId) {

		paymentDetailsModel.resetModel();
		int sn = 0;
		try {
			List<PurchasePaymentInfo> paymentInfos = (List<PurchasePaymentInfo>) DBUtils
					.getPaymentDetailsByPurchaseId(PurchasePaymentInfo.class, purchaseTechId, "purchaseEntry");
			for (PurchasePaymentInfo paymentInfo : paymentInfos) {
				paymentDetailsModel.addRow(new Object[] { ++sn, DateTimeUtils.getCvDateMMMddyyyy(paymentInfo.getDate()),
						paymentInfo.getAmount(), paymentInfo.getNote(), purchaseTechId });
			}

			returnTable.setModel(paymentDetailsModel);
			paymentDetailsModel.fireTableDataChanged();

		} catch (Exception e) {
			System.out.println("populateSelectedRowInForm");
			handleDBError(e);
		}
	}

	private void removeSelectedRowInCartTable(int selectedId, int selRow) {
		paymentDetailsModel.removeRowWithKey(selRow);
		paymentDetailsModel.fireTableDataChanged();
//		cellQtyEditors.remove(selRow);
	}

	private JPanel getAddToCartPane() {
		if (addToCartPanel == null) {
			addToCartPanel = new JPanel();
			addToCartPanel.setLayout(new BorderLayout());
			paymentDetailsModel = new EasyTableModel(returnTblHeader);

			returnTable = new ReturnTable(paymentDetailsModel);
			returnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			returnTable.setRowSorter(null);

			JScrollPane sp = new JScrollPane(returnTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			// TODO: number of rows into scrl pane
			addToCartPanel.add(sp, BorderLayout.CENTER);
		}
		return addToCartPanel;
	}

	private void readAndShowAll(boolean showSize0Error) {
		List<PurchaseEntry> brsL = ItemServiceImpl.getAddedItems();
		editingPrimaryId = -1;
		if (brsL == null || brsL.size() == 0) {
			if (showSize0Error) {
				JOptionPane.showMessageDialog(null, "No Records Found");
			}
		}
		showListInGrid(brsL);

	}

	private String getPrice() {
		BigDecimal rate = new BigDecimal("0");
		BigDecimal qty = new BigDecimal("0");
		BigDecimal transportCharges = new BigDecimal("0");
		BigDecimal OtherCharges = new BigDecimal("0");
		if (!StringUtils.isEmpty(ratePerQty.getText())) {
			rate = new BigDecimal(ratePerQty.getText().trim());
		}
		if (!StringUtils.isEmpty(quantity.getText())) {
			qty = new BigDecimal(quantity.getText().trim());
		}

		if (!StringUtils.isEmpty(otherCharges.getText())) {
			OtherCharges = new BigDecimal(otherCharges.getText().trim());
		}
		System.out.println("Rate " + ratePerQty.getText() + " Qty " + quantity.getText());
		System.out.println("Rate " + rate + " Qty " + qty);
		BigDecimal amt = rate.multiply(qty).add(transportCharges).add(OtherCharges);
		return amt + "";
	}

	private String getPendingAmount() {
		BigDecimal totalAmt = new BigDecimal("0");
		BigDecimal paidAmt = new BigDecimal("0");
		if (!StringUtils.isEmpty(totalValue.getText())) {
			totalAmt = new BigDecimal(totalValue.getText().trim());
		}

		if (!StringUtils.isEmpty(totalPaidAmount.getText())) {
			paidAmt = new BigDecimal(totalPaidAmount.getText().trim());
		}
		BigDecimal amt = totalAmt.subtract(paidAmt);
		return amt + "";
	}

	private void showListInGrid(List<PurchaseEntry> brsL) {
		dataModel.resetModel();
		int sn = 0;
		for (PurchaseEntry bo : brsL) {

			dataModel.addRow(new Object[] { ++sn, DateTimeUtils.getCvDateMMMddyyyy(bo.getPurchaseDate()),
					bo.getVendor().getName(), ProductComboUtil.getProductDisplayName(bo), bo.getQuantity(),
					bo.getRate(), bo.getOtherCharge(), bo.getTotalValue(), bo.getId() });
		}
		table.setModel(dataModel);
		paymentDetailsModel.resetModel();
		dataModel.fireTableDataChanged();
		table.adjustColumns();
		editingPrimaryId = -1;
	}

	@Override
	public final String getFunctionName() {
		return "Purchase";
	}

	private JPanel getUpperSplitPane() {
		if (upperPane == null) {
			upperPane = new JPanel();
			upperPane.setLayout(new BorderLayout(0, 2));
			upperPane.add(getUpperFormPanel(), BorderLayout.CENTER);
//			upperPane.add(getPaymentDetailsPanel(), BorderLayout.EAST);
			upperPane.add(getButtonPanel(), BorderLayout.SOUTH);
		}
		return upperPane;
	}

	private JPanel getLowerSplitPane() {
		if (lowerPane == null) {
			lowerPane = new JPanel();
			lowerPane.setLayout(new BorderLayout());
			dataModel = new EasyTableModel(header);

			table = new BetterJTable(dataModel);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane sp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

			lowerPane.add(sp, BorderLayout.CENTER);
			table.getSelectionModel().addListSelectionListener(e -> {
				int selRow = table.getSelectedRow();
				if (selRow != -1) {
					/*
					 * if second column doesnot have primary id info, then
					 */
					int selectedId = (Integer) dataModel.getValueAt(selRow, 8);
					populateSelectedRowInForm(selectedId);
				}
			});
		}
		return lowerPane;
	}

	private void savePaymentDetails() {
		try {
			PurchasePaymentServiceImpl.savePaymentDetails(getPurchaseTechId(), getPaymentDetailsFromTable());
			handleTransferSuccess();
			PurchaseEntry purchaseEntry = (PurchaseEntry) DBUtils.getById(PurchaseEntry.class, getPurchaseTechId());
			totalPaidAmount.setText(purchaseEntry.getPaidAmt().toString());
			PendingAmount.setText(purchaseEntry.getPendingAmt().toString());
			cmbPaymentStatus.selectItem(purchaseEntry.getPaymentStatus().getId());
		} catch (Exception er) {
			handleDBError(er);
		}
	}

	public int getPurchaseTechId() {
		int rows = paymentDetailsModel.getRowCount();
		for (int i = 0; i < rows;) {
			return Integer.parseInt(paymentDetailsModel.getValueAt(i, 4).toString());

		}
		return 0;
	}

	public List<PurchasePaymentInfo> getPaymentDetailsFromTable() throws ParseException {
		int rows = paymentDetailsModel.getRowCount();
		List<PurchasePaymentInfo> infos = new ArrayList<>();
		PurchasePaymentInfo paymentInfo = null;
		for (int i = 0; i < rows; i++) {
			paymentInfo = new PurchasePaymentInfo();
			BigDecimal amount = new BigDecimal(paymentDetailsModel.getValueAt(i, 2).toString());
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
			String dateInString = paymentDetailsModel.getValueAt(i, 1).toString();
			Date date = formatter.parse(dateInString);
			String note = paymentDetailsModel.getValueAt(i, 3).toString();
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
//			paymentDetailsModel.resetModel();
			paymentDetailsModel.fireTableDataChanged();
//			dataModel.resetModel();
			dataModel.fireTableDataChanged();

		});
	}

	private void populateSelectedRowInForm(int selectedId) {
		try {
			PurchaseEntry bro = (PurchaseEntry) DBUtils.getById(PurchaseEntry.class, selectedId);

			if (bro != null) {
				setModelIntoForm(bro);
				populatePaymentDetailsInCartTable(selectedId);
				editingPrimaryId = bro.getId();
			}
		} catch (Exception e) {
			System.out.println("populateSelectedRowInForm");
			handleDBError(e);
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

	}

}
