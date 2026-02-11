package mfabian.snappo.comparison

import mfabian.snappo.Comparison

class BinaryComparison implements Comparison {
  final String extension

  BinaryComparison(String extension = 'bin') {
    this.extension = extension
  }

  @Override
  String fileExtension() {
    extension
  }

  @Override
  Object beforeComparison(Object input) {
    input
  }

  @Override
  byte[] beforeStore(Object input) {
    (byte[]) input
  }

  @Override
  Object afterRestore(byte... bytes) {
    bytes
  }
}
