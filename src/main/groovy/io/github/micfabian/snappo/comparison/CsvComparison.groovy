package io.github.micfabian.snappo.comparison

import io.github.micfabian.snappo.Comparison

import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.Arrays

class CsvComparison implements Comparison {
  char columnSeparator = ','
  String recordSeparator = '\n'

  @Override
  String fileExtension() {
    'csv'
  }

  @Override
  Object beforeComparison(Object input) {
    toRows(input)
  }

  @Override
  byte[] beforeStore(Object input) {
    renderCsv(toRows(input)).getBytes(StandardCharsets.UTF_8)
  }

  @Override
  Object afterRestore(byte... bytes) {
    new String(bytes, StandardCharsets.UTF_8)
  }

  private List<List<String>> toRows(Object input) {
    if (input == null) {
      return []
    }
    if (input instanceof CharSequence) {
      return parseCsv(input.toString())
    }
    if (input instanceof Collection) {
      return new ArrayList<>(input as Collection).collect { Object row -> normalizeRow(row) }
    }
    if (input.getClass().isArray()) {
      return Arrays.asList((Object[]) input).collect { Object row -> normalizeRow(row) }
    }
    throw new RuntimeException("CsvComparison input must provide CSV text or tabular rows, input was ${input?.class}")
  }

  private List<String> normalizeRow(Object row) {
    if (row == null) {
      return ['']
    }
    if (row instanceof Collection) {
      return (row as Collection).collect { Object value -> normalizeValue(value) }
    }
    if (row.getClass().isArray()) {
      return ((Object[]) row).collect { Object value -> normalizeValue(value) }
    }
    [normalizeValue(row)]
  }

  private String normalizeValue(Object value) {
    value == null ? '' : String.valueOf(value)
  }

  private List<List<String>> parseCsv(String input) {
    if (input.isEmpty()) {
      return []
    }

    List<List<String>> rows = []
    List<String> row = []
    StringBuilder field = new StringBuilder()
    boolean inQuotes = false
    boolean justEndedRow = false

    for (int index = 0; index < input.length(); index++) {
      char current = input.charAt(index)

      if (inQuotes) {
        if (current == '"') {
          if (index + 1 < input.length() && input.charAt(index + 1) == '"') {
            field.append('"')
            index++
          } else {
            inQuotes = false
          }
        } else {
          field.append(current)
        }
        justEndedRow = false
        continue
      }

      if (current == '"') {
        if (field.length() > 0) {
          throw invalidCsv()
        }
        inQuotes = true
        justEndedRow = false
        continue
      }

      if (current == columnSeparator) {
        row.add(field.toString())
        field.setLength(0)
        justEndedRow = false
        continue
      }

      if (current == '\n' || current == '\r') {
        row.add(field.toString())
        rows.add(row)
        row = []
        field.setLength(0)
        justEndedRow = true
        if (current == '\r' && index + 1 < input.length() && input.charAt(index + 1) == '\n') {
          index++
        }
        continue
      }

      field.append(current)
      justEndedRow = false
    }

    if (inQuotes) {
      throw invalidCsv()
    }

    if (!justEndedRow || !row.isEmpty() || field.length() > 0) {
      row.add(field.toString())
      rows.add(row)
    }

    rows
  }

  private String renderCsv(List<List<String>> rows) {
    rows.collect { List<String> row ->
      row.collect { String value -> escapeValue(value) }.join(String.valueOf(columnSeparator))
    }.join(recordSeparator)
  }

  private String escapeValue(String value) {
    String normalized = value == null ? '' : value
    boolean requiresQuotes = normalized.contains(String.valueOf(columnSeparator)) ||
      normalized.contains('"'.toString()) ||
      normalized.contains('\n'.toString()) ||
      normalized.contains('\r'.toString())

    if (!requiresQuotes) {
      return normalized
    }

    "\"${normalized.replace('\"', '\"\"')}\""
  }

  private RuntimeException invalidCsv() {
    new RuntimeException('CsvComparison input must provide valid CSV text')
  }
}
