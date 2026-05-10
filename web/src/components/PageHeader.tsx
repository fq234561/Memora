import Link from 'next/link';
import { Eyebrow } from './Eyebrow';

interface PageHeaderProps {
  eyebrow: string;
  title: React.ReactNode;
  description?: React.ReactNode;
  backHref?: string;
  backLabel?: string;
  rightSlot?: React.ReactNode;
}

export function PageHeader({
  eyebrow,
  title,
  description,
  backHref = '/',
  backLabel = 'Back to projects',
  rightSlot,
}: PageHeaderProps) {
  return (
    <header className="relative w-full bg-[var(--hero-bg)] text-[var(--hero-fg)] film-grain overflow-hidden">
      <div
        className="absolute inset-0 opacity-60"
        style={{
          background:
            'radial-gradient(ellipse at top, rgba(212,165,116,0.18) 0%, rgba(26,18,11,0) 60%)',
        }}
        aria-hidden
      />
      <div className="relative max-w-4xl mx-auto px-6 sm:px-10 pt-10 pb-14 sm:pt-14 sm:pb-20">
        <div className="flex items-center justify-between mb-10">
          <Link
            href={backHref}
            className="inline-flex items-center gap-2 text-xs tracking-wider uppercase text-[var(--hero-fg)]/70 hover:text-[var(--hero-accent)] transition"
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M19 12H5" />
              <path d="M12 19l-7-7 7-7" />
            </svg>
            {backLabel}
          </Link>
          {rightSlot}
        </div>

        <div className="space-y-5">
          <Eyebrow tone="dark">{eyebrow}</Eyebrow>
          <h1 className="font-serif text-3xl sm:text-5xl leading-[1.1]">{title}</h1>
          {description && (
            <p className="max-w-xl text-sm sm:text-base text-[var(--hero-fg)]/75 leading-relaxed">
              {description}
            </p>
          )}
        </div>
      </div>
    </header>
  );
}
