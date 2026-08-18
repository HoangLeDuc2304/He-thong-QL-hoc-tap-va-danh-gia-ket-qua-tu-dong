# Tài liệu Giao diện Sinh viên (Student Interface)

## 📋 Giới thiệu

Tài liệu này mô tả cấu trúc và cách sử dụng giao diện sinh viên trong hệ thống quản lý học tập và đánh giá kết quả tự động.

Giao diện sinh viên cung cấp các tính năng chính:
- 📊 **Dashboard**: Tổng quan về học tập, thống kê điểm, hoạt động gần đây
- 📚 **Danh sách khóa học**: Xem chi tiết các khóa học, tiến độ học, điểm số
- 📝 **Danh sách kỳ thi**: Xem thông tin thi, lịch thi, trạng thái
- 📈 **Kết quả thi**: Xem điểm số, nhận xét giáo viên, phân tích kết quả

## 📁 Cấu trúc Tệp

```
frontend/src/
├── components/
│   ├── StudentInterface.tsx       # Component chính quản lý các view
│   ├── StudentDashboard.tsx       # Dashboard tổng quan
│   ├── StudentCourses.tsx         # Danh sách khóa học
│   ├── StudentExams.tsx           # Danh sách kỳ thi
│   └── StudentResults.tsx         # Kết quả thi
├── styles/
│   ├── StudentInterface.css       # Styles toàn cục
│   ├── StudentDashboard.css       # Styles Dashboard
│   ├── StudentCourses.css         # Styles Khóa học
│   ├── StudentExams.css           # Styles Kỳ thi
│   └── StudentResults.css         # Styles Kết quả
└── types/
    └── student.ts                 # TypeScript types cho Student Interface
```

## 🚀 Cách Sử dụng

### 1. Import Component Chính

```tsx
import StudentInterface from './components/StudentInterface';

// Sử dụng trong App
<StudentInterface onLogout={() => console.log('Logout')} />
```

### 2. Component Chi tiết

#### StudentDashboard
Hiển thị tổng quan về học tập của sinh viên:
- Thông tin sinh viên (avatar, mã sinh viên, lớp)
- Thống kê: tổng khóa học, kỳ thi, điểm trung bình
- Kỳ thi sắp tới
- Khóa học nổi bật
- Hoạt động gần đây
- Menu hành động nhanh

```tsx
<StudentDashboard
  onViewCourses={() => handleViewChange('courses')}
  onViewExams={() => handleViewChange('exams')}
  onViewResults={() => handleViewChange('results')}
/>
```

#### StudentCourses
Danh sách tất cả khóa học:
- Bộ lọc: Đang học, Đã hoàn thành, Tất cả
- Thông tin chi tiết: Giảng viên, lịch học, phòng học
- Tiến độ học tập (thanh progress)
- Điểm số (nếu có)
- Nút hành động: Xem tài liệu, bài tập, diễn đàn

```tsx
<StudentCourses onBack={() => handleViewChange('dashboard')} />
```

#### StudentExams
Danh sách tất cả kỳ thi:
- Bộ lọc: Sắp tới, Đang diễn ra, Đã kết thúc
- Thông tin thi: Môn học, ngày thi, giờ thi, thời lượng
- Hiển thị điểm khi đã kết thúc
- Nút hành động: Làm bài thi, xem hướng dẫn

```tsx
<StudentExams onBack={() => handleViewChange('dashboard')} />
```

#### StudentResults
Danh sách kết quả thi:
- Thống kê: Điểm trung bình, tỉ lệ đạt, tổng kỳ thi
- Bộ sắp xếp: Theo ngày, theo điểm
- Hiển thị điểm, trạng thái chấm
- Nhận xét của giáo viên
- Danh sách câu hỏi sai (nếu có)

```tsx
<StudentResults onBack={() => handleViewChange('dashboard')} />
```

## 📊 TypeScript Types

### StudentInfo
```typescript
interface StudentInfo {
  id: number;
  fullName: string;
  studentCode: string;
  email: string;
  phone: string;
  className: string;
  avatar?: string;
}
```

### EnrolledCourse
```typescript
interface EnrolledCourse {
  id: number;
  code: string;
  title: string;
  instructor: string;
  schedule: string;
  room: string;
  credits: number;
  status: 'Đang học' | 'Hoàn thành' | 'Bị trì hoãn';
  progress: number;
  grade?: number;
}
```

### ExamForStudent
```typescript
interface ExamForStudent {
  id: number;
  title: string;
  courseCode: string;
  courseName: string;
  type: 'Kiểm tra' | 'Thi giữa kỳ' | 'Thi cuối kỳ';
  startTime: string;
  endTime: string;
  duration: number;
  status: 'Sắp tới' | 'Đang diễn ra' | 'Đã kết thúc' | 'Vắng';
  questions: number;
  score?: number;
}
```

### ExamResult
```typescript
interface ExamResult {
  id: number;
  examTitle: string;
  courseCode: string;
  courseName: string;
  score: number;
  maxScore: number;
  percentage: number;
  submittedAt: string;
  status: 'Đã chấm' | 'Chờ chấm' | 'Vắng';
  teacherComment?: string;
  wrongQuestions?: number[];
}
```

