import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';

export default function AdminEventsPage() {
  const [events, setEvents] = useState([]);
  const [form, setForm] = useState({ name: '', description: '', eventDate: '' });
  const [loading, setLoading] = useState(false);

const handleDelete = async (id) => {
  if (!window.confirm("Sei sicuro di voler eliminare questo evento?")) {
    return;
  }

  try {
    await api.delete(`/api/admin/events/${id}`);

    setEvents(prev => prev.filter(event => event.id !== id));

    alert("Evento eliminato con successo");
  } catch (error) {
    console.error(error);
    alert("Errore durante l'eliminazione");
  }
};

  async function loadEvents() {
    const res = await api.get('/api/admin/events');
    setEvents(res.data);
  }

  useEffect(() => { loadEvents(); }, []);

  async function createEvent(e) {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/api/admin/events', form);
      setForm({ name: '', description: '', eventDate: '' });
      await loadEvents();
    } finally {
      setLoading(false);
    }
  }

  return <main className="container">
    <div className="header">
      <div>
        <p className="muted">Admin</p>
        <h1>Shared Memories</h1>
      </div>
    </div>

    <section className="card" style={{ marginBottom: 24 }}>
      <h2>Crea evento</h2>
      <form onSubmit={createEvent}>
        <label>Nome evento</label>
        <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Nome evento" required />
        <label>Descrizione</label>
        <textarea value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Condividi con noi i tuoi ricordi" />
        <label>Data evento</label>
        <input type="date" value={form.eventDate} onChange={e => setForm({ ...form, eventDate: e.target.value })} />
        <div style={{ marginTop: 16 }}><button disabled={loading}>{loading ? 'Creazione...' : 'Crea evento'}</button></div>
      </form>
    </section>

    <section className="card">
      <h2>Eventi</h2>
      {events.map(event => <div className="event-row" key={event.id}>
        <div>
          <strong>{event.name}</strong>

        </div>
        <Link className="btn secondary" to={`/admin/events/${event.id}`}>Apri</Link>
        <button className="btn danger" onClick={() => handleDelete(event.id)}>
          Elimina
        </button>
      </div>)}
      {events.length === 0 && <p className="muted">Nessun evento creato.</p>}
    </section>
  </main>;
}
