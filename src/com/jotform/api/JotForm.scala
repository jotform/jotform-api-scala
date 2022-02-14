/**
 * JotForm API - Scala Client
 *
 * @copyright   2013 Interlogy, LLC.
 * @link        http://www.jotform.com
 * @version     1.0
 * @package     JotFormAPI
 */

package com.jotform.api

import org.apache.commons._
import org.apache.http._
import org.apache.http.client._
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicNameValuePair
import org.apache.http.protocol.HTTP
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.utils.URIBuilder

import org.json._

import scala.io.Source
import scala.collection.Map

import java.io._
import java.io.InputStream
import java.util.ArrayList
import java.net.URI

class JotForm() {
  private var apiKey: String = null
  private var debugMode: Boolean = false
  
  private final val baseURL: String = "https://api.jotform.com/"
  private final val apiVersion: String = "v1"
  
  def setAPIKey(key: String){
    apiKey = key
  }
    
  def getDebugMode(): Boolean = {
    return debugMode
  }
  
  def setDebugMode(value: Boolean) {
    debugMode = value
  }
  
  def debug(str: String) {
    if(debugMode){
      println(str)
    }
  }
  
  private def executeHttpRequest(path: String, parameters: Map[String, String], method: String): JSONObject = {
    val client: DefaultHttpClient = new DefaultHttpClient()
  
    var resp: HttpResponse = null
  
    debug(baseURL + apiVersion + path)
    if (parameters != null){
      debug(parameters.toString())
    }
    
    if(method == "GET") {
		var req = new HttpGet(baseURL + apiVersion + path)
		req.addHeader("apiKey", apiKey)
		
        var uri: URI = null
        var ub: URIBuilder = new URIBuilder(req.getURI())
        
		if(parameters != null) {
	        parameters.keys.foreach{key =>
	          uri = ub.addParameter(key, parameters(key)).build()
	        }
	        req.setURI(uri)
		}

    	resp = client.execute(req)
    } else if (method == "POST") {
	    var req = new HttpPost(baseURL + apiVersion + path)
	    req.addHeader("apiKey", apiKey)
	    
	    if(parameters != null){
      	    val nameValuePairs = new ArrayList[NameValuePair](parameters.size)
      	    		parameters.keys.foreach{ key =>
      	    		nameValuePairs.add(new BasicNameValuePair(key, parameters(key)))
      	    }
      	    req.setEntity(new UrlEncodedFormEntity(nameValuePairs))
	    }
	    resp = client.execute(req)
    } else if(method == "DELETE") {
    	var req = new HttpDelete(baseURL + apiVersion + path)
    	
    	req.addHeader("apiKey", apiKey)
    	resp = client.execute(req)
    }
    
    var result: JSONObject = new JSONObject(Source.fromInputStream(resp.getEntity().getContent()).getLines.reduceLeft(_ + _))
    checkResponseStatus(resp.getStatusLine().getStatusCode(), result)
    	
    return result
  }
  
  private def executeHttpRequest(path: String, parameters: JSONObject, method: String): JSONObject = {
    val client: DefaultHttpClient  = new DefaultHttpClient()
    
    var resp: HttpResponse  = null
    
	var req: HttpPut = new HttpPut(baseURL + apiVersion + path)
    req.addHeader("apiKey", apiKey)
    
    if (parameters != null) {
		var s: StringEntity  = new StringEntity(parameters.toString())
	    s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"): Header)
	    var entity: HttpEntity  = s
	    req.setEntity(entity)
    }
    
    resp = client.execute(req);
    
    var result: JSONObject = new JSONObject(Source.fromInputStream(resp.getEntity().getContent()).getLines.reduceLeft(_ + _))
    checkResponseStatus(resp.getStatusLine().getStatusCode(), result)
    	
    return result
  }
  
