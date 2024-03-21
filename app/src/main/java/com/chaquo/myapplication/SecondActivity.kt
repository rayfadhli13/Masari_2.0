package com.chaquo.myapplication
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        Log.d("MyActivity", "Using state name: $selectedStateName")

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
                val filteredCities = cities.filterKeys { it != selectedCity }
                val cityNames = filteredCities.keys.toTypedArray()
                val coordX = filteredCities.values.map { it.first }.toIntArray()
                val coordY = filteredCities.values.map { it.second }.toIntArray()

                intent.putExtra("cityNames", cityNames)
                intent.putExtra("coordX", coordX)
                intent.putExtra("coordY", coordY)
                intent.putExtra("SELECTED_STATE_NAME", selectedStateName)
                Log.d("MyActivity", "Using state name1: $selectedStateName")


                startActivity(intent)
            }
        }
    }

    private fun getCitiesForState(state: String): Map<String, Pair<Int, Int>> {
        // Define maps of cities for each state
        val state1Cities = mapOf(
            "Sabah Al-Salem" to Pair(10, 30),
            "Al-Misela" to Pair(26, 17),
            "Al-Adan" to Pair(9, 9),
            "Al-Wista" to Pair(-15, 5),
            "Sabhan" to Pair(-40, 9),
            "Al-Funaites" to Pair(30, -1),
            "Al-Qusour" to Pair(15, -7),
            "Abu Futaira" to Pair(34, -23),
            "Al-Qurain" to Pair(16, -20),
            "Mubarak Al-Kabeer" to Pair(21, -36),
            "South Al-Wista" to Pair(-15, -32)
        )
        val state2Cities = mapOf(
            "Al-Fintas" to Pair(10, 48),
            "Al-Aqeila" to Pair(3, 44),
            "Jaber Al-Ali" to Pair(-3, 45),
            "Al-Dhaher" to Pair(-10, 44),
            "Al-Miqwa'" to Pair(-13, 31),
            "Al-Mahbula" to Pair(10, 36),
            "Al-Riqqa" to Pair(4, 37),
            "Hadiya" to Pair(-1, 35),
            "Abu Halifa" to Pair(12, 28),
            "Fahad Al-Ahmad" to Pair(6, 26),
            "Al-Mangaf" to Pair(12, 17),
            "Al-Fahaheel" to Pair(12, 2),
            "Al-Sabahiya" to Pair(4, 18),
            "South Al-Sabahiya" to Pair(4, 5),
            "Al-Ahmadi City" to Pair(-8, 6),
            "North Al-Shu'aiba" to Pair(12, -8),
            "South Al-Shu'aiba" to Pair(12, -20),
            "Abdullah Port" to Pair(12, -33)
        )
        val state3Cities = mapOf(
            "Mu'skrat Al-Jahra" to Pair(-8, 6),
            "West Al-Jahra" to Pair(-44, 11),
            "Al-Jahra" to Pair(-34, 10),
            "Al-Waha" to Pair(-41, 5),
            "Al-Qasr" to Pair(-25, 6),
            "Al-Naeem" to Pair(-26, 3),
            "Taima" to Pair(-32, -1),
            "Al-Oyoun" to Pair(-41, -3),
            "Al-Nasseem" to Pair(-33, -7),
            "Al-Jahra Industrial" to Pair(-45, -6),
            "Amghara" to Pair(-8, -13),
            "Maqbara" to Pair(32,-16),
            "Al-Sulaibiya" to Pair(30, -26),
            "Al-Sulaibiya Agricultural" to Pair(19, -26),
            "Al-Sulaibiya Industrial 1" to Pair(35, -24),
            "Al-Sulaibiya Industrial 2" to Pair(40, -29)
        )
        // Return the map of cities based on the selected state
        return when (state) {
            "Mubarak Al-Kabeer" -> state1Cities
            "Al-Ahmadi" -> state2Cities
            "Al-Jahra" -> state3Cities
            // Add more cases for additional states
            else -> emptyMap() // Return an empty map if the state is not recognized
        }
    }
}
