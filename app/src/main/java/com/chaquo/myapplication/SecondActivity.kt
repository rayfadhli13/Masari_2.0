package com.chaquo.myapplication
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SecondActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private var selectedCity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_second)

        val selectedStateName = intent.getStringExtra("SELECTED_STATE_NAME")
        val cities = getCitiesForState(selectedStateName ?: "")

        listView = findViewById(R.id.listView)
        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.list_item_city_checkbox,
            R.id.tvCityName,
            cities.keys.toList()
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    R.layout.list_item_city_checkbox,
                    parent,
                    false
                )
                val cityName = getItem(position) ?: ""
                val coordinates = cities[cityName]!!
                val tvCityName = view.findViewById<TextView>(R.id.tvCityName)
                tvCityName.text = cityName
                val tvCoordinates = view.findViewById<TextView>(R.id.tvCoordinates)
                tvCoordinates.text = "At Coordinates (${coordinates.first}, ${coordinates.second})"
                val checkBoxCity = view.findViewById<CheckBox>(R.id.checkBoxCity)
                checkBoxCity.isChecked = cityName == selectedCity
                checkBoxCity.setOnClickListener {
                    selectedCity = if (checkBoxCity.isChecked) cityName else null
                    notifyDataSetChanged()
                    listView.setItemChecked(position, checkBoxCity.isChecked)
                }
                view.setOnClickListener { checkBoxCity.performClick() }
                return view
            }
        }
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val city = cities.keys.toList()[position]
            selectedCity = if (selectedCity != city) city else null
            adapter.notifyDataSetChanged()
            listView.setItemChecked(position, selectedCity != null)
        }

        val continueButton = findViewById<Button>(R.id.button_next)
        continueButton.setOnClickListener {
            if (selectedCity == null) {
                Toast.makeText(this, "Please select a city to continue.", Toast.LENGTH_SHORT).show()
            } else {
                val coordinates = cities[selectedCity]!!
                val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("SELECTED_CITY_NAME", selectedCity)
                    putInt("SELECTED_COORD_X", coordinates.first)
                    putInt("SELECTED_COORD_Y", coordinates.second)
                    apply()
                }
                val intent = Intent(this, ThirdActivity::class.java)

// Assuming 'cities' is a Map<String, Pair<Int, Int>>
                val filteredCities = cities.filterKeys { it != selectedCity }
                val cityNames = filteredCities.keys.toTypedArray()
                val coordX = filteredCities.values.map { it.first }.toIntArray()
                val coordY = filteredCities.values.map { it.second }.toIntArray()

                intent.putExtra("cityNames", cityNames)
                intent.putExtra("coordX", coordX)
                intent.putExtra("coordY", coordY)

                startActivity(intent)
            }
        }
    }

    private fun getCitiesForState(state: String): Map<String, Pair<Int, Int>> {
        // Define maps of cities for each state
        val state1Cities = mapOf(
            "Kabd" to Pair(0, -2),
            "Abraq Khaitan" to Pair(3, -3),
            "Al-Arthieya" to Pair(5, -2)
            // Add more cities that belong to State1
        )
        val state2Cities = mapOf(
            "Al-Shamiya" to Pair(-6, -5),
            "Al-Shuwaikh" to Pair(-2, -3),
            "Al-Sulaibikhat" to Pair(1, -5)
            // Add more cities that belong to State2
        )
        // Add more states and their cities as needed

        // Return the map of cities based on the selected state
        return when (state) {
            "State1" -> state1Cities
            "State2" -> state2Cities
            // Add more cases for additional states
            else -> emptyMap() // Return an empty map if the state is not recognized
        }
    }
}
