package com.example.d_id_videostream.commons

object Constants {
    val name= "bW9hdGF6Lm5vYW1hbjEyQGdtYWlsLmNvbQ"
    val password= "xL5VxSt38tfb0o1XlHriC"

    val header = mapOf(
        "content-type" to "application/json",
        "accept" to "application/json",
        "authorization" to " Basic $name:$password"
    )
}