package com.akhnaton.foodvisits.ui.home.cardPrint

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.akhnaton.foodvisits.data.statusValue.cardPrint.CardPrintIntent
import com.akhnaton.foodvisits.data.statusValue.cardPrint.CardPrintStatus
import com.akhnaton.foodvisits.databinding.FragmentCardPrintDetailsBinding
import com.akhnaton.foodvisits.shared.DialogUtils
import com.akhnaton.foodvisits.shared.SharedPreferencesHelper
import com.akhnaton.foodvisits.ui.auth.LoginActivity2
import com.akhnaton.foodvisits.ui.home.MainActivity
import com.akhnaton.foodvisits.ui.home.printFood.PrinterManager
import com.akhnaton.foodvisits.utils.NumberFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CardPrintDetailsFragment : Fragment() {

    private var _binding: FragmentCardPrintDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CardPrintViewModel by viewModels()
    private val orderSalesNumber: String by lazy {
        arguments?.getString("orderSalesNumber") ?: ""
    }
    private lateinit var adapter: CardPrintDetailsAdapter
    private lateinit var printMe: PrinterManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardPrintDetailsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        printMe = PrinterManager(requireContext())

        adapter = CardPrintDetailsAdapter(emptyList())
        binding.rvInvoiceDetails.layoutManager = LinearLayoutManager(requireContext())
        binding.rvInvoiceDetails.adapter = adapter
        binding.rvInvoiceDetails.isNestedScrollingEnabled = false
        binding.rvInvoiceDetails.itemAnimator = null

        binding.tvInvoiceNumber.text = orderSalesNumber
        binding.tvDateTime.text = SimpleDateFormat("dd/MM/yyyy", Locale("ar")).format(Date())
        binding.tvTime.text = SimpleDateFormat("hh:mm:ss a", Locale("ar")).format(Date())

        observeStatus()

        viewModel.cardPrintIntent.trySend(
            CardPrintIntent.GetPrintInvoiceDetails(orderSalesNumber)
        )

        binding.btnPrint.setOnClickListener {
            binding.tvDateTime.text = SimpleDateFormat("dd/MM/yyyy", Locale("ar")).format(Date())
            binding.tvTime.text = SimpleDateFormat("hh:mm:ss a", Locale("ar")).format(Date())
            lifecycleScope.launch {
                delay(400)
                val bitmap = createBitmapFromView(binding.printMeLayout)
                bitmap?.let { fullBitmap ->
                    if (printMe.isPrinterAvailable()) {
                        val chunks = splitBitmapIntoChunks(fullBitmap)
                        printMe.printBitmapChunksSuspend(chunks)
                        printMe.feedPaperSuspend(4)
                    } else {
                        exportBitmapAsPdf(fullBitmap)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MainActivity.binding.navView2.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
          MainActivity.binding.navView2.visibility = View.VISIBLE
//        MainActivity.binding.gooeyMenu.visibility = View.VISIBLE
//        MainActivity.binding.gooeyMenu.openCloseMenu(true)
    }

    private fun splitBitmapIntoChunks(bitmap: Bitmap, chunkHeight: Int = 256): List<Bitmap> {
        val chunks = mutableListOf<Bitmap>()
        var y = 0
        while (y < bitmap.height) {
            val height = minOf(chunkHeight, bitmap.height - y)
            val chunk = Bitmap.createBitmap(bitmap, 0, y, bitmap.width, height)
            chunks.add(chunk)
            y += height
        }
        return chunks
    }

    private fun createBitmapFromView(view: View, targetWidth: Int = 384): Bitmap? {

        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(view.left, view.top, view.left + view.measuredWidth, view.top + view.measuredHeight)

        val originalWidth = view.measuredWidth
        val originalHeight = view.measuredHeight

        if (originalWidth <= 0 || originalHeight <= 0) return null

        val scale = targetWidth.toFloat() / originalWidth.toFloat()
        val targetHeight = (originalHeight * scale).toInt()

        val bitmap = Bitmap.createBitmap(
            targetWidth,
            targetHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        canvas.scale(scale, scale)

        view.draw(canvas)

        return bitmap
    }

    private fun exportBitmapAsPdf(bitmap: Bitmap) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        val fileName = "Invoice_${orderSalesNumber}_${System.currentTimeMillis()}.pdf"
        val outputDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(outputDir, fileName)

        try {
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)
        } catch (e: Exception) {
            pdfDocument.close()
            e.printStackTrace()
        }
    }

    private fun observeStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.status.collect { status ->
                    when (status) {
                        is CardPrintStatus.GetPrintInvoiceDetails -> {
                            if (status.response.status == 401) {
                                sendRefreshToken()
                            } else {
                                val info = status.response.data.invoice_info
                                val details = status.response.data.invoice_details

                                binding.invoiceInfo = info
                                adapter.updateList(details)

                                binding.printMeLayout.post {
                                    binding.rvInvoiceDetails.requestLayout()
                                    binding.printMeLayout.requestLayout()
                                }

                                val totalVat = details.sumOf { it.tax_value }
                                val totalWithoutTax = info.invoice_total_value - totalVat

                                binding.tvVat.text = NumberFormatter.format(totalVat)
                                binding.tvTotalInvoiceWithoutTax.text = NumberFormatter.format(totalWithoutTax)
                                binding.tvTotalInvoice.text = NumberFormatter.format(info.invoice_total_value)

                                binding.executePendingBindings()
                            }
                        }
                        is CardPrintStatus.RefreshToken -> {
                            if (status.data.status == 200) {
                                val tokenData = com.google.gson.Gson().fromJson(
                                    status.data.data,
                                    com.akhnaton.foodvisits.data.model.refreshToken.Data::class.java
                                )
                                SharedPreferencesHelper.getInstance().saveUserToken(tokenData.TOKEN)
                            } else {
                                DialogUtils.showResultDialog(
                                    context = requireContext(),
                                    message = status.data.message,
                                    isSuccess = false,
                                    showOkButton = true,
                                    onOk = {
                                        SharedPreferencesHelper.getInstance().logOut()
                                        startActivity(Intent(requireContext(), LoginActivity2::class.java))
                                        requireActivity().finishAffinity()
                                    })
                            }
                        }
                        is CardPrintStatus.Error -> {}
                        else -> {}
                    }
                }
            }
        }
    }

    private fun sendRefreshToken() {
        lifecycleScope.launch {
            viewModel.cardPrintIntent.send(
                CardPrintIntent.RefreshToken(
                    SharedPreferencesHelper.getInstance().getEmployeeId(),
                    SharedPreferencesHelper.getInstance().getUserToken()
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}