package com.my.hangzhang.ebook.mode;

/**
 * Created by hangzhang209526 on 2016/1/19.
 */
public class Book {
    /**ID*/
    private long id;
    /**书籍名称*/
    private String bookName;
    /**书籍作者*/
    private String author;
    /**书籍文件路径*/
    private String path;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
