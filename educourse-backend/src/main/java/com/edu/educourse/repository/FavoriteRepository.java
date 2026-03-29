package com.edu.educourse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edu.educourse.entity.Course;
import com.edu.educourse.entity.Favorite;
import com.edu.educourse.entity.User;

/**
 * FavoriteRepository — queries for the Favorite join table.
 *
 * findByUser()             → get all favorites for a user (for the favorites page)
 * findByUserAndCourse()    → check if a specific favorite exists (to toggle)
 * deleteByUserAndCourse()  → remove a favorite
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /* All favorites for a given user */
    List<Favorite> findByUser(User user);

    /* Find a specific user+course favorite record */
    Optional<Favorite> findByUserAndCourse(User user, Course course);

    /* Delete a specific favorite record */
    void deleteByUserAndCourse(User user, Course course);
}
