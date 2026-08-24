import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import Footer from "./Footer";

/** Shell for every public/customer-facing page - Auth pages use AuthLayout instead. */
export default function MainLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-ink-50">
      {/* First tab stop: lets keyboard users skip the nav on every page. */}
      <a href="#main-content" className="skip-link">
        Skip to content
      </a>
      <Navbar />
      <main id="main-content" tabIndex={-1} className="flex-1 focus:outline-none">
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
