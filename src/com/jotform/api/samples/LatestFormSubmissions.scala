package com.jotform.api.samples

import com.jotform.api._
import org.json._

object LatestFormSubmissions {
  
  def main(args: Array[String]) {
    val client = new JotForm()
    client.setAPIKey("YOUR API KEY")
    
    val response: JSONObject = client.getForms(0, 1, null, null)
    
    val forms: JSONArray = response.getJSONArray("content")
    
    val latestForm: JSONObject = forms.getJSONObject(0)
    
    val latestFormID: Long = latestForm.getLong("id")
    
    val submissions: JSONObject = client.getFormSubmissions(latestFormID)
    
    println(submissions)

  }
}
