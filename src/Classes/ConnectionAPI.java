package Classes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import Enums.ConnectionAPIMethods;
import android.app.ProgressDialog;
import android.content.Context;
import org.json.*;
import java.io.OutputStreamWriter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import Enums.Field;
import android.os.AsyncTask;

/**
 * ConnectionAPI is the class responsible of the iteration with the HTTP API of Aftership, it wrap all the
 * funcntionalities in different methods
 * Created by User on 10/6/14
 */
public class ConnectionAPI extends AsyncTask<ConnectionAPIMethods,Void,ConnectionAPI> {

    private static String URL_SERVER = "https://api.aftership.com/";
    private static String VERSION_API = "v3";

    private String keyAPI;

    /** callback*/
    private AsyncTaskCompleteListener<ConnectionAPI> callback;
    /** mehtod call */
    private ConnectionAPIMethods method;
    /** Parameters TrackingNumber,slug fields, lang*/
    private String trackingNumber;
    private String slug;
    private List<Field> fields;
    private String lang;
    /**Parameter ParametersTracking parameters **/
    private ParametersTracking parameters;
    /** Paramater int page*/
    private int page;
    /** Parameter Tracking tracking*/
    private Tracking tracking;

    private Exception exception;
    /**returns**/
    private Checkpoint checkpointReturn;
    private Tracking trackingReturn;
    boolean confirmationReturn;
    private List<Tracking> trackingsReturn;
    private List<Courier> couriersReturn;
    /** show the progresion of the dialog*/
    private ProgressDialog dialog;

    private  ConnectionAPI(ConnectionAPIMethods method,AsyncTaskCompleteListener<ConnectionAPI> callback, String keyAPI){
        this.method = method;
        this.callback = callback;
        this.keyAPI = keyAPI;
        /**show the dialog from the context from where is called, if error, delete this line*/
        this.dialog = callback instanceof Context? new ProgressDialog((Context)callback):null;
    }

    public ConnectionAPI(String keyAPI,ConnectionAPIMethods method,AsyncTaskCompleteListener<ConnectionAPI> callback){
        this(method,callback,keyAPI);
        if(method!=ConnectionAPIMethods.getCouriers)
           this.exception = new AftershipAPIException("The consntructor only can be called with ConnectionAPIMethods.getCouriers");

    }

    public ConnectionAPI(String keyAPI,ConnectionAPIMethods method,AsyncTaskCompleteListener<ConnectionAPI> callback,
                         String trackingNumber){
        this(method,callback,keyAPI);
        if(method!=ConnectionAPIMethods.detectCouriers)
            this.exception =  new AftershipAPIException("The consntructor only can be called with " +
                    "ConnectionAPIMethods.detectCouriers");
        this.trackingNumber = trackingNumber;

    }

    public ConnectionAPI(String keyAPI,ConnectionAPIMethods method,AsyncTaskCompleteListener<ConnectionAPI> callback,
                         String trackingNumber,String slug){
        this(method,callback,keyAPI);
        if(method!=ConnectionAPIMethods.getTrackingByNumber && method!=ConnectionAPIMethods.deleteTracking &&
                method!=ConnectionAPIMethods.reactivate && method!=ConnectionAPIMethods.getLastCheckpoint)
            this.exception =  new AftershipAPIException("The consntructor only can be called with" +
                    "ConnectionAPIMethods.getTrackingByNumber,ConnectionAPIMethods.deleteTracking," +
                    "ConnectionAPIMethods.reactivate, ConnectionAPIMethods.getLastCheckpoint");
        this.trackingNumber = trackingNumber;
        this.slug = slug;

    }

    public ConnectionAPI(String keyAPI,ConnectionAPIMethods method, AsyncTaskCompleteListener<ConnectionAPI> callback,
                         ParametersTracking parameters){
        this(method,callback,keyAPI);
        if(method!=ConnectionAPIMethods.getTrackings && method!=ConnectionAPIMethods.getTrackingsNext)
           this.exception =  new AftershipAPIException("The consntructor only can be called with ConnectionAPIMethods.getTracking" +
                   "or ConnectionAPIMethods.getTrackingsNext");
        this.parameters = parameters;

    }

