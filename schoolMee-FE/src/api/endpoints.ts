import { api } from './axios';
import type {
  SchoolDashboardResponse,
  StudentListResponse,
  StoryGenerateResponse,
  StoryResponse,
  OrderListResponse,
  SchoolExportResponse,
  PhotoUploadResponse,
  PhotoAnalysisResponse,
  PhotoMatchResponse
} from '../types';

// School
export const getDashboard = (schoolId: number) => 
  api.get<SchoolDashboardResponse>(`/api/v1/admin/schools/${schoolId}`);

// Student
export const getStudents = (schoolId: number) => 
  api.get<StudentListResponse>(`/api/v1/admin/schools/${schoolId}/students`);

// Story
export const generateStories = (schoolId: number) => 
  api.post<StoryGenerateResponse>(`/api/v1/admin/schools/${schoolId}/stories/generate`);

export const getStudentStories = (studentId: number) => 
  api.get<StoryResponse[]>(`/api/v1/admin/students/${studentId}/stories`);

// Order
export const createOrder = (storyId: number) => 
  api.post('/api/v1/admin/orders', { storyId });

export const getOrders = (schoolId: number) => 
  api.get<OrderListResponse>(`/api/v1/admin/schools/${schoolId}/orders`);

export const updateOrderStatus = (orderId: number, status: 'PENDING' | 'PROCESSING' | 'COMPLETED') => 
  api.patch(`/api/v1/admin/orders/${orderId}/status`, { status });

// Export
export const exportSchoolData = (schoolId: number) => 
  api.get<SchoolExportResponse>(`/api/v1/admin/schools/${schoolId}/export`);

// Photos Pipeline
export const uploadPhotos = (schoolId: number, files: File[]) => {
  const formData = new FormData();
  formData.append('schoolId', schoolId.toString());
  files.forEach(file => {
    formData.append('files', file);
  });
  return api.post<PhotoUploadResponse>('/api/v1/admin/photos/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

export const analyzePhotos = (photoIds: number[]) => {
  return api.post<PhotoAnalysisResponse>('/api/v1/admin/photos/analyze', photoIds);
};

export const matchStudents = (photoIds: number[]) => {
  return api.post<PhotoMatchResponse>('/api/v1/admin/photos/match', photoIds);
};
