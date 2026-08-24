using eTour.Application.Services;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Hosting;

namespace eTour.Infrastructure.Files;

/// <summary>
/// Local-disk storage, matching Java's FileStorageService. Stores under {ContentRoot}/{uploadDir}/{subfolder}/
/// with a UUID-prefixed filename, and returns the relative URL path ("/uploads/{subfolder}/{name}")
/// that gets persisted on entities and served back by the /uploads static file mapping in Program.cs.
/// </summary>
public class FileStorageService : IFileStorageService
{
    private readonly string _uploadRoot;

    public FileStorageService(IConfiguration configuration, IHostEnvironment environment)
    {
        var uploadDir = configuration["Upload:Directory"] ?? "uploads";
        _uploadRoot = Path.Combine(environment.ContentRootPath, uploadDir);
    }

    public async Task<string> StoreAsync(Stream fileStream, string originalFileName, string subfolder, CancellationToken ct = default)
    {
        var targetDir = Path.Combine(_uploadRoot, subfolder);
        Directory.CreateDirectory(targetDir);

        var safeOriginalName = Path.GetFileName(originalFileName);
        var storedName = $"{Guid.NewGuid()}_{safeOriginalName}";
        var fullPath = Path.Combine(targetDir, storedName);

        await using (var output = File.Create(fullPath))
        {
            await fileStream.CopyToAsync(output, ct);
        }

        return $"/uploads/{subfolder}/{storedName}";
    }
}
