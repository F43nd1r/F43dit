package com.faendir.om.online

class SyntaxException(message: String, val line: Int) : Exception(message) {
}