import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FoodBillingFull {

    static final String DB_URL = "jdbc:mysql://localhost:3306/food_db";
    static final String USER = "root";
    static final String PASS = "12345690";

    JFrame frame;
    JTable table;
    DefaultTableModel model;

    JTextField tfId, tfFood, tfPrice, tfStock, tfQty, tfSearch;

    public FoodBillingFull() {

        frame = new JFrame("Food Billing System");

        model = new DefaultTableModel();
        table = new JTable(model);

        model.addColumn("ID");
        model.addColumn("Food");
        model.addColumn("Price");
        model.addColumn("Stock");

        loadData("");

        // INPUTS
        tfId = new JTextField(5);
        tfFood = new JTextField(10);
        tfPrice = new JTextField(5);
        tfStock = new JTextField(5);
        tfQty = new JTextField(5);
        tfSearch = new JTextField(10);

        JPanel panel = new JPanel();

        panel.add(new JLabel("ID")); panel.add(tfId);
        panel.add(new JLabel("Food")); panel.add(tfFood);
        panel.add(new JLabel("Price")); panel.add(tfPrice);
        panel.add(new JLabel("Stock")); panel.add(tfStock);
        panel.add(new JLabel("Qty")); panel.add(tfQty);
        panel.add(new JLabel("Search")); panel.add(tfSearch);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton searchBtn = new JButton("Search");
        JButton billBtn = new JButton("Bill");

        panel.add(addBtn);
        panel.add(updateBtn);
        panel.add(deleteBtn);
        panel.add(searchBtn);
        panel.add(billBtn);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        // 🔹 ADD
        addBtn.addActionListener(e -> {
            try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)){

                String sql = "INSERT INTO products (food_name, price, stock) VALUES (?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);

                pst.setString(1, tfFood.getText());
                pst.setDouble(2, Double.parseDouble(tfPrice.getText()));
                pst.setInt(3, Integer.parseInt(tfStock.getText()));

                pst.executeUpdate();
                loadData("");

            }catch(Exception ex){ ex.printStackTrace(); }
        });

        // 🔹 UPDATE
        updateBtn.addActionListener(e -> {
            try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)){

                String sql = "UPDATE products SET food_name=?, price=?, stock=? WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);

                pst.setString(1, tfFood.getText());
                pst.setDouble(2, Double.parseDouble(tfPrice.getText()));
                pst.setInt(3, Integer.parseInt(tfStock.getText()));
                pst.setInt(4, Integer.parseInt(tfId.getText()));

                pst.executeUpdate();
                loadData("");

            }catch(Exception ex){ ex.printStackTrace(); }
        });

        // 🔹 DELETE
        deleteBtn.addActionListener(e -> {
            try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)){

                String sql = "DELETE FROM products WHERE id=?";
                PreparedStatement pst = conn.prepareStatement(sql);

                pst.setInt(1, Integer.parseInt(tfId.getText()));
                pst.executeUpdate();

                loadData("");

            }catch(Exception ex){ ex.printStackTrace(); }
        });

        // 🔹 SEARCH
        searchBtn.addActionListener(e -> {
            loadData(tfSearch.getText());
        });

        // 🔥 BILL (MAIN LOGIC)
        billBtn.addActionListener(e -> {
            try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)){

                int id = Integer.parseInt(tfId.getText());
                int qty = Integer.parseInt(tfQty.getText());

                String check = "SELECT stock, price, food_name FROM products WHERE id=?";
                PreparedStatement pst1 = conn.prepareStatement(check);
                pst1.setInt(1, id);
                ResultSet rs = pst1.executeQuery();

                if(rs.next()){
                    int stock = rs.getInt("stock");
                    double price = rs.getDouble("price");
                    String name = rs.getString("food_name");

                    if(stock >= qty){

                        double total = price * qty;

                        // insert order
                        String orderSql = "INSERT INTO orders (food_name, quantity, total) VALUES (?, ?, ?)";
                        PreparedStatement pst2 = conn.prepareStatement(orderSql);
                        pst2.setString(1, name);
                        pst2.setInt(2, qty);
                        pst2.setDouble(3, total);
                        pst2.executeUpdate();

                        // update stock
                        String update = "UPDATE products SET stock = stock - ? WHERE id=?";
                        PreparedStatement pst3 = conn.prepareStatement(update);
                        pst3.setInt(1, qty);
                        pst3.setInt(2, id);
                        pst3.executeUpdate();

                        JOptionPane.showMessageDialog(null, "Bill Success! Stock updated ✅");

                        loadData("");

                    } else {
                        JOptionPane.showMessageDialog(null, "Not enough stock ❌");
                    }
                }

            }catch(Exception ex){ ex.printStackTrace(); }
        });

        // TABLE CLICK
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0){
                tfId.setText(model.getValueAt(row,0).toString());
                tfFood.setText(model.getValueAt(row,1).toString());
                tfPrice.setText(model.getValueAt(row,2).toString());
                tfStock.setText(model.getValueAt(row,3).toString());
            }
        });

        frame.setSize(900,450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // 🔄 LOAD DATA
    void loadData(String search){

        model.setRowCount(0);

        try(Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)){

            String sql = "SELECT * FROM products WHERE food_name LIKE ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + search + "%");

            ResultSet rs = pst.executeQuery();

            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("food_name"),
                        rs.getDouble("price"),
                        rs.getInt("stock")
                });
            }

        }catch(Exception e){ e.printStackTrace(); }
    }

    public static void main(String[] args) {
        new FoodBillingFull();
    }
}