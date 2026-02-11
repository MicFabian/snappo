package mfabian.snappo.comparison

import mfabian.snappo.Comparison

import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.Arrays

class ArrayComparison implements Comparison {
  BigDecimal minValue = BigDecimal.valueOf(Integer.MIN_VALUE)
  BigDecimal maxValue = BigDecimal.valueOf(Integer.MAX_VALUE)
  List ignoreValues = []
  String columnSeparator = ','
  int rounding = 4

  @Override
  String fileExtension() {
    'csv'
  }

  @Override
  Object beforeComparison(Object input) {
    toCsv(input)
  }

  @Override
  byte[] beforeStore(Object input) {
    toCsv(input).getBytes(StandardCharsets.UTF_8)
  }

  @Override
  Object afterRestore(byte... bytes) {
    new String(bytes, StandardCharsets.UTF_8)
  }

  String toCsv(Object input) {
    toList(input)
      .collect { Object row -> normalizeRow(row) }
      .findAll { List row -> !isEmptyLine(row) }
      .collect { List row -> row.collect { Object v -> v == null ? '' : v }.join(columnSeparator) }
      .join('\n')
  }

  private List normalizeRow(Object row) {
    if (row == null) {
      return [null]
    }
    if (row instanceof Collection) {
      return row.collect { Object v -> normalizeValue(v) }
    }
    if (row.getClass().isArray()) {
      return (row as Object[]).collect { Object v -> normalizeValue(v) }
    }
    [normalizeValue(row)]
  }

  private Object normalizeValue(Object value) {
    if (ignoreValues?.contains(value)) {
      return null
    }
    if (value instanceof Number) {
      BigDecimal bd = new BigDecimal(value.toString())
      if (bd < minValue) {
        bd = minValue
      }
      if (bd > maxValue) {
        bd = maxValue
      }
      return bd.setScale(rounding, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }
    value
  }

  private boolean isEmptyLine(Collection row) {
    row.findAll { Object v -> v != null && String.valueOf(v).trim().length() > 0 }.isEmpty()
  }

  private List toList(Object input) {
    if (input instanceof Collection) {
      return new ArrayList(input as Collection)
    }
    if (input?.getClass()?.isArray()) {
      return Arrays.asList((Object[]) input)
    }
    [input]
  }
}
