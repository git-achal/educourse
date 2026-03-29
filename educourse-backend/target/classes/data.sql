-- ============================================================
--  data.sql — Seed Data
-- ============================================================
--  Roles seeded here. Admin user "elkorf/123123" is created
--  by DataInitializer.java using passwordEncoder at runtime.
--
--  50 courses across 8 categories:
--    Programming (10), Web Dev (8), Data Science (8),
--    Database (6), DevOps (6), Mobile Dev (5),
--    Cloud (4), Cybersecurity (3)
-- ============================================================

-- ── Roles ─────────────────────────────────────────────────
INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_STUDENT');
INSERT INTO roles (id, name) VALUES (3, 'ROLE_STUDENT_ADMIN');
ALTER TABLE roles ALTER COLUMN id RESTART WITH 100;

-- ── 50 Courses ────────────────────────────────────────────

-- PROGRAMMING (10)
INSERT INTO courses (title, instructor, category, price, duration, level, description) VALUES
('Java for Beginners',            'Rajesh Kumar',  'Programming',    0,   '10 hours',  'Beginner',     'Start your programming journey with Java. Covers variables, loops, OOP, and exception handling.'),
('Advanced Java & Design Patterns','Amit Shah',    'Programming',    799, '22 hours',  'Advanced',     'Deep dive into design patterns, generics, streams, and concurrency in Java.'),
('Spring Boot Masterclass',       'Amit Shah',     'Programming',    499, '18 hours',  'Intermediate', 'Build production-ready REST APIs with Spring Boot, JPA, Security, and JWT.'),
('Python Crash Course',           'Priya Nair',    'Programming',    0,   '8 hours',   'Beginner',     'Learn Python from scratch. Variables, functions, lists, dictionaries, and file I/O.'),
('Python: Beyond the Basics',     'Priya Nair',    'Programming',    399, '14 hours',  'Intermediate', 'Decorators, generators, async/await, testing, and packaging Python applications.'),
('C Programming Fundamentals',    'Vikas Rao',     'Programming',    0,   '12 hours',  'Beginner',     'Understand memory management, pointers, structs, and system-level programming in C.'),
('Data Structures & Algorithms',  'Meera Iyer',    'Programming',    599, '20 hours',  'Intermediate', 'Master arrays, linked lists, trees, graphs, sorting, and Big-O complexity analysis.'),
('Kotlin for Java Developers',    'Sneha Patil',   'Programming',    299, '10 hours',  'Intermediate', 'Transition from Java to Kotlin. Null safety, coroutines, extension functions, and more.'),
('Go Language Essentials',        'Arun Mehta',    'Programming',    349, '9 hours',   'Intermediate', 'Learn Go (Golang) for high-performance backend development. Goroutines, channels, and HTTP servers.'),
('Competitive Programming',       'Rajesh Kumar',  'Programming',    499, '16 hours',  'Advanced',     'Solve real interview problems with dynamic programming, greedy algorithms, and graph theory.');

-- WEB DEV (8)
INSERT INTO courses (title, instructor, category, price, duration, level, description) VALUES
('HTML & CSS from Scratch',       'Pooja Singh',   'Web Dev',        0,   '6 hours',   'Beginner',     'Build your first website with HTML5 and CSS3. Flexbox, Grid, and responsive design.'),
('JavaScript Complete Guide',     'Sneha Patil',   'Web Dev',        399, '20 hours',  'Beginner',     'JavaScript fundamentals, DOM manipulation, events, fetch API, and ES6+ features.'),
('React JS for Beginners',        'Sneha Patil',   'Web Dev',        499, '14 hours',  'Intermediate', 'Build dynamic UIs with React. Components, hooks, state management, and React Router.'),
('Advanced React & Redux',        'Sneha Patil',   'Web Dev',        699, '18 hours',  'Advanced',     'Redux Toolkit, Context API, performance optimization, testing, and code splitting.'),
('Node.js & Express API',         'Arun Mehta',    'Web Dev',        449, '12 hours',  'Intermediate', 'Build scalable REST APIs with Node.js, Express, MongoDB, and JWT authentication.'),
('TypeScript for Developers',     'Amit Shah',     'Web Dev',        299, '8 hours',   'Intermediate', 'Add static typing to JavaScript. Interfaces, generics, decorators, and type utilities.'),
('Next.js Full Stack',            'Pooja Singh',   'Web Dev',        599, '16 hours',  'Advanced',     'Full-stack apps with Next.js 14. Server components, API routes, SSR, and deployment.'),
('Web Performance Optimization',  'Vikas Rao',     'Web Dev',        349, '7 hours',   'Advanced',     'Core Web Vitals, lazy loading, caching strategies, bundle optimization, and CDN setup.');

