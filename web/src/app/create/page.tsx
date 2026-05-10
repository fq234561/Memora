'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { createProject } from '@/lib/api';
import { PhotoStyle, ActivityType, PersonType } from '@/lib/types';
import { Nav } from '@/components/Nav';
import { PageHeader } from '@/components/PageHeader';
import { Eyebrow } from '@/components/Eyebrow';

const styles = [
  { value: PhotoStyle.NATURAL_FAMILY, label: 'Natural Family', desc: 'Warm, natural family portrait style' },
  { value: PhotoStyle.TRAVEL_MEMORY, label: 'Travel Memory', desc: 'Blend loved ones into travel scenery' },
  { value: PhotoStyle.PARTY_GATHERING, label: 'Party Gathering', desc: 'Relaxed, joyful party scenes' },
  { value: PhotoStyle.HOLIDAY_CELEBRATION, label: 'Holiday Celebration', desc: 'Warm holiday reunion atmosphere' },
  { value: PhotoStyle.MILESTONE_EVENT, label: 'Milestone Event', desc: 'Weddings, graduations, life milestones' },
];

const activityTypes = Object.values(ActivityType);
const personTypes = Object.values(PersonType);

function titleCase(value: string) {
  return value.charAt(0) + value.slice(1).toLowerCase();
}

