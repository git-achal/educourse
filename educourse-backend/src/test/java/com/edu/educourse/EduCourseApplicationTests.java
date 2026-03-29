package com.edu.educourse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ============================================================
 *  EduCourseApplicationTests — Basic Application Context Test
 * ============================================================
 *
 *  @SpringBootTest:
 *    Loads the full Spring ApplicationContext for testing.
 *    Verifies that all beans are configured correctly and
 *    the application context starts without errors.
 *
 *  This test is the most basic sanity check:
 *    If any bean is misconfigured (e.g., missing dependency,
 *    circular reference, wrong annotation), this test will fail.
 * ============================================================
 */
@SpringBootTest
class EduCourseApplicationTests {

    /**
     * Verifies the Spring ApplicationContext loads successfully.
     *
     * If this test passes, it means:
     *  - All @Component, @Service, @Repository, @Controller beans are created
     *  - All @Autowired dependencies are resolved
     *  - SecurityConfig, JwtAuthFilter, etc. are configured correctly
     *  - The database connection and JPA configuration are valid
     */
    @Test
    void contextLoads() {
        // No assertions needed — the test passes if the context starts without errors
    }
}
