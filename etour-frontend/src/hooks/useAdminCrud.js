import { useCallback, useEffect, useState } from "react";
import { useToast } from "./useToast";

/**
 * Shared state machine for admin CRUD pages: fetch list, create, update,
 * delete - each mutation toasts on success and reloads the list. Pages pass
 * their domain service functions; the hook handles the plumbing.
 */
export function useAdminCrud({ fetchAll, create, update, remove, noun = "record" }) {
  const [items, setItems] = useState([]);
  const [status, setStatus] = useState("loading");
  const [isMutating, setIsMutating] = useState(false);
  const { showToast } = useToast();

  const load = useCallback(async () => {
    setStatus("loading");
    try {
      setItems(await fetchAll());
      setStatus("succeeded");
    } catch (err) {
      setStatus("failed");
    }
  }, [fetchAll]);

  useEffect(() => {
    load();
  }, [load]);

  async function run(mutation, successMessage) {
    setIsMutating(true);
    try {
      await mutation();
      showToast(successMessage, "success");
      await load();
      return true;
    } catch (err) {
      showToast(err.message || `Couldn't save the ${noun.toLowerCase()}.`, "error");
      return false;
    } finally {
      setIsMutating(false);
    }
  }

  const createItem = (payload) => run(() => create(payload), `${noun} created.`);
  const updateItem = (id, payload) => run(() => update(id, payload), `${noun} updated.`);
  const removeItem = (id) => run(() => remove(id), `${noun} deleted.`);

  return { items, status, isMutating, load, createItem, updateItem, removeItem };
}
