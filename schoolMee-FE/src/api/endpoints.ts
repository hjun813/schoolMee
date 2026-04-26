import { api } from './axios';
import type {
  SchoolDashboardResponse,
  StudentListResponse,
  StoryGenerateResponse,
  StoryResponse,
  OrderListResponse,
  SchoolExportResponse
} from '../types';

// School
export const getDashboard = (schoolId: number) => 
  api.get<SchoolDashboardResponse>(`/schools/${schoolId}`);

// Student
export const getStudents = (schoolId: number) => 
  api.get<StudentListResponse>(`/schools/${schoolId}/students`);

// Story
export const generateStories = (schoolId: number) => 
  api.post<StoryGenerateResponse>(`/schools/${schoolId}/stories/generate`);

export const getStudentStories = (studentId: number) => 
  api.get<StoryResponse[]>(`/students/${studentId}/stories`);

// Order
export const createOrder = (storyId: number) => 
  api.post('/orders', { storyId });

export const getOrders = (schoolId: number) => 
  api.get<OrderListResponse>(`/schools/${schoolId}/orders`);

export const updateOrderStatus = (orderId: number, status: 'PENDING' | 'PROCESSING' | 'COMPLETED') => 
  api.patch(`/orders/${orderId}/status`, { status });

// Export
export const exportSchoolData = (schoolId: number) => 
  api.get<SchoolExportResponse>(`/schools/${schoolId}/export`);
