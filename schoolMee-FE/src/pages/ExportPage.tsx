import React, { useState } from 'react';
import { exportSchoolData } from '../api/endpoints';
import { Download, FileJson } from 'lucide-react';

const ExportPage = () => {
  const [exporting, setExporting] = useState(false);
  const SCHOOL_ID = 1;

  const handleExport = async () => {
    setExporting(true);
    try {
      const res = await exportSchoolData(SCHOOL_ID);
      
      // JSON 다운로드 로직
      const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(res.data, null, 2));
      const downloadAnchorNode = document.createElement('a');
      downloadAnchorNode.setAttribute("href", dataStr);
      downloadAnchorNode.setAttribute("download", `schoolmee_export_school_${SCHOOL_ID}_${new Date().getTime()}.json`);
      document.body.appendChild(downloadAnchorNode);
      downloadAnchorNode.click();
      downloadAnchorNode.remove();
      
    } catch (error) {
      console.error(error);
      alert('데이터 Export 중 오류가 발생했습니다.');
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto mt-10">
      <div className="bg-white p-8 rounded-xl border border-gray-200 shadow-sm text-center">
        <div className="w-16 h-16 bg-indigo-50 rounded-full flex items-center justify-center mx-auto mb-4">
          <FileJson size={32} className="text-indigo-600" />
        </div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">인쇄소 전달용 데이터 Export</h2>
        <p className="text-gray-500 mb-8">
          학교 전체 주문 데이터를 JSON 형태로 다운로드합니다.<br/>
          (인쇄 파트너 시스템 연동용)
        </p>
        
        <button
          onClick={handleExport}
          disabled={exporting}
          className="inline-flex items-center gap-2 px-6 py-3 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 disabled:opacity-50 transition-colors shadow-sm"
        >
          <Download size={20} />
          {exporting ? '데이터 생성 및 다운로드 중...' : '전체 주문 데이터 JSON 다운로드'}
        </button>
      </div>
    </div>
  );
};

export default ExportPage;
