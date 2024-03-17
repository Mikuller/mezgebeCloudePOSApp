package com.mikucode.mymezgebcloudposapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mikucode.mymezgebcloudposapp.R
import com.mikucode.mymezgebcloudposapp.adaptersAndViewModels.SharedViewModel
import com.mikucode.mymezgebcloudposapp.classes.Item
import com.mikucode.mymezgebcloudposapp.classes.ItemModule

class BarCodeReaderFragment : Fragment() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView
    private lateinit var btnGotoCounter: Button
    private lateinit var itemModule: ItemModule
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sharedViewModel: SharedViewModel
    private var itemList: ArrayList<Item> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bar_code_reader, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE)
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.beep)//initialize beep sound
        itemModule = ItemModule(sharedPreferences, requireContext())
        btnGotoCounter = view.findViewById(R.id.btnGotoCounter)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        btnGotoCounter.setOnClickListener {

            if(itemList.isNotEmpty()){
                for(item in itemList){
                    sharedViewModel.addItem(item)
                    sharedViewModel.countClick(item.itemID)
                }
            }
            //go to items fragment when add new sales card view is clicked
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.navBar)
            bottomNav?.selectedItemId = R.id.counter
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container, CounterFragment())
            fragmentTransaction.commit()
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(requireContext(), scannerView)
        setUpCameraPermission()
    }
    private fun codeScanner() {
        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                scannerView.setOnClickListener {
                    codeScanner.startPreview()
                }

                if(it.text.isNotEmpty()){
                    mediaPlayer.start()

                    itemModule.getItemVariantFromBarCode(it.text.trim()){item->
                        if (item != null) {
                            Toast.makeText(requireContext(), "Scan result: ${item.itemName}" +" "+ item.variantName, Toast.LENGTH_SHORT).show()
                            itemList.add(item)

                        }else{
                            Toast.makeText(requireContext(), "No Item is registered with this bar Code", Toast.LENGTH_LONG).show()
                        }
                    }
                }else{
                        codeScanner.startPreview()
                }

            }
        }

        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun setUpCameraPermission(){
        val permission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
        else{
            codeScanner()
        }
    }

    private fun makeRequest() {
        permissionRequest.launch(android.Manifest.permission.CAMERA)
    }


    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
          Toast.makeText(requireContext(), "You need Camera Permission to Read Bar Code",Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        if(itemList.isNotEmpty()){
            for(item in itemList){
                sharedViewModel.addItem(item)
                sharedViewModel.countClick(item.itemID)
            }
        }
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }



}