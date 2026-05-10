'use client';

import Link from 'next/link';

interface NavProps {
  variant?: 'transparent' | 'solid';
  rightSlot?: React.ReactNode;
}

export function Nav({ variant = 'solid', rightSlot }: NavProps) {
  const isTransparent = variant === 'transparent';

  const wrapperClass = isTransparent
    ? 'absolute top-0 inset-x-0 z-30 text-[var(--hero-fg)]'
    : 'sticky top-0 z-30 bg-[var(--background)]/85 backdrop-blur-md border-b border-[var(--border)] text-[var(--foreground)]';

  const linkClass = isTransparent
    ? 'text-sm tracking-wide text-[var(--hero-fg)]/80 hover:text-[var(--hero-accent)] transition'
    : 'text-sm tracking-wide text-[var(--muted)] hover:text-[var(--foreground)] transition';

  const brandClass = isTransparent
    ? 'font-serif text-lg tracking-wide text-[var(--hero-fg)]'
    : 'font-serif text-lg tracking-wide text-[var(--foreground)]';

  return (
    <nav className={wrapperClass}>
      <div className="max-w-6xl mx-auto px-6 sm:px-10 h-16 flex items-center justify-between">
        <Link href="/" className={brandClass}>
          Memora
        </Link>

        <div className="hidden sm:flex items-center gap-8">
          <Link href="/#story" className={linkClass}>Our Story</Link>
          <Link href="/#how" className={linkClass}>How It Works</Link>
          <Link href="/#pricing" className={linkClass}>Pricing</Link>
        </div>

        <div className="flex items-center gap-3">
          {rightSlot}
        </div>
      </div>
    </nav>
  );
}
