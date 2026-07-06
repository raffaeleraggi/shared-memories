import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client.js';

export default function PublicGalleryPage() {
  const { slug } = useParams();
  const [media, setMedia] = useState([]);

  useEffect(() => {
    api.get(`/api/public/events/${slug}/gallery`).then(res => setMedia(res.data));
  }, [slug]);

  return <main className="container">
    <Link to={`/e/${slug}`} className="muted">← Carica altri ricordi</Link>
    <section className="card" style={{ marginTop: 18 }}>
      <h1>Galleria</h1>
      <div className="grid">
        {media.map(item => <div className="media-card" key={item.id}>
          {item.contentType?.startsWith('video/')
            ? <video src={item.publicUrl} controls />
            : <img className="preview" src={item.publicUrl} alt={item.originalFilename} />}
        </div>)}
      </div>
      {media.length === 0 && <p className="muted">Ancora nessun contenuto.</p>}
    </section>
  </main>;
}
