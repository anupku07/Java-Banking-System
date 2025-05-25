//package JavaProject;



import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

// Transaction class to store transaction history
class Transaction {
    private String type;
    private double amount;
    private Date timestamp;
    private double balanceAfter;

    public Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = new Date();
    }

    // Getters
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public Date getTimestamp() { return timestamp; }
    public double getBalanceAfter() { return balanceAfter; }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.format("%s | %s | Rs %.2f | Balance: Rs %.2f",
                sdf.format(timestamp), type, amount, balanceAfter);
    }

    public String getFormattedAmount() {
        return String.format("Rs %.2f", amount);
    }

    public String getFormattedBalance() {
        return String.format("Rs %.2f", balanceAfter);
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(timestamp);
    }
}

// Enhanced Bank Operations class
abstract class BankOperations {
    protected double balance;
    protected String pin;
    protected String accountNumber;
    protected String accountHolderName;
    protected List<Transaction> transactionHistory;
    protected boolean isBlocked;
    protected int failedAttempts;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    public BankOperations(double initialBalance, String pin, String accountNumber, String holderName) {
        this.balance = initialBalance;
        this.pin = pin;
        this.accountNumber = accountNumber;
        this.accountHolderName = holderName;
        this.transactionHistory = new ArrayList<>();
        this.isBlocked = false;
        this.failedAttempts = 0;
    }

    // Getters and Setters
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolderName() { return accountHolderName; }
    public List<Transaction> getTransactionHistory() { return transactionHistory; }
    public boolean isBlocked() { return isBlocked; }

    public boolean validatePin(String enteredPin) {
        if (isBlocked) return false;

        if (this.pin.equals(enteredPin)) {
            failedAttempts = 0;
            return true;
        } else {
            failedAttempts++;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                isBlocked = true;
            }
            return false;
        }
    }

    public void addTransaction(String type, double amount) {
        transactionHistory.add(new Transaction(type, amount, balance));
    }

    public abstract void showMenu();
}

// Enhanced ATM class with detailed error messages
class ATM extends BankOperations {
    private static final double MAX_WITHDRAWAL = 1000.0;
    private static final double MAX_DEPOSIT = 10000.0;
    private static final double MAX_TRANSFER = 5000.0;

    public ATM(double initialBalance, String pin, String accountNumber, String holderName) {
        super(initialBalance, pin, accountNumber, holderName);
    }

    @Override
    public void showMenu() {
        // This will be handled by the GUI
    }

    public TransactionResult withdraw(double amount) {
        if (amount <= 0) {
            return new TransactionResult(false, "Amount must be greater than Rs 0");
        }
        if (amount > balance) {
            return new TransactionResult(false, String.format("Insufficient balance. Current balance: Rs %.2f", balance));
        }
        if (amount > MAX_WITHDRAWAL) {
            return new TransactionResult(false, String.format("Daily withdrawal limit exceeded. Maximum: Rs %.2f", MAX_WITHDRAWAL));
        }

        balance -= amount;
        addTransaction("WITHDRAWAL", amount);
        return new TransactionResult(true, String.format("Successfully withdrawn Rs %.2f", amount));
    }

    public TransactionResult deposit(double amount) {
        if (amount <= 0) {
            return new TransactionResult(false, "Amount must be greater than Rs 0");
        }
        if (amount > MAX_DEPOSIT) {
            return new TransactionResult(false, String.format("Daily deposit limit exceeded. Maximum: Rs %.2f", MAX_DEPOSIT));
        }

        balance += amount;
        addTransaction("DEPOSIT", amount);
        return new TransactionResult(true, String.format("Successfully deposited Rs %.2f", amount));
    }

