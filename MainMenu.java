import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class MainMenu extends JFrame implements ActionListener {
    JButton customerBtn, categoryBtn, foodBtn, orderBtn, viewBtn;

    public MainMenu() {
        setTitle("Food Ordering System");
        setSize(420, 340);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        customerBtn = new JButton("Customer Form");
        categoryBtn = new JButton("Category Form");
        foodBtn = new JButton("Food Item Form");
        orderBtn = new JButton("Place Order");
        viewBtn = new JButton("View Orders");

        customerBtn.setBounds(110, 40, 180, 30);
        categoryBtn.setBounds(110, 80, 180, 30);
        foodBtn.setBounds(110, 120, 180, 30);
        orderBtn.setBounds(110, 160, 180, 30);
        viewBtn.setBounds(110, 200, 180, 30);

        add(customerBtn);
        add(categoryBtn);
        add(foodBtn);
        add(orderBtn);
        add(viewBtn);

        customerBtn.addActionListener(this);
        categoryBtn.addActionListener(this);
        foodBtn.addActionListener(this);
        orderBtn.addActionListener(this);
        viewBtn.addActionListener(this);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == customerBtn) {
            new CustomerForm();
        } else if (e.getSource() == categoryBtn) {
            new CategoryForm();
        } else if (e.getSource() == foodBtn) {
            new FoodItemForm();
        } else if (e.getSource() == orderBtn) {
            new OrderForm();
        } else if (e.getSource() == viewBtn) {
            new ViewOrders();
        }
    }

    public static void main(String[] args) {
        new MainMenu();
    }
}

class DBConnection {
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/food_ordering_system",
                "root",
                "12345690"
        );
    }
}

class CustomerForm extends JFrame implements ActionListener {
    JLabel nameLabel, phoneLabel, addressLabel;
    JTextField nameField, phoneField, addressField;
    JButton insertBtn, viewBtn, updateBtn, deleteBtn;
    JTable table;
    JScrollPane scrollPane;
    int selectedCustomerId = -1;

    public CustomerForm() {
        setTitle("Customer Form");
        setSize(760, 460);
        setLayout(null);

        nameLabel = new JLabel("Customer Name");
        phoneLabel = new JLabel("Phone");
        addressLabel = new JLabel("Address");

        nameField = new JTextField();
        phoneField = new JTextField();
        addressField = new JTextField();

        insertBtn = new JButton("Insert");
        viewBtn = new JButton("View");
        updateBtn = new JButton("Update");
        deleteBtn = new JButton("Delete");

        nameLabel.setBounds(30, 30, 100, 30);
        nameField.setBounds(150, 30, 180, 30);

        phoneLabel.setBounds(30, 70, 100, 30);
        phoneField.setBounds(150, 70, 180, 30);

        addressLabel.setBounds(30, 110, 100, 30);
        addressField.setBounds(150, 110, 180, 30);

        insertBtn.setBounds(30, 170, 100, 30);
        viewBtn.setBounds(140, 170, 100, 30);
        updateBtn.setBounds(250, 170, 100, 30);
        deleteBtn.setBounds(360, 170, 100, 30);

        add(nameLabel);
        add(nameField);
        add(phoneLabel);
        add(phoneField);
        add(addressLabel);
        add(addressField);
        add(insertBtn);
        add(viewBtn);
        add(updateBtn);
        add(deleteBtn);

        table = new JTable();
        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 220, 690, 200);
        add(scrollPane);

        insertBtn.addActionListener(this);
        viewBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedCustomerId = Integer.parseInt(table.getValueAt(row, 0).toString());
                    nameField.setText(table.getValueAt(row, 1).toString());
                    phoneField.setText(table.getValueAt(row, 2).toString());
                    addressField.setText(table.getValueAt(row, 3).toString());
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void loadCustomers() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM customers");

            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();

