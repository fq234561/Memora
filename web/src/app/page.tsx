'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { getProjects, deleteProject } from '@/lib/api';
import { initGoogleSignIn, renderGoogleButton, handleGoogleLogin, logout, getStoredUser, isLoggedIn } from '@/lib/auth';
import { Project, ProjectStatus } from '@/lib/types';
import { Hero } from '@/components/Hero';
import { Nav } from '@/components/Nav';
import { Eyebrow } from '@/components/Eyebrow';

const HERO_IMAGE =
  'https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=2560&q=80';
const DASHBOARD_HERO_IMAGE =
  'https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?auto=format&fit=crop&w=2560&q=80';
const EMPTY_STATE_IMAGE =
  'https://images.unsplash.com/photo-1542652694-40abf526446e?auto=format&fit=crop&w=1600&q=80';

const statusLabels: Record<string, string> = {
  [ProjectStatus.DRAFT]: 'Draft',
  [ProjectStatus.UPLOADED]: 'Photos Uploaded',
  [ProjectStatus.GENERATING]: 'Generating',
  [ProjectStatus.PREVIEW_READY]: 'Preview Ready',
  [ProjectStatus.PURCHASED]: 'Purchased',
  [ProjectStatus.COMPLETED]: 'Completed',
  [ProjectStatus.FAILED]: 'Failed',
};

function getProjectActionUrl(project: Project): string {
  switch (project.status) {
    case ProjectStatus.DRAFT:
      return `/upload/${project.id}`;
    case ProjectStatus.UPLOADED:
      return `/consent/${project.id}`;
    case ProjectStatus.GENERATING:
    case ProjectStatus.PREVIEW_READY:
    case ProjectStatus.FAILED:
      return `/preview/${project.id}`;
    case ProjectStatus.PURCHASED:
    case ProjectStatus.COMPLETED:
      return `/download/${project.id}`;
    default:
      return `/upload/${project.id}`;
  }
}

interface StoredUser {
  email?: string;
  name?: string;
}

