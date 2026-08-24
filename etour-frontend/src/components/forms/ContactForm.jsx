import { useState } from "react";
import { Send, CheckCircle2 } from "lucide-react";
import { submitContactEnquiry } from "../../services/contactService";
import { useToast } from "../../hooks/useToast";
import Input from "../common/Input";
import Button from "../common/Button";

const emptyEnquiry = () => ({
  name: "",
  email: "",
  phone: "",
  subject: "",
  message: "",
});

/**
 * Public contact form. Field names match ContactEnquiryRequest exactly, which
 * is what lets a 400 from bean validation be mapped straight back onto the
 * inputs that caused it.
 */
export default function ContactForm() {
  const { showToast } = useToast();
  const [form, setForm] = useState(emptyEnquiry);
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isSent, setIsSent] = useState(false);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
    // Clear the field's error as soon as the user starts fixing it, rather
    // than leaving stale red text until the next submit.
    setErrors((current) => (current[field] ? { ...current, [field]: "" } : current));
  }

  /** Mirrors the server rules so the common mistakes never cost a round trip. */
  function validate() {
    const next = {
      name: form.name.trim() ? "" : "Full name is required",
      email: !form.email.trim()
        ? "Email is required"
        : /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())
          ? ""
          : "Enter a valid email address",
      // Optional - only checked when something was typed.
      phone: !form.phone.trim() || /^[0-9]{10}$/.test(form.phone.trim())
        ? ""
        : "Phone number must contain exactly 10 digits",
      subject: form.subject.trim() ? "" : "Subject is required",
      message: form.message.trim() ? "" : "Message is required",
    };
    setErrors(next);
    return Object.values(next).every((message) => !message);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    try {
      await submitContactEnquiry({
        name: form.name.trim(),
        email: form.email.trim(),
        phone: form.phone.trim(),
        subject: form.subject.trim(),
        message: form.message.trim(),
      });

      setForm(emptyEnquiry());
      setErrors({});
      setIsSent(true);
      showToast("Thanks for getting in touch - we'll reply soon.", "success");
    } catch (err) {
      // Bean validation failures come back as a flat { field: message } map
      // (see GlobalExceptionHandler), carried on the ApiError as `details`.
      // Anything else is a real failure and gets surfaced as a toast.
      const fieldErrors = err.status === 400 && err.details && typeof err.details === "object"
        ? Object.fromEntries(
            Object.entries(err.details).filter(([, message]) => typeof message === "string")
          )
        : null;

      if (fieldErrors && Object.keys(fieldErrors).length > 0) {
        setErrors(fieldErrors);
        showToast("Please correct the highlighted fields.", "error");
      } else {
        showToast(err.message || "Couldn't send your message. Please try again.", "error");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  if (isSent) {
    return (
      <div className="rounded-card bg-white p-8 text-center shadow-card">
        <CheckCircle2 className="mx-auto h-10 w-10 text-emerald-500" />
        <h3 className="mt-3 font-display text-xl font-bold text-ink-900">Message sent</h3>
        <p className="mt-2 text-sm text-ink-500">
          Thanks for writing in. Our team will get back to you within one working day.
        </p>
        <Button variant="outline" className="mt-6" onClick={() => setIsSent(false)}>
          Send another message
        </Button>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="rounded-card bg-white p-6 shadow-card sm:p-8">
      <h2 className="font-display text-xl font-bold text-ink-900">Send us a message</h2>
      <p className="mt-1 text-sm text-ink-500">
        Fill in the form and we&apos;ll get back to you within one working day.
      </p>

      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Input
          label="Full name"
          required
          value={form.name}
          onChange={(e) => update("name", e.target.value)}
          error={errors.name}
          autoComplete="name"
          disabled={isSubmitting}
        />
        <Input
          label="Email"
          type="email"
          required
          value={form.email}
          onChange={(e) => update("email", e.target.value)}
          error={errors.email}
          autoComplete="email"
          disabled={isSubmitting}
        />
        <Input
          label="Phone number"
          placeholder="10 digits"
          value={form.phone}
          onChange={(e) => update("phone", e.target.value)}
          error={errors.phone}
          autoComplete="tel"
          disabled={isSubmitting}
        />
        <Input
          label="Subject"
          required
          value={form.subject}
          onChange={(e) => update("subject", e.target.value)}
          error={errors.subject}
          disabled={isSubmitting}
        />
      </div>

      {/* No shared Textarea component exists, so this matches Input's markup
          and classes rather than introducing a differently-styled control. */}
      <div className="mt-4 flex flex-col gap-1.5">
        <label htmlFor="contact-message" className="text-sm font-medium text-ink-700">
          Message
          <span className="text-amber-600"> *</span>
        </label>
        <textarea
          id="contact-message"
          rows={5}
          value={form.message}
          onChange={(e) => update("message", e.target.value)}
          disabled={isSubmitting}
          aria-invalid={Boolean(errors.message)}
          aria-describedby={errors.message ? "contact-message-error" : undefined}
          placeholder="Tell us what you're planning, or what you'd like to know."
          className={[
            "w-full rounded-xl border bg-white px-3.5 py-2.5 text-sm text-ink-900 placeholder:text-ink-300",
            "transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-400",
            isSubmitting && "cursor-not-allowed bg-ink-50 text-ink-400",
            errors.message ? "border-red-400" : "border-ink-200 focus:border-amber-400",
          ]
            .filter(Boolean)
            .join(" ")}
        />
        {errors.message && (
          <p id="contact-message-error" className="text-xs font-medium text-red-600">
            {errors.message}
          </p>
        )}
      </div>

      <Button
        type="submit"
        size="lg"
        icon={Send}
        isLoading={isSubmitting}
        disabled={isSubmitting}
        className="mt-6 w-full sm:w-auto"
      >
        {isSubmitting ? "Sending..." : "Send message"}
      </Button>
    </form>
  );
}
