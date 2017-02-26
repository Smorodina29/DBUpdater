package com.company.ui;

import javax.swing.*;
import java.awt.event.*;

public class MainWindow extends JDialog {
    private JPanel contentPane;
    private JButton addBtn;
    private JButton updateBtn;
    private JButton commitBtn;
    private JButton settingsBtn;

    public MainWindow() {
        this.setBounds(100,100,600,400);
        setContentPane(contentPane);
        setModal(true);
// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        MainWindow dialog = new MainWindow();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}