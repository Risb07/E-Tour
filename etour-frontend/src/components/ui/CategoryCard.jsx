import { ArrowUpRight } from "lucide-react";
import { resolveMediaUrl } from "../../utils/media";

/**
 * Home page category tile - image, gradient overlay, name, tour count.
 *
 * `activeTourCount` is computed server-side and counts ACTIVE tours only, so
 * the number here matches what the category page will actually list. It used
 * to read `category.tours.length`, but that collection is @JsonIgnore'd on the
 * backend, so the tile always showed "0 tours".
 *
 * The image is category.image_url from the database. When a category has no
 * image the tile renders on the gradient alone - no stock photo stand-in, so
 * missing media stays visible as missing.
 */
export default function CategoryCard({ category, onClick }) {
  const tourCount = category.activeTourCount ?? 0;
  const imageUrl = resolveMediaUrl(category.imageUrl);

  return (
    <button
      onClick={onClick}
      className="group relative aspect-[4/5] w-full overflow-hidden rounded-card bg-ink-800 shadow-card transition-all duration-300 hover:-translate-y-1 hover:shadow-lifted"
    >
      {imageUrl && (
        <img
          src={imageUrl}
          alt={category.categoryName}
          loading="lazy"
          className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-110"
        />
      )}
      <div className="absolute inset-0 bg-gradient-to-t from-ink-900/90 via-ink-900/20 to-transparent" />
      <div className="absolute inset-x-0 bottom-0 p-5 text-left">
        <h3 className="font-display text-lg font-bold text-white">{category.categoryName}</h3>
        <div className="mt-1 flex items-center justify-between">
          <span className="text-xs font-medium text-ink-200">
            {tourCount} {tourCount === 1 ? "tour" : "tours"}
          </span>
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-white/15 text-white backdrop-blur transition-colors group-hover:bg-amber-500 group-hover:text-ink-900">
            <ArrowUpRight className="h-4 w-4" />
          </span>
        </div>
      </div>
    </button>
  );
}

