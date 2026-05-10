interface EyebrowProps {
  children: React.ReactNode;
  tone?: 'dark' | 'light';
  className?: string;
}

export function Eyebrow({ children, tone = 'light', className = '' }: EyebrowProps) {
  const color = tone === 'dark' ? 'text-[var(--hero-accent)]' : 'text-[var(--accent)]';
  return (
    <span className={`eyebrow ${color} ${className}`}>{children}</span>
  );
}
