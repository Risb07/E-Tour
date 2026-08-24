namespace eTour.Application.Services;

public interface IFileStorageService
{
    /// <summary>Stores the file under {UploadDir}/{subfolder}/ and returns the relative URL path (e.g. "/uploads/excel-batches/{name}") that gets persisted in entities and served back statically.</summary>
    Task<string> StoreAsync(Stream fileStream, string originalFileName, string subfolder, CancellationToken ct = default);
}
