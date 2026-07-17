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
  const [pendingFiles, setPendingFiles] = useState([]);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadInfo, setUploadInfo] = useState({
    uploadedBy: "",
    message: "",
  });
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    api.get(`/api/public/events/${slug}`).then(res => setEvent(res.data));
  }, [slug]);

  async function uploadOne(file, metadata) {
    const signed = await api.post(
      `/api/public/events/${slug}/upload-url`,
      {
        filename: file.name,
        contentType: file.type,
        size: file.size,
      }
    );

    const { uploadUrl, storageKey, method } = signed.data;

    if (method === "POST") {
      const formData = new FormData();
      formData.append("file", file);

      await api.post(uploadUrl, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
        onUploadProgress: (e) =>
          setProgress((p) => ({
            ...p,
            [file.name]: e.total
              ? Math.round((e.loaded * 100) / e.total)
              : 0,
          })),
      });
    } else {
      await api.put(uploadUrl, file, {
        headers: {
          "Content-Type": file.type || "application/octet-stream",
        },
        onUploadProgress: (e) =>
          setProgress((p) => ({
            ...p,
            [file.name]: e.total
              ? Math.round((e.loaded * 100) / e.total)
              : 0,
          })),
      });
    }

    await api.post(`/api/public/events/${slug}/media/complete`, {
      storageKey,
      filename: file.name,
      contentType: file.type,
      size: file.size,
      uploadedBy: metadata.uploadedBy.trim() || null,
      message: metadata.message.trim() || null,
    });
  }

  async function confirmUpload() {
    if (pendingFiles.length === 0 || uploading) {
      return;
    }

    setUploading(true);
    setDone(false);

    try {
      for (const file of pendingFiles) {
        await uploadOne(file, uploadInfo);
      }

      setDone(true);
      setShowUploadModal(false);
      setPendingFiles([]);
      setUploadInfo({
        uploadedBy: "",
        message: "",
      });
    } catch (error) {
      console.error("Errore durante l'upload", error);
      alert("Si è verificato un errore durante il caricamento.");
    } finally {
      setUploading(false);
    }
  }

  function cancelUpload() {
    if (uploading) {
      return;
    }

    setShowUploadModal(false);
    setPendingFiles([]);
    setUploadInfo({
      uploadedBy: "",
      message: "",
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
        style={{ display: "none" }}
        onChange={(e) => {
          const selectedFiles = Array.from(e.target.files || []);

          if (selectedFiles.length === 0) {
            return;
          }

          setPendingFiles(selectedFiles);
          setUploadInfo({
            uploadedBy: "",
            message: "",
          });
          setShowUploadModal(true);

          e.target.value = "";
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

    {showUploadModal && (
      <div
        className="upload-modal-overlay"
        onClick={cancelUpload}
      >
        <div
          className="upload-modal"
          onClick={(e) => e.stopPropagation()}
        >
          <button
            type="button"
            className="upload-modal-close"
            onClick={cancelUpload}
            disabled={uploading}
            aria-label="Chiudi"
          >
            ×
          </button>

          <p className="muted">Prima di condividere</p>
          <h2>
            {pendingFiles.length === 1
              ? "Se vuoi, facci sapere..."
              : `Hai selezionato ${pendingFiles.length} contenuti`}
          </h2>

          <div className="selected-files-summary">
            {pendingFiles.slice(0, 3).map((file) => (
              <div className="selected-file-row" key={`${file.name}-${file.size}`}>
                <span>{file.name}</span>
                <small>
                  {(file.size / 1024 / 1024).toFixed(1)} MB
                </small>
              </div>
            ))}

            {pendingFiles.length > 3 && (
              <p className="muted">
                e altri {pendingFiles.length - 3} contenuti
              </p>
            )}
          </div>

          <label htmlFor="uploadedBy">Chi sta caricando?</label>
          <input
            id="uploadedBy"
            type="text"
            maxLength={100}
            value={uploadInfo.uploadedBy}
            onChange={(e) =>
              setUploadInfo((current) => ({
                ...current,
                uploadedBy: e.target.value,
              }))
            }
            placeholder="Il tuo nome"
            disabled={uploading}
          />

          <label htmlFor="uploadMessage">Messaggio o nota</label>
          <textarea
            id="uploadMessage"
            maxLength={1000}
            value={uploadInfo.message}
            onChange={(e) =>
              setUploadInfo((current) => ({
                ...current,
                message: e.target.value,
              }))
            }
            placeholder="Scrivi qualcosa su questo momento"
            disabled={uploading}
          />

          <div className="upload-modal-actions">
            <button
              type="button"
              className="btn secondary"
              onClick={cancelUpload}
              disabled={uploading}
            >
              Annulla
            </button>

            <button
              type="button"
              onClick={confirmUpload}
              disabled={uploading}
            >
              {uploading ? "Caricamento..." : "Condividi"}
            </button>
          </div>
        </div>
      </div>
    )}
  </main>;
}
