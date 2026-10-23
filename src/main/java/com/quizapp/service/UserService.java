package com.quizapp.service;

import com.quizapp.entity.UserEntity;
import com.quizapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserEntity> getAllUsers(String role) {
        if (role != null && !role.trim().isEmpty()) {
            return userRepository.findByRole(role.trim().toLowerCase());
        }
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(String id) {
        return userRepository.findById(id);
    }

    public UserEntity createUser(UserEntity user) {
        if (user.getId() == null || user.getId().trim().isEmpty()) {
            String prefix = "usr-" + (user.getRole() != null ? user.getRole().substring(0, Math.min(3, user.getRole().length())) : "gen");
            user.setId(prefix + "-" + System.currentTimeMillis());
        }
        if (user.getAvatar() == null || user.getAvatar().trim().isEmpty()) {
            String seed = user.getEnrolmentNumber() != null ? user.getEnrolmentNumber() : user.getName();
            user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed);
        }
        if (user.getStatus() == null) {
            user.setStatus("active");
        }
        return userRepository.save(user);
    }

    public UserEntity updateUser(String id, UserEntity updated) {
        return userRepository.findById(id).map(user -> {
            if (updated.getName() != null) user.setName(updated.getName());
            if (updated.getEmail() != null) user.setEmail(updated.getEmail());
            if (updated.getAvatar() != null) user.setAvatar(updated.getAvatar());
            if (updated.getEnrolmentNumber() != null) user.setEnrolmentNumber(updated.getEnrolmentNumber());
            if (updated.getClassId() != null) user.setClassId(updated.getClassId());
            if (updated.getSection() != null) user.setSection(updated.getSection());
            if (updated.getStatus() != null) user.setStatus(updated.getStatus());
            if (updated.getPassword() != null) user.setPassword(updated.getPassword());
            if (updated.getAssignedClassIds() != null) user.setAssignedClassIds(updated.getAssignedClassIds());
            if (updated.getAssignedSubjectIds() != null) user.setAssignedSubjectIds(updated.getAssignedSubjectIds());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public UserEntity toggleStatus(String id) {
        return userRepository.findById(id).map(user -> {
            String newStatus = "active".equalsIgnoreCase(user.getStatus()) ? "inactive" : "active";
            user.setStatus(newStatus);
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public UserEntity resetPassword(String id, String newPassword) {
        return userRepository.findById(id).map(user -> {
            user.setPassword(newPassword.trim());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public UserEntity assignTeacherClassesAndSubjects(String teacherId, List<String> classIds, List<String> subjectIds) {
        return userRepository.findById(teacherId).map(user -> {
            if (classIds != null) user.setAssignedClassIds(classIds);
            if (subjectIds != null) user.setAssignedSubjectIds(subjectIds);
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("Teacher not found with id: " + teacherId));
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
