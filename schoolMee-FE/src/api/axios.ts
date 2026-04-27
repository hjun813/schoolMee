import axios from 'axios';

export const api = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for debugging
api.interceptors.request.use(config => {
  console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, config.data || '');
  return config;
}, error => {
  console.error('[API Request Error]', error);
  return Promise.reject(error);
});

// Response interceptor for debugging
api.interceptors.response.use(response => {
  console.log(`[API Response] ${response.status} ${response.config.url}`);
  return response;
}, error => {
  console.error(`[API Error] ${error.response?.status || 'Unknown'} ${error.config?.url}`, error.response?.data || error.message);
  return Promise.reject(error);
});
