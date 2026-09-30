package com.bibliomanager.models;

import java.time.LocalDate;

public class Loan {

    private int id;
    private String bookTitle;
    private String memberName;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status;
    private int bookId;
    private int memberId;

    public Loan(int id, String bookTitle, String memberName,
                LocalDate loanDate, LocalDate dueDate,
                LocalDate returnDate, String status,
                int bookId, int memberId) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.memberName = memberName;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.bookId = bookId;
        this.memberId = memberId;
    }

    public int getId()               { return id; }
    public String getBookTitle()     { return bookTitle; }
    public String getMemberName()    { return memberName; }
    public LocalDate getLoanDate()   { return loanDate; }
    public LocalDate getDueDate()    { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public String getStatus()        { return status; }
    public int getBookId()           { return bookId; }
    public int getMemberId()         { return memberId; }

    public void setStatus(String status) { this.status = status; }
}