package com.example.touuri.data

import android.content.Context
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ExportService(private val context: Context) {
    fun exportToCsv(trees: List<TreeData>): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val fileName = "trees_${dateFormat.format(Date())}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)

        CSVWriter(FileWriter(file)).use { writer ->
            // Write header
            writer.writeNext(arrayOf(
                "Species",
                "Height (m)",
                "Diameter (cm)",
                "Health Status",
                "Age (years)",
                "Latitude",
                "Longitude",
                "Notes",
                "Timestamp",
                "Image URL",
                "OSM ID"
            ))

            // Write data
            trees.forEach { tree ->
                writer.writeNext(arrayOf(
                    tree.species,
                    tree.height?.toString() ?: "",
                    tree.diameter?.toString() ?: "",
                    tree.healthStatus.name,
                    tree.age?.toString() ?: "",
                    tree.latitude?.toString() ?: "",
                    tree.longitude?.toString() ?: "",
                    tree.notes,
                    tree.timestamp.toString(),
                    tree.imageUrl ?: "",
                    tree.osmId ?: ""
                ))
            }
        }

        return file
    }
} 