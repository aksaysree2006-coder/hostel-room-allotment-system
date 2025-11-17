import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class HostelRoomAllotment extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// JDBC connection details
    private static final String URL = "jdbc:mysql://localhost:3306/hostel_db";
    private static final String USER = "root"; // change if needed
    private static final String PASSWORD = "aksay@4560"; // add your MySQL password if any

    private Connection conn;
    private PreparedStatement pst;
    private ResultSet rs;

    // Swing components
    private JTextField tfName, tfRegNo, tfRoomNo, tfSearch;
    private JComboBox<String> cbBranch;
    private JTable table;
    private DefaultTableModel model;

    public HostelRoomAllotment() {
        setTitle("🏨 Hostel Room Allotment System");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));

        tfName = new JTextField();
        tfRegNo = new JTextField();
        tfRoomNo = new JTextField();
        cbBranch = new JComboBox<>(new String[]{"CSE", "ECE", "EEE", "MECH", "CIVIL", "IT"});
        tfSearch = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(tfName);
        formPanel.add(new JLabel("Reg No:"));
        formPanel.add(tfRegNo);
        formPanel.add(new JLabel("Branch:"));
        formPanel.add(cbBranch);
        formPanel.add(new JLabel("Room No:"));
        formPanel.add(tfRoomNo);

        add(formPanel, BorderLayout.NORTH);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnAllot = new JButton("Allot");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnClear = new JButton("Clear");

        buttonPanel.add(btnAllot);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClear);

        add(buttonPanel, BorderLayout.SOUTH);

        // --- Table Panel ---
        model = new DefaultTableModel(new String[]{"ID", "Name", "Reg No", "Branch", "Room No"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Allotment Records"));
        add(scrollPane, BorderLayout.CENTER);

        // Connect to Database
        connect();
        loadTable();

        // --- Button Actions ---
        btnAllot.addActionListener(e -> allotRoom());
        btnUpdate.addActionListener(e -> updateRoom());
        btnDelete.addActionListener(e -> deleteRoom());
        btnSearch.addActionListener(e -> searchRecord());
        btnRefresh.addActionListener(e -> loadTable());
        btnClear.addActionListener(e -> clearFields());

        // --- Press Enter to Allot ---
        tfRoomNo.addActionListener(e -> allotRoom());
    }

    // Connect to MySQL
    private void connect() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to MySQL successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Database Connection Failed:\n" + e.getMessage());
        }
    }

    // Load table data
    private void loadTable() {
        try {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database not connected!");
                return;
            }
            model.setRowCount(0);
            pst = conn.prepareStatement("SELECT * FROM allotments");
            rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("reg_no"),
                        rs.getString("branch"),
                        rs.getString("room_no")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Allot new room
    private void allotRoom() {
        if (tfName.getText().isEmpty() || tfRegNo.getText().isEmpty() || tfRoomNo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Please fill all fields!");
            return;
        }
        try {
            String sql = "INSERT INTO allotments (name, reg_no, branch, room_no) VALUES (?, ?, ?, ?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, tfName.getText());
            pst.setString(2, tfRegNo.getText());
            pst.setString(3, cbBranch.getSelectedItem().toString());
            pst.setString(4, tfRoomNo.getText());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Room Allotted Successfully!");
            loadTable();
            clearFields();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Update room record
    private void updateRoom() {
        if (tfRegNo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Enter Reg No to update record!");
            return;
        }
        try {
            String sql = "UPDATE allotments SET name=?, branch=?, room_no=? WHERE reg_no=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, tfName.getText());
            pst.setString(2, cbBranch.getSelectedItem().toString());
            pst.setString(3, tfRoomNo.getText());
            pst.setString(4, tfRegNo.getText());
            int updated = pst.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "✅ Record Updated Successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Record Not Found!");
            }
            loadTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Delete record
    private void deleteRoom() {
        if (tfRegNo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Enter Reg No to delete record!");
            return;
        }
        try {
            String sql = "DELETE FROM allotments WHERE reg_no=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, tfRegNo.getText());
            int deleted = pst.executeUpdate();
            if (deleted > 0) {
                JOptionPane.showMessageDialog(this, "✅ Record Deleted Successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Record Not Found!");
            }
            loadTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Search record by Reg No
    private void searchRecord() {
        if (tfSearch.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Enter Reg No to search!");
            return;
        }
        try {
            String sql = "SELECT * FROM allotments WHERE reg_no=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, tfSearch.getText());
            rs = pst.executeQuery();
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("reg_no"),
                        rs.getString("branch"),
                        rs.getString("room_no")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Clear all text fields
    private void clearFields() {
        tfName.setText("");
        tfRegNo.setText("");
        tfRoomNo.setText("");
        tfSearch.setText("");
        cbBranch.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HostelRoomAllotment().setVisible(true));
    }
}
