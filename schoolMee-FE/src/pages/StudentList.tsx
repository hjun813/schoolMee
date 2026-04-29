import { useEffect, useState, useRef } from 'react';
import { getStudents, bulkUploadProfilesAndCreateStudents } from '../api/endpoints';
import { useSchool } from '../context/SchoolContext';
import type { StudentItem } from '../types';
import { useNavigate } from 'react-router-dom';
import { Eye, UserPlus } from 'lucide-react';

const StudentList = () => {
  const [students, setStudents] = useState<StudentItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const { currentSchoolId: schoolId } = useSchool();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const fetchStudents = async (id: number) => {
    try {
      const res = await getStudents(id);
      setStudents(res.data.students);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (schoolId) {
      fetchStudents(schoolId);
    }
  }, [schoolId]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!schoolId) return;
    if (!e.target.files || e.target.files.length === 0) return;
    
    const files = Array.from(e.target.files);
    setUploading(true);
    try {
      const res = await bulkUploadProfilesAndCreateStudents(schoolId, files);
      alert(`✅ 성공적으로 ${res.data.totalUploaded}명의 학생이 자동 생성되고 얼굴 식별 앵커가 등록되었습니다!`);
      fetchStudents(schoolId); // 새로고침
    } catch (error: any) {
      console.error(error);
      alert('학생 일괄 생성 중 오류가 발생했습니다: ' + (error.response?.data?.message || error.message));
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  if (loading) return <div>로딩 중...</div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-xl font-bold text-gray-900">학교 학생 목록</h2>
          <p className="text-sm text-gray-500">스토리 생성 현황과 학생별 상태를 관리합니다.</p>
        </div>
        <div>
          <input 
            type="file" 
            multiple 
            accept="image/*" 
            className="hidden" 
            ref={fileInputRef}
            onChange={handleFileChange}
            disabled={uploading}
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors shadow-sm"
          >
            <UserPlus size={18} />
            {uploading ? '일괄 등록 중...' : '증명사진 파일명 기반 일괄 생성'}
          </button>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 border-b border-gray-200 text-gray-500 text-sm">
              <th className="p-4 font-medium">학생 ID</th>
              <th className="p-4 font-medium">이름</th>
              <th className="p-4 font-medium">학년/반</th>
              <th className="p-4 font-medium">상태</th>
              <th className="p-4 font-medium text-right">액션</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {students.map((student) => (
              <tr key={student.studentId} className="hover:bg-gray-50 transition-colors">
                <td className="p-4 text-gray-600">#{student.studentId}</td>
                <td className="p-4 font-medium text-gray-900">{student.name}</td>
                <td className="p-4 text-gray-600">{student.grade}학년 {student.classNum}반</td>
                <td className="p-4">
                  {student.hasStory ? (
                    <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                      스토리 생성 완료
                    </span>
                  ) : (
                    <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-600">
                      스토리 미생성
                    </span>
                  )}
                </td>
                <td className="p-4 text-right flex justify-end gap-2">
                  <button
                    onClick={() => navigate(`/students/${student.studentId}/stories`)}
                    disabled={!student.hasStory}
                    className="flex items-center gap-1.5 px-3 py-1.5 bg-indigo-50 text-indigo-700 rounded-lg text-sm font-medium hover:bg-indigo-100 disabled:opacity-50 transition-colors"
                  >
                    <Eye size={16} /> 앨범 보기
                  </button>
                </td>
              </tr>
            ))}
            {students.length === 0 && (
              <tr>
                <td colSpan={5} className="p-12 text-center">
                  <div className="text-gray-500 mb-4">아직 등록된 학생이 없습니다.</div>
                  <button
                    onClick={() => fileInputRef.current?.click()}
                    className="mx-auto flex items-center justify-center gap-2 px-6 py-3 bg-indigo-50 text-indigo-700 rounded-xl font-bold hover:bg-indigo-100 transition-colors"
                  >
                    <UserPlus size={20} />
                    증명사진 올려서 자동 생성하기
                  </button>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default StudentList;
