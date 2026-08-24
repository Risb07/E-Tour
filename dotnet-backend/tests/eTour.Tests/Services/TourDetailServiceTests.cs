using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Domain.Exceptions;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using NUnit.Framework;

namespace eTour.Tests.Services;

/// <summary>
/// The tour-page detail tabs, pinned against the Java backend's behaviour in
/// <c>TourDetailServiceImpl</c>. Both backends run against the same MySQL schema, so a
/// delete that means "soft" on one side and "hard" on the other is not a style difference -
/// it decides whether a row the admin removed under Java is still on the tour page under .NET.
///
/// Runs on the real EF pipeline (in-memory provider) rather than mocks, so the LINQ that
/// enforces the status filter is actually executed.
/// </summary>
[TestFixture]
public class TourDetailServiceTests
{
    private EtourDbContext _context = null!;
    private TourDetailService _service = null!;
    private long _tourId;
    private long _otherTourId;

    [SetUp]
    public async Task SetUp()
    {
        _context = TestDbContextFactory.Create();
        var mapper = TestMapper.Create();

        _service = new TourDetailService(
            new GenericRepository<Tour, long>(_context),
            new GenericRepository<JourneyDetail, long>(_context),
            new GenericRepository<StayMeal, long>(_context),
            new GenericRepository<TourContent, long>(_context),
            new GenericRepository<TourMedia, long>(_context),
            new GenericRepository<TourAddon, long>(_context),
            new GenericRepository<Itinerary, long>(_context),
            new GenericRepository<Location, long>(_context),
            mapper);

        var tour = NewTour("Kerala Backwaters");
        var other = NewTour("Rajasthan Heritage");
        _context.Tours.AddRange(tour, other);
        await _context.SaveChangesAsync();
        _tourId = tour.TourId;
        _otherTourId = other.TourId;
    }

    [TearDown]
    public void TearDown() => _context.Dispose();

    private static Tour NewTour(string title) => new()
    {
        Title = title,
        Description = "d",
        DurationDays = 5,
        BasePrice = 1000,
        TourCode = TourCode.DOM,
        Status = TourStatus.ACTIVE
    };

    // ---------------------------------------------------------------- content

    [Test]
    public async Task UpsertContent_SavingTheSameTabTwice_EditsTheRowInsteadOfDuplicatingIt()
    {
        // The admin "Good to know" form has one editor per tab and re-POSTs it on every save.
        // Inserting unconditionally would render the section twice on the tour page.
        await _service.UpsertContentAsync(_tourId, ContentDto("WEATHER", "Warm and dry in June."));
        await _service.UpsertContentAsync(_tourId, ContentDto("WEATHER", "Monsoon from July."));

        var stored = _context.TourContents.Where(c => c.TourId == _tourId).ToList();
        stored.Should().ContainSingle();
        stored[0].ContentText.Should().Be("Monsoon from July.");
    }

    [Test]
    public async Task UpsertContent_WhenTheClientOmitsTheLanguage_DefaultsToEnglish()
    {
        // Not cosmetic: the upsert looks the row up by (tour, type, language), so a null language
        // here and "en" on the next save would miss the match and insert a second row.
        var dto = ContentDto("DOS_DONTS", "Remove shoes at temples.");
        dto.LanguageCode = null!;

        var saved = await _service.UpsertContentAsync(_tourId, dto);

        saved.LanguageCode.Should().Be("en");
        _context.TourContents.Single(c => c.TourId == _tourId).LanguageCode.Should().Be("en");
    }

    [Test]
    public async Task UpsertContent_ForAnUnknownTour_Throws()
    {
        var act = async () => await _service.UpsertContentAsync(9999, ContentDto("WEATHER", "irrelevant"));

        await act.Should().ThrowAsync<ResourceNotFoundException>();
        _context.TourContents.Should().BeEmpty();
    }

    [Test]
    public async Task DeleteContent_SoftDeletes_SoGetContentStopsReturningTheRow()
    {
        var saved = await _service.UpsertContentAsync(_tourId, ContentDto("WEATHER", "Warm and dry."));

        await _service.DeleteContentAsync(_tourId, saved.TourContentId);

        (await _service.GetContentAsync(_tourId)).Should().BeEmpty();
        // The row itself survives - that is what makes it a soft delete rather than a DELETE.
        _context.TourContents.Single(c => c.TourContentId == saved.TourContentId).Status.Should().BeFalse();
    }

    [Test]
    public async Task UpsertContent_AfterARemove_RevivesTheRow()
    {
        // "Remove" then "Save" is the natural way to re-add a tab in the admin form. Honouring the
        // DTO's status instead of forcing true would leave the row soft-deleted and invisible.
        var saved = await _service.UpsertContentAsync(_tourId, ContentDto("TERMS_CONDITIONS", "Free cancellation."));
        await _service.DeleteContentAsync(_tourId, saved.TourContentId);

        await _service.UpsertContentAsync(_tourId, ContentDto("TERMS_CONDITIONS", "30-day cancellation."));

        var live = await _service.GetContentAsync(_tourId);
        live.Should().ContainSingle().Which.ContentText.Should().Be("30-day cancellation.");
    }

