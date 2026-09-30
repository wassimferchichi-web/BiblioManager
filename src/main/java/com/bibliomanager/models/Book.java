package com.bibliomanager.models;

public class Book {

    private int id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String title, String author, String isbn,
                String category, int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public int getId()                  { return id; }
    public String getTitle()            { return title; }
    public String getAuthor()           { return author; }
    public String getIsbn()             { return isbn; }
    public String getCategory()         { return category; }
    public int getTotalCopies()         { return totalCopies; }
    public int getAvailableCopies()     { return availableCopies; }

    public void setId(int id)                          { this.id = id; }
    public void setTitle(String title)                 { this.title = title; }
    public void setAuthor(String author)               { this.author = author; }
    public void setIsbn(String isbn)                   { this.isbn = isbn; }
    public void setCategory(String category)           { this.category = category; }
    public void setTotalCopies(int totalCopies)        { this.totalCopies = totalCopies; }
    public void setAvailableCopies(int availableCopies){ this.availableCopies = availableCopies; }
}