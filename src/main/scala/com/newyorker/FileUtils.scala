package com.newyorker

import java.io._

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

object FileUtils {

  /**
    * Gets the required files to parse
    * @param dirPath directory location of files
    * @param fileExtn file extension to parse
    * @return
    */
  def getFileList(dirPath: String, fileExtn: String): Option[List[File]] = {
    val inputTarDir = new File(dirPath)
    val parent = inputTarDir.getParent
    var outputPath = new File(parent + "/yelp_dataset")
    outputPath = uncompressTar(inputTarDir, outputPath)
    if (outputPath.exists) {
      Some(outputPath.listFiles.filter { f => f.isFile && f.getName.endsWith(fileExtn) }.toList.sorted)
    } else {
      None
    }
  }

  /**
    * Uncompress the tar file
    * @param inputTarFile Location of input tar file
    * @param outputPath Output location of uncompressed files
    * @return
    */
  def uncompressTar(inputTarFile: File, outputPath: File): File = {
    if (!outputPath.exists()) {
      outputPath.mkdir()
    }
    val tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(inputTarFile))))
    try {
      var tarEntry = tarIn.getNextTarEntry
      while (tarEntry != null) {
        // create a file with the same name as the tarEntry
        val destPath = new File(outputPath, tarEntry.getName)
        if (tarEntry.isDirectory) {
          destPath.mkdirs()
        } else {
          destPath.createNewFile()
          val btoRead = new Array[Byte](1024)
          var bout: BufferedOutputStream = null
          try {
            bout = new BufferedOutputStream(new FileOutputStream(destPath))
            var len = 0
            while (len != -1) {
              len = tarIn.read(btoRead)
              if (len != -1) {
                bout.write(btoRead, 0, len)
              }
            }
          } finally {
            if (bout != null) {
              bout.close()
            }
          }
        }
        tarEntry = tarIn.getNextTarEntry
      }
    } finally {
      if (tarIn != null) {
        tarIn.close()
      }
    }
    outputPath
  }

  /**
    * Get the full qualified path of file
    * @param files List of input files
    * @param outputPath Filename to filter
    * @return
    */
  def getFile(files: List[File], filter: String): String = {
    val localFilePathPrefix = "file:///"
    val path = files.filter { f => f.getAbsolutePath().endsWith(filter) }
    localFilePathPrefix + path.head.getAbsolutePath
  }
}
