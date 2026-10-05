package servlets;

import dao.LeakDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/reportLeak")
public class ReportLeakServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String areaId = req.getParameter("areaId");
            int severity = Integer.parseInt(req.getParameter("severity"));
            String description = req.getParameter("description");
            new LeakDAO().insertLeak(areaId, severity, description, System.currentTimeMillis());
            resp.sendRedirect("leaks.jsp?msg=Leak+reported");
        } catch (Exception e) {
            String safeMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect("leaks.jsp?msg=Error:" + safeMsg);
        }
    }
}