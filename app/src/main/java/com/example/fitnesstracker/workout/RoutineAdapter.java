package com.example.fitnesstracker.workout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.database.Routine;
import java.util.List;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    private List<Routine> routines;
    private final OnRoutineClickListener listener;
    private final OnRoutineEditListener editListener;
    private final OnRoutineDeleteListener deleteListener;

    public interface OnRoutineClickListener {
        void onRoutineClick(Routine routine);
    }

    public interface OnRoutineEditListener {
        void onEdit(Routine routine);
    }

    public interface OnRoutineDeleteListener {
        void onDelete(Routine routine);
    }

    public RoutineAdapter(List<Routine> routines, OnRoutineClickListener listener,
                          OnRoutineEditListener editListener, OnRoutineDeleteListener deleteListener) {
        this.routines = routines;
        this.listener = listener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routines.get(position);
        holder.tvName.setText(routine.getName());
        holder.tvCount.setText(routine.getExerciseCount() + " Exercises");
        holder.itemView.setOnClickListener(v -> listener.onRoutineClick(routine));
        holder.ibEdit.setOnClickListener(v -> editListener.onEdit(routine));
        holder.ibDelete.setOnClickListener(v -> deleteListener.onDelete(routine));
    }

    @Override
    public int getItemCount() {
        return routines != null ? routines.size() : 0;
    }

    public void setRoutines(List<Routine> routines) {
        this.routines = routines;
        notifyDataSetChanged();
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCount;
        ImageView ibEdit, ibDelete;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName   = itemView.findViewById(R.id.tvRoutineName);
            tvCount  = itemView.findViewById(R.id.tvExerciseCount);
            ibEdit   = itemView.findViewById(R.id.ibEditRoutine);
            ibDelete = itemView.findViewById(R.id.ibDeleteRoutine);
        }
    }
}
