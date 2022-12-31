package dev.bitspittle.racketeer.site.components.util

import kotlinx.browser.document
import org.w3c.dom.Document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.dom.url.URL as DomURL

fun Document.downloadFileToDisk(
    filename: String,
    type: String,
    content: String,
) {
    val snapshotBlob = Blob(arrayOf(content), BlobPropertyBag(type))
    val url = DomURL.createObjectURL(snapshotBlob)
    val tempAnchor = (createElement("a") as HTMLAnchorElement).apply {
        style.display = "none"
        href = url
        download = filename
    }
    document.body!!.append(tempAnchor)
    tempAnchor.click()
    DomURL.revokeObjectURL(url)
    tempAnchor.remove()
}

fun Document.loadFileFromDisk(
    accept: String,
    onLoaded: (String) -> Unit,
) {
   val tempInput = (createElement("input") as HTMLInputElement).apply {
        type = "file"
        style.display = "none"
        this.accept = accept
        multiple = false
    }

    tempInput.onchange = { changeEvt ->
        val file = changeEvt.target.asDynamic().files[0] as File

        val reader = FileReader()
        reader.onload = { loadEvt ->
            val content = loadEvt.target.asDynamic().result as String
            onLoaded(content)
        }
        reader.readAsText(file, "UTF-8")
    }

    body!!.append(tempInput)
    tempInput.click()
    tempInput.remove()
}