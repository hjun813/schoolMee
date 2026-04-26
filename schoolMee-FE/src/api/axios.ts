import axios from 'axios';

export const api = axios.create({
  // 실제 백엔드 주소 (로컬)
  baseURL: 'http://localhost:8080/api/v1/admin',
  headers: {
    'Content-Type': 'application/json',
  },
});
