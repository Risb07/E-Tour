import { createContext, useReducer, useEffect, useCallback, useMemo, useState } from "react";
import { authReducer, initialAuthState, AUTH_ACTIONS } from "../reducers/authReducer";
import { loginUser, registerUser } from "../services/authService";
import { saveSession, loadSession, clearSession } from "../utils/storage";

export const AuthContext = createContext(null);

function toUser(apiResponse) {
  return {
    userId: apiResponse.userId,
    firstName: apiResponse.firstName,
    lastName: apiResponse.lastName,
    email: apiResponse.email,
    role: apiResponse.role,
  };
}

export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, initialAuthState);
  const [isInitializing, setIsInitializing] = useState(true);

  // Rehydrate on first mount so a page refresh doesn't log the user out.
  useEffect(() => {
    const session = loadSession();
    if (session) {
      dispatch({
        type: AUTH_ACTIONS.AUTH_SUCCESS,
        payload: { user: session.user, token: session.token },
      });
    }
    // Runs regardless of whether a session was found - this is what lets
    // ProtectedRoute distinguish "still checking" from "checked, logged out".
    setIsInitializing(false);
  }, []);

  const login = useCallback(async (credentials) => {
    dispatch({ type: AUTH_ACTIONS.AUTH_START });
    try {
      const response = await loginUser(credentials);
      const user = toUser(response);
      saveSession(response.token, user);
      dispatch({ type: AUTH_ACTIONS.AUTH_SUCCESS, payload: { user, token: response.token } });
      return user;
    } catch (err) {
      dispatch({ type: AUTH_ACTIONS.AUTH_FAILURE, payload: err.message });
      throw err;
    }
  }, []);

  const register = useCallback(async (payload) => {
    dispatch({ type: AUTH_ACTIONS.AUTH_START });
    try {
      // Registration now logs the user in immediately: the backend returns
      // the same JWT + profile shape as /login, so the session is stored
      // right away and the caller can redirect straight to the site.
      const response = await registerUser(payload);
      const user = toUser(response);
      if (response.token) {
        saveSession(response.token, user);
        dispatch({ type: AUTH_ACTIONS.AUTH_SUCCESS, payload: { user, token: response.token } });
      } else {
        dispatch({ type: AUTH_ACTIONS.CLEAR_ERROR });
      }
      return user;
    } catch (err) {
      dispatch({ type: AUTH_ACTIONS.AUTH_FAILURE, payload: err.message });
      throw err;
    }
  }, []);

  /**
   * Establishes a session from a token that was obtained outside the normal
   * form submit - currently only Google sign-in, where the backend completes
   * the OAuth 2.0 exchange and hands the SPA a finished JWT on the callback
   * URL.
   *
   * The token is the *same* JWT `/api/auth/login` returns, so from here on
   * everything behaves identically to a password login: same storage, same
   * reducer action, same `isAuthenticated` derivation.
   *
   * @param {string} token
   * @param {{userId: number, firstName: string, lastName: string, email: string, role: string}} profile
   */
  const loginWithToken = useCallback((token, profile) => {
    if (!token) {
      throw new Error("Cannot establish a session without a token.");
    }
    const user = toUser(profile);
    saveSession(token, user);
    dispatch({ type: AUTH_ACTIONS.AUTH_SUCCESS, payload: { user, token } });
    return user;
  }, []);

  const logout = useCallback(() => {
    clearSession();
    dispatch({ type: AUTH_ACTIONS.LOGOUT });
  }, []);

  const value = useMemo(
    () => ({
      user: state.user,
      token: state.token,
      status: state.status,
      error: state.error,
      isAuthenticated: Boolean(state.user && state.token),
      isInitializing,
      login,
      loginWithToken,
      register,
      logout,
    }),
    [state, isInitializing, login, loginWithToken, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
