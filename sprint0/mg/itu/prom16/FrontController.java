package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet 
{
    private boolean isScanned; 
    private List<Class<?>> classes;
    private String basePackageName;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        isScanned = false;
        classes = new ArrayList<Class<?>>();
        basePackageName = config.getInitParameter("basePackage");
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
    {
        processRequest(req,res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
    {
        processRequest(req,res);
    }

    protected void initVariable() throws Exception
    {
        try{
            classes = ClassScanner.getControllers(basePackageName, Controller.class);
            isScanned = true;
        }
        catch(Exception e){
            isScanned = false;
            throw new ServletException(e);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException 
    {
        //Print url
        PrintWriter out = res.getWriter(); 
        out.println("Bienvenue "+req.getRequestURL().toString());

        //Print controllers
        try {
            System.out.println("Le nom du package : " + basePackageName);

            if (!isScanned) {
                initVariable();
            }

            out.println("Liste des controllers : ");

            out.println("<ul>");
            for (Class<?> class1 : classes) {
                out.println("<li>" + class1.getSimpleName() + "</li>");
            }
            out.println("</ul>");
        } 
        
        catch (Exception e) {
            out.println("Exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}

