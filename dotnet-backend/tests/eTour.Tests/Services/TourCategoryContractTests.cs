using System.Text.Json;
using eTour.Application.Dtos;
using eTour.Application.Services;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using NUnit.Framework;

namespace eTour.Tests.Services;

/// <summary>
/// Pins the tour/category wire contract to the shape the existing React client was built against
/// (i.e. what the Java backend emits by serializing its JPA entities directly). These are JSON
/// property-name and relationship-diff bugs: everything compiles and returns 200 either way, so
/// only assertions at this level catch a regression. Both failures below were real - the admin
/// tour form silently saved zero categories, and every sub-category page rendered empty.
/// </summary>
[TestFixture]
public class TourCategoryContractTests
{
    private EtourDbContext _context = null!;
    private TourService _tourService = null!;
    private CategoryService _categoryService = null!;

    private static readonly JsonSerializerOptions CamelCase =
        new() { PropertyNamingPolicy = JsonNamingPolicy.CamelCase };

    [SetUp]
    public void SetUp()
    {
        _context = TestDbContextFactory.Create();
        var mapper = TestMapper.Create();
        var tourRepository = new GenericRepository<Tour, long>(_context);
        var categoryRepository = new GenericRepository<Category, long>(_context);

        _tourService = new TourService(
            new eTour.Application.Common.GenericService<Tour, TourResponse, long>(tourRepository, mapper),
            tourRepository,
            categoryRepository,
            new GenericRepository<TourSchedule, long>(_context),
            new GenericRepository<Itinerary, long>(_context),
            new GenericRepository<TourMedia, long>(_context),
            new GenericRepository<TourAddon, long>(_context),
            new GenericRepository<Review, long>(_context),
            mapper);

        _categoryService = new CategoryService(categoryRepository, mapper);
    }

    [TearDown]
    public void TearDown() => _context.Dispose();

    private async Task<(long adventure, long beach, long europe)> SeedCategoriesAsync()
    {
        var adventure = new Category { CategoryName = "Adventure", CategoryCode = "ADV", IsFeatured = "Y" };
        var beach = new Category { CategoryName = "Beach", CategoryCode = "DOM", IsFeatured = "N" };
        _context.Categories.AddRange(adventure, beach);
        await _context.SaveChangesAsync();

        var europe = new Category
        {
            CategoryName = "Europe",
            CategoryCode = "INT",
            IsFeatured = "N",
            ParentCategoryId = adventure.CategoryId
        };
        _context.Categories.Add(europe);
        await _context.SaveChangesAsync();

        return (adventure.CategoryId, beach.CategoryId, europe.CategoryId);
    }

    private static TourRequest RequestFor(params long[] categoryIds) => new()
    {
        Title = "Test Tour",
        Description = "d",
        DurationDays = 5,
        BasePrice = 1000,
        TourCode = "DOM",
        Status = "ACTIVE",
        Categories = categoryIds.Select(id => new CategoryRef { CategoryId = id }).ToList()
    };

    [Test]
    public async Task UpdateAsync_ReplacingCategories_AddsTheNewOneAndDropsTheOldOne()
    {
        var (adventure, beach, _) = await SeedCategoriesAsync();
        var created = await _tourService.CreateAsync(RequestFor(adventure));

        await _tourService.UpdateAsync(created.TourId, RequestFor(beach));

        // Reads back through EF rather than the returned DTO, so this asserts the join table
        // itself was diffed - not just that the in-memory entity looked right.
        var stored = _context.Tours.Single(t => t.TourId == created.TourId);
        await _context.Entry(stored).Collection(t => t.Categories).LoadAsync();
        stored.Categories.Select(c => c.CategoryId).Should().BeEquivalentTo([beach]);
    }

    [Test]
    public async Task UpdateAsync_KeepingAnExistingCategory_DoesNotDuplicateTheJoinRow()
    {
        // Re-sending a category the tour already has used to re-INSERT the same join row,
        // failing with a duplicate-key violation on tour_category's composite primary key.
        var (adventure, beach, _) = await SeedCategoriesAsync();
        var created = await _tourService.CreateAsync(RequestFor(adventure));

        var act = async () => await _tourService.UpdateAsync(created.TourId, RequestFor(adventure, beach));

        await act.Should().NotThrowAsync();
        var stored = _context.Tours.Single(t => t.TourId == created.TourId);
        await _context.Entry(stored).Collection(t => t.Categories).LoadAsync();
        stored.Categories.Select(c => c.CategoryId).Should().BeEquivalentTo([adventure, beach]);
    }

    [Test]
    public void TourRequest_BindsTheAdminFormPayload_WhichSendsCategoryObjectsNotIds()
    {
        // Verbatim shape from the admin tour form's submit handler.
        const string payload = """
        {
          "title": "Test Tour",
          "description": "d",
          "durationDays": 5,
          "basePrice": 1000,
          "tourCode": "DOM",
          "status": "ACTIVE",
          "categories": [{ "categoryId": 7 }, { "categoryId": 9 }]
        }
        """;

        var request = JsonSerializer.Deserialize<TourRequest>(payload, CamelCase)!;

        request.Categories.Select(c => c.CategoryId).Should().Equal(7, 9);
    }

    [Test]
    public async Task TourResponse_SerializesCategoriesAsObjects_SoTheAdminFormCanPreselectThem()
    {
        var (adventure, _, _) = await SeedCategoriesAsync();
        var created = await _tourService.CreateAsync(RequestFor(adventure));

        var json = JsonSerializer.SerializeToElement(await _tourService.GetByIdAsync(created.TourId), CamelCase);

        var categories = json.GetProperty("categories").EnumerateArray().ToList();
        categories.Should().HaveCount(1);
        categories[0].GetProperty("categoryId").GetInt64().Should().Be(adventure);
        categories[0].GetProperty("categoryName").GetString().Should().Be("Adventure");
    }

    [Test]
    public async Task CategoryResponse_SerializesParentAsNestedObject_SoTheSiteCanBuildItsTree()
    {
        var (adventure, _, europe) = await SeedCategoriesAsync();

        var all = await _categoryService.GetAllAsync();
        var json = JsonSerializer.SerializeToElement(all, CamelCase);

        var child = json.EnumerateArray().Single(c => c.GetProperty("categoryId").GetInt64() == europe);
        child.GetProperty("parentCategory").GetProperty("categoryId").GetInt64().Should().Be(adventure);

        // A top-level category must have no parentCategory at all - that absence is exactly what
        // the client filters on to decide which categories to show on the home page.
        var parent = json.EnumerateArray().Single(c => c.GetProperty("categoryId").GetInt64() == adventure);
        parent.GetProperty("parentCategory").ValueKind.Should().Be(JsonValueKind.Null);
    }

    [Test]
    public async Task CategoryResponse_ActiveTourCount_CountsOnlyActiveTours()
    {
        var (adventure, _, _) = await SeedCategoriesAsync();
        await _tourService.CreateAsync(RequestFor(adventure));
        var draft = RequestFor(adventure);
        draft.Status = "DRAFT";
        await _tourService.CreateAsync(draft);

        var category = await _categoryService.GetByIdAsync(adventure);

        category.ActiveTourCount.Should().Be(1);
    }
}
