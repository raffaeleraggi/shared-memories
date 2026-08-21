import { useEffect, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api/client.js";

export default function GuestUploadPage() {
  const { slug, sourceToken } = useParams();

  const [event, setEvent] = useState(null);
  const [uploadSource, setUploadSource] = useState(null);
  const [progress, setProgress] = useState({});
  const [done, setDone] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");

  const fileInputRef = useRef(null);

  const MULTIPART_THRESHOLD =
  100 * 1024 * 1024;

  const PART_SIZE =
  25 * 1024 * 1024;

  useEffect(() => {
    api
      .get(`/api/public/events/${slug}`)
      .then((response) => setEvent(response.data))
      .catch((err) => {
        console.error("Errore caricamento evento", err);
        setError("Evento non trovato.");
      });
  }, [slug]);

  useEffect(() => {
    if (!sourceToken) {
      setError("QR code non valido: manca il token del tavolo.");
      return;
    }

    api
      .get(`/api/public/upload-sources/${sourceToken}`)
      .then((response) => {
        setUploadSource(response.data);

        if (
          response.data.eventSlug &&
          response.data.eventSlug !== slug
        ) {
          setError("Il QR code non appartiene a questo evento.");
        }
      })
      .catch((err) => {
        console.error("Errore caricamento sorgente QR", err);
        setError("QR code non valido o non più attivo.");
      });
  }, [slug, sourceToken]);

  async function uploadOne(file) {
  if (file.size >= MULTIPART_THRESHOLD) {
    return uploadMultipart(file);
  }

  return uploadSingle(file);
}

  async function uploadSingle(file) {
    /*
     * Prima chiediamo al backend la URL di upload.
     * Questo endpoint non deve essere /media/complete.
     */
    const signedResponse = await api.post(
      `/api/public/events/${slug}/upload-url`,
      {
        filename: file.name,
        contentType: file.type || "application/octet-stream",
        size: file.size,
      }
    );

    const {
      uploadUrl,
      storageKey,
      method,
    } = signedResponse.data;

    if (!uploadUrl || !storageKey) {
      throw new Error("Risposta upload non valida");
    }

    if (method === "POST") {
      const formData = new FormData();
      formData.append("file", file);

      /*
       * Se uploadUrl è un path locale, api.post va bene.
       * Se è una URL completa R2/S3, Axios la usa comunque come URL assoluta.
       */
      await api.post(uploadUrl, formData, {
        onUploadProgress: (event) => {
          const percentage = event.total
            ? Math.round((event.loaded * 100) / event.total)
            : 0;

          setProgress((current) => ({
            ...current,
            [file.name]: percentage,
          }));
        },
      });
    } else {
      await api.put(uploadUrl, file, {
        headers: {
          "Content-Type":
            file.type || "application/octet-stream",
        },
        onUploadProgress: (event) => {
          const percentage = event.total
            ? Math.round((event.loaded * 100) / event.total)
            : 0;

          setProgress((current) => ({
            ...current,
            [file.name]: percentage,
          }));
        },
      });
    }

    /*
     * Solo dopo l'upload effettivo registriamo il media nel DB.
     * uploadedBy viene ricavato dal backend usando sourceToken.
     */
    await api.post(
      `/api/public/events/${slug}/media/complete`,
      {
        storageKey,
        filename: file.name,
        contentType:
          file.type || "application/octet-stream",
        size: file.size,
        sourceToken,
      }
    );
  }

  async function uploadMultipart(file) {

  const startResponse = await api.post(
    `/api/public/events/${slug}/multipart/start`,
    {
      filename: file.name,
      contentType:
        file.type || "application/octet-stream",
      size: file.size,
    }
  );

  const {
    storageKey,
    uploadId,
  } = startResponse.data;

  const numberOfParts =
    Math.ceil(file.size / PART_SIZE);

  const completedParts = [];

  try {

    for (
      let partNumber = 1;
      partNumber <= numberOfParts;
      partNumber++
    ) {

      const start =
        (partNumber - 1) * PART_SIZE;

      const end =
        Math.min(
          start + PART_SIZE,
          file.size
        );

      const blob =
        file.slice(start, end);

      const signedResponse =
        await api.post(
          `/api/public/events/${slug}/multipart/part-url`,
          {
            storageKey,
            uploadId,
            partNumber,
          }
        );

      const uploadUrl =
        signedResponse.data.uploadUrl;

      const response = await api.put(
        uploadUrl,
        blob,
        {
          headers: {
            "Content-Type":
              "application/octet-stream",
          },

          onUploadProgress: (event) => {

            if (!event.total) {
              return;
            }

            const currentPartProgress =
              event.loaded / event.total;

            const totalUploaded =
              start +
              blob.size *
                currentPartProgress;

            const percentage =
              Math.round(
                totalUploaded /
                  file.size *
                  100
              );

            setProgress(current => ({
              ...current,
              [file.name]: percentage,
            }));
          },
        }
      );

      const eTag =
        response.headers.etag;

      if (!eTag) {
        throw new Error(
          `ETag mancante per la parte ${partNumber}`
        );
      }

      completedParts.push({
        partNumber,
        eTag,
      });
    }

    await api.post(
      `/api/public/events/${slug}/multipart/complete`,
      {
        storageKey,
        uploadId,

        parts: completedParts,

        filename: file.name,

        contentType:
          file.type ||
          "application/octet-stream",

        size: file.size,

        sourceToken,
      }
    );

  } catch (error) {

    console.error(
      "Multipart upload fallito",
      error
    );

    try {
      await api.post(
        `/api/public/events/${slug}/multipart/abort`,
        {
          storageKey,
          uploadId,
        }
      );
    } catch (abortError) {
      console.error(
        "Errore abort multipart",
        abortError
      );
    }

    throw error;
  }
}

  async function uploadAll(selectedFiles) {
    if (
      selectedFiles.length === 0 ||
      uploading ||
      error
    ) {
      return;
    }

    setUploading(true);
    setDone(false);
    setError("");
    setProgress({});

    try {
      /*
       * Upload sequenziale: più semplice e stabile da smartphone.
       * Successivamente puoi introdurre un parallelismo limitato.
       */
      for (const file of selectedFiles) {
        await uploadOne(file);
      }

      setDone(true);
    } catch (err) {
      console.error("Errore durante l'upload", err);

      const message =
        err.response?.data?.message ||
        "Si è verificato un errore durante il caricamento.";

      setError(message);
    } finally {
      setUploading(false);
    }
  }

  function handleFileSelection(event) {
    const selectedFiles = Array.from(
      event.target.files || []
    );

    event.target.value = "";

    if (selectedFiles.length === 0) {
      return;
    }

    uploadAll(selectedFiles);
  }

  return (
    <main className="container">
      <section className="card">
        <p className="muted">Shared Memories</p>

        <h1>{event?.name || "Evento"}</h1>

        {uploadSource && (
          <div className="table-badge">
            {uploadSource.label}
          </div>
        )}

        <p>
          {event?.description ||
            "Carica qui foto e video della giornata."}
        </p>

        {error && (
          <div className="upload-error">
            {error}
          </div>
        )}

        <input
          ref={fileInputRef}
          type="file"
          multiple
          accept="image/*,video/*"
          hidden
          onChange={handleFileSelection}
          disabled={
            uploading ||
            !uploadSource ||
            Boolean(error)
          }
        />

        <div
          className={`upload-dropzone ${uploading ? "uploading disabled" : ""
            }`}
          onClick={() => {
            if (
              uploading ||
              !uploadSource ||
              Boolean(error)
            ) {
              return;
            }

            fileInputRef.current?.click();
          }}
          role="button"
          tabIndex={0}
          onKeyDown={(event) => {
            if (
              event.key === "Enter" ||
              event.key === " "
            ) {
              event.preventDefault();

              if (
                !uploading &&
                uploadSource &&
                !error
              ) {
                fileInputRef.current?.click();
              }
            }
          }}
          aria-disabled={
            uploading ||
            !uploadSource ||
            Boolean(error)
          }
        >
          <div className="upload-plus">
            {uploading ? "…" : "+"}
          </div>

          <h2 className="upload-title">
            {uploading
              ? "Caricamento in corso"
              : "Aggiungi foto o video"}
          </h2>

          <p className="upload-description">
            {uploading
              ? "Non chiudere questa pagina"
              : "Tocca qui per condividere i tuoi ricordi"}
          </p>
        </div>

        {Object.entries(progress).map(
          ([filename, percentage]) => (
            <div
              className="upload-progress-row"
              key={filename}
            >
              <div className="upload-progress-header">
                <span>{filename}</span>
                <span>{percentage}%</span>
              </div>

              <div className="upload-progress-track">
                <div
                  className="upload-progress-value"
                  style={{
                    width: `${percentage}%`,
                  }}
                />
              </div>
            </div>
          )
        )}

        {done && (
          <h2 className="upload-successfull">
            Grazie per aver condiviso con noi il tuo
            ricordo!
          </h2>
        )}

        <p style={{ marginTop: 20 }}>
          <Link to={`/e/${slug}/tavolo/${sourceToken}/gallery`}>
            Guarda la galleria
          </Link>
        </p>
      </section>
    </main >
  );
}