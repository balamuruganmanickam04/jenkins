package com.ca.ui.panels;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.SystemUtils;

import com.ca.db.model.PurchaseEntry;
import com.ca.db.model.StockBalanceInfo;
import com.ca.db.model.Vendor;
import com.ca.db.service.DBUtils;
import com.ca.db.service.ItemServiceImpl;
import com.ca.db.service.StockSearchServiceImpl;
import com.gt.common.constants.Status;
import com.gt.common.utils.DateTimeUtils;
import com.gt.common.utils.ExcelUtils;
import com.gt.common.utils.ProductComboUtil;
import com.gt.common.utils.UIUtils;
import com.gt.uilib.components.AbstractFunctionPanel;
import com.gt.uilib.components.input.DataComboBox;
import com.gt.uilib.components.table.BetterJTable;
import com.gt.uilib.components.table.EasyTableModel;
import com.gt.uilib.inputverifier.Validator;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.toedter.calendar.JDateChooser;

public class StockQueryPanel extends AbstractFunctionPanel {
	private final String[] header = new String[] { "S.N.", "Purchase date", "Product", "Vendor", "Total Quantity",
			"Available Full Quantity", "Available loose Quantity" };
	private JPanel formPanel = null;
	private JPanel buttonPanel;
	private Validator v;
	private JDateChooser txtFromDate;
	private JDateChooser txtToDate;
	private JButton btnSearch;
	private JPanel upperPane;
	private JPanel lowerPane;
	private BetterJTable table;
	private EasyTableModel dataModel;
	private DataComboBox cmbCategory;
	private DataComboBox cmbVendor;
	private JPanel specPanelHolder;

	public StockQueryPanel() {
		/*
		 * all gui components added from here;
		 */
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.1);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);
		splitPane.setLeftComponent(getUpperSplitPane());
		splitPane.setRightComponent(getLowerSplitPane());
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
				StockQueryPanel panel = new StockQueryPanel();
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

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();

			JButton btnSaveToExcel = new JButton("Save to Excel");
			btnSaveToExcel.addActionListener(e -> {
				JFileChooser jf = new JFileChooser();
				jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jf.showDialog(StockQueryPanel.this, "Select Save location");
				String fileName = jf.getSelectedFile().getAbsolutePath();
				try {
					ExcelUtils.writeExcelFromJTable(table, fileName + ".xls");
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
			table.setEnabled(true);
			btnSearch.setEnabled(true);
			break;

		case READ:
			UIUtils.clearAllFields(formPanel);
			table.clearSelection();
			table.setEnabled(true);
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

			formPanel.setBorder(
					new TitledBorder(null, "Stock Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			formPanel.setBounds(10, 49, 474, 135);
			formPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("left:max(115dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("left:default"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("max(118dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"), },
					new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(51dlu;default)"),
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JLabel lblN = new JLabel("Product");
			formPanel.add(lblN, "4, 4");

			cmbCategory = new DataComboBox();
			formPanel.add(cmbCategory, "8, 4, fill, default");

			JLabel lblVendor = new JLabel("Vendor");
			formPanel.add(lblVendor, "12, 4");

			cmbVendor = new DataComboBox();
			formPanel.add(cmbVendor, "16, 4, fill, default");

//			specPanelHolder = new JPanel();
//			formPanel.add(specPanelHolder, "4, 6, 21, 1, fill, fill");
//			specPanelHolder.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

			JLabel lblFrom = new JLabel("Purchase Date From");
			formPanel.add(lblFrom, "4, 6");

			txtFromDate = new JDateChooser();
			formPanel.add(txtFromDate, "8, 6, fill, default");

			JLabel lblTo = new JLabel("Purchase Date To");
			formPanel.add(lblTo, "12, 6");

			txtToDate = new JDateChooser();
			txtToDate.setDate(new Date());
			formPanel.add(txtToDate, "16, 6, fill, default");

			btnSearch = new JButton("Search");
			btnSearch.addActionListener(e -> handleSearchQuery());

			formPanel.add(btnSearch, "18, 12");

			JButton btnReset = new JButton("Reset");
			btnReset.addActionListener(e -> {
				UIUtils.clearAllFields(formPanel);
				cmbCategory.selectDefaultItem();
				cmbVendor.selectDefaultItem();
				// itemReceiverPanel.clearAll();
			});
			formPanel.add(btnReset, "20, 12");
		}
		return formPanel;
	}

	private void handleSearchQuery() {
		readAndShowAll();
	}

	private void readAndShowAll() {
		try {
			ItemServiceImpl is = new ItemServiceImpl();
			List<StockBalanceInfo> brsL;
			List<String> specs;
			brsL = StockSearchServiceImpl.StockSearchQuery(cmbCategory.getSelectedId(), cmbVendor.getSelectedId(),
					txtFromDate.getDate(), txtToDate.getDate());

			if (brsL == null || brsL.size() == 0) {
				JOptionPane.showMessageDialog(null, "No Records Found");
				dataModel.resetModel();
				dataModel.fireTableDataChanged();
				table.adjustColumns();
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

			dataModel.addRow(
					new Object[] { ++sn, DateTimeUtils.getCvDateMMMddyyyy(bo.getPurchaseEntry().getPurchaseDate()),
							ProductComboUtil.getProductDisplayName(bo.getPurchaseEntry()),
							bo.getPurchaseEntry().getVendor().getName(), bo.getTotalQuantity(),
							bo.getAvailableFullQuantity(), bo.getAvailablePartialQuantity() });
		}
		table.setModel(dataModel);
		dataModel.fireTableDataChanged();
		table.adjustColumns();
	}

	@Override
	public final String getFunctionName() {
		return "Stock Query";
	}

	private JPanel getUpperSplitPane() {
		if (upperPane == null) {
			upperPane = new JPanel();
			upperPane.setLayout(new BorderLayout(0, 0));
			upperPane.add(getUpperFormPanel(), BorderLayout.WEST);
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
		}
		return lowerPane;
	}

}
