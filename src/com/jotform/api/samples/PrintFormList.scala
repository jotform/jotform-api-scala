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