import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import PhotoUpload from './pages/PhotoUpload';
import StudentList from './pages/StudentList';
import StoryDetail from './pages/StoryDetail';
import OrderManagement from './pages/OrderManagement';
import ExportPage from './pages/ExportPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="photos" element={<PhotoUpload />} />
          <Route path="students" element={<StudentList />} />
          <Route path="students/:studentId/stories" element={<StoryDetail />} />
          <Route path="orders" element={<OrderManagement />} />
          <Route path="export" element={<ExportPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
