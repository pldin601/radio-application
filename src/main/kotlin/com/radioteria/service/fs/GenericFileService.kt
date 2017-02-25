package com.radioteria.service.fs

import com.radioteria.domain.entity.Blob
import com.radioteria.domain.entity.File
import com.radioteria.domain.repository.BlobRepository
import com.radioteria.domain.repository.FileRepository
import com.peacefulbit.util.orElse
import com.radioteria.service.storage.Metadata
import com.radioteria.service.storage.ObjectStorage
import com.peacefulbit.util.sha1
import org.springframework.stereotype.Service
import java.io.InputStream
import javax.transaction.Transactional

@Service
class GenericFileService(
        val blobRepository: BlobRepository,
        val fileRepository: FileRepository,
        val objectStorage: ObjectStorage
) : FileService {

    @Transactional
    override fun put(filename: String, inputStreamSupplier: () -> InputStream): File {
        val contentType = getContentType(filename)
        val sha1 = inputStreamSupplier.invoke().use(::sha1)

        val blob = blobRepository.findByHash(sha1).orElse {
            inputStreamSupplier.invoke().use {
                objectStorage.put(sha1, it, Metadata(contentType = contentType)) }

            val storedObject = objectStorage.get(sha1)

            Blob(contentType = contentType, size = storedObject.length, hash = sha1)
                .apply { blobRepository.save(this) }
        }

        return createAndSaveFile(blob, filename)
    }

    @Transactional
    override fun get(file: File): ObjectStorage.Object {
        val blob = blobRepository.findOne(file.blob.id!!)!!
        return objectStorage.get(blob.hash)
    }

    @Transactional
    override fun delete(file: File) {
        val fileBlob = file.blob

        fileRepository.delete(file)

        if (fileRepository.findAllByBlob(fileBlob).isEmpty()) {
            deleteBlobAndDataObject(fileBlob)
        }
    }

    private fun deleteBlobAndDataObject(blob: Blob) {
        objectStorage.delete(blob.hash)
        blobRepository.delete(blob)
    }

    @Transactional
    override fun markPermanent(file: File) {
        file.isPermanent = true
        fileRepository.save(file)
    }

    private fun createAndSaveFile(blob: Blob, filename: String): File {
        return File(name = filename, blob = blob)
                .apply { fileRepository.save(this) }
    }

    private fun getContentType(filename: String): String {
        return "application/octet-stream"
    }

}