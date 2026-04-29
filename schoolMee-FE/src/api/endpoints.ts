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
  PhotoMatchResponse,
  PipelineStatusResponse,
  StudentBulkUploadResponse
} from '../types';

// School
export const getSchools = () => 
  api.get<SchoolDashboardResponse[]>('/api/v1/admin/schools');

export const registerSchool = (name: string) =>
  api.post<number>('/api/v1/admin/schools', { name });

export const getDashboard = (schoolId: number) => 
  api.get<SchoolDashboardResponse>(`/api/v1/admin/schools/${schoolId}`);

export const createClassRoom = (schoolId: number, data: { grade: number; classNum: number }) =>
  api.post<number>(`/api/v1/admin/schools/${schoolId}/classes`, data);

// Student
export const getStudents = (schoolId: number) => 
  api.get<StudentListResponse>(`/api/v1/admin/schools/${schoolId}/students`);

// Profile Photo Upload (Anchor)
export const uploadProfilePhoto = (studentId: number, file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post(`/api/v1/admin/students/${studentId}/profile-photo`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

export const bulkUploadProfilesAndCreateStudents = (schoolId: number, files: File[]) => {
  const formData = new FormData();
  formData.append('schoolId', schoolId.toString());
  files.forEach(f => formData.append('files', f));
  return api.post<StudentBulkUploadResponse>('/api/v1/admin/students/profile-bulk-upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

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
export const uploadPhotos = (schoolId: number, type: 'PROFILE' | 'GROUP', files: File[]) => {
  const formData = new FormData();
  formData.append('schoolId', schoolId.toString());
  formData.append('type', type);
  files.forEach(file => {
    formData.append('files', file);
  });
  return api.post<PhotoUploadResponse>('/api/v1/admin/photos/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};

export const runPipeline = (photoIds: number[], schoolId: number) => {
  return api.post<PipelineStatusResponse>('/api/v1/admin/pipeline/process', { photoIds, schoolId });
};

export const getPipelineStatus = (jobId: number) => {
  return api.get<PipelineStatusResponse>(`/api/v1/admin/pipeline/status/${jobId}`);
};

export const analyzePhotos = (photoIds: number[]) => {
  return api.post<PhotoAnalysisResponse>('/api/v1/admin/photos/analyze', photoIds);
};

export const matchStudents = (photoIds: number[]) => {
  return api.post<PhotoMatchResponse>('/api/v1/admin/photos/match', photoIds);
};

// Demo
export const setupDemoData = () => 
  api.post<string>('/api/v1/admin/demo/setup');

export const resetDemoData = () =>
  api.post<string>('/api/v1/admin/demo/reset');
