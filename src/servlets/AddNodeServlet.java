package servlets;

import dao.NodeDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/addNode")
public class AddNodeServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String id = req.getParameter("id");
            String name = req.getParameter("name");
            String type = req.getParameter("type");
            double val1 = Double.parseDouble(req.getParameter("val1"));
            double val2 = Double.parseDouble(req.getParameter("val2"));
            new NodeDAO().insertNode(id, name, type, val1, val2);
            resp.sendRedirect("network.jsp?msg=Node+added");
        }          catch (Exception e) {
            String safeMsg = java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            resp.sendRedirect("network.jsp?msg=Error:" + safeMsg);
        }
    }
}