export default function CreateProjectPage() {
  const router = useRouter();
  const [title, setTitle] = useState('');
  const [style, setStyle] = useState<PhotoStyle>(PhotoStyle.NATURAL_FAMILY);
  const [eventDate, setEventDate] = useState('');
  const [activityType, setActivityType] = useState<ActivityType | ''>('');
  const [selectedPersonTypes, setSelectedPersonTypes] = useState<PersonType[]>([]);
  const [loading, setLoading] = useState(false);

  const togglePersonType = (pt: PersonType) => {
    setSelectedPersonTypes((prev) =>
      prev.includes(pt) ? prev.filter((p) => p !== pt) : [...prev, pt]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;
    setLoading(true);
    try {
      const project = await createProject({
        title: title.trim(),
        style,
        eventDate: eventDate || undefined,
        activityType: activityType || undefined,
        personTypes: selectedPersonTypes.length > 0 ? selectedPersonTypes : undefined,
      });
      router.push(`/upload/${project.id}`);
    } catch {
      alert('Failed to create project. Please try again.');
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen">
      <Nav variant="solid" />

      <PageHeader
        eyebrow="Step 01 · New Memory"
        title={
          <>
            Begin a new
            <br />
            <em className="italic font-normal text-[var(--hero-accent)]">family memory.</em>
          </>
        }
        description="Give your memory a name and choose the mood. You'll add photos in the next step."
        backHref="/"
        backLabel="Back to projects"
      />

      <section className="max-w-3xl mx-auto px-6 sm:px-10 py-14">
        <form onSubmit={handleSubmit} className="space-y-12">
          {/* Title */}
          <div className="space-y-3">
            <Eyebrow>Title</Eyebrow>
            <h2 className="font-serif text-2xl text-[var(--foreground)]">
              What should we call this memory?
            </h2>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g., Summer Family Trip"
              className="w-full rounded-xl border border-[var(--border)] bg-[var(--card)] px-4 py-3 text-base text-[var(--foreground)] placeholder:text-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-[var(--primary)]/30 focus:border-[var(--primary)]/40 transition"
              required
            />
          </div>

          {/* Style */}
          <div className="space-y-4">
            <div className="space-y-2">
              <Eyebrow>The Mood</Eyebrow>
              <h2 className="font-serif text-2xl text-[var(--foreground)]">
                Choose a style.
              </h2>
              <p className="text-sm text-[var(--muted)]">
                Each style shapes the lighting, framing, and atmosphere of the final photo.
              </p>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {styles.map((s) => {
                const active = style === s.value;
                return (
                  <button
                    key={s.value}
                    type="button"
                    onClick={() => setStyle(s.value)}
                    className={`text-left rounded-2xl border p-5 transition ${
                      active
                        ? 'border-[var(--primary)] bg-[var(--primary)]/[0.06] ring-1 ring-[var(--primary)]/40'
                        : 'border-[var(--border)] bg-[var(--card)] hover:border-[var(--primary)]/40 hover:shadow-sm'
                    }`}
                  >
                    <div className="font-serif text-lg text-[var(--foreground)]">{s.label}</div>
                    <div className="text-xs text-[var(--muted)] mt-1 leading-relaxed">{s.desc}</div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Event Date */}
          <div className="space-y-3">
            <Eyebrow>The Moment</Eyebrow>
            <h2 className="font-serif text-2xl text-[var(--foreground)]">
              When did this happen?
            </h2>
            <p className="text-sm text-[var(--muted)]">Optional — helps us anchor seasonal feel.</p>
            <input
              type="text"
              value={eventDate}
              onChange={(e) => setEventDate(e.target.value)}
              placeholder="YYYY-MM-DD"
              pattern="\d{4}-\d{2}-\d{2}"
              inputMode="numeric"
              maxLength={10}
              className="w-full sm:w-auto rounded-xl border border-[var(--border)] bg-[var(--card)] px-4 py-3 text-base text-[var(--foreground)] placeholder:text-[var(--muted)] focus:outline-none focus:ring-2 focus:ring-[var(--primary)]/30 focus:border-[var(--primary)]/40 transition"
            />
          </div>

          {/* Activity Type */}
          <div className="space-y-3">
            <Eyebrow>The Occasion</Eyebrow>
            <h2 className="font-serif text-2xl text-[var(--foreground)]">
              What kind of moment was it?
            </h2>
            <div className="flex flex-wrap gap-2 pt-1">
              {activityTypes.map((at) => {
                const active = activityType === at;
                return (
                  <button
                    key={at}
                    type="button"
                    onClick={() => setActivityType(active ? '' : at)}
                    className={`rounded-full px-4 py-2 text-xs tracking-wide font-medium border transition ${
                      active
                        ? 'border-[var(--primary)] bg-[var(--primary)]/10 text-[var(--primary)]'
                        : 'border-[var(--border)] bg-[var(--card)] text-[var(--muted)] hover:border-[var(--primary)]/40 hover:text-[var(--foreground)]'
                    }`}
                  >
                    {titleCase(at)}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Person Types */}
          <div className="space-y-3">
            <Eyebrow>The People</Eyebrow>
            <h2 className="font-serif text-2xl text-[var(--foreground)]">
              Who will appear in the photo?
            </h2>
            <p className="text-sm text-[var(--muted)]">Pick all that apply.</p>
            <div className="flex flex-wrap gap-2 pt-1">
              {personTypes.map((pt) => {
                const active = selectedPersonTypes.includes(pt);
                return (
                  <button
                    key={pt}
                    type="button"
                    onClick={() => togglePersonType(pt)}
                    className={`rounded-full px-4 py-2 text-xs tracking-wide font-medium border transition ${
                      active
                        ? 'border-[var(--primary)] bg-[var(--primary)]/10 text-[var(--primary)]'
                        : 'border-[var(--border)] bg-[var(--card)] text-[var(--muted)] hover:border-[var(--primary)]/40 hover:text-[var(--foreground)]'
                    }`}
                  >
                    {titleCase(pt)}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Submit */}
          <div className="pt-4 border-t border-[var(--border)] flex flex-col-reverse sm:flex-row sm:items-center sm:justify-between gap-4">
            <p className="text-xs text-[var(--muted)]">
              You can refine these details later — nothing is final until you preview.
            </p>
            <button
              type="submit"
              disabled={loading || !title.trim()}
              className="inline-flex items-center justify-center gap-2 rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Creating…' : 'Continue to upload'}
              {!loading && (
                <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M5 12h14" />
                  <path d="M12 5l7 7-7 7" />
                </svg>
              )}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}
