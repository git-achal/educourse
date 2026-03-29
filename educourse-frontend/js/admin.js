/* admin.js — Admin panel. ROLE_ADMIN only.
   Role model: many-to-many. Users can hold multiple roles simultaneously.
   STUDENT + STUDENT_ADMIN is a valid combination.
*/

auth.guard();
initNav();

refreshSession().then(function() {
  if (!auth.isAdmin()) { alert("Admin access required."); location.href = "index.html"; }
});

/* ── Edit Course Modal ───────────────────────────────────── */
(function() {
  var modal = document.createElement("div");
  modal.id = "edit-modal";
  modal.style.cssText =
    "display:none;position:fixed;inset:0;z-index:1000;background:rgba(0,0,0,.45);" +
    "align-items:center;justify-content:center;padding:20px";
  modal.innerHTML =
    '<div style="background:#fff;border-radius:12px;padding:32px;width:100%;max-width:560px;' +
    'max-height:90vh;overflow-y:auto;box-shadow:0 8px 40px rgba(0,0,0,.25)">' +
      '<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">' +
        '<h2 style="font-size:17px;font-weight:700;color:var(--navy)">&#9998; Edit Course</h2>' +
        '<button id="modal-close" style="background:none;border:none;font-size:22px;cursor:pointer;color:var(--muted);line-height:1">&times;</button>' +
      '</div>' +
      '<div id="msg-edit" class="msg hidden"></div>' +
      '<input type="hidden" id="edit-id" />' +
      '<div class="form-grid">' +
        '<div class="field"><label>Course Title</label><input id="edit-title" /></div>' +
        '<div class="field"><label>Instructor</label><input id="edit-instructor" /></div>' +
        '<div class="field"><label>Category</label>' +
          '<select id="edit-category">' +
            '<option value="">Select...</option>' +
            '<option>Programming</option><option>Web Dev</option><option>Data Science</option>' +
            '<option>Database</option><option>DevOps</option><option>Mobile Dev</option>' +
            '<option>Cloud</option><option>Cybersecurity</option>' +
          '</select></div>' +
        '<div class="field"><label>Price (0 = Free)</label><input id="edit-price" type="number" min="0" /></div>' +
        '<div class="field"><label>Duration</label><input id="edit-duration" /></div>' +
        '<div class="field"><label>Level</label>' +
          '<select id="edit-level">' +
            '<option value="Beginner">Beginner</option>' +
            '<option value="Intermediate">Intermediate</option>' +
            '<option value="Advanced">Advanced</option>' +
          '</select></div>' +
        '<div class="field span-2"><label>Description</label>' +
          '<textarea id="edit-desc" rows="3"></textarea></div>' +
      '</div>' +
      '<div style="display:flex;gap:10px;margin-top:4px">' +
        '<button id="btn-save-edit" class="btn btn-primary">Save Changes</button>' +
        '<button id="modal-cancel" class="btn">Cancel</button>' +
      '</div>' +
    '</div>';
  document.body.appendChild(modal);
  document.getElementById("modal-close").addEventListener("click", closeEditModal);
  document.getElementById("modal-cancel").addEventListener("click", closeEditModal);
  modal.addEventListener("click", function(e) { if (e.target === modal) closeEditModal(); });
})();

function openEditModal(course) {
  document.getElementById("edit-id").value         = course.id;
  document.getElementById("edit-title").value      = course.title;
  document.getElementById("edit-instructor").value = course.instructor;
  document.getElementById("edit-category").value   = course.category;
  document.getElementById("edit-price").value      = course.price;
  document.getElementById("edit-duration").value   = course.duration;
  document.getElementById("edit-level").value      = course.level;
  document.getElementById("edit-desc").value       = course.description;
  hideMsg("msg-edit");
  document.getElementById("edit-modal").style.display = "flex";
  document.getElementById("edit-title").focus();
}
function closeEditModal() {
  document.getElementById("edit-modal").style.display = "none";
}

document.getElementById("btn-save-edit").addEventListener("click", async function() {
  hideMsg("msg-edit");
  var id = document.getElementById("edit-id").value;
  var title = document.getElementById("edit-title").value.trim();
  var instructor = document.getElementById("edit-instructor").value.trim();
  var category = document.getElementById("edit-category").value;
  var price = parseFloat(document.getElementById("edit-price").value) || 0;
  var duration = document.getElementById("edit-duration").value.trim();
  var level = document.getElementById("edit-level").value;
  var description = document.getElementById("edit-desc").value.trim();
  if (!title || !instructor || !category || !duration || !description)
    return msg("msg-edit", "Please fill in all fields.", "error");
  try {
    await api.put("/api/courses/" + id, { title: title, instructor: instructor, category: category, price: price, duration: duration, level: level, description: description });
    closeEditModal();
    msg("msg-courses", '"' + title + '" updated.', "success");
    loadCourses();
  } catch (e) { msg("msg-edit", e.message, "error"); }
});

