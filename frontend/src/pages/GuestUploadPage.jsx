import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client.js';
import { useRef } from 'react';


export default function GuestUploadPage() {
  const { slug } = useParams();
  const [event, setEvent] = useState(null);
  const [progress, setProgress] = useState({});
  const [done, setDone] = useState(false);
  const fileInputRef = useRef(null);

  useEffect(() => {
    api.get(`/api/public/events/${slug}`).then(res => setEvent(res.data));
  }, [slug]);

  async function uploadOne(file) {
    const signed = await api.post(`/api/public/events/${slug}/upload-url`, {
      filename: file.name,
      contentType: file.type,
      size: file.size
    });

    const { uploadUrl, storageKey, method } = signed.data;
    if (method === 'POST') {
      const formData = new FormData();
      formData.append('file', file);
      await api.post(uploadUrl, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: e => setProgress(p => ({ ...p, [file.name]: Math.round((e.loaded * 100) / e.total) }))
      });
    } else {
      await api.put(uploadUrl, file, {
        headers: { 'Content-Type': file.type },
        onUploadProgress: e => setProgress(p => ({ ...p, [file.name]: Math.round((e.loaded * 100) / e.total) }))
      });
    }

    await api.post(`/api/public/events/${slug}/media/complete`, {
      storageKey,
      filename: file.name,
      contentType: file.type,
      size: file.size
    });
  }

  async function uploadAll(selectedFiles) {
    setDone(false);

    for (const file of selectedFiles) {
      await uploadOne(file);
    }

    setDone(true);
  }

  return <main className="container">
    <section className="card">
      <p className="muted">Shared Memories</p>
      <h1>{event?.name || 'Evento'}</h1>
      <p>{event?.description || 'Carica qui foto e video della giornata.'}</p>

      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="image/*,video/*"
        style={{ display: 'none' }}
        onChange={e => {
          const selectedFiles = Array.from(e.target.files || []);
          if (selectedFiles.length > 0) {
            uploadAll(selectedFiles);
          }
          e.target.value = '';
        }}
      />

      <div
        className="upload-dropzone"
        onClick={() => fileInputRef.current?.click()}
      >
        <div className="upload-plus">+</div>
        <h2>Aggiungi foto o video</h2>
        <p>Tocca qui per caricare i tuoi ricordi</p>
      </div>

      {done && <h2 className='upload-successfull'><strong>Grazie per aver condiviso con noi il tuo ricordo!</strong></h2>}
      <p style={{ marginTop: 20 }}><Link to={`/e/${slug}/gallery`}>Guarda la galleria</Link></p>
    </section>
  </main>;
}
