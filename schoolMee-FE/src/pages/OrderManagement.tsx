import { useEffect, useState } from 'react';
import { getOrders, updateOrderStatus } from '../api/endpoints';
import { useSchool } from '../context/SchoolContext';
import type { OrderItem } from '../types';
import StatusBadge from '../components/StatusBadge';
import { Package } from 'lucide-react';

const OrderManagement = () => {
  const [orders, setOrders] = useState<OrderItem[]>([]);
  const [summary, setSummary] = useState({ PENDING: 0, PROCESSING: 0, COMPLETED: 0 });
  const [loading, setLoading] = useState(true);
  const { currentSchoolId: schoolId } = useSchool();

  const fetchOrders = async (id: number) => {
    try {
      const res = await getOrders(id);
      setOrders(res.data.orders);
      setSummary(res.data.summary);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (schoolId) {
      fetchOrders(schoolId);
    }
  }, [schoolId]);

  const handleStatusChange = async (orderId: number, newStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED') => {
    try {
      await updateOrderStatus(orderId, newStatus);
      if (schoolId) fetchOrders(schoolId); // 갱신
    } catch (error) {
      console.error(error);
      alert('상태 업데이트에 실패했습니다.');
    }
  };

  if (loading) return <div>로딩 중...</div>;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-3 gap-4">
        {Object.entries(summary).map(([key, count]) => (
          <div key={key} className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Package size={20} className="text-indigo-500" />
              <span className="font-medium text-gray-600">{key === 'PENDING' ? '주문 대기' : key === 'PROCESSING' ? '제작 진행중' : '제작 완료'}</span>
            </div>
            <span className="text-2xl font-bold text-gray-900">{count as number}</span>
          </div>
        ))}
      </div>

      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 border-b border-gray-200 text-gray-500 text-sm">
              <th className="p-4 font-medium">주문 ID</th>
              <th className="p-4 font-medium">학생</th>
              <th className="p-4 font-medium">스토리 제목</th>
              <th className="p-4 font-medium">주문일시</th>
              <th className="p-4 font-medium">상태</th>
              <th className="p-4 font-medium text-right">상태 변경</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {orders.map((order) => (
              <tr key={order.orderId} className="hover:bg-gray-50 transition-colors">
                <td className="p-4 text-gray-600">#{order.orderId}</td>
                <td className="p-4 font-medium text-gray-900">{order.studentName}</td>
                <td className="p-4 text-gray-600">{order.storyTitle}</td>
                <td className="p-4 text-gray-500 text-sm">{new Date(order.createdAt).toLocaleDateString()}</td>
                <td className="p-4">
                  <StatusBadge status={order.status} />
                </td>
                <td className="p-4 text-right">
                  <select
                    value={order.status}
                    onChange={(e) => handleStatusChange(order.orderId, e.target.value as any)}
                    className="border border-gray-300 rounded-md text-sm px-2 py-1.5 focus:ring-2 focus:ring-indigo-500 outline-none"
                  >
                    <option value="PENDING">대기</option>
                    <option value="PROCESSING">진행중</option>
                    <option value="COMPLETED">완료</option>
                  </select>
                </td>
              </tr>
            ))}
            {orders.length === 0 && (
              <tr>
                <td colSpan={6} className="p-8 text-center text-gray-500">
                  주문 내역이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default OrderManagement;
