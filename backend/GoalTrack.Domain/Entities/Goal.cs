using GoalTrack.Domain.Enums;

namespace GoalTrack.Domain.Entities;

public class Goal
{
    // Hedefin benzersiz kimliği
    public int Id { get; set;  }

    // Hedefin sahibi olan kullanıcı
    public int UserId { get; set; }
    public User User { get; set; } = null!;

    // Hedefin ait olduğu kategori
    public int CategoryId { get; set; }
    public Category Category { get; set; } = null!;

    // Hedef bilgileri
    public string Title { get; set; } = string.Empty;
    public string MeasurementType {  get; set; } = string.Empty;
    public string? Unit {  get; set; }

    // Hedef ve mevcut ilerleme değerleri
    public decimal TargetValue { get; set; }
    public decimal CurrentValue { get; set; }

    // Hedef tarihleri
    public DateTime StartDate { get; set; }
    public DateTime EndDate { get; set; }

    // Hedef sıklığı ve durumu
    public GoalFrequency Frequency { get; set; }
    public GoalStatus Status { get; set; }

    // Hatırlatıcı bilgileri
    public bool IsReminderActive { get; set; }
    public int? ReminderHour { get; set; }
    public int? ReminderMinute { get; set; }

    // Ek bilgiler
    public string? Note { get; set; }
    public string? SelectedDays { get; set; }

    // İlişkili kayıtlar
    public ICollection<DailyProgress> DailyProgresses { get; set; } 
        = new List<DailyProgress>();

    public ICollection<Notification> Notifications { get; set; }
        = new List<Notification>();
}