  private def checkResponseStatus(statusCode: Int, response: JSONObject) {
      if(statusCode != 200) {
      var exp: JotFormException = new JotFormException("400")
      
      statusCode match {
        case 400 => throw exp.create(response.get("message").toString())
        case 404 => throw exp.create(response.get("message").toString())
        case 401 => throw exp.create("Unauthorized API call")
        case 503 => throw exp.create("Service is unavailable, rate limits etc exceeded!")
        case _ => throw exp.create(response.get("info").toString())
      }
    }
  }

  private def executeGetRequest(path: String, parameters: Map[String, String] = null): JSONObject = {
    return executeHttpRequest(path, parameters, "GET")
  }
  
  private def executePostRequest(path: String, parameters: Map[String, String] = null): JSONObject = {
    executeHttpRequest(path, parameters, "POST")
  }
  
  private def executePutRequest(path: String, parameters: JSONObject = null): JSONObject = {
    executeHttpRequest(path, parameters, "PUT")
  }
  
  private def executeDeleteRequest(path: String, parameters: Map[String, String] = null): JSONObject = {
   executeHttpRequest(path, parameters, "DELETE") 
  }
  
  private def createConditions(offset: Int, limit: Int, filter: Map[String, String], orderBy: String): Map[String, String] = {
    var args: Map[String, String] = Map("offset" -> offset.toString(), "limit" -> limit.toString(), "orderby" -> orderBy)
    
    var parameters: Map[String, String] = Map()
    
    args.keys.foreach{ key => 
      if(args(key) != null && args(key) != "0"){
        parameters += key -> args(key)
      }
    }

    if(filter != null) {
		var filterObject: JSONObject  = new JSONObject(filter)
		parameters += "filter" -> filterObject.toString()
	}

    return parameters
  }

  private def createHistoryQuery(action: String, date: String, sortBy: String, startDate: String, endDate: String): Map[String, String] = {
    var args: Map[String, String] = Map("action" -> action, "date" -> date, "sortBy" -> sortBy, "startDate" -> startDate, "endDate" ->endDate)
    
    var parameters: Map[String, String] = Map()
    
    args.keys.foreach{ key =>
      if(args(key) != null) {
        parameters += key -> args(key)
      }
    }
    return parameters
  }
  
/**
 * Get user account details for a JotForm user.
 * @return Returns user account type, avatar URL, name, email, website URL and account limits.
 */
  def getUser(): JSONObject = {
    return executeGetRequest("/user")
  }
  
 /**
 * Get number of form submissions received this month.
 * @return Returns number of submissions, number of SSL form submissions, payment form submissions and upload space used by user.
 */
  def getUsage(): JSONObject = {
    return executeGetRequest("/user/usage")
  }
  
 /**
 * Get a list of forms for this account
 * @param offset Start of each result set for form list.
 * @param limit Number of results in each result set for form list.
 * @param filter Filters the query results to fetch a specific form range.
 * @param orderBy Order results by a form field name.
 * @return Returns basic details such as title of the form, when it was created, number of new and total submissions.
 */
  def getForms(offset: Int = 0, limit: Int = 0, filter: Map[String, String] = null, orderBy: String = null): JSONObject = {
    var parameters: Map[String, String] = createConditions(offset, limit, filter, orderBy)
    return executeGetRequest("/user/forms", parameters)
  }
  
 /**
 * Get a list of submissions for this account.
 * @param offset Start of each result set for form list.
 * @param limit Number of results in each result set for form list.
 * @param filter Filters the query results to fetch a specific form range.
 * @param orderBy Order results by a form field name.
 * @return Returns basic details such as title of the form, when it was created, number of new and total submissions.
 */
  def getSubmissions(offset: Int = 0, limit: Int = 0, filter: Map[String, String] = null, orderBy: String = null): JSONObject = {
    var parameters: Map[String, String] = createConditions(offset, limit, filter, orderBy)
    return executeGetRequest("/user/submissions", parameters)
  }
  
 /**
 * Get a list of sub users for this account.
 * @return Returns list of forms and form folders with access privileges.
 */
  def getSubusers(): JSONObject = {
    return executeGetRequest("/user/subusers")
  }
  
