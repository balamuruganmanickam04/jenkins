package com.ca.ui.panels;

import com.ca.db.model.LoginUser;
import com.ca.db.service.LoginUserServiceImpl;
import com.gt.common.ResourceManager;
import com.gt.common.constants.StrConstants;
import com.gt.common.utils.UIUtils;
import com.gt.uilib.components.AbstractFunctionPanel;
import com.gt.uilib.components.AppFrame;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends AbstractFunctionPanel {
	private JPanel innerPanel;
	private JTextField userName;
	private JTextField passWord;

	public LoginPanel() {
		add(getLoginPanel());
		init();
	}

	public static void main(String[] args) throws Exception {
		if (SystemUtils.IS_OS_WINDOWS) {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		EventQueue.invokeLater(() -> {
			try {
				JFrame jf = new JFrame();
				LoginPanel panel = new LoginPanel();
				jf.setBounds(panel.getBounds());
				jf.getContentPane().add(panel);
				jf.setVisible(true);
				jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private JPanel getLoginPanel() {
		JPanel fullPanel = new JPanel();
		fullPanel.setAlignmentX(Component.ALLBITS);
		innerPanel = new JPanel();
		innerPanel.setBounds(11, 66, 436, 244);
		fullPanel.add(innerPanel);
		innerPanel.setLayout(new FormLayout(
				new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(18dlu;default)"),
						FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(21dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(61dlu;default):grow"), FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(65dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(65dlu;default)"), },
				new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(57dlu;default):grow"),
						FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
						RowSpec.decode("max(20dlu;default)"), FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
						RowSpec.decode("max(16dlu;default)"), FormFactory.RELATED_GAP_ROWSPEC,
						RowSpec.decode("max(16dlu;default)"), FormFactory.RELATED_GAP_ROWSPEC,
						RowSpec.decode("max(14dlu;default)"), FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

		JLabel lblNewLabel = new JLabel("UserName :");
		innerPanel.add(lblNewLabel, "4, 18, left, default");

		userName = new JTextField();
		innerPanel.add(userName, "8, 18, 3, 1, fill, fill");
		userName.addActionListener(e -> doLogin());

		JLabel lblPassword = new JLabel("Password :");
		innerPanel.add(lblPassword, "4, 20, left, default");

		passWord = new JPasswordField();
		passWord.addActionListener(e -> doLogin());

		innerPanel.add(passWord, "8, 20, 3, 1, fill, fill");

		JButton loginButton = new JButton("Login");
		innerPanel.add(loginButton, "8, 24, fill, default");
		loginButton.addActionListener(e -> doLogin());
		JButton restPassword = new JButton("Reset");
		restPassword.addActionListener(e -> clearAll());
		innerPanel.add(restPassword, "10, 24");
		
		JLabel lblImg = new JLabel("");
		lblImg.setIcon(ResourceManager.getImageIcon("logo2.png"));
		fullPanel.add(lblImg, "2, 8, 11, 17");
		
		return fullPanel;
	}

	private void clearAll() {
		UIUtils.clearAllFields(innerPanel);

	}

	private void doLogin() {
		LoginUserServiceImpl lus;
		try {
			lus = new LoginUserServiceImpl();
			LoginUser user = LoginUserServiceImpl.getLoginUser(userName.getText().trim(), passWord.getText().trim());
			if (user != null) {
				AppFrame.loginSuccess();
			} else {
				JOptionPane.showMessageDialog(null, "Username password error");
				passWord.setText("");
				userName.requestFocus();
			}

		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "DB Connection Error");
		}

	}

	@Override
	public final String getFunctionName() {
		return "Login";
	}

	@Override
	public void handleSaveAction() {

	}

	@Override
	public final void init() {
		super.init();
		userName.requestFocus();
		isReadyToClose = true;
	}

	@Override
	public void enableDisableComponents() {

	}

}