## 🎨 Giao diện (CSS)

### Màu sắc Chính
- **Primary Gradient**: #667eea → #764ba2
- **Success**: #4caf50
- **Error**: #f44336
- **Warning**: #ff9800
- **Info**: #2196f3

### Font
- **Font Family**: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif
- **Font Sizes**: 
  - Header h1: 28px
  - Header h2: 22px
  - Header h3: 16-18px
  - Body: 14px
  - Small: 12px

### Responsive Design
- Desktop: Full width
- Tablet (≤768px): 2 columns grid
- Mobile (≤480px): 1 column grid

## 🔄 Luồng Dữ liệu

### Mô Phỏng Dữ Liệu
Hiện tại, các component sử dụng dữ liệu mô phỏng (`useState`). Để kết nối với backend:

1. **Tạo API Service** (nếu chưa có)
```tsx
export const studentApiService = {
  getStudentInfo: async () => { /* ... */ },
  getEnrolledCourses: async () => { /* ... */ },
  getUpcomingExams: async () => { /* ... */ },
  getExamResults: async () => { /* ... */ },
};
```

2. **Thay thế `useState` bằng `useEffect` + API call**
```tsx
useEffect(() => {
  const fetchCourses = async () => {
    try {
      const data = await studentApiService.getEnrolledCourses();
      setCourses(data);
    } catch (error) {
      console.error('Failed to fetch courses:', error);
    }
  };
  fetchCourses();
}, []);
```

## 🎯 Các Tính Năng Chính

### Dashboard
✅ Thống kê tổng quan
✅ Kỳ thi sắp tới
✅ Khóa học nổi bật
✅ Hoạt động gần đây
✅ Menu hành động nhanh

### Khóa học
✅ Danh sách với bộ lọc
✅ Thông tin chi tiết khóa học
✅ Tiến độ học tập
✅ Điểm số (nếu có)
✅ Nút hành động

### Kỳ thi
✅ Danh sách với bộ lọc
✅ Thông tin chi tiết kỳ thi
✅ Hiển thị kết quả (nếu đã kết thúc)
✅ Nút hành động
✅ Hiệu ứng "Vào làm bài thi ngay" cho thi đang diễn ra

### Kết quả
✅ Thống kê kết quả
✅ Bộ sắp xếp
✅ Hiển thị điểm với màu sắc
✅ Nhận xét của giáo viên
✅ Danh sách câu hỏi sai

## 🛠️ Phát triển Thêm

### Các Tính năng Có thể Thêm
1. **Search & Filter Nâng cao**
   - Tìm kiếm khóa học/kỳ thi theo tên
   - Filter theo môn học, giảng viên

2. **Thống kê Chi tiết**
   - Biểu đồ tiến độ học tập
   - Phân tích điểm theo từng chương
   - So sánh điểm với lớp

3. **Tương tác Người dùng**
   - Download bảng điểm
   - In kết quả thi
   - Gửi thắc mắc cho giáo viên

4. **Notifications**
   - Thông báo kỳ thi sắp tới
   - Thông báo cập nhật điểm
   - Thông báo từ giáo viên

5. **Profile & Settings**
   - Chỉnh sửa thông tin cá nhân
   - Đổi mật khẩu
   - Cài đặt thông báo

## 📱 Responsive Design

Giao diện được tối ưu hóa cho:
- **Desktop**: Màn hình ≥1200px
- **Tablet**: Màn hình 768px - 1199px
- **Mobile**: Màn hình <768px
- **Smartphone**: Màn hình <480px

## ⚙️ Cài đặt

### 1. Import CSS Toàn cục
```tsx
// Trong App.tsx hoặc index.tsx
import './styles/StudentInterface.css';
import './styles/StudentDashboard.css';
import './styles/StudentCourses.css';
import './styles/StudentExams.css';
import './styles/StudentResults.css';
```

### 2. Import Component
```tsx
import StudentInterface from './components/StudentInterface';
```

### 3. Sử dụng trong App
```tsx
function App() {
  return (
    <div className="app">
      <StudentInterface />
    </div>
  );
}
```

## 🚨 Troubleshooting

### Vấn đề: CSS không được áp dụng
**Giải pháp**: Kiểm tra import CSS trong App hoặc index file

### Vấn đề: Dữ liệu không hiển thị
**Giải pháp**: Kiểm tra browser console cho lỗi, đảm bảo dữ liệu được khởi tạo đúng

### Vấn đề: Layout bị lỗi trên mobile
**Giải pháp**: Kiểm tra media queries trong CSS, test trên các kích thước khác nhau

## 📞 Hỗ trợ

Để nhận hỗ trợ hoặc báo cáo lỗi:
1. Kiểm tra browser console cho error messages
2. Xem lại dữ liệu được truyền vào
3. Kiểm tra TypeScript types
4. Liên hệ với đội phát triển

---

**Cập nhật lần cuối**: 2024
**Phiên bản**: 1.0
