import { useNavigate } from "react-router-dom";
import ErrorState from "../components/common/ErrorState";
import { ROUTE_PATHS } from "../constants/routes";

export default function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <div className="min-h-screen bg-ink-50 flex items-center justify-center p-6">
      <ErrorState variant="notFound" onRetry={() => navigate(ROUTE_PATHS.HOME)} retryLabel="Back to home" />
    </div>
  );
}
