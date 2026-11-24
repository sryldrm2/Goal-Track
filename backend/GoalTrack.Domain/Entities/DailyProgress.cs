namespace GoalTrack.Domain.Entities;

public class DailyProgress
{
    // Günlük ilerleme kaydının benzersiz kimliği
    public int Id { get; set; }

    // İlerlemenin hangi hedefe ait olduğu
    public int GoalId { get; set; }
    public Goal Goal { get; set; } = null!;

    // İlerlemenin gerçekleştiği gün
    public DateTime Date { get; set; }

    // O gün gerçekleştirilen ilerleme miktarı
    public decimal Value { get; set; }
}