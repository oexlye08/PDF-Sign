package id.my.okisulton.pdfsign

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import id.my.okisulton.pdfsign.databinding.ActivityYourSignBinding
import id.my.okisulton.pdfsign.utils.Cosntants.KEY_SIGN
import id.my.okisulton.pdfsign.utils.PaintView
import java.io.ByteArrayOutputStream

class YourSignActivity : AppCompatActivity() {
    private var _binding: ActivityYourSignBinding? = null
    private val binding get() = _binding!!


    private lateinit var mPaintView: PaintView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityYourSignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mPaintView = PaintView(this, null)
        binding.llCanvas.addView(mPaintView, 0)
        mPaintView.requestFocus()
    }

    fun onClick(view: View) {

        when (view.id) {
            R.id.ivDelete -> {
                mPaintView = PaintView(this, null)
                binding.llCanvas.addView(mPaintView, 0)
                mPaintView.requestFocus()
            }
            R.id.btnSave -> saveBitmap()
        }
    }

    private fun saveBitmap() {

        if (mPaintView.arl.size == 0) {
            Toast.makeText(this, "Signature not valid", Toast.LENGTH_LONG).show()
            return
        }

        // View view = mLlCanvas.getRootView();
        val view = binding.llCanvas.getChildAt(0) as View
        view.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
        val intent = Intent()
        intent.putExtra(KEY_SIGN, encoded)
        setResult(RESULT_OK, intent)
        finish()
    }
}