 /**
 * Get a list of form folders for this account.
 * @return Returns name of the folder and owner of the folder for shared folders.
 */
  def getFolders(): JSONObject = {
    return executeGetRequest("/user/folders")
  }
  
 /**
 * List of URLS for reports in this account.
 * @return Returns reports for all of the forms. ie. Excel, CSV, printable charts, embeddable HTML tables.
 */
  def getReports(): JSONObject = {
    return executeGetRequest("/user/reports")
  }
  
 /**
 * Get user's settings for this account.
 * @return Returns user's time zone and language.
 */
  def getSettings(): JSONObject = {
    return executeGetRequest("/user/settings")
  }
  
 /**
 * Update user's settings
 * @param settings New user setting values with setting keys
 * @return Returns changes on user settings.
 */
  def updateSettings(settings: Map[String, String]): JSONObject = {
    return executePostRequest("/user/settings", settings)
  }
  
/**
 * Get user activity log.
 * @param action Filter results by activity performed. Default is 'all'.
 * @param date Limit results by a date range. If you'd like to limit results by specific dates you can use startDate and endDate fields instead.
 * @param sortBy Lists results by ascending and descending order.
 * @param startDate Limit results to only after a specific date. Format: MM/DD/YYYY.
 * @param endDate Limit results to only before a specific date. Format: MM/DD/YYYY.
 * @return Returns activity log about things like forms created/modified/deleted, account logins and other operations.
 */
  def getHistory(action: String = null, date: String = null, sortBy: String = null, startDate: String = null, endDate: String = null): JSONObject = {
    var parameters = createHistoryQuery(action, date, sortBy, startDate, endDate)
    return executeGetRequest("/user/history", parameters)
  }
  
 /**
 * Get basic information about a form.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @return Returns form ID, status, update and creation dates, submission count etc.
 */
  def getForm(formID: Long): JSONObject = {
    return executeGetRequest("/form/" + formID.toString())
  }
  
 /**
 * Get a list of all questions on a form.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @return Returns question properties of a form.
 */
  def getFormQuestions(formID: Long): JSONObject = {
    return executeGetRequest("/form/" + formID.toString() + "/questions")
  }
  
 /**
 * Get details about a question
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param qid Identifier for each question on a form. You can get a list of question IDs from /form/{id}/questions.
 * @return Returns question properties like required and validation.
 */
  def getFormQuestion(formID: Long, qid: Int): JSONObject = {
    return executeGetRequest("/form/" + formID.toString() + "/question/" + qid.toString())
  }
  
/**
 * List of a form submissions.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param offset Start of each result set for form list.
 * @param limit Number of results in each result set for form list.
 * @param filter Filters the query results to fetch a specific form range.
 * @param orderBy Order results by a form field name.
 * @return Returns submissions of a specific form.
 */
  def getFormSubmissions(formID: Long, offset: Int = 0, limit: Int = 0, filter: Map[String, String] = null, orderBy: String = null): JSONObject = {
    var parameters: Map[String, String] = createConditions(offset, limit, filter, orderBy)
    return executeGetRequest("/form/" + formID.toString() + "/submissions", parameters)
  }
  
 /**
 * Submit data to this form using the API.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param submission Submission data with question IDs.
 * @return Returns posted submission ID and URL.
 */
  def createFormSubmission(formID: Long, submission: Map[String, String]): JSONObject = {
    var parameters: Map[String, String] = Map()
    
    submission.keys.foreach{ key =>
      if(key contains "_"){
        parameters += "submission[" + key.substring(0, (key indexOf "_")) + "][" + key.substring((key indexOf "_") + 1) + "]" -> submission(key)
      } else {
        parameters += "submission[" + key + "]" -> submission(key)
      }
    }
    
    return executePostRequest("/form/" + formID.toString() + "/submissions", parameters)
  }
  
