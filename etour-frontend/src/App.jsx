import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ToastProvider } from "./context/ToastContext";
import { CategoriesProvider } from "./context/CategoriesContext";
import { BookingFlowProvider } from "./context/BookingFlowContext";
import { WishlistProvider } from "./context/WishlistContext";
import { ContentProvider } from "./context/ContentContext";
import AppRoutes from "./routes/AppRoutes";

export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        {/* Site text/images come from the `content` table - loaded once here
            so no page hard-codes copy or fires its own request. */}
        <ContentProvider>
          <AuthProvider>
            {/* WishlistProvider sits inside AuthProvider - it reads auth state
                to decide whether to load the wishlist at all. */}
            <WishlistProvider>
              <CategoriesProvider>
                <BookingFlowProvider>
                  <AppRoutes />
                </BookingFlowProvider>
              </CategoriesProvider>
            </WishlistProvider>
          </AuthProvider>
        </ContentProvider>
      </ToastProvider>
    </BrowserRouter>
  );
}