-- DATA SCIENCE (8)
INSERT INTO courses (title, instructor, category, price, duration, level, description) VALUES
('Python for Data Analysis',      'Priya Nair',    'Data Science',   299, '14 hours',  'Beginner',     'Data analysis with Python, Pandas, NumPy, and Matplotlib. From raw CSV to insights.'),
('Machine Learning A-Z',          'Meera Iyer',    'Data Science',   799, '25 hours',  'Advanced',     'Supervised, unsupervised, and reinforcement learning with Scikit-learn and TensorFlow.'),
('Deep Learning with PyTorch',    'Meera Iyer',    'Data Science',   899, '22 hours',  'Advanced',     'Neural networks, CNNs, RNNs, transformers, and model deployment with PyTorch.'),
('Data Visualization Mastery',    'Priya Nair',    'Data Science',   249, '8 hours',   'Intermediate', 'Create compelling charts with Matplotlib, Seaborn, and Plotly. Dashboard design principles.'),
('SQL for Data Analysis',         'Vikas Rao',     'Data Science',   199, '6 hours',   'Beginner',     'Write advanced SQL for analytics. Window functions, CTEs, subqueries, and aggregations.'),
('Statistics for Data Science',   'Meera Iyer',    'Data Science',   299, '10 hours',  'Beginner',     'Probability, distributions, hypothesis testing, regression, and A/B testing fundamentals.'),
('Natural Language Processing',   'Meera Iyer',    'Data Science',   699, '16 hours',  'Advanced',     'Text classification, sentiment analysis, named entity recognition, and transformer models.'),
('Big Data with Apache Spark',    'Arun Mehta',    'Data Science',   799, '18 hours',  'Advanced',     'Process large datasets with PySpark. DataFrames, Spark SQL, streaming, and MLlib.');

-- DATABASE (6)
INSERT INTO courses (title, instructor, category, price, duration, level, description) VALUES
('MySQL for Developers',          'Vikas Rao',     'Database',       0,   '6 hours',   'Beginner',     'Master MySQL: schema design, joins, indexes, stored procedures, and query optimization.'),
('PostgreSQL Advanced',           'Vikas Rao',     'Database',       399, '12 hours',  'Advanced',     'Advanced PostgreSQL: partitioning, full-text search, JSON support, and performance tuning.'),
('MongoDB Complete Guide',        'Arun Mehta',    'Database',       349, '10 hours',  'Intermediate', 'Document-oriented database design, aggregation pipeline, indexing, and Atlas deployment.'),
('Redis for Backend Developers',  'Amit Shah',     'Database',       299, '7 hours',   'Intermediate', 'Caching strategies, pub/sub messaging, rate limiting, and session management with Redis.'),
('Database Design & Modeling',    'Vikas Rao',     'Database',       249, '8 hours',   'Beginner',     'ER diagrams, normalization, relationships, and translating business rules into schemas.'),
('Elasticsearch in Practice',     'Arun Mehta',    'Database',       449, '9 hours',   'Advanced',     'Full-text search, mappings, aggregations, relevance scoring, and ELK stack integration.');

