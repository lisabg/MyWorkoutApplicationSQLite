package com.example.myapplication.exercise

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entities.Exercise
import com.example.myapplication.entities.calculateProgress
import com.google.android.material.snackbar.Snackbar

class ExerciseRecyclerViewAdapter(private val exerciseList: ArrayList<Exercise>, val context: Context) :
    RecyclerView.Adapter<ExerciseRecyclerViewAdapter.MyViewHolder>() {

    private var removedPosition : Int = 0
    private var removedItem: Exercise = Exercise("", "", 0, 0, 0, 0)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exercise_card_view_layout, parent, false)
        return MyViewHolder(view, exerciseList)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your data set at this position
        // - replace the contents of the view with that element
        val exercise: Exercise = exerciseList[position]

        val repetitions = "Repetitions: " + exercise.repetition
        val sets = "Sets: " + exercise.sets
        val weight = "Weight: " + exercise.weight

        holder.exerciseName.text = exercise.name
        holder.exerciseRepetitions.text = repetitions
        holder.exerciseSets.text = sets
        holder.exerciseWeight.text = weight

        if (exercise.weight == exercise.weightStart) {
            holder.progressBar.progress = 0
            holder.progressPercentage.text = "0%"
        }
        else {
            val percentage = calculateProgress(exercise.weightStart, exercise.weight, exercise.weightGoal)
            holder.progressBar.progress = percentage
            holder.progressPercentage.text = context.getString(R.string.percentage_text, percentage)
        }
    }

    fun removeExerciseItem(viewHolder: RecyclerView.ViewHolder, db: ExerciseDataBaseHandler) {
        removedPosition = viewHolder.adapterPosition
        removedItem = exerciseList[viewHolder.adapterPosition]

        exerciseList.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)

        db.deleteExerciseData(removedItem.name)

        Snackbar.make(viewHolder.itemView, "${removedItem.name} deleted", Snackbar.LENGTH_LONG).setAction("UNDO") {
            exerciseList.add(removedPosition, removedItem)
            db.insertExerciseData(removedItem)
            notifyItemInserted(removedPosition)
        }.show()

    }

    // Return the size of your data sett (invoked by the layout manager)
    override fun getItemCount() = exerciseList.size


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(itemView: View, private val exerciseList: ArrayList<Exercise>) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        val exerciseName = itemView.findViewById(R.id.exercise_name_id) as TextView
        val exerciseRepetitions = itemView.findViewById(R.id.exercise_repetition_id) as TextView
        val exerciseSets = itemView.findViewById(R.id.exercise_sets_id) as TextView
        val exerciseWeight = itemView.findViewById(R.id.exercise_weight_id) as TextView
        val progressBar = itemView.findViewById(R.id.exercise_card_view_progress_bar) as ProgressBar
        val progressPercentage = itemView.findViewById(R.id.exercise_card_view_progress_percentage) as TextView

        override fun onClick(p0: View?) {

            val exercise = findExercise(exerciseName.text.toString())
            if (exercise.id == -1) {
                Toast.makeText(itemView.context, "ERROR. Try again.", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(itemView.context, ExerciseDetailsActivity::class.java)

            intent.putExtra("id", exercise.id.toString())
            intent.putExtra("name", exercise.name)
            intent.putExtra("description", exercise.description)
            intent.putExtra("repetition", exercise.repetition.toString())
            intent.putExtra("sets", exercise.sets.toString())
            intent.putExtra("weight", exercise.weight.toString())
            intent.putExtra("start", exercise.weightStart.toString())
            intent.putExtra("goal", exercise.weightGoal.toString())

            startActivity(itemView.context, intent, null)
        }

        private fun findExercise(name : String) : Exercise {

            for (e in exerciseList) {
                if (e.name == name) return e
            }
            return Exercise("", "", 0, 0, 0, 0)
        }

    }

}