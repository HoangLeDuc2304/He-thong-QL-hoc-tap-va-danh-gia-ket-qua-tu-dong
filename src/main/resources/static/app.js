const page = document.body.dataset.page;
const toast = document.querySelector("#toast");

async function apiRequest(url, options = {}) {
    const response = await fetch(url, {
        headers: { "Content-Type": "application/json" },
        ...options
    });

    const text = await response.text();
    const payload = text ? JSON.parse(text) : {};

    if (!response.ok) {
        throw new Error(payload.message || "Yêu cầu thất bại");
    }

    return payload;
}

function loadUsers() {
    const savedUsers = localStorage.getItem("qlht_users");
    return savedUsers ? JSON.parse(savedUsers) : [];
}

function saveUsers(users) {
    localStorage.setItem("qlht_users", JSON.stringify(users));
}

function getCurrentUser() {
    const user = localStorage.getItem("qlht_current_user");
    return user ? JSON.parse(user) : null;
}

function setCurrentUser(user) {
    localStorage.setItem("qlht_current_user", JSON.stringify(user));
}

function logout() {
    localStorage.removeItem("qlht_current_user");
    window.location.href = "index.html";
}

function showToast(message) {
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add("show");
    window.setTimeout(() => toast.classList.remove("show"), 2200);
}

function requireRole(role) {
    const user = getCurrentUser();

    if (!user) {
        window.location.href = "index.html";
        return null;
    }

    if (user.role !== role) {
        window.location.href = user.role === "teacher" ? "teacher.html" : "student.html";
        return null;
    }

    return user;
}

function initLoginPage() {
    const users = loadUsers();
    const currentUser = getCurrentUser();

    if (currentUser) {
        window.location.href = currentUser.role === "TEACHER" || currentUser.role === "teacher" ? "teacher.html" : "student.html";
        return;
    }

    const loginTab = document.querySelector("#loginTab");
    const registerTab = document.querySelector("#registerTab");
    const loginForm = document.querySelector("#loginForm");
    const registerForm = document.querySelector("#registerForm");
    const loginEmail = document.querySelector("#loginEmail");
    const loginPassword = document.querySelector("#loginPassword");
    const registerName = document.querySelector("#registerName");
    const registerEmail = document.querySelector("#registerEmail");
    const registerPassword = document.querySelector("#registerPassword");
    const registerRole = document.querySelector("#registerRole");

    function setAuthMode(mode) {
        const isLogin = mode === "login";
        loginForm.hidden = !isLogin;
        registerForm.hidden = isLogin;
        loginTab.classList.toggle("active", isLogin);
        registerTab.classList.toggle("active", !isLogin);
    }

    loginTab.addEventListener("click", () => setAuthMode("login"));
    registerTab.addEventListener("click", () => setAuthMode("register"));

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const email = loginEmail.value.trim().toLowerCase();
        const password = loginPassword.value;

        try {
            const user = await apiRequest("/api/auth/login", {
                method: "POST",
                body: JSON.stringify({ email, password })
            });

            const normalizedUser = {
                id: user.id,
                name: user.fullName || user.username,
                email: user.email,
                username: user.username,
                role: user.role === "TEACHER" ? "teacher" : "student"
            };

            setCurrentUser(normalizedUser);
            window.location.href = normalizedUser.role === "teacher" ? "teacher.html" : "student.html";
        } catch (error) {
            showToast(error.message || "Email hoặc mật khẩu không đúng.");
        }
    });

    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const email = registerEmail.value.trim().toLowerCase();
        const user = {
            username: (registerName.value.trim().toLowerCase().replace(/\s+/g, "") || email.split('@')[0]),
            email,
            password: registerPassword.value,
            fullName: registerName.value.trim(),
            role: registerRole.value.toUpperCase()
        };

        try {
            const created = await apiRequest("/api/auth/register", {
                method: "POST",
                body: JSON.stringify(user)
            });

            const normalizedUser = {
                id: created.id,
                name: created.fullName || created.username,
                email: created.email,
                username: created.username,
                role: created.role === "TEACHER" ? "teacher" : "student"
            };

            setCurrentUser(normalizedUser);
            window.location.href = normalizedUser.role === "teacher" ? "teacher.html" : "student.html";
        } catch (error) {
            showToast(error.message || "Đăng ký không thành công.");
        }
    });
}

