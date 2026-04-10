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

@Database(entities = {
        WorkoutLog.class,
        UserMetricLog.class,
        Routine.class,
        Exercise.class,
        RoutineExercise.class
}, version = 7, exportSchema = false)
public abstract class FitnessDatabase extends RoomDatabase {

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
                            .addCallback(roomCallback)
                            .fallbackToDestructiveMigration() // Handles the v7 wipe
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                RoutineDao dao = INSTANCE.routineDao();

                List<Long> exIds = dao.insertExercises(Arrays.asList(
                        new Exercise("Bench Press",            "Chest",           "Intermediate", "file:///android_asset/images/benchpress.jpg", "file:///android_asset/gifs/benchpress.gif"),
                        new Exercise("Push-Ups",               "Chest",           "Beginner",     "file:///android_asset/images/pushup.jpg", "file:///android_asset/gifs/pushup.gif"),
                        new Exercise("Incline Dumbbell Press", "Chest",           "Intermediate", "file:///android_asset/images/inclinedumbellpress.jpg", "file:///android_asset/gifs/inclinedumbellpress.gif"),
                        new Exercise("Deadlifts",              "Back",            "Advanced",     "file:///android_asset/images/barbelldeadlift.png", "file:///android_asset/gifs/barbelldeadlift.gif"),
                        new Exercise("Pull-Ups",               "Back",            "Intermediate", "file:///android_asset/images/pullup.png", "file:///android_asset/gifs/pullup.gif"),
                        new Exercise("Barbell Row",            "Back",            "Intermediate", "file:///android_asset/images/barbellrow.png", "file:///android_asset/gifs/barbellrow.gif"),
                        new Exercise("Barbell Squats",         "Legs",            "Beginner",     "file:///android_asset/images/barbellsquat.jpg", "file:///android_asset/gifs/barbellsquat.gif"),
                        new Exercise("Leg Press",              "Legs",            "Beginner",     "file:///android_asset/images/legpress.png", "file:///android_asset/gifs/legpress.gif"),
                        new Exercise("Walking Lunges",         "Legs",            "Beginner",     "file:///android_asset/images/walkinglunges.png", "file:///android_asset/gifs/walking lunges.gif"),
                        new Exercise("Overhead Press",         "Shoulders",       "Intermediate", "file:///android_asset/images/overheadpress.png", "file:///android_asset/gifs/overheadpress.gif"),
                        new Exercise("Lateral Raise",          "Shoulders",       "Beginner",     "file:///android_asset/images/lateralraise.png", "file:///android_asset/gifs/lateralraise.gif"),
                        new Exercise("Bicep Curls",            "Arms",            "Beginner",     "file:///android_asset/images/bicepcurls.png", "file:///android_asset/gifs/bicepcurls.gif"),
                        new Exercise("Tricep Dips",            "Arms",            "Beginner",     "file:///android_asset/images/tricepdips.png", "file:///android_asset/gifs/tricepdips.gif"),
                        new Exercise("Plank",                  "Core",            "Beginner",     "file:///android_asset/images/plank.png", "file:///android_asset/gifs/plank.gif"),
                        new Exercise("Crunches",               "Core",            "Beginner",     "file:///android_asset/images/crunches.png", "file:///android_asset/gifs/cruches.gif")
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

                // 2. ADDED PHASE, MINUTES, CALORIES, AND IMAGE STRINGS
                long r1 = dao.insertRoutine(new Routine("Starter Full Body", "Kết hợp cả 3 nhóm cơ chính. Hoàn hảo cho người mới bắt đầu.", "PHASE 01 / STARTER", 3, 24, 240, "file:///android_asset/images/cover_starter.jpg"));
                List<RoutineExercise> r1ex = new ArrayList<>();
                r1ex.add(new RoutineExercise((int)r1, idSquats,     3, 12, 90,  0, 0));
                r1ex.add(new RoutineExercise((int)r1, idBenchPress, 3, 10, 60,  0, 1));
                r1ex.add(new RoutineExercise((int)r1, idDeadlifts,  3,  8, 120, 0, 2));
                dao.insertRoutineExercises(r1ex);

                long r2 = dao.insertRoutine(new Routine("Upper Body Power", "Tập trung vào ngực, lưng và vai. Tăng sức mạnh phần trên.", "PHASE 02 / POWER", 4, 32, 320, "file:///android_asset/images/cover_power.webp"));
                List<RoutineExercise> r2ex = new ArrayList<>();
                r2ex.add(new RoutineExercise((int)r2, idBenchPress,  4, 8,  90,  0, 0));
                r2ex.add(new RoutineExercise((int)r2, idOHPress,     4, 8,  90,  0, 1));
                r2ex.add(new RoutineExercise((int)r2, idBarbellRow,  4, 10, 60,  0, 2));
                r2ex.add(new RoutineExercise((int)r2, idPullUps,     3, 8,  90,  0, 3));
                dao.insertRoutineExercises(r2ex);

                long r3 = dao.insertRoutine(new Routine("Leg Day Shred", "Đốt cháy cơ đùi và mông. Không bỏ ngày tập chân!", "PHASE 03 / SHRED", 3, 24, 240, "file:///android_asset/images/cover_shred.webp"));
                List<RoutineExercise> r3ex = new ArrayList<>();
                r3ex.add(new RoutineExercise((int)r3, idSquats,   4, 10, 90, 0, 0));
                r3ex.add(new RoutineExercise((int)r3, idLegPress, 4, 12, 60, 0, 1));
                r3ex.add(new RoutineExercise((int)r3, idLunges,   3, 12, 60, 0, 2));
                dao.insertRoutineExercises(r3ex);
            });
        }
    };
}