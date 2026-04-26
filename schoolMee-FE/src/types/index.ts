export interface SchoolDashboardResponse {
  schoolId: number;
  name: string;
  totalStudents: number;
  storiesGenerated: number;
  ordersCreated: number;
  ordersCompleted: number;
  createdAt: string;
}

export interface StudentItem {
  studentId: number;
  name: string;
  grade: number;
  classNum: number;
  hasStory: boolean;
  hasOrder: boolean;
}

export interface StudentListResponse {
  schoolId: number;
  students: StudentItem[];
}

export interface StoryGenerateResponse {
  schoolId: number;
  generated: number;
  skipped: number;
  message: string;
}

export interface PhotoDto {
  photoId: number;
  url: string;
  totalScore: number;
}

export interface ChapterDto {
  chapterId: number;
  title: string;
  sequence: number;
  photos: PhotoDto[];
}

export interface StoryResponse {
  storyId: number;
  title: string;
  studentId: number;
  studentName: string;
  createdAt: string;
  chapters: ChapterDto[];
}

export interface OrderItem {
  orderId: number;
  studentId: number;
  studentName: string;
  storyId: number;
  storyTitle: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED';
  createdAt: string;
}

export interface OrderListResponse {
  schoolId: number;
  summary: {
    PENDING: number;
    PROCESSING: number;
    COMPLETED: number;
  };
  orders: OrderItem[];
}

export interface SchoolExportResponse {
  schoolId: number;
  schoolName: string;
  exportedAt: string;
  totalOrders: number;
  orders: any[]; // 자세한 스키마는 생략
}
