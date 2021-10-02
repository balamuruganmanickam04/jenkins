package com.ca.ui.panels;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import com.ca.db.model.ChargeInfo;
import com.ca.db.service.DBUtils;
import com.gt.common.constants.Status;
import com.gt.common.utils.UIUtils;
import com.gt.uilib.components.AbstractFunctionPanel;
import com.gt.uilib.components.input.NumberTextField;
import com.gt.uilib.components.table.BetterJTable;
import com.gt.uilib.components.table.EasyTableModel;
import com.gt.uilib.inputverifier.Validator;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class AddOtherChargesPanel extends AbstractFunctionPanel {
	private final String[] header = new String[] { "S.N.", "Charge Category", "Value", "Applied to all product",
			"TECH ID" };
	private JPanel formPanel = null;
	private JPanel buttonPanel;
	private Validator v;
	private NumberTextField chargeCategory;
	private NumberTextField chargeValue;
	private JCheckBox appliedToAll;
	private JButton btnReadAll;
	private JButton btnNew;
	private JButton btnSave;
	private JPanel upperPane;
	private JPanel lowerPane;
	private BetterJTable table;
	private EasyTableModel dataModel;
	private int editingPrimaryId = 0;
	private JButton btnCancel;

	public AddOtherChargesPanel() {
		/*
		 * all gui components added from here;
		 */
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setResizeWeight(0.4);
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
				AddOtherChargesPanel panel = new AddOtherChargesPanel();
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
	}

	public final void changeStatusToCreate() {
		changeStatus(Status.CREATE);
		readAndShowAll(false);
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

	private void handleDeleteAction() {
		if (status == Status.READ) {
			deleteSelectedVendor();
		}

	}

	private void deleteSelectedVendor() {
		try {
			DBUtils.deleteById(ChargeInfo.class, editingPrimaryId);
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
		v.addTask(chargeCategory, "Req", null, true);
		v.addTask(chargeValue, "Req", null, true);
	}

	private ChargeInfo getModelFromForm() {
		ChargeInfo chargeInfo = new ChargeInfo();
		chargeInfo.setChageCategory(Double.parseDouble(chargeCategory.getText().trim()));
		chargeInfo.setChargeValue(Double.parseDouble(chargeValue.getText().trim()));
		chargeInfo.setAppliedToAll(Boolean.valueOf(appliedToAll.isSelected()));
		return chargeInfo;
	}

	private void setModelIntoForm(ChargeInfo chargeInfo) {
		chargeCategory.setText(chargeInfo.getChageCategory().toString());
		chargeValue.setText(chargeInfo.getChargeValue().toString());
		appliedToAll.setSelected(chargeInfo.getAppliedToAll());
	}

	private void save(boolean isModified) {
		initValidator();
		if (v.validate()) {
			try {

				ChargeInfo chargeInfo = getModelFromForm();
				if (isModified) {
					ChargeInfo bo = (ChargeInfo) DBUtils.getById(ChargeInfo.class, editingPrimaryId);
					bo.setChageCategory(chargeInfo.getChageCategory());
					bo.setChargeValue(chargeInfo.getChargeValue());
					bo.setAppliedToAll(chargeInfo.getAppliedToAll());
					DBUtils.saveOrUpdate(bo);
				} else {
					DBUtils.saveOrUpdate(chargeInfo);
				}
				JOptionPane.showMessageDialog(null, "Saved Successfully");
				changeStatus(Status.READ);
				UIUtils.clearAllFields(upperPane);
				readAndShowAll(false);
			} catch (Exception e) {
				handleDBError(e);
			}
		}
	}

	private JPanel getUpperFormPanel() {
		if (formPanel == null) {
			formPanel = new JPanel();

			formPanel.setBorder(
					new TitledBorder(null, "Selling Charge Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			formPanel.setBounds(10, 49, 474, 135);
			formPanel.setLayout(new FormLayout(
					new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
							FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
							FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:default"),
							FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:default"), },
					new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
							FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
							FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

			JLabel chargeCategorylbl = new JLabel("Holding Cost");
			formPanel.add(chargeCategorylbl, "4, 2");

			chargeCategory = new NumberTextField();
			formPanel.add(chargeCategory, "8, 2, fill, default");
			chargeCategory.setColumns(10);
			chargeCategory.setDecimalPlace(2);
			
			JLabel displayLbl = new JLabel("% value given here will be reduced in sale value");
			formPanel.add(displayLbl, "10, 2");

			JLabel chargeValuelbl = new JLabel("Shipping Charge per KG");
			formPanel.add(chargeValuelbl, "4, 4");

			chargeValue = new NumberTextField();
			formPanel.add(chargeValue, "8, 4, fill, default");
			chargeValue.setColumns(10);
			chargeValue.setDecimalPlace(2);

			JLabel appliedToAlllbl = new JLabel("Applied to all product");
			formPanel.add(appliedToAlllbl, "4, 6");

			appliedToAll = new JCheckBox();
			formPanel.add(appliedToAll, "8, 6, fill, default");

			btnSave = new JButton("Save");
			btnSave.addActionListener(e -> {
				btnSave.setEnabled(false);
				handleSaveAction();
				btnSave.setEnabled(true);
			});
			formPanel.add(btnSave, "10, 6");
		}
		return formPanel;
	}

	private void readAndShowAll(boolean showSize0Error) {
		try {
			List<ChargeInfo> brsL = DBUtils.readAll(ChargeInfo.class);
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

	private void showBranchOfficesInGrid(List<ChargeInfo> brsL) {
		dataModel.resetModel();
		int sn = 0;
		for (ChargeInfo bo : brsL) {
			dataModel.addRow(new Object[] { ++sn, bo.getChageCategory(), bo.getChargeValue(), bo.getAppliedToAll(),
					bo.getId() });
		}
		// table.setTableHeader(tableHeader);
		table.setModel(dataModel);
		dataModel.fireTableDataChanged();
		table.adjustColumns();
		editingPrimaryId = -1;
	}

	@Override
	public final String getFunctionName() {
		return "Charge Information";
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
			ChargeInfo bro = (ChargeInfo) DBUtils.getById(ChargeInfo.class, selectedId);
			if (bro != null) {
				setModelIntoForm(bro);
				editingPrimaryId = bro.getId();
			}
		} catch (Exception e) {
			handleDBError(e);
		}

	}

}
