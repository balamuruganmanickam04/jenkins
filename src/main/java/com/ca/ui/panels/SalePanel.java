package com.ca.ui.panels;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellEditor;

import org.apache.commons.lang3.SystemUtils;

import com.ca.db.model.StockBalanceInfo;
import com.ca.db.model.Vendor;
import com.ca.db.service.DBUtils;
import com.ca.db.service.SaleServiceImpl;
import com.ca.db.service.StockSearchServiceImpl;
import com.ca.db.service.dto.SaleItemDTO;
import com.gt.common.constants.Status;
import com.gt.common.exception.QuantityException;
import com.gt.common.utils.DateTimeUtils;
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

public class SalePanel extends AbstractFunctionPanel {
	private final int qtyCol = 6;
	private final String[] header = new String[] { "", "Product", "Vendor Name", "Purchase date", "Total Quantity",
			"Available Full Quantity", "Available loose quanity(KG)", "Price Per KG(Inclusive all charges)",
			"TECH ID" };
	private final String[] cartHeader = new String[] { "", "Rice Category", "Full Quantity", "Loose Qty(KG)",
			"Price Per KG", "TECH ID" };
	private final List<TableCellEditor> cellEditors = new ArrayList<>();
	private JPanel formPanel = null;
	private JButton btnSave;
	private JPanel upperPane;
	private JPanel lowerPane;
	private BetterJTable itemDetailTable;
	private BetterJTable cartDetailTable;
//	private CartTable cartTable;
	private EasyTableModel dataModel;
	private EasyTableModel cartDataModel;
	private DataComboBox cmbCategory;
	private DataComboBox cmbVendor;
	private JTextField txtItemname;
	private JSplitPane lowerPanel;
	private JPanel cartPanel;
	private ItemReceiverPanel itemReceiverPanel;
	private JDateChooser transferDateChooser;
	private JButton btnSend;
	private JPanel addToCartPanel;
	private JTextField invoiceNumber;
	private JDateChooser txtFromDate;
	private JDateChooser txtToDate;

	public SalePanel() {
		/*
		 * all gui components added from here;
		 */
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(false);
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
				SalePanel panel = new SalePanel();
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
			lowerPanel.setResizeWeight(0.5);
			lowerPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);

			lowerPanel.setLeftComponent(getLowerSplitPane());

			JPanel panel_1 = new JPanel();
			lowerPanel.setRightComponent(panel_1);
			panel_1.setLayout(new BorderLayout(0, 0));

