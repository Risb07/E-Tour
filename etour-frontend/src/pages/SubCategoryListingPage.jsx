import { useNavigate, useParams } from "react-router-dom";
import { useCategories } from "../hooks/useCategories";
import { ROUTE_PATHS } from "../constants/routes";
import Breadcrumb from "../components/ui/Breadcrumb";
import CategoryCard from "../components/ui/CategoryCard";
import EmptyState from "../components/common/EmptyState";
import Loader from "../components/common/Loader";
import { Compass } from "lucide-react";

export default function SubCategoryListingPage() {
  const { categoryId } = useParams();
  const navigate = useNavigate();
  const { getCategoryById, getSubCategories, status } = useCategories();

  const category = getCategoryById(categoryId);
  const subCategories = getSubCategories(Number(categoryId));

  if (status === "loading") {
    return <Loader label="Loading categories..." />;
  }

  if (!category) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <EmptyState
          icon={Compass}
          title="Category not found"
          description="This category may have been removed."
          primaryAction={{ label: "Back to home", onClick: () => navigate(ROUTE_PATHS.HOME) }}
        />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <Breadcrumb items={[{ label: category.categoryName }]} />
      <h1 className="mt-3 font-display text-2xl font-bold text-ink-900 sm:text-3xl">{category.categoryName}</h1>
      <p className="mt-1 text-sm text-ink-500">Choose a sub-category to see its tours.</p>

      {subCategories.length === 0 ? (
        <div className="mt-10">
          <EmptyState title="No sub-categories here" description="This category has no sub-categories configured." />
        </div>
      ) : (
        <div className="mt-8 grid grid-cols-2 gap-5 sm:grid-cols-3 lg:grid-cols-4">
          {subCategories.map((subCategory) => (
            <CategoryCard
              key={subCategory.categoryId}
              category={subCategory}
              onClick={() => navigate(ROUTE_PATHS.tourListing(subCategory.categoryId))}
            />
          ))}
        </div>
      )}
    </div>
  );
}
