import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client.js';

export default function AdminEventDetailPage() {
  const { id } = useParams();
  const [events, setEvents] = useState([]);
  const [media, setMedia] = useState([]);
  const [selectedMediaIds, setSelectedMediaIds] = useState([]);
  const [selectedMedia, setSelectedMedia] = useState(null);
  const [showDetails, setShowDetails] = useState(false);
  const [detailMedia, setDetailMedia] = useState(null);


  async function load() {
    const eventsRes = await api.get('/api/admin/events');
    setEvents(eventsRes.data);
    const mediaRes = await api.get(`/api/admin/events/${id}/media`);
    setMedia(mediaRes.data);
  }

  function toggleMedia(id) {
    setSelectedMediaIds(prev =>
      prev.includes(id)
        ? prev.filter(itemId => itemId !== id)
        : [...prev, id]
    );
  }

  function downloadSelected() {
    if (selectedMediaIds.length === 0) {
      alert("Seleziona almeno un elemento");
      return;
    }

    const confirmed = window.confirm(
      `Vuoi scaricare ${selectedMediaIds.length} ${selectedMediaIds.length === 1 ? "elemento" : "elementi"
      }?`
    );

    if (!confirmed) {
      return;
    }

    const ids = selectedMediaIds.join(",");

    window.location.href =
      `${api.defaults.baseURL}/api/admin/events/media/download-selected?ids=${ids}`;
  }

  function downloadAll() {
    const confirmed = window.confirm(`Vuoi scaricare tutti i file della galleria?`);

    if (!confirmed) {
      return;
    }

    window.location.href = `${api.defaults.baseURL}/api/admin/events/media/download-all`;
  }

  useEffect(() => { load(); }, [id]);
  const event = events.find(e => e.id === id);
  const publicUrl = event ? `${window.location.origin}/e/${event.slug}` : '';

  return <main className="container">
    <Link to="/admin/events" className="muted">← Torna agli eventi</Link>
    <div className="card" style={{ marginTop: 18, marginBottom: 24 }}>
      <h1>{event?.name || 'Evento'}</h1>
      {event && <>
        <p className="muted">Link ospiti: <a href={publicUrl}>{publicUrl}</a></p>
        <img alt="QR Code" src={`${api.defaults.baseURL}/api/admin/events/${id}/qr`} width="220" height="220" />
      </>}
    </div>

    <section className="card">
      <h2>Media caricati</h2>
      {showDetails && detailMedia && (
        <div
          className="modal-overlay"
          onClick={() => {
            setShowDetails(false);
            setDetailMedia(null);
          }}
        >
          <div
            className="details-modal"
            onClick={e => e.stopPropagation()}
          >
            <button
              type="button"
              className="close-button"
              onClick={() => {
                setShowDetails(false);
                setDetailMedia(null);
              }}
            >
              ×
            </button>

            <h2>Dettagli</h2>

            <div className="detail-row">
              <strong>Caricato da</strong>
              <span>{detailMedia.uploadedBy || "Anonimo"}</span>
            </div>

            <div className="detail-row">
              <strong>Messaggio</strong>
              <p>{detailMedia.message || "Nessun messaggio"}</p>
            </div>

            <div className="detail-row">
              <strong>Caricato il</strong>
              <span>
                {detailMedia.uploadedAt
                  ? new Date(detailMedia.uploadedAt).toLocaleString("it-IT")
                  : "Data non disponibile"}
              </span>
            </div>
          </div>
        </div>
      )}
      <div className="media-grid">
        {media.map(item => (
          <div
            className={`media-card ${selectedMediaIds.includes(item.id) ? "selected" : ""
              }`}
            key={item.id}
          >
            <label className="media-select">
              <input
                type="checkbox"
                checked={selectedMediaIds.includes(item.id)}
                onChange={() => toggleMedia(item.id)}
              />
              <span className="checkmark">✓</span>
            </label>

            {item.contentType?.startsWith("image/") && (
              <a
                href={item.publicUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="media-link"
              >
                <img
                  src={item.publicUrl}
                  alt={item.originalFilename}
                  className="media-preview"
                />
              </a>
            )}

            {item.contentType?.startsWith("video/") && (
              <a
                href={item.publicUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="media-link"
              >
                <video
                  src={item.publicUrl}
                  className="media-preview video"
                  preload="metadata"
                />
              </a>
            )}

            <button
              type="button"
              className="details-link"
              onClick={() => {
                setDetailMedia(item);
                setShowDetails(true);
              }}
            >
              @ Dettagli
            </button>
          </div>
        ))}
      </div>
      {media.length === 0 && <p className="muted">Ancora nessun contenuto caricato.</p>}
      <button className="btn secondary" onClick={downloadSelected}>
        Download elementi selezionati
      </button>
      <button className="btn secondary" onClick={downloadAll}>
        Download di tutta la galleria
      </button>
    </section>
  </main>;
}
