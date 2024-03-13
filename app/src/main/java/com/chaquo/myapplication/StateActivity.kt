package com.chaquo.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.myapplication.R

data class State(val name: String)

class StateAdapter(context: Context, states: List<State>) :
    ArrayAdapter<State>(context, 0, states) {

    private var selectedPosition = -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_city_checkbox, parent, false)
        val stateNameTextView: TextView = view.findViewById(R.id.tvCityName)
        val checkBox: CheckBox = view.findViewById(R.id.checkBoxCity)
        val state = getItem(position)

        stateNameTextView.text = state?.name
        checkBox.isChecked = position == selectedPosition

        view.setOnClickListener {
            selectedPosition = if (selectedPosition == position) -1 else position
            notifyDataSetChanged() // Notify the adapter to refresh views.
        }

        return view
    }

    fun getSelectedState(): State? = if (selectedPosition >= 0) getItem(selectedPosition) else null
}

class StateActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: StateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state)

        listView = findViewById(R.id.listView)

        val states = listOf(
            State("State1"),
            State("State2")
            // Add more states as needed.
        )

        adapter = StateAdapter(this, states)
        listView.adapter = adapter

        val continueButton: Button = findViewById(R.id.button_next)
        continueButton.setOnClickListener {
            adapter.getSelectedState()?.let { state ->
                // Proceed only when the continue button is clicked.
                val intent = Intent(this, SecondActivity::class.java).apply {
                    putExtra("SELECTED_STATE_NAME", state.name)
                }
                startActivity(intent)
            } ?: Toast.makeText(this, "Please select a state.", Toast.LENGTH_LONG).show()
        }
    }
}
