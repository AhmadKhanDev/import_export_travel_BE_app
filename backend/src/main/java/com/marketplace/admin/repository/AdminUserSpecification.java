package com.marketplace.admin.repository;

import com.marketplace.admin.dto.AdminUserFilter;
import com.marketplace.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class AdminUserSpecification {

    private AdminUserSpecification() {
    }

    public static Specification<User> withFilter(AdminUserFilter filter) {
        return Specification.where(byRole(filter.getRole()))
                .and(byAccountStatus(filter.getAccountStatus()))
                .and(byEmail(filter.getEmail()))
                .and(byFullName(filter.getFullName()))
                .and(byPhoneNumber(filter.getPhoneNumber()))
                .and(byProfileCompleted(filter.getProfileCompleted()));
    }

    private static Specification<User> byRole(com.marketplace.user.entity.Role role) {
        if (role == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    private static Specification<User> byAccountStatus(com.marketplace.user.entity.AccountStatus accountStatus) {
        if (accountStatus == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("accountStatus"), accountStatus);
    }

    private static Specification<User> byEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase().trim() + "%");
    }

    private static Specification<User> byFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("fullName")), "%" + fullName.toLowerCase().trim() + "%");
    }

    private static Specification<User> byPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(root.get("phoneNumber"), "%" + phoneNumber.trim() + "%");
    }

    private static Specification<User> byProfileCompleted(Boolean profileCompleted) {
        if (profileCompleted == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("profileCompleted"), profileCompleted);
    }
}
