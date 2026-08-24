using System.Text.RegularExpressions;

namespace eTour.Application.Gateways;

/// <summary>Card data passed to a gateway for a single charge. Deliberately not persisted anywhere.</summary>
public partial class CardDetails
{
    private const string DeclineCardPrefix = "4000000000000002";

    public CardDetails(string? number, string holderName, int expiryMonth, int expiryYear, string cvv)
    {
        Number = number is null ? null : WhitespaceOrDash().Replace(number, "");
        HolderName = holderName;
        ExpiryMonth = expiryMonth;
        ExpiryYear = expiryYear;
        Cvv = cvv;
    }

    public string? Number { get; }
    public string HolderName { get; }
    public int ExpiryMonth { get; }
    public int ExpiryYear { get; }
    public string Cvv { get; }

    /// <summary>Last four digits - the only part of the number safe to keep.</summary>
    public string Last4 => Number is null || Number.Length < 4 ? "____" : Number[^4..];

    /// <summary>Brand inferred from the IIN/BIN prefix. Presentational only.</summary>
    public string Brand
    {
        get
        {
            if (string.IsNullOrEmpty(Number))
            {
                return "CARD";
            }
            if (Number.StartsWith('4'))
            {
                return "VISA";
            }
            if (MastercardPrefix().IsMatch(Number))
            {
                return "MASTERCARD";
            }
            if (AmexPrefix().IsMatch(Number))
            {
                return "AMEX";
            }
            if (Number.StartsWith('6'))
            {
                return "DISCOVER";
            }
            if (Number.StartsWith("35"))
            {
                return "JCB";
            }
            if (RupayPrefix().IsMatch(Number))
            {
                return "RUPAY";
            }
            return "CARD";
        }
    }

    /// <summary>e.g. "VISA ****4242" - safe to store and display.</summary>
    public string MaskedSummary => $"{Brand} ****{Last4}";

    /// <summary>Luhn check digit validation.</summary>
    public bool PassesLuhn()
    {
        if (Number is null || !DigitsOnly12To19().IsMatch(Number))
        {
            return false;
        }

        var sum = 0;
        var doubleIt = false;
        for (var i = Number.Length - 1; i >= 0; i--)
        {
            var digit = Number[i] - '0';
            if (doubleIt)
            {
                digit *= 2;
                if (digit > 9)
                {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    /// <summary>A card is valid through the LAST day of its expiry month (YearMonth comparison, not exact day).</summary>
    public bool IsExpired(DateOnly asOf)
    {
        if (ExpiryMonth is < 1 or > 12)
        {
            return true;
        }
        return ExpiryYear * 12 + ExpiryMonth < asOf.Year * 12 + asOf.Month;
    }

    /// <summary>AMEX uses a 4-digit CID; everything else uses 3.</summary>
    public bool HasValidCvv()
    {
        if (Cvv is null)
        {
            return false;
        }
        var expected = Brand == "AMEX" ? 4 : 3;
        return Cvv.Length == expected && Cvv.All(char.IsDigit);
    }

    public override string ToString() => $"CardDetails{{{MaskedSummary}}}";

    [GeneratedRegex(@"\s|-")]
    private static partial Regex WhitespaceOrDash();

    [GeneratedRegex("^5[1-5]|^2[2-7]")]
    private static partial Regex MastercardPrefix();

    [GeneratedRegex("^3[47]")]
    private static partial Regex AmexPrefix();

    [GeneratedRegex("^(60|65|81|82|508)")]
    private static partial Regex RupayPrefix();

    [GeneratedRegex(@"^\d{12,19}$")]
    private static partial Regex DigitsOnly12To19();
}
