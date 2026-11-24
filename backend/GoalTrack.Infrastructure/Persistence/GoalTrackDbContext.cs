using Microsoft.EntityFrameworkCore;
using GoalTrack.Domain.Entities;

namespace GoalTrack.Infrastructure.Persistence;

public class GoalTrackDbContext : DbContext
{
    public GoalTrackDbContext(
        DbContextOptions<GoalTrackDbContext> options)
        : base(options)
    {
       
    }

    public DbSet<User> Users { get; set; }
    public DbSet<Category> Categories { get; set; }
    public DbSet<Goal> Goals { get; set; }
    public DbSet<DailyProgress> DailyProgresses { get; set; }
    public DbSet<Notification> Notifications { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // User configuration
        modelBuilder.Entity<User>(entity =>
        {
            entity.HasKey(u => u.Id);

            entity.Property(u => u.Email)
                .IsRequired()
                .HasMaxLength(256);

            entity.HasIndex(u => u.Email)
                .IsUnique();

            entity.Property(u => u.PasswordHash)
                .IsRequired()
                .HasMaxLength(512);

            entity.Property(u => u.DisplayName)
                .IsRequired()
                .HasMaxLength(100);

            entity.Property(u => u.Avatar)
                .HasMaxLength(500);

            entity.Property(u => u.CreatedAt)
                .IsRequired();

            entity.Property(u => u.UpdatedAt)
                .IsRequired();
        });

        // Category configuration
        modelBuilder.Entity<Category>(entity =>
        {
            entity.HasKey(c => c.Id);

            entity.Property(c => c.Name)
                .IsRequired()
                .HasMaxLength(100);

            entity.HasIndex(c => c.Name)
                .IsUnique();

            entity.Property(c => c.Icon)
                .IsRequired()
                .HasMaxLength(100);
        });

        // Notification configuration
        modelBuilder.Entity<Notification>(entity =>
        {
            entity.HasKey(n => n.Id);

            entity.Property(n => n.Title)
                .IsRequired()
                .HasMaxLength(200);

            entity.Property(n => n.Message)
                .IsRequired()
                .HasMaxLength(1000);

            entity.Property(n => n.IsRead)
                .HasDefaultValue(false);

            entity.Property(n => n.CreatedAt)
                .IsRequired();
        });

        // Goal configuration
        modelBuilder.Entity<Goal>(entity =>
        {
            entity.HasKey(g => g.Id);

            entity.Property(g => g.Title)
                .IsRequired()
                .HasMaxLength(200);

            entity.Property(g => g.MeasurementType)
                .IsRequired()
                .HasMaxLength(100);

            entity.Property(g => g.Unit)
                .HasMaxLength(50);

            entity.Property(g => g.TargetValue)
                .HasPrecision(18, 2);

            entity.Property(g => g.CurrentValue)
                .HasPrecision(18, 2);

            entity.Property(g => g.StartDate)
                .IsRequired();

            entity.Property(g => g.EndDate)
                .IsRequired();

            entity.Property(g => g.Frequency)
                .IsRequired();

            entity.Property(g => g.Status)
                .IsRequired();

            entity.Property(g => g.Note)
                .HasMaxLength(1000);

            entity.Property(g => g.SelectedDays)
                .HasMaxLength(100);
        });

        // DailyProgress configuration
        modelBuilder.Entity<DailyProgress>(entity =>
        {
            entity.HasKey(dp => dp.Id);

            entity.Property(dp => dp.Date)
                .IsRequired();

            entity.Property(dp => dp.Value)
                .HasPrecision(18, 2);

            entity.HasIndex(dp => new { dp.GoalId, dp.Date })
                .IsUnique();
        });

        // User -> Goal
        modelBuilder.Entity<User>()
            .HasMany(u => u.Goals)
            .WithOne(g => g.User)
            .HasForeignKey(g => g.UserId);

        // Category -> Goal
        modelBuilder.Entity<Category>()
            .HasMany(c => c.Goals)
            .WithOne(g => g.Category)
            .HasForeignKey(g => g.CategoryId);

        // Goal -> DailyProgress
        modelBuilder.Entity<Goal>()
            .HasMany(g => g.DailyProgresses)
            .WithOne(dp => dp.Goal)
            .HasForeignKey(dp => dp.GoalId);

        // User -> Notification
        modelBuilder.Entity<User>()
            .HasMany(u => u.Notifications)
            .WithOne(n => n.User)
            .HasForeignKey(n => n.UserId);

        // Goal -> Notification
        modelBuilder.Entity<Goal>()
            .HasMany(g => g.Notifications)
            .WithOne(n => n.Goal)
            .HasForeignKey(n => n.GoalId)
            .OnDelete(DeleteBehavior.SetNull);
    }
}
