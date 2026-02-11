package de.xm.snappo

interface Comparison {
  String fileExtension()

  Object beforeComparison(Object input)

  byte[] beforeStore(Object input)

  Object afterRestore(byte... bytes)
}
