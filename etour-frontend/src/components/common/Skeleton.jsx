/**
 * Shimmer loading placeholder. `Skeleton` is the raw primitive (a shaped
 * shimmering block); `SkeletonCard` composes it into the shape of a Card
 * so lists can render N of them while data loads, avoiding blank screens.
 */
export function Skeleton({ className = "" }) {
  return <div className={`skeleton-shimmer rounded-lg bg-ink-100 ${className}`} aria-hidden="true" />;
}

/**
 * Mirrors TourCard's shape - same 4:3 image ratio and padding - so swapping
 * the skeleton for real content causes no layout shift.
 */
export function SkeletonCard() {
  return (
    <div className="h-full overflow-hidden rounded-card bg-white shadow-card">
      <Skeleton className="aspect-[4/3] w-full rounded-none" />
      <div className="flex flex-col gap-3 p-4">
        <Skeleton className="h-4 w-3/4" />
        <Skeleton className="h-3 w-1/2" />
        <Skeleton className="h-3 w-2/3" />
        <div className="flex items-end justify-between border-t border-ink-100 pt-3">
          <Skeleton className="h-6 w-20" />
          <Skeleton className="h-8 w-24 rounded-pill" />
        </div>
      </div>
    </div>
  );
}

/** Circular icon + label tile, matching the sector/category grid. */
export function SkeletonTile() {
  return (
    <div className="flex flex-col items-center gap-3 rounded-card bg-white p-5 shadow-soft">
      <Skeleton className="h-14 w-14 rounded-full" />
      <Skeleton className="h-3 w-20" />
    </div>
  );
}

/** Matches CategoryCard's 4:5 portrait tile. */
export function SkeletonCategoryCard() {
  return <Skeleton className="aspect-[4/5] w-full rounded-card" />;
}
