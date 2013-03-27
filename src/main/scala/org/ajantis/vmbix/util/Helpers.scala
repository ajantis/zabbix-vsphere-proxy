package org.ajantis.vmbix.util

/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
object Helpers {

  def logTimeTaken[T](marker: String, logFunc: String => Unit)(block: => T): T = {
    val start = System.currentTimeMillis
    val result = block
    val end = System.currentTimeMillis
    logFunc(s"Time taken for ${marker} is ${end - start} ms")
    result
  }

}
