package com.quizapp.controller;

import com.quizapp.entity.BookSourceEntity;
import com.quizapp.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookSourceEntity>> getAllBooks(@RequestParam(required = false) String subjectId) {
        return ResponseEntity.ok(bookService.getAllBooks(subjectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookSourceEntity> getBookById(@PathVariable String id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookSourceEntity> createBook(@RequestBody BookSourceEntity book) {
        return ResponseEntity.ok(bookService.createBook(book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
