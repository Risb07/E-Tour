import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ShoppingCart, Trash2 } from "lucide-react";
import { fetchMyCart, removeFromCart, checkoutCartItem } from "../services/cartService";
import { useToast } from "../hooks/useToast";
import { formatCurrency } from "../utils/format";
import { ROUTE_PATHS } from "../constants/routes";
import Button from "../components/common/Button";
import Loader from "../components/common/Loader";
import EmptyState from "../components/common/EmptyState";

export default function CartPage() {
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState("loading");
  const [busyId, setBusyId] = useState(null);
  const { showToast } = useToast();
  const navigate = useNavigate();

  function loadCart() {
    setStatus("loading");
    fetchMyCart()
      .then((data) => {
        setItems(data);
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }

  useEffect(loadCart, []);

  async function handleRemove(cartId) {
    setBusyId(cartId);
    try {
      await removeFromCart(cartId);
      showToast("Removed from cart.", "success");
      loadCart();
    } catch (err) {
      showToast(err.message || "Couldn't remove this item.", "error");
    } finally {
      setBusyId(null);
    }
  }

  async function handleCheckout(cartId) {
    setBusyId(cartId);
    try {
      // Creates a PENDING booking with an estimated total - passenger
      // details (needed for the real, age-banded price) are collected next.
      const booking = await checkoutCartItem(cartId);
      navigate(ROUTE_PATHS.cartCheckout(booking.bookingId));
    } catch (err) {
      showToast(err.message || "Couldn't check out this item.", "error");
      setBusyId(null);
    }
  }

  if (status === "loading") return <Loader label="Loading your cart..." />;

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
      <h1 className="font-display text-2xl font-bold text-ink-900 sm:text-3xl">Your cart</h1>

      {status === "failed" && (
        <p className="mt-4 text-sm text-red-600">Could not load your cart. Please try again.</p>
      )}

      {status === "succeeded" && items.length === 0 && (
        <EmptyState
          icon={ShoppingCart}
          title="Your cart is empty"
          description="Add a tour from its details page to see it here."
          primaryAction={{ label: "Explore tours", onClick: () => navigate(ROUTE_PATHS.SEARCH) }}
        />
      )}

      {items.length > 0 && (
        <div className="mt-6 flex flex-col gap-4">
          {items.map((item) => (
            <div key={item.cartId} className="rounded-card bg-white p-5 shadow-soft">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div>
                  <Link
                    to={ROUTE_PATHS.tourDetails(item.tourId)}
                    className="font-semibold text-ink-900 hover:text-amber-600"
                  >
                    {item.tourTitle}
                  </Link>
                  <p className="mt-1 text-sm text-ink-500">
                    {/* Show the party split when the item carries one; older
                        cart rows only have the headcount summary. */}
                    {item.adultCount != null
                      ? `${item.adultCount} adult${item.adultCount === 1 ? "" : "s"}` +
                        (item.childCount > 0
                          ? `, ${item.childCount} child${item.childCount === 1 ? "" : "ren"}`
                          : "")
                      : item.paxSummary}
                  </p>
                  {item.addons?.length > 0 && (
                    <p className="mt-1 text-xs text-ink-400">
                      {item.addons.map((a) => `${a.addonName} x${a.quantity}`).join(", ")}
                    </p>
                  )}
                </div>
                <div className="flex items-center gap-4">
                  <span className="font-display text-lg font-bold text-ink-900">
                    {formatCurrency(item.estimatedAmount)}
                  </span>
                  <Button
                    variant="ghost"
                    size="sm"
                    icon={Trash2}
                    disabled={busyId === item.cartId}
                    onClick={() => handleRemove(item.cartId)}
                  >
                    Remove
                  </Button>
                  <Button
                    size="sm"
                    isLoading={busyId === item.cartId}
                    onClick={() => handleCheckout(item.cartId)}
                  >
                    Checkout
                  </Button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
