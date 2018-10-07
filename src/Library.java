import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;

public class Library {


    private String host;
    private int port;

    /*use specific data structure to store required information*/
    private HashMap<String,String> header = new HashMap<>();
    private String body;
    private HashMap<String,String> query = new HashMap<>();

    public String GET(String str) throws MalformedURLException{

        /*to get host and port from given URL*/
        String url = str.substring(str.indexOf("http://"), str.indexOf("'",str.indexOf("http://")));
        URL u = new URL(url);
        host = u.getHost();
        port = u.getDefaultPort();

        if(str.contains("?")) {
            queryParameters(u);//handle query parameters
        }

        String response = null;

        try {

            /*open a socket*/
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress);

            /*send http request*/
            sendRequest("GET",socket,str,u,url);

            /*receive http response*/
            response = receiveResponse("GET",socket,str,u,url);
            if(response.contains("HTTP/1.0")||response.contains("HTTP/1.1")||response.contains("HTTP/2.0")){
                if(needRedirection(response)){//check if this request needs to be redirected or not
                    //ex:  httpc get 'http:httpbin.org/redirect-to?url=http://httpbin.org/get?course=networking&assignment=1&status_code=200'
                    sendRequest(response,socket,str,u,query.get("url"));
                    response = receiveResponse("POST",socket,str,u,query.get("url"));
                }
            }

            /*close a socket*/
            socket.close();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }

    public String POST(String str) throws MalformedURLException{

        /*to get host and port from given URL*/
        String url = str.substring(str.indexOf("http://"), str.indexOf("'",str.indexOf("http://")));
        URL u = new URL(url);
        host = u.getHost();
        port = u.getDefaultPort();

        if(str.contains("?")) {
            queryParameters(u);//handle query parameters
        }

        String response = null;

        try {
            /*open a socket*/
            Socket socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(host, port);
            socket.connect(socketAddress);

            /*send http request*/
            sendRequest("POST",socket,str,u,url);

            /*receive http response*/
            response = receiveResponse("POST",socket,str,u,url);
            if(response.contains("HTTP/1.0")||response.contains("HTTP/1.1")||response.contains("HTTP/2.0")){
                if(needRedirection(response)){//check if this request needs to be redirected or not
                    //ex:  httpc post 'http:httpbin.org/redirect-to?url=http://httpbin.org/get?course=networking&assignment=1&status_code=200'
                    sendRequest(response,socket,str,u,query.get("url"));
                    response = receiveResponse("POST",socket,str,u,query.get("url"));
                }
            }

            /*close a socket*/
            socket.close();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            return response;
        }
    }

    public void queryParameters(URL u){
        String queryLine = u.getQuery();
        String [] pair = queryLine.split("&");
        for (String s:pair) {
            String [] rest = s.split("=");
            query.put(rest[0],rest[1]);
        }
    }

    /*allow :get/post method ; str: whole command line input ; url: only the URL part*/
    public void sendRequest(String allow,Socket socket,String str,URL u,String url) throws IOException{

        /*default information setting(Notice : not all of http header field definitions are defined,only those that appeared in the assignment are defined)*/
        String connectionType = "close";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36";
        String contentType = null;
        String contentLength = null;
        if(allow.equals("POST")) {//these options are only valid under POST request
            contentType = "text/plain";
            if(str.contains("-d")) {
                body = str.substring(str.indexOf("{", str.indexOf("-d")), str.indexOf("}") + 1);
            }else if(str.contains("-f")){
                String path = str.substring(str.indexOf("-f") + 3,str.indexOf(" ",str.indexOf("-f") + 3));
                body = readFile(path);
            }
            contentLength = String.valueOf(body.length());
        }

        /*initial headers to hash-map*/
        header.put("Host",this.host);
        header.put("Connection",connectionType);
        header.put("User-Agent",userAgent);
        if(allow.equals("POST")) {
            header.put("Content-Length", contentLength);
            header.put("Content-Type", contentType);
        }


        /*-h Header requirement:support multiple headers add or update*/
        String temp = str;
        String key;
        String value;
        for (int i = 0; i < str.length(); i++) {
            if(!temp.contains("-h")){
                break;
            }else{
                i = temp.indexOf("-h") + 3;
                temp = temp.substring(i);
                key = temp.substring(0,temp.indexOf(":",0));
                value = temp.substring(temp.indexOf(":",temp.indexOf(key))+ 1,temp.indexOf(" ",temp.indexOf(key)));
                if(header.containsKey(key)){
                    header.replace(key,value);
                    continue;
                }
                header.put(key,value);
            }
        }

        /*sending the request*/
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
        BufferedWriter bufferedWriter= new BufferedWriter(outputStreamWriter);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(allow + " " + url + " HTTP/1.0\r\n");
        for (String keys:header.keySet()) {
            stringBuilder.append(keys).append(": ").append(header.get(keys)).append("\r\n");
        }
        if(allow.equals("POST")){
            stringBuilder.append("\r\n").append(body);
        }else if(allow.equals("GET")){
            stringBuilder.append("\r\n");
        }
        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.flush();
    }

    /*str: whole command line input*/
    public String receiveResponse(String allow,Socket socket,String str,URL u,String url) throws IOException{
        InputStream inputStream = socket.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String data;
        do {
            data = bufferedReader.readLine();
            stringBuilder.append(data+"\r\n");
        }
        while (data != null);
        String response = stringBuilder.toString();
        bufferedReader.close();
        inputStream.close();

        /*verbose requirement*/
        if(str.contains("-v")) {//case that needs verbose
            if(needOutputFile(str)){//case need to output body data
                outputFile(response,str.substring(str.indexOf("-o") + 3));
            }
            return response;
        }else {//case that does not need verbose
            response = response.substring(response.indexOf("{"),response.lastIndexOf("}")+ 1);
            if(needOutputFile(str)){//case need to output body data
                outputFile(response,str.substring(str.indexOf("-o") + 3));
            }
        }
        return response;
    }


    /*-f read body data from a file*/
    public String readFile(String path) throws IOException{
        File file = new File(path);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String nextLine;
        do{
            nextLine = bufferedReader.readLine();
            stringBuilder.append(nextLine);
        }while (nextLine != null);
        return stringBuilder.toString();
    }

    /*-o file output option*/
    public void outputFile(String body,String filePath){
        File file = new File(filePath);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(body);
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public boolean needOutputFile(String str){
        if(str.contains("-o")){
            return true;
        }
        return false;
    }


    /*method to determine if the http response needs a redirection or not */
    public boolean needRedirection(String data){
        data = data.substring(0,20);//this is due to status will always be the first line, 0-20 characters for approximation of it.
        if(data.contains("300")||data.contains("301")||data.contains("302")||data.contains("304")){//satisfy any of those will need redirect
            return true;
        }
        return false;
    }
}
