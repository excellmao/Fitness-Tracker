package com.example.fitnesstracker.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

// Import your Profile module's database model!
import com.example.fitnesstracker.profile.models.UserMetricLog;

// 1. COMBINED ENTITIES & VERSION BUMP TO 6
@Database(entities = {
        WorkoutLog.class,
        UserMetricLog.class,
        Routine.class,
        Exercise.class,
        RoutineExercise.class
}, version = 6, exportSchema = false)
public abstract class FitnessDatabase extends RoomDatabase {

    // 2. COMBINED DAOs
    public abstract WorkoutDao workoutDao();
    public abstract RoutineDao routineDao();
    public abstract MetricDao metricDao();

    private static volatile FitnessDatabase INSTANCE;

    public static FitnessDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (FitnessDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FitnessDatabase.class, "fitness_tracker_database")
                            .addCallback(roomCallback) // Kept teammate's auto-loader
                            .fallbackToDestructiveMigration() // Safely handles the Version 6 bump!
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // 3. TEAMMATE'S MOCK DATA SEEDER
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                RoutineDao dao = INSTANCE.routineDao();

                // Exercise Library
                List<Long> exIds = dao.insertExercises(Arrays.asList(
                        new Exercise("Bench Press",            "Chest",           "Intermediate", ""),
                        new Exercise("Push-Ups",               "Chest",           "Beginner",     ""),
                        new Exercise("Incline Dumbbell Press", "Chest",           "Intermediate", ""),
                        new Exercise("Deadlifts",              "Back",            "Advanced",     ""),
                        new Exercise("Pull-Ups",               "Back",            "Intermediate", ""),
                        new Exercise("Barbell Row",            "Back",            "Intermediate", ""),
                        new Exercise("Barbell Squats",         "Legs",            "Beginner",     ""),
                        new Exercise("Leg Press",              "Legs",            "Beginner",     ""),
                        new Exercise("Walking Lunges",         "Legs",            "Beginner",     ""),
                        new Exercise("Overhead Press",         "Shoulders",       "Intermediate", ""),
                        new Exercise("Lateral Raise",          "Shoulders",       "Beginner",     ""),
                        new Exercise("Bicep Curls",            "Arms",            "Beginner",     ""),
                        new Exercise("Tricep Dips",            "Arms",            "Beginner",     ""),
                        new Exercise("Plank",                  "Core",            "Beginner",     ""),
                        new Exercise("Crunches",               "Core",            "Beginner",     "")
                ));

                int idBenchPress  = exIds.get(0).intValue();
                int idPushUps     = exIds.get(1).intValue();
                int idDeadlifts   = exIds.get(3).intValue();
                int idPullUps     = exIds.get(4).intValue();
                int idBarbellRow  = exIds.get(5).intValue();
                int idSquats      = exIds.get(6).intValue();
                int idLegPress    = exIds.get(7).intValue();
                int idLunges      = exIds.get(8).intValue();
                int idOHPress     = exIds.get(9).intValue();
                int idBicepCurls  = exIds.get(11).intValue();

                // Routine 1: Starter Full Body
                long r1 = dao.insertRoutine(new Routine("Starter Full Body", "Kết hợp cả 3 nhóm cơ chính. Hoàn hảo cho người mới bắt đầu.", 3));
                List<RoutineExercise> r1ex = new ArrayList<>();
                r1ex.add(new RoutineExercise((int)r1, idSquats,     3, 12, 90,  0, 0));
                r1ex.add(new RoutineExercise((int)r1, idBenchPress, 3, 10, 60,  0, 1));
                r1ex.add(new RoutineExercise((int)r1, idDeadlifts,  3,  8, 120, 0, 2));
                dao.insertRoutineExercises(r1ex);

                // Routine 2: Upper Body Power
                long r2 = dao.insertRoutine(new Routine("Upper Body Power", "Tập trung vào ngực, lưng và vai. Tăng sức mạnh phần trên.", 4));
                List<RoutineExercise> r2ex = new ArrayList<>();
                r2ex.add(new RoutineExercise((int)r2, idBenchPress,  4, 8,  90,  0, 0));
                r2ex.add(new RoutineExercise((int)r2, idOHPress,     4, 8,  90,  0, 1));
                r2ex.add(new RoutineExercise((int)r2, idBarbellRow,  4, 10, 60,  0, 2));
                r2ex.add(new RoutineExercise((int)r2, idPullUps,     3, 8,  90,  0, 3));
                dao.insertRoutineExercises(r2ex);

                // Routine 3: Leg Day Shred
                long r3 = dao.insertRoutine(new Routine("Leg Day Shred", "Đốt cháy cơ đùi và mông. Không bỏ ngày tập chân!", 3));
                List<RoutineExercise> r3ex = new ArrayList<>();
                r3ex.add(new RoutineExercise((int)r3, idSquats,   4, 10, 90, 0, 0));
                r3ex.add(new RoutineExercise((int)r3, idLegPress, 4, 12, 60, 0, 1));
                r3ex.add(new RoutineExercise((int)r3, idLunges,   3, 12, 60, 0, 2));
                dao.insertRoutineExercises(r3ex);
            });
        }
    };
}