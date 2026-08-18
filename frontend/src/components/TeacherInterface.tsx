import React, { useEffect, useMemo, useState } from 'react';
import type { CourseMaterial as MaterialItem } from '../types/material';
import { coursesApiService, examApiService, questionsApiService, materialsApiService, umlApiService } from '../services/apiService';
import './styles/TeacherInterface.css';

// =============================================================================
// 1. INTERFACES
// =============================================================================
interface Course { id: number; code: string; title: string; description: string; }
interface Subject { id: number; courseId: number; code: string; title: string; }
interface QuestionOption { content: string; isCorrect: boolean; }
interface Question {
  id: number; subjectId: number; content: string; chapterTopic: string; difficulty: string;
  questionType?: string; options?: QuestionOption[];
}
interface Exam {
  id: number; subjectId: number; title: string; durationMinutes: number;
  startTime: string; endTime: string; status: string; questions?: number;
}
interface UmlAssignment {
  id: number; subjectId: number; title: string; description: string;
  dueDate: string; maxScore: number; rubricCriteria?: string;
}
type CourseMaterial = MaterialItem;

type ViewKey = 'overview' | 'courses' | 'questions' | 'exams' | 'results' | 'reports';

interface TeacherInterfaceProps {
  onLogout?: () => void;
  materials?: CourseMaterial[];
  setMaterials?: (materials: CourseMaterial[]) => void;
  userName?: string;
}

const navItems: { key: ViewKey; label: string; icon: string }[] = [
  { key: 'overview', label: 'Tổng quan', icon: '📊' },
  { key: 'courses', label: 'Khóa học', icon: '📚' },
  { key: 'questions', label: 'Ngân hàng câu hỏi', icon: '❓' },
  { key: 'exams', label: 'Bài kiểm tra', icon: '📝' },
  { key: 'results', label: 'Kết quả học tập', icon: '📈' },
  { key: 'reports', label: 'Báo cáo', icon: '📄' },
];

const fallbackCourses: Course[] = [
  { id: 1, code: 'CNTT2026', title: 'Công nghệ thông tin', description: 'Chương trình đào tạo ngành Công nghệ thông tin' },
  { id: 2, code: 'SE2026', title: 'Kỹ thuật phần mềm', description: 'Chương trình đào tạo Kỹ thuật phần mềm' },
];

const fallbackSubjects: Subject[] = [
  { id: 1, courseId: 1, code: 'JAVA01', title: 'Lập trình Java' },
  { id: 2, courseId: 1, code: 'DB01', title: 'Cơ sở dữ liệu' },
  { id: 3, courseId: 1, code: 'WEB01', title: 'Lập trình Web' },
  { id: 4, courseId: 2, code: 'SE01', title: 'Công nghệ phần mềm' },
];

const fallbackQuestions: Question[] = [
  { id: 1, subjectId: 1, content: 'Java được phát triển bởi công ty nào?', chapterTopic: 'Java cơ bản', difficulty: 'EASY' },
  { id: 2, subjectId: 1, content: 'Tính chất nào cho phép một lớp kế thừa thuộc tính và phương thức?', chapterTopic: 'OOP', difficulty: 'EASY' },
  { id: 6, subjectId: 2, content: 'Lệnh SQL nào dùng để lấy dữ liệu từ bảng?', chapterTopic: 'SQL', difficulty: 'EASY' },
  { id: 9, subjectId: 3, content: 'Thẻ HTML nào dùng để tạo liên kết?', chapterTopic: 'HTML', difficulty: 'EASY' },
];

const fallbackExams: Exam[] = [
  { id: 1, subjectId: 1, title: 'Kiểm tra Java cơ bản', durationMinutes: 30, startTime: new Date(Date.now() + 86400000).toISOString(), endTime: new Date(Date.now() + 90000000).toISOString(), status: 'Sắp diễn ra', questions: 5 },
  { id: 2, subjectId: 2, title: 'Kiểm tra Cơ sở dữ liệu', durationMinutes: 45, startTime: new Date(Date.now() + 172800000).toISOString(), endTime: new Date(Date.now() + 175200000).toISOString(), status: 'Sắp diễn ra', questions: 3 },
];

// =============================================================================
// 2. COMPONENT CON (Phải khai báo trước)
// =============================================================================

