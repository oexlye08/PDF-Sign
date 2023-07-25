package id.my.okisulton.pdfsign

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.my.okisulton.pdfsign.databinding.ActivityFormSubmitBinding

class FormSubmitActivity : AppCompatActivity() {
    private var _binding: ActivityFormSubmitBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFormSubmitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val files = filesDir.listFiles().filter { it.name == "submitted_form.pdf" }
        binding.pdfView.fromFile(files[0]).load()
    }
}