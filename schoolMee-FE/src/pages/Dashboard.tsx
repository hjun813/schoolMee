import React, { useEffect, useState } from 'react';
import { getDashboard, generateStories } from '../api/endpoints';
import { SchoolDashboardResponse } from '../types';
import { Users, BookOpen, ShoppingCart, CheckCircle, Wand2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const [data, setData] = useState<SchoolDashboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const navigate = useNavigate();
  const SCHOOL_ID = 1; // MVP용 하드코딩

  const fetchData = async () => {
    try {
      const res = await getDashboard(SCHOOL_ID);
      setData(res.data);
    } catch (error) {
      console.error('대시보드 데이터를 불러오는데 실패했습니다.', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleGenerateStories = async () => {
    if (!confirm('전체 학생의 AI 스토리를 일괄 생성하시겠습니까? (이미 생성된 학생은 제외됩니다)')) return;
    setGenerating(true);
    try {
      const res = await generateStories(SCHOOL_ID);
      alert(res.data.message);
      fetchData(); // 상태 갱신
    } catch (error) {
      console.error(error);
      alert('스토리 생성 중 오류가 발생했습니다.');
    } finally {
      setGenerating(false);
    }
  };

  if (loading) return <div className="p-8">로딩 중...</div>;
  if (!data) return <div className="p-8">데이터를 불러오지 못했습니다. 백엔드가 실행 중인지 확인하세요.</div>;

  const stats = [
    { label: '전체 학생 수', value: data.totalStudents, icon: Users, color: 'text-blue-600', bg: 'bg-blue-100' },
    { label: '생성된 스토리', value: data.storiesGenerated, icon: BookOpen, color: 'text-indigo-600', bg: 'bg-indigo-100' },
    { label: '요청된 주문', value: data.ordersCreated, icon: ShoppingCart, color: 'text-yellow-600', bg: 'bg-yellow-100' },
    { label: '완료된 주문', value: data.ordersCompleted, icon: CheckCircle, color: 'text-green-600', bg: 'bg-green-100' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{data.name} 대시보드</h2>
          <p className="text-gray-500 mt-1">AI 기반 학생 맞춤형 앨범 생성 현황을 확인하세요.</p>
        </div>
        <div className="flex gap-3">
          <button
            onClick={() => navigate('/students')}
            className="px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition-colors"
          >
            상세 보기
          </button>
          <button
            onClick={handleGenerateStories}
            disabled={generating}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors"
          >
            <Wand2 size={18} />
            {generating ? '생성 중...' : 'AI 스토리 일괄 생성'}
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, index) => {
          const Icon = stat.icon;
          return (
            <div key={index} className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm flex items-center gap-4">
              <div className={`p-4 rounded-lg ${stat.bg}`}>
                <Icon size={24} className={stat.color} />
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Dashboard;
