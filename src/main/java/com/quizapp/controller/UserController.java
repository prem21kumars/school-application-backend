package com.quizapp.controller;

import com.quizapp.entity.UserEntity;
import com.quizapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers(@RequestParam(required = false) String role) {
        return ResponseEntity.ok(userService.getAllUsers(role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserEntity> createUser(@RequestBody UserEntity user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable String id, @RequestBody UserEntity user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserEntity> toggleStatus(@PathVariable String id) {
        return ResponseEntity.ok(userService.toggleStatus(id));
    }

    @PostMapping("/{teacherId}/assignments")
    public ResponseEntity<UserEntity> assignTeacherClassesAndSubjects(
            @PathVariable String teacherId,
            @RequestBody Map<String, List<String>> payload) {
        List<String> classIds = payload.get("classIds");
        List<String> subjectIds = payload.get("subjectIds");
        return ResponseEntity.ok(userService.assignTeacherClassesAndSubjects(teacherId, classIds, subjectIds));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String id, @RequestBody Map<String, String> body) {
        String newPassword = body != null ? body.get("newPassword") : null;
        if (newPassword == null || newPassword.trim().length() < 3) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Password must be at least 3 characters long."));
        }
        UserEntity updated = userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(Map.of("success", true, "message", "Password updated successfully for " + updated.getName(), "user", updated));
    }
}
