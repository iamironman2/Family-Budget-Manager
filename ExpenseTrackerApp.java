import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Locale;

public class ExpenseTrackerApp extends JFrame {

    private List<Expense> expenses;
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private JLabel familyBalanceLabel;
    private List<User> familyMembers;
    private User currentUser;
    private double currentBalance;
    private double familyBalance;

    private JLabel balanceLabel;
    private List<AggregatedExpense> aggregatedExpenses = new ArrayList<>();

    public ExpenseTrackerApp(User user) {
        this.familyMembers = UserRegistration.getRegisteredUsers();
        this.currentUser = user;
        setTitle("Family Budget Manager - Welcome, " + currentUser.getUsername());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load expenses and balance from file
        currentBalance = loadBalance(currentUser);
        expenses = loadExpenses(currentUser);
        familyBalance = loadFamilyBalance();

        // Initialize table model with columns
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Category");
        tableModel.addColumn("Total Amount");
        tableModel.addColumn("Expense Count");

        expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        add(scrollPane, BorderLayout.CENTER);
        expenseTable.setVisible(true);
        updateTable();

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        buttonPanel.setBackground(Color.BLACK);

        addButton(buttonPanel, "Add Expense", e -> showAddExpenseDialog());
        addButton(buttonPanel, "Save Expenses", e -> saveExpenses(currentUser));
        addButton(buttonPanel, "Logout", e -> logout());
        addButton(buttonPanel, "Set Income", e -> showSetIncomeDialog());

        // Display balance
        balanceLabel = new JLabel("Balance: " + formatCurrency(loadBalance(currentUser)));
        balanceLabel.setForeground(Color.BLACK);
        balanceLabel.setBackground(Color.LIGHT_GRAY);
        balanceLabel.setOpaque(true);
        buttonPanel.add(balanceLabel);

        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);

