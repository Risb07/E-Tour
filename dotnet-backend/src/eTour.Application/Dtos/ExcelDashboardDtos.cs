namespace eTour.Application.Dtos;

public class ExcelUploadResult
{
    public long BatchId { get; set; }
    public string FileName { get; set; } = null!;
    public int TotalRows { get; set; }
    public int SuccessRows { get; set; }
    public int FailedRows { get; set; }
    public string Status { get; set; } = null!;
    public List<string> Errors { get; set; } = [];
}

public class DashboardStatsResponse
{
    public long TotalUsers { get; set; }
    public long TotalCustomers { get; set; }
    public long TotalTours { get; set; }
    public long ActiveTours { get; set; }
    public long TotalBookings { get; set; }
    public long PendingBookings { get; set; }
    public long ConfirmedBookings { get; set; }
    public long CancelledBookings { get; set; }
    public decimal TotalRevenue { get; set; }
}
