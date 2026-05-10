import Image from 'next/image';
import { Eyebrow } from './Eyebrow';
import { ScrollCue } from './ScrollCue';

interface HeroProps {
  imageSrc: string;
  imageAlt: string;
  eyebrow: string;
  title: React.ReactNode;
  description?: React.ReactNode;
  children?: React.ReactNode;
  showScrollCue?: boolean;
  scrollLabel?: string;
}

export function Hero({
  imageSrc,
  imageAlt,
  eyebrow,
  title,
  description,
  children,
  showScrollCue = true,
  scrollLabel = 'Scroll to begin',
}: HeroProps) {
  return (
    <section className="relative w-full h-[100svh] min-h-[640px] overflow-hidden bg-[var(--hero-bg)] film-grain">
      <Image
        src={imageSrc}
        alt={imageAlt}
        fill
        priority
        sizes="100vw"
        className="object-cover"
        unoptimized
      />

      <div className="absolute inset-0 hero-overlay" aria-hidden />

      <div className="relative z-10 h-full flex flex-col items-center justify-center px-6 text-center text-[var(--hero-fg)]">
        <div className="max-w-3xl mx-auto space-y-8 fade-up">
          <Eyebrow tone="dark" className="fade-up fade-up-delay-1">
            {eyebrow}
          </Eyebrow>
          <h1 className="font-serif text-4xl sm:text-6xl md:text-7xl leading-[1.05] fade-up fade-up-delay-2">
            {title}
          </h1>
          {description && (
            <p className="max-w-xl mx-auto text-sm sm:text-base text-[var(--hero-fg)]/80 leading-relaxed fade-up fade-up-delay-3">
              {description}
            </p>
          )}
          {children && (
            <div className="pt-4 fade-up fade-up-delay-3">{children}</div>
          )}
        </div>

        {showScrollCue && (
          <div className="absolute bottom-10 left-1/2 -translate-x-1/2 fade-up fade-up-delay-3">
            <ScrollCue label={scrollLabel} />
          </div>
        )}
      </div>
    </section>
  );
}