const TeacherOverview: React.FC<{ courses: Course[]; questions: Question[]; exams: Exam[]; }> = ({ courses, questions, exams }) => (
  <div className="teacher-overview-stats">
    <div className="teacher-stat-card"><h3>{courses?.length || 0}</h3><p>Khóa học</p></div>
    <div className="teacher-stat-card"><h3>{questions?.length || 0}</h3><p>Câu hỏi</p></div>
    <div className="teacher-stat-card"><h3>{exams?.length || 0}</h3><p>Bài thi hiện có</p></div>
  </div>
);

const DIFFICULTY_OPTIONS = [
  { value: 'EASY', label: 'Dễ' },
  { value: 'MEDIUM', label: 'Trung bình' },
  { value: 'HARD', label: 'Khó' },
];

const QUESTION_TYPE_OPTIONS = [
  { value: 'SINGLE_CHOICE', label: 'Một đáp án đúng' },
  { value: 'MULTIPLE_CHOICE', label: 'Nhiều đáp án đúng' },
];

const emptyOptions = (): QuestionOption[] => [
  { content: '', isCorrect: false },
  { content: '', isCorrect: false },
];

const emptyNewQuestion = () => ({
  subjectId: 0,
  chapterTopic: '',
  content: '',
  questionType: 'SINGLE_CHOICE',
  difficulty: 'EASY',
  options: emptyOptions(),
});

