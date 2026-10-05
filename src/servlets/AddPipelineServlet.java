package servlets;

import dao.PipelineDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/addPipeline")
public class AddPipelineServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String fromId = req.getParameter("fromId");
            String toId = req.getParameter("toId");
            double weight = Double.parseDouble(req.getParameter("weight"));
            new PipelineDAO().insertPipeline(fromId, toId, weight);
            resp.sendRedirect("network.jsp?msg=Pipeline+added");
               } catch (Exception e) {
            String safeMsg = java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            resp.sendRedirect("network.jsp?msg=Error:" + safeMsg);
        }
    }
}