    public ConnectionAPI(String keyAPI,ConnectionAPIMethods method,AsyncTaskCompleteListener<ConnectionAPI> callback,
                         int page){
        this(method,callback,keyAPI);
        if(method!=ConnectionAPIMethods.getTrackings)
            this.exception =  new AftershipAPIException("The consntructor only can be called with ConnectionAPIMethods.getTracking");
        this.page = page;
    }

    public ConnectionAPI(String keyAPI,ConnectionAPIMethods method,AsyncTaskCompleteListener<ConnectionAPI> callback,
                         Tracking tracking){
        this(method,callback,keyAPI);
        if(method!=ConnectionAPIMethods.postTracking && method!=ConnectionAPIMethods.putTracking)
            this.exception =  new AftershipAPIException("The consntructor only can be called with," +
                    " ConnectionAPIMethods.postTracking or ConnectionAPIMethods.putTracking");
        this.tracking = tracking;
    }

    public ConnectionAPI(String keyAPI,ConnectionAPIMethods method,AsyncTaskCompleteListener<ConnectionAPI> callback,
                         String trackingNumber,String slug, List<Field> fields, String lang){
        this(method,callback,keyAPI);
        if(method!=ConnectionAPIMethods.postTracking && method!=ConnectionAPIMethods.putTracking)
            this.exception =  new AftershipAPIException("The consntructor only can be called with" +
                    " ConnectionAPIMethods.postTracking or method!=ConnectionAPIMethods.putTracking");
        this.trackingNumber = trackingNumber;
        this.slug = slug;
        this.fields = fields;
        this.lang = lang;
    }

    protected void onPreExecute() {
        if(dialog!=null){
            this.dialog.setMessage("Progress start");
            this.dialog.show();
        }
    }
    protected ConnectionAPI doInBackground(ConnectionAPIMethods... connectionAPI){
//        getLastCheckpoint(0),reactivate(1),getTrackingByNumber(2),getTracking(3),deleteTracking(4),
//                postTracking(5),putTracking(6),getCouriers(7),detectCouriers(8);
        if(this.exception ==null) {
            try {
                switch (this.method.getNumberMethod()) {
                    case 0://getLastCheckpoint
                        if (this.fields == null && this.lang == null)
                            this.checkpointReturn = this.getLastCheckpoint(this.trackingNumber, this.slug);
                        else
                            this.checkpointReturn = this.getLastCheckpoint(this.trackingNumber,
                                    this.slug, this.fields, this.slug);
                        break;
                    case 1: //reactivate
                        this.confirmationReturn = this.reactivate(this.trackingNumber, this.slug);
                        break;
                    case 2://getTrackingByNumber
                        if (this.fields == null && this.lang == null)
                            this.trackingReturn = this.getTrackingByNumber(this.trackingNumber, this.slug);
                        else
                            this.trackingReturn = this.getTrackingByNumber(
                                    this.trackingNumber, this.slug, this.fields, this.lang);
                        break;
                    case 3://getTracking
                        if (this.parameters == null)
                            this.trackingsReturn = this.getTrackings(this.page);
                        else
                            this.trackingsReturn = this.getTrackings(this.parameters);
                        break;
                    case 4://deleteTracking
                        this.confirmationReturn = this.deleteTracking(this.trackingNumber, this.slug);
                        break;
                    case 5://postTracking
                        this.trackingReturn = this.postTracking(this.tracking);
                        break;
                    case 6://putTracking
                        this.trackingReturn = this.putTracking(this.tracking);
                        break;
                    case 7://getCouriers(7)
                        this.couriersReturn = this.getCouriers();
                        break;
                    case 8://detectCouriers(8)
                        this.couriersReturn = this.detectCouriers(this.trackingNumber);
                        break;
                    case 9://getTrackingsNext(9)
                        this.trackingsReturn = this.getTrackingsNext(this.parameters);
                        break;

                }
            } catch (Exception e) {
                this.exception = e;
            }

        }
        return this;
    }