        familyBalanceLabel = new JLabel("Family Balance: " + formatCurrency(calculateFamilyBalance()));
        familyBalanceLabel.setForeground(Color.BLACK);
        familyBalanceLabel.setBackground(Color.LIGHT_GRAY);
        familyBalanceLabel.setOpaque(true);
        familyBalanceLabel.setVisible(true);
        familyBalanceLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showFamilyBalancesDialog();
            }
        });
        buttonPanel.add(familyBalanceLabel);
    }

    private void addButton(JPanel panel, String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        styleButton(button);
        panel.add(button);
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    private void showAddExpenseDialog() {
        AddExpenseDialog dialog = new AddExpenseDialog(this);
        dialog.setVisible(true);

        if (dialog.isExpenseAdded()) {
            Expense addedExpense = dialog.getExpense();
            updateAggregatedExpenses(addedExpense);
            updateTable();
        }
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        updateBalance();
        updateTable();
    }

    private void updateAggregatedExpenses(Expense addedExpense) {
        boolean categoryExists = false;
        for (AggregatedExpense aggregatedExpense : aggregatedExpenses) {
            if (aggregatedExpense.getCategory().equals(addedExpense.getCategory())) {
                // Update existing aggregated expense
                aggregatedExpense.addToTotalAmount(addedExpense.getAmount());
                aggregatedExpense.incrementExpenseCount();
                categoryExists = true;
                break;
            }
        }

        if (!categoryExists) {
            // Create a new aggregated expense
            aggregatedExpenses.add(new AggregatedExpense(addedExpense.getCategory(), addedExpense.getAmount(), 1));
        }
    }

    private void showSetIncomeDialog() {
        String userInput = JOptionPane.showInputDialog(this, "Enter your income in rupees:");
        try {
            // Update the user's income
            currentBalance = Double.parseDouble(userInput);
            balanceLabel.setText("Balance : " + formatCurrency(currentBalance));
            saveBalance(currentUser, currentBalance);
            updateFamilyBalance();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBalance() {
        balanceLabel.setText("Balance : " + formatCurrency(calculateBalance()));
        currentBalance = calculateBalance();
        updateFamilyBalance();
    }

    private void saveBalance(User user, double balance) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(user.getUsername() + "_balance.dat"))) {
            oos.writeDouble(balance);
            saveFamilyBalance();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving balance.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double loadBalance(User user) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(user.getUsername() + "_balance.dat"))) {
            return ois.readDouble();
        } catch (IOException ex) {
            return 0.0;
        }
    }

    private double calculateBalance() {
        if (currentBalance >= expenses.get(expenses.size() - 1).getAmount()) {
            // Deduct the expense from the user's balance
            return currentBalance - expenses.get(expenses.size() - 1).getAmount();
        } else {
            // If the user's balance is not sufficient, deduct as much as possible
            double remBal = expenses.get(expenses.size() - 1).getAmount() - currentBalance;
            currentBalance = 0.0;

            // Deduct the remaining expense from the family balance
            familyBalance -= remBal;
            // Ensure family balance doesn't go negative
            if (familyBalance < 0.0) {
                familyBalance = 0.0;
                return currentBalance;
            } else
                return currentBalance;
        }
    }

    private void saveExpenses(User user) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(user.getUsername() + "_expenses.dat"))) {
            oos.writeObject(expenses);
            JOptionPane.showMessageDialog(this, "Expenses saved successfully.", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            saveBalance(currentUser, currentBalance);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving expenses: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Expense> loadExpenses(User user) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(user.getUsername() + "_expenses.dat"))) {
            List<Expense> loadedExpenses = (List<Expense>) ois.readObject();
            return loadedExpenses;
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void logout() {
        dispose();
        new LoginScreen();
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(amount);
    }

    private void updateTable() {
        // Clear the table
        tableModel.setRowCount(0);

        // Aggregate expenses based on category
        Map<String, AggregatedExpense> aggregatedExpenses = aggregateExpenses();

        // Populate the table with aggregated expenses
        for (Map.Entry<String, AggregatedExpense> entry : aggregatedExpenses.entrySet()) {
            AggregatedExpense aggregatedExpense = entry.getValue();
            Object[] rowData = {entry.getKey(), aggregatedExpense.getTotalAmount(), aggregatedExpense.getExpenseCount()};
            tableModel.addRow(rowData);
        }

        // Repaint the table to reflect changes
        expenseTable.repaint();
    }

    private Map<String, AggregatedExpense> aggregateExpenses() {
        Map<String, AggregatedExpense> aggregatedExpenses = new HashMap<>();

        for (Expense expense : expenses) {
            String category = expense.getCategory();

            // If category already exists, update the aggregated data
            if (aggregatedExpenses.containsKey(category)) {
                AggregatedExpense aggregatedExpense = aggregatedExpenses.get(category);
                aggregatedExpense.addToTotalAmount(expense.getAmount());
                aggregatedExpense.incrementExpenseCount();
            } else {
                // If category doesn't exist, create a new entry
                AggregatedExpense aggregatedExpense = new AggregatedExpense(expense.getCategory(), expense.getAmount(), 1);
                aggregatedExpenses.put(category, aggregatedExpense);
            }
        }

        return aggregatedExpenses;
    }

    private double calculateFamilyBalance() {
        familyBalance = 0.0;
        for (User familyMember : familyMembers) {
            familyBalance+=loadBalance(familyMember);
        }
        return familyBalance-loadBalance(currentUser)+currentBalance;
    }

    private void updateFamilyBalance() {
        familyBalanceLabel.setText("Family Balance: " + formatCurrency(calculateFamilyBalance()));
    }

    private void saveFamilyBalance() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Family's Balance.dat"))) {
            familyBalance=0.0;
            for (User familyMember : familyMembers) {
                familyBalance += loadBalance(familyMember);
            }
            oos.writeDouble(familyBalance);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving balance.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double loadFamilyBalance() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Family's Balance.dat"))) {
            return ois.readDouble();
        } catch (IOException ex) {
            return 0.0;
        }
    }

    private void showFamilyBalancesDialog() {
        StringBuilder familyBalancesInfo = new StringBuilder("Family Balances:\n");

        for (User familyMember : familyMembers) {
            double memberBalance = loadBalance(familyMember);
            familyBalancesInfo.append(familyMember.getUsername())
                    .append(": ")
                    .append(formatCurrency(memberBalance))
                    .append("\n");
        }

        JOptionPane.showMessageDialog(this, familyBalancesInfo.toString(), "Family Balances", JOptionPane.INFORMATION_MESSAGE);
    }


    private static class AggregatedExpense implements Serializable {
        private double totalAmount;
        private int expenseCount;
        private String category;

        public AggregatedExpense(String category, double totalAmount, int expenseCount) {
            this.category = category;
            this.totalAmount = totalAmount;
            this.expenseCount = expenseCount;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public int getExpenseCount() {
            return expenseCount;
        }

        public void addToTotalAmount(double amount) {
            totalAmount += amount;
        }

        public void incrementExpenseCount() {
            expenseCount++;
        }

        public String getCategory() {
            return category;
        }
    }
}
