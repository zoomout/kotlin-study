package com.bogdan

fun main(args: Array<String>) {
    extensionFunc()
}

fun extensionFunc() {
    fun String.addMe(addition: String) = this + addition

    println("someString".addMe("Added"))
}