    protected void onPostExecute(ConnectionAPI result) {
        if (dialog!=null && dialog.isShowing()) {
            try{
                dialog.dismiss();
            } catch (Exception e) {e.printStackTrace();}
        }
        callback.onTaskComplete(result);
    }

    /**
     * Return the tracking information of the last checkpoint of a single tracking
     *
     * @param trackingNumber A String with the trackingNumber to get the last checkpoint, mandatory param
     * @param slug A String with the slug of the courier to get the last checkpoint, mandatory param
     * @return   The last Checkpoint object
     * @throws Classes.AftershipAPIException  If the request response an error
     *           The tracking is not defined in your account
     * @throws   java.io.IOException If there is a problem with the connection
     * @throws   java.text.ParseException    If the response can not be parse to JSONObject
     * @see Checkpoint
     **/
    public Checkpoint getLastCheckpoint(String trackingNumber,String slug)
            throws AftershipAPIException,IOException,ParseException,JSONException{

        JSONObject response = this.request("GET","/last_checkpoint/"+slug+"/"+trackingNumber,null);
        JSONObject checkpointJSON = response.getJSONObject("data").getJSONObject("checkpoint");
        Checkpoint checkpoint = null;
        if(checkpointJSON.length()!=0) {
            checkpoint = new Checkpoint(checkpointJSON);
        }

        return checkpoint;
    }

    /**
     * Return the tracking information of the last checkpoint of a single tracking
     *
     * @param trackingNumber A String with the trackingNumber to get the last checkpoint, mandatory param
     * @param slug A String with the slug of the courier to get the last checkpoint, mandatory param
     * @param fields         A list of fields wanted to be in the response
     * @param lang           A String with the language desired. Support Chinese to English translation
     *                       for china-ems and china-post only
     * @return   The last Checkpoint object
     * @throws Classes.AftershipAPIException  If the request response an error
     *           The tracking is not defined in your account
     * @throws   java.io.IOException If there is a problem with the connection
     * @throws   java.text.ParseException    If the response can not be parse to JSONObject
     * @see Checkpoint
     **/
    public Checkpoint getLastCheckpoint(String trackingNumber,String slug,List<Field> fields, String lang)
            throws AftershipAPIException,IOException,JSONException,ParseException,JSONException{

        String params;
        QueryString qs = new QueryString();
        if (fields!=null) qs.add("fields", fields);
        if (lang!=null || !lang.equals("")) qs.add("lang",lang);
        params = qs.toString().replace('&','?');

        JSONObject response = this.request("GET","/last_checkpoint/"+slug+"/"+trackingNumber+params,null);
        JSONObject checkpointJSON = response.getJSONObject("data").getJSONObject("checkpoint");
        Checkpoint checkpoint = null;
        if(checkpointJSON.length()!=0) {
            checkpoint = new Checkpoint(checkpointJSON);
        }

        return checkpoint;
    }
    /**
     * Reactivate an expired tracking from your account
     *
     * @param trackingNumber A String with the trackingNumber to reactivate, mandatory param
     * @param slug A String with the slug of the courier to reactivate, mandatory param
     * @return   A JSONObject with the response. It will contain the status code of the operation, trackingNumber,
     *           slug and active (to true)
     * @throws Classes.AftershipAPIException  If the request response an error
     *           The tracking is not defined in your account
     * @throws   java.io.IOException If there is a problem with the connection
     * @throws   java.text.ParseException    If the response can not be parse to JSONObject
     **/
    public boolean reactivate(String trackingNumber, String slug)
            throws AftershipAPIException,IOException,ParseException,JSONException{

        JSONObject response = this.request("POST","/trackings/"+slug+"/"+trackingNumber+"/reactivate",null);

        if (response.getJSONObject("meta").getInt("code")==200)
            return true;
        else
            return false;

    }

