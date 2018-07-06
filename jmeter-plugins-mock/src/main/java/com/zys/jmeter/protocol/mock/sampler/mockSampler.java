package com.zys.jmeter.protocol.mock.sampler;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by zhuyongsheng on 2018/7/5.
 */
public class mockSampler extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(mockSampler.class);

    private String server;
    private String request;
    private String response;
    private int timeout;

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSamplerData(request);
        result.setDataType("text");
        result.sampleStart();
        try {
            result.setResponseData(excute(), "utf8");
            result.setSuccessful(true);
            result.setResponseCode("0");
            result.setResponseMessage("OK");
        } catch (Exception e) {
            e.printStackTrace();
            result.setResponseData(e.toString(), "utf8");
            result.setSuccessful(false);
            result.setResponseCode("500");
            result.setResponseMessage("KO");
        }
        result.sampleEnd();
        return result;
    }

    private String excute() throws Exception{
        Socket socket = ((ServerSocket)getProperty(server).getObjectValue()).accept();
        String[] requestArray = request.split("\n");
        String recvRequest = recvRequest(socket);
        for (String requestLine : requestArray){
            if (!recvRequest.contains(requestLine)){
                return "request not match.";
            }
        }
        return doResponse(socket);
    }

    private String recvRequest(Socket socket) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestHeaderLine;
        StringBuffer requestHeaders = new StringBuffer();
        long beginTime = System.currentTimeMillis();
        while ((requestHeaderLine = in.readLine()) != null && !requestHeaderLine.isEmpty() && System.currentTimeMillis() - beginTime < timeout) {
            requestHeaders.append(requestHeaderLine);
        }
        String requestHeaderString = requestHeaders.toString();
        if (requestHeaderString.startsWith("POST")){
            return requestHeaderString + getPostParas(in, Integer.parseInt(requestHeaderString.substring(requestHeaderString.indexOf("Content-Length:"),requestHeaderString.indexOf("\n")).trim()));
        }
        return requestHeaderString;
    }

    private String doResponse(Socket socket) throws IOException {

        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.println(response);
        pw.flush();
        socket.close();
        return response;
    }


    private String getPostParas(BufferedReader bd, int length) {
        StringBuffer paras = new StringBuffer();
        for (int i = 0; i < length; i++) {
            try {
                paras.append((char) bd.read());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return paras.toString();
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }



}
