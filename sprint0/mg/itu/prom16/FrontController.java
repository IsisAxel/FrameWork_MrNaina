package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mg.itu.prom16.ClassScanner;
import mg.itu.prom16.Controller;
import mg.itu.prom16.Mapping;
import mg.itu.prom16.ModelView;

public class FrontController extends HttpServlet {

    Map<String,Mapping> controllerList;
    Class<? extends Annotation > annClass=Controller.class;
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            controllerList=ClassScanner.getMapping(getInitParameter("basePackage"), annClass);
        } catch (Exception e) {
            throw new Error("Duplicate URL",e);
        }
    }
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            response.setContentType("text/html");

            try (PrintWriter out = response.getWriter()) {
                out.println("<html><body>");
                out.println("<h1>Servlet Path: " + request.getServletPath() + "</h1>");
                String path = request.getServletPath().trim();
                Mapping map = controllerList.get(path);
                if (map!=null) {
                    Object valueFunction = map.invoke(request);
                    if (valueFunction instanceof ModelView) {

                        ModelView modelAndView = (ModelView)valueFunction;
        
                        String nameView = modelAndView.getViewName();
                        HashMap<String, Object> listKeyAndValue = modelAndView.getData();
        
                        for (Map.Entry<String, Object> maap : listKeyAndValue.entrySet()) {
                            request.setAttribute(maap.getKey(),  maap.getValue());
                        }

                        RequestDispatcher dispatcher = request.getRequestDispatcher(nameView);
                        dispatcher.forward(request, response);
                    } else if (valueFunction instanceof String) {
                        out.println("Nom de la metode : "+map.getMethod().getName()+"<br>Nom de la Classe : "+map.getControlleClass().getSimpleName()+"<br>"); 
                        out.println("Invocation de la methode : "+valueFunction);
                    } else {
                        throw new Error(new ServletException("Unsupported return type"));
                    }
                }
                else{
                    response.sendError(404);   
                }
                
                
                out.println("</body></html>");
            }
        } catch (Exception e) {
            throw new ServletException(e);    
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
}
