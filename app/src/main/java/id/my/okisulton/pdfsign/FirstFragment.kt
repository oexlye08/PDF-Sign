package id.my.okisulton.pdfsign

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore.Images.Media.getBitmap
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.Display
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.navigation.fragment.findNavController
import com.github.barteksc.pdfviewer.link.DefaultLinkHandler
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.github.kittinunf.fuel.Fuel
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import id.my.okisulton.pdfsign.databinding.DialogTermsConditionBinding
import id.my.okisulton.pdfsign.databinding.FragmentFirstBinding
import id.my.okisulton.pdfsign.utils.Cosntants.KEY_SIGN
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.text.DateFormat
import java.util.Calendar

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var file: File


    var imgBase64 = ""
    lateinit var bitmap: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filePath = activity?.filesDir!!.path + "/registration_form.pdf"
        val url = ""
        downloadPdfFile(url, filePath) { success, message ->
            if (success) {
                Log.d(TAG, "onViewCreated: success")
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()

            } else {
                Log.d(TAG, "onViewCreated: failed")
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            showPdf(filePath)
        }



        val contract = registerForActivityResult(Contract()) {
            imgBase64 = it
            if (TextUtils.isEmpty(imgBase64)) {
                Toast.makeText(requireContext(), "Invalid Signature. Sign Again", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    bitmap = getBitmap(imgBase64)!!
                    binding.labelSignature.visibility = View.GONE
                    binding.ivSignature.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.ivSignature.setOnClickListener {
            contract.launch(Unit)
        }

        binding.btnAdd.setOnClickListener {

            if (imgBase64.isNotBlank()) agreeTermsDialog()
            else Toast.makeText(
                requireContext(),
                "Add Signature",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun agreeTermsDialog() {

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)

        val dialogBinding = DialogTermsConditionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.buttonYes.setOnClickListener {
            addSignatureToPdf()
            dialog.dismiss()
        }

        dialogBinding.buttonNo.setOnClickListener { dialog.cancel() }

        dialog.show()
        val display: Display = activity?.windowManager!!.defaultDisplay
        val width: Int = display.width - 100
        val window: Window? = dialog.window
        window?.setLayout(width, ActionBar.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun addSignatureToPdf() {

        try {

            val getPdf: InputStream = FileInputStream(file)
            val doc = PDDocument.load(getPdf)
            var page: PDPage?
            var contentStream: PDPageContentStream
            val setImage = JPEGFactory.createFromImage(doc, bitmap)
            val currentDate =
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Calendar.getInstance().time)


            for (pageNo in 0 until doc.numberOfPages) {
                page = doc.getPage(pageNo)
                contentStream = PDPageContentStream(doc, page, true, true, true)
//                contentStream.beginText()
//                contentStream.setFont(PDType1Font.COURIER_BOLD, 14F)
//                contentStream.newLineAtOffset(430F, 100F)
//                contentStream.showText(currentDate)
//                contentStream.endText()
                contentStream.drawImage(setImage, 420F, 25F, 100F, 100F)
                contentStream.close()
            }

            doc.save("${requireContext().filesDir.path}/submitted_form.pdf")
            doc.close()
            startActivity(Intent(requireActivity(), FormSubmitActivity::class.java))
            requireActivity().finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showPdf(filePath: String) {
        val files = requireActivity().filesDir.listFiles().filter { it.name == "registration_form.pdf" }
        file = files[0]
        Log.d(TAG, "showPdf: $file")
//        val files = filePath
//        file = File(files)
        binding.pdfView.fromFile(file)
            .enableSwipe(true)

            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
            .password(null)
            .scrollHandle(null)
            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
            // spacing between pages in dp. To define spacing color, set view background
            .spacing(0)
            .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
            .pageFitPolicy(FitPolicy.WIDTH) // mode to fit pages in the view
            .fitEachPage(false) // fit each page to the view, else smaller pages are scaled relative to largest page.
            .pageSnap(false) // snap pages to screen boundaries
            .pageFling(false) // make a fling change only a single page like ViewPager
            .nightMode(false) // toggle night mode


            .load()
    }

    private fun downloadPdfFile(url: String, filePath: String, onComplete: (Boolean, String?) -> Unit) {
        Fuel.download(url)
            .fileDestination { _, _ -> File(filePath) }
            .progress { readBytes, totalBytes ->
                val progress = (readBytes.toFloat() / totalBytes.toFloat()) * 100
                // Anda dapat melakukan sesuatu dengan progres ini, jika diperlukan
            }
            .response { result ->
                val (data, error) = result
                if (error != null) {
                    onComplete(false, "Gagal mendownload file: ${error.message}")
                } else {
                    showPdf(filePath)
                    Log.d(TAG, "downloadPdfFile: $filePath")
                    onComplete(true, "File PDF berhasil didownload dan disimpan di: $filePath")
                }
            }
    }



    inner class Contract : ActivityResultContract<Unit, String>() {

        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(context, YourSignActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String {
            return if (intent != null) {
                intent.getStringExtra(KEY_SIGN)!!
            } else ""

        }

    }

    private fun getBitmap(encodedString: String): Bitmap? {
        return try {
            val decodedString = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: java.lang.Exception) {
            e.message
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "FirstFragment"
    }
}