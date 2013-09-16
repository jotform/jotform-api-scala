package com.jotform.api.samples

import com.jotform.api._
import org.json._

object SubmissionFormFilters {
  
  def main(args: Array[String]) {
    val client = new JotForm()
    client.setAPIKey("YOUR API KEY")
    
    val submissionfilter = Map("id:gt" -> "244605793257787946", "created_at:gt" -> "2013-09-06 03:18:44")
    
    val submissions: JSONObject = client.getSubmissions(0, 0, submissionfilter, null)

    println(submissions)
    
    val formFilter = Map("id:gt" -> "32522773744962")
    
    val forms: JSONObject = client.getForms(0, 0, formFilter, null)
    
    println(forms)
  }
}
