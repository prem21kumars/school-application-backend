package com.quizapp.service;

import com.quizapp.entity.BookSourceEntity;
import com.quizapp.repository.BookSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookSourceRepository bookSourceRepository;

    @Autowired
    public BookService(BookSourceRepository bookSourceRepository) {
        this.bookSourceRepository = bookSourceRepository;
    }

    public List<BookSourceEntity> getAllBooks(String subjectId) {
        if (subjectId != null && !subjectId.trim().isEmpty()) {
            return bookSourceRepository.findBySubjectId(subjectId);
        }
        return bookSourceRepository.findAll();
    }

    public Optional<BookSourceEntity> getBookById(String id) {
        return bookSourceRepository.findById(id);
    }

    public BookSourceEntity createBook(BookSourceEntity book) {
        if (book.getId() == null || book.getId().trim().isEmpty()) {
            book.setId("bk-" + System.currentTimeMillis());
        }
        return bookSourceRepository.save(book);
    }

    public void deleteBook(String id) {
        bookSourceRepository.deleteById(id);
    }
}
