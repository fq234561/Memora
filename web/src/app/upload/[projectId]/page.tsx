'use client';

import { useState, useCallback } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Image from 'next/image';
import { uploadPhoto } from '@/lib/api';
import { Nav } from '@/components/Nav';
import { PageHeader } from '@/components/PageHeader';
import { Eyebrow } from '@/components/Eyebrow';

type Slot = 'base' | 'person';

interface ApiErrorShape {
  response?: { data?: { error?: string } };
}

function extractApiError(err: unknown): string | null {
  if (err && typeof err === 'object' && 'response' in err) {
    const e = err as ApiErrorShape;
    if (e.response?.data?.error) return e.response.data.error;
  }
  return null;
}

interface UploadSlotProps {
  label: string;
  hint: string;
  preview: string;
  onFile: (file: File) => void;
  iconPath: React.ReactNode;
}

function UploadSlot({ label, hint, preview, onFile, iconPath }: UploadSlotProps) {
  const inputId = `upload-input-${label.replace(/\s+/g, '-').toLowerCase()}`;

  return (
    <div
      className="group relative rounded-2xl border border-dashed border-[var(--border)] bg-[var(--card)] p-3 transition hover:border-[var(--primary)]/40 cursor-pointer overflow-hidden"
      onDragOver={(e) => e.preventDefault()}
      onDrop={(e) => {
        e.preventDefault();
        const file = e.dataTransfer.files[0];
        if (file) onFile(file);
      }}
      onClick={() => document.getElementById(inputId)?.click()}
    >
      <input
        id={inputId}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        className="hidden"
        onChange={(e) => e.target.files?.[0] && onFile(e.target.files[0])}
      />
      {preview ? (
        <div className="relative w-full aspect-[4/3] rounded-xl overflow-hidden">
          <Image src={preview} alt={`${label} preview`} fill className="object-cover" unoptimized />
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/0 to-black/0" />
          <div className="absolute bottom-3 left-3 right-3 flex items-center justify-between">
            <span className="text-[0.7rem] tracking-widest uppercase text-white/85">{label}</span>
            <span className="rounded-full bg-white/15 backdrop-blur px-3 py-1 text-[0.65rem] tracking-wider uppercase text-white">
              Replace
            </span>
          </div>
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center text-center aspect-[4/3] px-6 space-y-3">
          <div className="w-12 h-12 rounded-full bg-[var(--hero-bg)]/5 border border-[var(--border)] flex items-center justify-center text-[var(--accent)]">
            <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              {iconPath}
            </svg>
          </div>
          <div>
            <div className="font-serif text-lg text-[var(--foreground)]">{label}</div>
            <div className="text-xs text-[var(--muted)] mt-1 leading-relaxed">{hint}</div>
          </div>
          <div className="text-[0.65rem] tracking-widest uppercase text-[var(--muted)]/80">
            Drag & drop · or click to choose
          </div>
        </div>
      )}
    </div>
  );
}

export default function UploadPhotosPage() {
  const router = useRouter();
  const params = useParams();
  const projectId = params.projectId as string;

  const [baseFile, setBaseFile] = useState<File | null>(null);
  const [personFile, setPersonFile] = useState<File | null>(null);
  const [basePreview, setBasePreview] = useState<string>('');
  const [personPreview, setPersonPreview] = useState<string>('');
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  const validateFile = (file: File): string | null => {
    const allowed = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowed.includes(file.type)) return 'Only JPG, PNG, and WebP are allowed.';
    if (file.size > 20 * 1024 * 1024) return 'File must be under 20MB.';
    return null;
  };

  const handleFile = useCallback((file: File, type: Slot) => {
    const err = validateFile(file);
    if (err) {
      setError(err);
      return;
    }
    setError('');
    const url = URL.createObjectURL(file);
    if (type === 'base') {
      setBaseFile(file);
      setBasePreview(url);
    } else {
      setPersonFile(file);
      setPersonPreview(url);
    }
  }, []);

  const handleContinue = async () => {
    if (!baseFile || !personFile) {
      setError('Please upload both photos.');
      return;
    }
    setUploading(true);
    setError('');
    try {
      await uploadPhoto(projectId, baseFile, 'base');
      await uploadPhoto(projectId, personFile, 'person');
      router.push(`/consent/${projectId}`);
    } catch (e: unknown) {
      setError(extractApiError(e) || 'Upload failed. Please try again.');
      setUploading(false);
    }
  };

  return (
    <main className="min-h-screen">
      <Nav variant="solid" />

      <PageHeader
        eyebrow="Step 02 · Upload Photos"
        title={
          <>
            The two photos that
            <br />
            <em className="italic font-normal text-[var(--hero-accent)]">become a memory.</em>
          </>
        }
        description="Upload the event scene and a clear reference photo of the loved one. We'll handle the rest."
        backHref="/"
        backLabel="Back to projects"
      />

      <section className="max-w-3xl mx-auto px-6 sm:px-10 py-14">
        {error && (
          <div className="mb-8 rounded-2xl border border-red-200 bg-red-50 px-5 py-4 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <div className="space-y-3">
            <Eyebrow>The Scene</Eyebrow>
            <p className="text-sm text-[var(--muted)] leading-relaxed">
              The original event photo — birthday, vacation, gathering — where the loved one should appear.
            </p>
            <UploadSlot
              label="Event photo"
              hint="JPG, PNG or WebP — up to 20MB"
              preview={basePreview}
              onFile={(f) => handleFile(f, 'base')}
              iconPath={
                <>
                  <rect x="3" y="5" width="18" height="14" rx="2" />
                  <circle cx="9" cy="11" r="2" />
                  <path d="M21 17l-5-5-9 9" />
                </>
              }
            />
          </div>

          <div className="space-y-3">
            <Eyebrow>The Person</Eyebrow>
            <p className="text-sm text-[var(--muted)] leading-relaxed">
              A clear, front-facing photo of the loved one we should bring into the scene.
            </p>
            <UploadSlot
              label="Reference photo"
              hint="A clear face shot works best"
              preview={personPreview}
              onFile={(f) => handleFile(f, 'person')}
              iconPath={
                <>
                  <circle cx="12" cy="8" r="4" />
                  <path d="M4 21v-1a8 8 0 0116 0v1" />
                </>
              }
            />
          </div>
        </div>

        <div className="mt-12 pt-8 border-t border-[var(--border)] flex flex-col-reverse sm:flex-row sm:items-center sm:justify-between gap-4">
          <p className="text-xs text-[var(--muted)]">
            Photos are private to your account and used only to compose this memory.
          </p>
          <button
            onClick={handleContinue}
            disabled={uploading || !baseFile || !personFile}
            className="inline-flex items-center justify-center gap-2 rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {uploading ? 'Uploading…' : 'Continue to consent'}
            {!uploading && (
              <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M5 12h14" />
                <path d="M12 5l7 7-7 7" />
              </svg>
            )}
          </button>
        </div>
      </section>
    </main>
  );
}
