import axios from 'axios';

export const api = axios.create({
  // 실제 백엔드 주소 (로컬)
  baseURL: '/api/v1/admin',
  headers: {
    'Content-Type': 'application/json',
  },
});
