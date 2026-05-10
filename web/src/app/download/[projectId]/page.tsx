'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { getProject } from '@/lib/api';
import { Project } from '@/lib/types';
import { Nav } from '@/components/Nav';
import { PageHeader } from '@/components/PageHeader';
import { Eyebrow } from '@/components/Eyebrow';

export default function DownloadPage() {
  const params = useParams();
  const projectId = params.projectId as string;
  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getProject(projectId)
      .then((p) => setProject(p))
      .catch(() => setError('Failed to load project.'))
      .finally(() => setLoading(false));
  }, [projectId]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--background)]">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[var(--primary)]"></div>
      </div>
    );
  }

  if (error || !project) {
    return (
      <main className="min-h-screen">
        <Nav variant="solid" />
        <PageHeader
          eyebrow="Not Found"
          title={error || 'This memory could not be found.'}
          backHref="/"
        />
      </main>
    );
  }

  const photoUrl = project.hdPhotoUrl || project.generatedPhotoUrl;
  const fileName = `memora-${project.title.replace(/\s+/g, '-').toLowerCase()}.jpg`;

  return (
    <main className="min-h-screen">
      <Nav variant="solid" />

      <PageHeader
        eyebrow="The Keepsake"
        title={
          <>
            {project.title}
            <br />
            <em className="italic font-normal text-[var(--hero-accent)]">is ready.</em>
          </>
        }
        description="Your AI-generated family memory photo is ready to download. For private family use only."
        backHref="/"
        backLabel="Back to projects"
      />

      <section className="max-w-3xl mx-auto px-6 sm:px-10 py-14">
        {photoUrl ? (
          <div className="space-y-8">
            <div className="relative w-full aspect-[3/4] rounded-3xl overflow-hidden border border-[var(--border)] shadow-xl bg-[var(--hero-bg)]">
              <Image
                src={photoUrl}
                alt="Generated memory photo"
                fill
                className="object-cover"
                unoptimized
              />
              <div className="absolute top-4 left-4">
                <span className="rounded-full bg-black/45 backdrop-blur px-3 py-1 text-[0.65rem] tracking-widest uppercase text-white">
                  AI-Generated · HD
                </span>
              </div>
            </div>

            <div className="text-center space-y-4">
              <Eyebrow>Saved for keeps</Eyebrow>
              <p className="font-serif text-xl text-[var(--foreground)] max-w-md mx-auto leading-relaxed">
                A quiet moment, finally completed.
              </p>
              <p className="text-xs text-[var(--muted)] max-w-md mx-auto">
                AI-generated family memory photo — for private family use only. Please do not present
                it as an unaltered original.
              </p>
            </div>

            <div className="flex flex-col sm:flex-row gap-3 justify-center pt-2">
              <a
                href={photoUrl}
                download={fileName}
                className="inline-flex items-center justify-center gap-2 rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition"
              >
                <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 3v14" />
                  <path d="M5 10l7 7 7-7" />
                  <path d="M5 21h14" />
                </svg>
                Download HD photo
              </a>
              <Link
                href="/"
                className="inline-flex items-center justify-center rounded-full border border-[var(--border)] px-6 py-3 text-sm text-[var(--muted)] hover:text-[var(--foreground)] hover:border-[var(--primary)]/40 transition"
              >
                Back to projects
              </Link>
            </div>
          </div>
        ) : (
          <div className="rounded-3xl border border-dashed border-[var(--border)] bg-[var(--card)] p-12 text-center space-y-4">
            <Eyebrow>Still composing</Eyebrow>
            <p className="font-serif text-2xl text-[var(--foreground)]">
              Your photo is still being prepared.
            </p>
            <p className="text-sm text-[var(--muted)] max-w-md mx-auto">
              We&apos;ll finish composing your memory in a moment. You can check progress on the preview screen.
            </p>
            <div className="pt-2">
              <Link
                href={`/preview/${projectId}`}
                className="inline-flex items-center justify-center gap-2 rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition"
              >
                Check progress
                <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M5 12h14" />
                  <path d="M12 5l7 7-7 7" />
                </svg>
              </Link>
            </div>
          </div>
        )}
      </section>
    </main>
  );
}
