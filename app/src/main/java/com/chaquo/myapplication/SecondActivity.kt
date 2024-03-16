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
            "Sabah Al-Salem" to Pair(-4, 20),
            "Al-Misela" to Pair(11, 10),
            "Al-Adan" to Pair(-3, 5),
            "Al-Wista" to Pair(-22, 5),
            "Sabhan" to Pair(-39, 6),
            "Al-Funaites" to Pair(14, -3),
            "Al-Qusour" to Pair(-1, -7),
            "Abu Futaira" to Pair(18, -19),
            "Al-Qurain" to Pair(1, -16),
            "Mubarak Al-Kabeer" to Pair(3, -27),
            "South Al-Wista" to Pair(-23, -26)
        )
        val state2Cities = mapOf(
            "Al-Fintas" to Pair(9, 48),
            "Al-Aqeila" to Pair(2, 49),
            "Jaber Al-Ali" to Pair(-6, 47),
            "Al-Dhaher" to Pair(-11, 43),
            "Al-Miqwa'" to Pair(-20, 33),
            "Al-Mahbula" to Pair(10, 36),
            "Al-Riqqa" to Pair(3, 37),
            "Hadiya" to Pair(-2, 35),
            "Abu Halifa" to Pair(11, 26),
            "Fahad Al-Ahmad" to Pair(3, 26),
            "Al-Mangaf" to Pair(11, 16),
            "Al-Fahaheel" to Pair(11, 3),
            "Al-Sabahiya" to Pair(3, 17),
            "South Al-Sabahiya" to Pair(3, 1),
            "Al-Ahmadi City" to Pair(-13, 9),
            "North Al-Shu'aiba" to Pair(8, -9),
            "South Al-Shu'aiba" to Pair(8, -21),
            "Abdullah Port" to Pair(5, -42)
        )
        val state3Cities = mapOf(
            "Mu'skrat Al-Jahra" to Pair(-12, 8),
            "West Al-Jahra" to Pair(-43, 11),
            "Al-Jahra" to Pair(-33, 7),
            "Al-Waha" to Pair(-40, 4),
            "Al-Qasr" to Pair(-25, 5),
            "Al-Naeem" to Pair(-24, -1),
            "Taima" to Pair(-30, -2),
            "Al-Oyoun" to Pair(-40, -2),
            "Al-Nasseem" to Pair(-31, -7),
            "Al-Jahra Industrial" to Pair(-39, -13),
            "Amghara" to Pair(-4, -11),
            "Maqbara" to Pair(30,-17),
            "Al-Sulaibiya" to Pair(30, -24),
            "Al-Sulaibiya Agricultural" to Pair(20, -28),
            "Al-Sulaibiya Industrial 1" to Pair(38, -25),
            "Al-Sulaibiya Industrial 2" to Pair(42, -34)
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
