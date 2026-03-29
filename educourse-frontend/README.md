# EduCourse — Frontend (Vanilla HTML/CSS/JS)

The frontend for the EduCourse online learning platform. Pure HTML, CSS, and JavaScript — no frameworks. Communicates with the Spring Boot backend via REST API calls using `fetch()`.

---

## Pages

| File | Access | Description |
|------|--------|-------------|
| `index.html` | Public | Browse all 50+ courses. Search, filter by category, favorite, enroll |
| `login.html` | Public | Email + password login |
| `register.html` | Public | Create a new student account |
| `favorites.html` | Login required | Saved courses |
| `purchased.html` | Login required | Enrolled courses (My Learning) |
| `manage-courses.html` | Student Admin / Admin | Add, edit, delete your own courses |
| `admin.html` | Admin only | Roles table, user management, all courses |

---

## File Structure

```
educourse-frontend/
├── favicon.svg               — Graduation cap + book logo
├── index.html
├── login.html
├── register.html
├── favorites.html
├── purchased.html
├── manage-courses.html
├── admin.html
├── css/
│   └── style.css             — All styles (DM Sans font, navy + amber theme)
└── js/
    ├── api.js                — MUST load first on every page
    ├── courses.js            — Course browsing logic
    ├── admin.js              — Admin panel logic
    └── manage-courses.js     — Student admin course management
```

---

## How to Run (Local)

1. Start the Spring Boot backend on `http://localhost:8080`
2. Open `index.html` directly in your browser — OR serve with VS Code Live Server
3. No build step, no npm, no package.json needed

> **Important**: The backend must be running for the frontend to work. The server-down banner will show if it can't connect.

---

## Key Architecture: api.js

`api.js` **must be loaded first** on every page. It provides all shared functions:

### session{} — localStorage wrappers
```javascript
session.set('token', jwtString)  // store JWT
session.get('token')             // retrieve JWT
session.clear()                  // logout — wipe all stored data
```

### auth{} — Authentication helpers
```javascript
auth.token()          // → JWT string or null
auth.email()          // → logged-in user's email
auth.fullName()       // → display name
auth.roles()          // → ['ROLE_STUDENT', 'ROLE_STUDENT_ADMIN']
auth.isAdmin()        // → true if has ROLE_ADMIN
auth.isStudentAdmin() // → true if has ROLE_STUDENT_ADMIN
auth.logout()         // → clears session, redirects to login.html
auth.guard()          // → redirects to login.html if no token
```

### api{} — HTTP methods with auto Bearer header
```javascript
api.get('/api/courses')                       // GET request
api.post('/api/auth/login', {email, password}) // POST with body
api.put('/api/courses/42', updatedData)        // PUT with body
api.delete('/api/courses/42')                  // DELETE request
```

### Other utilities
```javascript
refreshSession()   // re-fetches /api/user/me, updates localStorage roles
initNav()          // populates navbar username + logout handler
injectFooter()     // appends footer HTML to document.body
msg(id, text, type) // shows flash message in element with given id
```

---

## Authentication Flow

```
User logs in → JWT stored in localStorage → persists across browser restarts
↓
Every page calls refreshSession() on load
→ GET /api/user/me with stored token
→ Updates roles in localStorage (so role changes work without re-login)
↓
On logout: localStorage.clear() → redirect to login.html
On 401/403: auth.logout() called automatically
```

---

## Script Loading Order

Every HTML page loads scripts in this exact order:

```html
<script src="js/api.js"></script>       <!-- ALWAYS FIRST -->
<script src="js/courses.js"></script>   <!-- page-specific script -->
<script>injectFooter();</script>        <!-- ALWAYS LAST -->
```

> `api.js` must be loaded before any other script because `auth`, `api`, `msg`, `injectFooter` etc. are all defined there.

---

## Changing the Backend URL

If the backend is deployed (not localhost), update one line in `js/api.js`:

```javascript
// Local development:
const BASE = "http://localhost:8080";

// After deploying to Railway:
const BASE = "https://your-app.up.railway.app";
```

---

## Production Deployment (Vercel)

1. Update `BASE` in `js/api.js` to your production backend URL
2. Push to GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial frontend commit"
   git remote add origin https://github.com/YOUR_USERNAME/educourse-frontend.git
   git push -u origin main
   ```
3. Go to [vercel.com](https://vercel.com) → New Project → Import from GitHub
4. Framework: **Other** (plain static files)
5. Deploy — Vercel serves the static files globally via CDN

---

## Design

- **Font**: DM Sans (Google Fonts)
- **Colors**: Navy `#0f1f3d` + Amber `#f59e0b` + Emerald `#059669`
- **Theme**: Clean card-based layout, dark navbar, responsive grid
- **No frameworks**: Pure CSS with CSS variables

---

## Developer

**Nikhil Korkatti** — EduCourse Full-Stack Project
