package com.chaquo.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
    private val maxSelections = 30
    private lateinit var listViewCities: ListView
    private lateinit var btnConfirm: Button
    private lateinit var progressBar: ProgressBar
    private val selectedCities = mutableListOf<String>()
    private lateinit var cities: Map<String, Pair<Int, Int>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        initializeViews()
        loadDataFromIntent()
    }

    private fun loadDataFromIntent() {
        val cityNames = intent.getStringArrayExtra("cityNames") ?: arrayOf()
        val coordX = intent.getIntArrayExtra("coordX") ?: intArrayOf()
        val coordY = intent.getIntArrayExtra("coordY") ?: intArrayOf()

        // Reconstruct the map from the arrays
        cities = cityNames.zip(coordX.zip(coordY)) { name, coords ->
            name to Pair(coords.first, coords.second)
        }.toMap()

        setupListView(cityNames.toList())
    }
    private fun initializeViews() {
        listViewCities = findViewById(R.id.listViewCities)
        btnConfirm = findViewById(R.id.btnConfirmSelection)
        progressBar = findViewById(R.id.progressBar)
        val sharedPref = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        setupConfirmButton(sharedPref)
    }



    private fun setupListView(cities: List<String>) {
        listViewCities.adapter = object : ArrayAdapter<String>(this, R.layout.list_item_city_checkbox, cities) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.list_item_city_checkbox, parent, false)
                val cityName = getItem(position) ?: ""
                val coordinates = this@ThirdActivity.cities[cityName] ?: Pair(0, 0) // Default to (0,0) if not found

                val tvCityName = view.findViewById<TextView>(R.id.tvCityName)
                tvCityName.text = cityName

                val tvCoordinates = view.findViewById<TextView>(R.id.tvCoordinates)
                tvCoordinates.text = "Coordinates: (${coordinates.first}, ${coordinates.second})"
                tvCoordinates.visibility = View.VISIBLE // Show coordinates

                val checkBoxCity = view.findViewById<CheckBox>(R.id.checkBoxCity)
                checkBoxCity.isChecked = selectedCities.contains(cityName)

                checkBoxCity.setOnClickListener {
                    if (checkBoxCity.isChecked && !selectedCities.contains(cityName)) {
                        if (selectedCities.size < maxSelections) {
                            selectedCities.add(cityName)
                        } else {
                            checkBoxCity.isChecked = false // Prevent selection if max is reached
                            Toast.makeText(this@ThirdActivity, "You can select up to $maxSelections cities.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        selectedCities.remove(cityName)
                    }
                }

                view.setOnClickListener {
                    checkBoxCity.performClick()
                }

                return view
            }
        }

        listViewCities.choiceMode = ListView.CHOICE_MODE_MULTIPLE
    }

    private fun setupConfirmButton(sharedPref: SharedPreferences) {
        btnConfirm.setOnClickListener {
            val progressBar: ProgressBar = findViewById(R.id.progressBar)
            progressBar.visibility = View.VISIBLE
            progressBar.isIndeterminate = true

            if (selectedCities.isEmpty()) {
                Toast.makeText(this, "Please select at least one city.", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE  // Hide the progress bar
                return@setOnClickListener
            }

            // Launch coroutine in IO dispatcher
            GlobalScope.launch(Dispatchers.IO) {
                val selectedCityName = sharedPref.getString("SELECTED_CITY_NAME", null) ?: "Unknown City"
                val firstCoordX = sharedPref.getInt("SELECTED_COORD_X", 0)
                val firstCoordY = sharedPref.getInt("SELECTED_COORD_Y", 0)
                val combinedCoordinates = mutableListOf(Pair(firstCoordX, firstCoordY))
                val combinedNames = mutableListOf("\"$selectedCityName\"")
                // Assuming you have the name of the initially selected city

                // Ensure `selectedCities` list contains the names of the cities user has selected
                selectedCities.forEach { cityName ->
                    cities[cityName]?.let {
                        combinedCoordinates.add(it)
                        combinedNames.add("\"$cityName\"")
                    }
                }

                val coordinatesString = combinedCoordinates.joinToString(separator = ", ", prefix = "[", postfix = "]") { "(${it.first}, ${it.second})" }
                val namesString = combinedNames.joinToString(separator = ", ", prefix = "[", postfix = "]")

                val fileContents = "c = $coordinatesString\nN = $namesString"

                openFileOutput("selected_cities.txt", Context.MODE_PRIVATE).use {
                    it.write(fileContents.toByteArray())
                }

                // Continue with the Python script execution
                try {
                    if (!Python.isStarted()) {
                        Python.start(AndroidPlatform(this@ThirdActivity))
                    }
                    val py = Python.getInstance()
                    val pyObject = py.getModule("plot_cities1")  // Adjust the module name as needed
                    val fileDir = filesDir.absolutePath
                    val dataFilePath = "$fileDir/selected_cities.txt"
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val outputImagePath = "$fileDir/output_image_$timeStamp.png"

                    // Execute the Python function
                    val results = pyObject.callAttr("generate_plot_and_save", dataFilePath, outputImagePath).asList()

                    val bestPathText = results[0].toString()
                    val distanceBeforeText = results[1].toString()
                    val bestDistanceText = results[2].toString()

                    // Move to the Main thread to update UI and navigate
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
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
                        Toast.makeText(this@ThirdActivity, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    // This is a placeholder for the actual Python script execution logic.
// Replace it with your actual Python calling code.
    private suspend fun runPythonScript(namesList: String, coordinatesString: String): PythonScriptResult {
        // Ensure progressBar is a member variable for accessibility
        // progressBar.visibility = View.VISIBLE should be set outside this function

        return withContext(Dispatchers.IO) {
            try {
                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(this@ThirdActivity))
                }
                val py = Python.getInstance()
                val pyObject = py.getModule("plot_cities1") // Ensure this matches your actual Python module name
                val fileDir = filesDir.absolutePath
                val dataFilePath = "$fileDir/selected_cities.txt"
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val outputImagePath = "$fileDir/output_image_$timeStamp.png"

                // Here, ensure that 'generate_plot_and_save' matches the function in your Python module
                val results = pyObject.callAttr("generate_plot_and_save", dataFilePath, outputImagePath).asList()

                val bestPathText = results[0].toString()
                val distanceBeforeText = results[1].toString()
                val bestDistanceText = results[2].toString()

                // Return success result
                PythonScriptResult(true, bestPathText, distanceBeforeText, bestDistanceText, outputImagePath)
            } catch (e: Exception) {
                // Return failure result
                PythonScriptResult(false, errorMessage = e.message)
            }
        }.also {
            // This also block ensures progressBar visibility is handled on the main thread after Python script execution
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                if (!it.isSuccess) {
                    Toast.makeText(this@ThirdActivity, "An error occurred: ${it.errorMessage}", Toast.LENGTH_LONG).show()
                } else {
                    // Handle successful execution, e.g., navigating to a results activity
                    val intent = Intent(this@ThirdActivity, FourthActivity::class.java).apply {
                        putExtra("IMAGE_PATH", it.outputImagePath)
                        putExtra("BEST_PATH_TEXT", it.bestPathText)
                        putExtra("DISTANCE_BEFORE_TEXT", it.distanceBeforeText)
                        putExtra("BEST_DISTANCE_TEXT", it.bestDistanceText)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    data class PythonScriptResult(
        val isSuccess: Boolean,
        val bestPathText: String? = null,
        val distanceBeforeText: String? = null,
        val bestDistanceText: String? = null,
        val outputImagePath: String? = null,
        val errorMessage: String? = null
    )
}