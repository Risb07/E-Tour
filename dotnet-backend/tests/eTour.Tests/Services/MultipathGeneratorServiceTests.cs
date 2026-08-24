using eTour.Application.Services;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using Microsoft.Extensions.Logging.Abstractions;
using NUnit.Framework;

namespace eTour.Tests.Services;

[TestFixture]
public class MultipathGeneratorServiceTests
{
    private EtourDbContext _context = null!;
    private MultipathGeneratorService _service = null!;
    private Category _category = null!;
    private Tour _matchingTour = null!;
    private Tour _draftTour = null!;

    [SetUp]
    public async Task SetUp()
    {
        _context = TestDbContextFactory.Create();

        _category = new Category { CategoryName = "Europe", Status = true };
        _context.Categories.Add(_category);

        _matchingTour = new Tour { Title = "Paris Explorer", DurationDays = 5, BasePrice = 30000m, TourCode = TourCode.INT, Status = TourStatus.ACTIVE };
        _draftTour = new Tour { Title = "Unpublished Tour", DurationDays = 5, BasePrice = 30000m, TourCode = TourCode.INT, Status = TourStatus.DRAFT };
        _context.Tours.AddRange(_matchingTour, _draftTour);
        await _context.SaveChangesAsync();

        _context.TourTagRules.Add(new TourTagRule
        {
            Name = "International tours to Europe",
            MatchField = RuleMatchField.TOUR_CODE,
            MatchOperator = RuleOperator.EQUALS,
            MatchValue = "INT",
            TargetCategoryId = _category.CategoryId,
            Priority = 1,
            Active = true
        });
        await _context.SaveChangesAsync();

        _service = new MultipathGeneratorService(
            new GenericRepository<TourTagRule, long>(_context),
            new GenericRepository<Tour, long>(_context),
            new GenericRepository<TourProduct, long>(_context),
            new FakeMultipathLinkRepository(_context),
            NullLogger<MultipathGeneratorService>.Instance);
    }

    [TearDown]
    public void TearDown() => _context.Dispose();

    [Test]
    public async Task Preview_OnlyMatchesActiveTours_AndWritesNothing()
    {
        var preview = await _service.PreviewAsync();

        preview.ToursEvaluated.Should().Be(1); // draft tour excluded
        preview.NewCategoryLinks.Should().Be(1);
        preview.Changes.Should().ContainSingle(c => c.TourId == _matchingTour.TourId && c.LinkType == "CATEGORY" && !c.AlreadyLinked);

        // Preview must never write - the whole point is dry-run parity with Apply.
        (await _context.Tours.FindAsync(_matchingTour.TourId))!.Categories.Should().BeEmpty();
    }

    [Test]
    public async Task Apply_CreatesCategoryLink_AndIsIdempotentOnRerun()
    {
        var first = await _service.ApplyAsync();
        first.NewCategoryLinks.Should().Be(1);

        var reloaded = await _context.Tours.FindAsync(_matchingTour.TourId);
        // Sqlite/InMemory doesn't auto-populate the join collection without a fresh query -
        // re-fetch via a fresh context-free query through the many-to-many table directly.
        var linked = _context.Categories.Where(c => c.CategoryId == _category.CategoryId)
            .SelectMany(c => c.Tours).Any(t => t.TourId == _matchingTour.TourId);
        linked.Should().BeTrue();

        var second = await _service.ApplyAsync();
        second.NewCategoryLinks.Should().Be(0); // already linked - re-running changes nothing further
    }

    [Test]
    public async Task Apply_RuleWithNoMatches_IsReportedByName()
    {
        _context.TourTagRules.Add(new TourTagRule
        {
            Name = "Domestic-only rule",
            MatchField = RuleMatchField.TOUR_CODE,
            MatchOperator = RuleOperator.EQUALS,
            MatchValue = "DOM",
            TargetCategoryId = _category.CategoryId,
            Priority = 2,
            Active = true
        });
        await _context.SaveChangesAsync();

        var result = await _service.ApplyAsync();

        result.RulesWithNoMatches.Should().Contain("Domestic-only rule");
    }
}
