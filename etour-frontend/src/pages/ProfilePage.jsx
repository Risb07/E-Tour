import { useEffect, useState } from "react";
import { User, Mail, Phone, Save } from "lucide-react";
import { fetchMyProfile, updateMyProfile } from "../services/customerService";
import { useToast } from "../hooks/useToast";
import { validateRequired, validatePhone } from "../utils/validators";
import Input from "../components/common/Input";
import Button from "../components/common/Button";
import Loader from "../components/common/Loader";
import ErrorState from "../components/common/ErrorState";

export default function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ fullName: "", phone: "" });
  const [errors, setErrors] = useState({});
  const [status, setStatus] = useState("loading");
  const [isSaving, setIsSaving] = useState(false);
  const { showToast } = useToast();

  useEffect(() => {
    fetchMyProfile()
      .then((data) => {
        setProfile(data);
        setForm({ fullName: data.fullName, phone: data.phone });
        setStatus("succeeded");
      })
      .catch(() => setStatus("failed"));
  }, []);

  function handleChange(field) {
    return (e) => {
      setForm((current) => ({ ...current, [field]: e.target.value }));
      setErrors((current) => ({ ...current, [field]: "" }));
    };
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const nextErrors = {
      fullName: validateRequired(form.fullName, "Full name"),
      phone: validatePhone(form.phone),
    };
    setErrors(nextErrors);
    if (Object.values(nextErrors).some(Boolean)) return;

    setIsSaving(true);
    try {
      const updated = await updateMyProfile(form);
      setProfile(updated);
      showToast("Profile updated.", "success");
    } catch (err) {
      showToast(err.message || "Couldn't update your profile.", "error");
    } finally {
      setIsSaving(false);
    }
  }

  if (status === "loading") return <Loader label="Loading profile..." />;
  if (status === "failed") {
    return (
      <div className="mx-auto max-w-lg px-4 py-16">
        <ErrorState variant="server" message="Couldn't load your profile." />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-lg px-4 py-10 sm:px-6">
      <h1 className="font-display text-2xl font-bold text-ink-900">My Profile</h1>
      <p className="mt-1 text-sm text-ink-500">Update your contact details.</p>

      <form onSubmit={handleSubmit} className="mt-6 flex flex-col gap-5 rounded-card bg-white p-6 shadow-card">
        <Input
          label="Full name"
          required
          icon={User}
          value={form.fullName}
          onChange={handleChange("fullName")}
          error={errors.fullName}
        />
        <Input label="Email" icon={Mail} value={profile.email} disabled onChange={() => {}} />
        <p className="-mt-3 text-xs text-ink-400">
          Email cannot be changed here since it is tied to your login.
        </p>
        <Input
          label="Phone number"
          required
          icon={Phone}
          value={form.phone}
          onChange={handleChange("phone")}
          error={errors.phone}
        />

        <Button type="submit" icon={Save} isLoading={isSaving} className="mt-1">
          Save changes
        </Button>
      </form>
    </div>
  );
}
