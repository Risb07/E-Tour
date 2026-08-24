using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Exceptions;
using eTour.Infrastructure.Persistence;
using eTour.Tests.TestSupport;
using FluentAssertions;
using NUnit.Framework;

namespace eTour.Tests.Common;

/// <summary>
/// Requirement #7/#8 coverage: the generic repository/service pair, exercised against a real
/// (in-memory) EF Core provider through the concrete GenericRepository implementation.
/// </summary>
[TestFixture]
public class GenericServiceTests
{
    private EtourDbContext _context = null!;
    private GenericService<Location, LocationDto, long> _service = null!;

    [SetUp]
    public void SetUp()
    {
        _context = TestDbContextFactory.Create();
        var repository = new GenericRepository<Location, long>(_context);
        _service = new GenericService<Location, LocationDto, long>(repository, TestMapper.Create());
    }

    [TearDown]
    public void TearDown() => _context.Dispose();

    [Test]
    public async Task CreateAsync_PersistsAndReturnsGeneratedId()
    {
        var created = await _service.CreateAsync(new LocationDto { LocationName = "Goa", Country = "India", Status = true });

        created.LocationId.Should().BeGreaterThan(0);
        (await _context.Locations.FindAsync(created.LocationId)).Should().NotBeNull();
    }

    [Test]
    public async Task GetByIdAsync_UnknownId_ThrowsResourceNotFoundException()
    {
        var act = async () => await _service.GetByIdAsync(999);
        await act.Should().ThrowAsync<ResourceNotFoundException>();
    }

    [Test]
    public async Task GetAllAsync_ReturnsEveryPersistedRow()
    {
        await _service.CreateAsync(new LocationDto { LocationName = "Goa" });
        await _service.CreateAsync(new LocationDto { LocationName = "Kerala" });

        var all = await _service.GetAllAsync();

        all.Should().HaveCount(2);
    }

    [Test]
    public async Task UpdateAsync_MutatesExistingRow()
    {
        var created = await _service.CreateAsync(new LocationDto { LocationName = "Goa", Status = true });

        var updated = await _service.UpdateAsync(created.LocationId, new LocationDto { LocationName = "Goa (updated)", Status = false });

        updated.LocationName.Should().Be("Goa (updated)");
        updated.Status.Should().BeFalse();
    }

    [Test]
    public async Task DeleteAsync_RemovesRow()
    {
        var created = await _service.CreateAsync(new LocationDto { LocationName = "Goa" });

        await _service.DeleteAsync(created.LocationId);

        (await _context.Locations.FindAsync(created.LocationId)).Should().BeNull();
    }

    [Test]
    public async Task DeleteAsync_UnknownId_ThrowsResourceNotFoundException()
    {
        var act = async () => await _service.DeleteAsync(999);
        await act.Should().ThrowAsync<ResourceNotFoundException>();
    }
}