    /**
     * Get a specific tracking from your account
     *
     * @param trackingNumber A String with the trackingNumber to get, mandatory param
     * @param slug A String with the slug of the courier to get, mandatory param
     * @return  A Tracking object with the response
     * @throws Classes.AftershipAPIException  If the request response an error
     *          The tracking is not defined in your account
     * @throws  java.io.IOException If there is a problem with the connection
     * @throws  java.text.ParseException    If the response can not be parse to JSONObject
     * @see     Tracking
     **/
    public Tracking getTrackingByNumber(String trackingNumber,String slug)
            throws AftershipAPIException,IOException,ParseException,JSONException{

        JSONObject response = this.request("GET","/trackings/"+slug+"/"+trackingNumber,null);
        JSONObject trackingJSON = response.getJSONObject("data").getJSONObject("tracking");
        Tracking tracking = null;
        if(trackingJSON.length()!=0) {
            tracking = new Tracking(trackingJSON);
        }

        return tracking;
    }

    /**
     * Get a specific tracking from your account
     *
     * @param trackingNumber A String with the trackingNumber to get, mandatory param
     * @param slug           A String with the slug of the courier to get, mandatory param
     * @param fields         A list of fields wanted to be in the response
     * @param lang           A String with the language desired. Support Chinese to English translation
     *                       for china-ems and china-post only
    (Example: en)
     * @return  A Tracking object with the response
     * @throws Classes.AftershipAPIException  If the request response an error
     *          The tracking is not defined in your account
     * @throws  java.io.IOException If there is a problem with the connection
     * @throws  java.text.ParseException    If the response can not be parse to JSONObject
     * @see     Tracking
     **/
    public Tracking getTrackingByNumber(String trackingNumber,String slug,List<Field> fields,String lang)
            throws AftershipAPIException,IOException,ParseException,JSONException{

        String params;
        QueryString qs = new QueryString();
        if (fields!=null) qs.add("fields", fields);
        if (lang!=null || !lang.equals("")) qs.add("lang",lang);
        params = qs.toString().replace('&','?');

        JSONObject response = this.request("GET","/trackings/"+slug+"/"+trackingNumber+params,null);
        JSONObject trackingJSON = response.getJSONObject("data").getJSONObject("tracking");
        Tracking tracking = null;
        if(trackingJSON.length()!=0) {
            tracking = new Tracking(trackingJSON);
        }

        return tracking;
    }

    /**
     * Get trackings from your account with the ParametersTracking defined in the params
     *
     * @param parameters ParametersTracking Object, with the information to get
     * @return  An Tracking List with trackings that match then values of ParametersTracking in param,
     *          accessing the trackings should be made through the ParametersTracking passed as param
     * @throws Classes.AftershipAPIException  If the request response an error
     * @throws  java.io.IOException If there is a problem with the connection
     * @throws  java.text.ParseException    If the response can not be parse to JSONObject
     * @see     ParametersTracking
     * @see     Tracking
     **/
    public List<Tracking> getTrackings(ParametersTracking parameters)throws AftershipAPIException,IOException,ParseException,JSONException{
        List<Tracking> trackingList = null;
        JSONObject response = this.request("GET","/trackings?"+parameters.generateQueryString(),null);
        JSONArray trackingJSON = response.getJSONObject("data").getJSONArray("trackings");
        if(trackingJSON.length()!=0) {
            trackingList = new ArrayList<Tracking>(trackingJSON.length());
            for (int i = 0; i < trackingJSON.length(); i++) {
                trackingList.add(new Tracking(trackingJSON.getJSONObject(i)));
            }

        }

        return trackingList;
    }

