using eTour.Application.Gateways;
using FluentAssertions;
using NUnit.Framework;

namespace eTour.Tests.Domain;

[TestFixture]
public class CardDetailsTests
{
    [TestCase("4242424242424242", true)] // Visa test number - passes Luhn
    [TestCase("4000000000000002", true)] // Luhn-valid but a "decline" test number (checked elsewhere)
    [TestCase("1234567890123456", false)]
    public void PassesLuhn_DetectsCheckDigit(string number, bool expected)
    {
        var card = new CardDetails(number, "Test Holder", 12, 2030, "123");
        card.PassesLuhn().Should().Be(expected);
    }

    [Test]
    public void Brand_DetectsVisaFromPrefix()
    {
        new CardDetails("4242424242424242", "Holder", 12, 2030, "123").Brand.Should().Be("VISA");
    }

    [Test]
    public void Brand_DetectsMastercardFromPrefix()
    {
        new CardDetails("5105105105105100", "Holder", 12, 2030, "123").Brand.Should().Be("MASTERCARD");
    }

    [Test]
    public void MaskedSummary_ShowsBrandAndLastFourOnly()
    {
        var card = new CardDetails("4242424242424242", "Holder", 12, 2030, "123");
        card.MaskedSummary.Should().Be("VISA ****4242");
    }

    [Test]
    public void IsExpired_PastMonth_ReturnsTrue()
    {
        var card = new CardDetails("4242424242424242", "Holder", 1, 2020, "123");
        card.IsExpired(new DateOnly(2026, 1, 1)).Should().BeTrue();
    }

    [Test]
    public void IsExpired_CurrentMonth_ReturnsFalse()
    {
        var card = new CardDetails("4242424242424242", "Holder", 8, 2026, "123");
        card.IsExpired(new DateOnly(2026, 8, 5)).Should().BeFalse();
    }

    [Test]
    public void HasValidCvv_Amex_Requires4Digits()
    {
        var amex = new CardDetails("340000000000009", "Holder", 12, 2030, "1234");
        amex.HasValidCvv().Should().BeTrue();

        var amexWith3 = new CardDetails("340000000000009", "Holder", 12, 2030, "123");
        amexWith3.HasValidCvv().Should().BeFalse();
    }

    [Test]
    public void HasValidCvv_NonAmex_Requires3Digits()
    {
        var visa = new CardDetails("4242424242424242", "Holder", 12, 2030, "123");
        visa.HasValidCvv().Should().BeTrue();
    }

    [Test]
    public void NumberStripsWhitespaceAndDashes()
    {
        var card = new CardDetails("4242 4242-4242 4242", "Holder", 12, 2030, "123");
        card.Number.Should().Be("4242424242424242");
    }
}
