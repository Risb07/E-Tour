/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        // "Dusk horizon" palette: deep indigo (the sky just after sunset,
        // when a journey feels most possible) with a warm amber accent
        // (the last light on the horizon). Used sparingly - amber is for
        // primary actions only, never decoration.
        ink: {
          50: "#f4f5fa",
          100: "#e6e8f5",
          200: "#c3c8e6",
          300: "#9aa3d1",
          400: "#6b74ad",
          500: "#454d85",
          600: "#2f3566",
          700: "#22254d",
          800: "#181a38",
          900: "#0f1026",
        },
        amber: {
          50: "#fff8ec",
          100: "#ffedc7",
          200: "#ffd889",
          300: "#ffbe4d",
          400: "#ffa41f",
          500: "#f5850a",
          600: "#d66505",
          700: "#b04708",
          800: "#8e380d",
          900: "#752f0e",
        },
      },
      fontFamily: {
        display: ["'Sora'", "sans-serif"],
        body: ["'Inter'", "sans-serif"],
      },
      borderRadius: {
        card: "1rem",
        pill: "999px",
      },
      boxShadow: {
        // Soft, low-contrast shadows - a travel platform should feel light
        // and airy, not heavy/skeuomorphic. Elevation increases on hover,
        // never on default state, so pages don't look cluttered at rest.
        soft: "0 2px 8px -2px rgba(15, 16, 38, 0.08)",
        card: "0 4px 16px -4px rgba(15, 16, 38, 0.10)",
        lifted: "0 16px 32px -12px rgba(15, 16, 38, 0.22)",
      },
      keyframes: {
        fadeIn: {
          "0%": { opacity: 0 },
          "100%": { opacity: 1 },
        },
        slideUp: {
          "0%": { opacity: 0, transform: "translateY(12px)" },
          "100%": { opacity: 1, transform: "translateY(0)" },
        },
        shimmer: {
          "0%": { backgroundPosition: "-400px 0" },
          "100%": { backgroundPosition: "400px 0" },
        },
        // Mobile drawer.
        slideInRight: {
          "0%": { transform: "translateX(100%)" },
          "100%": { transform: "translateX(0)" },
        },
      },
      animation: {
        // 200-300ms, ease-out: fast enough to feel responsive, slow enough
        // to read as motion rather than a jump.
        "fade-in": "fadeIn 0.2s ease-out",
        "slide-up": "slideUp 0.25s cubic-bezier(0.16, 1, 0.3, 1)",
        "slide-in-right": "slideInRight 0.28s cubic-bezier(0.16, 1, 0.3, 1)",
        shimmer: "shimmer 1.6s infinite linear",
      },
      // 4.5 is not part of Tailwind's default scale, but several components
      // already used h-4.5/w-4.5 - which silently rendered as no size at all.
      // Defining it makes that existing markup work as intended.
      spacing: {
        4.5: "1.125rem",
      },
    },
  },
  plugins: [],
};
