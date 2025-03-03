package com.example.sdaassign4_2022;

import java.time.LocalDateTime;
import java.util.Date;


// this class represent a book
public class Book {
    public String Title;
    public String Author;
    public String Url;
    public String ID;

    public LocalDateTime DateLastBorrow;
    boolean Available;
    public Book(String title, String author,  String url, boolean available, LocalDateTime dateLastBorrow, String id) {
        Title = title;
        ID = id;
        Author = author;
        Url = url;
        Available = available;
        DateLastBorrow =  dateLastBorrow;
    }
}
