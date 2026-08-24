using eTour.Domain.Enums;

namespace eTour.Domain.Entities;

public class ExcelUploadBatch
{
    public long BatchId { get; set; }
    public string FileName { get; set; } = null!;
    public string? FilePath { get; set; }
    public int TotalRows { get; set; }
    public int SuccessRows { get; set; }
    public int FailedRows { get; set; }
    public UploadBatchStatus Status { get; set; } = UploadBatchStatus.UPLOADED;
    public DateTime UploadedAt { get; set; }

    public long UploadedById { get; set; }
    public User UploadedBy { get; set; } = null!;
}
