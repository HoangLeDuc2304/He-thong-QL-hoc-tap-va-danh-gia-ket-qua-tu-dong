## 🚀 HƯỚNG DẪN BẮT ĐẦU NHANH - STUDENT INTERFACE

Tài liệu này hướng dẫn các bước cơ bản để sử dụng Giao diện Sinh viên.

---

## 📦 Files Đã Tạo

### Components (4 files)
- ✅ `frontend/src/components/StudentInterface.tsx` - Component chính
- ✅ `frontend/src/components/StudentDashboard.tsx` - Dashboard
- ✅ `frontend/src/components/StudentCourses.tsx` - Danh sách khóa học
- ✅ `frontend/src/components/StudentExams.tsx` - Danh sách kỳ thi
- ✅ `frontend/src/components/StudentResults.tsx` - Kết quả thi

### Styles (5 files)
- ✅ `frontend/src/styles/StudentInterface.css` - Styles toàn cục
- ✅ `frontend/src/styles/StudentDashboard.css` - Styles Dashboard
- ✅ `frontend/src/styles/StudentCourses.css` - Styles Khóa học
- ✅ `frontend/src/styles/StudentExams.css` - Styles Kỳ thi
- ✅ `frontend/src/styles/StudentResults.css` - Styles Kết quả

### Types (1 file)
- ✅ `frontend/src/types/student.ts` - TypeScript interfaces

### Documentation (3 files)
- ✅ `README_STUDENT_INTERFACE.md` - Tài liệu chi tiết
- ✅ `INTEGRATION_GUIDE.tsx` - Hướng dẫn tích hợp
- ✅ `EXAMPLE_APP.tsx` - Ví dụ App.tsx hoàn chỉnh
- ✅ `QUICKSTART.md` - File này

---

## ⚡ Bước 1: Thêm imports vào App.tsx

```tsx
import StudentInterface from './components/StudentInterface';
import './styles/StudentInterface.css';
import './styles/StudentDashboard.css';
import './styles/StudentCourses.css';
import './styles/StudentExams.css';
import './styles/StudentResults.css';
```

---

## ⚡ Bước 2: Sử dụng Component

### Cách đơn giản nhất:

```tsx
function App() {
  return <StudentInterface />;
}

export default App;
```

### Với Logic Đăng nhập:

```tsx
import React, { useState } from 'react';
import StudentInterface from './components/StudentInterface';

function App() {
  const [isStudent, setIsStudent] = useState(false);

  if (isStudent) {
    return (
      <StudentInterface 
        onLogout={() => setIsStudent(false)} 
      />
    );
  }

  return (
    <div>
      <h1>Chọn vai trò</h1>
      <button onClick={() => setIsStudent(true)}>
        Sinh Viên
      </button>
    </div>
  );
}

export default App;
```

---

## ⚡ Bước 3: Chạy ứng dụng

```bash
# Cài đặt dependencies (nếu chưa)
npm install

# Chạy development server
npm run dev

# Hoặc
yarn dev
```

Mở browser và truy cập: `http://localhost:5173` (hoặc port của bạn)

---

## 🎯 Các Tính Năng Chính

### 📊 Dashboard
- Thông tin sinh viên
- Thống kê tổng quan (khóa học, kỳ thi, điểm trung bình)
- Kỳ thi sắp tới
- Khóa học nổi bật
- Hoạt động gần đây
- Menu hành động nhanh

### 📚 Khóa Học
- Danh sách tất cả khóa học
- Bộ lọc (Đang học, Hoàn thành, Tất cả)
- Chi tiết: Giáo viên, lịch học, tín chỉ
- Tiến độ học tập
- Điểm số
- Nút hành động

### 📝 Kỳ Thi
- Danh sách tất cả kỳ thi
- Bộ lọc (Sắp tới, Đang diễn ra, Đã kết thúc)
- Chi tiết: Thời gian, số câu hỏi
- Hiển thị điểm (khi đã kết thúc)
- Nút hành động

### 📈 Kết Quả
- Thống kê kết quả
- Bộ sắp xếp (Ngày, Điểm)
- Hiển thị điểm với màu sắc
- Nhận xét giáo viên
- Danh sách câu sai

---

## 🎨 Customization

### Thay đổi Màu sắc

Mở `frontend/src/styles/StudentInterface.css` và tìm:

```css
:root {
  --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --success: #4caf50;
  --error: #f44336;
  --warning: #ff9800;
  --info: #2196f3;
}
```

### Thay đổi Font chữ

```css
body {
  font-family: 'Your Font Family', sans-serif;
}
```

### Thêm Logo

Chỉnh sửa `StudentDashboard.tsx`:

```tsx
<div className="logo">
  <img src="/logo.png" alt="Logo" />
</div>
```

---

## 🔌 Kết nối Backend

