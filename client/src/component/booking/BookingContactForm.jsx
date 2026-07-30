import { User, Mail, Phone } from "lucide-react";

function BookingContactForm({
  customer,
  setCustomer,
  errors = {},
  disabled = false,
}) {
  function handleChange(e) {
    const { name, value } = e.target;

    setCustomer((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-md">
      {/* Heading */}

      <div className="mb-6">
        <h2 className="text-2xl font-bold text-slate-900">Booking Contact</h2>

        <p className="mt-1 text-sm text-slate-500">
          Booking confirmation and travel updates will be sent to these details.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
        {/* Full Name */}

        <div className="md:col-span-2">
          <label className="mb-2 block text-sm font-medium text-slate-700">
            Full Name <span className="text-red-500">*</span>
          </label>

          <div className="relative">
            <User
              size={18}
              className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
            />

            <input
              type="text"
              name="fullName"
              placeholder="Enter Full Name"
              value={customer.fullName}
              onChange={handleChange}
              disabled={disabled}
              className={`h-11 w-full rounded-xl border pl-11 pr-4 outline-none transition
                ${
                  errors.fullName
                    ? "border-red-500 focus:ring-red-500"
                    : "border-slate-300 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
                }`}
            />
          </div>

          {errors.fullName && (
            <p className="mt-1 text-sm text-red-500">{errors.fullName}</p>
          )}
        </div>

        {/* Email */}

        <div>
          <label className="mb-2 block text-sm font-medium text-slate-700">
            Email Address <span className="text-red-500">*</span>
          </label>

          <div className="relative">
            <Mail
              size={18}
              className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
            />

            <input
              type="email"
              name="email"
              placeholder="example@email.com"
              value={customer.email}
              onChange={handleChange}
              disabled={disabled}
              className={`h-11 w-full rounded-xl border pl-11 pr-4 outline-none transition
                ${
                  errors.email
                    ? "border-red-500 focus:ring-red-500"
                    : "border-slate-300 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
                }`}
            />
          </div>

          {errors.email && (
            <p className="mt-1 text-sm text-red-500">{errors.email}</p>
          )}
        </div>

        {/* Phone */}

        <div>
          <label className="mb-2 block text-sm font-medium text-slate-700">
            Phone Number <span className="text-red-500">*</span>
          </label>

          <div className="relative">
            <Phone
              size={18}
              className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"
            />

            <input
              type="tel"
              name="phone"
              placeholder="+91 9876543210"
              value={customer.phone}
              onChange={handleChange}
              disabled={disabled}
              maxLength={10}
              className={`h-11 w-full rounded-xl border pl-11 pr-4 outline-none transition
                ${
                  errors.phone
                    ? "border-red-500 focus:ring-red-500"
                    : "border-slate-300 focus:border-blue-500 focus:ring-2 focus:ring-blue-500"
                }`}
            />
          </div>

          {errors.phone && (
            <p className="mt-1 text-sm text-red-500">{errors.phone}</p>
          )}
        </div>
      </div>

      {/* Information Box */}

      <div className="mt-6 rounded-xl border border-blue-100 bg-blue-50 p-4">
        <p className="text-sm text-blue-700">
          Please provide a valid email address and phone number. Your booking
          confirmation, e-ticket, payment receipt and future travel updates will
          be sent here.
        </p>
      </div>
    </div>
  );
}

export default BookingContactForm;