			cartPanel = new JPanel();
			cartPanel.setBorder(new TitledBorder(null,
					"Add sellling information",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
							FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:max(23dlu;default)"),
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JPanel panel_3 = new JPanel();
			cartPanel.add(panel_3, "2, 2, fill, fill");
			panel_3.setLayout(
					new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
							new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
									FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JButton btnAddItem = new JButton("Add Product");
			panel_3.add(btnAddItem, "2, 2");

			JButton btnDelete = new JButton("Remove Product");
			btnDelete.addActionListener(e -> {
				if (cartDetailTable.getRowCount() > 0) {
					int selRow = cartDetailTable.getSelectedRow();
					if (selRow != -1) {
						/*
						 * if second column doesnot have primary id info, then
						 */

						int selectedId = (Integer) cartDataModel.getValueAt(selRow, 3);
						System.out.println("Selected ID : " + selectedId + "_  >>  row " + selRow);
						if (cartDataModel.containsKey(3, selectedId)) {
							removeSelectedRowInCartTable(selectedId, selRow);
						}

					}
				}
			});
			panel_3.add(btnDelete, "2, 4");
			btnAddItem.addActionListener(e -> {
				if (itemDetailTable.getRowCount() > 0) {
					int selRow = itemDetailTable.getSelectedRow();
					if (selRow != -1) {
						/*
						 * if second column doesnot have primary id info, then
						 */

						Integer selectedId = (Integer) dataModel.getValueAt(selRow, 8);

						if (!cartDataModel.containsKey(3, selectedId)) {
							addSelectedRowInCartTable(selectedId);
						} else {
							JOptionPane.showMessageDialog(null, "This Item Already Selected", "Duplicate Selection",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});

			cartPanel.add(getAddToCartPane(), "4, 2, 13, 1, fill, top");

			JLabel lblReceiver = new JLabel("SALE TO:");
			cartPanel.add(lblReceiver, "4, 4");

			itemReceiverPanel = new ItemReceiverPanel();
			cartPanel.add(itemReceiverPanel, "6, 4, fill, top");

			JLabel lblSentDate = new JLabel("Date");
			cartPanel.add(lblSentDate, "10, 4, default, bottom");

			transferDateChooser = new JDateChooser();
			transferDateChooser.setDate(new Date());
			cartPanel.add(transferDateChooser, "14, 4, fill, bottom");

			btnSend = new JButton("Sale");
			btnSend.addActionListener(e -> {

				if (!isValidCart()) {
					JOptionPane.showMessageDialog(null, "Please fill the required data");
					return;
				}
				btnSend.setEnabled(false);
				SwingWorker worker = new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() {
						if (DataEntryUtils.confirmDBSave())
							saveTransfer();
						return null;
					}

				};
				worker.addPropertyChangeListener(evt -> {
					System.out
							.println("Event " + evt + " name" + evt.getPropertyName() + " value " + evt.getNewValue());
					if ("DONE".equals(evt.getNewValue().toString())) {
						btnSend.setEnabled(true);
					}
				});

				worker.execute();

			});
			cartPanel.add(btnSend, "16, 4, default, bottom");

			JLabel lblRequestNumber = new JLabel("Invoice Number");
			cartPanel.add(lblRequestNumber, "4, 8, left, default");

			invoiceNumber = new JTextField();
			invoiceNumber.setEditable(false);
			cartPanel.add(invoiceNumber, "6, 8, fill, default");
			invoiceNumber.setColumns(10);
		}

		return lowerPanel;
	}

	private void saveTransfer() {
		try {
			SaleServiceImpl.saveTransfer(getIdAndSaleItemDtoMap(), transferDateChooser.getDate(),
					itemReceiverPanel.getCurrentType(), itemReceiverPanel.getSelectedId(),
					invoiceNumber.getText().trim());

			handleTransferSuccess();
		} catch (QuantityException er) {
			handleQuantityException(er);
		} catch (Exception er) {
			handleDBError(er);
		}
	}

	private void removeSelectedRowInCartTable(int selectedId, int selRow) {
		cartDataModel.removeRowWithKey(selRow);
		cartDataModel.fireTableDataChanged();
		// TODO:
		cellEditors.remove(selRow);
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
			cellEditors.clear();

		});

	}

	private void addSelectedRowInCartTable(int selectedId) {
		try {
			StockBalanceInfo bo = (StockBalanceInfo) DBUtils.getById(StockBalanceInfo.class, selectedId);
			System.out.println("Adding to cart id = " + selectedId + ">>" + bo.getTotalQuantity());
			int sn = cartDataModel.getRowCount();

			cartDataModel.addRow(new Object[] { ++sn, ProductComboUtil.getProductDisplayName(bo.getPurchaseEntry()),
					bo.getAvailableFullQuantity(), bo.getAvailablePartialQuantity(), "", bo.getId() });
			cartDataModel.fireTableDataChanged();

			SwingUtilities.invokeLater(() -> {
				cartDetailTable.setModel(cartDataModel);
				cartDetailTable.adjustColumns();
			});

		} catch (Exception e) {
			System.out.println("populateSelectedRowInForm" + e.getMessage());
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
	}

	private void intCombo() {
		try {
			/* Category Combo */

			ProductComboUtil.addProductDetails(cmbCategory);
			/* Vendor Combo */
			cmbVendor.init();
			List<Vendor> vl = DBUtils.readAll(Vendor.class);
			for (Vendor v : vl) {
				cmbVendor.addRow(new Object[] { v.getId(), v.getName(), v.getAddress() });
			}

		} catch (Exception e) {
			handleDBError(e);
		}

	}

	@Override
	public final void enableDisableComponents() {
		switch (status) {
		case NONE:
			UIUtils.clearAllFields(formPanel);
			itemDetailTable.setEnabled(true);
			cartDetailTable.setEnabled(true);
			btnSave.setEnabled(true);
			break;

		case READ:
			UIUtils.clearAllFields(formPanel);
			itemDetailTable.clearSelection();
			itemDetailTable.setEnabled(true);
			cartDetailTable.setEnabled(true);
			break;

		default:
			break;
		}
	}

	@Override
	public void handleSaveAction() {

	}

	private boolean isValidCart() {
		if (isValidCartQty() && cartDetailTable.getRowCount() > 0 && itemReceiverPanel.isSelected()
				&& transferDateChooser.getDate() != null) {
			return true;
		}
		return false;
	}

	private JPanel getUpperFormPanel() {
		if (formPanel == null) {
			formPanel = new JPanel();

			formPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
					"Search stock to select product and quantity to sell", TitledBorder.LEADING, TitledBorder.TOP, null,
					null));
			formPanel.setBounds(10, 49, 474, 135);
			formPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("left:max(115dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("left:default"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("max(125dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
					new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(51dlu;default)"),
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JLabel lblN = new JLabel("Rice Category");
			formPanel.add(lblN, "4, 4");

			cmbCategory = new DataComboBox();
			formPanel.add(cmbCategory, "8, 4, fill, default");

			JLabel lblVendor = new JLabel("Vendor");
			formPanel.add(lblVendor, "12, 4");

			cmbVendor = new DataComboBox();
			formPanel.add(cmbVendor, "16, 4, fill, default");

			JLabel lblFrom = new JLabel("From");
			formPanel.add(lblFrom, "4, 6");

			txtFromDate = new JDateChooser();
			formPanel.add(txtFromDate, "8, 6, fill, default");

			JLabel lblTo = new JLabel("To");
			formPanel.add(lblTo, "12, 6");

			txtToDate = new JDateChooser();
			formPanel.add(txtToDate, "16, 6, fill, default");

			btnSave = new JButton("Search");
			btnSave.addActionListener(e -> handleSearchQuery());

			formPanel.add(btnSave, "18, 8, left, default");

			JButton btnReset = new JButton("Reset");
			btnReset.addActionListener(e -> {
				UIUtils.clearAllFields(formPanel);
				cmbCategory.selectDefaultItem();
				cmbVendor.selectDefaultItem();
			});
			formPanel.add(btnReset, "20, 8");

		}
		return formPanel;
	}

	private void handleSearchQuery() {
		readAndShowAll();
	}

	private void readAndShowAll() {
		try {
			List<StockBalanceInfo> brsL;
			brsL = StockSearchServiceImpl.StockSearchQuery(cmbCategory.getSelectedId(), cmbVendor.getSelectedId(),
					txtFromDate.getDate(), txtToDate.getDate());

			if (brsL == null || brsL.size() == 0) {
				JOptionPane.showMessageDialog(null, "No Records Found");
				dataModel.resetModel();
				dataModel.fireTableDataChanged();
				itemDetailTable.adjustColumns();
				return;
			}
			showListInGrid(brsL);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	private void showListInGrid(List<StockBalanceInfo> brsL) {
		dataModel.resetModel();
		int sn = 0;
		for (StockBalanceInfo bo : brsL) {

			double pricePerKg = bo.getPurchaseEntry().getTotalValue().doubleValue()
					/ (bo.getPurchaseEntry().getProduct().getWeight() * bo.getPurchaseEntry().getQuantity());
			dataModel.addRow(new Object[] { ++sn, ProductComboUtil.getProductDisplayName(bo.getPurchaseEntry()),
					bo.getPurchaseEntry().getVendor().getName(),
					DateTimeUtils.getCvDateMMMddyyyy(bo.getPurchaseEntry().getPurchaseDate()), bo.getTotalQuantity(),
					bo.getAvailableFullQuantity(), bo.getAvailablePartialQuantity(), pricePerKg, bo.getId() });
		}
		SwingUtilities.invokeLater(() -> {
			itemDetailTable.setModel(dataModel);
			itemDetailTable.adjustColumns();
		});
	}

	@Override
	public final String getFunctionName() {
		return "Sale";
	}

	private JPanel getUpperSplitPane() {
		if (upperPane == null) {
			upperPane = new JPanel();
			GridBagLayout gbl_upperPane = new GridBagLayout();
			gbl_upperPane.columnWidths = new int[] { 728, 0 };
			gbl_upperPane.rowHeights = new int[] { 194, 0 };
			gbl_upperPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
			gbl_upperPane.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			upperPane.setLayout(gbl_upperPane);
			GridBagConstraints gbc_formPanel = new GridBagConstraints();
			gbc_formPanel.anchor = GridBagConstraints.NORTH;
			gbc_formPanel.fill = GridBagConstraints.HORIZONTAL;
			gbc_formPanel.gridx = 0;
			gbc_formPanel.gridy = 0;
			upperPane.add(getUpperFormPanel(), gbc_formPanel);
		}
		return upperPane;
	}

	private JPanel getLowerSplitPane() {
		if (lowerPane == null) {
			lowerPane = new JPanel();
			lowerPane.setLayout(new BorderLayout());
			dataModel = new EasyTableModel(header);

			itemDetailTable = new BetterJTable(dataModel);
			itemDetailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane sp = new JScrollPane(itemDetailTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

			lowerPane.add(sp, BorderLayout.CENTER);
		}
		return lowerPane;
	}

	private JPanel getAddToCartPane() {
		if (addToCartPanel == null) {
			addToCartPanel = new JPanel();
			addToCartPanel.setLayout(new BorderLayout());
			cartDataModel = new EasyTableModel(cartHeader);
			cartDetailTable = new BetterJTable(cartDataModel);
			/*
			 * cartTable = new CartTable(cartDataModel);
			 * cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			 * cartTable.setRowSorter(null);
			 */

			JScrollPane sp = new JScrollPane(cartDetailTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			// TODO: number of rows into scrl pane
			addToCartPanel.add(sp, BorderLayout.WEST);
		}
		return addToCartPanel;
	}

	public boolean isValidCartQty() {
		Map<Integer, SaleItemDTO> cartMap = getIdAndSaleItemDtoMap();
		for (Entry<Integer, SaleItemDTO> entry : cartMap.entrySet()) {
			SaleItemDTO dto = entry.getValue();

			if (dto.getFullqty() > 0 || dto.getPartialqty() > 0) {
				return true;
			}
		}
		return false;

	}

	public Map<Integer, SaleItemDTO> getIdAndSaleItemDtoMap() {
		Map<Integer, SaleItemDTO> cartIdQtyMap = new HashMap<>();
		int rows = cartDetailTable.getRowCount();
		for (int i = 0; i < rows; i++) {

			Integer id = Integer.parseInt(cartDataModel.getValueAt(i, 5).toString());
			int fullQty = Integer.parseInt(cartDataModel.getValueAt(i, 2).toString());
			int partialQty = Integer.parseInt(cartDataModel.getValueAt(i, 3).toString());
			double pricePerKG = Double.parseDouble(cartDataModel.getValueAt(i, 4).toString());

			cartIdQtyMap.put(id, new SaleItemDTO(fullQty, partialQty, pricePerKG));
		}
		return cartIdQtyMap;
	}
}
