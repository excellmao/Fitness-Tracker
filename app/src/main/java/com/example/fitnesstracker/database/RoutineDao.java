package com.example.fitnesstracker.database;

import androidx.room.OnConflictStrategy;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RoutineDao {

    @Insert
    long insertRoutine(Routine routine);

    @Insert
    void insertRoutineExercises(List<RoutineExercise> routineExercises);

    @Query("SELECT * FROM routines")
    List<Routine> getAllRoutines();

    @Query("SELECT * FROM exercises")
    List<Exercise> getAllExercises();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertExercises(List<Exercise> exercises);

    @Transaction
    @Query("SELECT " +
            "exercises.id AS exerciseId, " +
            "exercises.name, " +
            "exercises.category, " +
            "exercises.imageUrl, " +
            "exercises.gifUrl, " +
            "exercises.level, " +
            "routine_exercises.sets, " +
            "routine_exercises.reps, " +
            "routine_exercises.restSeconds, " +
            "routine_exercises.durationSeconds, " +
            "routine_exercises.orderIndex, " +
            "routine_exercises.id AS routineExerciseId " +
            "FROM routine_exercises " +
            "INNER JOIN exercises ON routine_exercises.exerciseId = exercises.id " +
            "WHERE routine_exercises.routineId = :routineId " +
            "ORDER BY routine_exercises.orderIndex ASC")
    List<ExerciseWithSettings> getExercisesForRoutine(int routineId);

    @Query("DELETE FROM routine_exercises WHERE id = :id")
    void deleteRoutineExerciseById(int id);

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    void deleteRoutineExercisesByRoutineId(int routineId);

    @Query("DELETE FROM routines WHERE id = :routineId")
    void deleteRoutine(int routineId);

    @Update
    void updateRoutine(Routine routine);

    // Helper for checking if we need to seed data
    @Query("SELECT COUNT(*) FROM exercises")
    int getExerciseCount();

    @Query("SELECT * FROM routines WHERE id = :routineId LIMIT 1")
    Routine getRoutineById(int routineId);

    @Query("DELETE FROM routines WHERE id = :routineId")
    void deleteRoutineExercises(int routineId);
}