const questions = [
    {
        id: 1,
        content: "Trong mô hình học tập trực tuyến, LMS có vai trò chính là gì?",
        optionA: "Quản lý nội dung, lớp học và tiến độ học tập",
        optionB: "Chỉ lưu trữ tài liệu dạng PDF",
        optionC: "Chỉ dùng để gửi email thông báo",
        optionD: "Thay thế hoàn toàn giảng viên",
        correctAnswer: "A",
        difficulty: "Dễ",
        chapter: "Chương 1"
    },
    {
        id: 2,
        content: "Thuật toán nào phù hợp để tự động tính điểm trắc nghiệm theo đáp án chuẩn?",
        optionA: "Sắp xếp nổi bọt",
        optionB: "So khớp đáp án",
        optionC: "Nén dữ liệu",
        optionD: "Mã hóa bất đối xứng",
        correctAnswer: "B",
        difficulty: "Dễ",
        chapter: "Chương 2"
    },
    {
        id: 3,
        content: "Khi đánh giá tự động bài tự luận ngắn, hệ thống cần tiền xử lý dữ liệu văn bản như thế nào?",
        optionA: "Xóa toàn bộ câu trả lời",
        optionB: "Chỉ đếm số ký tự",
        optionC: "Chuẩn hóa, tách từ và loại bỏ nhiễu",
        optionD: "Chỉ kiểm tra thời gian nộp bài",
        correctAnswer: "C",
        difficulty: "Trung bình",
        chapter: "Chương 3"
    },
    {
        id: 4,
        content: "Chỉ số nào thường dùng để đánh giá mức độ hoàn thành học phần của người học?",
        optionA: "Màu nền giao diện",
        optionB: "Tỷ lệ hoàn thành, điểm trung bình, số lần làm bài",
        optionC: "Kích thước màn hình",
        optionD: "Tên trình duyệt",
        correctAnswer: "B",
        difficulty: "Trung bình",
        chapter: "Chương 4"
    },
    {
        id: 5,
        content: "Cách nào giúp phát hiện câu hỏi có độ phân biệt thấp trong ngân hàng câu hỏi?",
        optionA: "Đổi màu đáp án đúng",
        optionB: "Ẩn câu hỏi khỏi giao diện",
        optionC: "Phân tích tỷ lệ trả lời đúng theo nhóm điểm",
        optionD: "Tăng thời gian làm bài",
        correctAnswer: "C",
        difficulty: "Khó",
        chapter: "Chương 5"
    }
];

let editingId = null;

const teacherOverviewCards = [
    { title: "Câu hỏi đang hoạt động", value: "5", note: "2 câu dễ · 2 trung bình · 1 khó" },
    { title: "Khóa học phụ trách", value: "3", note: "Java, Web, Cơ sở dữ liệu" },
    { title: "Bài kiểm tra sắp diễn ra", value: "2", note: "Trong tuần này" },
    { title: "Báo cáo chưa xử lý", value: "4", note: "Cần xem xét và xuất file" }
];

const teacherNotifications = [
    "Sinh viên lớp CNTT21 đã nộp bài kiểm tra Java.",
    "Khóa học Thiết kế giao diện Web cần cập nhật lịch học.",
    "Báo cáo tuần trước đã sẵn sàng để xuất PDF."
];

const teacherCourses = [
    {
        code: "JAVA101",
        name: "Lập trình Java cơ bản",
        students: 42,
        schedule: "Thứ 2, 07:30 - 09:30",
        room: "P.204",
        status: "Đang chạy",
        progress: 78
    },
    {
        code: "WEB202",
        name: "Thiết kế giao diện Web",
        students: 36,
        schedule: "Thứ 3, 13:00 - 15:00",
        room: "Lab 3",
        status: "Cập nhật",
        progress: 64
    },
    {
        code: "DBI201",
        name: "Cơ sở dữ liệu",
        students: 40,
        schedule: "Thứ 5, 09:45 - 11:45",
        room: "P.301",
        status: "Ổn định",
        progress: 89
    }
];

const teacherTests = [
    { name: "Quiz Java - Chương 5", course: "JAVA101", due: "20:00 hôm nay", totalQuestions: 20, published: true },
    { name: "Quiz CSS Grid", course: "WEB202", due: "Ngày mai", totalQuestions: 15, published: true },
    { name: "Quiz SQL Join", course: "DBI201", due: "Thứ Sáu", totalQuestions: 25, published: false }
];

const teacherResults = [
    { className: "CNTT21", students: 42, average: 7.8, pass: 38, fail: 4 },
    { className: "CNTT22", students: 39, average: 8.1, pass: 36, fail: 3 },
    { className: "CNTT23", students: 40, average: 7.2, pass: 34, fail: 6 }
];

const teacherReports = [
    { title: "Báo cáo tuần 1", desc: "Tổng hợp số lượng câu hỏi, bài kiểm tra và kết quả nộp bài." },
    { title: "Báo cáo tiến độ", desc: "So sánh tiến độ học tập giữa các lớp phụ trách." },
    { title: "Báo cáo chất lượng câu hỏi", desc: "Phân tích độ khó và mức độ phân biệt của ngân hàng câu hỏi." }
];

// Assignments & submissions stored in localStorage for demo
function loadAssignments() {
    const raw = localStorage.getItem('qlht_assignments');
    return raw ? JSON.parse(raw) : [];
}

function saveAssignments(list) {
    localStorage.setItem('qlht_assignments', JSON.stringify(list));
}

function loadSubmissions() {
    const raw = localStorage.getItem('qlht_submissions');
    return raw ? JSON.parse(raw) : [];
}

function saveSubmissions(list) {
    localStorage.setItem('qlht_submissions', JSON.stringify(list));
}

let assignments = loadAssignments();
let submissions = loadSubmissions();


