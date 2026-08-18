/**
 * VÍ DỤ: CẬP NHẬT App.tsx ĐỂ SỬ DỤNG STUDENT INTERFACE
 * 
 * Đây là cách đơn giản nhất để tích hợp giao diện sinh viên
 * vào ứng dụng hiện tại.
 * 
 * ⚠️ LƯU Ý: Hãy backup file App.tsx cũ trước khi thay đổi!
 */

import React, { useState } from 'react';
import StudentInterface from './components/StudentInterface';

// Import CSS toàn cục
import './styles/StudentInterface.css';
import './styles/StudentDashboard.css';
import './styles/StudentCourses.css';
import './styles/StudentExams.css';
import './styles/StudentResults.css';

type AppView = 'welcome' | 'student' | 'teacher' | 'admin';

interface AppState {
  currentView: AppView;
  isLoggedIn: boolean;
  userRole?: 'student' | 'teacher' | 'admin';
}

const App: React.FC = () => {
  const [appState, setAppState] = useState<AppState>({
    currentView: 'welcome',
    isLoggedIn: false,
  });

  /**
   * Xử lý khi người dùng chọn vai trò
   */
  const handleSelectRole = (role: 'student' | 'teacher' | 'admin') => {
    setAppState({
      currentView: role,
      isLoggedIn: true,
      userRole: role,
    });
  };

  /**
   * Xử lý đăng xuất
   */
  const handleLogout = () => {
    setAppState({
      currentView: 'welcome',
      isLoggedIn: false,
    });
  };

  /**
   * Hiển thị giao diện chào mừng
   */
  if (appState.currentView === 'welcome') {
    return <WelcomeScreen onSelectRole={handleSelectRole} />;
  }

  /**
   * Hiển thị giao diện sinh viên
   */
  if (appState.currentView === 'student') {
    return <StudentInterface onLogout={handleLogout} />;
  }

  /**
   * Các giao diện khác có thể được thêm ở đây
   */
  if (appState.currentView === 'teacher') {
    return (
      <div style={{ padding: '20px' }}>
        <button onClick={handleLogout}>← Quay lại</button>
        <h1>Giao diện Giáo viên (Sắp có)</h1>
        <p>Giao diện giáo viên đang được phát triển...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '20px' }}>
      <button onClick={handleLogout}>← Quay lại</button>
      <h1>Giao diện Quản trị (Sắp có)</h1>
      <p>Giao diện quản trị đang được phát triển...</p>
    </div>
  );
};

/**
 * Component giao diện chào mừng
 */
interface WelcomeScreenProps {
  onSelectRole: (role: 'student' | 'teacher' | 'admin') => void;
}

const WelcomeScreen: React.FC<WelcomeScreenProps> = ({ onSelectRole }) => {
  return (
    <div style={styles.welcomeContainer}>
      <div style={styles.welcomeContent}>
        <div style={styles.welcomeHeader}>
          <h1 style={styles.welcomeTitle}>
            🎓 Hệ Thống Quản Lý Học Tập
          </h1>
          <p style={styles.welcomeSubtitle}>
            và Đánh Giá Kết Quả Tự Động
          </p>
        </div>

        <div style={styles.descriptionContainer}>
          <p style={styles.description}>
            Chọn vai trò của bạn để tiếp tục:
          </p>
        </div>

        <div style={styles.rolesContainer}>
          {/* Role Card - Student */}
          <RoleCard
            icon="👨‍🎓"
            title="Sinh Viên"
            description="Xem khóa học, kỳ thi, kết quả học tập"
            onClick={() => onSelectRole('student')}
          />

          {/* Role Card - Teacher */}
          <RoleCard
            icon="👨‍🏫"
            title="Giáo Viên"
            description="Quản lý khóa học, tạo kỳ thi, chấm điểm"
            onClick={() => onSelectRole('teacher')}
            disabled
          />

          {/* Role Card - Admin */}
          <RoleCard
            icon="👨‍💼"
            title="Quản Trị Viên"
            description="Quản lý hệ thống, người dùng, cấu hình"
            onClick={() => onSelectRole('admin')}
            disabled
          />
        </div>

        <div style={styles.featuresList}>
          <h3>✨ Các Tính Năng Chính:</h3>
          <ul>
            <li>📚 Quản lý khóa học và tài liệu học tập</li>
            <li>📝 Tạo và quản lý kỳ thi trắc nghiệm</li>
            <li>⚡ Chấm điểm tự động</li>
            <li>📊 Thống kê và phân tích kết quả</li>
            <li>📱 Giao diện responsive thân thiện với người dùng</li>
          </ul>
        </div>

        <footer style={styles.footer}>
          <p>© 2024 Hệ Thống Quản Lý Học Tập. Tất cả quyền được bảo lưu.</p>
        </footer>
      </div>
    </div>
  );
};

