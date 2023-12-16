import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddExpenseDialog extends JDialog {
    private JTextField categoryField;
    private JTextField amountField;

    private boolean expenseAdded;
    private Expense expense;

    private ExpenseTrackerApp expenseTrackerApp;

    public AddExpenseDialog(ExpenseTrackerApp expenseTrackerApp) {
        this.expenseTrackerApp = expenseTrackerApp;

        setTitle("Add Expense");
        setSize(300, 200);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(expenseTrackerApp);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel categoryLabel = new JLabel("Category:");
        panel.add(categoryLabel);

        categoryField = new JTextField();
        panel.add(categoryField);

        JLabel amountLabel = new JLabel("Amount:");
        panel.add(amountLabel);

        amountField = new JTextField();
        panel.add(amountField);

        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addExpense();
            }
        });
        addButton.setAlignmentX(CENTER_ALIGNMENT);
        addButton.setVisible(true);
        panel.add(addButton);
        add(panel);
    }

    private void addExpense() {
        String category = categoryField.getText();
        String amountText = amountField.getText();

        if (category.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both category and amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);

            // Create the expense
            expense = new Expense(category, amount);

            // Add the expense to the tracker app
            expenseTrackerApp.addExpense(expense);

            expenseAdded = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isExpenseAdded() {
        return expenseAdded;
    }

    public Expense getExpense() {
        return expense;
    }
}