 /**
 * Submit data to this form using the API
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param submissions Submission data with question IDs.
 * @return Returns posted submission ID and URL.
 */
  def createFormSubmissions(formID: Long, submissions: JSONObject): JSONObject = {
    return executePutRequest("/form/" + formID.toString() + "/submissions", submissions)
  }

 /**
 * List of files uploaded on a form.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @return Returns uploaded file information and URLs on a specific form.
 */
  def getFormFiles(formID: Long): JSONObject = {
    return executeGetRequest("/form/" + formID.toString() + "/files")
  }
  
 /**
 * Get list of webhooks for a form
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @return Returns list of webhooks for a specific form.
 */
  def getFormWebhooks(formID: Long): JSONObject = {
    return executeGetRequest("/form/" + formID.toString() + "/webhooks")
  }
  
 /**
 * Add a new webhook
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param webhookURL Webhook URL is where form data will be posted when form is submitted.
 * @return Returns list of webhooks for a specific form.
 */
  def createFormWebhook(formID: Long, webhookURL: String): JSONObject = {
    var parameters = Map("webhookURL" -> webhookURL)
    return executePostRequest("/form/" + formID.toString() + "/webhooks", parameters)
  }
  
 /**
 * Delete a specific webhook of a form.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param webhookID You can get webhook IDs when you call /form/{formID}/webhooks.
 * @return Returns remaining webhook URLs of form.
 */
  def deleteFormWebhook(formID: Long, webhookID: Int): JSONObject = {
    return executeDeleteRequest("/form/" + formID.toString() + "/webhooks/" + webhookID.toString())
  }
  
 /**
 * Get submission data
 * @param sid You can get submission IDs when you call /form/{id}/submissions.
 * @return Returns information and answers of a specific submission.
 */
  def getSubmission(sid: Long): JSONObject = {
    return executeGetRequest("/submission/" + sid.toString())
  }
  
 /**
 * Get report details
 * @param reportID You can get a list of reports from /user/reports.
 * @return Returns properties of a specific report like fields and status.
 */
  def getReport(reportID: Long): JSONObject = {
    return executeGetRequest("/report/" + reportID.toString())
  }
  
 /**
 * Get folder details
 * @param folderID You can get a list of folders from /user/folders.
 * @return Returns a list of forms in a folder, and other details about the form such as folder color.
 */
  def getFolder(folderID: String): JSONObject = {
    return executeGetRequest("/folder/" + folderID)
  }

 /**
 * Create a folder
 * @param folderProperties Properties of new folder.
 * @return Returns folder details.
 */
  def createFolder(folderProperties: Map[String, String]): JSONObject = {
    return executePostRequest("/folder", folderProperties)
  }

 /**
 * Delete a folder and its subfolders
 * @param folderID You can get a list of folders from /user/folders.
 * @return Returns status of the request.
 */
  def deleteFolder(folderID: String): JSONObject = {
    return executeDeleteRequest("/folder/" + folderID)
  }

  /**
  * Update a specific folder.
  * @param folderID You can get folder IDs when you call /user/folders.
  * @param folderProperties New properties of the specified folder.
  * @return Returns properties of the updated folder.
  */
  def updateFolder(folderID: String, folderProperties: JSONObject): JSONObject = {
    return executePutRequest("/folder/" + folderID, folderProperties)
  }

  /**
  * Add forms to the specified folder.
  * @param  $folderID You can get the list of folders from /user/folders.
  * @param  $formIDs You can get the list of forms from /user/forms.
  * @return Returns properties of the updated folder.
  */
  def addFormsToFolder(folderID: String, formIDs: String*): JSONObject = {
    val folder: HashMap[String, Object] = new HashMap[String, Object]()
    folder.put("forms", formIDs)
    val data: JSONObject = new JSONObject(folder)

    return updateFolder(folderID, data);
  }

