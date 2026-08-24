import { createContext, useReducer, useCallback, useMemo, useEffect } from "react";
import { categoriesReducer, initialCategoriesState, CATEGORIES_ACTIONS } from "../reducers/categoriesReducer";
import { fetchCategories } from "../services/categoryService";

export const CategoriesContext = createContext(null);

export function CategoriesProvider({ children }) {
  const [state, dispatch] = useReducer(categoriesReducer, initialCategoriesState);

  const loadCategories = useCallback(async () => {
    dispatch({ type: CATEGORIES_ACTIONS.FETCH_START });
    try {
      const data = await fetchCategories();
      dispatch({ type: CATEGORIES_ACTIONS.FETCH_SUCCESS, payload: data });
    } catch (err) {
      dispatch({ type: CATEGORIES_ACTIONS.FETCH_FAILURE, payload: err.message });
    }
  }, []);

  // Fetch once on app load - avoids every page that needs categories
  // (Home, Tour Listing filters, SubCategory page) re-fetching the same
  // rarely-changing list.
  useEffect(() => {
    loadCategories();
  }, [loadCategories]);

  // Top-level categories have no parentCategory. A category "has
  // subcategories" if any other category's parentCategoryId points at it -
  // this mirrors the backend-driven branching the BRD requires (never
  // hardcode which categories have children).
  const topLevelCategories = useMemo(
    () => state.items.filter((category) => !category.parentCategory),
    [state.items]
  );

  const getSubCategories = useCallback(
    (categoryId) => state.items.filter((category) => category.parentCategory?.categoryId === categoryId),
    [state.items]
  );

  const hasSubCategories = useCallback(
    (categoryId) => getSubCategories(categoryId).length > 0,
    [getSubCategories]
  );

  const getCategoryById = useCallback(
    (categoryId) => state.items.find((category) => category.categoryId === Number(categoryId)),
    [state.items]
  );

  const value = useMemo(
    () => ({
      allCategories: state.items,
      topLevelCategories,
      status: state.status,
      error: state.error,
      getSubCategories,
      hasSubCategories,
      getCategoryById,
      reload: loadCategories,
    }),
    [state, topLevelCategories, getSubCategories, hasSubCategories, getCategoryById, loadCategories]
  );

  return <CategoriesContext.Provider value={value}>{children}</CategoriesContext.Provider>;
}