/**
 * Component Card cho mỗi vai trò
 */
interface RoleCardProps {
  icon: string;
  title: string;
  description: string;
  onClick: () => void;
  disabled?: boolean;
}

const RoleCard: React.FC<RoleCardProps> = ({
  icon,
  title,
  description,
  onClick,
  disabled = false,
}) => {
  return (
    <button
      style={{
        ...styles.roleCard,
        ...(disabled ? styles.roleCardDisabled : {}),
      }}
      onClick={onClick}
      disabled={disabled}
    >
      <div style={styles.roleIcon}>{icon}</div>
      <h3 style={styles.roleTitle}>{title}</h3>
      <p style={styles.roleDescription}>{description}</p>
      {disabled && <span style={styles.comingSoon}>Sắp có</span>}
    </button>
  );
};

/**
 * CSS-in-JS Styles
 */
const styles: Record<string, React.CSSProperties> = {
  welcomeContainer: {
    width: '100%',
    minHeight: '100vh',
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '20px',
    fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
  },

  welcomeContent: {
    maxWidth: '900px',
    width: '100%',
    background: 'white',
    borderRadius: '15px',
    padding: '50px 40px',
    boxShadow: '0 10px 40px rgba(0, 0, 0, 0.2)',
  },

  welcomeHeader: {
    textAlign: 'center',
    marginBottom: '40px',
  },

  welcomeTitle: {
    margin: '0 0 10px 0',
    fontSize: '32px',
    color: '#333',
    fontWeight: 'bold',
  },

  welcomeSubtitle: {
    margin: 0,
    fontSize: '18px',
    color: '#666',
  },

  descriptionContainer: {
    textAlign: 'center',
    marginBottom: '30px',
  },

  description: {
    fontSize: '16px',
    color: '#666',
    margin: 0,
  },

  rolesContainer: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
    gap: '20px',
    marginBottom: '40px',
  },

  roleCard: {
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    color: 'white',
    border: 'none',
    borderRadius: '12px',
    padding: '30px 20px',
    cursor: 'pointer',
    transition: 'all 0.3s ease',
    textAlign: 'center',
    fontSize: '16px',
    fontWeight: '600',
    minHeight: '220px',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
  },

  roleCardDisabled: {
    background: 'linear-gradient(135deg, #ccc 0%, #999 100%)',
    cursor: 'not-allowed',
    opacity: 0.6,
  },

  roleIcon: {
    fontSize: '48px',
    marginBottom: '15px',
    display: 'block',
  },

  roleTitle: {
    margin: '10px 0',
    fontSize: '20px',
    fontWeight: '700',
  },

  roleDescription: {
    margin: '10px 0',
    fontSize: '14px',
    opacity: 0.9,
  },

  comingSoon: {
    display: 'block',
    marginTop: '10px',
    fontSize: '12px',
    opacity: 0.8,
    textTransform: 'uppercase',
    letterSpacing: '1px',
  },

  featuresList: {
    background: '#f9f9f9',
    borderRadius: '12px',
    padding: '20px',
    marginBottom: '30px',
  },

  footer: {
    textAlign: 'center',
    borderTop: '1px solid #e0e0e0',
    paddingTop: '20px',
    color: '#999',
    fontSize: '14px',
  },
};

// Thêm hover effect cho role cards (cần CSS hoặc styled-components)
const styleSheet = document.createElement('style');
styleSheet.textContent = `
  button[style*="background: linear-gradient(135deg, #667eea"]:hover:not(:disabled) {
    transform: translateY(-5px);
    box-shadow: 0 15px 40px rgba(102, 126, 234, 0.4);
  }

  @media (max-width: 768px) {
    /* Styles cho tablet */
  }

  @media (max-width: 480px) {
    /* Styles cho mobile */
  }
`;
document.head.appendChild(styleSheet);

export default App;

/**
 * ============================================
 * HƯỚNG DẪN SỬ DỤNG
 * ============================================
 * 
 * 1. Backup file App.tsx cũ:
 *    mv src/App.tsx src/App.tsx.backup
 * 
 * 2. Thay thế nội dung App.tsx với code trên
 * 
 * 3. Kiểm tra imports:
 *    - Đảm bảo tất cả imports là đúng đường dẫn
 * 
 * 4. Chạy development server:
 *    npm run dev
 *    hoặc
 *    yarn dev
 * 
 * 5. Kiểm tra kết quả:
 *    - Mở http://localhost:5173 (hoặc port của bạn)
 *    - Bấm "Sinh Viên" để xem Student Interface
 * 
 * ============================================
 */
