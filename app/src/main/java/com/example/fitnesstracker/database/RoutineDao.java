package com.example.fitnesstracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
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

    @Insert
    List<Long> insertExercises(List<Exercise> exercises);

    /**
     * The magical JOIN query that connects Exercise and RoutineExercise tables.
     */
    @Query("SELECT exercises.id AS exerciseId, exercises.name, exercises.category, exercises.imageUrl, exercises.level, " +
            "routine_exercises.sets, routine_exercises.reps, routine_exercises.restSeconds, routine_exercises.durationSeconds, " +
            "routine_exercises.orderIndex, routine_exercises.id AS reId " +
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

    @Query("UPDATE routines SET name = :name, exerciseCount = :count WHERE id = :routineId")
    void updateRoutine(int routineId, String name, int count);

    // Helper for checking if we need to seed data
    @Query("SELECT COUNT(*) FROM exercises")
    int getExerciseCount();
}