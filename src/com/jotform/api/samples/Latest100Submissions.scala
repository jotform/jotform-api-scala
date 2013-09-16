package com.jotform.api.samples

import com.jotform.api._
import org.json._

object Latest100Submissions {
  
  def main(args: Array[String]) {
    val client = new JotForm()
    client.setAPIKey("YOUR API KEY")
    
    val response: JSONObject = client.getSubmissions(0, 100, null, "created_at")

    println(response)

  }
}
