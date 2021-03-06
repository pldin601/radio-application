package com.radioteria.service.storage

import com.fasterxml.jackson.databind.ObjectMapper
import com.radioteria.config.spring.logging.Logging
import com.peacefulbit.util.copyToAndClose
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.io.*

@Logging
@ConditionalOnProperty(value = "radioteria.storage", havingValue = "local")
@Service
class LocalObjectStorage(@Value("\${radioteria.storage.local.dir}") val root: File) : ObjectStorage {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(LocalObjectStorage::class.java)
    }

    init {
        root.exists() || root.mkdirs() || throw IOException("Directory '$root' could not be created.")
    }

    override fun has(key: String): Boolean {
        val isContentFileExists = getContentFile(key).exists()
        val isMetadataFileExists = getMetadataFile(key).exists()

        if (isContentFileExists != isMetadataFileExists) {
            logger.warn("Content file and metadata file existence are different ({}, {}, {}).",
                    key, isContentFileExists, isMetadataFileExists)
        }

        return getContentFile(key).exists() && getMetadataFile(key).exists()
    }

    override fun get(key: String): ObjectStorage.Object {
        val contentFile = getContentFile(key)
        val objectMetadata = readMetadata(key)

        val objectLength: Long = contentFile.length()

        return ObjectStorage.Object(key,  objectLength, objectMetadata, { FileInputStream(contentFile) })
    }

    override fun delete(key: String) {
        if (!has(key)) {
            logger.warn("Object does not exist ({}).", key)
        }

        getMetadataFile(key).delete()
        getContentFile(key).delete()
    }

    override fun put(key: String, inputStream: InputStream, metadata: Metadata) {
        if (has(key)) {
            logger.warn("Object already exists and will be overwritten ({}).", key)
        }

        val contentFile = getContentFile(key)

        createParentDirsIfNecessary(contentFile)

        inputStream.copyToAndClose(FileOutputStream(contentFile))

        writeMetadata(key, metadata)
    }

    private fun createParentDirsIfNecessary(contentFile: File) {
        if (!contentFile.parentFile.exists()) {
            contentFile.parentFile.mkdirs()
        }
    }

    private fun getContentFile(key: String): File {
        val filename = keyToFileName(key)
        return File("$root${File.separator}$filename.blob")
    }

    private fun getMetadataFile(key: String): File {
        val filename = keyToFileName(key)
        return File("$root${File.separator}$filename.json")
    }

    private fun readMetadata(key: String): Metadata {
        return FileInputStream(getMetadataFile(key)).use {
            ObjectMapper().readValue(it, Metadata::class.java)
        }
    }

    private fun writeMetadata(key: String, metadata: Metadata) {
        val bytes = ObjectMapper().writeValueAsBytes(metadata)
        FileOutputStream(getMetadataFile(key)).use { it.write(bytes) }
    }

    private fun keyToFileName(key: String): String {
        return key.replace(File.separatorChar, '.')
    }

}