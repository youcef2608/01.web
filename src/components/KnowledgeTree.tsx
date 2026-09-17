/**
 * Animated SVG "knowledge tree": branches draw themselves and nodes pop in,
 * looping continuously to express accumulating knowledge.
 */
const BRANCHES: { d: string; dash: number; delay: number; width: number }[] = [
  { d: "M200 330 L200 220", dash: 120, delay: 0, width: 6 },
  { d: "M200 250 C200 250 150 230 120 185", dash: 110, delay: 0.5, width: 4.5 },
  { d: "M200 250 C200 250 250 230 280 185", dash: 110, delay: 0.8, width: 4.5 },
  { d: "M200 220 C200 220 185 165 200 120", dash: 110, delay: 1.1, width: 4.5 },
  { d: "M120 185 C120 185 95 160 70 150", dash: 70, delay: 1.5, width: 3 },
  { d: "M280 185 C280 185 305 160 330 150", dash: 70, delay: 1.7, width: 3 },
  { d: "M200 120 C200 120 160 95 135 90", dash: 80, delay: 1.9, width: 3 },
  { d: "M200 120 C200 120 240 95 265 90", dash: 80, delay: 2.1, width: 3 },
];

const NODES: { cx: number; cy: number; r: number; delay: number; clay?: boolean }[] = [
  { cx: 120, cy: 185, r: 9, delay: 1.1 },
  { cx: 280, cy: 185, r: 9, delay: 1.4, clay: true },
  { cx: 200, cy: 120, r: 11, delay: 1.7 },
  { cx: 70, cy: 150, r: 6.5, delay: 2.1, clay: true },
  { cx: 330, cy: 150, r: 6.5, delay: 2.3 },
  { cx: 135, cy: 90, r: 7.5, delay: 2.5, clay: true },
  { cx: 265, cy: 90, r: 7.5, delay: 2.7 },
];

export function KnowledgeTree() {
  return (
    <div className="athar-float relative mx-auto w-full max-w-lg">
      <div className="absolute inset-0 -z-10 rounded-full bg-accent/50 blur-3xl" />
      <svg
        viewBox="0 0 400 360"
        className="h-auto w-full"
        role="img"
        aria-label="شجرة معرفة متنامية"
      >
        <ellipse cx="200" cy="336" rx="96" ry="10" className="fill-accent" />

        {BRANCHES.map((b, i) => (
          <path
            key={i}
            d={b.d}
            className="athar-branch fill-none stroke-primary"
            strokeWidth={b.width}
            strokeLinecap="round"
            style={
              {
                "--dash": b.dash,
                animationDelay: `${b.delay}s`,
              } as React.CSSProperties
            }
          />
        ))}

        {NODES.map((n, i) => (
          <g key={i}>
            <circle
              cx={n.cx}
              cy={n.cy}
              r={n.r * 1.9}
              className={`athar-ring ${n.clay ? "fill-clay/30" : "fill-primary/25"}`}
              style={{ animationDelay: `${n.delay}s` }}
            />
            <circle
              cx={n.cx}
              cy={n.cy}
              r={n.r}
              className={`athar-node ${n.clay ? "fill-clay" : "fill-primary"}`}
              style={{ animationDelay: `${n.delay}s` }}
            />
          </g>
        ))}
      </svg>
    </div>
  );
}