    /**
     * Get next page of Trackings from your account with the ParametersTracking defined in the params
     *
     * @param parameters ParametersTracking Object, with the information to get
     * @return  The next page of Tracking List that match then values of ParametersTracking in param,
     *          accessing the trackings should be made through the ParametersTracking passed as param
     * @throws Classes.AftershipAPIException  If the request response an error
     * @throws  java.io.IOException If there is a problem with the connection
     * @throws  java.text.ParseException    If the response can not be parse to JSONObject
     * @see     ParametersTracking
     * @see     Tracking
     **/
    public List<Tracking> getTrackingsNext(ParametersTracking parameters)
            throws AftershipAPIException,IOException,ParseException,JSONException{
        List<Tracking> trackingList = null;
        parameters.setPage(parameters.getPage()+1);
        JSONObject response = this.request("GET","/trackings?"+parameters.generateQueryString(),null);
        JSONArray trackingJSON = response.getJSONObject("data").getJSONArray("trackings");
        if(trackingJSON.length()!=0) {
            trackingList = new ArrayList<Tracking>(trackingJSON.length());
            for (int i = 0; i < trackingJSON.length(); i++) {
                trackingList.add(new Tracking(trackingJSON.getJSONObject(i)));
            }

        }
        return trackingList;
    }

    /**
     * Get as much as 100 trackings from your account, created less than 30 days ago. If you delete right before,
     *          you may obtain less than 100 trackings.
     *
     * @param page Indicated the page of 100 trackings to return, if page is 1 will return the first 100, if is 2
     *             100-200 etc
     * @return  A List of Tracking Objects from your account. Max 100 trackings
     * @throws Classes.AftershipAPIException  If the request response an error
     * @throws  java.io.IOException If there is a problem with the connection
     * @throws  java.text.ParseException    If the response can not be parse to JSONObject
     * @see     Tracking
     **/
    public List<Tracking> getTrackings(int page)throws AftershipAPIException,IOException,ParseException,JSONException{

        List<Tracking> trackingList = null;

        JSONObject response = this.request("GET","/trackings?limit=100&page="+page,null);
        JSONArray trackingJSON = response.getJSONObject("data").getJSONArray("trackings");
        if(trackingJSON.length()!=0) {
             trackingList = new ArrayList<Tracking>(trackingJSON.length());

            for (int i = 0; i < trackingJSON.length(); i++) {
                trackingList.add(new Tracking((JSONObject)trackingJSON.get(i)));
            }
        }
        return trackingList;

    }

    /**
     * Delete a tracking from your account
     *
     * @param trackingNumber A String with the trackingNumber to delete, mandatory param
     * @param slug A String with the slug of the courier to delete, mandatory param
     * @return   A boolean, true if delete correctly, and false otherwise
     * @throws Classes.AftershipAPIException  If the request response an error
     *           The tracking is not defined in your account
     * @throws   java.io.IOException If there is a problem with the connection
     * @throws   java.text.ParseException    If the response can not be parse to JSONObject
     **/
    public boolean deleteTracking(String trackingNumber,String slug)throws AftershipAPIException,IOException,ParseException,JSONException{
        JSONObject response = this.request("DELETE","/trackings/"+slug+"/"+trackingNumber,null);

        if (response.getJSONObject("meta").getInt("code")==200)
            return true;
        else
            return false;
    }

