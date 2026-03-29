package com.edu.educourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================
 *  EduCourseApplication — Main Entry Point
 * ============================================================
 *
 *  This is the starting point of the entire Spring Boot application.
 *
 *  @SpringBootApplication is a convenience annotation that combines:
 *
 *    1. @Configuration
 *       Marks this class as a source of Spring bean definitions.
 *
 *    2. @EnableAutoConfiguration
 *       Tells Spring Boot to automatically configure the application
 *       based on the dependencies present on the classpath.
 *       For example:
 *         - H2 on classpath → auto-configures in-memory datasource
 *         - Spring Security on classpath → auto-enables security
 *         - Spring Data JPA on classpath → auto-configures EntityManager
 *
 *    3. @ComponentScan
 *       Scans this package and all sub-packages for Spring-managed
 *       components: @Component, @Service, @Repository, @Controller, etc.
 *
 *  How Spring Boot starts:
 *    SpringApplication.run() does the following:
 *      1. Creates the Spring ApplicationContext (IoC container)
 *      2. Registers all beans found via component scanning
 *      3. Runs auto-configuration
 *      4. Starts the embedded Tomcat server on port 8080
 *      5. Executes data.sql to seed the database
 *
 *  After startup, the API is available at: http://localhost:8080
 * ============================================================
 */
@SpringBootApplication
public class EduCourseApplication {

    public static void main(String[] args) {
        /*
         * SpringApplication.run() bootstraps the application.
         *
         * Parameters:
         *   - EduCourseApplication.class → the primary configuration class
         *   - args → command-line arguments (e.g., --server.port=9090)
         */
        SpringApplication.run(EduCourseApplication.class, args);
    }
}