  /**
  * Add a form to the specified folder.
  * @param  $folderID You can get the list of folders from /user/folders.
  * @param  $formID You can get the list of forms from /user/forms.
  * @return Returns properties of the updated folder.
  */
  def addFormToFolder(folderID: String, formID: String): JSONObject = {
    val folder: HashMap[String, Object] = new HashMap[String, Object]()
    folder.put("forms", Array(formID))
    val data: JSONObject = new JSONObject(folder)

    return updateFolder(folderID, data);
  }

 /**
 * Get a list of all properties on a form.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @return Returns form properties like width, expiration date, style etc.
 */
  def getFormProperties(formID: Long): JSONObject = {
    return executeGetRequest("/form/" + formID.toString() + "/properties")
  }
  
 /**
 * Get a specific property of the form.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param propertyKey You can get property keys when you call /form/{id}/properties.
 * @return Returns given property key value.
 */
  def getFormProperty(formID: Long, propertyKey: String): JSONObject = {
    return executeGetRequest("/form/" + formID.toString() + "/properties/" + propertyKey)
  }
  
 /**
 * Get all the reports of a form, such as excel, csv, grid, html, etc.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @return Returns list of all reports in a form, and other details about the reports such as title.
 */
  def getFormReports(formID: Long): JSONObject = {
    return executeGetRequest("/form/" + formID.toString() + "/reports")
  }
  
/**
 * Create new report of a form
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param report Report details. List type, title etc.
 * @return Returns report details and URL.
 */
  def createReport(formID: Long, report: Map[String, String]): JSONObject = {
    return executePostRequest("/form/" + formID.toString() + "/reports", report)
  }
  
 /**
 * Delete a single submission.
 * @param sid You can get submission IDs when you call /user/submissions.
 * @return Returns status of request.
 */
  def deleteSubmission(sid: Long): JSONObject = {
    return executeDeleteRequest("/submission/" + sid.toString())
  }
  
 /**
 * Edit a single submission.
 * @param sid You can get submission IDs when you call /form/{id}/submissions.
 * @param submission New submission data with question IDs.
 * @return Returns status of request.
 */
  def editSubmission(sid: Long, submission: Map[String, String]): JSONObject = {
    var parameters: Map[String, String] = Map()
    
    submission.keys.foreach{ key =>
      if((key contains "_") && (key != "created_at")){
        parameters += "submission[" + key.substring(0, (key indexOf "_")) + "][" + key.substring((key indexOf "_") + 1) + "]" -> submission(key)
      } else {
        parameters += "submission[" + key + "]" -> submission(key)
      }
    }
    return executePostRequest("/submission/" + sid.toString(), parameters);
  }
  
 /**
 * Clone a single form.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @return Returns status of request.
 */
  def cloneForm(formID: Long): JSONObject = {
    return executePostRequest("/form/" + formID.toString() + "/clone")
  }
  
 /**
 * Delete a single form question.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param qid Identifier for each question on a form. You can get a list of question IDs from /form/{id}/questions.
 * @return Returns status of request.
 */
  def deleteFormQuestion(formID: Long, qid: Int): JSONObject = {
    return executeDeleteRequest("/form/" + formID.toString() + "/question/" + qid.toString())
  }
  
 /**
 * Add new question to specified form.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param question New question properties like type and text.
 * @return Returns properties of new question.
 */
  def createFormQuestion(formID: Long, question: Map[String, String]): JSONObject = {
    var parameters: Map[String, String] = Map()
    
    question.keys.foreach{ key =>
      parameters += "question[" + key + "]" -> question(key)
    }
    
    return executePostRequest("/form/" + formID.toString() + "/questions", parameters)
  }
  
 /**
 *  Add new questions to specified form.
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param questions New question properties like type and text.
 * @return Returns properties of new questions.
 */
  def createFormQuestions(formID: Long, questions: JSONObject): JSONObject = {
    return executePutRequest("/form/" + formID.toString() + "/questions", questions)
  }
    
