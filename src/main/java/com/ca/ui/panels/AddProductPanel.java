package com.ca.ui.panels;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.SystemUtils;

import com.ca.db.model.ProductInfo;
import com.ca.db.service.DBUtils;
import com.gt.common.constants.Status;
import com.gt.common.utils.StringUtils;
import com.gt.common.utils.UIUtils;
import com.gt.uilib.components.AbstractFunctionPanel;
import com.gt.uilib.components.table.BetterJTable;
import com.gt.uilib.components.table.EasyTableModel;
import com.gt.uilib.inputverifier.Validator;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class AddProductPanel extends AbstractFunctionPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// --
	private final String[] header = new String[] { "S.N.", "Product Name", "Product Type", "Weight", "TECH ID" };
	private JPanel formPanel = null;
	private JPanel buttonPanel;
	private Validator v;
	private JButton btnReadAll;
	private JButton btnNew;
	private JButton btnSave;
	private JPanel upperPane;
	private JPanel lowerPane;
	private BetterJTable table;
	private EasyTableModel dataModel;
	private int editingPrimaryId = 0;
	private JButton btnCancel;
	private JTextField productName;
	private JTextField productType;
	private JTextField weight;

	public AddProductPanel() {
		/*
		 * all gui components added from here;
		 */
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.1);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerSize(0);
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
				AddProductPanel panel = new AddProductPanel();
				jf.setBounds(panel.getBounds());
				jf.getContentPane().add(panel);
				jf.setVisible(true);
				jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private static boolean checkEntryOrder(List<String> strL) {
		boolean isEmptyFind = false;
		for (String st : strL) {
			if (!isEmptyFind && StringUtils.isEmpty(st)) {
				isEmptyFind = true;
			}
			if (isEmptyFind && !StringUtils.isEmpty(st)) {
				return false;
			}
		}
		return true;

	}

	@Override
	public final void init() {
		/* never forget to call super.init() */
		super.init();
		UIUtils.clearAllFields(upperPane);
		changeStatus(Status.NONE);
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			btnReadAll = new JButton("Read All");
			btnReadAll.addActionListener(e -> {
				readAndShowAll(true);
				changeStatus(Status.READ);
			});
			buttonPanel.add(btnReadAll);

			btnNew = new JButton("New");
			btnNew.addActionListener(e -> changeStatus(Status.CREATE));
			buttonPanel.add(btnNew);

			JButton btnDeleteThis = new JButton("Delete This");
			btnDeleteThis.addActionListener(e -> {
				if (editingPrimaryId > 0)
					handleDeleteAction();
			});

			JButton btnModify = new JButton("Modify");
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

	public final void changeStatusToCreate() {
		changeStatus(Status.CREATE);
		readAndShowAll(false);
	}

	private void handleDeleteAction() {
		if (status == Status.READ) {
			if (DataEntryUtils.confirmDBDelete())
				deleteSelectedBranchOffice();
		}

	}

	private void deleteSelectedBranchOffice() {
		try {
			DBUtils.deleteById(ProductInfo.class, editingPrimaryId);
			changeStatus(Status.READ);
			JOptionPane.showMessageDialog(null, "Deleted");
			readAndShowAll(false);
		} catch (Exception e) {
			handleDBError(e);
		}
	}

	@Override
	public final void enableDisableComponents() {
		switch (status) {
		case NONE:
			UIUtils.toggleAllChildren(buttonPanel, false);
			UIUtils.toggleAllChildren(formPanel, false);
			UIUtils.clearAllFields(formPanel);
			btnReadAll.setEnabled(true);
			btnNew.setEnabled(true);
			table.setEnabled(true);
			break;
		case CREATE:
			UIUtils.toggleAllChildren(buttonPanel, false);
			UIUtils.toggleAllChildren(formPanel, true);
			table.setEnabled(false);
			btnCancel.setEnabled(true);
			btnSave.setEnabled(true);
			break;
		case MODIFY:
			UIUtils.toggleAllChildren(formPanel, true);
			UIUtils.toggleAllChildren(buttonPanel, false);
			btnCancel.setEnabled(true);
			btnSave.setEnabled(true);
			table.setEnabled(false);

			break;

		case READ:
			UIUtils.toggleAllChildren(formPanel, false);
			UIUtils.toggleAllChildren(buttonPanel, true);
			UIUtils.clearAllFields(formPanel);
			table.clearSelection();
			table.setEnabled(true);
			editingPrimaryId = -1;
			btnCancel.setEnabled(false);
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
			if (DataEntryUtils.confirmDBUpdate())
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
		v.addTask(productName, "Name is Required", null, true);
		v.addTask(productType, "Type is Required", null, true);
		v.addTask(weight, "Weight is Required", null, true);

	}

	private ProductInfo getModelFromForm() {
		ProductInfo bo = new ProductInfo();
		bo.setName(productName.getText().trim());
		bo.setType(productType.getText().trim());
		bo.setWeight(Double.parseDouble(weight.getText().trim()));
		return bo;
	}

	private void setModelIntoForm(ProductInfo bro) {
		productName.setText(bro.getName());
		productType.setText(bro.getType());
		weight.setText(String.valueOf(bro.getWeight()));
	}

	private void save(boolean isModified) {
		initValidator();
		if (!v.validate()) {
			JOptionPane.showMessageDialog(null, "Feed the data properly");
			return;
		}

		try {
			ProductInfo newBo = getModelFromForm();
			if (isModified) {
				ProductInfo bo = (ProductInfo) DBUtils.getById(ProductInfo.class, editingPrimaryId);
				bo.setName(newBo.getName());
				bo.setType(newBo.getType());
				bo.setWeight(newBo.getWeight());
				DBUtils.saveOrUpdate(bo);
			} else {
				DBUtils.saveOrUpdate(newBo);
			}
			JOptionPane.showMessageDialog(null, "Saved Successfully");
			changeStatus(Status.READ);
			UIUtils.clearAllFields(upperPane);
			readAndShowAll(false);
		} catch (Exception e) {
			handleDBError(e);
		}
	}

	private JPanel getUpperFormPanel() {
		if (formPanel == null) {
			formPanel = new JPanel();

			formPanel.setBorder(
					new TitledBorder(null, "Product Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			formPanel.setBounds(10, 49, 474, 135);
			formPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("left:max(119dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("left:default"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
					new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(21dlu;default)"),
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JLabel lblRiceName = new JLabel("Product Name");
			formPanel.add(lblRiceName, "4, 2");

			productName = new JTextField();
			formPanel.add(productName, "8, 2, fill, default");
			productName.setColumns(10);

			JLabel lblRiceType = new JLabel("Product Type");
			formPanel.add(lblRiceType, "4, 4");

			productType = new JTextField();
			formPanel.add(productType, "8, 4, fill, default");
			productType.setColumns(10);

			JLabel lblWeight = new JLabel("Weight (KG)");
			formPanel.add(lblWeight, "4, 6");

			weight = new JTextField();
			formPanel.add(weight, "8, 6, fill, default");
			weight.setColumns(5);

			btnSave = new JButton("Save");
			btnSave.addActionListener(e -> {
				btnSave.setEnabled(false);
				handleSaveAction();
				btnSave.setEnabled(true);
			});
			formPanel.add(btnSave, "10, 24, right, default");
		}
		return formPanel;
	}

	private void readAndShowAll(boolean showSize0Error) {
		try {
			List<ProductInfo> brsL = DBUtils.readAll(ProductInfo.class);
			editingPrimaryId = -1;
			if (brsL == null || brsL.size() == 0) {
				if (showSize0Error) {
					JOptionPane.showMessageDialog(null, "No Records Found");
				}
			}
			showBranchOfficesInGrid(brsL);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}

	private void showBranchOfficesInGrid(List<ProductInfo> brsL) {
		dataModel.resetModel();
		int sn = 0;
		for (ProductInfo bo : brsL) {
			dataModel.addRow(new Object[] { ++sn, bo.getName(), bo.getType(), bo.getWeight(), bo.getId() });
		}
		// table.setTableHeader(tableHeader);
		table.setModel(dataModel);
		dataModel.fireTableDataChanged();
		table.adjustColumns();
		editingPrimaryId = -1;
	}

	@Override
	public final String getFunctionName() {
		return "New Item Category Add/View/Edit";
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
			table.getSelectionModel().addListSelectionListener(e -> {
				int selRow = table.getSelectedRow();
				if (selRow != -1) {
					/*
					 * if second column doesnot have primary id info, then
					 */
					int selectedId = (Integer) dataModel.getValueAt(selRow, 4);
					populateSelectedRowInForm(selectedId);
				}
			});
		}
		return lowerPane;
	}

	private void populateSelectedRowInForm(int selectedId) {
		try {
			ProductInfo bro = (ProductInfo) DBUtils.getById(ProductInfo.class, selectedId);
			if (bro != null) {
				setModelIntoForm(bro);
				editingPrimaryId = bro.getId();
			}
		} catch (Exception e) {
			handleDBError(e);
		}

	}
}