function initTeacherPage() {
    const user = requireRole("teacher");
    if (!user) return;

    document.querySelector("#teacherName").textContent = `${user.name} - Giảng viên`;
    document.querySelector("#logoutBtn").addEventListener("click", logout);

    // Nav handling: show/hide sections based on sidebar clicks
    const teacherNavItems = [...document.querySelectorAll('.nav-item[data-target]')];
    function showTeacherSection(target) {
        teacherNavItems.forEach((item) => item.classList.toggle('active', item.dataset.target === target));
        const sections = document.querySelectorAll('.section-content');
        sections.forEach((s) => {
            s.hidden = s.id !== `section-${target}`;
        });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    teacherNavItems.forEach((item) => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            showTeacherSection(item.dataset.target);
        });
    });

    // ensure initial visible section (questionBank by default)
    const active = teacherNavItems.find((i) => i.classList.contains('active')) || teacherNavItems[0];
    if (active) showTeacherSection(active.dataset.target);

    const tableBody = document.querySelector("#questionTable");
    const emptyState = document.querySelector("#emptyState");
    const totalQuestions = document.querySelector("#totalQuestions");
    const searchInput = document.querySelector("#searchInput");
    const difficultyFilter = document.querySelector("#difficultyFilter");
    const chapterFilter = document.querySelector("#chapterFilter");
    const modal = document.querySelector("#questionModal");
    const form = document.querySelector("#questionForm");
    const modalTitle = document.querySelector("#modalTitle");
    const questionText = document.querySelector("#questionText");
    const optionA = document.querySelector("#optionA");
    const optionB = document.querySelector("#optionB");
    const optionC = document.querySelector("#optionC");
    const optionD = document.querySelector("#optionD");
    const correctAnswer = document.querySelector("#correctAnswer");
    const questionDifficulty = document.querySelector("#questionDifficulty");
    const questionChapter = document.querySelector("#questionChapter");
    const overviewGrid = document.querySelector("#overviewGrid");
    const overviewNotifications = document.querySelector("#overviewNotifications");
    const teacherCourseGrid = document.querySelector("#teacherCourseGrid");
    const teacherTestList = document.querySelector("#teacherTestList");
    const teacherResultTable = document.querySelector("#teacherResultTable");
    const teacherReportList = document.querySelector("#teacherReportList");

    function getDifficultyClass(value) {
        if (value === "Dễ") return "easy";
        if (value === "Trung bình") return "medium";
        return "hard";
    }

    function getCorrectAnswerText(question) {
        return question[`option${question.correctAnswer}`];
    }

    function renderTeacherOverview() {
        if (overviewGrid) {
            overviewGrid.innerHTML = teacherOverviewCards.map((card) => `
                <article class="overview-card">
                    <span>${card.title}</span>
                    <strong>${card.value}</strong>
                    <p>${card.note}</p>
                </article>
            `).join("");
        }

        if (overviewNotifications) {
            overviewNotifications.innerHTML = teacherNotifications.map((item) => `
                <div class="mini-item">${item}</div>
            `).join("");
        }
    }

    function renderTeacherCourses() {
        if (!teacherCourseGrid) return;

        teacherCourseGrid.innerHTML = teacherCourses.map((course) => `
            <article class="teacher-card">
                <div class="teacher-card-head">
                    <span class="course-code">${course.code}</span>
                    <span class="course-status">${course.status}</span>
                </div>
                <h3>${course.name}</h3>
                <p>${course.schedule} · ${course.room}</p>
                <div class="teacher-meta">${course.students} sinh viên</div>
                <div class="course-progress-row">
                    <span>Tiến độ</span>
                    <strong>${course.progress}%</strong>
                </div>
                <div class="course-progress"><span style="width:${course.progress}%"></span></div>
            </article>
        `).join("");
    }

    function renderTeacherTests() {
        if (!teacherTestList) return;

        teacherTestList.innerHTML = assignments.map((a) => {
            const typeLabel = a.type === 'trac-nghiem' ? 'Trắc nghiệm' : 'UML';
            const typeClass = a.type === 'trac-nghiem' ? 'medium' : 'easy';
            const totalQuestions = a.totalQuestions || (Array.isArray(a.questionIds) ? a.questionIds.length : 0);
            return `
                <article class="teacher-row">
                    <div>
                        <strong>${a.title}</strong>
                        <p>${a.course || ''} · ${typeLabel} · ${totalQuestions ? `${totalQuestions} câu hỏi` : ''} Hạn: ${a.due || '-'} · Thang điểm: ${a.max || 10}</p>
                    </div>
                    <div style="display:flex;gap:8px;align-items:center;">
                        <span class="badge ${typeClass}">${typeLabel}</span>
                        <button class="text-btn" type="button" data-action="viewSubs" data-id="${a.id}">Xem nộp bài</button>
                        <button class="text-btn" type="button" data-action="publish" data-id="${a.id}">${a.published ? 'Thu hồi' : 'Xuất bản'}</button>
                    </div>
                </article>
            `;
        }).join("");
    }

    function renderTeacherResults() {
        if (!teacherResultTable) return;

        teacherResultTable.innerHTML = teacherResults.map((row) => `
            <tr>
                <td><strong>${row.className}</strong></td>
                <td>${row.students}</td>
                <td>${row.average.toFixed(1)}</td>
                <td>${row.pass}</td>
                <td>${row.fail}</td>
            </tr>
        `).join("");
    }

    function renderTeacherReports() {
        if (!teacherReportList) return;

        teacherReportList.innerHTML = teacherReports.map((report) => `
            <article class="report-card">
                <strong>${report.title}</strong>
                <p>${report.desc}</p>
                <button class="text-btn" type="button">Xem chi tiết</button>
            </article>
        `).join("");
    }

    function getFilteredQuestions() {
        const keyword = searchInput.value.trim().toLowerCase();
        const difficulty = difficultyFilter.value;
        const chapter = chapterFilter.value;

        return questions.filter((question) => {
            const searchableText = [
                question.content,
                question.optionA,
                question.optionB,
                question.optionC,
                question.optionD
            ].join(" ").toLowerCase();

            const matchKeyword = searchableText.includes(keyword);
            const matchDifficulty = !difficulty || question.difficulty === difficulty;
            const matchChapter = !chapter || question.chapter === chapter;
            return matchKeyword && matchDifficulty && matchChapter;
        });
    }

    // Pagination state
    const pageSize = 5;
    let currentPage = 1;

    function getTotalPages(filtered) {
        return Math.max(1, Math.ceil(filtered.length / pageSize));
    }

    function renderPagination(filtered) {
        const pagination = document.querySelector("#pagination");
        if (!pagination) return;

        const totalPages = getTotalPages(filtered);
        pagination.innerHTML = "";

        function makeButton(label, disabled) {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "page-btn";
            btn.textContent = label;
            if (disabled) btn.disabled = true;
            return btn;
        }

        const prev = makeButton("‹ Trước", currentPage <= 1);
        prev.addEventListener("click", () => {
            if (currentPage > 1) {
                currentPage--;
                renderQuestions();
            }
        });
        pagination.appendChild(prev);

        // show up to 7 page buttons
        const maxButtons = 7;
        let start = Math.max(1, currentPage - Math.floor(maxButtons / 2));
        let end = start + maxButtons - 1;
        if (end > totalPages) {
            end = totalPages;
            start = Math.max(1, end - maxButtons + 1);
        }

        for (let p = start; p <= end; p++) {
            const btn = makeButton(p, false);
            btn.classList.toggle("active", p === currentPage);
            btn.addEventListener("click", () => {
                currentPage = p;
                renderQuestions();
            });
            pagination.appendChild(btn);
        }

        const next = makeButton("Tiếp ›", currentPage >= totalPages);
        next.addEventListener("click", () => {
            if (currentPage < totalPages) {
                currentPage++;
                renderQuestions();
            }
        });
        pagination.appendChild(next);
    }

    function renderOptions(question) {
        return `
            <ul class="option-list">
                <li class="${question.correctAnswer === "A" ? "correct" : ""}"><strong>A.</strong> ${question.optionA}</li>
                <li class="${question.correctAnswer === "B" ? "correct" : ""}"><strong>B.</strong> ${question.optionB}</li>
                <li class="${question.correctAnswer === "C" ? "correct" : ""}"><strong>C.</strong> ${question.optionC}</li>
                <li class="${question.correctAnswer === "D" ? "correct" : ""}"><strong>D.</strong> ${question.optionD}</li>
            </ul>
        `;
    }

    function renderQuestions() {
        const filteredQuestions = getFilteredQuestions();
        totalQuestions.textContent = questions.length;
        emptyState.hidden = filteredQuestions.length > 0;

        // ensure currentPage is valid
        const totalPages = getTotalPages(filteredQuestions);
        if (currentPage > totalPages) currentPage = totalPages;

        const startIdx = (currentPage - 1) * pageSize;
        const pageItems = filteredQuestions.slice(startIdx, startIdx + pageSize);

        tableBody.innerHTML = pageItems.map((question) => `
            <tr>
                <td class="question-cell">${question.content}</td>
                <td>${renderOptions(question)}</td>
                <td><span class="answer-pill">${question.correctAnswer}. ${getCorrectAnswerText(question)}</span></td>
                <td><span class="badge ${getDifficultyClass(question.difficulty)}">${question.difficulty}</span></td>
                <td>${question.chapter}</td>
                <td>
                    <div class="row-actions">
                        <button class="text-btn" type="button" data-action="edit" data-id="${question.id}">Sửa</button>
                        <button class="text-btn danger" type="button" data-action="delete" data-id="${question.id}">Xóa</button>
                    </div>
                </td>
            </tr>
        `).join("");

        renderPagination(filteredQuestions);
    }

    function openModal(question = null) {
        editingId = question ? question.id : null;
        modalTitle.textContent = question ? "Sửa câu hỏi" : "Thêm câu hỏi";
        questionText.value = question?.content || "";
        optionA.value = question?.optionA || "";
        optionB.value = question?.optionB || "";
        optionC.value = question?.optionC || "";
        optionD.value = question?.optionD || "";
        correctAnswer.value = question?.correctAnswer || "A";
        questionDifficulty.value = question?.difficulty || "Dễ";
        questionChapter.value = question?.chapter || "Chương 1";
        modal.showModal();
        questionText.focus();
    }

    function closeModal() {
        modal.close();
        form.reset();
        editingId = null;
    }

    document.querySelector("#addBtn").addEventListener("click", () => openModal());
    document.querySelector("#closeModal").addEventListener("click", closeModal);
    document.querySelector("#cancelBtn").addEventListener("click", closeModal);
    document.querySelector("#importBtn").addEventListener("click", () => {
        showToast("Chức năng import Excel đã sẵn sàng để nối API.");
    });

    // Teacher: create assignment modal handlers
    const createAssignmentBtn = document.querySelector('#createAssignmentBtn');
    const createAssignmentModal = document.querySelector('#createAssignmentModal');
    const createAssignmentForm = document.querySelector('#createAssignmentForm');
    const closeCreateAssignment = document.querySelector('#closeCreateAssignment');
    const cancelCreateAssign = document.querySelector('#cancelCreateAssign');
    const createQuizBtn = document.querySelector('#createQuizBtn');
    const createQuizModal = document.querySelector('#createQuizModal');
    const createQuizForm = document.querySelector('#createQuizForm');
    const closeCreateQuiz = document.querySelector('#closeCreateQuiz');
    const cancelCreateQuiz = document.querySelector('#cancelCreateQuiz');
    const submissionsModal = document.querySelector('#submissionsModal');
    const submissionsList = document.querySelector('#submissionsList');
    const submissionsTitle = document.querySelector('#submissionsTitle');

    function buildRandomQuizQuestions(chapter, count) {
        const pool = chapter ? questions.filter((item) => item.chapter === chapter) : [...questions];
        if (!pool.length) return [];
        if (pool.length < count) return pool;
        const shuffled = [...pool].sort(() => Math.random() - 0.5);
        return shuffled.slice(0, count);
    }

    if (createAssignmentBtn) {
        createAssignmentBtn.addEventListener('click', () => createAssignmentModal.showModal());
    }
    if (closeCreateAssignment) closeCreateAssignment.addEventListener('click', () => createAssignmentModal.close());
    if (cancelCreateAssign) cancelCreateAssign.addEventListener('click', () => createAssignmentModal.close());
    if (createQuizBtn) {
        createQuizBtn.addEventListener('click', () => createQuizModal.showModal());
    }
    if (closeCreateQuiz) closeCreateQuiz.addEventListener('click', () => createQuizModal.close());
    if (cancelCreateQuiz) cancelCreateQuiz.addEventListener('click', () => createQuizModal.close());

    if (createAssignmentForm) {
        createAssignmentForm.addEventListener('submit', (ev) => {
            ev.preventDefault();
            const title = document.querySelector('#assignTitle').value.trim();
            const desc = document.querySelector('#assignDesc').value.trim();
            const due = document.querySelector('#assignDue').value;
            const max = Number(document.querySelector('#assignMax').value) || 10;
            const a = { id: Date.now(), title, desc, due, max, createdBy: user.email, published: true, type: 'uml' };
            assignments.unshift(a);
            saveAssignments(assignments);
            renderTeacherTests();
            createAssignmentModal.close();
            showToast('Đã tạo bài UML');
        });
    }

    if (createQuizForm) {
        createQuizForm.addEventListener('submit', (ev) => {
            ev.preventDefault();
            const title = document.querySelector('#quizTitle').value.trim();
            const desc = document.querySelector('#quizDesc').value.trim();
            const due = document.querySelector('#quizDue').value;
            const max = Number(document.querySelector('#quizMax').value) || 10;
            const count = Number(document.querySelector('#quizQuestionCount').value) || 5;
            const chapter = document.querySelector('#quizChapter').value;
            const selectedQuestions = buildRandomQuizQuestions(chapter, count);

            if (!selectedQuestions.length) {
                showToast('Không có câu hỏi nào phù hợp trong ngân hàng câu hỏi.');
                return;
            }

            if (selectedQuestions.length < count) {
                showToast(`Chỉ có ${selectedQuestions.length} câu hỏi trong chương đã chọn. Hệ thống đã tạo đề với số câu hiện có.`);
            }

            const quiz = {
                id: Date.now(),
                title,
                desc,
                due,
                max,
                type: 'trac-nghiem',
                chapter: chapter || 'Tất cả',
                questionIds: selectedQuestions.map((item) => item.id),
                totalQuestions: selectedQuestions.length,
                createdBy: user.email,
                published: true
            };

            assignments.unshift(quiz);
            saveAssignments(assignments);
            renderTeacherTests();
            createQuizForm.reset();
            createQuizModal.close();
            showToast('Đã tạo bài kiểm tra trắc nghiệm');
        });
    }

    function openSubmissionsModal(assignId) {
        const assign = assignments.find((x) => x.id === Number(assignId));
        submissionsTitle.textContent = `Nộp bài — ${assign?.title || ''}`;
        const list = submissions.filter((s) => s.assignmentId === Number(assignId));
        if (!list.length) {
            submissionsList.innerHTML = '<div class="empty-state">Chưa có học sinh nộp bài.</div>';
        } else {
            submissionsList.innerHTML = list.map((s) => `
                <div class="teacher-submission" data-submission-id="${s.id}">
                    <div><strong>${s.studentName || s.studentEmail}</strong>
                    <p>${s.content}</p></div>
                    <div>
                        <label>Điểm</label>
                        <input type="number" min="0" max="${assign?.max || 10}" value="${s.score ?? ''}" data-sub-id="${s.id}" class="grade-input" />
                        <button class="text-btn" data-action="saveGrade" data-id="${s.id}">Lưu</button>
                    </div>
                </div>
            `).join('');
        }
        submissionsModal.showModal();
    }

    if (document.querySelector('#closeSubmissions')) {
        document.querySelector('#closeSubmissions').addEventListener('click', () => submissionsModal.close());
    }

    // handle teacherTestList button clicks (view submissions / publish)
    if (teacherTestList) {
        teacherTestList.addEventListener('click', (e) => {
            const btn = e.target.closest('button');
            if (!btn) return;
            const action = btn.dataset.action;
            const id = btn.dataset.id;
            if (action === 'viewSubs') {
                openSubmissionsModal(id);
            }
            if (action === 'publish') {
                const a = assignments.find((x) => x.id === Number(id));
                if (!a) return;
                a.published = !a.published;
                saveAssignments(assignments);
                renderTeacherTests();
                showToast(a.published ? 'Đã xuất bản bài' : 'Đã thu hồi bài');
            }
        });
    }

    // handle grading save inside submissions modal
    if (submissionsList) {
        submissionsList.addEventListener('click', (e) => {
            const btn = e.target.closest('[data-action="saveGrade"]');
            if (!btn) return;
            const subId = Number(btn.dataset.id);
            const input = submissionsList.querySelector(`input[data-sub-id="${subId}"]`);
            const score = input ? Number(input.value) : null;
            const sub = submissions.find((s) => s.id === subId);
            if (!sub) return;
            sub.score = score;
            sub.gradedAt = new Date().toISOString();
            saveSubmissions(submissions);
            showToast('Đã lưu điểm');
            openSubmissionsModal(sub.assignmentId);
        });
    }

    [searchInput, difficultyFilter, chapterFilter].forEach((element) => {
        element.addEventListener("input", () => { currentPage = 1; renderQuestions(); });
    });

    tableBody.addEventListener("click", (event) => {
        const button = event.target.closest("button");
        if (!button) return;

        const id = Number(button.dataset.id);
        const index = questions.findIndex((question) => question.id === id);
        if (index === -1) return;

        if (button.dataset.action === "edit") {
            openModal(questions[index]);
        }

        if (button.dataset.action === "delete") {
            questions.splice(index, 1);
            renderQuestions();
            showToast("Đã xóa câu hỏi.");
        }
    });

    form.addEventListener("submit", (event) => {
        event.preventDefault();

        const payload = {
            content: questionText.value.trim(),
            optionA: optionA.value.trim(),
            optionB: optionB.value.trim(),
            optionC: optionC.value.trim(),
            optionD: optionD.value.trim(),
            correctAnswer: correctAnswer.value,
            difficulty: questionDifficulty.value,
            chapter: questionChapter.value
        };

        if (editingId) {
            const question = questions.find((item) => item.id === editingId);
            Object.assign(question, payload);
            showToast("Đã cập nhật câu hỏi.");
        } else {
            questions.unshift({
                id: Date.now(),
                ...payload
            });
            showToast("Đã thêm câu hỏi mới.");
        }

        closeModal();
        renderQuestions();
    });

    renderTeacherOverview();
    renderTeacherCourses();
    renderTeacherTests();
    renderTeacherResults();
    renderTeacherReports();
    renderQuestions();
}