    public TransactionResult transfer(double amount, String targetAccount) {
        if (amount <= 0) {
            return new TransactionResult(false, "Amount must be greater than Rs 0");
        }
        if (amount > balance) {
            return new TransactionResult(false, String.format("Insufficient balance. Current balance: Rs %.2f", balance));
        }
        if (amount > MAX_TRANSFER) {
            return new TransactionResult(false, String.format("Daily transfer limit exceeded. Maximum: Rs %.2f", MAX_TRANSFER));
        }
        if (targetAccount == null || targetAccount.trim().isEmpty()) {
            return new TransactionResult(false, "Target account number is required");
        }

        balance -= amount;
        addTransaction("TRANSFER TO " + targetAccount, amount);
        return new TransactionResult(true, String.format("Successfully transferred Rs %.2f to %s", amount, targetAccount));
    }

    public boolean changePin(String oldPin, String newPin) {
        if (!validatePin(oldPin)) return false;
        if (newPin.length() != 4) return false;

        this.pin = newPin;
        addTransaction("PIN CHANGE", 0);
        return true;
    }

    // Getter methods for limits (for UI display)
    public double getMaxWithdrawal() { return MAX_WITHDRAWAL; }
    public double getMaxDeposit() { return MAX_DEPOSIT; }
    public double getMaxTransfer() { return MAX_TRANSFER; }
}

// Transaction result class for better error handling
class TransactionResult {
    private boolean success;
    private String message;

    public TransactionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

// Main ATM GUI Application
public class ATMApplication extends JFrame{
    private ATM atm;
    private JPanel currentPanel;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private Transaction lastTransaction;

    // Color scheme
    private final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private final Color SECONDARY_COLOR = new Color(63, 81, 181);
    private final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private final Color ERROR_COLOR = new Color(244, 67, 54);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color CARD_COLOR = Color.WHITE;

