package com.bibliomanager.controllers;

import com.bibliomanager.dao.MemberDAO;
import com.bibliomanager.models.Member;
import com.bibliomanager.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class MembersController {

    @FXML private TableView<Member> membersTable;
    @FXML private TableColumn<Member, String> colName;
    @FXML private TableColumn<Member, String> colEmail;
    @FXML private TableColumn<Member, String> colPhone;
    @FXML private TableColumn<Member, String> colSince;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label loggedUserLabel;

    private MemberDAO memberDAO = new MemberDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colSince.setCellValueFactory(new PropertyValueFactory<>("memberSince"));
        loadMembers();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loggedUserLabel.setText("👤 " + user.getUsername() + " (" + user.getRole() + ")");
    }

    private void loadMembers() {
        List<Member> members = memberDAO.getAllMembers();
        ObservableList<Member> data = FXCollections.observableArrayList(members);
        membersTable.setItems(data);
        statusLabel.setText(members.size() + " member(s) found");
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        List<Member> members = keyword.isEmpty()
                ? memberDAO.getAllMembers()
                : memberDAO.searchMembers(keyword);
        membersTable.setItems(FXCollections.observableArrayList(members));
        statusLabel.setText(members.size() + " member(s) found");
    }

    @FXML
    private void handleAddMember() {
        Dialog<Member> dialog = createMemberDialog(null);
        Optional<Member> result = dialog.showAndWait();
        result.ifPresent(member -> {
            boolean success = memberDAO.addMember(
                    member.getFullName(), member.getEmail(), member.getPhone()
            );
            if (success) {
                loadMembers();
                statusLabel.setText("✅ Member added successfully!");
            }
        });
    }

    @FXML
    private void handleEditMember() {
        Member selected = membersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠️ Please select a member first.");
            return;
        }
        Dialog<Member> dialog = createMemberDialog(selected);
        Optional<Member> result = dialog.showAndWait();
        result.ifPresent(member -> {
            boolean success = memberDAO.updateMember(
                    selected.getId(), member.getFullName(),
                    member.getEmail(), member.getPhone()
            );
            if (success) {
                loadMembers();
                statusLabel.setText("✅ Member updated successfully!");
            }
        });
    }

    @FXML
    private void handleDeleteMember() {
        Member selected = membersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠️ Please select a member first.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Member");
        alert.setHeaderText("Delete \"" + selected.getFullName() + "\"?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            memberDAO.deleteMember(selected.getId());
            loadMembers();
            statusLabel.setText("✅ Member deleted.");
        }
    }

    private Dialog<Member> createMemberDialog(Member existing) {
        Dialog<Member> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Member" : "Edit Member");
        dialog.setHeaderText(existing == null ? "Enter member details" : "Edit member details");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField nameF  = new TextField(existing != null ? existing.getFullName() : "");
        TextField emailF = new TextField(existing != null ? existing.getEmail() : "");
        TextField phoneF = new TextField(existing != null ? existing.getPhone() : "");

        nameF.setPromptText("Full name");
        emailF.setPromptText("Email address");
        phoneF.setPromptText("Phone number");

        grid.add(new Label("Full Name:"), 0, 0); grid.add(nameF,  1, 0);
        grid.add(new Label("Email:"),     0, 1); grid.add(emailF, 1, 1);
        grid.add(new Label("Phone:"),     0, 2); grid.add(phoneF, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                return new Member(0, nameF.getText().trim(),
                        emailF.getText().trim(), phoneF.getText().trim(), null);
            }
            return null;
        });
        return dialog;
    }

    @FXML
    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            DashboardController controller = loader.getController();
            if (currentUser != null) controller.setUser(currentUser);
            Stage stage = (Stage) membersTable.getScene().getWindow();
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
            Stage stage = (Stage) membersTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToLoans() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/loans.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            LoansController controller = loader.getController();
            if (currentUser != null) controller.setUser(currentUser);
            Stage stage = (Stage) membersTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            Stage stage = (Stage) membersTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(500);
            stage.setHeight(600);
        } catch (Exception e) { e.printStackTrace(); }
    }
}