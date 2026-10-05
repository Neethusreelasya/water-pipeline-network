package servlets;

import core.LeakManager;
import dao.LeakDAO;
import model.LeakReport;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/criticalLeaks")
public class CriticalLeakServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, jakarta.servlet.ServletException {
        try {
            List<Map<String, Object>> raw = new LeakDAO().getAllUnresolvedLeaks();
            LeakManager manager = new LeakManager();
            for (Map<String, Object> row : raw) {
                manager.reportLeak(new LeakReport(
                    (int) row.get("id"),
                    (String) row.get("area_id"),
                    (int) row.get("severity"),
                    (String) row.get("description"),
                    (long) row.get("reported_timestamp")
                ));
            }
            req.setAttribute("leakList", manager.listAllPending());
            RequestDispatcher rd = req.getRequestDispatcher("leaks.jsp");
            rd.forward(req, resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}