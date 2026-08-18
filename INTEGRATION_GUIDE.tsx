/**
 * HƯỚNG DẪN TÍCH HỢP STUDENT INTERFACE VÀO APP
 * 
 * File này chứa ví dụ về cách tích hợp giao diện sinh viên
 * vào ứng dụng React chính.
 */

import React, { useState } from 'react';
import StudentInterface from './components/StudentInterface';
import './styles/StudentInterface.css';
import './styles/StudentDashboard.css';
import './styles/StudentCourses.css';
import './styles/StudentExams.css';
import './styles/StudentResults.css';

type UserRole = 'student' | 'teacher' | 'admin' | null;

interface AppState {
  isLoggedIn: boolean;
  userRole: UserRole;
  userId?: number;
  userName?: string;
}

/**
 * CÁCH 1: Thêm Student Interface vào App hiện tại
 */
export const AppWithStudentInterface: React.FC = () => {
  const [appState, setAppState] = useState<AppState>({
    isLoggedIn: false,
    userRole: null,
  });

  // Mô phỏng đăng nhập
  const handleLogin = (role: UserRole) => {
    setAppState({
      isLoggedIn: true,
      userRole: role,
      userId: 1,
      userName: 'Nguyễn Văn A',
    });
  };

  // Đăng xuất
  const handleLogout = () => {
    setAppState({
      isLoggedIn: false,
      userRole: null,
    });
  };

  // Hiển thị giao diện tương ứng với vai trò
  if (!appState.isLoggedIn) {
    return (
      <div className="login-page">
        <h1>Đăng nhập</h1>
        <button onClick={() => handleLogin('student')}>Đăng nhập Sinh viên</button>
        <button onClick={() => handleLogin('teacher')}>Đăng nhập Giáo viên</button>
      </div>
    );
  }

  if (appState.userRole === 'student') {
    return <StudentInterface onLogout={handleLogout} />;
  }

  // Các giao diện khác...
  return (
    <div>
      <h1>Xin chào {appState.userName}</h1>
      <button onClick={handleLogout}>Đăng xuất</button>
    </div>
  );
};

/**
 * CÁCH 2: Hiển thị Student Interface trong một Route
 * (Nếu sử dụng React Router)
 */
export const AppWithRouting: React.FC = () => {
  // Giả sử bạn sử dụng React Router v6
  // import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

  return (
    <div>
      {/* 
        <Router>
          <Routes>
            <Route path="/student/*" element={<StudentInterface />} />
            <Route path="/teacher/*" element={<TeacherInterface />} />
            <Route path="/admin/*" element={<AdminInterface />} />
          </Routes>
        </Router>
      */}

      <h1>Ví dụ sử dụng React Router</h1>
      <p>Xem comments phía trên</p>
    </div>
  );
};

/**
 * CÁCH 3: Sử dụng Modal hoặc Component Overlay
 */
export const AppWithModal: React.FC = () => {
  const [showStudentInterface, setShowStudentInterface] = useState(false);

  return (
    <div>
      <h1>Trang Chính</h1>
      <button onClick={() => setShowStudentInterface(true)}>
        Mở Giao diện Sinh viên
      </button>

      {showStudentInterface && (
        <div className="modal-overlay">
          <div className="modal-content">
            <button 
              className="close-btn" 
              onClick={() => setShowStudentInterface(false)}
            >
              ×
            </button>
            <StudentInterface 
              onLogout={() => setShowStudentInterface(false)} 
            />
          </div>
        </div>
      )}
    </div>
  );
};

/**
 * CÁCH 4: Sử dụng Context API để Quản lý User
 */

import { createContext, useContext } from 'react';

interface UserContextType {
  user: {
    id: number;
    name: string;
    role: UserRole;
  } | null;
  login: (role: UserRole) => void;
  logout: () => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserContextType['user']>(null);

  const login = (role: UserRole) => {
    setUser({
      id: 1,
      name: 'Nguyễn Văn A',
      role: role,
    });
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <UserContext.Provider value={{ user, login, logout }}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useUser must be used within UserProvider');
  }
  return context;
};

export const AppWithContext: React.FC = () => {
  const { user, logout } = useUser();

  if (!user) {
    return <h1>Vui lòng đăng nhập</h1>;
  }

  if (user.role === 'student') {
    return <StudentInterface onLogout={logout} />;
  }

  return <h1>Xin chào {user.name}</h1>;
};

/**
 * CÁCH 5: Tích hợp vào App.tsx hiện tại
 * 
 * Ví dụ đơn giản nhất: Thay thế nội dung App
 */
export const SimpleApp: React.FC = () => {
  const [userRole, setUserRole] = useState<UserRole>(null);

  const handleRoleSelect = (role: UserRole) => {
    setUserRole(role);
  };

  if (userRole === 'student') {
    return (
      <>
        <StudentInterface 
          onLogout={() => handleRoleSelect(null)} 
        />
      </>
    );
  }

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>Hệ Thống Quản Lý Học Tập</h1>
      <div style={{ marginTop: '20px' }}>
        <button 
          onClick={() => handleRoleSelect('student')}
          style={{ marginRight: '10px', padding: '10px 20px' }}
        >
          Sinh Viên
        </button>
        <button 
          onClick={() => handleRoleSelect('teacher')}
          style={{ marginRight: '10px', padding: '10px 20px' }}
        >
          Giáo Viên
        </button>
        <button 
          onClick={() => handleRoleSelect('admin')}
          style={{ padding: '10px 20px' }}
        >
          Quản Trị Viên
        </button>
      </div>
    </div>
  );
};

/**
 * ============================================
 * HƯỚNG DẪN SỬ DỤNG TRONG App.tsx
 * ============================================
 * 
 * 1. Thêm imports vào App.tsx:
 *    import StudentInterface from './components/StudentInterface';
 *    import './styles/StudentInterface.css';
 *    import './styles/StudentDashboard.css';
 *    import './styles/StudentCourses.css';
 *    import './styles/StudentExams.css';
 *    import './styles/StudentResults.css';
 * 
 * 2. Chọn một trong các cách trên:
 *    - Cách 1: Thay toàn bộ App component
 *    - Cách 2: Sử dụng React Router
 *    - Cách 3: Hiển thị trong Modal/Drawer
 *    - Cách 4: Sử dụng Context API
 *    - Cách 5: Sử dụng SimpleApp (cách đơn giản nhất)
 * 
 * 3. Export component được chọn:
 *    export default SimpleApp;
 * 
 * ============================================
 */

// Export component mặc định (chọn một trong số trên)
export default SimpleApp;
