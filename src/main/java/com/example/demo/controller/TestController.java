package com.example.demo.controller;

import com.example.demo.common.Method;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

@Controller
@RequestMapping("/mock")
public class TestController {
    @Autowired
    private RestTemplate restTemplate;

    private static final String PLACE_HOLDER = "__PH__";

    @GetMapping("/**")
    public ResponseEntity<String> mockGetResponse(HttpServletRequest request) throws IOException {
        return handleRequest(request, Method.GET);
    }

    @PostMapping("/**")
    public ResponseEntity<String> mockPostResponse(HttpServletRequest request) throws IOException {
        return handleRequest(request, Method.POST);
    }

    private static ResponseEntity<String> handleRequest(HttpServletRequest request, Method method) throws IOException {
        StringBuffer mockFolderPathBuff = new StringBuffer("mocks");
        File mockFolder = new File(mockFolderPathBuff.toString());
        System.out.println(">>>>>" + mockFolder.exists());

        String fullUrl = request.getRequestURL().toString();

        // You can also get the query string if needed
        String queryString = request.getQueryString();
        if (queryString != null) {
            fullUrl += "?" + queryString;
        }


        String requestURI = request.getRequestURI();
        requestURI = requestURI.replace("/mock/", "");
        if(requestURI.endsWith("/")) {
            requestURI = requestURI.substring(0, requestURI.length() - 1);
        }
        System.out.println(">>>>>" + requestURI);
        String[] subFolders = requestURI.split("/");
        File curFolderPath = null;
        boolean pathExists = true;
        for(String folderName: subFolders) {
            System.out.println("folderName:" + folderName);
            mockFolderPathBuff.append(File.separatorChar).append(folderName);
            curFolderPath = new File(mockFolderPathBuff.toString());
            System.out.println(mockFolderPathBuff + ":" + curFolderPath.exists());

            // If current folder path doesn't exists, check for _PH_ folder.
            if(!curFolderPath.exists()) {
                System.out.println("Check for PH folder");
                //sb.delete(sb.length() - n, sb.length());
                mockFolderPathBuff.delete(mockFolderPathBuff.length() - folderName.length(), mockFolderPathBuff.length());
                System.out.println("After removal:" + mockFolderPathBuff);
                mockFolderPathBuff.append(PLACE_HOLDER);
                curFolderPath = new File(mockFolderPathBuff.toString());
                System.out.println(mockFolderPathBuff + ":" + curFolderPath.exists());

                pathExists = curFolderPath.exists();
                if(!pathExists) {
                    break;
                }
            }
        }

        if(!pathExists) {
            return new ResponseEntity<String>(fullUrl + " not found.", HttpStatusCode.valueOf(404));
        }

        String finalPath = mockFolderPathBuff.toString();
        File jsFile = new File(finalPath + File.separatorChar + method.name() + ".js");
        File responseFile = new File(finalPath + File.separatorChar + method.name() + ".json");

        System.out.println(">>>>> " + jsFile.getAbsolutePath());
        System.out.println(">>>>> " + responseFile.getAbsolutePath());
        if(!jsFile.exists() && !responseFile.exists()) {
            System.out.println("Response file does not exits.");
            return new ResponseEntity<String>(fullUrl + method.name() + ".json not found.", HttpStatusCode.valueOf(404));
        }

        if(jsFile.exists()) {
            String jsContent = Files.readString(jsFile.toPath());

            try (Context context = Context.create()) {
                context.getBindings("js").putMember("name", "World");
                Value result = context.eval("js", "var greeting = 'Hello, ' + name; greeting;");
                System.out.println(result.asString());
            }

            return new ResponseEntity<>(jsContent, HttpStatusCode.valueOf(200));
        } else {
            String jsonContent = Files.readString(responseFile.toPath());
            HttpHeaders headers = new HttpHeaders();
            String contentType = request.getContentType();
            System.out.println(">>>>> contentType: " + contentType);
            headers.setContentType(MediaType.APPLICATION_JSON);

            return new ResponseEntity<>(jsonContent, headers, HttpStatusCode.valueOf(200));
        }
    }

    @GetMapping("/test1111111")
    public ResponseEntity<String> testCall() {
        System.out.println("Calling api");
        ResponseEntity<String>  response = null;
        try
        {
            //response = restTemplate.getForEntity("http://localhost:9995/test", String.class);
//            System.out.println(">>>> " + response);
//            System.out.println(">>>> " + response.getBody());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ResponseEntity<String>(response.getBody(), HttpStatusCode.valueOf(200));
        //return response.getBody();
    }
}
