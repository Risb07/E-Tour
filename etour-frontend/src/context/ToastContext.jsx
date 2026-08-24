import { createContext, useCallback, useMemo, useState } from "react";

export const ToastContext = createContext(null);

let idCounter = 0;

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((current) => current.filter((toast) => toast.id !== id));
  }, []);

  const showToast = useCallback(
    (message, variant = "info", durationMs = 4000) => {
      const id = ++idCounter;
      setToasts((current) => [...current, { id, message, variant }]);
      window.setTimeout(() => removeToast(id), durationMs);
    },
    [removeToast]
  );

  const value = useMemo(() => ({ showToast }), [showToast]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div
        className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 w-[calc(100%-2rem)] max-w-sm"
        aria-live="polite"
      >
        {toasts.map((toast) => (
          <div
            key={toast.id}
            role="status"
            className={[
              "rounded-lg px-4 py-3 shadow-lg text-sm font-medium text-white animate-[fadeIn_0.2s_ease-out]",
              toast.variant === "success" && "bg-emerald-600",
              toast.variant === "error" && "bg-red-600",
              toast.variant === "info" && "bg-slate-800",
            ]
              .filter(Boolean)
              .join(" ")}
          >
            {toast.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
