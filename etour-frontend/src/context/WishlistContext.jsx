import { createContext, useState, useEffect, useCallback, useMemo } from "react";
import { useAuth } from "../hooks/useAuth";
import { ROLES } from "../constants/roles";
import {
  fetchMyWishlist,
  addToWishlist,
  removeFromWishlist,
} from "../services/wishlistService";

export const WishlistContext = createContext(null);

/**
 * Holds the logged-in customer's saved tour ids once, so a grid of N tour
 * cards costs one request instead of N "is this saved?" calls.
 *
 * Toggling updates local state immediately and rolls back if the request
 * fails - the heart shouldn't lag behind the click.
 */
export function WishlistProvider({ children }) {
  const { isAuthenticated, user } = useAuth();
  const [items, setItems] = useState([]);
  const [savedTourIds, setSavedTourIds] = useState(() => new Set());
  const [status, setStatus] = useState("idle");

  // Only customers have a wishlist - admins have no customer profile, so
  // calling the API as an admin would 403/404.
  const canUseWishlist = isAuthenticated && user?.role === ROLES.CUSTOMER;

  const load = useCallback(async () => {
    if (!canUseWishlist) {
      setItems([]);
      setSavedTourIds(new Set());
      setStatus("idle");
      return;
    }
    setStatus("loading");
    try {
      const data = await fetchMyWishlist();
      setItems(data);
      setSavedTourIds(new Set(data.map((i) => i.tourId)));
      setStatus("succeeded");
    } catch {
      setItems([]);
      setSavedTourIds(new Set());
      setStatus("failed");
    }
  }, [canUseWishlist]);

  useEffect(() => {
    load();
  }, [load]);

  const isSaved = useCallback((tourId) => savedTourIds.has(Number(tourId)), [savedTourIds]);

  /** @returns {Promise<boolean>} the new saved state */
  const toggle = useCallback(
    async (tourId) => {
      const id = Number(tourId);
      const wasSaved = savedTourIds.has(id);

      // Optimistic update.
      setSavedTourIds((current) => {
        const next = new Set(current);
        if (wasSaved) next.delete(id);
        else next.add(id);
        return next;
      });

      try {
        if (wasSaved) {
          await removeFromWishlist(id);
          setItems((current) => current.filter((i) => i.tourId !== id));
        } else {
          const created = await addToWishlist(id);
          setItems((current) => [created, ...current.filter((i) => i.tourId !== id)]);
        }
        return !wasSaved;
      } catch (err) {
        // Roll back so the UI matches the server.
        setSavedTourIds((current) => {
          const next = new Set(current);
          if (wasSaved) next.add(id);
          else next.delete(id);
          return next;
        });
        throw err;
      }
    },
    [savedTourIds]
  );

  const value = useMemo(
    () => ({ items, status, canUseWishlist, isSaved, toggle, reload: load }),
    [items, status, canUseWishlist, isSaved, toggle, load]
  );

  return <WishlistContext.Provider value={value}>{children}</WishlistContext.Provider>;
}
