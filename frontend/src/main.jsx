import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes, Navigate } from 'react-router-dom';
import './style.css';
import AdminEventsPage from './pages/AdminEventsPage.jsx';
import AdminEventDetailPage from './pages/AdminEventDetailPage.jsx';
import GuestUploadPage from './pages/GuestUploadPage.jsx';
import PublicGalleryPage from './pages/PublicGalleryPage.jsx';

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/admin/events" replace />} />
        <Route path="/admin/events" element={<AdminEventsPage />} />
        <Route path="/admin/events/:id" element={<AdminEventDetailPage />} />
        <Route path="/e/:slug" element={<GuestUploadPage />} />
        <Route path="/e/:slug/gallery" element={<PublicGalleryPage />} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);
