package com.edu.educourse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edu.educourse.entity.Course;
import com.edu.educourse.entity.Purchase;
import com.edu.educourse.entity.User;

/**
 * PurchaseRepository — queries for the Purchase table.
 *
 * findByUser()          → get all purchases for a user (My Courses page)
 * findByUserAndCourse() → check if already purchased before buying again
 */
@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByUser(User user);

    Optional<Purchase> findByUserAndCourse(User user, Course course);
}
