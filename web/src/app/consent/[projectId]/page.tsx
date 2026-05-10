'use client';

import { useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { giveConsent } from '@/lib/api';
import { Nav } from '@/components/Nav';
import { PageHeader } from '@/components/PageHeader';
import { Eyebrow } from '@/components/Eyebrow';

const CONSENT_ITEMS = [
  {
    key: 'rights' as const,
    title: 'Photo rights',
    desc: 'I have the right to use all uploaded photos and confirm I own or have permission to use them.',
  },
  {
    key: 'private' as const,
    title: 'Private family use',
    desc: 'This memory is for private family use only — not for public galleries, impersonation, or commercial purposes.',
  },
  {
    key: 'ai' as const,
    title: 'AI-generated output',
    desc: 'I understand the result is AI-generated and will be clearly labeled as such.',
  },
];

type ChecksState = { rights: boolean; private: boolean; ai: boolean };

export default function ConsentPage() {
  const router = useRouter();
  const params = useParams();
  const projectId = params.projectId as string;

  const [checks, setChecks] = useState<ChecksState>({
    rights: false,
    private: false,
    ai: false,
  });
  const [loading, setLoading] = useState(false);

  const allChecked = checks.rights && checks.private && checks.ai;

  const handleSubmit = async () => {
    if (!allChecked) return;
    setLoading(true);
    try {
      await giveConsent(projectId);
      router.push(`/preview/${projectId}`);
    } catch {
      alert('Failed to save consent. Please try again.');
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen">
      <Nav variant="solid" />

      <PageHeader
        eyebrow="Step 03 · A Quiet Promise"
        title={
          <>
            Before we begin,
            <br />
            <em className="italic font-normal text-[var(--hero-accent)]">a few honest words.</em>
          </>
        }
        description="Memora exists for private family memories. Please confirm a few things so we can proceed in good faith."
        backHref="/"
        backLabel="Back to projects"
      />

      <section className="max-w-2xl mx-auto px-6 sm:px-10 py-14">
        <div className="space-y-3">
          {CONSENT_ITEMS.map((item) => {
            const active = checks[item.key];
            return (
              <button
                key={item.key}
                type="button"
                onClick={() =>
                  setChecks((prev) => ({ ...prev, [item.key]: !prev[item.key] }))
                }
                className={`w-full text-left rounded-2xl border p-5 transition flex items-start gap-4 ${
                  active
                    ? 'border-[var(--primary)] bg-[var(--primary)]/[0.06] ring-1 ring-[var(--primary)]/40'
                    : 'border-[var(--border)] bg-[var(--card)] hover:border-[var(--primary)]/40 hover:shadow-sm'
                }`}
              >
                <div
                  className={`mt-0.5 shrink-0 w-6 h-6 rounded-full border-2 flex items-center justify-center transition ${
                    active
                      ? 'border-[var(--primary)] bg-[var(--primary)] text-white'
                      : 'border-[var(--muted)]/50'
                  }`}
                >
                  {active && (
                    <svg className="w-3 h-3" fill="none" stroke="currentColor" strokeWidth="3" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                    </svg>
                  )}
                </div>
                <div className="space-y-1">
                  <Eyebrow>{`Confirm 0${CONSENT_ITEMS.indexOf(item) + 1}`}</Eyebrow>
                  <div className="font-serif text-lg text-[var(--foreground)]">{item.title}</div>
                  <div className="text-sm text-[var(--muted)] leading-relaxed">{item.desc}</div>
                </div>
              </button>
            );
          })}
        </div>

        <div className="mt-10 pt-8 border-t border-[var(--border)] flex flex-col-reverse sm:flex-row sm:items-center sm:justify-between gap-4">
          <p className="text-xs text-[var(--muted)] max-w-sm">
            By confirming, you agree to our Terms and Privacy Policy. We never share your photos.
          </p>
          <button
            onClick={handleSubmit}
            disabled={!allChecked || loading}
            className="inline-flex items-center justify-center gap-2 rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Saving…' : 'Confirm & continue'}
            {!loading && (
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
