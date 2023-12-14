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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ThirdActivity : AppCompatActivity() {
    private val maxSelections = 15
    private lateinit var listViewCities: ListView
    private lateinit var btnConfirm: Button
    private val selectedCities = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_third)
        listViewCities = findViewById(R.id.listViewCities)
        btnConfirm = findViewById(R.id.btnConfirmSelection)
        val sharedPref = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val selectedCityFromSecondActivity = sharedPref.getString("SELECTED_CITY_NAME", "")
        val cities = mapOf(
            "Kuwait City" to Pair(-6, -8),
            "Al-Ahmadi" to Pair(-2, -7),
            "Hawalli" to Pair(-3, -4),
            "Al-Farwaniyah" to Pair(-5, 7),
            "Al-Jahra" to Pair(-2, 3),
            "Mubarak Al-Kabeer" to Pair(-1, 8),
            "Al-Mahboula" to Pair(9, -1),
            "Sabah Al-Salem" to Pair(7, 2),
            "Al-Fintas" to Pair(-3, -7),
            "Al-Fahaheel" to Pair(-1, -2),
            "Al-Riqqah" to Pair(-4, 4),
            "Salwa" to Pair(-8, 8),
            "Al-Manqaf" to Pair(4, -1),
            "Al-Dasmah" to Pair(-4, 6),
            "Al-Salmiyah" to Pair(2, 1),
            "Shaab" to Pair(6, -5),
            "Al-Wafrah" to Pair(-2, -3),
            "Kabd" to Pair(1, 6),
            "Abraq Khaitan" to Pair(-9, 2),
            "Al-Arthieya" to Pair(-2, 2),
            "Al-Shamiya" to Pair(7, -5),
            "Al-Shuwaikh" to Pair(-3, 3),
            "Al-Sulaibikhat" to Pair(7, -6),
            "Al-Zour" to Pair(4, 3),
            "Abu Al-Hasaniya" to Pair(-8, -3),
            "Al-Naeem" to Pair(9, 6),
            "Al-Adan" to Pair(5, 4),
            "Qurain" to Pair(-4, -6),
            "Aswaq Al-Qurain" to Pair(0, 0),
            "Al-Fineates" to Pair(3, -8),
            "Jaber Al-Ali" to Pair(-8, -8)
        )
        val filteredCities = if (selectedCityFromSecondActivity.isNullOrEmpty()) {
            cities
        } else {
            cities.filterKeys { it != selectedCityFromSecondActivity }
        }
        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.list_item_city_checkbox,
            filteredCities.keys.toList()
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    R.layout.list_item_city_checkbox, parent, false
                )
                val cityName = filteredCities.keys.toList()[position]
                val tvCityName = view.findViewById<TextView>(R.id.tvCityName)
                tvCityName.text = cityName
                val tvCoordinates = view.findViewById<TextView>(R.id.tvCoordinates)
                val coordinates = filteredCities[cityName]
                tvCoordinates.text =
                    "At Coordinates (${coordinates?.first}, ${coordinates?.second})"
                val checkBoxCity = view.findViewById<CheckBox>(R.id.checkBoxCity)
                checkBoxCity.isChecked = selectedCities.contains(cityName)
                fun toggleCitySelection(cityName: String, isSelected: Boolean) {
                    if (isSelected) {
                        if (selectedCities.size < maxSelections || selectedCities.contains(cityName)) {
                            selectedCities.add(cityName)
                        } else {
                            Toast.makeText(
                                this@ThirdActivity,
                                "Please select only $maxSelections cities.",
                                Toast.LENGTH_SHORT
                            ).show()
                            checkBoxCity.isChecked = false
                        }
                    } else {
                        selectedCities.remove(cityName)
                    }
                }
                checkBoxCity.setOnClickListener {
                    toggleCitySelection(cityName, checkBoxCity.isChecked)
                }
                view.setOnClickListener {
                    if (!checkBoxCity.isChecked && selectedCities.size >= maxSelections) {
                        Toast.makeText(
                            this@ThirdActivity,
                            "You can select up to $maxSelections cities.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        toggleCitySelection(cityName, !checkBoxCity.isChecked)
                        checkBoxCity.isChecked = !checkBoxCity.isChecked
                    }
                }
                return view
            }
        }
        listViewCities.adapter = adapter
        listViewCities.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = cities.keys.toList()[position]
            if (!selectedCities.contains(selectedCity) && selectedCities.size >= maxSelections) {
                listViewCities.setItemChecked(position, false)
                Toast.makeText(
                    this,
                    "You can select up to $maxSelections cities.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val firstCityName = sharedPref.getString("SELECTED_CITY_NAME", "")

        if (firstCityName != null && firstCityName.isNotEmpty()) {
            print("yes")
        } else {
            findViewById<TextView>(R.id.tvSelectedCity).text = "No starting city selected"
        }
        btnConfirm.setOnClickListener {
            val progressBar: ProgressBar = findViewById(R.id.progressBar)
            progressBar.visibility = View.VISIBLE
            progressBar.isIndeterminate = true
            if (selectedCities.isEmpty()) {
                Toast.makeText(
                    this@ThirdActivity,
                    "Please select at least one city.",
                    Toast.LENGTH_LONG
                ).show()
                progressBar.visibility = View.GONE  // Hide the progress bar
                return@setOnClickListener
            }
            if (selectedCityFromSecondActivity != null && selectedCityFromSecondActivity.isNotEmpty()) {
                print("yes") // Or handle as needed
            } else {
                findViewById<TextView>(R.id.tvSelectedCity).text = "No starting city selected"
            }
            GlobalScope.launch(Dispatchers.IO) {
                val firstCoordX = sharedPref.getInt("SELECTED_COORD_X", 0)
                val firstCoordY = sharedPref.getInt("SELECTED_COORD_Y", 0)
                val combinedCoordinates = mutableListOf(Pair(firstCoordX, firstCoordY))
                val combinedNames = mutableListOf("\"$firstCityName\"")
                selectedCities.forEach { city ->
                    cities[city]?.let { combinedCoordinates.add(it) }
                    combinedNames.add("\"$city\"")
                }
                val fileContents =
                    "c = ${combinedCoordinates.joinToString(separator = ", ")}\nN = ${
                        combinedNames.joinToString(separator = ", ")
                    }"
                openFileOutput("selected_cities.txt", Context.MODE_PRIVATE).use {
                    it.write(fileContents.toByteArray())
                }
                try {
                    if (!Python.isStarted()) {
                        Python.start(AndroidPlatform(this@ThirdActivity))
                    }
                    val py = Python.getInstance()
                    val pyObject = py.getModule("plot_cities1")
                    val fileDir = filesDir.absolutePath
                    val dataFilePath = "$fileDir/selected_cities.txt"
                    val timeStamp =
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val outputImagePath = "$fileDir/output_image_$timeStamp.png"
                    val results = pyObject.callAttr(
                        "generate_plot_and_save",
                        dataFilePath,
                        outputImagePath
                    ).asList()

                    val bestPathText = results[0].toString()
                    val distanceBeforeText = results[1].toString()
                    val bestDistanceText = results[2].toString()
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@ThirdActivity, FourthActivity::class.java).apply {
                            putExtra("IMAGE_PATH", outputImagePath)
                            putExtra("BEST_PATH_TEXT", bestPathText)
                            putExtra("DISTANCE_BEFORE_TEXT", distanceBeforeText)
                            putExtra("BEST_DISTANCE_TEXT", bestDistanceText)
                        }
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@ThirdActivity,
                            "An error occurred: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}