package com.edu.educourse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * CourseDto — Request and response object for Course.
 * createdByEmail is populated server-side on POST, returned in responses.
 */
public class CourseDto {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Instructor is required")
    private String instructor;

    @NotBlank(message = "Category is required")
    private String category;

    @Min(value = 0, message = "Price cannot be negative")
    private double price;

    private String duration;
    private String level;
    private String description;
    private String createdByEmail;   // who created this course

    // ── Constructors ──────────────────────────────────────────────

    public CourseDto() {}

    public CourseDto(Long id, String title, String instructor, String category,
                     double price, String duration, String level,
                     String description, String createdByEmail) {
        this.id             = id;
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
