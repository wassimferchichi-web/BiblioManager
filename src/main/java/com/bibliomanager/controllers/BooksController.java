package com.bibliomanager.controllers;

import com.bibliomanager.dao.BookDAO;
import com.bibliomanager.models.Book;
import com.bibliomanager.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BooksController {

    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String>  colTitle;
    @FXML private TableColumn<Book, String>  colAuthor;
    @FXML private TableColumn<Book, String>  colIsbn;
    @FXML private TableColumn<Book, String>  colCategory;
    @FXML private TableColumn<Book, Integer> colTotal;
    @FXML private TableColumn<Book, Integer> colAvail;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label loggedUserLabel;

    private BookDAO bookDAO = new BookDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));
        colAvail.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        loadBooks();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loggedUserLabel.setText("👤 " + user.getUsername() + " (" + user.getRole() + ")");
    }

    private void loadBooks() {
        List<Book> books = bookDAO.getAllBooks();
        ObservableList<Book> data = FXCollections.observableArrayList(books);
        booksTable.setItems(data);
        statusLabel.setText(books.size() + " book(s) found");
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        List<Book> books = keyword.isEmpty()
                ? bookDAO.getAllBooks()
                : bookDAO.searchBooks(keyword);
        booksTable.setItems(FXCollections.observableArrayList(books));
        statusLabel.setText(books.size() + " book(s) found");
    }

    @FXML
    private void handleAddBook() {
        Dialog<Book> dialog = createBookDialog(null);
        Optional<Book> result = dialog.showAndWait();
        result.ifPresent(book -> {
            boolean success = bookDAO.addBook(
                    book.getTitle(), book.getAuthor(),
                    book.getIsbn(), book.getTotalCopies(),
                    book.getId()
            );
            if (success) {
                loadBooks();
                statusLabel.setText("✅ Book added successfully!");
            }
        });
    }

    @FXML
    private void handleEditBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠️ Please select a book first.");
            return;
        }
        Dialog<Book> dialog = createBookDialog(selected);
        Optional<Book> result = dialog.showAndWait();
        result.ifPresent(book -> {
            boolean success = bookDAO.updateBook(
                    selected.getId(), book.getTitle(),
                    book.getAuthor(), book.getIsbn(),
                    book.getTotalCopies(), book.getId()
            );
            if (success) {
                loadBooks();
                statusLabel.setText("✅ Book updated successfully!");
            }
        });
    }

    @FXML
    private void handleDeleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("⚠️ Please select a book first.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Book");
        alert.setHeaderText("Delete \"" + selected.getTitle() + "\"?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            bookDAO.deleteBook(selected.getId());
            loadBooks();
            statusLabel.setText("✅ Book deleted.");
        }
    }

    private Dialog<Book> createBookDialog(Book existing) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Book" : "Edit Book");
        dialog.setHeaderText(existing == null ? "Enter book details" : "Edit book details");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField titleF  = new TextField(existing != null ? existing.getTitle() : "");
        TextField authorF = new TextField(existing != null ? existing.getAuthor() : "");
        TextField isbnF   = new TextField(existing != null ? existing.getIsbn() : "");
        TextField copiesF = new TextField(existing != null
                ? String.valueOf(existing.getTotalCopies()) : "1");

        titleF.setPromptText("Book title");
        authorF.setPromptText("Author name");
        isbnF.setPromptText("ISBN");
        copiesF.setPromptText("Number of copies");

        // Categories ComboBox
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Select category");
        categoryBox.setPrefWidth(200);

        Map<Integer, String> categoriesMap = bookDAO.getCategoriesMap();
        categoryBox.getItems().addAll(categoriesMap.values());

        if (existing != null && existing.getCategory() != null) {
            categoryBox.setValue(existing.getCategory());
        }

        grid.add(new Label("Title:"),    0, 0); grid.add(titleF,      1, 0);
        grid.add(new Label("Author:"),   0, 1); grid.add(authorF,     1, 1);
        grid.add(new Label("ISBN:"),     0, 2); grid.add(isbnF,       1, 2);
        grid.add(new Label("Category:"), 0, 3); grid.add(categoryBox, 1, 3);
        grid.add(new Label("Copies:"),   0, 4); grid.add(copiesF,     1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    int copies = Integer.parseInt(copiesF.getText().trim());
                    String selectedCategory = categoryBox.getValue();

                    // Find categoryId
                    int categoryId = 0;
                    if (selectedCategory != null) {
                        for (Map.Entry<Integer, String> entry : categoriesMap.entrySet()) {
                            if (entry.getValue().equals(selectedCategory)) {
                                categoryId = entry.getKey();
                                break;
                            }
                        }
                    }

                    // Store categoryId temporarily in id field
                    Book book = new Book(
                            categoryId,
                            titleF.getText().trim(),
                            authorF.getText().trim(),
                            isbnF.getText().trim(),
                            selectedCategory != null ? selectedCategory : "",
                            copies,
                            copies
                    );
                    return book;
                } catch (NumberFormatException e) {
                    return null;
                }
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
            Stage stage = (Stage) booksTable.getScene().getWindow();
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
            Stage stage = (Stage) booksTable.getScene().getWindow();
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
            Stage stage = (Stage) booksTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            Stage stage = (Stage) booksTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setWidth(500);
            stage.setHeight(600);
        } catch (Exception e) { e.printStackTrace(); }
    }
}