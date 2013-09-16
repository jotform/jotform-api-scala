jotform-api-scala 
===============
[JotForm API](http://api.jotform.com/docs/) - Scala Client


### Installation

Install via git clone:

        $ git clone git://github.com/jotform/jotform-api-scala.git
        $ cd jotform-api-scala
        

### Documentation

You can find the docs for the API of this client at [http://api.jotform.com/docs/](http://api.jotform.com/docs)

### Authentication

JotForm API requires API key for all user related calls. You can create your API Keys at  [API section](http://www.jotform.com/myaccount/api) of My Account page.

### Examples

Print all forms of the user

```scala
package com.jotform.api.samples

import com.jotform.api._
import org.json._

object PrintFormList {
  
  def main(args: Array[String]) {
    val client = new JotForm()
    client.setAPIKey("YOUR API KEY")
    
    val response: JSONObject = client.getForms()
    
    val forms: JSONArray = response.getJSONArray("content")
			
	for (i <- 0 until forms.length()){
		val form: JSONObject = forms.getJSONObject(i)
		
		println(form.get("title") + " (Total:" +form.get("count") + " New:" + form.get("new") + ")")
	}
  }
}
``` 
   
Get submissions of the latest form

```scala
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
``` 

Get latest 100 submissions ordered by creation date

```scala
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
``` 

Submission and form filter examples

```scala
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
``` 

Delete last 50 submissions

```scala
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
``` 

First the _JotForm_ class is included from the _jotform-api-scala/JotForm.scala_ file. This class provides access to JotForm's API. You have to create an API client instance with your API key. 
In case of an exception (wrong authentication etc.), you can catch it or let it fail with a fatal error.