-- DEVOPS (6)
INSERT INTO courses (title, instructor, category, price, duration, level, description) VALUES
('Docker from Zero to Hero',      'Arun Mehta',    'DevOps',         399, '10 hours',  'Beginner',     'Containerize any application with Docker. Images, volumes, networking, and Docker Compose.'),
('Kubernetes in Production',      'Arun Mehta',    'DevOps',         699, '16 hours',  'Advanced',     'Deploy and manage containerized workloads. Pods, services, Helm charts, and auto-scaling.'),
('CI/CD with GitHub Actions',     'Amit Shah',     'DevOps',         299, '8 hours',   'Intermediate', 'Automate build, test, and deploy pipelines. Workflows, secrets, matrix builds, and environments.'),
('Linux for Developers',          'Vikas Rao',     'DevOps',         0,   '7 hours',   'Beginner',     'Essential Linux commands, bash scripting, file permissions, cron jobs, and process management.'),
('Terraform & Infrastructure',    'Arun Mehta',    'DevOps',         599, '14 hours',  'Advanced',     'Infrastructure as code with Terraform. Modules, state management, workspaces, and multi-cloud.'),
('Monitoring with Prometheus',    'Amit Shah',     'DevOps',         349, '8 hours',   'Intermediate', 'System and application monitoring with Prometheus, Grafana dashboards, and alerting rules.');

-- MOBILE DEV (5)
INSERT INTO courses (title, instructor, category, price, duration, level, description) VALUES
('Android Development Basics',    'Rajesh Kumar',  'Mobile Dev',     399, '14 hours',  'Beginner',     'Build Android apps with Kotlin. Activities, fragments, RecyclerView, Room DB, and Retrofit.'),
('Flutter Cross-Platform Apps',   'Sneha Patil',   'Mobile Dev',     599, '18 hours',  'Intermediate', 'Build iOS and Android apps from one codebase with Flutter and Dart. State management and animations.'),
('React Native Fundamentals',     'Sneha Patil',   'Mobile Dev',     449, '12 hours',  'Intermediate', 'Cross-platform mobile development with React Native, Expo, navigation, and native modules.'),
('iOS Development with SwiftUI',  'Pooja Singh',   'Mobile Dev',     699, '20 hours',  'Intermediate', 'Build modern iOS apps using SwiftUI. Data binding, navigation, networking, and App Store submission.'),
('Mobile App UI/UX Design',       'Pooja Singh',   'Mobile Dev',     249, '6 hours',   'Beginner',     'Design principles for mobile apps. Figma prototyping, navigation patterns, and accessibility.');

-- CLOUD (4)
INSERT INTO courses (title, instructor, category, price, duration, level, description) VALUES
('AWS Cloud Practitioner',        'Arun Mehta',    'Cloud',          499, '12 hours',  'Beginner',     'Prepare for AWS Cloud Practitioner certification. Core services: EC2, S3, RDS, Lambda, and IAM.'),
('AWS Solutions Architect',       'Arun Mehta',    'Cloud',          999, '28 hours',  'Advanced',     'Design highly available, fault-tolerant architectures on AWS. VPC, load balancers, and multi-region.'),
('Google Cloud Fundamentals',     'Meera Iyer',    'Cloud',          449, '10 hours',  'Beginner',     'GCP core services: Compute Engine, Cloud Storage, BigQuery, Cloud Run, and IAM basics.'),
('Serverless Architecture',       'Amit Shah',     'Cloud',          549, '11 hours',  'Intermediate', 'Build event-driven apps with AWS Lambda, API Gateway, DynamoDB, SQS, and Step Functions.');

-- CYBERSECURITY (3)
INSERT INTO courses (title, instructor, category, price, duration, level, description) VALUES
('Ethical Hacking Fundamentals',  'Vikas Rao',     'Cybersecurity',  599, '16 hours',  'Intermediate', 'Penetration testing methodology, reconnaissance, vulnerability scanning, and exploitation basics.'),
('Web Application Security',      'Vikas Rao',     'Cybersecurity',  699, '14 hours',  'Advanced',     'OWASP Top 10, SQL injection, XSS, CSRF, broken auth, and secure coding practices.'),
('Network Security Essentials',   'Rajesh Kumar',  'Cybersecurity',  499, '10 hours',  'Beginner',     'Firewalls, VPNs, TLS/SSL, intrusion detection, and securing network infrastructure.');