function initStudentPage() {
    const user = requireRole("student");
    if (!user) return;

    const courses = [
        {
            id: 1,
            code: "JAVA101",
            name: "Lập trình Java cơ bản",
            teacher: "ThS. Nguyễn Minh Anh",
            room: "P.204",
            progress: 68,
            nextLesson: "Mảng và phương thức",
            status: "Đang học",
            color: "green"
        },
        {
            id: 2,
            code: "WEB202",
            name: "Thiết kế giao diện Web",
            teacher: "ThS. Lê Hà Nam",
            room: "Lab 3",
            progress: 42,
            nextLesson: "Responsive layout",
            status: "Cần ôn tập",
            color: "orange"
        },
        {
            id: 3,
            code: "DBI201",
            name: "Cơ sở dữ liệu",
            teacher: "TS. Phạm Quốc Bình",
            room: "P.301",
            progress: 81,
            nextLesson: "Truy vấn nâng cao",
            status: "Tốt",
            color: "blue"
        }
    ];

    const tasks = [
        { title: "Quiz Java - Chương 5", course: "JAVA101", due: "Hạn nộp: 20:00 hôm nay", type: "Kiểm tra" },
        { title: "Bài tập CSS Grid", course: "WEB202", due: "Hạn nộp: ngày mai", type: "Bài tập" },
        { title: "Ôn tập SQL Join", course: "DBI201", due: "Kiểm tra: thứ Sáu", type: "Ôn tập" }
    ];

    const schedule = [
        { day: "Thứ 2", time: "07:30 - 09:30", title: "Lập trình Java cơ bản", room: "P.204" },
        { day: "Thứ 3", time: "13:00 - 15:00", title: "Thiết kế giao diện Web", room: "Lab 3" },
        { day: "Thứ 5", time: "09:45 - 11:45", title: "Cơ sở dữ liệu", room: "P.301" },
        { day: "Thứ 6", time: "14:00 - 14:45", title: "Quiz Java - Chương 5", room: "Online" }
    ];

    const results = [
        {
            id: 1,
            name: "Quiz 1",
            score: 8.0,
            duration: "15 phút",
            correct: 16,
            wrong: 4,
            total: 20,
            answersVisible: true,
            correctAnswers: "1A, 2B, 3C, 4B, 5D"
        },
        {
            id: 2,
            name: "Quiz 2",
            score: 9.5,
            duration: "12 phút",
            correct: 19,
            wrong: 1,
            total: 20,
            answersVisible: true,
            correctAnswers: "1B, 2A, 3D, 4C, 5A"
        },
        {
            id: 3,
            name: "Quiz 3",
            score: 7.0,
            duration: "18 phút",
            correct: 14,
            wrong: 6,
            total: 20,
            answersVisible: false,
            correctAnswers: ""
        },
        {
            id: 4,
            name: "Quiz 4",
            score: 8.5,
            duration: "16 phút",
            correct: 17,
            wrong: 3,
            total: 20,
            answersVisible: true,
            correctAnswers: "1C, 2C, 3A, 4D, 5B"
        },
        {
            id: 5,
            name: "Quiz 5",
            score: 9.0,
            duration: "14 phút",
            correct: 18,
            wrong: 2,
            total: 20,
            answersVisible: true,
            correctAnswers: "1D, 2B, 3B, 4A, 5C"
        }
    ];

    document.querySelector("#studentName").textContent = `${user.name} - Sinh viên`;
    document.querySelector("#logoutBtn").addEventListener("click", logout);

    const pageTitle = document.querySelector("#studentPageTitle");
    const navItems = [...document.querySelectorAll(".nav-item[data-view]")];
    const viewPanels = [...document.querySelectorAll("[data-view-panel]")];

    function showStudentView(view) {
        const activeItem = navItems.find((item) => item.dataset.view === view) || navItems[0];
        const targetView = activeItem.dataset.view;

        navItems.forEach((item) => item.classList.toggle("active", item === activeItem));
        viewPanels.forEach((panel) => {
            const views = panel.dataset.viewPanel.split(" ");
            panel.hidden = !views.includes(targetView);
        });

        pageTitle.textContent = activeItem.dataset.title;
        window.location.hash = targetView;
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    navItems.forEach((item) => {
        item.addEventListener("click", (event) => {
            event.preventDefault();
            showStudentView(item.dataset.view);
        });
    });

    const averageScore = results.reduce((sum, item) => sum + item.score, 0) / results.length;
    document.querySelector("#averageScore").textContent = averageScore.toFixed(1);
    document.querySelector("#completedTests").textContent = results.length;
    document.querySelector("#pendingTests").textContent = "2";
    document.querySelector("#latestScore").textContent = results[results.length - 1].score.toFixed(1);

    document.querySelector("#featuredCourseName").textContent = courses[0].name;
    document.querySelector("#featuredCourseMeta").textContent = `Tuần 5/10 · Bài tiếp theo: ${courses[0].nextLesson}`;

    const courseList = document.querySelector("#courseList");
    const taskList = document.querySelector("#taskList");
    const scheduleList = document.querySelector("#scheduleList");
    const assignmentListEl = document.querySelector('#assignmentList');
    const submitModal = document.querySelector('#submitModal');
    const submitForm = document.querySelector('#submitForm');
    const submitModalTitle = document.querySelector('#submitModalTitle');
    const submissionContent = document.querySelector('#submissionContent');
    const submissionFile = document.querySelector('#submissionFile');
    const historyTable = document.querySelector("#historyTable");
    const resultDetail = document.querySelector("#resultDetail");
    const scoreLine = document.querySelector("#scoreLine");
    const scoreDots = document.querySelector("#scoreDots");
    const scoreLabels = document.querySelector("#scoreLabels");

    function renderCourses() {
        courseList.innerHTML = courses.map((course) => `
            <article class="course-card ${course.color}">
                <div class="course-card-head">
                    <span class="course-code">${course.code}</span>
                    <span class="course-status">${course.status}</span>
                </div>
                <h3>${course.name}</h3>
                <p>${course.teacher} · ${course.room}</p>
                <div class="course-progress-row">
                    <span>Tiến độ</span>
                    <strong>${course.progress}%</strong>
                </div>
                <div class="course-progress">
                    <span style="width: ${course.progress}%"></span>
                </div>
                <div class="course-card-foot">
                    <span>Bài tiếp theo: ${course.nextLesson}</span>
                    <button class="text-btn" type="button" data-course-id="${course.id}">Vào học</button>
                </div>
            </article>
        `).join("");
    }

    function renderTasks() {
        taskList.innerHTML = tasks.map((task) => `
            <article class="task-item">
                <div>
                    <span class="task-type">${task.type}</span>
                    <strong>${task.title}</strong>
                    <p>${task.course} · ${task.due}</p>
                </div>
                <button class="text-btn" type="button">Mở</button>
            </article>
        `).join("");
    }

    function renderSchedule() {
        scheduleList.innerHTML = schedule.map((item) => `
            <article class="schedule-item">
                <div class="schedule-day">${item.day}</div>
                <div>
                    <strong>${item.title}</strong>
                    <span>${item.time} · ${item.room}</span>
                </div>
            </article>
        `).join("");
    }

    function scoreToY(score) {
        return 216 - (score / 10) * 186;
    }

    function renderChart() {
        const startX = 84;
        const gap = 122;
        const points = results.map((item, index) => {
            const x = startX + index * gap;
            const y = scoreToY(item.score);
            return { ...item, x, y };
        });

        scoreLine.setAttribute("points", points.map((point) => `${point.x},${point.y}`).join(" "));
        scoreDots.innerHTML = points.map((point) => `
            <circle class="score-dot" cx="${point.x}" cy="${point.y}" r="5" data-id="${point.id}"></circle>
        `).join("");
        scoreLabels.innerHTML = points.map((point) => `
            <text class="chart-x-label" x="${point.x}" y="244" text-anchor="middle">${point.name}</text>
        `).join("");
    }

    function renderHistory() {
        historyTable.innerHTML = results.map((item) => `
            <tr>
                <td><strong>${item.name}</strong></td>
                <td>${item.score.toFixed(1)}</td>
                <td>${item.duration}</td>
                <td>${item.correct}/${item.total}</td>
                <td>
                    <button class="text-btn" type="button" data-result-id="${item.id}">Xem chi tiết</button>
                </td>
            </tr>
        `).join("");
    }

    function renderAssignmentList() {
        if (!assignmentListEl) return;
        const visibleAssignments = assignments.filter(a => a.published);
        if (!visibleAssignments.length) {
            assignmentListEl.innerHTML = '<div class="empty-state">Không có bài tập.</div>';
            return;
        }
        assignmentListEl.innerHTML = visibleAssignments.map((a) => {
            const mySub = submissions.find(s => s.assignmentId === a.id && s.studentEmail === user.email);
            return `
                <article class="task-item">
                    <div>
                        <strong>${a.title}</strong>
                        <p>${a.desc || ''} · Hạn: ${a.due || '-'}</p>
                    </div>
                    <div>
                        ${mySub ? `<div>Trạng thái: ${mySub.score != null ? 'Đã chấm ('+mySub.score+')' : 'Đã nộp'}</div>` : ''}
                        <button class="text-btn" data-action="submit" data-id="${a.id}">${mySub ? 'Nộp lại' : 'Nộp bài'}</button>
                    </div>
                </article>
            `;
        }).join('');
    }

    // Open submit modal
    if (assignmentListEl) {
        assignmentListEl.addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-action]');
            if (!btn) return;
            const id = Number(btn.dataset.id);
            const assign = assignments.find(a => a.id === id);
            if (!assign) return;
            submitModalTitle.textContent = `Nộp: ${assign.title}`;
            submitForm.dataset.assignmentId = id;
            submissionContent.value = '';
            if (submissionFile) submissionFile.value = '';
            submitModal.showModal();
        });
    }

    if (submitForm) {
        submitForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const aid = Number(submitForm.dataset.assignmentId);
            const content = submissionContent.value.trim();
            const file = submissionFile && submissionFile.files && submissionFile.files[0] ? submissionFile.files[0].name : '';
            if (!content && !file) { showToast('Vui lòng nhập mô tả hoặc chọn file nộp bài'); return; }
            const existing = submissions.find(s => s.assignmentId === aid && s.studentEmail === user.email);
            const payload = {
                id: existing ? existing.id : Date.now(),
                assignmentId: aid,
                studentEmail: user.email,
                studentName: user.name,
                content,
                fileName: file,
                submittedAt: new Date().toISOString(),
                score: null
            };
            if (existing) {
                Object.assign(existing, payload);
            } else {
                submissions.push(payload);
            }
            saveSubmissions(submissions);
            submitModal.close();
            renderAssignmentList();
            showToast('Đã nộp bài');
        });
        const closeSubmit = document.querySelector('#closeSubmitModal');
        const cancelSubmit = document.querySelector('#cancelSubmit');
        if (closeSubmit) closeSubmit.addEventListener('click', () => submitModal.close());
        if (cancelSubmit) cancelSubmit.addEventListener('click', () => submitModal.close());
    }

    function renderDetail(result) {
        resultDetail.innerHTML = `
            <div class="detail-title">
                <span>${result.name}</span>
                <strong>${result.score.toFixed(1)}</strong>
            </div>
            <div class="detail-metrics">
                <div><span>Điểm</span><strong>${result.score.toFixed(1)}</strong></div>
                <div><span>Số câu đúng</span><strong>${result.correct}</strong></div>
                <div><span>Số câu sai</span><strong>${result.wrong}</strong></div>
                <div><span>Thời gian làm bài</span><strong>${result.duration}</strong></div>
            </div>
            <div class="answer-review">
                <span>Đáp án đúng</span>
                <p>${result.answersVisible ? result.correctAnswers : "Giảng viên chưa cho phép xem đáp án đúng."}</p>
            </div>
        `;
    }

    historyTable.addEventListener("click", (event) => {
        const button = event.target.closest("[data-result-id]");
        if (!button) return;

        const result = results.find((item) => item.id === Number(button.dataset.resultId));
        renderDetail(result);
        showStudentView("detail");
    });

    scoreDots.addEventListener("click", (event) => {
        const dot = event.target.closest("[data-id]");
        if (!dot) return;

        const result = results.find((item) => item.id === Number(dot.dataset.id));
        renderDetail(result);
        showStudentView("detail");
    });

    document.querySelector("#continueCourseBtn").addEventListener("click", () => {
        showToast(`Tiếp tục: ${courses[0].nextLesson}`);
    });

    document.querySelector("#viewScheduleBtn").addEventListener("click", () => {
        showStudentView("progress");
        window.setTimeout(() => {
            document.querySelector(".schedule-panel").scrollIntoView({ behavior: "smooth", block: "start" });
        }, 80);
    });

    courseList.addEventListener("click", (event) => {
        const button = event.target.closest("[data-course-id]");
        if (!button) return;

        const course = courses.find((item) => item.id === Number(button.dataset.courseId));
        showToast(`Đang mở khóa học: ${course.name}`);
    });

    renderCourses();
    renderTasks();
    renderSchedule();
    renderChart();
    renderHistory();
    renderDetail(results[results.length - 1]);
    renderAssignmentList();
    showStudentView(window.location.hash.replace("#", "") || "dashboard");
}

if (page === "login") {
    initLoginPage();
}

if (page === "teacher") {
    initTeacherPage();
}

if (page === "student") {
    initStudentPage();
}
