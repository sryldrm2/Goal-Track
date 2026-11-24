namespace GoalTrack.Domain.Entities;

public class Notification
{
    public int Id { get; set; }
    public int UserId { get; set; }
    public User User { get; set; } = null!;
    public int? GoalId { get; set; }
    public Goal? Goal { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Message { get; set; } = string.Empty;
    public bool IsRead { get; set; } 
    public DateTime CreatedAt { get; set; }
}