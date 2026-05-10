interface ScrollCueProps {
  label?: string;
}

export function ScrollCue({ label = 'Scroll' }: ScrollCueProps) {
  return (
    <div className="flex flex-col items-center gap-2 text-[var(--hero-fg)]/80">
      <svg
        className="scroll-cue w-5 h-5"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <path d="M12 19V5" />
        <path d="M5 12l7-7 7 7" />
      </svg>
      <span className="eyebrow">{label}</span>
    </div>
  );
}