    public ATMApplication() {
        // Initialize ATM with sample data (amount in Rs)
        atm = new ATM(25000.00, "1234", "ACC123456789", "John Doe");

        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("SecureBank ATM - Advanced Banking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main container with CardLayout
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Create all panels
        mainContainer.add(createWelcomePanel(), "WELCOME");
        mainContainer.add(createPinPanel(), "PIN");
        mainContainer.add(createMainMenuPanel(), "MENU");
        mainContainer.add(createBalancePanel(), "BALANCE");
        mainContainer.add(createWithdrawPanel(), "WITHDRAW");
        mainContainer.add(createDepositPanel(), "DEPOSIT");
        mainContainer.add(createTransferPanel(), "TRANSFER");
        mainContainer.add(createHistoryPanel(), "HISTORY");
        mainContainer.add(createSettingsPanel(), "SETTINGS");
        mainContainer.add(createReceiptPanel(), "RECEIPT");

        add(mainContainer);
        cardLayout.show(mainContainer, "WELCOME");
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(40, 20, 20, 20));

        JLabel titleLabel = new JLabel("SecureBank ATM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Advanced Banking System", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Center content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel cardPanel = createCard();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel welcomeText = new JLabel("<html><div style='text-align: center;'>"
                + "<h2>Welcome to SecureBank</h2>"
                + "<p>Your trusted banking partner</p>"
                + "<br><p>Please insert your card or tap to begin</p></div></html>");
        welcomeText.setHorizontalAlignment(SwingConstants.CENTER);
        cardPanel.add(welcomeText);

        JButton startButton = createStyledButton("START BANKING", SUCCESS_COLOR);
        startButton.addActionListener(e -> cardLayout.show(mainContainer, "PIN"));

        centerPanel.add(cardPanel, gbc);
        gbc.gridy = 1;
        centerPanel.add(startButton, gbc);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPinPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader("Enter PIN");

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        JPanel cardPanel = createCard();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        JLabel instructionLabel = new JLabel("Please enter your 4-digit PIN:");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField pinField = new JPasswordField(4);
        pinField.setMaximumSize(new Dimension(150, 30));
        pinField.setHorizontalAlignment(SwingConstants.CENTER);
        pinField.setFont(new Font("Arial", Font.BOLD, 18));

        JButton enterButton = createStyledButton("ENTER", SUCCESS_COLOR);
        JButton clearButton = createStyledButton("CLEAR", SECONDARY_COLOR);
        JButton cancelButton = createStyledButton("CANCEL", ERROR_COLOR);

        enterButton.addActionListener(e -> {
            String enteredPin = new String(pinField.getPassword());
            if (atm.isBlocked()) {
                showMessage("Account is blocked due to multiple failed attempts.", "Account Blocked", JOptionPane.ERROR_MESSAGE);
            } else if (atm.validatePin(enteredPin)) {
                cardLayout.show(mainContainer, "MENU");
                pinField.setText("");
            } else {
                showMessage("Invalid PIN. Please try again.", "Invalid PIN", JOptionPane.ERROR_MESSAGE);
                pinField.setText("");
            }
        });

        clearButton.addActionListener(e -> pinField.setText(""));
        cancelButton.addActionListener(e -> {
            pinField.setText("");
            cardLayout.show(mainContainer, "WELCOME");
        });

        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(instructionLabel);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(pinField);
        cardPanel.add(Box.createVerticalStrut(30));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(clearButton);
        buttonPanel.add(enterButton);
        buttonPanel.add(cancelButton);
        cardPanel.add(buttonPanel);

        centerPanel.add(cardPanel);
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader("Main Menu");

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Account info
        JPanel infoPanel = createCard();
        infoPanel.setLayout(new BorderLayout());
        JLabel infoLabel = new JLabel(String.format("<html><center>Welcome, %s<br>Account: %s</center></html>",
                atm.getAccountHolderName(), atm.getAccountNumber()));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(infoLabel, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        centerPanel.add(infoPanel, gbc);

        // Menu buttons
        gbc.gridwidth = 1;
        gbc.gridy = 1;

        JButton balanceButton = createMenuButton("Check Balance", "💰");
        balanceButton.addActionListener(e -> cardLayout.show(mainContainer, "BALANCE"));
        gbc.gridx = 0;
        centerPanel.add(balanceButton, gbc);

        JButton withdrawButton = createMenuButton("Withdraw Money", "💸");
        withdrawButton.addActionListener(e -> cardLayout.show(mainContainer, "WITHDRAW"));
        gbc.gridx = 1;
        centerPanel.add(withdrawButton, gbc);

        gbc.gridy = 2;
        JButton depositButton = createMenuButton("Deposit Money", "💵");
        depositButton.addActionListener(e -> cardLayout.show(mainContainer, "DEPOSIT"));
        gbc.gridx = 0;
        centerPanel.add(depositButton, gbc);

        JButton transferButton = createMenuButton("Transfer Money", "🔄");
        transferButton.addActionListener(e -> cardLayout.show(mainContainer, "TRANSFER"));
        gbc.gridx = 1;
        centerPanel.add(transferButton, gbc);

        gbc.gridy = 3;
        JButton historyButton = createMenuButton("Transaction History","📄");
        historyButton.addActionListener(e -> cardLayout.show(mainContainer, "HISTORY"));
        gbc.gridx = 0;
        centerPanel.add(historyButton, gbc);

        JButton settingsButton = createMenuButton("Settings","settings");
        settingsButton.addActionListener(e -> cardLayout.show(mainContainer, "SETTINGS"));
        gbc.gridx = 1;
        centerPanel.add(settingsButton, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton exitButton = createStyledButton("EXIT", ERROR_COLOR);
        exitButton.addActionListener(e -> cardLayout.show(mainContainer, "WELCOME"));
        centerPanel.add(exitButton, gbc);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBalancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader("Account Balance");

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        JPanel cardPanel = createCard();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        JLabel balanceLabel = new JLabel(String.format("Rs %.2f", atm.getBalance()));
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 48));
        balanceLabel.setForeground(SUCCESS_COLOR);

        JLabel accountLabel = new JLabel(String.format("Account: %s", atm.getAccountNumber()));
        accountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dateLabel = new JLabel(String.format("As of: %s", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(Color.GRAY);

        JButton backButton = createStyledButton("BACK TO MENU", PRIMARY_COLOR);
        backButton.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(balanceLabel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(accountLabel);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(dateLabel);
        cardPanel.add(Box.createVerticalStrut(30));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        cardPanel.add(buttonPanel);

        centerPanel.add(cardPanel);
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createWithdrawPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader("Withdraw Money");

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        JPanel cardPanel = createCard();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        JLabel instructionLabel = new JLabel("Enter amount to withdraw (Rs):");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel limitLabel = new JLabel(String.format("Daily withdrawal limit: Rs %.2f", atm.getMaxWithdrawal()));
        limitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        limitLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        limitLabel.setForeground(Color.GRAY);

        JTextField amountField = new JTextField(15);
        amountField.setMaximumSize(new Dimension(200, 30));
        amountField.setHorizontalAlignment(SwingConstants.CENTER);

        JButton withdrawButton = createStyledButton("WITHDRAW", ERROR_COLOR);
        JButton backButton = createStyledButton("BACK", PRIMARY_COLOR);

        withdrawButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                TransactionResult result = atm.withdraw(amount);

                if (result.isSuccess()) {
                    lastTransaction = atm.getTransactionHistory().get(atm.getTransactionHistory().size() - 1);
                    int choice = JOptionPane.showOptionDialog(this,
                            String.format("%s\nNew balance: Rs %.2f\n\nWould you like to print a receipt?",
                                    result.getMessage(), atm.getBalance()),
                            "Transaction Successful",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new String[]{"Print Receipt", "No Thanks"},
                            "Print Receipt");

                    if (choice == 0) {
                        cardLayout.show(mainContainer, "RECEIPT");
                    }
                    amountField.setText("");
                } else {
                    showMessage(result.getMessage(), "Transaction Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                showMessage("Please enter a valid numeric amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(instructionLabel);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(limitLabel);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(amountField);
        cardPanel.add(Box.createVerticalStrut(30));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        buttonPanel.add(withdrawButton);
        cardPanel.add(buttonPanel);

        centerPanel.add(cardPanel);
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDepositPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader("Deposit Money");

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        JPanel cardPanel = createCard();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        JLabel instructionLabel = new JLabel("Enter amount to deposit (Rs):");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel limitLabel = new JLabel(String.format("Daily deposit limit: Rs %.2f", atm.getMaxDeposit()));
        limitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        limitLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        limitLabel.setForeground(Color.GRAY);

        JTextField amountField = new JTextField(15);
        amountField.setMaximumSize(new Dimension(200, 30));
        amountField.setHorizontalAlignment(SwingConstants.CENTER);

        JButton depositButton = createStyledButton("DEPOSIT", SUCCESS_COLOR);
        JButton backButton = createStyledButton("BACK", PRIMARY_COLOR);

        depositButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                TransactionResult result = atm.deposit(amount);

                if (result.isSuccess()) {
                    lastTransaction = atm.getTransactionHistory().get(atm.getTransactionHistory().size() - 1);
                    int choice = JOptionPane.showOptionDialog(this,
                            String.format("%s\nNew balance: Rs %.2f\n\nWould you like to print a receipt?",
                                    result.getMessage(), atm.getBalance()),
                            "Transaction Successful",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new String[]{"Print Receipt", "No Thanks"},
                            "Print Receipt");

                    if (choice == 0) {
                        cardLayout.show(mainContainer, "RECEIPT");
                    }
                    amountField.setText("");
                } else {
                    showMessage(result.getMessage(), "Transaction Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                showMessage("Please enter a valid numeric amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(instructionLabel);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(limitLabel);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(amountField);
        cardPanel.add(Box.createVerticalStrut(30));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        buttonPanel.add(depositButton);
        cardPanel.add(buttonPanel);

        centerPanel.add(cardPanel);
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader("Transfer Money");

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        JPanel cardPanel = createCard();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        JLabel instructionLabel1 = new JLabel("Enter target account number:");
        instructionLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField accountField = new JTextField(15);
        accountField.setMaximumSize(new Dimension(200, 30));
        accountField.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel instructionLabel2 = new JLabel("Enter amount to transfer (Rs):");
        instructionLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel limitLabel = new JLabel(String.format("Daily transfer limit: Rs %.2f", atm.getMaxTransfer()));
        limitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        limitLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        limitLabel.setForeground(Color.GRAY);

        JTextField amountField = new JTextField(15);
        amountField.setMaximumSize(new Dimension(200, 30));
        amountField.setHorizontalAlignment(SwingConstants.CENTER);

        JButton transferButton = createStyledButton("TRANSFER", SECONDARY_COLOR);
        JButton backButton = createStyledButton("BACK", PRIMARY_COLOR);

        transferButton.addActionListener(e -> {
            try {
                String targetAccount = accountField.getText().trim();
                double amount = Double.parseDouble(amountField.getText());
                TransactionResult result = atm.transfer(amount, targetAccount);

                if (result.isSuccess()) {
                    lastTransaction = atm.getTransactionHistory().get(atm.getTransactionHistory().size() - 1);
                    int choice = JOptionPane.showOptionDialog(this,
                            String.format("%s\nNew balance: Rs %.2f\n\nWould you like to print a receipt?",
                                    result.getMessage(), atm.getBalance()),
                            "Transaction Successful",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new String[]{"Print Receipt", "No Thanks"},
                            "Print Receipt");

                    if (choice == 0) {
                        cardLayout.show(mainContainer, "RECEIPT");
                    }
                    accountField.setText("");
                    amountField.setText("");
                } else {
                    showMessage(result.getMessage(), "Transaction Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                showMessage("Please enter a valid numeric amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(instructionLabel1);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(accountField);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(instructionLabel2);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(limitLabel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(amountField);
        cardPanel.add(Box.createVerticalStrut(30));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        buttonPanel.add(transferButton);
        cardPanel.add(buttonPanel);

        centerPanel.add(cardPanel);
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader("Transaction History");

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create table for transaction history
        String[] columnNames = {"Date/Time", "Type", "Amount", "Balance"};
        List<Transaction> history = atm.getTransactionHistory();

        Object[][] data = new Object[history.size()][4];
        for (int i = 0; i < history.size(); i++) {
            Transaction t = history.get(i);
            data[i][0] = t.getFormattedTimestamp();
            data[i][1] = t.getType();
            data[i][2] = t.getFormattedAmount();
            data[i][3] = t.getFormattedBalance();
        }

        JTable table = new JTable(data, columnNames);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(230, 240, 255));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 300));

        JButton backButton = createStyledButton("BACK TO MENU", PRIMARY_COLOR);
        backButton.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        if (history.isEmpty()) {
            JLabel noDataLabel = new JLabel("No transactions found", SwingConstants.CENTER);
            noDataLabel.setFont(new Font("Arial", Font.ITALIC, 16));
            centerPanel.add(noDataLabel, BorderLayout.CENTER);
        } else {
            centerPanel.add(scrollPane, BorderLayout.CENTER);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(backButton);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader("Settings");

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        JPanel cardPanel = createCard();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        JLabel instructionLabel = new JLabel("Change PIN");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JLabel oldPinLabel = new JLabel("Enter current PIN:");
        oldPinLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField oldPinField = new JPasswordField(4);
        oldPinField.setMaximumSize(new Dimension(150, 30));
        oldPinField.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel newPinLabel = new JLabel("Enter new PIN:");
        newPinLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField newPinField = new JPasswordField(4);
        newPinField.setMaximumSize(new Dimension(150, 30));
        newPinField.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel confirmPinLabel = new JLabel("Confirm new PIN:");
        confirmPinLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField confirmPinField = new JPasswordField(4);
        confirmPinField.setMaximumSize(new Dimension(150, 30));
        confirmPinField.setHorizontalAlignment(SwingConstants.CENTER);

        JButton changePinButton = createStyledButton("CHANGE PIN", SUCCESS_COLOR);
        JButton backButton = createStyledButton("BACK", PRIMARY_COLOR);

        changePinButton.addActionListener(e -> {
            String oldPin = new String(oldPinField.getPassword());
            String newPin = new String(newPinField.getPassword());
            String confirmPin = new String(confirmPinField.getPassword());

            if (newPin.length() != 4) {
                showMessage("PIN must be 4 digits long.", "Invalid PIN", JOptionPane.ERROR_MESSAGE);
            } else if (!newPin.equals(confirmPin)) {
                showMessage("New PIN and confirmation do not match.", "PIN Mismatch", JOptionPane.ERROR_MESSAGE);
            } else if (atm.changePin(oldPin, newPin)) {
                showMessage("PIN changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                oldPinField.setText("");
                newPinField.setText("");
                confirmPinField.setText("");
            } else {
                showMessage("Invalid current PIN.", "Error", JOptionPane.ERROR_MESSAGE);
                oldPinField.setText("");
                newPinField.setText("");
                confirmPinField.setText("");
            }
        });

        backButton.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(instructionLabel);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(oldPinLabel);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(oldPinField);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(newPinLabel);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(newPinField);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(confirmPinLabel);
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(confirmPinField);
        cardPanel.add(Box.createVerticalStrut(30));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        buttonPanel.add(changePinButton);
        cardPanel.add(buttonPanel);

        centerPanel.add(cardPanel);
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeader("Transaction Receipt");

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        JPanel receiptPanel = createCard();
        receiptPanel.setLayout(new BorderLayout());
        receiptPanel.setPreferredSize(new Dimension(400, 500));

        // Receipt content
        JTextArea receiptText = new JTextArea();
        receiptText.setEditable(false);
        receiptText.setFont(new Font("Courier New", Font.PLAIN, 12));
        receiptText.setBorder(new EmptyBorder(20, 20, 20, 20));

        if (lastTransaction != null) {
            StringBuilder receipt = new StringBuilder();
            receipt.append("===============================\n");
            receipt.append("        SECUREBANK ATM\n");
            receipt.append("     Transaction Receipt\n");
            receipt.append("===============================\n\n");
            receipt.append("Date/Time: ").append(lastTransaction.getFormattedTimestamp()).append("\n");
            receipt.append("Account: ").append(atm.getAccountNumber()).append("\n");
            receipt.append("Account Holder: ").append(atm.getAccountHolderName()).append("\n\n");
            receipt.append("Transaction Type: ").append(lastTransaction.getType()).append("\n");
            receipt.append("Amount: ").append(lastTransaction.getFormattedAmount()).append("\n");
            receipt.append("Balance After: ").append(lastTransaction.getFormattedBalance()).append("\n\n");
            receipt.append("===============================\n");
            receipt.append("   Thank you for banking with us!\n");
            receipt.append("===============================");

            receiptText.setText(receipt.toString());
        } else {
            receiptText.setText("No recent transaction found.");
        }

        JScrollPane scrollPane = new JScrollPane(receiptText);
        receiptPanel.add(scrollPane, BorderLayout.CENTER);

        JButton printButton = createStyledButton("PRINT", SUCCESS_COLOR);
        JButton saveButton = createStyledButton("SAVE AS TEXT", SECONDARY_COLOR);
        JButton backButton = createStyledButton("BACK TO MENU", PRIMARY_COLOR);

        printButton.addActionListener(e -> {
            // Simulate printing
            showMessage("Receipt sent to printer!", "Print Success", JOptionPane.INFORMATION_MESSAGE);
        });

        saveButton.addActionListener(e -> {
            // Simulate saving
            showMessage("Receipt saved as receipt.txt", "Save Success", JOptionPane.INFORMATION_MESSAGE);
        });

        backButton.addActionListener(e -> cardLayout.show(mainContainer, "MENU"));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(printButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(backButton);

        centerPanel.add(receiptPanel);
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHeader(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        return headerPanel;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JButton createMenuButton(String text, String emoji) {
        JButton button = new JButton("<html><center>" + emoji + "<br>" + text + "</center></html>");
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(CARD_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                new EmptyBorder(20, 20, 20, 20)
        ));
        button.setPreferredSize(new Dimension(200, 100));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(CARD_COLOR);
                button.setForeground(Color.BLACK);
            }
        });

        return button;
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ATMApplication().setVisible(true);
        });
    }
}