/* ── Add Course ──────────────────────────────────────────── */
document.getElementById("btn-add").addEventListener("click", async function() {
  hideMsg("msg-course");
  var title = document.getElementById("c-title").value.trim();
  var instructor = document.getElementById("c-instructor").value.trim();
  var category = document.getElementById("c-category").value;
  var price = parseFloat(document.getElementById("c-price").value) || 0;
  var duration = document.getElementById("c-duration").value.trim();
  var level = document.getElementById("c-level").value;
  var description = document.getElementById("c-desc").value.trim();
  if (!title || !instructor || !category || !duration || !description)
    return msg("msg-course", "Please fill in all fields.", "error");
  try {
    await api.post("/api/courses", { title: title, instructor: instructor, category: category, price: price, duration: duration, level: level, description: description });
    msg("msg-course", '"' + title + '" added!', "success");
    ["c-title","c-instructor","c-price","c-duration","c-desc"].forEach(function(id) { document.getElementById(id).value = ""; });
    loadCourses();
  } catch (e) { msg("msg-course", e.message, "error"); }
});

/* ── All Courses ─────────────────────────────────────────── */
var _coursesCache = [];
async function loadCourses() {
  try {
    _coursesCache = await api.get("/api/courses");
    var tbody = document.getElementById("courses-body");
    if (!_coursesCache.length) {
      tbody.innerHTML = '<tr><td colspan="7" style="color:var(--muted);padding:20px">No courses found.</td></tr>';
      return;
    }
    tbody.innerHTML = _coursesCache.map(function(c) {
      return (
        '<tr>' +
        '<td><strong>' + c.title + '</strong></td>' +
        '<td><span class="badge badge-blue">' + c.category + '</span></td>' +
        '<td>' + c.instructor + '</td>' +
        '<td>' + c.level + '</td>' +
        '<td>' + (c.price === 0 ? '<span class="badge badge-green">Free</span>' : '&#8377;' + c.price) + '</td>' +
        '<td style="font-size:12px;color:var(--muted)">' + (c.createdByEmail || '<em>seeded</em>') + '</td>' +
        '<td style="display:flex;gap:6px;flex-wrap:wrap">' +
          '<button class="btn btn-primary btn-sm edit-btn" data-id="' + c.id + '">&#9998; Edit</button>' +
          '<button class="btn btn-danger btn-sm del-btn" data-id="' + c.id + '" data-title="' + c.title.replace(/"/g,'&quot;') + '">Delete</button>' +
        '</td>' +
        '</tr>'
      );
    }).join("");
    tbody.querySelectorAll(".edit-btn").forEach(function(btn) {
      btn.addEventListener("click", function() {
        var course = _coursesCache.find(function(c) { return c.id === +btn.dataset.id; });
        if (course) openEditModal(course);
      });
    });
    tbody.querySelectorAll(".del-btn").forEach(function(btn) {
      btn.addEventListener("click", async function() {
        if (!confirm('Delete "' + btn.dataset.title + '"?')) return;
        try { await api.delete("/api/courses/" + btn.dataset.id); msg("msg-courses", "Deleted.", "success"); loadCourses(); }
        catch (e) { msg("msg-courses", e.message, "error"); }
      });
    });
  } catch (e) { msg("msg-courses", e.message, "error"); }
}

/* ── Roles Reference Table ───────────────────────────────── */
async function loadRolesTable() {
  try {
    var roles = await api.get("/api/admin/roles");
    var tbody = document.getElementById("roles-body");
    var descriptions = {
      "ROLE_ADMIN":         "Full access. Manage all courses, all users, grant/revoke roles. Assigned via admin-users.properties only.",
      "ROLE_STUDENT_ADMIN": "Can create, edit and delete their own courses. Add via Make Student Admin button in Users table.",
      "ROLE_STUDENT":       "Default role on registration. Can browse, save favourites and enrol in courses."
    };
    tbody.innerHTML = roles.map(function(r) {
      var label = r.replace("ROLE_","");
      var cls = r === "ROLE_ADMIN" ? "badge-amber" : r === "ROLE_STUDENT_ADMIN" ? "badge-blue" : "badge-green";
      return (
        '<tr>' +
        '<td><span class="badge ' + cls + '">' + label + '</span></td>' +
        '<td style="font-size:13px;color:var(--muted)">' + (descriptions[r] || "—") + '</td>' +
        '<td style="font-size:12px;color:var(--muted)">' +
          (r === "ROLE_ADMIN" ? "admin-users.properties" :
           r === "ROLE_STUDENT_ADMIN" ? "Admin panel → Users → Make Student Admin" :
           "Auto-assigned on registration") +
        '</td>' +
        '</tr>'
      );
    }).join("");
  } catch (e) { /* silent */ }
}

/* ── Users Table ─────────────────────────────────────────── */
async function loadUsers() {
  try {
    var users = await api.get("/api/admin/users");
    var tbody = document.getElementById("users-body");

    tbody.innerHTML = users.map(function(u) {
      var isAdmin = u.roles.indexOf("ROLE_ADMIN") >= 0;
      var isSA    = u.roles.indexOf("ROLE_STUDENT_ADMIN") >= 0;

      /* Show every role as a badge — multiple badges for multi-role users */
      var badges = u.roles.map(function(r) {
        var label = r.replace("ROLE_","");
        var cls = r === "ROLE_ADMIN" ? "badge-amber" : r === "ROLE_STUDENT_ADMIN" ? "badge-blue" : "badge-green";
        return '<span class="badge ' + cls + '">' + label + '</span>';
      }).join(" ");

      var actions;
      if (isAdmin) {
        actions = '<span style="font-size:12px;color:var(--muted)">Protected — no changes allowed</span>';
      } else if (isSA) {
        /* User has STUDENT + STUDENT_ADMIN — can revoke STUDENT_ADMIN or delete */
        actions =
          '<button class="btn btn-sm" style="background:#fef3c7;color:#92400e" data-action="revoke" data-email="' + u.email + '">&#8722; Revoke Student Admin</button> ' +
          '<button class="btn btn-danger btn-sm" data-action="delete" data-email="' + u.email + '">Delete</button>';
      } else {
        /* Pure student — can grant STUDENT_ADMIN (additive) or delete */
        actions =
          '<button class="btn btn-primary btn-sm" data-action="promote" data-email="' + u.email + '">&#43; Make Student Admin</button> ' +
          '<button class="btn btn-danger btn-sm" data-action="delete" data-email="' + u.email + '">Delete</button>';
      }

      return (
        '<tr>' +
        '<td><strong>' + (u.fullName || u.username || "-") + '</strong></td>' +
        '<td style="font-size:13px;color:var(--muted)">' + u.email + '</td>' +
        '<td style="display:flex;gap:4px;flex-wrap:wrap">' + badges + '</td>' +
        '<td style="display:flex;gap:6px;flex-wrap:wrap">' + actions + '</td>' +
        '</tr>'
      );
    }).join("");

    tbody.querySelectorAll("[data-action]").forEach(function(btn) {
      btn.addEventListener("click", async function() {
        var action = btn.dataset.action, email = btn.dataset.email;
        if (action === "promote") {
          if (!confirm("Add ROLE_STUDENT_ADMIN to " + email + "?\nThey will keep their ROLE_STUDENT as well.")) return;
          try { await api.post("/api/admin/make-student-admin/" + encodeURIComponent(email)); msg("msg-users", email + " now has STUDENT + STUDENT_ADMIN roles.", "success"); loadUsers(); }
          catch (e) { msg("msg-users", e.message, "error"); }
        } else if (action === "revoke") {
          if (!confirm("Remove ROLE_STUDENT_ADMIN from " + email + "?\nThey will keep their ROLE_STUDENT.")) return;
          try { await api.delete("/api/admin/remove-student-admin/" + encodeURIComponent(email)); msg("msg-users", email + " — STUDENT_ADMIN revoked, STUDENT kept.", "success"); loadUsers(); }
          catch (e) { msg("msg-users", e.message, "error"); }
        } else if (action === "delete") {
          if (!confirm("Permanently delete user " + email + "?")) return;
          try { await api.delete("/api/admin/users/" + encodeURIComponent(email)); msg("msg-users", email + " deleted.", "success"); loadUsers(); }
          catch (e) { msg("msg-users", e.message, "error"); }
        }
      });
    });
  } catch (e) { msg("msg-users", e.message, "error"); }
}

loadCourses();
loadUsers();
loadRolesTable();
