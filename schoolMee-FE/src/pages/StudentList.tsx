import React, { useEffect, useState } from 'react';
import { getStudents, createOrder } from '../api/endpoints';
import { StudentItem } from '../types';
import { useNavigate } from 'react-router-dom';
import { Eye, ShoppingCart } from 'lucide-react';

const StudentList = () => {
  const [students, setStudents] = useState<StudentItem[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const SCHOOL_ID = 1;

  const fetchStudents = async () => {
    try {
      const res = await getStudents(SCHOOL_ID);
      setStudents(res.data.students);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudents();
  }, []);

  const handleCreateOrder = async (studentId: number, hasStory: boolean) => {
    if (!hasStory) {
      alert('스토리가 생성되지 않은 학생은 주문할 수 없습니다.');
      return;
    }
    try {
      // NOTE: MVP에서는 storyId를 조회해서 넘겨야 하지만, 
      // 편의상 백엔드의 createOrder 구조(storyId)를 고려하여 스토리 조회가 필요할 수 있습니다.
      // 백엔드를 수정하거나 여기서 스토리를 먼저 조회 후 주문을 생성합니다.
      alert('MVP 버전: 각 학생의 "결과 보기" 화면에서 검수 후 개별 주문 기능을 확인하세요.');
    } catch (error) {
      console.error(error);
    }
  };

  if (loading) return <div>로딩 중...</div>;

  return (
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
                  <Eye size={16} /> 검수
                </button>
              </td>
            </tr>
          ))}
          {students.length === 0 && (
            <tr>
              <td colSpan={5} className="p-8 text-center text-gray-500">
                학생 데이터가 없습니다.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default StudentList;