const TeacherQuestionBank: React.FC<{ questions: Question[]; setQuestions: (qs: Question[]) => void; subjects: Subject[]; showToast: (m: string) => void }> = ({ questions, setQuestions, subjects, showToast }) => {
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [newQ, setNewQ] = useState(emptyNewQuestion());

  useEffect(() => {
    if (subjects.length > 0 && newQ.subjectId === 0) {
      setNewQ(prev => ({ ...prev, subjectId: subjects[0].id }));
    }
  }, [subjects, newQ.subjectId]);

  const updateOption = (index: number, changes: Partial<QuestionOption>) => {
    setNewQ(prev => ({
      ...prev,
      options: prev.options.map((opt, i) => {
        if (i !== index) {
          // Chỉ 1 đáp án đúng khi là câu hỏi 1 lựa chọn
          return prev.questionType === 'SINGLE_CHOICE' && changes.isCorrect ? { ...opt, isCorrect: false } : opt;
        }
        return { ...opt, ...changes };
      }),
    }));
  };

  const addOption = () => setNewQ(prev => ({ ...prev, options: [...prev.options, { content: '', isCorrect: false }] }));

  const removeOption = (index: number) => setNewQ(prev => ({ ...prev, options: prev.options.filter((_, i) => i !== index) }));

  const resetForm = () => {
    setNewQ(emptyNewQuestion());
    setShowForm(false);
  };

  const handleSave = async () => {
    const filledOptions = newQ.options.filter(opt => opt.content.trim() !== '');
    if (!newQ.content.trim() || !newQ.chapterTopic.trim() || newQ.subjectId === 0) {
      return showToast('Vui lòng điền đủ thông tin.');
    }
    if (filledOptions.length < 2) {
      return showToast('Cần ít nhất 2 đáp án.');
    }
    if (!filledOptions.some(opt => opt.isCorrect)) {
      return showToast('Hãy chọn ít nhất 1 đáp án đúng.');
    }
    setLoading(true);
    try {
      const payload = { ...newQ, options: filledOptions };
      const saved = await questionsApiService.createQuestion(payload);
      setQuestions([saved, ...questions]);
      resetForm();
      showToast('Đã lưu câu hỏi thành công!');
    } catch (e) { showToast('Lỗi khi lưu câu hỏi.'); } finally { setLoading(false); }
  };

  return (
    <div className="teacher-section">
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
        <h2>Ngân hàng câu hỏi</h2>
        <button className="teacher-button teacher-button--primary" onClick={() => setShowForm(true)}>+ Thêm câu hỏi</button>
      </div>
      {showForm && (
        <div className="teacher-modal-overlay">
          <div className="teacher-modal">
            <h3>Tạo câu hỏi mới</h3>

            <div className="teacher-question-form-table">
              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Môn học</label>
                <div className="teacher-question-form-value">
                  <select value={newQ.subjectId} onChange={e => setNewQ({ ...newQ, subjectId: parseInt(e.target.value) })}>
                    {subjects.map(s => <option key={s.id} value={s.id}>{s.title}</option>)}
                  </select>
                </div>
              </div>

              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Chủ đề / chương</label>
                <div className="teacher-question-form-value">
                  <input placeholder="Ví dụ: OOP, SQL..." value={newQ.chapterTopic} onChange={e => setNewQ({ ...newQ, chapterTopic: e.target.value })} />
                </div>
              </div>

              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Nội dung câu hỏi</label>
                <div className="teacher-question-form-value">
                  <textarea placeholder="Nhập nội dung câu hỏi..." value={newQ.content} onChange={e => setNewQ({ ...newQ, content: e.target.value })} />
                </div>
              </div>

              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Loại câu hỏi</label>
                <div className="teacher-question-form-value">
                  <select
                    value={newQ.questionType}
                    onChange={e => {
                      const questionType = e.target.value;
                      setNewQ(prev => ({
                        ...prev,
                        questionType,
                        options: questionType === 'SINGLE_CHOICE'
                          ? prev.options.map((opt, i) => ({ ...opt, isCorrect: i === prev.options.findIndex(o => o.isCorrect) }))
                          : prev.options,
                      }));
                    }}
                  >
                    {QUESTION_TYPE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                  </select>
                </div>
              </div>

              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Độ khó</label>
                <div className="teacher-question-form-value">
                  <select value={newQ.difficulty} onChange={e => setNewQ({ ...newQ, difficulty: e.target.value })}>
                    {DIFFICULTY_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                  </select>
                </div>
              </div>

              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Đáp án</label>
                <div className="teacher-question-form-value">
                  <div className="teacher-answer-table">
                    <div className="teacher-answer-table__head">
                      <span>{newQ.questionType === 'SINGLE_CHOICE' ? 'Đúng (1 lựa chọn)' : 'Đúng (nhiều lựa chọn)'}</span>
                      <span>Nội dung đáp án</span>
                      <span>Thao tác</span>
                    </div>
                    {newQ.options.map((opt, index) => (
                      <div key={index} className="teacher-answer-table__row">
                        <div className="teacher-answer-table__flag">
                          <input
                            type={newQ.questionType === 'SINGLE_CHOICE' ? 'radio' : 'checkbox'}
                            name="correct-option"
                            checked={opt.isCorrect}
                            onChange={e => updateOption(index, { isCorrect: e.target.checked })}
                          />
                        </div>
                        <input
                          placeholder={`Đáp án ${index + 1}`}
                          value={opt.content}
                          onChange={e => updateOption(index, { content: e.target.value })}
                        />
                        <button
                          type="button"
                          className="teacher-button"
                          onClick={() => removeOption(index)}
                          disabled={newQ.options.length <= 2}
                        >
                          Xóa
                        </button>
                      </div>
                    ))}
                    <button type="button" className="teacher-button teacher-answer-table__add" onClick={addOption}>+ Thêm đáp án</button>
                  </div>
                </div>
              </div>
            </div>

            <div className="modal-actions" style={{ marginTop: '10px' }}>
              <button className="teacher-button teacher-button--primary" onClick={handleSave} disabled={loading}>{loading ? 'Đang lưu...' : 'Lưu vào Database'}</button>
              <button className="teacher-button" onClick={resetForm}>Hủy</button>
            </div>
          </div>
        </div>
      )}
      <div className="teacher-questions-list">
        {questions.map(q => (
          <div key={q.id} className="teacher-question-item">
            <strong>{q.content}</strong>
            <p>Môn: {subjects.find(s => s.id === q.subjectId)?.title || 'N/A'} | Chủ đề: {q.chapterTopic || 'N/A'}</p>
            <p>Loại: {QUESTION_TYPE_OPTIONS.find(o => o.value === q.questionType)?.label || q.questionType || 'N/A'} | Độ khó: {DIFFICULTY_OPTIONS.find(o => o.value === q.difficulty)?.label || q.difficulty}</p>
            {q.options && q.options.length > 0 && (
              <ul style={{ margin: '6px 0 0', paddingLeft: '18px' }}>
                {q.options.map((opt, i) => (
                  <li key={i} style={{ color: opt.isCorrect ? '#16a34a' : 'inherit', fontWeight: opt.isCorrect ? 600 : 400 }}>
                    {opt.content}{opt.isCorrect ? ' (đúng)' : ''}
                  </li>
                ))}
              </ul>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

const toDatetimeLocal = (iso: string) => {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return '';
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const emptyNewExam = () => ({ title: '', durationMinutes: 30, startTime: '', endTime: '', subjectId: 0 });

const TeacherExamsPanel: React.FC<{ exams: Exam[]; questions: Question[]; setExams: (exs: Exam[]) => void; subjects: Subject[]; showToast: (m: string) => void }> = ({ exams, questions, setExams, subjects, showToast }) => {
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [newExam, setNewExam] = useState(emptyNewExam());
  const [editingExamId, setEditingExamId] = useState<number | null>(null);

  useEffect(() => {
    if (subjects.length > 0 && newExam.subjectId === 0) {
      setNewExam(prev => ({ ...prev, subjectId: subjects[0].id }));
    }
  }, [subjects, newExam.subjectId]);

  const resetForm = () => {
    setNewExam(emptyNewExam());
    setSelectedIds([]);
    setEditingExamId(null);
    setShowForm(false);
  };

  const handleEdit = async (exam: Exam) => {
    setNewExam({
      title: exam.title,
      durationMinutes: exam.durationMinutes,
      startTime: toDatetimeLocal(exam.startTime),
      endTime: toDatetimeLocal(exam.endTime),
      subjectId: exam.subjectId,
    });
    setEditingExamId(exam.id);
    setShowForm(true);
    try {
      const examQuestions = await examApiService.getExamQuestions(exam.id);
      setSelectedIds(examQuestions.map((eq: any) => eq.question?.id).filter(Boolean));
    } catch {
      setSelectedIds([]);
      showToast('Không thể tải danh sách câu hỏi hiện có của bài thi.');
    }
  };

  const handleDelete = async (exam: Exam) => {
    if (!window.confirm(`Xóa bài kiểm tra "${exam.title}"? Hành động này không thể hoàn tác.`)) return;
    try {
      await examApiService.deleteExam(exam.id);
      setExams(exams.filter(e => e.id !== exam.id));
      showToast('Đã xóa bài kiểm tra.');
    } catch (e) {
      showToast(e instanceof Error ? e.message : 'Lỗi khi xóa bài thi.');
    }
  };

  const handleCreate = async () => {
    if (!newExam.title || !newExam.startTime || !newExam.endTime || newExam.subjectId === 0) return showToast('Vui lòng nhập đủ thông tin.');
    if (selectedIds.length === 0) return showToast('Hãy chọn ít nhất 1 câu hỏi.');
    setLoading(true);
    try {
      const payload = { ...newExam, selectedQuestionIds: selectedIds };
      if (editingExamId) {
        const saved = await examApiService.updateExam(editingExamId, payload);
        setExams(exams.map(e => (e.id === editingExamId ? { ...e, ...saved, subjectId: saved.subject?.id ?? newExam.subjectId, questions: selectedIds.length } : e)));
        showToast('Đã cập nhật bài kiểm tra!');
      } else {
        const saved = await examApiService.createExam(payload);
        setExams([{ ...saved, subjectId: saved.subject?.id ?? newExam.subjectId, questions: selectedIds.length }, ...exams]);
        showToast('Bài thi đã được lưu vĩnh viễn!');
      }
      resetForm();
    } catch (e) {
      showToast(e instanceof Error ? e.message : 'Lỗi khi lưu bài thi.');
    } finally { setLoading(false); }
  };

  return (
    <div className="teacher-section">
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
        <h2>Bài kiểm tra</h2>
        <button className="teacher-button teacher-button--primary" onClick={() => setShowForm(true)}>+ Tạo bài thi mới</button>
      </div>
      {showForm && (
        <div className="teacher-modal-overlay">
          <div className="teacher-modal">
            <h3>{editingExamId ? 'Chỉnh sửa đề thi' : 'Cấu hình đề thi'}</h3>
            <select value={newExam.subjectId} onChange={e => setNewExam({ ...newExam, subjectId: parseInt(e.target.value) })}>
              {subjects.map(s => <option key={s.id} value={s.id}>{s.title}</option>)}
            </select>
            <input placeholder="Tiêu đề..." value={newExam.title} onChange={e => setNewExam({ ...newExam, title: e.target.value })} style={{ width: '100%', margin: '10px 0' }} />
            <input type="datetime-local" value={newExam.startTime} onChange={e => setNewExam({ ...newExam, startTime: e.target.value })} style={{ width: '100%' }} />
            <input type="datetime-local" value={newExam.endTime} onChange={e => setNewExam({ ...newExam, endTime: e.target.value })} style={{ width: '100%', margin: '10px 0' }} />
            <div style={{ maxHeight: '150px', overflowY: 'auto', border: '1px solid #ddd', padding: '10px' }}>
              {questions.filter(q => q.subjectId === newExam.subjectId).map(q => (
                <label key={q.id} style={{ display: 'block', fontSize: '12px' }}>
                  <input type="checkbox" checked={selectedIds.includes(q.id)} onChange={e => e.target.checked ? setSelectedIds([...selectedIds, q.id]) : setSelectedIds(selectedIds.filter(id => id !== q.id))} />
                  {q.content}
                </label>
              ))}
            </div>
            <div className="modal-actions" style={{ marginTop: '10px' }}>
              <button className="teacher-button teacher-button--primary" onClick={handleCreate} disabled={loading}>{loading ? 'Đang lưu...' : (editingExamId ? 'Lưu thay đổi' : 'Tạo bài thi')}</button>
              <button className="teacher-button" onClick={resetForm}>Hủy</button>
            </div>
          </div>
        </div>
      )}
      <div className="teacher-simple-table">
        <table>
          <thead>
            <tr>
              <th>Tiêu đề</th>
              <th>Môn học</th>
              <th>Thời lượng</th>
              <th>Bắt đầu</th>
              <th>Kết thúc</th>
              <th>Số câu hỏi</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {exams.length === 0 ? (
              <tr><td colSpan={8} className="teacher-simple-table__empty">Chưa có bài kiểm tra nào.</td></tr>
            ) : exams.map(exam => (
              <tr key={exam.id}>
                <td>{exam.title}</td>
                <td>{subjects.find(s => s.id === exam.subjectId)?.title || 'Đang tải...'}</td>
                <td>{exam.durationMinutes} phút</td>
                <td>{new Date(exam.startTime).toLocaleString('vi-VN')}</td>
                <td>{new Date(exam.endTime).toLocaleString('vi-VN')}</td>
                <td>{exam.questions ?? '-'}</td>
                <td><span className="teacher-status-badge">{exam.status}</span></td>
                <td>
                  <div className="teacher-row-actions">
                    <button className="teacher-button" onClick={() => handleEdit(exam)}>Sửa</button>
                    <button className="teacher-button teacher-button--danger" onClick={() => handleDelete(exam)}>Xóa</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

const emptyNewUmlAssignment = () => ({
  subjectId: 0,
  title: '',
  description: '',
  dueDate: '',
  rubricCriteria: '',
  maxScore: '10.00',
});

const TeacherUmlPanel: React.FC<{ assignments: UmlAssignment[]; setAssignments: (a: UmlAssignment[]) => void; subjects: Subject[]; showToast: (m: string) => void }> = ({ assignments, setAssignments, subjects, showToast }) => {
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [newAssignment, setNewAssignment] = useState(emptyNewUmlAssignment());
  const [selectedAssignmentId, setSelectedAssignmentId] = useState<number | null>(null);

  useEffect(() => {
    if (subjects.length > 0 && newAssignment.subjectId === 0) {
      setNewAssignment(prev => ({ ...prev, subjectId: subjects[0].id }));
    }
  }, [subjects, newAssignment.subjectId]);

  const resetForm = () => {
    setNewAssignment(emptyNewUmlAssignment());
    setShowForm(false);
  };

  const handleCreate = async () => {
    if (!newAssignment.title.trim() || !newAssignment.description.trim() || !newAssignment.dueDate || newAssignment.subjectId === 0) {
      return showToast('Vui lòng nhập đủ thông tin.');
    }
    setLoading(true);
    try {
      // Backend dùng OffsetDateTime.parse nên bắt buộc chuỗi phải có offset (Z)
      const dueDateIso = new Date(newAssignment.dueDate).toISOString();
      const saved = await umlApiService.createAssignment({ ...newAssignment, dueDate: dueDateIso });
      const normalized: UmlAssignment = {
        id: saved.id,
        subjectId: saved.subject?.id ?? newAssignment.subjectId,
        title: saved.title,
        description: saved.description,
        dueDate: saved.dueDate,
        maxScore: Number(saved.maxScore),
        rubricCriteria: saved.rubricCriteria,
      };
      setAssignments([normalized, ...assignments]);
      resetForm();
      showToast('Đã tạo bài kiểm tra UML thành công!');
    } catch (e) {
      console.error('Lỗi khi tạo bài kiểm tra UML:', e);
      showToast(e instanceof Error ? e.message : 'Lỗi khi tạo bài kiểm tra UML.');
    } finally { setLoading(false); }
  };

  return (
    <div className="teacher-section">
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
        <h2>Bài kiểm tra UML</h2>
        <button className="teacher-button teacher-button--primary" onClick={() => setShowForm(true)}>+ Tạo bài kiểm tra UML</button>
      </div>
      {showForm && (
        <div className="teacher-modal-overlay">
          <div className="teacher-modal">
            <h3>Tạo bài kiểm tra UML</h3>
            <div className="teacher-question-form-table">
              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Môn học</label>
                <div className="teacher-question-form-value">
                  <select value={newAssignment.subjectId} onChange={e => setNewAssignment({ ...newAssignment, subjectId: parseInt(e.target.value) })}>
                    {subjects.map(s => <option key={s.id} value={s.id}>{s.title}</option>)}
                  </select>
                </div>
              </div>
              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Tiêu đề</label>
                <div className="teacher-question-form-value">
                  <input placeholder="Ví dụ: Thiết kế UML hệ thống bán hàng" value={newAssignment.title} onChange={e => setNewAssignment({ ...newAssignment, title: e.target.value })} />
                </div>
              </div>
              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Mô tả đề bài</label>
                <div className="teacher-question-form-value">
                  <textarea placeholder="Yêu cầu chi tiết cho sinh viên..." value={newAssignment.description} onChange={e => setNewAssignment({ ...newAssignment, description: e.target.value })} />
                </div>
              </div>
              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Tiêu chí chấm điểm</label>
                <div className="teacher-question-form-value">
                  <textarea placeholder="Ví dụ: Đầy đủ lớp; quan hệ kế thừa; multiplicity..." value={newAssignment.rubricCriteria} onChange={e => setNewAssignment({ ...newAssignment, rubricCriteria: e.target.value })} />
                </div>
              </div>
              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Điểm tối đa</label>
                <div className="teacher-question-form-value">
                  <input type="number" min="1" max="10" step="0.5" value={newAssignment.maxScore} onChange={e => setNewAssignment({ ...newAssignment, maxScore: e.target.value })} />
                </div>
              </div>
              <div className="teacher-question-form-row">
                <label className="teacher-question-form-key">Hạn nộp</label>
                <div className="teacher-question-form-value">
                  <input type="datetime-local" value={newAssignment.dueDate} onChange={e => setNewAssignment({ ...newAssignment, dueDate: e.target.value })} />
                </div>
              </div>
            </div>
            <div className="modal-actions" style={{ marginTop: '10px' }}>
              <button className="teacher-button teacher-button--primary" onClick={handleCreate} disabled={loading}>{loading ? 'Đang lưu...' : 'Tạo bài kiểm tra UML'}</button>
              <button className="teacher-button" onClick={resetForm}>Hủy</button>
            </div>
          </div>
        </div>
      )}
      <div className="teacher-simple-table">
        <table>
          <thead>
            <tr>
              <th>Tiêu đề</th>
              <th>Môn học</th>
              <th>Hạn nộp</th>
              <th>Điểm tối đa</th>
            </tr>
          </thead>
          <tbody>
            {assignments.length === 0 ? (
              <tr><td colSpan={4} className="teacher-simple-table__empty">Chưa có bài kiểm tra UML nào.</td></tr>
            ) : assignments.map(a => (
              <tr key={a.id}>
                <td>{a.title}</td>
                <td>{subjects.find(s => s.id === a.subjectId)?.title || 'N/A'}</td>
                <td>{new Date(a.dueDate).toLocaleString('vi-VN')}</td>
                <td>{a.maxScore}</td>
                <td>
                  <button className="teacher-button" onClick={() => setSelectedAssignmentId(a.id)}>Xem nộp bài</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {selectedAssignmentId && (
        <div style={{ marginTop: 12 }}>
          <h3>Danh sách nộp bài</h3>
          <button onClick={() => setSelectedAssignmentId(null)} style={{ marginBottom: 8 }}>Đóng</button>
          <TeacherUmlSubmissionsView assignmentId={selectedAssignmentId} maxScore={assignments.find(x => x.id === selectedAssignmentId)?.maxScore} />
        </div>
      )}
    </div>
  );
};

// Renders PlantUML source as an image by requesting the public PlantUML render server
const PlantUmlImage: React.FC<{ source: string }> = ({ source }) => {
  const [imgUrl, setImgUrl] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let objectUrl: string | null = null;
    let cancelled = false;

    const render = async () => {
      if (!source) return;
      try {
        const res = await fetch('https://www.plantuml.com/plantuml/png', {
          method: 'POST',
          headers: { 'Content-Type': 'text/plain' },
          body: source,
        });
        if (!res.ok) throw new Error('Không thể dựng hình từ PlantUML');
        const blob = await res.blob();
        if (cancelled) return;
        objectUrl = URL.createObjectURL(blob);
        setImgUrl(objectUrl);
      } catch (e: any) {
        if (!cancelled) setError(e.message || 'Lỗi dựng hình PlantUML');
      }
    };
    void render();

    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [source]);

  if (error) return <div style={{ color: '#ef4444', marginTop: 8 }}>{error}</div>;
  if (!imgUrl) return <div style={{ color: '#64748b', marginTop: 8 }}>Đang dựng hình...</div>;
  return <img src={imgUrl} alt="PlantUML render" style={{ maxWidth: '100%', border: '1px solid #e2e8f0', borderRadius: 6, marginTop: 8 }} />;
};

// Small sub-component for teacher to view submissions and grade
const TeacherUmlSubmissionsView: React.FC<{ assignmentId: number; maxScore?: number }> = ({ assignmentId, maxScore = 10 }) => {
  const [subs, setSubs] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState<Record<number, { finalScore: string; teacherFeedback: string }>>({});

  const load = async () => {
    setLoading(true);
    try {
      const res = await umlApiService.getSubmissionsByAssignment(assignmentId);
      setSubs(res);
    } catch (e) {
      setSubs([]);
    } finally { setLoading(false); }
  };

  useEffect(() => { void load(); }, [assignmentId]);

  const handleGrade = async (id: number) => {
    const data = editing[id];
    if (!data) return;
    const parsed = Number(data.finalScore);
    if (Number.isNaN(parsed)) return alert('Vui lòng nhập một số hợp lệ cho điểm.');
    if (parsed < 0 || parsed > Number(maxScore)) return alert(`Điểm phải trong khoảng 0 - ${maxScore}.`);
    try {
      await umlApiService.gradeSubmission(id, { finalScore: parsed, teacherFeedback: data.teacherFeedback });
      await load();
      alert('Đã lưu điểm.');
    } catch (e: any) { alert(e.message || 'Lỗi khi chấm bài'); }
  };

  if (loading) return <div>Đang tải danh sách nộp bài...</div>;
  if (subs.length === 0) return <div>Chưa có sinh viên nộp bài cho bài kiểm tra này.</div>;

  return (
    <div style={{ marginTop: 16 }}>
      {subs.map(s => (
        <div key={s.id} style={{ padding: 12, border: '1px solid #e2e8f0', marginBottom: 12, borderRadius: 6 }}>
          <div><strong>Sinh viên:</strong> {s.student?.username || s.student?.email || 'ID:' + s.student?.id}</div>
          <div><strong>Trạng thái:</strong> {s.status}</div>
          {s.fileType === 'PLANTUML' ? (
            <div style={{ marginTop: 8 }}>
              <strong>Sơ đồ UML (dựng từ PlantUML):</strong>
              <PlantUmlImage source={s.plantumlSource} />
            </div>
          ) : (
            <div style={{ marginTop: 8 }}><a href={s.fileUrl} target="_blank" rel="noreferrer">Xem tệp nộp</a></div>
          )}
          <div style={{ marginTop: 8 }}>
            <label>Điểm chính thức: </label>
            <input type="number" min="0" max="100" step="0.5" value={editing[s.id]?.finalScore ?? (s.finalScore ?? '')} onChange={e => setEditing(prev => ({ ...prev, [s.id]: { ...(prev[s.id] || { finalScore: String(s.finalScore ?? ''), teacherFeedback: s.teacherFeedback || '' }), finalScore: e.target.value } }))} />
          </div>
          <div style={{ marginTop: 8 }}>
            <label>Nhận xét giảng viên:</label>
            <textarea value={editing[s.id]?.teacherFeedback ?? (s.teacherFeedback ?? '')} onChange={e => setEditing(prev => ({ ...prev, [s.id]: { ...(prev[s.id] || { finalScore: String(s.finalScore ?? ''), teacherFeedback: s.teacherFeedback || '' }), teacherFeedback: e.target.value } }))} />
          </div>
          <div style={{ marginTop: 8 }}>
            <button onClick={() => handleGrade(s.id)}>Lưu điểm</button>
          </div>
        </div>
      ))}
    </div>
  );
};

// =============================================================================
// 3. COMPONENT CHÍNH
// =============================================================================
const TeacherInterface: React.FC<TeacherInterfaceProps> = ({
  onLogout, userName = 'Nguyễn Văn Toàn', materials, setMaterials
}) => {
  const [activeView, setActiveView] = useState<ViewKey>('overview');
  const [courses, setCourses] = useState<Course[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [exams, setExams] = useState<Exam[]>([]);
  const [umlAssignments, setUmlAssignments] = useState<UmlAssignment[]>([]);
  const [toast, setToast] = useState('');

  const showToast = (message: string) => {
    setToast(message);
    window.setTimeout(() => setToast(''), 3000);
  };

  useEffect(() => {
    const loadData = async () => {
      try {
        const [cData, eData, qData] = await Promise.all([
          coursesApiService.getAllCourses(),
          examApiService.getAllExams(),
          questionsApiService.getAllQuestions()
        ]);

        setCourses(Array.isArray(cData) ? cData : []);
        setExams(Array.isArray(eData) ? eData : []);
        setQuestions(Array.isArray(qData) ? qData : []);

        const allSubs: Subject[] = [];
        for (const course of cData) {
          const subs = await coursesApiService.getSubjectsByCourseId(course.id);
          if (Array.isArray(subs)) allSubs.push(...subs);
        }
        setSubjects(allSubs);

        const allUmlAssignments: UmlAssignment[] = [];
        for (const subject of allSubs) {
          const items = await umlApiService.getAssignmentsBySubject(subject.id).catch(() => []);
          if (Array.isArray(items)) {
            allUmlAssignments.push(...items.map((item: any) => ({
              id: item.id,
              subjectId: item.subject?.id ?? subject.id,
              title: item.title,
              description: item.description,
              dueDate: item.dueDate,
              maxScore: Number(item.maxScore),
              rubricCriteria: item.rubricCriteria,
            })));
          }
        }
        setUmlAssignments(allUmlAssignments);
      } catch (error) {
        console.error('Lỗi tải dữ liệu:', error);
        setCourses([]);
        setSubjects([]);
        setQuestions([]);
        setExams([]);
        setUmlAssignments([]);
        setToast('Không thể tải dữ liệu từ backend/database. Vui lòng đăng nhập lại hoặc kiểm tra server.');
      }
    };
    void loadData();
  }, []);

  const pageTitle = useMemo(() => navItems.find(item => item.key === activeView)?.label || 'Tổng quan', [activeView]);

  return (
    <div className="teacher-shell">
      <aside className="teacher-sidebar">
        <div className="teacher-brand"><div className="teacher-brand__mark">QL</div><div className="teacher-brand__name">QL Học Tập</div></div>
        <nav className="teacher-nav">
          {navItems.map((item) => (
            <button key={item.key} className={`teacher-nav__item ${activeView === item.key ? 'teacher-nav__item--active' : ''}`} onClick={() => setActiveView(item.key)}>
              <span className="teacher-nav__icon">{item.icon}</span><span className="teacher-nav__label">{item.label}</span>
            </button>
          ))}
        </nav>
      </aside>
      <main className="teacher-main">
        <header className="teacher-page-header">
          <h1>{pageTitle}</h1>
          <div className="teacher-account"><span>Chào, <strong>{userName}</strong></span><button className="teacher-button teacher-button--logout" onClick={onLogout}>Đăng xuất</button></div>
        </header>
        <div className="teacher-content">
          {activeView === 'overview' && <TeacherOverview courses={courses} questions={questions} exams={exams} />}
          {activeView === 'courses' && <div className="teacher-section"><h2>Khóa học</h2><p>Quản lý khóa học trong hệ thống.</p></div>}
          {activeView === 'questions' && <TeacherQuestionBank questions={questions} setQuestions={setQuestions} subjects={subjects} showToast={showToast} />}
          {activeView === 'exams' && (
            <>
              <TeacherExamsPanel exams={exams} questions={questions} setExams={setExams} subjects={subjects} showToast={showToast} />
              <TeacherUmlPanel assignments={umlAssignments} setAssignments={setUmlAssignments} subjects={subjects} showToast={showToast} />
            </>
          )}
          {activeView === 'results' && <div className="teacher-section"><h2>Kết quả học tập</h2><p>Dữ liệu đang được đồng bộ...</p></div>}
          {activeView === 'reports' && <div className="teacher-section"><h2>Báo cáo hệ thống</h2></div>}
        </div>
        {toast && <div className="teacher-toast">{toast}</div>}
      </main>
    </div>
  );
};

export default TeacherInterface;
