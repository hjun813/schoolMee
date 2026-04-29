import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useSchool } from '../context/SchoolContext';
import { BookOpen, Users, FolderHeart, ShoppingBag, Download, Image as ImageIcon, School as SchoolIcon, LogOut } from 'lucide-react';
import { useEffect } from 'react';

const Layout = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { currentSchoolId, currentSchoolName, clearSelection, isInitialized } = useSchool();

  useEffect(() => {
    if (isInitialized && !currentSchoolId) {
       // 온보딩 중에는 리다이렉트 안함 (하지만 온보딩은 Layout 밖이므로 사실 안전)
       navigate('/schools');
    }
  }, [currentSchoolId, isInitialized, navigate]);

  const navItems = [
    { path: '/', label: '대시보드', icon: BookOpen },
    { path: '/photos', label: '사진 업로드 및 매칭', icon: ImageIcon },
    { path: '/students', label: '학생 관리 및 스토리 생성', icon: Users },
    { path: '/orders', label: '주문 관리', icon: ShoppingBag },
    { path: '/export', label: '데이터 Export', icon: Download },
  ];

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-6 border-b border-gray-200 flex items-center gap-2 text-indigo-600">
          <FolderHeart size={28} />
          <h1 className="text-xl font-bold">SchoolMee Admin</h1>
        </div>
        <nav className="flex-1 p-4 space-y-1">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path || (item.path !== '/' && location.pathname.startsWith(item.path));
            const Icon = item.icon;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors ${
                  isActive 
                    ? 'bg-indigo-50 text-indigo-700' 
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                }`}
              >
                <Icon size={20} />
                {item.label}
              </Link>
            );
          })}
        </nav>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col h-screen overflow-hidden">
        <header className="bg-white border-b border-gray-200 px-8 py-4 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <h2 className="text-xl font-semibold text-gray-800">
              {navItems.find(item => location.pathname === item.path || (item.path !== '/' && location.pathname.startsWith(item.path)))?.label || '상세 보기'}
            </h2>
            <div className="h-4 w-[1px] bg-gray-200"></div>
            <div className="flex items-center gap-2 px-3 py-1 bg-indigo-50 text-indigo-700 rounded-lg text-sm font-bold">
              <SchoolIcon size={14} />
              {currentSchoolName}
            </div>
          </div>
          
          <button 
            onClick={() => {
              clearSelection();
              navigate('/schools');
            }}
            className="flex items-center gap-2 text-sm font-medium text-gray-500 hover:text-red-600 transition-colors"
          >
            <LogOut size={16} />
            학교 변경
          </button>
        </header>
        <div className="flex-1 overflow-auto p-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default Layout;
