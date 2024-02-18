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
        val cities = mapOf(
            "Kabd" to Pair(0, -2),
            "Abraq Khaitan" to Pair(3, -3),
            "Al-Arthieya" to Pair(5, -2),
            "Al-Shamiya" to Pair(-6, -5),
            "Al-Shuwaikh" to Pair(-2, -3),
            "Al-Sulaibikhat" to Pair(1, -5),
            "Al-Zour" to Pair(4, -6),
            "Abu Al-Hasaniya" to Pair(6, -5),
            "Al-Naeem" to Pair(-7, -8),
            "Mubarak Al-Kabeer" to Pair(-5, 4),
            "Al-Mahboula" to Pair(-2, 3),
            "Sabah Al-Salem" to Pair(1, 4),
            "Al-Fintas" to Pair(3, 3),
            "Al-Fahaheel" to Pair(5, 4),
            "Al-Riqqah" to Pair(-4, 1),
            "Salwa" to Pair(-1, 0),
            "Al-Manqaf" to Pair(2, 1),
            "Kuwait City" to Pair(-6, 7),
            "Al-Ahmadi" to Pair(-3, 6),
            "Hawalli" to Pair(2, 7),
            "Al-Farwaniyah" to Pair(4, 6),
            "Al-Jahra" to Pair(6, 7),
            "Al-Dasmah" to Pair(4, 0),
            "Al-Salmiyah" to Pair(6, 1),
            "Shaab" to Pair(-6, 0),
            "Al-Wafrah" to Pair(-4, -1),
            "Al-Adan" to Pair(-4, -7),
            "Aswaq Al-Qurain" to Pair(0, -8),
            "Al-Fineates" to Pair(3, -7),
            "Jaber Al-Ali" to Pair(6, -8)
        )


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
                val coordinates = cities[cityName]
                val tvCityName = view.findViewById<TextView>(R.id.tvCityName)
                tvCityName.text = cityName
                val tvCoordinates = view.findViewById<TextView>(R.id.tvCoordinates)
                tvCoordinates.text =
                    "At Coordinates (${coordinates?.first}, ${coordinates?.second})"
                val checkBoxCity = view.findViewById<CheckBox>(R.id.checkBoxCity)
                checkBoxCity.isChecked = cityName == selectedCity
                checkBoxCity.setOnClickListener {
                    selectedCity = if (checkBoxCity.isChecked) cityName else null
                    notifyDataSetChanged()
                    listView.setItemChecked(position, checkBoxCity.isChecked)
                }
                view.setOnClickListener {
                    checkBoxCity.performClick()
                }
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
                val coordinates = cities[selectedCity] ?: Pair(0, 0)
                val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("SELECTED_CITY_NAME", selectedCity)
                    putInt("SELECTED_COORD_X", coordinates.first)
                    putInt("SELECTED_COORD_Y", coordinates.second)
                    apply()
                }
                val intent = Intent(this, ThirdActivity::class.java)
                startActivity(intent)
            }
        }
    }
}