    /**
     * Add a new tracking to your account
     *
     * @param tracking A Tracking object with the information to update
     *                 The field trackingNumber SHOULD be informed, otherwise an exception will be thrown
     *                 The fields an user can add are: slug, smses, emails, title, customerName, orderID, orderIDPath,
     *                 customFields, destinationCountryISO3 (the others are provided by the Server)
     * @return   A Tracking object with the fields in the same state as the server, if a field has an error,
     *           it won't be added, and won't be shown in the response (for example if the smses
     *           phone number is not valid). This response doesn't have checkpoints informed!
     * @throws   AftershipAPIException  If the request response an error
     *           Duplicate trackingNumbers, or trackingNumber with invalid format will not be accepted
     * @throws   java.io.IOException If there is a problem with the connection
     * @throws   java.text.ParseException    If the response can not be parse to JSONObject
     **/
    public Tracking postTracking(Tracking tracking) throws AftershipAPIException,IOException,ParseException,JSONException{

        JSONObject response = this.request("POST", "/trackings", tracking.generateJSON());

        return new Tracking(response.getJSONObject("data").getJSONObject("tracking"));

    }
    /**
     * Updates a tracking of your account
     *
     * @param tracking A Tracking object with the information to update
     *                 The fields trackingNumber and slug SHOULD be informed, otherwise an exception will be thrown
     *                 The fields an user can update are: smses, emails, title, customerName, orderID, orderIDPath,
     *                 customFields
     * @return   A Tracking object with the fields in the same state as the server, if a field has an error,
     *           it won't be updated, and won't be shown in the response (for example if the smses
     *           phone number is not valid). This response doesn't have checkpoints informed!
     * @throws   AftershipAPIException  If the request response an error
     *           If the Tracking doesn't have informed trackingNumber and slug an exception will be thrown
     * @throws   java.io.IOException If there is a problem with the connection
     * @throws   java.text.ParseException    If the response can not be parse to JSONObject
     **/
    public Tracking putTracking(Tracking tracking) throws AftershipAPIException,IOException,ParseException,JSONException{

        JSONObject response = this.request("PUT", "/trackings/"+tracking.getSlug()+
                "/"+tracking.getTrackingNumber(), tracking.generatePutJSON());

        return new Tracking(response.getJSONObject("data").getJSONObject("tracking"));

    }
    /**
    * Return a list of couriers supported by AfterShip along with their names,
    * URLs and slugs
    *
    * @return   A list of Object Courier, with all the couriers supported by the API
    * @throws   AftershipAPIException  If the request response an error
    * @throws   java.io.IOException If there is a problem with the connection
    * @throws   java.text.ParseException    If the response can not be parse to JSONObject
    **/
    public List<Courier> getCouriers() throws AftershipAPIException,IOException,ParseException,JSONException{

        JSONObject response = this.request("GET","/couriers",null);


        JSONArray couriersJSON = response.getJSONObject("data").getJSONArray("couriers");
        List<Courier> couriers = new ArrayList<Courier>(couriersJSON.length());

        JSONObject element;

        for (int i = 0; i < couriersJSON.length(); i++) {
            element = couriersJSON.getJSONObject(i);

            Courier newCourier = new Courier(element);
            couriers.add(newCourier);
        }
        return couriers;
    }

     /**
     * Get a list of matched couriers for a tracking number based on the tracking number format
     * Note, only check the couriers you have defined in your account
     *
     * @param trackingNumber tracking number to match with couriers
     * @return A List of Couriers objects that match the provided trackingNumber
     * @throws AftershipAPIException if the request response an error
      * Invalid JSON data. If the tracking number doesn't match any courier defined in your account,
      * or it doesn't match any courier defined in Aftership
     * @throws  java.io.IOException If there is a problem with the connection
     * @throws  java.text.ParseException    If the response can not be parse to JSONObject
     **/
    public List<Courier> detectCouriers(String trackingNumber)throws AftershipAPIException,IOException,ParseException,JSONException{
        JSONObject response = this.request("GET","/couriers/detect/"+trackingNumber,null);
        List<Courier> couriers = new ArrayList<Courier>();

        JSONArray couriersJSON = response.getJSONObject("data").getJSONArray("couriers");
        JSONObject element;

        for (int i = 0; i < couriersJSON.length(); i++) {
            element = couriersJSON.getJSONObject(i);

            Courier newCourier = new Courier(element);
            couriers.add(newCourier);
        }
        return couriers;
    }