### Tạo API Service

```tsx
// src/services/studentApiService.ts
const API_BASE_URL = 'http://localhost:8080/api';

export const studentApiService = {
  getStudentInfo: async () => {
    const response = await fetch(`${API_BASE_URL}/student/info`);
    return response.json();
  },

  getEnrolledCourses: async () => {
    const response = await fetch(`${API_BASE_URL}/student/courses`);
    return response.json();
  },

  getUpcomingExams: async () => {
    const response = await fetch(`${API_BASE_URL}/student/exams/upcoming`);
    return response.json();
  },

  getExamResults: async () => {
    const response = await fetch(`${API_BASE_URL}/student/results`);
    return response.json();
  },
};
```

### Sử dụng API Service

```tsx
useEffect(() => {
  const fetchData = async () => {
    try {
      const courses = await studentApiService.getEnrolledCourses();
      setCourses(courses);
    } catch (error) {
      console.error('Failed to fetch:', error);
    }
  };
  fetchData();
}, []);
```

---

## 📱 Responsive Design

Giao diện được tối ưu hóa cho:
- ✅ Desktop (1200px+)
- ✅ Tablet (768px - 1199px)
- ✅ Mobile (480px - 767px)
- ✅ Smartphone (<480px)

Để test:
```bash
# Sử dụng DevTools (F12) của browser
# Hoặc mở trên thiết bị thực
```

---

## 🐛 Troubleshooting

### Vấn đề: CSS không hiển thị
**Giải pháp**: Kiểm tra import CSS trong App.tsx, đảm bảo đường dẫn đúng

### Vấn đề: Component không render
**Giải pháp**: Kiểm tra browser console cho lỗi, xem React DevTools

### Vấn đề: Dữ liệu không tải
**Giải pháp**: Mở console (F12), kiểm tra lỗi network hoặc API

### Vấn đề: Layout lỗi trên mobile
**Giải pháp**: Kiểm tra media queries, test trên nhiều độ phân giải

---

## 📚 Tài liệu Bổ sung

- **README_STUDENT_INTERFACE.md** - Tài liệu chi tiết
- **INTEGRATION_GUIDE.tsx** - Các cách tích hợp
- **EXAMPLE_APP.tsx** - Ví dụ hoàn chỉnh

---

## ✨ Tính Năng Có Thể Thêm

1. **Cải thiện tìm kiếm** - Thêm search bar
2. **Export dữ liệu** - Download PDF/Excel
3. **Thông báo** - Toast notifications
4. **Dark mode** - Chế độ tối
5. **Lưu trữ** - Local storage cho dữ liệu
6. **Analytics** - Biểu đồ chi tiết
7. **Chat** - Hỏi đáp với giáo viên
8. **Push notifications** - Thông báo đẩy

---

## 🎓 Ví dụ Sử Dụng

### Ví dụ 1: Hiển thị Student Interface cho tất cả người dùng

```tsx
import StudentInterface from './components/StudentInterface';

function App() {
  return <StudentInterface />;
}

export default App;
```

### Ví dụ 2: Với vai trò người dùng

```tsx
import { useUser } from './hooks/useUser';
import StudentInterface from './components/StudentInterface';
import TeacherInterface from './components/TeacherInterface';

function App() {
  const { user } = useUser();

  if (user?.role === 'student') {
    return <StudentInterface />;
  }

  if (user?.role === 'teacher') {
    return <TeacherInterface />;
  }

  return <LoginPage />;
}

export default App;
```

### Ví dụ 3: Với React Router

```tsx
import { Routes, Route } from 'react-router-dom';
import StudentInterface from './components/StudentInterface';

function App() {
  return (
    <Routes>
      <Route path="/student/*" element={<StudentInterface />} />
    </Routes>
  );
}

export default App;
```

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:
1. Kiểm tra browser console (F12)
2. Đọc lại tài liệu README_STUDENT_INTERFACE.md
3. Kiểm tra imports và đường dẫn file
4. Xem ví dụ trong EXAMPLE_APP.tsx

---

## ✅ Checklist Bắt Đầu

- [ ] Đã copy tất cả files
- [ ] Đã thêm imports vào App.tsx
- [ ] Đã chạy `npm install` (nếu cần)
- [ ] Đã chạy `npm run dev`
- [ ] Đã mở browser tại localhost
- [ ] Đã kiểm tra components hiển thị đúng
- [ ] Đã kiểm tra responsive design

---

## 🎉 Hoàn Thành!

Bây giờ bạn có một giao diện sinh viên đầy đủ và chuyên nghiệp!

**Tiếp theo:**
- Kết nối backend API
- Tùy chỉnh giao diện theo nhu cầu
- Thêm tính năng mới
- Deploy ứng dụng

---

**Cập nhật lần cuối**: 2024
**Phiên bản**: 1.0
