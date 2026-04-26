import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getStudentStories, createOrder } from '../api/endpoints';
import { StoryResponse } from '../types';
import { ArrowLeft, CheckCircle, Image as ImageIcon } from 'lucide-react';

const StoryDetail = () => {
  const { studentId } = useParams<{ studentId: string }>();
  const navigate = useNavigate();
  const [story, setStory] = useState<StoryResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [ordering, setOrdering] = useState(false);

  useEffect(() => {
    if (!studentId) return;
    const fetchStory = async () => {
      try {
        const res = await getStudentStories(Number(studentId));
        // MVP: 배열의 첫 번째 스토리를 사용
        if (res.data && res.data.length > 0) {
          setStory(res.data[0]);
        }
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchStory();
  }, [studentId]);

  const handleCreateOrder = async () => {
    if (!story) return;
    if (!confirm('이 스토리로 앨범 제작 주문을 생성하시겠습니까?')) return;
    
    setOrdering(true);
    try {
      await createOrder(story.storyId);
      alert('주문이 성공적으로 생성되었습니다.');
      navigate('/orders');
    } catch (error: any) {
      console.error(error);
      alert(error.response?.data?.message || '주문 생성 중 오류가 발생했습니다. (이미 주문되었을 수 있습니다.)');
    } finally {
      setOrdering(false);
    }
  };

  if (loading) return <div>로딩 중...</div>;
  if (!story) return <div className="p-8 text-center text-gray-500">생성된 스토리가 없습니다.</div>;

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate(-1)} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <ArrowLeft size={20} className="text-gray-600" />
          </button>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">{story.title}</h2>
            <p className="text-gray-500 mt-1">
              학생: <span className="font-medium text-gray-700">{story.studentName}</span> | 
              생성일: {new Date(story.createdAt).toLocaleDateString()}
            </p>
          </div>
        </div>
        <button
          onClick={handleCreateOrder}
          disabled={ordering}
          className="flex items-center gap-2 px-5 py-2.5 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors shadow-sm"
        >
          <CheckCircle size={18} />
          {ordering ? '주문 생성 중...' : '검수 완료 및 주문 생성'}
        </button>
      </div>

      <div className="space-y-8">
        {story.chapters.map((chapter) => (
          <div key={chapter.chapterId} className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
            <div className="border-b border-gray-100 pb-4 mb-4">
              <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2">
                <span className="flex items-center justify-center w-6 h-6 rounded-full bg-indigo-100 text-indigo-700 text-sm">
                  {chapter.sequence}
                </span>
                {chapter.title}
              </h3>
            </div>
            
            {chapter.photos.length > 0 ? (
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                {chapter.photos.map((photo) => (
                  <div key={photo.photoId} className="group relative aspect-[4/3] rounded-lg overflow-hidden bg-gray-100 border border-gray-200">
                    <img 
                      src={photo.url} 
                      alt={`Photo ${photo.photoId}`} 
                      className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                      onError={(e) => {
                        // S3 더미 URL이 깨질 경우 대비
                        (e.target as HTMLImageElement).src = `https://picsum.photos/seed/${photo.photoId}/400/300`;
                      }}
                    />
                    <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/70 to-transparent p-3 pt-8">
                      <p className="text-white text-xs font-medium flex justify-between items-center">
                        <span>AI Score</span>
                        <span className="bg-indigo-500 px-1.5 py-0.5 rounded text-[10px]">{photo.totalScore}</span>
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-8 text-gray-400 bg-gray-50 rounded-lg border border-dashed border-gray-200">
                <ImageIcon size={32} className="mb-2 opacity-50" />
                <p className="text-sm">해당 챕터에 적합한 사진이 부족합니다.</p>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default StoryDetail;
