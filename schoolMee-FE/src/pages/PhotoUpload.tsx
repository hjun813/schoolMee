import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { uploadPhotos, runPipeline } from '../api/endpoints';
import { useSchool } from '../context/SchoolContext';
import { UploadCloud, Sparkles, CheckCircle, ArrowRight, X } from 'lucide-react';

const PhotoUpload = () => {
  const [files, setFiles] = useState<File[]>([]);
  const [step, setStep] = useState<1 | 2 | 3>(1); // 1: Upload, 2: Process, 3: Done
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState<string[]>([]);
  const { currentSchoolId: schoolId } = useSchool();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setFiles(prev => [...prev, ...Array.from(e.target.files!)]);
    }
  };

  const removeFile = (index: number) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
  };

  const addMessage = (msg: string) => {
    setMessages(prev => [...prev, msg]);
  };

  const handleUploadAndProcess = async () => {
    if (!schoolId) return alert('학교 정보를 찾을 수 없습니다.');
    if (files.length === 0) {
      alert('업로드할 사진을 선택해주세요.');
      return;
    }
    setLoading(true);
    try {
      addMessage(`[1단계] 사진 ${files.length}장 업로드 시작...`);
      const uploadRes = await uploadPhotos(schoolId, 'GROUP', files);
      const photoIds = uploadRes.data.photos.map(p => p.photoId);
      addMessage(`✅ 사진 보관소 업로드 완전 성공: ${uploadRes.data.uploadedCount}장`);

      setStep(2);
      addMessage(`[2단계] 자동화 파이프라인(분석+매칭+스토리발행) 엔진을 가동합니다...`);
      const pipelineRes = await runPipeline(photoIds, schoolId);
      
      addMessage(`✅ 매칭된 사진 수: ${pipelineRes.data.processedPhotos}장`);
      addMessage(`🎉 신규 스토리 앨범 발행 수: ${pipelineRes.data.storiesGenerated}건`);

      setStep(3);
    } catch (error: any) {
      console.error(error);
      addMessage(`❌ 파이프라인 처리 실패: ${error.response?.data?.message || error.message}`);
      alert('서버 처리 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
        <h2 className="text-2xl font-bold text-gray-900">원클릭 스토리북 발행 엔진</h2>
        <p className="text-gray-500 mt-1">
          행사 사진을 밀어넣기만 하세요! AI 분석, 학생 매칭, 맞춤형 앨범 생성까지 한 번에 완료됩니다.
        </p>
      </div>

      {/* Progress Steps */}
      <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm flex items-center justify-between px-16 relative">
        <div className="absolute inset-x-16 top-1/2 -z-10 h-0.5 bg-gray-100 flex -translate-y-1/2">
        </div>
        
        {[
          { num: 1, label: '사진 선택', icon: UploadCloud },
          { num: 2, label: '자동화 파이프라인', icon: Sparkles },
          { num: 3, label: '앨범 발행 완료', icon: CheckCircle }
        ].map((s) => {
          const Icon = s.icon;
          const isActive = step >= s.num;
          const isCurrent = step === s.num;
          
          return (
            <div key={s.num} className="flex flex-col items-center bg-white px-4 z-10 transition-all">
              <div 
                className={`w-12 h-12 rounded-full flex items-center justify-center border-2 transition-colors ${
                  isActive 
                    ? 'border-indigo-600 bg-indigo-50 text-indigo-600' 
                    : 'border-gray-200 bg-gray-50 text-gray-400'
                } ${isCurrent ? 'ring-4 ring-indigo-100' : ''}`}
              >
                <Icon size={24} />
              </div>
              <p className={`mt-2 font-medium text-sm ${isActive ? 'text-gray-900' : 'text-gray-400'}`}>
                {s.label}
              </p>
            </div>
          );
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          {step <= 2 && (
            <div className={`bg-white p-6 rounded-xl border transition-all ${step === 1 ? 'border-indigo-300 ring-1 ring-indigo-300 shadow-md' : 'border-gray-200 shadow-sm opacity-60'}`}>
              <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2 mb-4">
                <span className="flex justify-center items-center w-6 h-6 rounded bg-indigo-100 text-indigo-700 text-sm">{step}</span>
                {step === 1 ? '전체 학교 행사 사진 선택' : '자동 파이프라인 가동 중...'}
              </h3>
              
              <div className="space-y-4">
                <div 
                  className={`border-2 border-dashed rounded-xl p-8 text-center transition-colors cursor-pointer ${
                    step === 1 ? 'border-gray-300 hover:border-indigo-400 hover:bg-indigo-50' : 'border-gray-200 pointer-events-none'
                  }`}
                  onClick={() => step === 1 && fileInputRef.current?.click()}
                >
                  <input 
                    type="file" 
                    multiple 
                    accept="image/*" 
                    className="hidden" 
                    ref={fileInputRef}
                    onChange={handleFileChange}
                    disabled={step !== 1 || loading}
                  />
                  <UploadCloud size={40} className={`mx-auto mb-3 ${step === 1 ? 'text-indigo-500' : 'text-gray-400'}`} />
                  <p className="font-medium text-gray-700">클릭하여 단체 행사 사진들을 고르세요</p>
                  <p className="text-sm text-gray-400 mt-1">이 사진들은 학생별로 쪼개져 앨범에 자동으로 들어갑니다.</p>
                </div>

                {files.length > 0 && (
                  <div className="bg-gray-50 p-4 rounded-lg border border-gray-200 space-y-3">
                    <div className="flex justify-between items-center text-sm font-medium text-gray-700">
                      <span>선택된 파일 ({files.length}장)</span>
                      {step === 1 && (
                        <button onClick={() => setFiles([])} className="text-red-500 hover:underline">전체 삭제</button>
                      )}
                    </div>
                    <div className="grid grid-cols-4 sm:grid-cols-6 gap-2 max-h-40 overflow-y-auto pr-2">
                      {files.map((f, idx) => (
                        <div key={idx} className="relative group aspect-square bg-gray-200 rounded-md overflow-hidden border border-gray-300">
                          <img src={URL.createObjectURL(f)} alt={f.name} className="w-full h-full object-cover" />
                          {step === 1 && (
                            <button 
                              onClick={(e) => { e.stopPropagation(); removeFile(idx); }}
                              className="absolute top-1 right-1 bg-black/50 text-white rounded-full p-0.5 opacity-0 group-hover:opacity-100 transition-opacity"
                            >
                              <X size={14} />
                            </button>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {step === 1 && (
                  <button
                    onClick={handleUploadAndProcess}
                    disabled={files.length === 0 || loading}
                    className="w-full flex justify-center items-center gap-2 py-4 bg-indigo-600 text-white rounded-xl font-bold text-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors shadow-lg shadow-indigo-200"
                  >
                    🚀 원클릭 파이프라인 가동! <ArrowRight size={20} />
                  </button>
                )}
                {step === 2 && (
                  <div className="w-full py-4 text-center text-indigo-700 font-bold bg-indigo-50 rounded-xl animate-pulse">
                    AI 분석 및 앨범을 발행하고 있습니다... 잠시만 기다려주세요!
                  </div>
                )}
              </div>
            </div>
          )}

          {step === 3 && (
             <div className="bg-green-50 p-6 rounded-xl border border-green-200 shadow-sm text-center">
               <CheckCircle size={48} className="mx-auto text-green-500 mb-4" />
               <h3 className="text-2xl font-bold text-green-800 mb-2">학생별 맞춤형 앨범 발행이 완료되었습니다!</h3>
               <p className="text-green-700 mb-8">AI가 얼굴을 식별하여 각 학생들의 고유한 스토리북을 엮어주었습니다.</p>
               <div className="flex gap-4 justify-center">
                 <button 
                  onClick={() => {
                    setStep(1);
                    setFiles([]);
                    setMessages([]);
                  }}
                  className="px-6 py-3 bg-white text-green-700 font-bold rounded-xl border border-green-300 hover:bg-green-50 transition-colors shadow-sm"
                >
                  다른 행사 사진 추가하기
                </button>
                <button 
                  onClick={() => navigate('/')}
                  className="px-6 py-3 bg-green-600 text-white font-bold rounded-xl hover:bg-green-700 transition-colors shadow-lg shadow-green-200"
                >
                  대시보드에서 앨범 확인하기
                </button>
               </div>
             </div>
          )}
        </div>

        <div className="lg:col-span-1 bg-gray-900 rounded-xl overflow-hidden flex flex-col h-[550px] lg:h-auto border border-gray-800 shadow-xl">
          <div className="bg-gray-800 px-4 py-3 flex items-center justify-between border-b border-gray-700">
            <h3 className="text-sm font-semibold text-gray-200 flex items-center gap-2">
              <span className={`w-2 h-2 rounded-full ${loading ? 'bg-indigo-400 animate-pulse' : step === 3 ? 'bg-green-400' : 'bg-gray-500'}`}></span>
              엔진 처리 로그
            </h3>
          </div>
          <div className="flex-1 p-4 overflow-y-auto font-mono text-xs sm:text-sm space-y-2 relative">
            {messages.length === 0 ? (
              <div className="text-gray-500 italic h-full flex flex-col items-center justify-center opacity-70">
                <Sparkles size={32} className="mb-2" />
                <span>버튼을 누르면 엔진이 가동됩니다.</span>
              </div>
            ) : (
              messages.map((m, i) => (
                <div key={i} className={`pb-1 border-b border-gray-800/50 ${
                  m.includes('❌') ? 'text-red-400 font-bold' :
                  m.includes('✅') || m.includes('🎉') ? 'text-green-400 font-bold' :
                  'text-gray-300'
                }`}>
                  <span className="text-gray-600 mr-2">[{new Date().toLocaleTimeString('en-US', {hour12: false})}]</span>
                  {m}
                </div>
              ))
            )}
            {loading && (
              <div className="text-indigo-400 flex items-center gap-2 pt-2">
                <div className="w-3 h-3 border-2 border-indigo-400 border-t-transparent rounded-full animate-spin"></div>
                파이프라인 통신 중...
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PhotoUpload;
