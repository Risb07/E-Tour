import { useContext } from "react";
import { CategoriesContext } from "../context/CategoriesContext";

export function useCategories() {
  const context = useContext(CategoriesContext);
  if (!context) {
    throw new Error("useCategories must be used within a CategoriesProvider");
  }
  return context;
}
