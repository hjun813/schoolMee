import { useEffect, useState } from 'react';
import { getSchools } from '../api/endpoints';
import type { SchoolDashboardResponse } from '../types';
import { useSchool } from '../context/SchoolContext';
import { useNavigate } from 'react-router-dom';
import { School, PlusCircle, Calendar, GraduationCap, ChevronRight } from 'lucide-react';

const SchoolSelection = () => {
  const [schools, setSchools] = useState<SchoolDashboardResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const { selectSchool } = useSchool();
  const navigate = useNavigate();

  useEffect(() => {
    getSchools()
      .then((res) => {
        setSchools(res.data);
        // 학교가 하나도 없으면 바로 온보딩으로 가이드
        if (res.data.length === 0) {
          navigate('/onboarding');
        }
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [navigate]);

  const handleSelect = (school: SchoolDashboardResponse) => {
    selectSchool(school.schoolId, school.name);
    navigate('/');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <div className="w-12 h-12 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
          <p className="text-gray-500 font-medium">학교 목록을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center py-20 px-4">
      <div className="max-w-4xl w-full">
        {/* Header */}
        <div className="text-center mb-12">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-white rounded-2xl shadow-sm border border-gray-100 mb-4">
            <School className="text-indigo-600" size={32} />
          </div>
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">학교 선택</h1>
          <p className="mt-2 text-gray-500">관리하실 학교를 선택해주세요.</p>
        </div>

        {/* School Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-10">
          {schools.map((school) => (
            <button
              key={school.schoolId}
              onClick={() => handleSelect(school)}
              className="bg-white p-6 rounded-2xl border border-gray-200 shadow-sm hover:shadow-md hover:border-indigo-300 transition-all text-left flex flex-col group"
            >
              <div className="flex justify-between items-start mb-4">
                <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                  <GraduationCap size={24} />
                </div>
                <div className="p-1 bg-gray-50 rounded-lg group-hover:bg-indigo-50 transition-colors">
                  <ChevronRight size={18} className="text-gray-400 group-hover:text-indigo-600" />
                </div>
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-1">{school.name}</h3>
              <div className="flex items-center gap-3 text-sm text-gray-400 mt-auto">
                <div className="flex items-center gap-1">
                  <Calendar size={14} />
                  {new Date(school.createdAt).toLocaleDateString()} 등록
                </div>
                <span className="w-1 h-1 bg-gray-300 rounded-full"></span>
                <div className="font-medium text-gray-600">{school.totalStudents}명의 학생</div>
              </div>
            </button>
          ))}

          {/* Add New School Button */}
          <button
            onClick={() => navigate('/onboarding')}
            className="p-6 rounded-2xl border-2 border-dashed border-gray-300 bg-gray-50/50 hover:bg-white hover:border-indigo-300 hover:text-indigo-600 transition-all flex flex-col items-center justify-center gap-3 group h-[180px]"
          >
            <PlusCircle size={32} className="text-gray-300 group-hover:text-indigo-500 transition-colors" />
            <div className="text-center font-bold text-gray-500 group-hover:text-indigo-600">새 학교 등록하기</div>
          </button>
        </div>

        {/* Footer info */}
        <div className="text-center">
            <p className="text-sm text-gray-400">SchoolMee Admin v1.0 • 앨범 제작 자동화 플랫폼</p>
        </div>
      </div>
    </div>
  );
};

export default SchoolSelection;
