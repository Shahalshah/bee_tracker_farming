package com.example.ben.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ben.databinding.ActivityAddHiveBinding
import com.example.ben.models.Hive
import com.example.ben.utils.FirebaseUtils

class AddHiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHiveBinding
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        latitude = intent.getDoubleOfDefault("LAT", 0.0)
        longitude = intent.getDoubleOfDefault("LNG", 0.0)
        
        binding.etLocation.setText("$latitude, $longitude")

        binding.btnSaveHive.setOnClickListener {
            saveHive()
        }
    }
    
    // Extension for convenience
    private fun android.content.Intent.getDoubleOfDefault(key: String, default: Double): Double {
        return if (hasExtra(key)) getDoubleExtra(key, default) else default
    }

    private fun saveHive() {
        val name = binding.etHiveName.text.toString().trim()
        val desc = binding.etDescription.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter hive name", Toast.LENGTH_SHORT).show()
            return
        }

        val id = FirebaseUtils.hivesRef().push().key ?: return
        val hive = Hive(
            id = id,
            beekeeperId = FirebaseUtils.currentUserUid ?: "",
            name = name,
            latitude = latitude,
            longitude = longitude,
            description = desc
        )

        FirebaseUtils.hivesRef().child(id).setValue(hive).addOnSuccessListener {
            Toast.makeText(this, "Hive added!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
