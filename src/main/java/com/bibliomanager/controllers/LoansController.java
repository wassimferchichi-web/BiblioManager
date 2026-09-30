package com.bibliomanager.controllers;

import com.bibliomanager.dao.LoanDAO;
import com.bibliomanager.models.Loan;
import com.bibliomanager.models.User;
import com.bibliomanager.utils.PDFExporter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LoansController {

    @FXML private TableView<Loan> loansTable;
    @FXML private TableColumn<Loan, String>    colBook;
    @FXML private TableColumn<Loan, String>    colMember;
    @FXML private TableColumn<Loan, LocalDate> colLoan;
    @FXML private TableColumn<Loan, LocalDate> colDue;
    @FXML private TableColumn<Loan, LocalDate> colReturn;
    @FXML private TableColumn<Loan, String>    colStatus;
    @FXML private Label statusLabel;
    @FXML private Label loggedUserLabel;

    private LoanDAO loanDAO = new LoanDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        colBook.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        colMember.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        colLoan.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        colDue.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        colReturn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Color rows by status
        loansTable.setRowFactory(tv -> new TableRow<Loan>() {
            @Override
            protected void updateItem(Loan loan, boolean empty) {
                super.updateItem(loan, empty);
                if (loan == null || empty) {
                    setStyle("");
                } else if ("OVERDUE".equals(loan.getStatus())) {
                    setStyle("-fx-background-color: #3d1a1a;");
                } else if ("RETURNED".equals(loan.getStatus())) {
                    setStyle("-fx-background-color: #1a3d1a;");
                } else {
                    setStyle("");
                }
            }
        });

        loanDAO.updateOverdueLoans();
        loadLoans();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loggedUserLabel.setText("👤 " + user.getUsername() + " (" + user.getRole() + ")");
    }

    private void loadLoans() {
        List<Loan> loans = loanDAO.getAllLoans();
        loansTable.setItems(FXCollections.observableArrayList(loans));
        statusLabel.setText(loans.size() + " loan(s) total");
    }

    @FXML
    private void handleIssueLoan() {
        Map<Integer, String> books   = loanDAO.getAvailableBooks();
        Map<Integer, String> members = loanDAO.getAllMembersMap();

        if (books.isEmpty()) {
            statusLabel.setText("⚠️ No books available.");
            return;
        }
        if (members.isEmpty()) {
            statusLabel.setText("⚠️ No members registered.");
            return;
        }

        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Issue Loan");
        dialog.setHeaderText("Select book, member and due date");

        ButtonType issueBtn = new ButtonType("Issue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(issueBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        ComboBox<String> bookBox = new ComboBox<>(
                FXCollections.observableArrayList(books.values())
        );
        ComboBox<String> memberBox = new ComboBox<>(
                FXCollections.observableArrayList(members.values())
        );
        DatePicker duePicker = new DatePicker(LocalDate.now().plusDays(14));

        bookBox.setPromptText("Select a book");
        memberBox.setPromptText("Select a member");
        bookBox.setPrefWidth(220);
        memberBox.setPrefWidth(220);

        grid.add(new Label("Book:"),     0, 0); grid.add(bookBox,   1, 0);
        grid.add(new Label("Member:"),   0, 1); grid.add(memberBox, 1, 1);
        grid.add(new Label("Due Date:"), 0, 2); grid.add(duePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == issueBtn && bookBox.getValue() != null && memberBox.getValue() != null) {
                int bookId = books.entrySet().stream()
                        .filter(e -> e.getValue().equals(bookBox.getValue()))
                        .findFirst().get().getKey();
                int memberId = members.entrySet().stream()
                        .filter(e -> e.getValue().equals(memberBox.getValue()))
                        .findFirst().get().getKey();
                return new int[]{bookId, memberId};
            }
            return null;
        });

        Optional<int[]> result = dialog.showAndWait();
        result.ifPresent(ids -> {
            boolean success = loanDAO.issueLoan(ids[0], ids[1], duePicker.getValue());
            if (success) {
                loadLoans();
                statusLabel.setText("✅ Loan issued successfully!");
            }
        });
    }

    @FXML
    private void handleReturnBook() {
        Loan selected = loansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠️ Please select a loan first.");
            return;
        }
        if ("RETURNED".equals(selected.getStatus())) {
            statusLabel.setText("⚠️ This book is already returned.");
            return;
        }
        boolean success = loanDAO.returnBook(selected.getId(), selected.getBookId());
        if (success) {
            loadLoans();
            statusLabel.setText("✅ Book returned successfully!");
        }
    }

    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.setInitialFileName("loans_report.pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        File file = fileChooser.showSaveDialog(loansTable.getScene().getWindow());
        if (file != null) {
            String path = PDFExporter.exportLoansReport(
                    loanDAO.getAllLoans(), file.getAbsolutePath()
            );
            if (path != null) {
                statusLabel.setText("✅ PDF exported: " + file.getName());
            } else {
                statusLabel.setText("❌ Export failed.");
            }
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            DashboardController controller = loader.getController();
            if (currentUser != null) controller.setUser(currentUser);
            Stage stage = (Stage) loansTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToBooks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/books.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            BooksController controller = loader.getController();
            if (currentUser != null) controller.setUser(currentUser);
            Stage stage = (Stage) loansTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToMembers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/members.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            MembersController controller = loader.getController();
            if (currentUser != null) controller.setUser(currentUser);
            Stage stage = (Stage) loansTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            Stage stage = (Stage) loansTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(500);
            stage.setHeight(600);
        } catch (Exception e) { e.printStackTrace(); }
    }
}