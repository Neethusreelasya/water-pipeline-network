package servlets;

import core.WaterNetworkGraph;
import dao.PipelineDAO;
import model.Pipeline;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/findRoute")
public class FindRouteServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, jakarta.servlet.ServletException {
        try {
            String sourceId = req.getParameter("sourceId");
            String targetId = req.getParameter("targetId");

            WaterNetworkGraph graph = new WaterNetworkGraph();
            List<Map<String, Object>> pipelines = new PipelineDAO().getAllPipelines();
            for (Map<String, Object> row : pipelines) {
                graph.addPipeline(new Pipeline(
                    (String) row.get("from_id"),
                    (String) row.get("to_id"),
                    (double) row.get("weight")
                ));
            }

            List<String> path = graph.findShortestRoute(sourceId, targetId);
            req.setAttribute("path", path);
            RequestDispatcher rd = req.getRequestDispatcher("route.jsp");
            rd.forward(req, resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}