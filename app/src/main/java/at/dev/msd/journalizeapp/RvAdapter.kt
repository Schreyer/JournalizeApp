package at.dev.msd.journalizeapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RvAdapter(private val userList: ArrayList<Model>, private var context: Context) :

    RecyclerView.Adapter<RvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val v = layoutInflater.inflate(R.layout.adapter_item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = userList[position].day
        val month = userList[position].month

        holder.title.text = userList[position].title
        holder.dayAndMonth.text = "$day.$month"
//        holder.dayAndMonth.text = Resources.getSystem().getString(R.string.date, day, month)
        holder.year.text = userList[position].year
        holder.documentId = userList[position].id

        //Animation
        holder.container.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.fade_scale_animation
            )
        )
    }


    class ViewHolder(itemView: View, var documentId: String = "") :

        RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val dayAndMonth: TextView = itemView.findViewById(R.id.dayAndMonth)
        val year: TextView = itemView.findViewById(R.id.year)
        val container: FrameLayout = itemView.findViewById(R.id.card_container)

        companion object {
            const val DOCUMENT_ID_KEY = "documentId"
        }

        init {
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra(DOCUMENT_ID_KEY, documentId)
                itemView.context.startActivity(intent)
            }

        }
    }
}
