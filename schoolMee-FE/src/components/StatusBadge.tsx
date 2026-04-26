import React from 'react';

interface Props {
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED';
}

const StatusBadge = ({ status }: Props) => {
  const statusConfig = {
    PENDING: { color: 'bg-yellow-100 text-yellow-800', label: '주문 대기' },
    PROCESSING: { color: 'bg-blue-100 text-blue-800', label: '제작 진행중' },
    COMPLETED: { color: 'bg-green-100 text-green-800', label: '제작 완료' },
  };

  const config = statusConfig[status];

  return (
    <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${config.color}`}>
      {config.label}
    </span>
  );
};

export default StatusBadge;
