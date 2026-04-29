import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSchool } from '../context/SchoolContext';
import { 
  registerSchool, 
  createClassRoom, 
  bulkUploadProfilesAndCreateStudents, 
  uploadPhotos, 
  runPipeline,
  getPipelineStatus
} from '../api/endpoints';
import { 
  School, 
  Users, 
  UserPlus, 
  Image as ImageIcon, 
  Play, 
  CheckCircle, 
  ChevronLeft,
  ChevronRight,
  Loader2,
  Trash2,
  Monitor
} from 'lucide-react';

const STEPS = [
  { id: 1, title: '학교 등록', icon: School, description: '학교 이름을 입력하세요' },
  { id: 2, title: '반 설정', icon: Users, description: '학년과 반 정보를 설정합니다' },
  { id: 3, title: '학생 등록', icon: UserPlus, description: '증명사진을 업로드하여 학생을 등록합니다' },
  { id: 4, title: '사진 업로드', icon: ImageIcon, description: '앨범에 사용할 단체 사진을 업로드합니다' },
  { id: 5, title: 'AI 생성', icon: Play, description: 'AI 파이프라인을 실행하여 스토리를 생성합니다' }
];

const Onboarding = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const { selectSchool } = useSchool();
  const navigate = useNavigate();

  // Step States
  const [schoolName, setSchoolName] = useState('');
  const [schoolId, setSchoolId] = useState<number | null>(null);
  
  const [grade, setGrade] = useState('6');
  const [classNum, setClassNum] = useState('1');

  const [studentFiles, setStudentFiles] = useState<File[]>([]);
  const [groupFiles, setGroupFiles] = useState<File[]>([]);
  
  const [pipelineResult, setPipelineResult] = useState<any>(null);
  const [polledJobStatus, setPolledJobStatus] = useState<string>('IDLE');
  const [logs, setLogs] = useState<string[]>([]);

  const addLog = (msg: string) => setLogs(prev => [...prev, `[${new Date().toLocaleTimeString()}] ${msg}`]);

  const handleNext = async () => {
    if (currentStep === 1) {
      if (!schoolName) return alert('학교 이름을 입력해주세요.');
      setLoading(true);
      try {
        const res = await registerSchool(schoolName);
        const newSchoolId = res.data;
        setSchoolId(newSchoolId);
        selectSchool(newSchoolId, schoolName); // 즉시 선택 상태로 전환
        setCurrentStep(2);
      } catch (e) {
        alert('학교 등록 실패');
      } finally {
        setLoading(false);
      }
    } else if (currentStep === 2) {
      if (!schoolId) return;
      setLoading(true);
      try {
        await createClassRoom(schoolId, { grade: parseInt(grade), classNum: parseInt(classNum) });
        setCurrentStep(3);
      } catch (e) {
        alert('반 설정 실패');
      } finally {
        setLoading(false);
      }
    } else if (currentStep === 3) {
      if (studentFiles.length === 0) return alert('학생 증명사진을 업로드해주세요.');
      setLoading(true);
      try {
        await bulkUploadProfilesAndCreateStudents(schoolId!, studentFiles);
        setCurrentStep(4);
      } catch (e) {
        alert('학생 등록 실패');
      } finally {
        setLoading(false);
      }
    } else if (currentStep === 4) {
      if (groupFiles.length === 0) return alert('단체 사진을 업로드해주세요.');
      setLoading(true);
      try {
        await uploadPhotos(schoolId!, 'GROUP', groupFiles);
        setCurrentStep(5);
      } catch (e) {
        alert('사진 업로드 실패');
      } finally {
        setLoading(false);
      }
    }
  };

  const pollJobStatus = async (jobId: number) => {
    const interval = setInterval(async () => {
      try {
        const res = await getPipelineStatus(jobId);
        const { status, processedPhotos, storiesGenerated } = res.data;
        
        setPolledJobStatus(status);
        
        if (status === 'PROCESSING') {
          addLog(`⏳ 처리 중... (현재 분석 완료: ${processedPhotos}장)`);
        } else if (status === 'SUCCEEDED') {
          clearInterval(interval);
          setPipelineResult(res.data);
          addLog(`✅ 분석 및 매칭 완료: ${processedPhotos}장 처리됨`);
          addLog(`🎉 맞춤형 스토리 생성 완료: ${storiesGenerated}건 발행됨`);
          setLoading(false);
        } else if (status === 'FAILED') {
          clearInterval(interval);
          addLog(`❌ 파이프라인 처리 실패: ${res.data.errorMessage}`);
          setLoading(false);
        }
      } catch (e) {
        clearInterval(interval);
        addLog('❌ 상태 조회 중 오류가 발생했습니다.');
        setLoading(false);
      }
    }, 2000);
  };

  const startPipeline = async () => {
    if (!schoolId) return;
    setLoading(true);
    setLogs([]);
    addLog('🚀 AI 파이프라인 엔진 가동 요청 중...');
    try {
      const res = await runPipeline([], schoolId); 
      const jobId = res.data.jobId;
      addLog(`📡 작업 예약 완료 (Job ID: ${jobId}). 비동기 처리를 시작합니다.`);
      setPolledJobStatus('PENDING');
      pollJobStatus(jobId);
    } catch (e) {
      addLog('❌ 파이프라인 요청 중 오류가 발생했습니다.');
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto py-12 px-4">
      {/* Header */}
      <div className="text-center mb-12">
        <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">서비스 시작하기</h1>
        <p className="mt-3 text-lg text-gray-500">SchoolMee의 AI 서비스를 위한 간단한 온보딩 절차를 진행합니다.</p>
      </div>

      {/* Stepper */}
      <div className="flex items-center justify-between mb-12 px-4 relative">
        <div className="absolute top-1/2 left-0 w-full h-0.5 bg-gray-100 -translate-y-1/2 -z-10"></div>
        {STEPS.map((step) => {
          const Icon = step.icon;
          const isActive = currentStep >= step.id;
          
          return (
            <div key={step.id} className="flex flex-col items-center bg-gray-50 px-2 sm:px-4 z-10">
              <div 
                className={`w-12 h-12 rounded-full flex items-center justify-center border-2 transition-all duration-300 ${
                  isActive 
                    ? 'border-indigo-600 bg-indigo-600 text-white shadow-lg shadow-indigo-100 scale-110' 
                    : 'border-gray-200 bg-white text-gray-400'
                }`}
              >
                {isActive && currentStep > step.id ? <CheckCircle size={24} /> : <Icon size={24} />}
              </div>
              <span className={`mt-3 text-xs font-bold ${isActive ? 'text-indigo-600' : 'text-gray-400'}`}>
                {step.title}
              </span>
            </div>
          );
        })}
      </div>

      {/* Step Content */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-xl overflow-hidden min-h-[400px] flex flex-col">
        <div className="p-8 flex-1">
          <div className="mb-8">
            <h2 className="text-2xl font-bold text-gray-900">{STEPS[currentStep - 1].title}</h2>
            <p className="text-gray-500">{STEPS[currentStep - 1].description}</p>
          </div>

          {currentStep === 1 && (
            <div className="space-y-4 animate-in fade-in slide-in-from-bottom-4 duration-500">
              <label className="block text-sm font-semibold text-gray-700">학교 이름</label>
              <input 
                type="text" 
                value={schoolName}
                onChange={(e) => setSchoolName(e.target.value)}
                placeholder="예: 은하수초등학교"
                className="w-full px-5 py-4 bg-gray-50 border-0 rounded-xl focus:ring-2 focus:ring-indigo-500 text-lg font-medium transition-all"
                autoFocus
              />
            </div>
          )}

          {currentStep === 2 && (
            <div className="grid grid-cols-2 gap-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
              <div className="space-y-3">
                <label className="block text-sm font-semibold text-gray-700">학년</label>
                <select 
                  value={grade}
                  onChange={(e) => setGrade(e.target.value)}
                  className="w-full px-5 py-4 bg-gray-50 border-0 rounded-xl focus:ring-2 focus:ring-indigo-500 font-medium"
                >
                  {[1,2,3,4,5,6].map(n => <option key={n} value={n}>{n}학년</option>)}
                </select>
              </div>
              <div className="space-y-3">
                <label className="block text-sm font-semibold text-gray-700">반</label>
                <input 
                  type="number" 
                  value={classNum}
                  onChange={(e) => setClassNum(e.target.value)}
                  className="w-full px-5 py-4 bg-gray-50 border-0 rounded-xl focus:ring-2 focus:ring-indigo-500 font-medium"
                />
              </div>
            </div>
          )}

          {currentStep === 3 && (
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
               <div 
                className="border-2 border-dashed border-gray-200 rounded-2xl p-10 text-center hover:border-indigo-400 hover:bg-indigo-50 transition-all cursor-pointer group"
                onClick={() => document.getElementById('student-upload')?.click()}
              >
                <input 
                  id="student-upload"
                  type="file" 
                  multiple 
                  className="hidden" 
                  onChange={(e) => e.target.files && setStudentFiles(prev => [...prev, ...Array.from(e.target.files!)])}
                />
                <UserPlus size={48} className="mx-auto text-gray-300 group-hover:text-indigo-500 mb-4 transition-colors" />
                <p className="text-lg font-bold text-gray-700">증명사진을 한꺼번에 부어넣으세요</p>
                <p className="text-gray-400 mt-1">파일명이 자동으로 학생 이름으로 등록됩니다.</p>
                <p className="text-xs text-indigo-500 mt-2 font-medium bg-indigo-50 inline-block px-3 py-1 rounded-full">예: 홍길동.jpg, 김철수.png</p>
              </div>
              {studentFiles.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {studentFiles.map((f, i) => (
                    <div key={i} className="px-3 py-1 bg-gray-100 rounded-full text-xs text-gray-600 flex items-center gap-1">
                      {f.name}
                      <Trash2 size={12} className="cursor-pointer hover:text-red-500" onClick={(e) => {
                        e.stopPropagation();
                        setStudentFiles(prev => prev.filter((_, idx) => idx !== i));
                      }}/>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {currentStep === 4 && (
             <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
             <div 
              className="border-2 border-dashed border-gray-200 rounded-2xl p-10 text-center hover:border-indigo-400 hover:bg-indigo-50 transition-all cursor-pointer group"
              onClick={() => document.getElementById('group-upload')?.click()}
            >
              <input 
                id="group-upload"
                type="file" 
                multiple 
                className="hidden" 
                onChange={(e) => e.target.files && setGroupFiles(prev => [...prev, ...Array.from(e.target.files!)])}
              />
              <ImageIcon size={48} className="mx-auto text-gray-300 group-hover:text-indigo-500 mb-4 transition-colors" />
              <p className="text-lg font-bold text-gray-700">추억이 담긴 단체 사진들을 업로드하세요</p>
              <p className="text-gray-400 mt-1">많이 올릴수록 더 풍성한 AI 앨범이 만들어집니다.</p>
            </div>
            {groupFiles.length > 0 && (
              <div className="grid grid-cols-6 sm:grid-cols-10 gap-2">
                {groupFiles.map((f, i) => (
                  <div key={i} className="aspect-square bg-gray-100 rounded-lg overflow-hidden border border-gray-200 relative group">
                    <img src={URL.createObjectURL(f)} className="w-full h-full object-cover" />
                    <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity" onClick={() => setGroupFiles(prev => prev.filter((_, idx) => idx !== i))}>
                      <Trash2 size={14} className="text-white" />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
          )}

          {currentStep === 5 && (
            <div className="space-y-8 animate-in fade-in zoom-in duration-500">
              <div className="bg-gray-50 rounded-2xl p-8 text-center">
                 {!pipelineResult ? (
                   <>
                    <div className="w-20 h-20 bg-indigo-100 text-indigo-600 rounded-2xl flex items-center justify-center mx-auto mb-6">
                      <Play size={40} />
                    </div>
                    <h3 className="text-xl font-bold text-gray-900 mb-2">모든 준비가 끝났습니다!</h3>
                    <p className="text-gray-500 mb-8">AI 파이프라인을 실행하여 학생별 맞춤형 스토리를 생성합니다.</p>
                     <button 
                      onClick={startPipeline}
                      disabled={loading}
                      className="inline-flex items-center gap-3 px-8 py-4 bg-indigo-600 text-white rounded-2xl font-bold text-lg hover:bg-indigo-700 transition-all shadow-lg shadow-indigo-200 disabled:opacity-50"
                    >
                      {loading ? <Loader2 size={24} className="animate-spin" /> : <Wand2 size={24} />}
                      {polledJobStatus === 'PROCESSING' ? 'AI 엔진 연산 중...' : 'AI 파이프라인 가동 시작하기'}
                    </button>
                    {polledJobStatus === 'PROCESSING' && (
                      <div className="mt-6 w-full max-w-sm mx-auto h-2 bg-gray-200 rounded-full overflow-hidden">
                        <div className="h-full bg-indigo-600 animate-[shimmer_2s_infinite] w-full" style={{
                          backgroundImage: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent)'
                        }}></div>
                      </div>
                    )}
                   </>
                 ) : (
                   <div className="text-center py-4">
                      <div className="w-20 h-20 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-6">
                        <CheckCircle size={40} />
                      </div>
                      <h3 className="text-2xl font-bold text-gray-900 mb-2">온보딩 완료!</h3>
                      <p className="text-gray-500 mb-8">이제 대시보드에서 생성된 앨범을 확인하실 수 있습니다.</p>
                      <button 
                        onClick={() => navigate('/')}
                        className="px-10 py-4 bg-green-600 text-white rounded-2xl font-bold text-lg hover:bg-green-700 transition-all"
                      >
                        대시보드로 이동
                      </button>
                   </div>
                 )}
              </div>

              {logs.length > 0 && (
                <div className="bg-gray-900 rounded-2xl p-6 font-mono text-xs sm:text-sm shadow-2xl">
                   <div className="flex items-center gap-2 mb-4 text-indigo-400 font-bold border-b border-gray-800 pb-2">
                     <Monitor size={16} /> 릴리즈 파이프라인 로그
                   </div>
                   <div className="space-y-2 max-h-[200px] overflow-y-auto custom-scrollbar">
                     {logs.map((log, i) => (
                       <div key={i} className={`flex items-start gap-3 ${log.includes('✅') || log.includes('🎉') ? 'text-green-400 font-bold' : log.includes('❌') ? 'text-red-400 font-bold' : 'text-gray-400'}`}>
                         <span className="text-gray-600 whitespace-nowrap shrink-0">{log.split(' ')[0]}</span>
                         <span>{log.substring(log.indexOf(' ') + 1)}</span>
                       </div>
                     ))}
                     {loading && (
                       <div className="text-indigo-400 animate-pulse flex items-center gap-2">
                         <Loader2 size={14} className="animate-spin" /> 연산 엔진 가속 중...
                       </div>
                     )}
                   </div>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer Actions */}
        <div className="p-8 bg-gray-50 border-t border-gray-100 flex justify-between items-center">
          {currentStep > 1 && currentStep < 5 && !pipelineResult && (
            <button 
              onClick={() => setCurrentStep(prev => prev - 1)}
              disabled={loading}
              className="flex items-center gap-2 px-6 py-3 text-gray-500 font-bold hover:text-gray-700 disabled:opacity-50"
            >
              <ChevronLeft size={20} /> 이전
            </button>
          )}
          <div className="ml-auto">
            {currentStep < 5 && (
              <button 
                onClick={handleNext}
                disabled={loading}
                className="flex items-center gap-2 px-8 py-4 bg-gray-900 text-white rounded-xl font-bold hover:bg-black transition-all shadow-lg disabled:opacity-50"
              >
                {loading ? <Loader2 size={20} className="animate-spin" /> : (
                  <>
                    다음 단계 <ChevronRight size={20} />
                  </>
                )}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// Simple Lucide wrapper for Wand2 which might be missing in some versions
const Wand2 = ({ size }: { size: number }) => (
  <svg 
    width={size} 
    height={size} 
    viewBox="0 0 24 24" 
    fill="none" 
    stroke="currentColor" 
    strokeWidth="2" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
  >
    <path d="m21.64 3.64-1.28-1.28a1.21 1.21 0 0 0-1.72 0L2.36 18.64a1.21 1.21 0 0 0 0 1.72l1.28 1.28a1.21 1.21 0 0 0 1.72 0L21.64 5.36a1.21 1.21 0 0 0 0-1.72Z" />
    <path d="m14 7 3 3" />
    <path d="M5 6v4" />
    <path d="M19 14v4" />
    <path d="M10 2v2" />
    <path d="M7 8H3" />
    <path d="M21 16h-4" />
    <path d="M11 3H9" />
  </svg>
);

export default Onboarding;