export default function HomePage() {
  const [loggedIn, setLoggedIn] = useState(false);
  const [user, setUser] = useState<StoredUser | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [loginLoading, setLoginLoading] = useState(false);

  useEffect(() => {
    const check = async () => {
      if (isLoggedIn()) {
        setLoggedIn(true);
        setUser(getStoredUser());
        try {
          const data = await getProjects();
          setProjects(data || []);
        } catch (e) {
          console.error('Failed to load projects', e);
        }
      }
      setLoading(false);
    };
    check();
  }, []);

  useEffect(() => {
    if (!loggedIn) {
      initGoogleSignIn(async (credential) => {
        setLoginLoading(true);
        try {
          const data = await handleGoogleLogin(credential);
          setUser(data.user);
          setLoggedIn(true);
          const projData = await getProjects();
          setProjects(projData || []);
        } catch {
          alert('Login failed. Please try again.');
        } finally {
          setLoginLoading(false);
        }
      });
    }
  }, [loggedIn]);

  useEffect(() => {
    if (!loggedIn) {
      const t = setTimeout(() => renderGoogleButton('google-signin-button'), 500);
      return () => clearTimeout(t);
    }
  }, [loggedIn, loginLoading]);

  const handleDelete = async (projectId: string) => {
    if (!confirm('Are you sure you want to delete this memory?')) return;
    try {
      await deleteProject(projectId);
      setProjects(projects.filter((p) => p.id !== projectId));
    } catch {
      alert('Failed to delete project');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[var(--hero-bg)]">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-[var(--hero-accent)]"></div>
      </div>
    );
  }

  /* ─────────────────  Logged-out landing  ───────────────── */
  if (!loggedIn) {
    return (
      <main className="relative">
        <Nav variant="transparent" />

        <Hero
          imageSrc={HERO_IMAGE}
          imageAlt="Family at golden hour"
          eyebrow="EST. 2026 · AI Family Memory Photos"
          title={
            <>
              Bring loved ones into the
              <br />
              <em className="italic font-normal text-[var(--hero-accent)]">moments they missed.</em>
            </>
          }
          description="From birthdays to weddings, travels to quiet afternoons — Memora gently composes the family memory photo that should have been."
          scrollLabel="Sign in below"
        >
          <div className="space-y-3 pt-2">
            {loginLoading ? (
              <div className="flex justify-center py-3">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-[var(--hero-accent)]"></div>
              </div>
            ) : (
              <div id="google-signin-button" className="flex justify-center"></div>
            )}
            <p className="text-xs text-[var(--hero-fg)]/60">
              By continuing, you agree to our Terms and Privacy Policy.
            </p>
          </div>
        </Hero>

        {/* How it works */}
        <section id="how" className="bg-[var(--background)] py-24 px-6">
          <div className="max-w-5xl mx-auto">
            <div className="text-center mb-16 space-y-4">
              <Eyebrow>The Process</Eyebrow>
              <h2 className="font-serif text-3xl sm:text-5xl text-[var(--foreground)]">
                A quiet, considered way to remember.
              </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
              {[
                {
                  n: '01',
                  title: 'Choose your memory',
                  body: 'Pick the event photo and a reference photo of the loved one you want to include.',
                },
                {
                  n: '02',
                  title: 'Set the mood',
                  body: 'Select a natural style — family portrait, travel, holiday, milestone — and we shape the rest.',
                },
                {
                  n: '03',
                  title: 'Receive your keepsake',
                  body: 'Preview four candidates, pick the one that feels right, and download in HD for safekeeping.',
                },
              ].map((step) => (
                <div key={step.n} className="space-y-3">
                  <div className="font-serif italic text-[var(--accent)] text-2xl">{step.n}</div>
                  <h3 className="font-serif text-xl text-[var(--foreground)]">{step.title}</h3>
                  <p className="text-sm text-[var(--muted)] leading-relaxed">{step.body}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Story / pricing strip */}
        <section id="story" className="bg-[var(--hero-bg)] text-[var(--hero-fg)] py-20 px-6 film-grain relative">
          <div className="max-w-3xl mx-auto text-center space-y-5">
            <Eyebrow tone="dark">Why Memora</Eyebrow>
            <p className="font-serif text-2xl sm:text-3xl leading-relaxed">
              Some moments arrived without the people we love most. <br />
              Memora is for the photo album that finally completes them — privately, naturally, and with care.
            </p>
          </div>
        </section>

        <footer className="bg-[var(--background)] border-t border-[var(--border)] py-10 px-6 text-center">
          <p className="text-xs text-[var(--muted)]">© 2026 Memora. AI-generated images are clearly labeled. Private family use only.</p>
        </footer>
      </main>
    );
  }

  /* ─────────────────  Logged-in dashboard  ───────────────── */
  return (
    <main className="min-h-screen">
      <Nav
        variant="solid"
        rightSlot={
          <div className="flex items-center gap-3">
            <Link
              href="/create"
              className="rounded-full bg-[var(--primary)] px-4 py-2 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition"
            >
              + New Memory
            </Link>
            <button
              onClick={logout}
              className="rounded-full border border-[var(--border)] px-3 py-2 text-sm text-[var(--muted)] hover:text-[var(--foreground)] transition"
            >
              Sign out
            </button>
          </div>
        }
      />

      <section className="relative w-full min-h-[60vh] flex items-center bg-[var(--hero-bg)] text-[var(--hero-fg)] film-grain overflow-hidden">
        <Image
          src={DASHBOARD_HERO_IMAGE}
          alt="Family album backdrop"
          fill
          priority
          sizes="100vw"
          className="object-cover opacity-70"
          unoptimized
        />
        <div className="absolute inset-0 hero-overlay" aria-hidden />
        <div
          className="absolute inset-0 opacity-50"
          style={{
            background:
              'radial-gradient(ellipse at 20% 30%, rgba(212,165,116,0.18) 0%, rgba(26,18,11,0) 60%)',
          }}
          aria-hidden
        />
        <div className="relative max-w-4xl mx-auto px-6 sm:px-10 py-20 sm:py-24 space-y-6 fade-up">
          <Eyebrow tone="dark" className="fade-up fade-up-delay-1">
            Welcome back{user?.name ? `, ${user.name.split(' ')[0]}` : ''}
          </Eyebrow>
          <h1 className="font-serif text-4xl sm:text-6xl leading-[1.05] fade-up fade-up-delay-2">
            Your collection of
            <br />
            <em className="italic font-normal text-[var(--hero-accent)]">family memories.</em>
          </h1>
          <p className="max-w-xl text-sm sm:text-base text-[var(--hero-fg)]/75 leading-relaxed fade-up fade-up-delay-3">
            Each project below is a private family memory in progress. Continue where you left off,
            or begin a new chapter — quietly, naturally, with care.
          </p>
        </div>
      </section>

      <section className="max-w-4xl mx-auto px-6 py-14">
        {projects.length === 0 ? (
          <div className="relative overflow-hidden rounded-3xl border border-[var(--border)] bg-[var(--card)] shadow-sm">
            <div className="grid grid-cols-1 md:grid-cols-2">
              <div className="relative aspect-[4/3] md:aspect-auto md:min-h-[360px] bg-[var(--hero-bg)]">
                <Image
                  src={EMPTY_STATE_IMAGE}
                  alt="A family moment"
                  fill
                  sizes="(min-width: 768px) 50vw, 100vw"
                  className="object-cover"
                  unoptimized
                />
                <div
                  className="absolute inset-0"
                  style={{
                    background:
                      'linear-gradient(135deg, rgba(26,18,11,0.10) 0%, rgba(26,18,11,0) 60%)',
                  }}
                  aria-hidden
                />
              </div>
              <div className="p-10 sm:p-12 flex flex-col justify-center text-left space-y-5">
                <Eyebrow>Empty for now</Eyebrow>
                <h2 className="font-serif text-3xl text-[var(--foreground)] leading-tight">
                  Begin your first
                  <br />
                  <em className="italic font-normal text-[var(--accent)]">family memory.</em>
                </h2>
                <p className="text-sm text-[var(--muted)] leading-relaxed max-w-sm">
                  Upload an event photo and a reference of the loved one — Memora will compose the
                  memory that should have been.
                </p>
                <div>
                  <Link
                    href="/create"
                    className="inline-flex items-center gap-2 rounded-full bg-[var(--primary)] px-6 py-3 text-sm font-medium text-white hover:bg-[var(--primary-dark)] transition"
                  >
                    Create a memory photo
                    <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M5 12h14" />
                      <path d="M12 5l7 7-7 7" />
                    </svg>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            {projects.map((project) => (
              <article
                key={project.id}
                className="group relative bg-[var(--card)] border border-[var(--border)] rounded-2xl overflow-hidden hover:shadow-lg transition"
              >
                <Link href={getProjectActionUrl(project)} className="block">
                  <div className="relative aspect-[4/3] bg-[var(--hero-bg)] overflow-hidden">
                    {project.basePhotoUrl ? (
                      <Image
                        src={project.basePhotoUrl}
                        alt={project.title}
                        fill
                        className="object-cover group-hover:scale-105 transition-transform duration-700"
                        unoptimized
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-[var(--hero-muted)] text-xs eyebrow">
                        No photo yet
                      </div>
                    )}
                    <div className="absolute top-3 left-3">
                      <span className="inline-flex items-center rounded-full bg-black/45 backdrop-blur px-3 py-1 text-[0.65rem] tracking-widest uppercase text-white">
                        {statusLabels[project.status] || project.status}
                      </span>
                    </div>
                  </div>
                  <div className="p-5 space-y-1">
                    <h3 className="font-serif text-xl text-[var(--foreground)] line-clamp-1">
                      {project.title}
                    </h3>
                    <p className="text-xs text-[var(--muted)] tracking-wide">
                      {project.style.replace(/_/g, ' ').toLowerCase()} ·{' '}
                      {new Date(project.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </p>
                  </div>
                </Link>
                <button
                  onClick={() => handleDelete(project.id)}
                  className="absolute top-3 right-3 rounded-full bg-black/40 backdrop-blur w-8 h-8 flex items-center justify-center text-white/80 hover:text-white hover:bg-red-600/70 transition opacity-0 group-hover:opacity-100"
                  aria-label="Delete memory"
                >
                  <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M3 6h18" />
                    <path d="M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2" />
                    <path d="M6 6l1 14a2 2 0 002 2h6a2 2 0 002-2l1-14" />
                  </svg>
                </button>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
