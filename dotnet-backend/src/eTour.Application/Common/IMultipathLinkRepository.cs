namespace eTour.Application.Common;

/// <summary>
/// Direct writer for the implicit tour_category join table. MultipathGeneratorService only ever
/// needs to add one link at a time after checking it doesn't already exist, so a raw upsert here
/// avoids the ceremony of loading a tracked Tour with its whole Categories collection just to
/// append one entry.
/// </summary>
public interface IMultipathLinkRepository
{
    Task LinkTourToCategoryAsync(long tourId, long categoryId, CancellationToken ct = default);
}
