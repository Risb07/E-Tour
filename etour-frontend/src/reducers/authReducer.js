export const AUTH_ACTIONS = {
  AUTH_START: "AUTH_START",
  AUTH_SUCCESS: "AUTH_SUCCESS",
  AUTH_FAILURE: "AUTH_FAILURE",
  LOGOUT: "LOGOUT",
  CLEAR_ERROR: "CLEAR_ERROR",
};

export const initialAuthState = {
  user: null,
  token: null,
  status: "idle", // 'idle' | 'loading' | 'succeeded' | 'failed'
  error: null,
};

export function authReducer(state, action) {
  switch (action.type) {
    case AUTH_ACTIONS.AUTH_START:
      return { ...state, status: "loading", error: null };

    case AUTH_ACTIONS.AUTH_SUCCESS:
      return {
        ...state,
        status: "succeeded",
        user: action.payload.user,
        token: action.payload.token,
        error: null,
      };

    case AUTH_ACTIONS.AUTH_FAILURE:
      return { ...state, status: "failed", error: action.payload };

    case AUTH_ACTIONS.CLEAR_ERROR:
      return { ...state, status: "idle", error: null };

    case AUTH_ACTIONS.LOGOUT:
      return { ...initialAuthState };

    default:
      return state;
  }
}
