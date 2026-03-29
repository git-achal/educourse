package com.edu.educourse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ============================================================
 *  Course — JPA Entity mapped to the "courses" table
 * ============================================================
 *
 *  createdByEmail tracks which user added the course.
 *  ROLE_STUDENT_ADMIN can only delete courses where
 *  createdByEmail matches their own email.
 *  ROLE_ADMIN can delete any course.
 * ============================================================
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String instructor;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private double price;

    private String duration;
    private String level;

    @Column(length = 1000)
    private String description;

    /**
     * Email of the user who created this course.
     * Set at course creation time.
     * Used to enforce ROLE_STUDENT_ADMIN can only delete their own courses.
     * Null for seeded courses (only admin can delete those).
     */
    @Column(name = "created_by_email")
    private String createdByEmail;

    // ── Constructors ──────────────────────────────────────────────

    public Course() {}

    public Course(String title, String instructor, String category,
                  double price, String duration, String level,
                  String description, String createdByEmail) {
        this.title          = title;
        this.instructor     = instructor;
        this.category       = category;
        this.price          = price;
        this.duration       = duration;
        this.level          = level;
        this.description    = description;
        this.createdByEmail = createdByEmail;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public String getTitle()                    { return title; }
    public void setTitle(String t)              { this.title = t; }

    public String getInstructor()               { return instructor; }
    public void setInstructor(String i)         { this.instructor = i; }

    public String getCategory()                 { return category; }
    public void setCategory(String c)           { this.category = c; }

    public double getPrice()                    { return price; }
    public void setPrice(double p)              { this.price = p; }

    public String getDuration()                 { return duration; }
    public void setDuration(String d)           { this.duration = d; }

    public String getLevel()                    { return level; }
    public void setLevel(String l)              { this.level = l; }

    public String getDescription()              { return description; }
    public void setDescription(String d)        { this.description = d; }

    public String getCreatedByEmail()           { return createdByEmail; }
    public void setCreatedByEmail(String e)     { this.createdByEmail = e; }
}