    /**
     * make a request to the HTTP API of Aftership
     *
     * @param method String with the method of the request: GET, POST, PUT, DELETE
     * @param url String with the URL of the request
     * @param body JSONObject with the body of the request, if the request doesn't need body "GET/DELETE", the body
     *             would be null
     * @return  A JSONObject with the response of the request
     * @throws  AftershipAPIException  If the request response an error
     * @throws  java.io.IOException If there is a problem with the connection
     * @throws  java.text.ParseException    If the response can not be parse to JSONObject
     **/
    public JSONObject request(String method, String url, JSONObject body)
            throws AftershipAPIException,IOException,ParseException,JSONException{
        BufferedReader rd;
        StringBuilder sb;
        OutputStreamWriter wr;

        HttpURLConnection connection;
        URL serverAddress= new URL(new URL(URL_SERVER),VERSION_API+ url);
        connection= (HttpURLConnection)serverAddress.openConnection();
        connection.setRequestMethod(method);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type","application/json");
        connection.setRequestProperty("aftership-api-key", keyAPI);
        if(body!=null){ connection.setDoOutput(true);}//if there is information in body, doOutput true, to write

        connection.connect();
        if(body!=null){
            wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(body.toString());
            wr.flush();
        }

        this.checkAPIResponse(connection.getResponseCode(),connection);
        rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null)
        {
            sb.append(line + '\n');
        }
        JSONObject response;
        response  = new JSONObject(sb.toString());

        return response;
    }

    /**
     * Check the status of a http response and if the status is an error throws an exception
     *
     * @param status Status of the connection response
     * @exception AftershipAPIException A customize exception with a different message
     * depending of the status error
     **/
    public void checkAPIResponse(int status,HttpURLConnection connection)throws AftershipAPIException,IOException,ParseException,JSONException{
        BufferedReader rd;
        StringBuilder sb;
        String message = "";

        if (status>201)
        {
            rd  = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null)
            {
                sb.append(line + '\n');
            }
            JSONObject response = new JSONObject(sb.toString());
            message = response.getJSONObject("meta").getString("error_message");
        }

        switch (status){
            case 200:
            case 201:
                break;
            case 400:
                throw new AftershipAPIException("Invalid JSON data. "+message);
            case 401:
                throw new AftershipAPIException("InvalidCredentials. "+message);
            case 402:
                throw new AftershipAPIException("Request Failed. "+message);
            case 404:
                throw new AftershipAPIException("ResourceNotFound. "+message);
            case 409:
                throw new AftershipAPIException("InvalidArgument. "+message);
            case 500:
            case 502:
            case 503:
            case 504:
                throw new AftershipAPIException("Server errors - something went wrong on AfterShip's end. "+message);

        }

    }

    public void prettyPrintJSON(JSONObject oldJSON){
        try {
            JSONTokener tokener = new JSONTokener(oldJSON.toString()); //tokenize the ugly JSON string
            JSONObject finalResult = new JSONObject(tokener); // convert it to JSON object
            System.out.println(finalResult.toString(4)); // To string method prints it with specified indentation.
        }catch( Exception e){
            System.out.println("exception printing pretty JSON: "+e.getMessage() );
        }
    }

    public Object getReturn(){
        switch (this.method.getNumberMethod()) {
            case 0://getLastCheckpoint
                return checkpointReturn;
            case 1: //reactivate
                return confirmationReturn;
            case 2://getTrackingByNumber
                return this.trackingReturn;
            case 3://getTracking
                return trackingsReturn;
            case 4://deleteTracking
                return this.confirmationReturn;
            case 5://postTracking
                return this.trackingReturn;
            case 6://putTracking
                return this.trackingReturn;
            case 7://getCouriers(7)
                return this.couriersReturn;
            case 8://detectCouriers(8)
                return this.couriersReturn;
            case 9://getTrackingsNext(9)
                return this.trackingsReturn;
            default:
                return null;

        }
    }

    public Exception getException() {
        return exception;
    }

    public ConnectionAPIMethods getMethod() {
        return method;
    }
}
