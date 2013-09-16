package com.jotform.api.samples

import com.jotform.api._
import org.json._

object Delete50Submissions{
  
  def main(args: Array[String]) {
    val client = new JotForm()
    client.setAPIKey("YOUR API KEY")
    
    val response: JSONObject = client.getSubmissions(0, 2, null, null)
    
    val submissions = response.getJSONArray("content")
    
    for(i <- 0 until submissions.length()) {
      val submission: JSONObject = submissions.getJSONObject(i)
      
      val result: JSONObject = client.deleteSubmission(submission.getLong("id"))
      
      println(submission.getLong("id") + " deleted.")
    }

  }
}

