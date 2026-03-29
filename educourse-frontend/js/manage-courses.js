/* manage-courses.js — STUDENT_ADMIN / ADMIN course management
   Supports: Add, Edit (update), Delete
   STUDENT_ADMIN: own courses only
   ADMIN: all courses
*/

auth.guard();
initNav();

refreshSession().then(function() {
  if (!auth.isStudentAdmin() && !auth.isAdmin()) {
    alert("Access denied: Student Admin role required.");
    location.href = "index.html";
  }
  if (auth.isAdmin()) {
    var el = document.getElementById("link-admin");
    if (el) el.style.display = "";
  }
});

/* ── Inject edit modal ───────────────────────────────────── */
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
  document.getElementById("modal-close").addEventListener("click", closeModal);
  document.getElementById("modal-cancel").addEventListener("click", closeModal);
  modal.addEventListener("click", function(e) { if (e.target === modal) closeModal(); });
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

function closeModal() {
  document.getElementById("edit-modal").style.display = "none";
}

document.getElementById("btn-save-edit").addEventListener("click", async function() {
  hideMsg("msg-edit");
  var id          = document.getElementById("edit-id").value;
  var title       = document.getElementById("edit-title").value.trim();
  var instructor  = document.getElementById("edit-instructor").value.trim();
  var category    = document.getElementById("edit-category").value;
  var price       = parseFloat(document.getElementById("edit-price").value) || 0;
  var duration    = document.getElementById("edit-duration").value.trim();
  var level       = document.getElementById("edit-level").value;
  var description = document.getElementById("edit-desc").value.trim();

  if (!title || !instructor || !category || !duration || !description)
    return msg("msg-edit", "Please fill in all fields.", "error");

  try {
    await api.put("/api/courses/" + id, {
      title: title, instructor: instructor, category: category,
      price: price, duration: duration, level: level, description: description
    });
    closeModal();
    msg("msg-courses", '"' + title + '" updated successfully.', "success");
    loadMyCourses();
  } catch (e) { msg("msg-edit", e.message, "error"); }
});

/* ── Add Course ──────────────────────────────────────────── */
document.getElementById("btn-add").addEventListener("click", async function() {
  hideMsg("msg-course");
  var title       = document.getElementById("c-title").value.trim();
  var instructor  = document.getElementById("c-instructor").value.trim();
  var category    = document.getElementById("c-category").value;
  var price       = parseFloat(document.getElementById("c-price").value) || 0;
  var duration    = document.getElementById("c-duration").value.trim();
  var level       = document.getElementById("c-level").value;
  var description = document.getElementById("c-desc").value.trim();

  if (!title || !instructor || !category || !duration || !description)
    return msg("msg-course", "Please fill in all required fields.", "error");

  try {
    await api.post("/api/courses", {
      title: title, instructor: instructor, category: category,
      price: price, duration: duration, level: level, description: description
    });
    msg("msg-course", '"' + title + '" added successfully!', "success");
    ["c-title","c-instructor","c-price","c-duration","c-desc"].forEach(function(id) {
      document.getElementById(id).value = "";
    });
    loadMyCourses();
  } catch (e) { msg("msg-course", e.message, "error"); }
});

/* ── Courses Table ───────────────────────────────────────── */
var _cache = [];

async function loadMyCourses() {
  try {
    var endpoint = auth.isAdmin() ? "/api/courses" : "/api/courses/my";
    _cache = await api.get(endpoint);
    var tbody = document.getElementById("courses-body");

    if (!_cache.length) {
      tbody.innerHTML =
        '<tr><td colspan="7" style="color:var(--muted);padding:20px">' +
        'No courses yet. Add your first course above!</td></tr>';
      return;
    }

    tbody.innerHTML = _cache.map(function(c) {
      return (
        '<tr>' +
        '<td><strong>' + c.title + '</strong></td>' +
        '<td><span class="badge badge-blue">' + c.category + '</span></td>' +
        '<td>' + c.instructor + '</td>' +
        '<td>' + c.level + '</td>' +
        '<td>' + (c.price === 0
          ? '<span class="badge badge-green">Free</span>'
          : '&#8377;' + c.price) + '</td>' +
        '<td style="display:flex;gap:6px;flex-wrap:wrap">' +
          '<button class="btn btn-primary btn-sm edit-btn" data-id="' + c.id + '">&#9998; Edit</button>' +
          '<button class="btn btn-danger btn-sm del-btn" data-id="' + c.id + '"' +
          ' data-title="' + c.title.replace(/"/g,'&quot;') + '">Delete</button>' +
        '</td>' +
        '</tr>'
      );
    }).join("");

    tbody.querySelectorAll(".edit-btn").forEach(function(btn) {
      btn.addEventListener("click", function() {
        var course = _cache.find(function(c) { return c.id === +btn.dataset.id; });
        if (course) openEditModal(course);
      });
    });

    tbody.querySelectorAll(".del-btn").forEach(function(btn) {
      btn.addEventListener("click", async function() {
        if (!confirm('Delete "' + btn.dataset.title + '"? Cannot be undone.')) return;
        try {
          await api.delete("/api/courses/" + btn.dataset.id);
          msg("msg-courses", "Course deleted.", "success");
          loadMyCourses();
        } catch (e) { msg("msg-courses", e.message, "error"); }
      });
    });
  } catch (e) { msg("msg-courses", e.message, "error"); }
}

loadMyCourses();
