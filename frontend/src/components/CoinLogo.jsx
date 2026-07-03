export default function CoinLogo({ className = 'w-12 h-12', id = 'coin', variant = 'gradient' }) {
  // "gradient": primary->accent coin with white € — for neutral/card backgrounds
  // "light": white coin with primary-colored € — for use on the colored hero panel
  const isLight = variant === 'light';

  return (
    <svg viewBox="0 0 48 48" className={className}>
      <defs>
        <linearGradient id={`${id}-bg`} x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="var(--primary)" />
          <stop offset="100%" stopColor="var(--accent)" />
        </linearGradient>
      </defs>
      <circle cx="24" cy="24" r="22" fill={isLight ? '#ffffff' : `url(#${id}-bg)`} />
      <circle
        cx="24" cy="24" r="17" fill="none"
        stroke={isLight ? 'var(--primary)' : '#ffffff'}
        strokeOpacity={isLight ? 0.25 : 0.5}
        strokeWidth="1.5"
      />
      <text
        x="24"
        y="32"
        fontFamily="Outfit, sans-serif"
        fontSize="24"
        fontWeight="800"
        fill={isLight ? 'var(--primary)' : '#ffffff'}
        textAnchor="middle"
      >
        €
      </text>
    </svg>
  );
}
