import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { uploadPhotos, analyzePhotos, matchStudents } from '../api/endpoints';
import { UploadCloud, Image as ImageIcon, Sparkles, Users, CheckCircle, ArrowRight, X } from 'lucide-react';

const PhotoUpload = () => {
  const [files, setFiles] = useState<File[]>([]);
  const [uploadedPhotoIds, setUploadedPhotoIds] = useState<number[]>([]);
  const [step, setStep] = useState<1 | 2 | 3 | 4>(1); // 1: Upload, 2: Analyze, 3: Match, 4: Done
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const SCHOOL_ID = 1; // MVP

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

  const handleUpload = async () => {
    if (files.length === 0) {
      alert('업로드할 사진을 선택해주세요.');
      return;
    }
    setLoading(true);
    try {
      addMessage(`사진 ${files.length}장 업로드 시작...`);
      const res = await uploadPhotos(SCHOOL_ID, files);
      const ids = res.data.photos.map(p => p.photoId);
      setUploadedPhotoIds(ids);
      addMessage(`✅ 업로드 완료: ${res.data.uploadedCount}장`);
      setStep(2);
    } catch (error: any) {
      console.error(error);
      addMessage(`❌ 업로드 실패: ${error.response?.data?.message || error.message}`);
      alert('사진 업로드 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleAnalyze = async () => {
    setLoading(true);
    try {
      addMessage('AI 분석(시뮬레이션) 시작...');
      const res = await analyzePhotos(uploadedPhotoIds);
      addMessage(`✅ 분석 완료: ${res.data.processedCount}장`);
      setStep(3);
    } catch (error: any) {
      console.error(error);
      addMessage(`❌ 분석 실패: ${error.response?.data?.message || error.message}`);
      alert('AI 분석 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleMatch = async () => {
    setLoading(true);
    try {
      addMessage('학생 매칭 시작...');
      const res = await matchStudents(uploadedPhotoIds);
      addMessage(`✅ 매칭 완료: 대상 사진 ${res.data.processedPhotoCount}장, 매칭 학생 ${res.data.matchedStudentCount}명`);
      setStep(4);
    } catch (error: any) {
      console.error(error);
      addMessage(`❌ 매칭 실패: ${error.response?.data?.message || error.message}`);
      alert('학생 매칭 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
        <h2 className="text-2xl font-bold text-gray-900">사진 업로드 및 매칭 파이프라인</h2>
        <p className="text-gray-500 mt-1">
          행사 사진을 업로드하고, AI로 점수를 분석하여 학생들과 매칭합니다.
        </p>
      </div>

      {/* Progress Steps */}
      <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm flex items-center justify-between px-10 relative">
        <div className="absolute inset-x-10 top-1/2 -z-10 h-0.5 bg-gray-100 flex -translate-y-1/2">
          {/* Progress bar line */}
        </div>
        
        {[
          { num: 1, label: '사진 업로드', icon: UploadCloud },
          { num: 2, label: 'AI 분석', icon: Sparkles },
          { num: 3, label: '학생 매칭', icon: Users },
          { num: 4, label: '완료', icon: CheckCircle }
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
        {/* Main Action Area */}
        <div className="lg:col-span-2 space-y-6">
          {/* Step 1: Upload */}
          <div className={`bg-white p-6 rounded-xl border transition-all ${step === 1 ? 'border-indigo-300 ring-1 ring-indigo-300 shadow-md' : 'border-gray-200 shadow-sm opacity-60'}`}>
            <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2 mb-4">
              <span className="flex justify-center items-center w-6 h-6 rounded bg-indigo-100 text-indigo-700 text-sm">1</span>
              사진 업로드
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
                <p className="font-medium text-gray-700">클릭하여 사진을 선택하세요</p>
                <p className="text-sm text-gray-400 mt-1">PNG, JPG, WEBP (여러 장 선택 가능)</p>
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

              <button
                onClick={handleUpload}
                disabled={step !== 1 || files.length === 0 || loading}
                className="w-full flex justify-center items-center gap-2 py-3 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700 disabled:opacity-50 transition-colors"
              >
                {loading && step === 1 ? '업로드 중...' : '서버로 업로드 (PENDING)'} <ArrowRight size={18} />
              </button>
            </div>
          </div>

          {/* Step 2: Analyze */}
          <div className={`bg-white p-6 rounded-xl border transition-all ${step === 2 ? 'border-indigo-300 ring-1 ring-indigo-300 shadow-md' : 'border-gray-200 shadow-sm opacity-60'}`}>
            <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2 mb-4">
              <span className="flex justify-center items-center w-6 h-6 rounded bg-indigo-100 text-indigo-700 text-sm">2</span>
              AI 점수 분석 (시뮬레이션)
            </h3>
            <p className="text-gray-600 text-sm mb-4">
              업로드된 PENDING 상태의 사진들을 분석하여 미소 점수(smileScore), 활동 점수(activityScore)를 랜덤(시뮬레이션)으로 부여하고 ANALYZED 상태로 변경합니다.
            </p>
            <button
              onClick={handleAnalyze}
              disabled={step !== 2 || loading}
              className="w-full flex justify-center items-center gap-2 py-3 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700 disabled:opacity-50 transition-colors"
            >
              <Sparkles size={18} />
              {loading && step === 2 ? '분석 중...' : 'AI 분석 실행'} <ArrowRight size={18} />
            </button>
          </div>

          {/* Step 3: Match */}
          <div className={`bg-white p-6 rounded-xl border transition-all ${step === 3 ? 'border-indigo-300 ring-1 ring-indigo-300 shadow-md' : 'border-gray-200 shadow-sm opacity-60'}`}>
            <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2 mb-4">
              <span className="flex justify-center items-center w-6 h-6 rounded bg-indigo-100 text-indigo-700 text-sm">3</span>
              학생 자동 매칭
            </h3>
            <p className="text-gray-600 text-sm mb-4">
              분석이 완료된 ANALYZED 사진에서 감지된 얼굴 수만큼 학생들을 매칭하고, 최종적으로 MATCHED 상태로 변경합니다. 이 단계가 끝나면 스토리 생성이 가능해집니다.
            </p>
            <button
              onClick={handleMatch}
              disabled={step !== 3 || loading}
              className="w-full flex justify-center items-center gap-2 py-3 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700 disabled:opacity-50 transition-colors"
            >
              <Users size={18} />
              {loading && step === 3 ? '매칭 중...' : '학생 매칭 실행'} <ArrowRight size={18} />
            </button>
          </div>
          
          {/* Step 4: Done */}
          {step === 4 && (
             <div className="bg-green-50 p-6 rounded-xl border border-green-200 shadow-sm text-center">
               <CheckCircle size={40} className="mx-auto text-green-500 mb-3" />
               <h3 className="text-xl font-bold text-green-800 mb-2">모든 준비가 완료되었습니다!</h3>
               <p className="text-green-700 mb-6">이제 대시보드나 학생 목록에서 스토리를 생성할 수 있습니다.</p>
               <div className="flex gap-4 justify-center">
                 <button 
                  onClick={() => {
                    setStep(1);
                    setFiles([]);
                    setUploadedPhotoIds([]);
                    setMessages([]);
                  }}
                  className="px-5 py-2 bg-white text-green-700 font-medium rounded-lg border border-green-300 hover:bg-green-50 transition-colors"
                >
                  새로운 사진 추가
                </button>
                <button 
                  onClick={() => navigate('/')}
                  className="px-5 py-2 bg-green-600 text-white font-medium rounded-lg hover:bg-green-700 transition-colors"
                >
                  대시보드로 이동
                </button>
               </div>
             </div>
          )}
        </div>

        {/* Logs Console */}
        <div className="lg:col-span-1 bg-gray-900 rounded-xl overflow-hidden flex flex-col h-[500px] lg:h-auto border border-gray-800 shadow-lg">
          <div className="bg-gray-800 px-4 py-3 flex items-center justify-between border-b border-gray-700">
            <h3 className="text-sm font-semibold text-gray-200 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse"></span>
              파이프라인 로그
            </h3>
            {messages.length > 0 && (
              <button 
                onClick={() => setMessages([])}
                className="text-gray-400 hover:text-white text-xs"
              >
                Clear
              </button>
            )}
          </div>
          <div className="flex-1 p-4 overflow-y-auto font-mono text-xs sm:text-sm space-y-2">
            {messages.length === 0 ? (
              <div className="text-gray-500 italic h-full flex items-center justify-center">
                수행 결과가 여기에 표시됩니다.
              </div>
            ) : (
              messages.map((m, i) => (
                <div key={i} className={`pb-1 border-b border-gray-800/50 ${
                  m.includes('❌') ? 'text-red-400' :
                  m.includes('✅') ? 'text-green-400' :
                  'text-gray-300'
                }`}>
                  <span className="text-gray-600 mr-2">[{new Date().toLocaleTimeString()}]</span>
                  {m}
                </div>
              ))
            )}
            {loading && (
              <div className="text-yellow-400 flex items-center gap-2">
                <div className="w-3 h-3 border-2 border-yellow-400 border-t-transparent rounded-full animate-spin"></div>
                처리 중...
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default PhotoUpload;