  def editFormQuestion(formID: Long, qid: Int, question: Map[String, String]): JSONObject = {
    var parameters: Map[String, String] = Map()
    
    question.keys.foreach{ key =>
      parameters += "question[" + key + "]" -> question(key)
    }
        
    return executePostRequest("/form/" + formID.toString() + "/question/" + qid.toString(), parameters)
  }
  
/**
 * Add or edit properties of a specific form
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param formProperties New properties like label width.
 * @return Returns edited properties.
 */
  def setFormProperties(formID: Long, properties: Map[String, String]): JSONObject = {
    var parameters: Map[String, String] = Map()
    
    properties.keys.foreach{ key =>
      parameters += "properties[" + key + "]" -> properties(key)
    }
    
    return executePostRequest("/form/" + formID.toString() + "/properties", parameters)
  }
  
 /**
 * Add or edit properties of a specific form
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @param formProperties New properties like label width.
 * @return Returns edited properties.
 */
  def setMultipleFormProperties(formID: Long, properties: JSONObject): JSONObject = {
    return executePutRequest("/form/" + formID.toString() + "/properties", properties)
  }
  
 /**
 * Create a new form
 * @param form Questions, properties and emails of new form.
 * @return Returns new form.
 */
  def createForm(form: Map[String, Any]): JSONObject = {
    var parameters: Map[String, String] = Map()
    
    form.keys.foreach{ key =>
    	if(key == "properties") {
    	  var properties = form.apply(key)
    	  properties.asInstanceOf[Map[String, String]].keys.foreach{ propertyKey =>
    	    parameters += key + "[" + propertyKey + "]" -> properties.asInstanceOf[Map[String, String]](propertyKey)
    	  }
    	} else {
    	  var formItem = form.apply(key)
    	  formItem.asInstanceOf[Map[String, Any]].keys.foreach{ formItemKey =>
    	    var item = formItem.asInstanceOf[Map[String, Any]].apply(formItemKey)
    	    
    	    item.asInstanceOf[Map[String, String]].keys.foreach{ itemKey =>
    	      parameters += key + "[" + formItemKey + "][" + itemKey + "]" -> item.asInstanceOf[Map[String, String]](itemKey)
    	    }
    	  }
    	}
    }
    return executePostRequest("/user/forms", parameters)
  }
  
 /**
 * Create a new form
 * @param form Questions, properties and emails of new form.
 * @return Returns new form.
 */
  def createForms(form: JSONObject): JSONObject = {
    return executePutRequest("/user/forms", form)
  }

 /**
 * Delete a single form
 * @param formID Form ID is the numbers you see on a form URL. You can get form IDs when you call /user/forms.
 * @return Properties of deleted form.
 */
  def deleteForm(formID: Long): JSONObject = {
	return executeDeleteRequest("/form/" + formID.toString())
  } 
  
 /**
 * Register with username, password and email
 * @param userDetails Username, password and email to register a new user
 * @return Returns new user's details
 */
  def registerUser(userDetails: Map[String, String]): JSONObject = {
	return executePostRequest("/user/register", userDetails)
  }
  
 /**
 * Login user with given credentials
 * @param credentials Username, password, application name and access type of user
 * @return Returns logged in user's settings and app key
 */
  def loginUser(credentials: Map[String, String]): JSONObject = {
	return executePostRequest("/user/login", credentials)
  }
  
 /**
 * Logout user
 * @returns Returns status of request
 */
  def logoutUser(): JSONObject = {
    return executeGetRequest("/user/logout")
  }
    
/**
 * Get details of a plan
 * @param planName Name of the requested plan. FREE, PREMIUM etc.
 * @return Returns details of a plan
 */
  def getPlan(planName: String): JSONObject = {
    return executeGetRequest("/system/plan/" + planName)
  }

   /**
   * Delete a single report
   * @param reportID You can get a list of reports from /user/reports.
   * @return Returns status of request.
   */
    def deleteReport(reportID: Long): JSONObject = {
    return executeDeleteRequest("/report/" + reportID.toString())
    }
}


