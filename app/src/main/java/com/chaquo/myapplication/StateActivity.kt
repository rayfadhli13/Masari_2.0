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


class CityAdapter(context: Context, cities: List<City>) :
    ArrayAdapter<City>(context, 0, cities) {

    private var selectedPosition = -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_city_checkbox, parent, false)

        val cityNameTextView: TextView = view.findViewById(R.id.tvCityName)
        val coordinatesTextView: TextView = view.findViewById(R.id.tvCoordinates)
        val checkBox: CheckBox = view.findViewById(R.id.checkBoxCity)

        val city = getItem(position)
        cityNameTextView.text = city?.name
        coordinatesTextView.text = "Coordinates: (${city?.coordinates})"
        checkBox.isChecked = position == selectedPosition

        checkBox.setOnClickListener {
            selectedPosition = if (selectedPosition == position) -1 else position
            notifyDataSetChanged()
        }

        return view
    }

    fun getSelectedCity(): City? = if (selectedPosition >= 0) getItem(selectedPosition) else null
}

data class City(val name: String, val coordinates: String)
class StateActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var adapter: CityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state)

        listView = findViewById(R.id.listView)

        // Assume your cities are initialized here
        val cities = listOf(
            City("City1", "(0, 0)"),
            City("City2", "(1, 1)"),
            // Add more cities as needed
        )

        adapter = CityAdapter(this, cities)
        listView.adapter = adapter

        val continueButton: Button = findViewById(R.id.button_next)
        continueButton.setOnClickListener {
            adapter.getSelectedCity()?.let { city ->
                // Create an intent to start SecondActivity
                val intent = Intent(this, SecondActivity::class.java).apply {
                    // Assuming you want to pass the city name and coordinates as extras
                    putExtra("SELECTED_CITY_NAME", city.name)
                    putExtra("SELECTED_CITY_COORDINATES", city.coordinates)
                }
                startActivity(intent)
            } ?: run {
                // If no city is selected, show a toast message
                Toast.makeText(this, "Please select a city.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
