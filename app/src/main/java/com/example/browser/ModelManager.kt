package com.example.browser

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class LocalModelMetadata(
    val id: String,
    val name: String,
    val filePath: String,
    val quantization: String,
    val requiredRamGb: Float
)

class ModelManager(private val context: Context) {
    private val registryFile = File(context.filesDir, "model_registry.json")

    fun getRegisteredModels(): List<LocalModelMetadata> {
        if (!registryFile.exists()) return emptyList()
        val list = mutableListOf<LocalModelMetadata>()
        try {
            val array = JSONArray(registryFile.readText())
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    LocalModelMetadata(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        filePath = obj.getString("filePath"),
                        quantization = obj.getString("quantization"),
                        requiredRamGb = obj.getDouble("requiredRamGb").toFloat()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun registerModel(model: LocalModelMetadata) {
        val current = getRegisteredModels().toMutableList()
        current.removeAll { it.id == model.id }
        current.add(model)

        val array = JSONArray()
        current.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("name", m.name)
            obj.put("filePath", m.filePath)
            obj.put("quantization", m.quantization)
            obj.put("requiredRamGb", m.requiredRamGb.toDouble())
            array.put(obj)
        }
        registryFile.writeText(array.toString())
    }
}