            String[] columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = md.getColumnName(i + 1);
            }

            DefaultTableModel model = new DefaultTableModel(columns, 0);

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }

            table.setModel(model);
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == insertBtn) {
            try {
                Connection con = DBConnection.getConnection();
                String sql = "INSERT INTO customers (customer_name, phone, address) VALUES (?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, nameField.getText());
                pst.setString(2, phoneField.getText());
                pst.setString(3, addressField.getText());

                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Customer inserted successfully");
                    nameField.setText("");
                    phoneField.setText("");
                    addressField.setText("");
                    selectedCustomerId = -1;
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Insert failed");
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        if (e.getSource() == updateBtn) {
            if (selectedCustomerId == -1) {
                JOptionPane.showMessageDialog(this, "Select a customer record from the table first.");
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                String sql = "UPDATE customers SET customer_name = ?, phone = ?, address = ? WHERE customer_id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, nameField.getText());
                pst.setString(2, phoneField.getText());
                pst.setString(3, addressField.getText());
                pst.setInt(4, selectedCustomerId);

                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Customer updated successfully");
                    nameField.setText("");
                    phoneField.setText("");
                    addressField.setText("");
                    selectedCustomerId = -1;
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed");
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        if (e.getSource() == deleteBtn) {
            if (selectedCustomerId == -1) {
                JOptionPane.showMessageDialog(this, "Select a customer record from the table first.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Deleting this customer will also remove all their orders. Continue?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                String deleteOrderDetailsSql = "DELETE od FROM order_details od JOIN orders o ON od.order_id = o.order_id WHERE o.customer_id = ?";
                PreparedStatement deleteOrderDetailsPst = con.prepareStatement(deleteOrderDetailsSql);
                deleteOrderDetailsPst.setInt(1, selectedCustomerId);
                deleteOrderDetailsPst.executeUpdate();

                String deleteOrdersSql = "DELETE FROM orders WHERE customer_id = ?";
                PreparedStatement deleteOrdersPst = con.prepareStatement(deleteOrdersSql);
                deleteOrdersPst.setInt(1, selectedCustomerId);
                deleteOrdersPst.executeUpdate();

                String sql = "DELETE FROM customers WHERE customer_id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, selectedCustomerId);

                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Customer and related orders deleted successfully");
                    nameField.setText("");
                    phoneField.setText("");
                    addressField.setText("");
                    selectedCustomerId = -1;
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed");
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        if (e.getSource() == viewBtn) {
            selectedCustomerId = -1;
            loadCustomers();
        }
    }
}

class CategoryForm extends JFrame implements ActionListener {
    JLabel categoryLabel;
    JTextField categoryField;
    JButton insertBtn, viewBtn, updateBtn, deleteBtn;
    JTable table;
    JScrollPane scrollPane;
    int selectedCategoryId = -1;

    public CategoryForm() {
        setTitle("Category Form");
        setSize(620, 380);
        setLayout(null);

        categoryLabel = new JLabel("Category Name");
        categoryField = new JTextField();
        insertBtn = new JButton("Insert");
        viewBtn = new JButton("View");
        updateBtn = new JButton("Update");
        deleteBtn = new JButton("Delete");

        categoryLabel.setBounds(30, 40, 100, 30);
        categoryField.setBounds(150, 40, 180, 30);
        insertBtn.setBounds(30, 100, 100, 30);
        viewBtn.setBounds(140, 100, 100, 30);
        updateBtn.setBounds(250, 100, 100, 30);
        deleteBtn.setBounds(360, 100, 100, 30);

        add(categoryLabel);
        add(categoryField);
        add(insertBtn);
        add(viewBtn);
        add(updateBtn);
        add(deleteBtn);

        table = new JTable();
        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 150, 530, 190);
        add(scrollPane);

        insertBtn.addActionListener(this);
        viewBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedCategoryId = Integer.parseInt(table.getValueAt(row, 0).toString());
                    categoryField.setText(table.getValueAt(row, 1).toString());
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void loadCategories() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM categories");

            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();

            String[] columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = md.getColumnName(i + 1);
            }

            DefaultTableModel model = new DefaultTableModel(columns, 0);

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }

            table.setModel(model);
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == insertBtn) {
            try {
                Connection con = DBConnection.getConnection();
                String sql = "INSERT INTO categories (category_name) VALUES (?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, categoryField.getText());

                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Category inserted successfully");
                    categoryField.setText("");
                    selectedCategoryId = -1;
                    loadCategories();
                } else {
                    JOptionPane.showMessageDialog(this, "Insert failed");
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        if (e.getSource() == updateBtn) {
            if (selectedCategoryId == -1) {
                JOptionPane.showMessageDialog(this, "Select a category record from the table first.");
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                String sql = "UPDATE categories SET category_name = ? WHERE category_id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, categoryField.getText());
                pst.setInt(2, selectedCategoryId);

                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Category updated successfully");
                    categoryField.setText("");
                    selectedCategoryId = -1;
                    loadCategories();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed");
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        if (e.getSource() == deleteBtn) {
            if (selectedCategoryId == -1) {
                JOptionPane.showMessageDialog(this, "Select a category record from the table first.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this category?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                String checkSql = "SELECT COUNT(*) FROM food_items WHERE category_id = ?";
                PreparedStatement checkPst = con.prepareStatement(checkSql);
                checkPst.setInt(1, selectedCategoryId);
                ResultSet checkRs = checkPst.executeQuery();
                if (checkRs.next() && checkRs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Cannot delete this category because food items exist for it.");
                    con.close();
                    return;
                }

                String sql = "DELETE FROM categories WHERE category_id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, selectedCategoryId);

                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Category deleted successfully");
                    categoryField.setText("");
                    selectedCategoryId = -1;
                    loadCategories();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed");
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        if (e.getSource() == viewBtn) {
            selectedCategoryId = -1;
            loadCategories();
        }
    }
}

class FoodItemForm extends JFrame implements ActionListener {
    JLabel foodNameLabel, priceLabel, categoryIdLabel;
    JTextField foodNameField, priceField, categoryIdField;
    JButton insertBtn, viewBtn, updateBtn, deleteBtn;
    JTable table;
    JScrollPane scrollPane;
    int selectedFoodId = -1;

    public FoodItemForm() {
        setTitle("Food Item Form");
        setSize(760, 460);
        setLayout(null);

        foodNameLabel = new JLabel("Food Name");
        priceLabel = new JLabel("Price");
        categoryIdLabel = new JLabel("Category ID");

        foodNameField = new JTextField();
        priceField = new JTextField();
        categoryIdField = new JTextField();

        insertBtn = new JButton("Insert");
        viewBtn = new JButton("View");
        updateBtn = new JButton("Update");
        deleteBtn = new JButton("Delete");

        foodNameLabel.setBounds(30, 30, 100, 30);
        foodNameField.setBounds(150, 30, 180, 30);

        priceLabel.setBounds(30, 70, 100, 30);
        priceField.setBounds(150, 70, 180, 30);

        categoryIdLabel.setBounds(30, 110, 100, 30);
        categoryIdField.setBounds(150, 110, 180, 30);

        insertBtn.setBounds(30, 170, 100, 30);
        viewBtn.setBounds(140, 170, 100, 30);
        updateBtn.setBounds(250, 170, 100, 30);
        deleteBtn.setBounds(360, 170, 100, 30);

        add(foodNameLabel);
        add(foodNameField);
        add(priceLabel);
        add(priceField);
        add(categoryIdLabel);
        add(categoryIdField);
        add(insertBtn);
        add(viewBtn);
        add(updateBtn);
        add(deleteBtn);

        table = new JTable();
        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 220, 700, 200);
        add(scrollPane);

        insertBtn.addActionListener(this);
        viewBtn.addActionListener(this);
        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    selectedFoodId = Integer.parseInt(table.getValueAt(row, 0).toString());
                    foodNameField.setText(table.getValueAt(row, 1).toString());
                    priceField.setText(table.getValueAt(row, 2).toString());
                    categoryIdField.setText(table.getValueAt(row, 3).toString());
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void loadFoodItems() {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM food_items");

            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();

            String[] columns = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = md.getColumnName(i + 1);
            }

            DefaultTableModel model = new DefaultTableModel(columns, 0);

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }

            table.setModel(model);
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == insertBtn) {
            try {
                Connection con = DBConnection.getConnection();
                String sql = "INSERT INTO food_items (food_name, price, category_id) VALUES (?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, foodNameField.getText());
                pst.setDouble(2, Double.parseDouble(priceField.getText()));
                pst.setInt(3, Integer.parseInt(categoryIdField.getText()));

                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Food item inserted successfully");
                    foodNameField.setText("");
                    priceField.setText("");
                    categoryIdField.setText("");
                    selectedFoodId = -1;
                    loadFoodItems();
                } else {
                    JOptionPane.showMessageDialog(this, "Insert failed");
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        if (e.getSource() == updateBtn) {
            if (selectedFoodId == -1) {
                JOptionPane.showMessageDialog(this, "Select a food item from the table first.");
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                String sql = "UPDATE food_items SET food_name = ?, price = ?, category_id = ? WHERE food_id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, foodNameField.getText());
                pst.setDouble(2, Double.parseDouble(priceField.getText()));
                pst.setInt(3, Integer.parseInt(categoryIdField.getText()));
                pst.setInt(4, selectedFoodId);

                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Food item updated successfully");
                    foodNameField.setText("");
                    priceField.setText("");
                    categoryIdField.setText("");
                    selectedFoodId = -1;
                    loadFoodItems();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed");
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        if (e.getSource() == deleteBtn) {
            if (selectedFoodId == -1) {
                JOptionPane.showMessageDialog(this, "Select a food item from the table first.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this food item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                String checkSql = "SELECT COUNT(*) FROM order_details WHERE food_id = ?";
                PreparedStatement checkPst = con.prepareStatement(checkSql);
                checkPst.setInt(1, selectedFoodId);
                ResultSet checkRs = checkPst.executeQuery();
                if (checkRs.next() && checkRs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Cannot delete this food item because orders exist for it.");
                    con.close();
                    return;
                }

                String sql = "DELETE FROM food_items WHERE food_id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, selectedFoodId);

                int result = pst.executeUpdate();

                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Food item deleted successfully");
                    foodNameField.setText("");
                    priceField.setText("");
                    categoryIdField.setText("");
                    selectedFoodId = -1;
                    loadFoodItems();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed");
                }

                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        if (e.getSource() == viewBtn) {
            selectedFoodId = -1;
            loadFoodItems();
        }
    }
}

class OrderForm extends JFrame implements ActionListener {
    JLabel customerIdLabel, foodIdLabel, quantityLabel;
    JTextField customerIdField, foodIdField, quantityField;
    JButton placeOrderBtn;

    public OrderForm() {
        setTitle("Order Form");
        setSize(420, 300);
        setLayout(null);

        customerIdLabel = new JLabel("Customer ID");
        foodIdLabel = new JLabel("Food ID");
        quantityLabel = new JLabel("Quantity");

        customerIdField = new JTextField();
        foodIdField = new JTextField();
        quantityField = new JTextField();

        placeOrderBtn = new JButton("Place Order");

        customerIdLabel.setBounds(40, 40, 100, 30);
        customerIdField.setBounds(160, 40, 150, 30);

        foodIdLabel.setBounds(40, 80, 100, 30);
        foodIdField.setBounds(160, 80, 150, 30);

        quantityLabel.setBounds(40, 120, 100, 30);
        quantityField.setBounds(160, 120, 150, 30);

        placeOrderBtn.setBounds(120, 180, 140, 30);

        add(customerIdLabel);
        add(customerIdField);
        add(foodIdLabel);
        add(foodIdField);
        add(quantityLabel);
        add(quantityField);
        add(placeOrderBtn);

        placeOrderBtn.addActionListener(this);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            Connection con = DBConnection.getConnection();

            int customerId = Integer.parseInt(customerIdField.getText());
            int foodId = Integer.parseInt(foodIdField.getText());
            int quantity = Integer.parseInt(quantityField.getText());

            String customerSql = "SELECT customer_id FROM customers WHERE customer_id = ?";
            PreparedStatement customerPst = con.prepareStatement(customerSql);
            customerPst.setInt(1, customerId);
            ResultSet customerRs = customerPst.executeQuery();

            if (!customerRs.next()) {
                JOptionPane.showMessageDialog(this, "Invalid Customer ID");
                con.close();
                return;
            }

            String foodSql = "SELECT price FROM food_items WHERE food_id = ?";
            PreparedStatement foodPst = con.prepareStatement(foodSql);
            foodPst.setInt(1, foodId);
            ResultSet foodRs = foodPst.executeQuery();

            if (!foodRs.next()) {
                JOptionPane.showMessageDialog(this, "Invalid Food ID");
                con.close();
                return;
            }

            double price = foodRs.getDouble("price");
            double subtotal = price * quantity;

            String orderSql = "INSERT INTO orders (customer_id, order_date, total_amount) VALUES (?, ?, ?)";
            PreparedStatement orderPst = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderPst.setInt(1, customerId);
            orderPst.setDate(2, Date.valueOf(LocalDate.now()));
            orderPst.setDouble(3, subtotal);
            orderPst.executeUpdate();

            ResultSet keyRs = orderPst.getGeneratedKeys();
            int orderId = 0;
            if (keyRs.next()) {
                orderId = keyRs.getInt(1);
            }

            String detailSql = "INSERT INTO order_details (order_id, food_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
            PreparedStatement detailPst = con.prepareStatement(detailSql);
            detailPst.setInt(1, orderId);
            detailPst.setInt(2, foodId);
            detailPst.setInt(3, quantity);
            detailPst.setDouble(4, subtotal);

            int result = detailPst.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Order placed successfully\nTotal Amount: " + subtotal);
                customerIdField.setText("");
                foodIdField.setText("");
                quantityField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Order failed");
            }

            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}

class ViewOrders extends JFrame {
    JTable table;
    JScrollPane scrollPane;

    public ViewOrders() {
        setTitle("View Orders");
        setSize(820, 400);
        setLayout(null);

        String[] columns = {"Order ID", "Customer Name", "Food Name", "Quantity", "Subtotal", "Order Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        table = new JTable(model);
        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 20, 760, 300);
        add(scrollPane);

        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT o.order_id, c.customer_name, f.food_name, od.quantity, od.subtotal, o.order_date " +
                         "FROM orders o " +
                         "JOIN customers c ON o.customer_id = c.customer_id " +
                         "JOIN order_details od ON o.order_id = od.order_id " +
                         "JOIN food_items f ON od.food_id = f.food_id";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("order_id"),
                        rs.getString("customer_name"),
                        rs.getString("food_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("subtotal"),
                        rs.getDate("order_date")
                };
                model.addRow(row);
            }

            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        setLocationRelativeTo(null);
        setVisible(true);
    }
}