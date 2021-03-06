package com.etna.mob3.mob3

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Toast
import com.etna.mob3.mob3.classes.CustomAdapter
import com.etna.mob3.mob3.classes.DataModel
import com.etna.mob3.mob3.classes.FileDatas
import com.etna.mob3.mob3.tools.Tools
import kotlinx.android.synthetic.main.content_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private var adapter: CustomAdapter? = null

    private val READ_REQUEST_CODE: Int = 42
    private val FILE_SELECT_CODE: Int = 1
    private val TAG: String = "MainActivity"
    private val DL_DIR: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
    private val APP_DIR: String = "/storage/emulated/0/Meteo/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fillList()

        downloadButton.setOnClickListener {
            downloadButtonPressed()
        }

        fileList.setOnItemClickListener { parent, view, position: Int, id ->

            val fileData: DataModel = fileList.getItemAtPosition(position) as DataModel

            Log.d("hello", "" + fileData.name)

            val selectedFile : File = File(APP_DIR + fileData.name)

            val intent = Intent(this, MeteoInfoActivity::class.java)

            intent.putExtra("file", selectedFile)

            startActivity(intent)
        }
    }

    private fun downloadButtonPressed() {
        Log.d("download","download button pressed")
        showFileChooser()
        // this.showCheckbox()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showFileChooser() {

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE)
        } catch (ex: android.content.ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var dl_file: File
        var cp_file: File
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            Log.d("Info", "Intent is null, no file selected")
        } else {

            when (requestCode) {
                FILE_SELECT_CODE -> if (resultCode == Activity.RESULT_OK) {
                    // Get the Uri of the selected file
                    val uri = data.data
                    // Get the path in file utils
                    var path = uri.path.removePrefix("/document/raw:")
                    Log.d("path", path)

                    // Pour get le filename on crée une variable de type File() pour
                    // avoir accès à la méthode getName()
                    dl_file = File(path)
                    var file_name: String = Tools.getFileDate(dl_file)

                    // On modifie la route du dl file pour que celui ci soit
                    // utilisable par la fonction copyTo
                    cp_file = File(APP_DIR + file_name)

                    // On try catch pour éviter que si le fichier n'est pas trouvable on bloque
                    // L'application
                    try {
                        dl_file.copyTo(cp_file, true)
                        Log.d("Result", "files copied")
                        Log.d("Resultpath", cp_file.canonicalPath)
                    } catch (e: Exception) {
                        Log.d(TAG, e.toString())
                    }
                }
            }
        }

        fillList()

    }

    fun fillList() {

        var dataModels: ArrayList<DataModel> = ArrayList()
        adapter = CustomAdapter(dataModels, this)

        File(APP_DIR).walkTopDown().forEach {
            if (it.name != "Meteo") {
                dataModels.add(DataModel(it.name, it.path))
            }
        }

        //if (dataModels.size > 0) {
        this.fileList.setAdapter(adapter)
        
        this.fileList.isLongClickable = true

        this.fileList.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            Log.d("press", "long press")

            showDeleteAlert(position)

            true
        }
    }

    private fun showDeleteAlert(position: Int) {
        val alertDialog = AlertDialog.Builder(this).create()

        alertDialog.setTitle("Delete")
        alertDialog.setMessage("Are you sure that you want to delete this file ?")

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", {
            dialogInterface, i ->

            this.removeFileAt(position)

            Toast.makeText(applicationContext, "The file has been deleted", Toast.LENGTH_SHORT).show()
        })

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", {
            dialogInterface, i ->
            Toast.makeText(applicationContext, "File not deleted", Toast.LENGTH_SHORT).show()
        })

        alertDialog.show()
    }

    fun removeFileAt(position: Int) {

        this.adapter?.removeFile(position)

        fillList()
    }
}