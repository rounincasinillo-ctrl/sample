import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class Main extends JFrame {

    private ArrayList<Billable> billableList = new ArrayList<>();
    private HashMap<String, MeterReading> readingMap = new HashMap<>();

    private JComboBox<String> cmbType;
    private JTextField txtName, txtAddress;
    private JLabel lblTotalCount;
    private DefaultTableModel consumerTableModel;
    private JTable consumerTable;

    private JTextField txtFacName, txtAgency;
    private JLabel lblFacilityCount;
    private DefaultTableModel facilityTableModel;
    private JTable facilityTable;

    private JTextField txtPrevious, txtCurrent, txtReadingDate;
    private DefaultTableModel billingTableModel;
    private JTable billingTable;

    public Main() {
        setTitle("Electric Cooperative Billing System");
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());          // Layout Manager
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("1. Consumers", createConsumerTab());
        tabbedPane.addTab("2. Public Facilities", createFacilityTab());
        tabbedPane.addTab("3. Billing", createBillingTab());
        add(tabbedPane, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        Main.this,
                        "Are you sure you want to exit?",
                        "Exit",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice==JOptionPane.YES_OPTION){
                    dispose();
                }
                super.windowClosing(e);
            }
        });
        try {
            billableList = DatabaseHelper.loadAll();
            readingMap = DatabaseHelper.loadLatestReadings();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not load data: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshTables();
    }
    private JPanel createConsumerTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Register New Consumer"));

        formPanel.add(new JLabel("Account Type: "));
        cmbType = new JComboBox<>(new String[]{"Residential", "Commercial"});
        formPanel.add(cmbType);
        formPanel.add(new JLabel("Full Name: "));
        txtName = new JTextField();
        formPanel.add(txtName);
        formPanel.add(new JLabel("Address: "));
        txtAddress = new JTextField();
        formPanel.add(txtAddress);
        lblTotalCount = new JLabel("Total Consumers: 0");
        formPanel.add(lblTotalCount);
        formPanel.add(new JLabel(""));
        mainPanel.add(formPanel, BorderLayout.NORTH);


        String[] columns = {"Consumer ID", "Name", "Address", "Type", "Rate per kWh"};
        consumerTableModel = new DefaultTableModel(columns, 0);
        consumerTable = new JTable(consumerTableModel);
        mainPanel.add(new JScrollPane(consumerTable), BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd = new JButton("Add Consumer");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = txtName.getText();
                    String address = txtAddress.getText();

                    Billable newItem;
                    if (cmbType.getSelectedIndex() == 0) {
                        newItem = new Residential(name, address);
                    } else {
                        newItem = new Commercial(name, address);
                    }
                    DatabaseHelper.save(newItem);

                    billableList.add(newItem);
                    refreshTables();
                    txtName.setText("");
                    txtAddress.setText("");

                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Consumer added! \nID: " + getIdOf(newItem),
                            "Success", JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            ex.getMessage(),
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Could not save: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Billable selected = getSelected(consumerTable);
                if (selected == null) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Select a consumer row from the table first.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                Consumer old = (Consumer) selected;
                String oldName = old.getConsumerName();
                String oldAddress = old.getConsumerAddress();

                JTextField fldName = new JTextField(oldName);
                JTextField fldAddress = new JTextField(oldAddress);
                JComboBox<String> fldType = new JComboBox<>(new String[]{"Residential", "Commercial"});
                fldType.setSelectedIndex(old instanceof Commercial ? 1 : 0);

                JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
                panel.add(new JLabel("Type: "));
                panel.add(fldType);
                panel.add(new JLabel("Name: "));
                panel.add(fldName);
                panel.add(new JLabel("Address: "));
                panel.add(fldAddress);

                int result = JOptionPane.showConfirmDialog(
                        Main.this, panel, "Update " + old.getConsumerId(),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    Billable updated = selected;
                    try {
                        String newName = fldName.getText();
                        String newAddress = fldAddress.getText();
                        boolean toCommercial = fldType.getSelectedIndex() == 1;

                        if (toCommercial != (old instanceof Commercial)) {
                            updated = toCommercial
                                    ? new Commercial(old.getConsumerId(), newName, newAddress)
                                    : new Residential(old.getConsumerId(), newName, newAddress);
                        } else {
                            applyEdit(selected, newName, newAddress);
                        }
                        DatabaseHelper.updateDetail(updated);
                        billableList.set(billableList.indexOf(selected), updated);
                        refreshTables();
                        JOptionPane.showMessageDialog(
                                Main.this,
                                "Consumer updated!",
                                "Updated",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (IllegalArgumentException ex) {
                        if (updated == selected) applyEdit(selected, oldName, oldAddress);   // undo
                        JOptionPane.showMessageDialog(
                                Main.this,
                                ex.getMessage(),
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE
                        );
                    } catch (SQLException ex) {
                        if (updated == selected) applyEdit(selected, oldName, oldAddress);   // undo
                        JOptionPane.showMessageDialog(
                                Main.this,
                                "Could not update: " + ex.getMessage(),
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });


        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected(consumerTable);
            }
        });


        return mainPanel;
    }


    // =====================================================
    //  TAB 2: PUBLIC FACILITIES
    // =====================================================
    private JPanel createFacilityTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Register New Public Facility"));
        formPanel.add(new JLabel("Facility Name: "));
        txtFacName = new JTextField();
        formPanel.add(txtFacName);
        formPanel.add(new JLabel("Agency: "));
        txtAgency = new JTextField();
        formPanel.add(txtAgency);
        lblFacilityCount = new JLabel("Total Facilities: 0");
        formPanel.add(lblFacilityCount);
        formPanel.add(new JLabel(""));
        mainPanel.add(formPanel, BorderLayout.NORTH);


        String[] columns = {"Facility ID", "Name", "Agency", "Rate per kWh"};
        facilityTableModel = new DefaultTableModel(columns, 0);
        facilityTable = new JTable(facilityTableModel);
        mainPanel.add(new JScrollPane(facilityTable), BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd = new JButton("Add Facility");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Selected");
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);


        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PublicFacility newItem = new PublicFacility(txtFacName.getText(), txtAgency.getText());


                    DatabaseHelper.save(newItem);


                    billableList.add(newItem);
                    refreshTables();
                    txtFacName.setText("");
                    txtAgency.setText("");


                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Facility added! \nID: " + newItem.getFacilityId(),
                            "Success", JOptionPane.INFORMATION_MESSAGE
                    );


                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            ex.getMessage(),
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Could not save: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });


        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Billable selected = getSelected(facilityTable);
                if (selected == null) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Select a facility row from the table first.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                PublicFacility f = (PublicFacility) selected;
                String oldName = f.getFacilityName();
                String oldAgency = f.getAgency();


                JTextField fldName = new JTextField(oldName);
                JTextField fldAgency = new JTextField(oldAgency);


                JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
                panel.add(new JLabel("Name: "));
                panel.add(fldName);
                panel.add(new JLabel("Agency: "));
                panel.add(fldAgency);


                int result = JOptionPane.showConfirmDialog(
                        Main.this, panel, "Update " + f.getFacilityId(),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                );


                if (result == JOptionPane.OK_OPTION) {
                    try {
                        applyEdit(f, fldName.getText(), fldAgency.getText());
                        DatabaseHelper.updateDetail(f);
                        refreshTables();
                        JOptionPane.showMessageDialog(
                                Main.this,
                                "Facility updated!",
                                "Updated",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (IllegalArgumentException ex) {
                        applyEdit(f, oldName, oldAgency);   // undo
                        JOptionPane.showMessageDialog(
                                Main.this,
                                ex.getMessage(),
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE
                        );
                    } catch (SQLException ex) {
                        applyEdit(f, oldName, oldAgency);   // undo
                        JOptionPane.showMessageDialog(
                                Main.this,
                                "Could not update: " + ex.getMessage(),
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });


        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected(facilityTable);
            }
        });


        return mainPanel;
    }


    // =====================================================
    //  TAB 3: BILLING
    // =====================================================
    private JPanel createBillingTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));


        JPanel formPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                "Enter Meter Reading (Select an account row in the table first)."
        ));


        formPanel.add(new JLabel("Previous Reading : "));
        txtPrevious = new JTextField();
        formPanel.add(txtPrevious);


        formPanel.add(new JLabel("Current Reading : "));
        txtCurrent = new JTextField();
        formPanel.add(txtCurrent);


        formPanel.add(new JLabel("Reading Date (YYYY-MM-DD)"));
        txtReadingDate = new JTextField(LocalDate.now().toString());
        formPanel.add(txtReadingDate);
        mainPanel.add(formPanel, BorderLayout.NORTH);


        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        String[] columns = {"ID", "Name", "Type", "Previous Reading ", "Current Reading", "Actual Consumption", "Reading Date", "Total Bill"};
        billingTableModel = new DefaultTableModel(columns, 0);
        billingTable = new JTable(billingTableModel);
        centerPanel.add(new JScrollPane(billingTable), BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnCompute = new JButton("Compute Bill");
        buttonPanel.add(btnCompute);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);


        btnCompute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Billable selected = getSelected(billingTable);
                if (selected == null) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Select an account row from the table first.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                try {
                    double previous = Double.parseDouble(txtPrevious.getText().trim());
                    double current = Double.parseDouble(txtCurrent.getText().trim());
                    String date = txtReadingDate.getText().trim();
                    LocalDate.parse(date);   // must be a real date in YYYY-MM-DD
                    MeterReading reading = new MeterReading(previous, current, date);
                    double bill = selected.calculateBill(reading);

                    DatabaseHelper.saveReading(selected, reading, bill);   // save to MySQL
                    readingMap.put(getIdOf(selected), reading);
                    refreshBillingTable();


                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Account: " + getNameOf(selected) + "(" + selected.getBillableType() + ")\n" + reading.toString() + String.format("%nAmount due: PHP %.2f", bill),
                            "Bill Computed",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    txtPrevious.setText("");
                    txtCurrent.setText("");
                    txtReadingDate.setText(LocalDate.now().toString());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Please type valid numbers for both readings",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            ex.getMessage(),
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Reading date must be a real date in YYYY-MM-DD format.",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Could not save the reading: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }


            }
        });


        return mainPanel;
    }
    //  HELPER
    // Finds the account for the selected table row (matches by ID in column 0)
    private Billable getSelected(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        String id = String.valueOf(table.getValueAt(row, 0));
        for (Billable b : billableList) {
            if (getIdOf(b).equals(id)) return b;
        }
        return null;
    }

    private void deleteSelected(JTable table) {
        Billable selected = getSelected(table);
        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a row to delete.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete " + getNameOf(selected) + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseHelper.delete(selected);
                readingMap.remove(getIdOf(selected));
                billableList.remove(selected);
                refreshTables();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not delete: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }


    private void applyEdit(Billable b, String name, String detail) {
        if (b instanceof PublicFacility) {
            PublicFacility f = (PublicFacility) b;
            f.setFacilityName(name);
            f.setAgency(detail);
        } else {
            Consumer c = (Consumer) b;
            c.setConsumerName(name);
            c.setConsumerAddress(detail);
        }
    }

    private void refreshTables() {
        refreshConsumerTable();
        refreshFacilityTable();
        refreshBillingTable();
    }

    private void refreshConsumerTable() {
        consumerTableModel.setRowCount(0);
        int count = 0;
        for (Billable b : billableList) {
            if (b instanceof Consumer) {
                Object[] row = {
                        getIdOf(b),
                        getNameOf(b),
                        getDetailOf(b),
                        b.getBillableType(),
                        "PHP " + b.getRatePerKwh()
                };
                consumerTableModel.addRow(row);
                count++;
            }
        }
        lblTotalCount.setText("Total Consumers: " + count);
    }


    private void refreshFacilityTable() {
        facilityTableModel.setRowCount(0);
        int count = 0;
        for (Billable b : billableList) {
            if (b instanceof PublicFacility) {
                Object[] row = {
                        getIdOf(b),
                        getNameOf(b),
                        getDetailOf(b),
                        "PHP " + b.getRatePerKwh()
                };
                facilityTableModel.addRow(row);
                count++;
            }
        }
        lblFacilityCount.setText("Total Facilities: " + count);
    }


    private void refreshBillingTable() {
        billingTableModel.setRowCount(0);
        for (Billable b : billableList) {
            MeterReading r = readingMap.get(getIdOf(b));
            Object[] row;
            if (r == null) {
                row = new Object[]{getIdOf(b), getNameOf(b), b.getBillableType(), "-", "-", "-", "-", "-"};
            } else {
                row = new Object[]{getIdOf(b), getNameOf(b), b.getBillableType(),
                        r.getPreviousReading(), r.getCurrentReading(), r.getConsumption(),
                        r.getReadingDate(),
                        String.format("PHP %.2f", b.calculateBill(r))};
            }
            billingTableModel.addRow(row);
        }
    }


    private String getIdOf(Billable b) {
        if (b instanceof Consumer) {
            return ((Consumer) b).getConsumerId();
        } else {
            return ((PublicFacility) b).getFacilityId();
        }
    }


    private String getNameOf(Billable b) {
        if (b instanceof Consumer) {
            return ((Consumer) b).getConsumerName();
        } else {
            return ((PublicFacility) b).getFacilityName();
        }
    }


    private String getDetailOf(Billable b) {     // address o agency
        if (b instanceof Consumer) {
            return ((Consumer) b).getConsumerAddress();
        } else {
            return ((PublicFacility) b).getAgency();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}

