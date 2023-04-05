package com.iarigo.spelling.ui.words

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iarigo.spelling.R
import com.iarigo.spelling.storage.entity.Words
import java.util.*

class WordsAdapter(myDataSet: ArrayList<Words>): RecyclerView.Adapter<WordsAdapter.WordsAdapterViewHolder>() {

    private var mDataSet: ArrayList<Words>? = null

    var onItemClick: ((Words) -> Unit)? = null

    init {
        mDataSet = myDataSet
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    inner class WordsAdapterViewHolder (val view: LinearLayout) : RecyclerView.ViewHolder(view) {
        // each data item is just a string in this case
        val itemName: TextView // название элемента

        init {
            // слой элемента
            itemName = view.findViewById(R.id.list_elem_name)
            view.setOnClickListener {
                onItemClick?.invoke(mDataSet!![adapterPosition])
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordsAdapterViewHolder {
        // create a new view
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_word_element, parent, false)
        return WordsAdapterViewHolder(v as LinearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    //Внешний вид элемента
    override fun onBindViewHolder(holder: WordsAdapterViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val map = mDataSet!![position]
        setView(holder, map, position) // внешний вид. что показывать,что нет
    }

    /**
     * поиск по списку
     */
    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterlist: ArrayList<Words>) {
        // below line is to add our filtered
        // list in our course array list.
        mDataSet = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    /**
     * Определяем внешний вид элемента
     */
    private fun setView(holder: WordsAdapterViewHolder, map: Words, position: Int) {
        holder.itemName.text = map.word // название
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mDataSet!!.size
    }

    fun getData(): ArrayList<Words>? {
        return mDataSet
    }
}