export const CATEGORIES_ACTIONS = {
  FETCH_START: "FETCH_START",
  FETCH_SUCCESS: "FETCH_SUCCESS",
  FETCH_FAILURE: "FETCH_FAILURE",
};

export const initialCategoriesState = {
  items: [],
  status: "idle", // 'idle' | 'loading' | 'succeeded' | 'failed'
  error: null,
};

export function categoriesReducer(state, action) {
  switch (action.type) {
    case CATEGORIES_ACTIONS.FETCH_START:
      return { ...state, status: "loading", error: null };
    case CATEGORIES_ACTIONS.FETCH_SUCCESS:
      return { ...state, status: "succeeded", items: action.payload };
    case CATEGORIES_ACTIONS.FETCH_FAILURE:
      return { ...state, status: "failed", error: action.payload };
    default:
      return state;
  }
}
