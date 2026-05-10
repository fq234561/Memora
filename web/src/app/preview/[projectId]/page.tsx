'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Image from 'next/image';
import {
  getProject,
  getProjectStatus,
  generatePreview,
  selectCandidate,
  createStripeCheckout,
} from '@/lib/api';
import { Project, ProjectStatus } from '@/lib/types';
import { Nav } from '@/components/Nav';
import { PageHeader } from '@/components/PageHeader';
import { Eyebrow } from '@/components/Eyebrow';

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

export default function PreviewPage() {
  const router = useRouter();
  const params = useParams();
  const projectId = params.projectId as string;

  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [progress, setProgress] = useState(0);
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [error, setError] = useState('');
  const [regenNote, setRegenNote] = useState('');
  const [showRegen, setShowRegen] = useState(false);

  const load = useCallback(async () => {
    try {
      const p = await getProject(projectId);
      setProject(p);
      if (p.selectedCandidateIndex !== undefined) {
        setSelectedIndex(p.selectedCandidateIndex);
      }
    } catch {
      setError('Failed to load project.');
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!project || project.status !== ProjectStatus.GENERATING) return;
    setGenerating(true);
    const interval = setInterval(async () => {
      try {
        const status = await getProjectStatus(projectId);
        setProgress(status.progress || 0);
        if (
          status.status === ProjectStatus.PREVIEW_READY ||
          status.status === ProjectStatus.COMPLETED
        ) {
          clearInterval(interval);
          setGenerating(false);
          const p = await getProject(projectId);
          setProject(p);
        } else if (status.status === ProjectStatus.FAILED) {
          clearInterval(interval);
          setGenerating(false);
          setError('Generation failed. Please try again.');
        }
      } catch {
        clearInterval(interval);
        setGenerating(false);
      }
    }, 3000);
    return () => clearInterval(interval);
  }, [project, projectId]);

  const handleGenerate = async (isRegeneration = false) => {
    setError('');
    setGenerating(true);
    setProgress(10);
    try {
      await generatePreview(projectId, undefined, isRegeneration, regenNote || undefined);
      setRegenNote('');
      setShowRegen(false);
      await load(); // Refresh project state to trigger polling
    } catch (e: unknown) {
      setError(extractApiError(e) || 'Generation failed.');
      setGenerating(false);
    }
  };

  const handleSelect = async (index: number) => {
    setSelectedIndex(index);
    try {
      await selectCandidate(projectId, index);
      const p = await getProject(projectId);
      setProject(p);
    } catch (e: unknown) {
      setError(extractApiError(e) || 'Selection failed.');
    }
  };

  const handlePurchase = async (productId: string) => {
    try {
      const data = await createStripeCheckout(projectId, productId);
      window.location.href = data.url;
    } catch (e: unknown) {
      setError(extractApiError(e) || 'Checkout failed.');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--background)]">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[var(--primary)]"></div>
      </div>
    );
  }

  if (!project) {
    return (
      <main className="min-h-screen">
        <Nav variant="solid" />
        <PageHeader
          eyebrow="Not Found"
          title="This memory could not be found."
          description="It may have been deleted or never existed."
          backHref="/"
        />
      </main>
    );
  }

  const needsPurchase = !project.purchasedProductId;
  const canSelect =
    project.status === ProjectStatus.PREVIEW_READY && !!project.candidateUrls?.length;
  const isPaid =
    project.purchasedProductId === 'full_pack' || project.purchasedProductId === 'hd_unlock';

  const styleLabel = project.style.replace(/_/g, ' ').toLowerCase();

  return (
    <main className="min-h-screen">
      <Nav variant="solid" />

      <PageHeader
        eyebrow={`Step 04 · ${styleLabel}`}
        title={
          <>
            {project.title}
            <br />
            <em className="italic font-normal text-[var(--hero-accent)]">in preview.</em>
          </>
        }
        description="Review four candidates, choose the one that feels right, and unlock the HD keepsake."
        backHref="/"
        backLabel="Back to projects"
      />

      <section className="max-w-4xl mx-auto px-6 sm:px-10 py-14 space-y-10">
        {error && (
          <div className="rounded-2xl border border-red-200 bg-red-50 px-5 py-4 text-sm text-red-700">
            {error}
          </div>
        )}

        {/* Purchase gate */}
        {needsPurchase && (
          <div className="rounded-3xl border border-[var(--border)] bg-[var(--card)] p-8 sm:p-10 text-center space-y-5">
            <Eyebrow>Unlock previews</Eyebrow>
            <h2 className="font-serif text-2xl sm:text-3xl text-[var(--foreground)]">
              Generate four AI candidates.
            </h2>
            <p className="text-sm text-[var(--muted)] max-w-md mx-auto">
              Purchase a pack to compose your private memory. You&apos;ll preview each candidate before unlocking HD.
            </p>
            <div className="flex flex-col sm:flex-row gap-3 justify-center pt-2">
              <button
                onClick={() => handlePurchase('preview_pack')}
                className="rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition"
              >
                Preview Pack — $2.99
              </button>
              <button
                onClick={() => handlePurchase('full_pack')}
                className="rounded-full border border-[var(--primary)] px-6 py-3 text-sm font-medium text-[var(--primary)] hover:bg-[var(--primary)]/5 transition"
              >
                Full Pack — $14.99
              </button>
            </div>
          </div>
        )}

        {/* Begin generation CTA */}
        {!needsPurchase && project.status === ProjectStatus.UPLOADED && (
          <div className="rounded-3xl border border-[var(--border)] bg-[var(--card)] p-8 sm:p-10 text-center space-y-5">
            <Eyebrow>Ready to begin</Eyebrow>
            <h2 className="font-serif text-2xl sm:text-3xl text-[var(--foreground)]">
              Compose your memory photo.
            </h2>
            <p className="text-sm text-[var(--muted)] max-w-md mx-auto">
              We&apos;ll prepare four candidates. This usually takes a minute or two.
            </p>
            <button
              onClick={() => handleGenerate(false)}
              disabled={generating}
              className="inline-flex items-center justify-center gap-2 rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition disabled:opacity-50"
            >
              {generating ? 'Starting…' : 'Generate preview'}
            </button>
          </div>
        )}

        {/* Generating progress */}
        {generating && (
          <div className="rounded-3xl border border-[var(--border)] bg-[var(--card)] p-8 sm:p-10 text-center space-y-4">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[var(--primary)] mx-auto"></div>
            <p className="font-serif text-xl text-[var(--foreground)]">
              Composing your memory photo…
            </p>
            <div className="max-w-md mx-auto">
              <div className="w-full h-1.5 rounded-full bg-[var(--border)] overflow-hidden">
                <div
                  className="h-full bg-[var(--primary)] transition-all duration-500"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <p className="text-[0.7rem] tracking-widest uppercase text-[var(--muted)] mt-2">
                {progress}%
              </p>
            </div>
          </div>
        )}

        {/* Candidate selection */}
        {canSelect && project.candidateUrls && (
          <div className="space-y-6">
            <div className="flex items-end justify-between gap-4 flex-wrap">
              <div className="space-y-2">
                <Eyebrow>Choose one</Eyebrow>
                <h2 className="font-serif text-2xl sm:text-3xl text-[var(--foreground)]">
                  Which one feels right?
                </h2>
              </div>
              {project.regenerationCount < project.regenerationLimit && (
                <button
                  onClick={() => setShowRegen(!showRegen)}
                  className="text-xs tracking-widest uppercase text-[var(--primary)] hover:text-[var(--primary-dark)] transition"
                >
                  {showRegen ? 'Cancel regenerate' : 'Regenerate'}
                </button>
              )}
            </div>

            {showRegen && (
              <div className="rounded-2xl border border-[var(--border)] bg-[var(--card)] p-5 space-y-3">
                <p className="text-xs text-[var(--muted)]">
                  Add an optional note to nudge the next pass — lighting, expression, framing, etc.
                </p>
                <input
                  type="text"
                  value={regenNote}
                  onChange={(e) => setRegenNote(e.target.value)}
                  placeholder="e.g., softer lighting, more candid"
                  className="w-full rounded-xl border border-[var(--border)] bg-[var(--background)] px-4 py-2.5 text-sm text-[var(--foreground)] focus:outline-none focus:ring-2 focus:ring-[var(--primary)]/30 focus:border-[var(--primary)]/40 transition"
                />
                <div className="flex gap-3 pt-1">
                  <button
                    onClick={() => handleGenerate(true)}
                    disabled={generating}
                    className="rounded-full bg-[var(--primary)] px-5 py-2 text-xs tracking-wide font-medium text-white hover:bg-[var(--primary-dark)] transition disabled:opacity-50"
                  >
                    Regenerate
                  </button>
                  <button
                    onClick={() => setShowRegen(false)}
                    className="rounded-full border border-[var(--border)] px-5 py-2 text-xs tracking-wide text-[var(--muted)] hover:text-[var(--foreground)] transition"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {project.candidateUrls.map((url, i) => {
                const active = selectedIndex === i;
                return (
                  <button
                    key={i}
                    onClick={() => handleSelect(i)}
                    className={`relative rounded-2xl overflow-hidden border transition group ${
                      active
                        ? 'border-[var(--primary)] ring-2 ring-[var(--primary)]/30'
                        : 'border-[var(--border)] hover:border-[var(--primary)]/40'
                    }`}
                  >
                    <div className="relative w-full aspect-[3/4] bg-[var(--hero-bg)]">
                      <Image
                        src={url}
                        alt={`Candidate ${i + 1}`}
                        fill
                        className="object-cover transition-transform duration-700 group-hover:scale-[1.03]"
                        unoptimized
                      />
                    </div>
                    <div className="absolute top-3 left-3">
                      <span className="rounded-full bg-black/45 backdrop-blur px-3 py-1 text-[0.65rem] tracking-widest uppercase text-white">
                        Candidate 0{i + 1}
                      </span>
                    </div>
                    {active && (
                      <div className="absolute top-3 right-3 bg-[var(--primary)] text-white rounded-full p-1.5 shadow-md">
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="3" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                        </svg>
                      </div>
                    )}
                  </button>
                );
              })}
            </div>

            {selectedIndex !== null && project.status === ProjectStatus.PREVIEW_READY && (
              <div className="rounded-3xl border border-[var(--border)] bg-[var(--card)] p-8 text-center space-y-4">
                <Eyebrow>The keepsake</Eyebrow>
                <h3 className="font-serif text-2xl text-[var(--foreground)]">
                  Unlock your selection in HD.
                </h3>
                <p className="text-sm text-[var(--muted)] max-w-md mx-auto">
                  Receive a high-resolution version of your chosen candidate, free of watermarks.
                </p>
                <div className="flex flex-col sm:flex-row gap-3 justify-center pt-2">
                  <button
                    onClick={() => handlePurchase('hd_unlock')}
                    className="rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition"
                  >
                    HD Unlock — $9.99
                  </button>
                  <button
                    onClick={() => handlePurchase('full_pack')}
                    className="rounded-full border border-[var(--primary)] px-6 py-3 text-sm font-medium text-[var(--primary)] hover:bg-[var(--primary)]/5 transition"
                  >
                    Full Pack — $14.99
                  </button>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Already paid → go to download */}
        {isPaid && project.status === ProjectStatus.COMPLETED && (
          <div className="rounded-3xl border border-[var(--border)] bg-[var(--card)] p-8 text-center space-y-4">
            <Eyebrow>Ready</Eyebrow>
            <h3 className="font-serif text-2xl text-[var(--foreground)]">
              Your memory photo is ready.
            </h3>
            <button
              onClick={() => router.push(`/download/${projectId}`)}
              className="inline-flex items-center justify-center gap-2 rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition"
            >
              Go to download
              <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M5 12h14" />
                <path d="M12 5l7 7-7 7" />
              </svg>
            </button>
          </div>
        )}
      </section>
    </main>
  );
}
