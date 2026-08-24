namespace eTour.Domain.Entities;

public class NavMenuItem
{
    public long NavMenuItemId { get; set; }
    public string Label { get; set; } = null!;
    public string? Link { get; set; }
    public int SortOrder { get; set; }
    public bool Active { get; set; } = true;

    public long? ParentItemId { get; set; }
    public NavMenuItem? ParentItem { get; set; }
    public ICollection<NavMenuItem> Children { get; set; } = new List<NavMenuItem>();
}
