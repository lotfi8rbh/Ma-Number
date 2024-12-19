package fr.mastersd.sime.rabah.manumber

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.mastersd.sime.rabah.manumber.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
