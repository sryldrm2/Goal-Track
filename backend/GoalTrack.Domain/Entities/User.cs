namespace GoalTrack.Domain.Entities;

public class User
{
    public int Id { get; set; }
    public String Email { get; set; } = string.Empty;
    public string PasswordHash { get; set; } = string.Empty;
    public String DisplayName { get; set; } = string.Empty;
    public string? Avatar {  get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }

    public ICollection<Goal> Goals { get; set; } = new List<Goal>();
    public ICollection<Notification> Notifications { get; set; } = new List<Notification>();
}