    [Test]
    public async Task DeleteContent_ForARowBelongingToAnotherTour_Throws()
    {
        // Guards /api/tours/{other}/content/{id} against deleting this tour's row by guessing an id.
        var saved = await _service.UpsertContentAsync(_tourId, ContentDto("WEATHER", "Warm."));

        var act = async () => await _service.DeleteContentAsync(_otherTourId, saved.TourContentId);

        await act.Should().ThrowAsync<ResourceNotFoundException>();
        _context.TourContents.Single(c => c.TourContentId == saved.TourContentId).Status.Should().BeTrue();
    }

    [Test]
    public async Task DeleteContent_ForAMissingRow_Throws()
    {
        var act = async () => await _service.DeleteContentAsync(_tourId, 404);

        await act.Should().ThrowAsync<ResourceNotFoundException>();
    }

    // ------------------------------------------------------------- stay/meals

    [Test]
    public async Task DeleteStayMeal_HardDeletesTheRow_BecauseStayMealHasNoStatusColumn()
    {
        var saved = await _service.AddStayMealAsync(_tourId, new StayMealDto { DayNumber = 1, HotelName = "Hotel Rajmahal" });

        await _service.DeleteStayMealAsync(_tourId, saved.StayMealId);

        (await _service.GetStayMealsAsync(_tourId)).Should().BeEmpty();
        _context.StayMeals.Should().BeEmpty();
    }

    [Test]
    public async Task DeleteStayMeal_ForARowBelongingToAnotherTour_Throws()
    {
        var saved = await _service.AddStayMealAsync(_tourId, new StayMealDto { DayNumber = 1, HotelName = "Hotel Rajmahal" });

        var act = async () => await _service.DeleteStayMealAsync(_otherTourId, saved.StayMealId);

        await act.Should().ThrowAsync<ResourceNotFoundException>();
        _context.StayMeals.Should().ContainSingle();
    }

    [Test]
    public async Task DeleteStayMeal_ForAMissingRow_Throws()
    {
        var act = async () => await _service.DeleteStayMealAsync(_tourId, 404);

        await act.Should().ThrowAsync<ResourceNotFoundException>();
    }

    // ------------------------------------------------------------------ media

    [Test]
    public async Task DeleteMedia_SoftDeletes_SoGetMediaStopsReturningTheImage()
    {
        var saved = await _service.AddMediaAsync(_tourId, new TourMediaDto
        {
            FilePath = "/uploads/tours/beach.jpg",
            MediaType = "IMAGE",
            TabContext = "GALLERY"
        });

        await _service.DeleteMediaAsync(_tourId, saved.MediaId);

        (await _service.GetMediaAsync(_tourId)).Should().BeEmpty();
        _context.TourMedia.Single(m => m.MediaId == saved.MediaId).Status.Should().BeFalse();
    }

    [Test]
    public async Task DeleteMedia_ForMediaBelongingToAnotherTour_Throws()
    {
        var saved = await _service.AddMediaAsync(_tourId, new TourMediaDto
        {
            FilePath = "/uploads/tours/beach.jpg",
            MediaType = "IMAGE",
            TabContext = "GALLERY"
        });

        var act = async () => await _service.DeleteMediaAsync(_otherTourId, saved.MediaId);

        await act.Should().ThrowAsync<ResourceNotFoundException>();
        _context.TourMedia.Single(m => m.MediaId == saved.MediaId).Status.Should().BeTrue();
    }

    // --------------------------------------------------------------- add-ons

    [Test]
    public async Task DeleteAddon_SoftDeletes_SoPastBookingsKeepTheirCostBreakdown()
    {
        // Bookings reference add-ons by id. A hard delete would leave a booking's line items
        // pointing at a row that no longer exists.
        var saved = await _service.AddAddonAsync(_tourId, new TourAddonDto
        {
            AddonName = "Airport transfer",
            Price = 500,
            PriceType = "PER_PERSON"
        });

        await _service.DeleteAddonAsync(_tourId, saved.AddonId);

        (await _service.GetAddonsAsync(_tourId)).Should().BeEmpty();
        _context.TourAddons.Single(a => a.AddonId == saved.AddonId).Status.Should().BeFalse();
    }

    [Test]
    public async Task DeleteAddon_ForAnAddonBelongingToAnotherTour_Throws()
    {
        var saved = await _service.AddAddonAsync(_tourId, new TourAddonDto
        {
            AddonName = "Airport transfer",
            Price = 500,
            PriceType = "PER_PERSON"
        });

        var act = async () => await _service.DeleteAddonAsync(_otherTourId, saved.AddonId);

        await act.Should().ThrowAsync<ResourceNotFoundException>();
        _context.TourAddons.Single(a => a.AddonId == saved.AddonId).Status.Should().BeTrue();
    }

    private static TourContentDto ContentDto(string contentType, string text) => new()
    {
        ContentType = contentType,
        ContentText = text,
        LanguageCode = "en"
    };
}
